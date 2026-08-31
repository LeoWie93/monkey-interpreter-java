package com.github.leowie93.evaluation;

import com.github.leowie93.ast.Program;
import com.github.leowie93.lexer.Lexer;
import com.github.leowie93.parser.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class EvalTest {
    @Test
    public void testEvalIntegerExpression() {
        List<InputOutputTest<Integer>> testList = List.of(
                new InputOutputTest<>("5", 5),
                new InputOutputTest<>("4", 4),
                new InputOutputTest<>("5 + 5 + 5 + 5 -10", 10),
                new InputOutputTest<>("5 + 5 * 2", 15),
                new InputOutputTest<>("2 * 5 + 5", 15),
                new InputOutputTest<>("2 * 2 * 2", 8),
                new InputOutputTest<>("50 / 2 * 2 + 10", 60),
                new InputOutputTest<>("4 * (2 + 2)", 16),
                new InputOutputTest<>("-4 * (2 + 2)", -16),
                new InputOutputTest<>("2 * (2 * 3) / 2", 6),
                new InputOutputTest<>("-50 - -10", -40),
                new InputOutputTest<>("-2 * -2", 4),
                new InputOutputTest<>("-2 * 2", -4)
        );

        for (InputOutputTest<Integer> test : testList) {
            var evaluated = this.testEval(test.input());
            this.testIntegerObject(evaluated, test.expected());
        }
    }

    @Test
    public void testEvalBooleanExpression() {
        List<InputOutputTest<Boolean>> testList = List.of(
                new InputOutputTest<>("true", true),
                new InputOutputTest<>("false", false),
                new InputOutputTest<>("1 < 2", true),
                new InputOutputTest<>("1 > 2", false),
                new InputOutputTest<>("1 == 1", true),
                new InputOutputTest<>("1 == 2", false),
                new InputOutputTest<>("1 != 2", true),
                new InputOutputTest<>("1 != 1", false),
                new InputOutputTest<>("true == true", true),
                new InputOutputTest<>("true == false", false),
                new InputOutputTest<>("false == false", true),
                new InputOutputTest<>("false != false", false),
                new InputOutputTest<>("false != true", true),
                new InputOutputTest<>("(1 < 2) == true", true),
                new InputOutputTest<>("(1 < 2) == false", false),
                new InputOutputTest<>("(1 > 2) == false", true),
                new InputOutputTest<>("(1 > 2) == true", false)
        );

        for (InputOutputTest<Boolean> test : testList) {
            var evaluated = this.testEval(test.input());
            this.testBooleanObject(evaluated, test.expected());
        }
    }

    @Test
    public void testEvalBangExpression() {
        List<InputOutputTest<Boolean>> testList = List.of(
                new InputOutputTest<>("!true", false),
                new InputOutputTest<>("!!true", true),
                new InputOutputTest<>("!false", true),
                new InputOutputTest<>("!!false", false),
                new InputOutputTest<>("!5", false),
                new InputOutputTest<>("!!5", true)
        );

        for (InputOutputTest<Boolean> test : testList) {
            var evaluated = this.testEval(test.input());
            this.testBooleanObject(evaluated, test.expected());
        }
    }

    @Test
    public void testEvalMinusExpression() {
        List<InputOutputTest<Integer>> testList = List.of(
                new InputOutputTest<>("-5", -5),
                new InputOutputTest<>("--5", 5),
                new InputOutputTest<>("---5", -5),
                new InputOutputTest<>("5", 5)
        );

        for (InputOutputTest<Integer> test : testList) {
            var evaluated = this.testEval(test.input());
            this.testIntegerObject(evaluated, test.expected());
        }
    }

    @Test
    public void testIfElseExpression() {
        List<InputOutputTest<?>> testList = List.of(
                new InputOutputTest<>("if(true) {10}", 10),
                new InputOutputTest<>("if(false) {10}", null),
                new InputOutputTest<>("if(1) {10}", 10),
                new InputOutputTest<>("if(1<2) {10}", 10),
                new InputOutputTest<>("if(1>2) {10}", null),
                new InputOutputTest<>("if(1<2) {10} else {20}", 10),
                new InputOutputTest<>("if(1>2) {10} else {20}", 20)
                // "if" is an expression. The following does not work
                //new InputOutputTest<>("if(1>2) {return 10} else {5+5; return 20;}", 20)
        );

        for (InputOutputTest<?> test : testList) {
            var evaluated = this.testEval(test.input());
            if (evaluated instanceof NullObject) {
                this.testNullObject(evaluated);
            } else {
                this.testIntegerObject(evaluated, (int) test.expected());
            }
        }
    }

    @Test
    public void testReturnStatement() {
        List<InputOutputTest<Integer>> testList = List.of(
                new InputOutputTest<>("return 10;", 10),
                new InputOutputTest<>("return 10;9;", 10),
                new InputOutputTest<>("9;return 10;", 10),
                new InputOutputTest<>("9;return 10;9;", 10),
                new InputOutputTest<>("return 2 * 5;", 10),
                new InputOutputTest<>("""
                        if(10>1){
                            if(10>1){
                                return 10;
                            }
                        }
                        
                        return 1;
                        """, 10)
        );

        for (InputOutputTest<Integer> test : testList) {
            var evaluated = this.testEval(test.input());
            testIntegerObject(evaluated, test.expected());
        }
    }

    @Test
    public void testLetStatements() {
        List<InputOutputTest<Integer>> testList = List.of(
                new InputOutputTest<>("let a = 5; a;", 5),
                new InputOutputTest<>("let a = 5 * 5; a;", 25),
                new InputOutputTest<>("let a = 5 * 5;let b = a; b;", 25),
                new InputOutputTest<>("let a = 5;let b = a; let c = a + b + 5; c;", 15),
                new InputOutputTest<>("let a = 5; let a = 10; let b = 5; a;", 10)
        );

        for (InputOutputTest<Integer> test : testList) {
            var evaluated = this.testEval(test.input());
            this.testIntegerObject(evaluated, test.expected());
        }
    }

    @Test
    public void testFunctionObject() {
        String input = "fn(x) { x + 2 };";
        var evaluated = this.testEval(input);

        Assertions.assertInstanceOf(FunctionObject.class, evaluated);
        FunctionObject fn = (FunctionObject) evaluated;

        Assertions.assertEquals(1, fn.params.size());
        Assertions.assertEquals("(x + 2)", fn.body.nodeToString());
    }

    @Test
    public void testFunctionApplication() {
        List<InputOutputTest<Integer>> testList = List.of(
                new InputOutputTest<>("let identity = fn(x) { x; }; identity(5);", 5),
                new InputOutputTest<>("let identity = fn(x) { return x; }; identity(5);", 5),
                new InputOutputTest<>("let double = fn(x) { x * 2; }; double(5);", 10),
                new InputOutputTest<>("let add = fn(x, y) { x + y; }; add(2, 3);", 5),
                new InputOutputTest<>("let add = fn(x, y) { x + y; }; add(2, add(1, 2));", 5),
                new InputOutputTest<>("fn(x, y) { x + y; }(5,5);", 10),
                new InputOutputTest<>("""
                        let devide = fn(x) {
                            let d = 40;
                            return d / x;
                        };
                        devide(2);
                        """, 20)
        );

        for (InputOutputTest<Integer> test : testList) {
            var evaluated = this.testEval(test.input());
            this.testIntegerObject(evaluated, test.expected());
        }
    }

    @Test
    public void testClosures() {
        String input = """
                let newAdder = fn(x) {
                    fn(y) { x + y };
                };
                
                let addTwo = newAdder(2);
                addTwo(3);
                """;

        var evaluated = this.testEval(input);
        this.testIntegerObject(evaluated, 5);
    }

    @Test
    public void testFunctionAsArguments() {
        String input = """
                let add = fn(a, b) { a + b };
                let sub = fn(a, b) { a - b };
                
                let applyFunc = fn(a, b, func) { return func(a,b); };
                applyFunc(10,2,sub);
                """;

        var evaluated = this.testEval(input);
        this.testIntegerObject(evaluated, 8);
    }

    @Test
    public void testErrorHandling() {
        List<InputOutputTest<String>> testList = List.of(
                new InputOutputTest<>("5 + true;", "type mismatch: INTEGER + BOOLEAN"),
                new InputOutputTest<>("5 + true; 5;", "type mismatch: INTEGER + BOOLEAN"),
                new InputOutputTest<>("-true", "unknown operator: -BOOLEAN"),
                new InputOutputTest<>("foobar", "identifier not found: foobar"),
                new InputOutputTest<>("false - true;", "unknown operator: BOOLEAN - BOOLEAN"),
                new InputOutputTest<>("5; false - true; 5;", "unknown operator: BOOLEAN - BOOLEAN"),
                new InputOutputTest<>("if(10 > 1){ true + false;}", "unknown operator: BOOLEAN + BOOLEAN"),
                new InputOutputTest<>("""
                        if(10 > 1){ 
                            if(10 > 1){
                                return true + false;
                            }
                        
                            return 1;
                        }
                        """, "unknown operator: BOOLEAN + BOOLEAN")
        );

        for (InputOutputTest<String> test : testList) {
            var evaluated = this.testEval(test.input());
            if (evaluated instanceof ErrorObject) {
                if (!test.expected().equals(((ErrorObject) evaluated).message)) {
                    Assertions.fail(String.format(
                            "wrong error message. Expected {%s} but got {%s}",
                            test.expected(),
                            ((ErrorObject) evaluated).message));
                } else {
                    continue;
                }
            } else {
                Assertions.fail("No error received: " + evaluated.type());
            }
        }
    }

    private void testNullObject(ValueObject object) {
        Assertions.assertInstanceOf(NullObject.class, object);
    }

    private void testBooleanObject(ValueObject object, Boolean expected) {
        BooleanObject booleanObject = (BooleanObject) object;
        Assertions.assertEquals(expected, booleanObject.value);
    }

    private void testIntegerObject(ValueObject object, int expected) {
        IntegerObject integerObject = (IntegerObject) object;
        Assertions.assertEquals(expected, integerObject.value);
    }

    private ValueObject testEval(String input) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        return new Evaluator().eval(program, new Environment());
    }
}

record InputOutputTest<T>(String input, T expected) {
}
