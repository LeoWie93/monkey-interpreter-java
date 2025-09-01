package com.github.leowie93.lexer;

import com.github.leowie93.token.Token;
import com.github.leowie93.token.TokenType;

public class Lexer {
    private final String input;
    private int position;
    private int readPosition;
    private char ch;

    public Lexer(String input) {
        this.input = input;
        this.readChar();
    }

    public Token nextToken() {
        this.eatWhitespaces();
        Token token;

        switch (this.ch) {
            case '!' -> {
                if (this.peakChar() == '=') {
                    this.readChar();
                    token = new Token(TokenType.NEQUAL, "!=");
                } else {
                    token = new Token(TokenType.BANG, this.ch);
                }
            }
            case '=' -> {
                if (this.peakChar() == '=') {
                    this.readChar();
                    token = new Token(TokenType.EQUAL, "==");
                } else {
                    token = new Token(TokenType.ASSIGN, this.ch);
                }
            }
            case ';' -> token = new Token(TokenType.SEMICOLON, this.ch);
            case ',' -> token = new Token(TokenType.COMMA, this.ch);
            case '+' -> token = new Token(TokenType.PLUS, this.ch);
            case '-' -> token = new Token(TokenType.MINUS, this.ch);
            case '*' -> token = new Token(TokenType.ASTERISK, this.ch);
            case '/' -> token = new Token(TokenType.SLASH, this.ch);
            case '>' -> token = new Token(TokenType.GT, this.ch);
            case '<' -> token = new Token(TokenType.LT, this.ch);
            case '(' -> token = new Token(TokenType.LPAREN, this.ch);
            case ')' -> token = new Token(TokenType.RPAREN, this.ch);
            case '{' -> token = new Token(TokenType.LBRACE, this.ch);
            case '}' -> token = new Token(TokenType.RBRACE, this.ch);
            case 0 -> token = new Token(TokenType.EOF, "");
            default -> {
                if (isLetter(this.ch)) {
                    String literal = this.readIdentifier();
                    return new Token(TokenType.lookupIdentifier(literal), literal);
                } else if (isDigit(this.ch)) {
                    String literal = this.readDigit();
                    return new Token(TokenType.INT, literal);
                } else {
                    System.out.println("ILLEAGAL");
                    return new Token(TokenType.ILLEGAL, this.ch);
                }
            }
        }

        this.readChar();
        return token;
    }


    String readDigit() {
        int startPos = this.position;
        while (isDigit(this.ch)) {
            this.readChar();
        }

        return this.input.substring(startPos, this.position);
    }

    String readIdentifier() {
        int startPos = this.position;
        while (isLetter(this.ch)) {
            this.readChar();
        }

        return this.input.substring(startPos, this.position);
    }

    private boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

    boolean isLetter(char ch) {
        return 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || '_' == ch;
    }

    private void eatWhitespaces() {
        while (Character.isWhitespace(this.ch)) {
            this.readChar();
        }
    }

    public char peakChar() {
        if (this.readPosition >= this.input.length()) {
            return 0;
        }

        return this.input.charAt(this.readPosition);
    }

    public void readChar() {
        if (this.readPosition >= this.input.length()) {
            this.ch = 0;
        } else {
            this.ch = this.input.charAt(this.readPosition);
        }

        this.position = this.readPosition;
        this.readPosition++;
    }
}
