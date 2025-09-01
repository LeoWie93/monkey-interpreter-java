package com.github.leowie93.token;

public enum TokenType {
    ILLEGAL("ILLEGAL"),
    EOF("EOF"),
    IDENT("IDENT"),
    INT("INT"),

    ASSIGN("="),
    PLUS("+"),
    MINUS("-"),
    BANG("!"),
    ASTERISK("*"),
    SLASH("/"),

    GT(">"),
    LT("<"),

    COMMA("COMMA"),
    SEMICOLON("SEMICOLON"),

    EQUAL("=="),
    NEQUAL("!="),

    LBRACE("{"),
    RBRACE("}"),
    LPAREN("("),
    RPAREN(")"),

    FUNCTION("FUNCTION"),
    LET("LET"),
    TRUE("TRUE"),
    FALSE("FALSE"),
    IF("IF"),
    ELSE("ELSE"),
    RETURN("RETURN");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static TokenType lookupIdentifier(String identifier) {
        return switch (identifier) {
            case "let" -> LET;
            case "fn" -> FUNCTION;
            case "if" -> IF;
            case "else" -> ELSE;
            case "return" -> RETURN;
            case "true" -> TRUE;
            case "false" -> FALSE;
            default -> IDENT;
        };
    }
}
