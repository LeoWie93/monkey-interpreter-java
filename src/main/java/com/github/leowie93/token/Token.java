package com.github.leowie93.token;

public class Token {

    private final TokenType tokenType;
    private final String literal;

    public Token(TokenType tokenType, String literal) {
        this.tokenType = tokenType;
        this.literal = literal;
    }

    public Token(TokenType tokenType, char ch) {
        this.tokenType = tokenType;
        this.literal = String.valueOf(ch);
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getLiteral() {
        return literal;
    }
}
