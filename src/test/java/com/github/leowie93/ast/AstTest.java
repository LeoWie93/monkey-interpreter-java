package com.github.leowie93.ast;

import com.github.leowie93.ast.Expression.IdentifierExpression;
import com.github.leowie93.ast.Statement.LetStatement;
import com.github.leowie93.token.Token;
import com.github.leowie93.token.TokenType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AstTest {

    @Test
    public void TestString() {
        Program program = new Program(new ArrayList<>(List.of(
                new LetStatement(
                        new Token(TokenType.LET, "let"),
                        new IdentifierExpression(
                                new Token(TokenType.IDENT, "myVar"),
                                "myVar"
                        ),
                        //currently assigning one variable to another
                        new IdentifierExpression(
                                new Token(TokenType.IDENT, "anotherVar"),
                                "anotherVar"
                        )
                )
        )));

        String output = program.nodeToString();
        assertEquals("let myVar = anotherVar;", output);
    }
}
