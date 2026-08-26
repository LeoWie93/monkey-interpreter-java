package com.github.leowie93.evaluation;


import com.github.leowie93.ast.Expression.*;
import com.github.leowie93.ast.Node;
import com.github.leowie93.ast.Program;
import com.github.leowie93.ast.Statement.BlockStatement;
import com.github.leowie93.ast.Statement.ExpressionStatement;
import com.github.leowie93.ast.Statement.ReturnStatement;
import com.github.leowie93.ast.Statement.Statement;

import java.util.List;
import java.util.Objects;

public class Evaluator {

    NullObject nullCase = new NullObject();
    BooleanObject trueCase = new BooleanObject(true);
    BooleanObject falseCase = new BooleanObject(false);

    public ValueObject eval(Node node) {
        return switch (node) {
            case Program p -> this.evalProgram(p.getStatements());
            case BlockStatement bs -> this.evalBlockStatement(bs.statementList);
            case ExpressionStatement es -> this.eval(es.expression);
            case ReturnStatement rs -> new ReturnValueObject(this.eval(rs.returnValue));
            case IntegerLiteralExpression ie -> new IntegerObject(ie.getValue());
            case BooleanLiteralExpression bi -> this.nativeBoolToBooleanObject(bi.getValue());
            case IfExpression ie -> this.evalIfExpression(ie);
            case PrefixExpression pe -> {
                ValueObject right = this.eval(pe.getRight());
                yield this.evalPrefixExpression(pe.getOperator(), right);
            }
            case InfixExpression ie -> {
                ValueObject left = this.eval(ie.getLeft());
                ValueObject right = this.eval(ie.getRight());
                yield this.evalInfixExpression(ie.getOperator(), left, right);
            }
            default -> null;
        };
    }

    private ValueObject evalIfExpression(IfExpression ie) {
        ValueObject condition = this.eval(ie.condition);
        if (this.isTruthy(condition)) {
            return this.eval(ie.consequence);
        } else if (ie.alternative != null) {
            return this.eval(ie.alternative);
        } else {
            return this.nullCase;
        }
    }

    /**
     * Every Integer value is truthy -inf <-> +inf, including 0
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
        //TODO this means (null == null) => truthy?
        if (Objects.equals(operator, "==")) {
            return this.nativeBoolToBooleanObject(left == right);
        }
        if (Objects.equals(operator, "!=")) {
            return this.nativeBoolToBooleanObject(left != right);
        }

        return this.nullCase;
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
            default -> this.nullCase;
        };
    }

    private ValueObject evalPrefixExpression(String operator, ValueObject right) {
        return switch (operator) {
            case "!" -> this.evalBangPrefixOperatorExpression(right);
            case "-" -> this.evalMinusPerfixOperatorExpression(right);
            default -> null;
        };
    }

    private ValueObject evalMinusPerfixOperatorExpression(ValueObject right) {
        if (!(right instanceof IntegerObject)) {
            return this.nullCase;
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

    private ValueObject evalProgram(List<Statement> statements) {
        ValueObject object = new NullObject();

        for (Statement statement : statements) {
            object = eval(statement);

            if (object instanceof ReturnValueObject) {
                return ((ReturnValueObject) object).value;
            }
        }

        return object;
    }

    private ValueObject evalBlockStatement(List<Statement> statements) {
        ValueObject result = new NullObject();

        for (Statement statement : statements) {
            result = eval(statement);

            if (result instanceof ReturnValueObject) {
                return result;
            }
        }

        return result;
    }
}
