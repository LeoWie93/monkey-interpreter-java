package com.github.leowie93.lexer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LexerTest {

    @Test
    public void isLetterTest() {
        char test = 'a';

        Lexer lexer = new Lexer("a");
        assertTrue(lexer.isLetter(test));
        assertFalse(lexer.isLetter(';'));
    }
}
