package com.craftinginterpretor.proflang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpretor.proflang.TokenType.*;

public class Scanner {
    //string storing the raw source code
    private final String source;

    //list of tokens 
    private final List<Token> tokens = new ArrayList<Token>();          // - I added Token in ArrayList<Type> from myself
    private int start = 0;
    private int current = 0;
    private int line = 1;

    
    //constructor initializing the source stiring
    Scanner(String source) {
      this.source = source;
    }

    //function that iterates over the tokens
    List<Token> scanTokens()
    {
        while (!isAtEnd()){
          // We are at the beginning of the next lexeme.
          start = current;
          scanToken();
        }
        
        // End of line token
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    //function that scans the single character tokens
    private void scanToken() {
        char c = advance();

        switch (c) {
            case '('    :   addToken(LEFT_PAREN);
                            break;
            case ')'    :   addToken(RIGHT_PAREN);
                            break;
            case '{'    :   addToken(LEFT_BRACE);
                            break;
            case '}'    :   addToken(RIGHT_BRACE);
                            break;
            case ','    :   addToken(COMMA);
                            break;
            case '.'    :   addToken(DOT);
                            break;
            case '-'    :   addToken(MINUS);
                            break;
            case '+'    :   addToken(PLUS);
                            break;
            case ';'    :   addToken(SEMICOLON);
                            break;
            case '*'    :   addToken(STAR);
                            break; 
            //possible 2-character lexemes
            case '!'    :   addToken(match('=') ? BANG_EQUAL : BANG);
                            break;
            case '='    :   addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                            break;
            case '<'    :   addToken(match('=') ? LESS_EQUAL : LESS);
                            break;
            case '>'    :   addToken(match('=') ? GREATER_EQUAL : GREATER);
                            break;
            case '/'    :   if (match('/')) {
                                //A commment goes until the end of the line.
                                while (peek() != '\n' && !isAtEnd()) advance();
                            } 
                            else {
                                addToken(SLASH);
                            }
                            break;
            //ignoring white spaces and new lines
            case ' '    :
            case '\r'   :
            case '\t'   :
                            break;
            case '\n'   :   line++;
                            break;
            //string time
            case '"'    :   string(); 
                            break;
            
            default     :   if (isDigit(c)) {
                                number();
                            } 
                            else if (isAlpha(c)) {
                                identifier();
                            }                  
                            else {
                                prof_lang.error(line, "Unexpected character.");
                            }
                            break;
  
        }
    }

    private static final Map<String, TokenType> keywords = new HashMap<>();

    //Hash map for the reserved keywords of proflang
    static {
      keywords.put("and",    AND);
      keywords.put("class",  CLASS);
      keywords.put("else",   ELSE);
      keywords.put("false",  FALSE);
      keywords.put("for",    FOR);
      keywords.put("fun",    FUN);
      keywords.put("if",     IF);
      keywords.put("nil",    NIL);
      keywords.put("or",     OR);
      keywords.put("print",  PRINT);
      keywords.put("return", RETURN);
      keywords.put("super",  SUPER);
      keywords.put("this",   THIS);
      keywords.put("true",   TRUE);
      keywords.put("var",    VAR);
      keywords.put("while",  WHILE);
    }
    
    //boolean function that tells of we are at the end of the source file
    private boolean isAtEnd() {
        return current >= source.length();
    }

    //method returning the next character from the source file
    private char advance() {
        return source.charAt(current++);
    }

    //overloaded method calling it's overloaded method to add token to the token list
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    
    //overloaded method adding token to the token list
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    //method to match the 2-character lexeme
    private boolean match(char expected) {
        //if at the end of the file then false
        if (isAtEnd()) 
            return false;
        //else if the expected character is not there then false
        if (source.charAt(current) != expected) 
            return false;
        
        //else if the expected character is there then increment current index
        current++;
        return true;
    }

    //method that checks if we are at the end of the line
    private char peek() {
        if (isAtEnd()) 
            return '\0';
        return source.charAt(current);
    }

    //method that returns the next character that will be read
    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    } 

    //method that adds the string token to the token list
    private void string() {
        //until don't get another " and not at end of file
        while (peek() != '"' && !isAtEnd()) {
            //if got a new line then increment line
            if (peek() == '\n') 
                line++;
            //move to the next character
            advance();
        }
        //if at the end of the file then throw error
        if (isAtEnd()) {
          prof_lang.error(line, "Unterminated string.");
          return;
        }
    
        // The closing ".
        advance();
    
        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    //method checks if the input is a number
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    //method that deal with numbers
    private void number() {
        //until we getting number keep reading next character
        while(isDigit(peek())) 
            advance();
    
        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
          // Consume the "."
          advance();
        //check for numbers after the decimal
        while (isDigit(peek())) 
            advance();
        }
        
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    //method checks if the input is an alphabet (considering bot upper and lower case)
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }
    
    //method checks if the input is alphanumeric
      private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    //method that checks if the input lexeme is an identifier
    private void identifier() {
        while (isAlphaNumeric(peek())) 
            advance();
        
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
      
        if (type == null) 
            type = IDENTIFIER;
        
        addToken(type);
    }
}