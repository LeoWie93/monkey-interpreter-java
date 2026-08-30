package com.github.leowie93.evaluation;


import com.github.leowie93.ast.Expression.*;
import com.github.leowie93.ast.Node;
import com.github.leowie93.ast.Program;
import com.github.leowie93.ast.Statement.*;

import java.util.List;
import java.util.Objects;

public class Evaluator {

    NullObject nullCase = new NullObject();
    BooleanObject trueCase = new BooleanObject(true);
    BooleanObject falseCase = new BooleanObject(false);

    public ValueObject eval(Node node, Environment env) {
        return switch (node) {
            case Program p -> this.evalProgram(p.getStatements(), env);
            case BlockStatement bs -> this.evalBlockStatement(bs.statementList, env);
            case LetStatement bs -> {
                ValueObject result = this.eval(bs.getValue(), env);
                if (this.isError(result)) {
                    yield result;
                }

                env.set(bs.getIdentifier().getValue(), result);
                yield null;
            }
            case IdentifierExpression ie -> this.evalIdentifier(ie, env);
            case ExpressionStatement es -> this.eval(es.expression, env);
            case ReturnStatement rs -> {
                ValueObject result = this.eval(rs.returnValue, env);
                if (this.isError(result)) {
                    yield result;
                }

                yield new ReturnValueObject(result);
            }
            case IntegerLiteralExpression ie -> new IntegerObject(ie.getValue());
            case BooleanLiteralExpression bi -> this.nativeBoolToBooleanObject(bi.getValue());
            case IfExpression ie -> this.evalIfExpression(ie, env);
            case PrefixExpression pe -> {
                ValueObject right = this.eval(pe.getRight(), env);
                if (this.isError(right)) {
                    yield right;
                }
                yield this.evalPrefixExpression(pe.getOperator(), right);
            }
            case InfixExpression ie -> {
                ValueObject left = this.eval(ie.getLeft(), env);
                ValueObject right = this.eval(ie.getRight(), env);
                if (this.isError(left)) {
                    yield left;
                }
                if (this.isError(right)) {
                    yield right;
                }
                yield this.evalInfixExpression(ie.getOperator(), left, right);
            }
            default -> null;
        };
    }

    private ValueObject evalIdentifier(IdentifierExpression node, Environment env) {
        var value = env.get(node.getValue());
        if (value == null) {
            return new ErrorObject("identifier not found: " + node.getValue());
        }

        return value;
    }

    private ValueObject evalIfExpression(IfExpression ie, Environment env) {
        ValueObject condition = this.eval(ie.condition, env);
        if (this.isError(condition)) {
            return condition;
        }

        if (this.isTruthy(condition)) {
            return this.eval(ie.consequence, env);
        } else if (ie.alternative != null) {
            return this.eval(ie.alternative, env);
        } else {
            return this.nullCase;
        }
    }

    /**
     * Every Integer value is truthy [-inf,+inf], including 0
     */
    private boolean isTruthy(ValueObject condition) {
        if (condition.equals(trueCase)) {
            return true;
        }
        if (condition.equals(falseCase)) {
            return false;
        }
        if (condition.equals(nullCase)) {
            return false;
        }

        return true;
    }

    private ValueObject nativeBoolToBooleanObject(boolean value) {
        return value ? this.trueCase : this.falseCase;
    }

    /**
     * Notable: Comparisons between pointers vs raw values
     * - BooleanObjects can be directly compared
     * - IntegerObjects need to be unpacked: we need to check the values directly
     */
    private ValueObject evalInfixExpression(String operator, ValueObject left, ValueObject right) {
        if (left instanceof IntegerObject && right instanceof IntegerObject) {
            return this.evalIntegerInfixExpression(operator, (IntegerObject) left, (IntegerObject) right);
        }
        //We can compare a boolean and an INT object without unpacking it, they are never the same

        if (Objects.equals(operator, "==")) {
            return this.nativeBoolToBooleanObject(left == right);
        }

        if (Objects.equals(operator, "!=")) {
            return this.nativeBoolToBooleanObject(left != right);
        }

        if (left.type() != right.type()) {
            return createError("type mismatch: %s %s %s", left.type(), operator, right.type());
        }

        return createError("unknown operator: %s %s %s", left.type(), operator, right.type());
    }

    private ValueObject evalIntegerInfixExpression(String operator, IntegerObject left, IntegerObject right) {
        return switch (operator) {
            case "+" -> new IntegerObject(left.value + right.value);
            case "-" -> new IntegerObject(left.value - right.value);
            case "*" -> new IntegerObject(left.value * right.value);
            case "/" -> new IntegerObject(left.value / right.value);
            case "==" -> this.nativeBoolToBooleanObject(left.value == right.value);
            case "!=" -> this.nativeBoolToBooleanObject(left.value != right.value);
            case "<" -> this.nativeBoolToBooleanObject(left.value < right.value);
            case ">" -> this.nativeBoolToBooleanObject(left.value > right.value);
            case ">=" -> this.nativeBoolToBooleanObject(left.value >= right.value);
            case "<=" -> this.nativeBoolToBooleanObject(left.value <= right.value);
            default -> createError("unknown operator: %s %s %s", left.type(), operator, right.type());
        };
    }

    private ValueObject evalPrefixExpression(String operator, ValueObject right) {
        return switch (operator) {
            case "!" -> this.evalBangPrefixOperatorExpression(right);
            case "-" -> this.evalMinusPrefixOperatorExpression(right);
            default -> createError("unknown operator: %s%s", operator, right.type());
        };
    }

    private ValueObject evalMinusPrefixOperatorExpression(ValueObject right) {
        if (!(right instanceof IntegerObject)) {
            return createError("unknown operator: -%s", right.type());
        }
        return new IntegerObject(-Integer.parseInt(right.inspect()));
    }

    private ValueObject evalBangPrefixOperatorExpression(ValueObject right) {
        if (right.equals(this.trueCase)) {
            return this.falseCase;
        }
        if (right.equals(this.falseCase)) {
            return this.trueCase;
        }
        if (right.equals(nullCase)) {
            return this.trueCase;
        }

        return this.falseCase;
    }

    private ValueObject evalProgram(List<Statement> statements, Environment env) {
        ValueObject object = new NullObject();

        for (Statement statement : statements) {
            object = eval(statement, env);

            if (object instanceof ReturnValueObject) {
                return ((ReturnValueObject) object).value;
            }
            if (object instanceof ErrorObject) {
                return object;
            }
        }

        return object;
    }

    private ValueObject evalBlockStatement(List<Statement> statements, Environment env) {
        ValueObject result = new NullObject();

        for (Statement statement : statements) {
            result = eval(statement, env);

            if (result instanceof ReturnValueObject || result instanceof ErrorObject) {
                return result;
            }
        }

        return result;
    }

    private boolean isError(ValueObject valueObject) {
        if (valueObject != null) {
            return valueObject instanceof ErrorObject;
        }
        return false;
    }

    private ErrorObject createError(String format, Object a) {
        return new ErrorObject(String.format(format, a));
    }

    private ErrorObject createError(String format, Object a, Object b) {
        return new ErrorObject(String.format(format, a, b));
    }

    private ErrorObject createError(String format, Object a, Object b, Object c) {
        return new ErrorObject(String.format(format, a, b, c));
    }
}
