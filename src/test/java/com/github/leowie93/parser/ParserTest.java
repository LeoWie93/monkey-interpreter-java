package com.github.leowie93.parser;

import com.github.leowie93.ast.Expression.*;
import com.github.leowie93.ast.Program;
import com.github.leowie93.ast.Statement.ExpressionStatement;
import com.github.leowie93.ast.Statement.LetStatement;
import com.github.leowie93.ast.Statement.ReturnStatement;
import com.github.leowie93.ast.Statement.Statement;
import com.github.leowie93.lexer.Lexer;
import com.github.leowie93.token.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//The tests are construct to assert at every step and will early exit
public class ParserTest {

    @Test
    void testOperatorPrecedenceParsing() {
        PrecedenceTest[] tests = {
                new PrecedenceTest("-a * b", "((-a) * b)"),
                new PrecedenceTest("!-a", "(!(-a))"),
                new PrecedenceTest("-a + -b * 2", "((-a) + ((-b) * 2))"),
                new PrecedenceTest("a + b + c", "((a + b) + c)"),
                new PrecedenceTest("a + b - c", "((a + b) - c)"),
                new PrecedenceTest("a * b * c", "((a * b) * c)"),
                new PrecedenceTest("a * b / c", "((a * b) / c)"),
                new PrecedenceTest("a + b / c", "(a + (b / c))"),
                new PrecedenceTest("a + b * c + d / e - f", "(((a + (b * c)) + (d / e)) - f)"),
                new PrecedenceTest("3 + 4; -5 * 5", "(3 + 4)((-5) * 5)"),
                new PrecedenceTest("5 > 4 == 3 < 4", "((5 > 4) == (3 < 4))"),
                new PrecedenceTest("5 < 4 != 3 > 4", "((5 < 4) != (3 > 4))"),
                new PrecedenceTest("3 + 4 * 5 == 3 * 1 + 4 * 5", "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))"),
                new PrecedenceTest("true", "true"),
                new PrecedenceTest("3 > 5 == false", "((3 > 5) == false)"),
                new PrecedenceTest("3 < 5 == true", "((3 < 5) == true)"),
                new PrecedenceTest("a * (b + c)", "(a * (b + c))"),
                new PrecedenceTest("(b + c) * a", "((b + c) * a)")
        };

        for (PrecedenceTest precedenceTest : tests) {
            Lexer l = new Lexer(precedenceTest.input);
            Parser p = new Parser(l);
            Program program = p.parseProgram();
            checkParseErrors(p);

            String actual = program.nodeToString();
            assertEquals(precedenceTest.expected, actual, String.format("Expected=%s, got=%s", precedenceTest.expected, actual));
        }
    }

    @Test
    public void testInfixExpression() {
        List<InfixTest<?>> infixTests = List.of(
                new InfixTest<Integer>("5 + 5", 5, "+", 5),
                new InfixTest<Integer>("5 - 5", 5, "-", 5),
                new InfixTest<Integer>("5 * 5", 5, "*", 5),
                new InfixTest<Integer>("5 / 5", 5, "/", 5),
                new InfixTest<Integer>("5 < 5", 5, "<", 5),
                new InfixTest<Integer>("5 > 5", 5, ">", 5),
                new InfixTest<Integer>("5 == 5", 5, "==", 5),
                new InfixTest<Integer>("5 != 5", 5, "!=", 5),
                new InfixTest<Boolean>("true == true", true, "==", true)
        );

        for (var infixTest : infixTests) {
            Lexer l = new Lexer(infixTest.getInput());
            Parser p = new Parser(l);
            Program program = p.parseProgram();

            checkParseErrors(p);

            assertEquals(1, program.getStatements().size());

            ExpressionStatement expression = (ExpressionStatement) program.getStatements().getFirst();
            testInfixExpression(expression.getExpression(), infixTest.getLeftValue(), infixTest.getOperator(), infixTest.getRightValue());
        }
    }

    @Test
    public void testLayeredPrefixExpression() {
        List<PrefixExpressionTestData<?>> prefixTests = List.of(
                new PrefixExpressionTestData<Integer>("!!5", "!", 5),
                new PrefixExpressionTestData<Boolean>("!!true", "!", true)
        );

        for (var testData : prefixTests) {
            Lexer l = new Lexer(testData.getInput());
            Parser p = new Parser(l);
            Program program = p.parseProgram();

            checkParseErrors(p);

            assertEquals(1, program.getStatements().size());

            ExpressionStatement statement = (ExpressionStatement) program.getStatements().getFirst();
            PrefixExpression prefixExpression = (PrefixExpression) statement.getExpression();

            assertEquals(testData.getOperator(), prefixExpression.getOperator());

            PrefixExpression prefixExpression1 = (PrefixExpression) prefixExpression.getRight();
            assertEquals(testData.getOperator(), prefixExpression1.getOperator());

            testLiteralExpression(prefixExpression1.getRight(), testData.getValue());
        }
    }

    @Test
    public void testParsingPrefixExpression() {
        List<PrefixExpressionTestData<?>> prefixTests = List.of(
                new PrefixExpressionTestData<Integer>("!5", "!", 5),
                new PrefixExpressionTestData<Integer>("-15", "-", 15),
                new PrefixExpressionTestData<Boolean>("!true", "!", true),
                new PrefixExpressionTestData<Boolean>("!false", "!", false)
                //also test Prefixes before Identifiers
                //also test Prefixes before Functions
        );

        for (var testData : prefixTests) {
            Lexer l = new Lexer(testData.getInput());
            Parser p = new Parser(l);
            Program program = p.parseProgram();

            checkParseErrors(p);

            assertEquals(1, program.getStatements().size());

            ExpressionStatement statement = (ExpressionStatement) program.getStatements().getFirst();
            PrefixExpression prefixExpression = (PrefixExpression) statement.getExpression();

            assertEquals(testData.getOperator(), prefixExpression.getOperator());
            testLiteralExpression(prefixExpression.getRight(), testData.getValue());
        }
    }

