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
                new PrecedenceTest("(b + c) * a", "((b + c) * a)"),
                new PrecedenceTest("a + add(b * c) + d", "((a + add((b * c))) + d)"),
                new PrecedenceTest("add(a, b, 1, 2 * 3, 4 + 5, add(6, 7 * 8))", "add(a, b, 1, (2 * 3), (4 + 5), add(6, (7 * 8)))"),
                new PrecedenceTest("add(a + b + c * d / f + g)", "add((((a + b) + ((c * d) / f)) + g))")
        };

        for (PrecedenceTest precedenceTest : tests) {
            Lexer l = new Lexer(precedenceTest.input());
            Parser p = new Parser(l);
            Program program = p.parseProgram();
            checkParseErrors(p);

            String actual = program.nodeToString();
            assertEquals(precedenceTest.expected(), actual, String.format("Expected=%s, got=%s", precedenceTest.expected(), actual));
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
            Lexer l = new Lexer(infixTest.input());
            Parser p = new Parser(l);
            Program program = p.parseProgram();

            checkParseErrors(p);

            assertEquals(1, program.getStatements().size());

            ExpressionStatement expression = (ExpressionStatement) program.getStatements().getFirst();
            testInfixExpression(expression.expression, infixTest.leftValue(), infixTest.operator(), infixTest.rightValue());
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
            PrefixExpression prefixExpression = (PrefixExpression) statement.expression;

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
            PrefixExpression prefixExpression = (PrefixExpression) statement.expression;

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
        testIntegerLiteral(statement.expression, 5);
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
        testIdentifier(statement.expression, "foobar");
    }

    @Test
    public void testReturnStatements() {
        List<ReturnStatementTest<?>> tests = List.of(
                new ReturnStatementTest<Integer>("return 5;", 5),
                new ReturnStatementTest<Integer>("return 10;", 10),
                new ReturnStatementTest<Integer>("return 838383;", 838383),
                new ReturnStatementTest<String>("return world;", "world")
        );

        for (ReturnStatementTest<?> test : tests) {
            Lexer lexer = new Lexer(test.input());
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            this.checkParseErrors(parser);

            assertEquals(1, program.getStatements().size());

            Statement statement = program.getStatements().getFirst();
            assertInstanceOf(ReturnStatement.class, statement);
            ReturnStatement returnStatement = (ReturnStatement) statement;

            assertEquals(TokenType.RETURN, returnStatement.getToken().getTokenType());
            this.testLiteralExpression(returnStatement.getReturnValue(), test.expectedValue());
        }
    }

    @Test
    public void testParseLetStatement() {
        String input = """
                let x = 5;
                let y = 10;
                let foobar  =   838383;
                let another = foobar;
                """;

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        this.checkParseErrors(parser);

        assertEquals(4, program.getStatements().size());

        List<LetStatementTest<?>> tests = List.of(
                new LetStatementTest<Integer>("x", 5),
                new LetStatementTest<Integer>("y", 10),
                new LetStatementTest<Integer>("foobar", 838383),
                new LetStatementTest<String>("another", "foobar")
        );

        for (int i = 0; i < tests.size(); i++) {
            Statement statement = program.getStatements().get(i);
            LetStatement letStatement = (LetStatement) statement;
            this.testLetStatement(letStatement, tests.get(i).expectedIdentifier());
            this.testLiteralExpression(letStatement.getValue(), tests.get(i).expectedValue());
        }
    }

    @Test
    public void testParseIfExpression() {
        String input = """
                if (x < y) {x}
                """;

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        this.checkParseErrors(parser);

        assertEquals(1, program.getStatements().size());

        ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().getFirst();
        IfExpression ifExpression = (IfExpression) expressionStatement.expression;

        assertEquals(TokenType.IF, ifExpression.token.getTokenType());
        testInfixExpression(ifExpression.condition, "x", "<", "y");

        ExpressionStatement consequence = (ExpressionStatement) ifExpression.consequence.statementList.getFirst();
        testIdentifier(consequence.expression, "x");

        assertNull(ifExpression.alternative);
    }

    @Test
    public void testParseIfElseExpression() {
        String input = """
                if (x < y) {x} else {y}
                """;

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        this.checkParseErrors(parser);

        assertEquals(1, program.getStatements().size());

        ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().getFirst();
        IfExpression ifExpression = (IfExpression) expressionStatement.expression;

        assertEquals(TokenType.IF, ifExpression.token.getTokenType());
        testInfixExpression(ifExpression.condition, "x", "<", "y");

        ExpressionStatement consequence = (ExpressionStatement) ifExpression.consequence.statementList.getFirst();
        testIdentifier(consequence.expression, "x");

        ExpressionStatement alternative = (ExpressionStatement) ifExpression.alternative.statementList.getFirst();
        testIdentifier(alternative.expression, "y");
    }

    @Test
    public void testParseFunctionExpression() {
        String input = """
                fn(x, y) { x + y; }
                """;

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        this.checkParseErrors(parser);

        assertEquals(1, program.getStatements().size());

        ExpressionStatement expressionStatement = (ExpressionStatement) program.getStatements().getFirst();
        FunctionLiteralExpression functionExpression = (FunctionLiteralExpression) expressionStatement.expression;

        assertEquals("fn", functionExpression.getTokenLiteral());

        assertEquals(2, functionExpression.parameters.size());
        testLiteralExpression(functionExpression.parameters.get(0), "x");
        testLiteralExpression(functionExpression.parameters.get(1), "y");

        assertEquals(1, functionExpression.body.statementList.size());
        ExpressionStatement firstBodyStatement = (ExpressionStatement) functionExpression.body.statementList.get(0);
        testInfixExpression(firstBodyStatement.expression, "x", "+", "y");
    }

    @Test
    public void testParseFunctionParameters() {
        List<FunctionParametersTest> tests = List.of(
                new FunctionParametersTest("fn() {}", List.of()),
                new FunctionParametersTest("fn(x) {}", List.of("x")),
                new FunctionParametersTest("fn(x,y,z) {}", List.of("x", "y", "z"))
        );

        for (FunctionParametersTest test : tests) {
            Lexer lexer = new Lexer(test.input());
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();

            ExpressionStatement statement = (ExpressionStatement) program.getStatements().getFirst();
            FunctionLiteralExpression functionExpression = (FunctionLiteralExpression) statement.expression;

            int expectedParamsCount = test.expectedParams().size();
            assertEquals(expectedParamsCount, functionExpression.parameters.size());

            for (int i = 0; i < expectedParamsCount; i++) {
                testIdentifier(functionExpression.parameters.get(i), test.expectedParams().get(i));
            }
        }
    }

    @Test
    public void testParseCallExpression() {
        String input = """
                add(1, 2 * 3, 4 + 5,b);
                """;

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        this.checkParseErrors(parser);

        ExpressionStatement statement = (ExpressionStatement) program.getStatements().getFirst();
        CallExpression callExpression = (CallExpression) statement.expression;

        testIdentifier(callExpression.function, "add");

        assertEquals(4, callExpression.arguments.size());
        testLiteralExpression(callExpression.arguments.get(0), 1);
        testInfixExpression(callExpression.arguments.get(1), 2, "*", 3);
        testInfixExpression(callExpression.arguments.get(2), 4, "+", 5);
        testIdentifier(callExpression.arguments.get(3), "b");
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

    /**
     *
     * @param exp   the expression to evaluate
     * @param value the type and value we expect to be the result of the evaluation
     */
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
record LetStatementTest<T>(String expectedIdentifier, T expectedValue) {
}

record ReturnStatementTest<T>(String input, T expectedValue) {
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

record InfixTest<T>(String input, T leftValue, String operator, T rightValue) {
}

record PrecedenceTest(String input, String expected) {
}

record FunctionParametersTest(String input, List<String> expectedParams) {
}
