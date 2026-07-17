package com.github.leowie93.parser;

// precedence is defined by the ordenality
public enum ParserPrecedence {
    LOWEST,
    EQUALS, // ==
    LESSGREATER, // > or <
    SUM, // +
    PRODUCT, // *
    PREFIX, // -x or !x
    CALL, // func(x)
}
