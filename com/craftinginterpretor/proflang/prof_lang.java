package com.craftinginterpretor.proflang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class prof_lang
{
    // bool variable as an index to identify if there is error in the code
    static boolean hadError = false;

    //function that executes the code
    private static void run(String source)
    {
        Scanner scanner = new Scanner(source);
        
        List<Token> tokens = scanner.scanTokens();
    
        // For now, just print the tokens.
        for (Token token : tokens) {
          System.out.println(token);
        }

    }
    
    //executes code from path to the source file is given
    private static void runFile(String path) throws IOException 
    {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        
        // Indicate an error in the exit code.
        if (hadError)
            System.exit(65);
    }
    
    //executes one line at a time in the prompt window
    private static void runPrompt() throws IOException 
    {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
    
        for (;;) 
        { 
            System.out.print(">> ");
            String line = reader.readLine();
            if (line == null)
                break;
            run(line);
            hadError = false;
        }
    }

    //------------------ error handler ------------------//
    
    //error method - returning line number in which the error occured and the error message
    static void error(int line, String message) {
        report(line, "", message);
    }
    
    //report method - returning line number in which the error occured and the error message
    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
   
    public static void main(String[] args) throws IOException 
    {   
        //if argument overflow in string args[] then exit
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }
        //else if given proper then read the file from source
        else if (args.length == 1) {
            runFile(args[0]);
        }
        //else if string args[] is empty read file line by line
        else {
            runPrompt();
        }
    }
}