    @Test
    public void testIntegerLiteralExpression() {
        String input = """
                5;
                """;

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        this.checkParseErrors(parser);

        assertEquals(1, program.getStatements().size());

        ExpressionStatement statement = (ExpressionStatement) program.getStatements().getFirst();
        testIntegerLiteral(statement.getExpression(), 5);
    }

    @Test
    public void testIdentifierExpression() {
        String input = """
                foobar;
                """;

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        this.checkParseErrors(parser);

        assertEquals(1, program.getStatements().size());

        ExpressionStatement statement = (ExpressionStatement) program.getStatements().getFirst();
        testIdentifier(statement.getExpression(), "foobar");
    }

    @Test
    public void testReturnStatements() {
        String input = """
                return 5;
                return 10;
                return      838383;
                """;

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        this.checkParseErrors(parser);

        assertEquals(3, program.getStatements().size());

        for (Statement statement : program.getStatements()) {
            assertInstanceOf(ReturnStatement.class, statement);
            ReturnStatement returnStatement = (ReturnStatement) statement;

            assertEquals(TokenType.RETURN, returnStatement.getToken().getTokenType());
            //TODO also test the return expression, if their is any?
        }
    }

    @Test
    public void testLetStatements() {
        String input = """
                let x = 5;
                let y = 10;
                let foobar  =   838383;
                """;

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        this.checkParseErrors(parser);

        assertEquals(3, program.getStatements().size());

        ExpectedStatement[] tests = {
                new ExpectedStatement("x"),
                new ExpectedStatement("y"),
                new ExpectedStatement("foobar")
        };

        for (int i = 0; i < tests.length; i++) {
            Statement statement = program.getStatements().get(i);
            this.testLetStatement(statement, tests[i].getIdent());
            //TODO test value part
        }
    }

    private void testLetStatement(Statement statement, String identifierName) {
        assertEquals("let", statement.getTokenLiteral());
        assertInstanceOf(LetStatement.class, statement);

        LetStatement letStatement = (LetStatement) statement;

        testIdentifier(letStatement.getIdentifier(), identifierName);
    }

    private void testIntegerLiteral(Expression right, int integerValue) {
        IntegerLiteralExpression integerLiteral = (IntegerLiteralExpression) right;

        assertEquals(TokenType.INT, integerLiteral.getToken().getTokenType());
        assertEquals(String.valueOf(integerValue), integerLiteral.getTokenLiteral());
        assertEquals(integerValue, integerLiteral.getValue());
    }

    private void testIdentifier(Expression exp, String value) {
        IdentifierExpression ident = (IdentifierExpression) exp;

        assertEquals(TokenType.IDENT, ident.getToken().getTokenType());
        assertEquals(value, ident.getValue());
        assertEquals(value, ident.getTokenLiteral());
    }

    private void testBooleanLiteral(Expression exp, boolean value) {
        BooleanLiteralExpression booleanLiteralExpression = (BooleanLiteralExpression) exp;

        assertEquals(value, booleanLiteralExpression.getValue());
        assertEquals(String.valueOf(value), booleanLiteralExpression.getTokenLiteral());
    }

    private void testLiteralExpression(Expression exp, Object value) {
        switch (value) {
            case Integer i -> testIntegerLiteral(exp, i);
            case String s -> testIdentifier(exp, s);
            case Boolean b -> testBooleanLiteral(exp, b);
            case null, default -> throw new RuntimeException("Type of exp not handled. Got=" + exp.getClass());
        }
    }

    /**
     * Only handles left and right if they are Literals
     */
    private void testInfixExpression(Expression exp, Object left, String operator, Object right) {
        InfixExpression infixExpression = (InfixExpression) exp;

        testLiteralExpression(infixExpression.getLeft(), left);
        assertEquals(operator, infixExpression.getOperator());
        testLiteralExpression(infixExpression.getRight(), right);
    }

    private void checkParseErrors(Parser parser) {
        List<String> errors = parser.getErrors();
        if (errors.isEmpty()) {
            return;
        }

        System.out.println("Parser has " + errors.size() + " errors");
        for (String error : errors) {
            System.out.println(error);
        }

        fail();
    }
}


//TODO refactor/consolidate these "testcases". Maybe they could be simpler or more generic. InputOuputTestHelper or smth

class ExpectedStatement {
    private final String ident;

    ExpectedStatement(String ident) {
        this.ident = ident;
    }

    public String getIdent() {
        return this.ident;
    }
}

class PrefixExpressionTestData<T> {
    private final String input;
    private final String operator;
    private final T value;

    PrefixExpressionTestData(String input, String operator, T integerValue) {
        this.operator = operator;
        this.input = input;
        this.value = integerValue;
    }

    public String getOperator() {
        return operator;
    }

    public String getInput() {
        return input;
    }

    public T getValue() {
        return value;
    }
}

class InfixTest<T> {
    private String input;
    private T leftValue;
    private String operator;
    private T rightValue;

    public InfixTest(String input, T leftValue, String operator, T rightValue) {
        this.input = input;
        this.leftValue = leftValue;
        this.operator = operator;
        this.rightValue = rightValue;
    }

    public String getInput() {
        return input;
    }

    public T getLeftValue() {
        return leftValue;
    }

    public String getOperator() {
        return operator;
    }

    public T getRightValue() {
        return rightValue;
    }
}

// Helper class to represent test cases
class PrecedenceTest {
    String input;
    String expected;

    PrecedenceTest(String input, String expected) {
        this.input = input;
        this.expected = expected;
    }
}
