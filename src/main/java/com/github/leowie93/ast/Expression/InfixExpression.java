package com.github.leowie93.ast.Expression;

import com.github.leowie93.ast.Node;
import com.github.leowie93.token.Token;

public class InfixExpression implements Expression {
    private Token token;
    private Expression left;
    private String operator;
    private Expression right;

    public InfixExpression(Token token, Expression left, String operator, Expression right) {
        this.token = token; //Operator as a token
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public Node getExpressionNode() {
        return null;
    }

    @Override
    public String getTokenLiteral() {
        return this.token.getLiteral();
    }

    @Override
    public String nodeToString() {
        StringBuilder output = new StringBuilder();

        output.append("(");
        output.append(this.left.nodeToString());
        output.append(" ").append(this.operator).append(" ");
        output.append(this.right.nodeToString());
        output.append(")");

        return output.toString();
    }

    public Token getToken() {
        return token;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public String getOperator() {
        return this.operator;
    }
}
