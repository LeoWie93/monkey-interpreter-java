package com.github.leowie93.ast.Expression;

import com.github.leowie93.ast.Node;
import com.github.leowie93.token.Token;

public class PrefixExpression implements Expression {

    private Token token;
    private Expression right;
    private String Operator;

    //TODO could we not just pass the token and get its literals as the operator String?
    public PrefixExpression(Token token, String operator, Expression right) {
        this.token = token;
        Operator = operator;
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
        output.append(this.Operator);
        output.append(this.right.nodeToString());
        output.append(")");

        return String.valueOf(output);
    }

    public Token getToken() {
        return token;
    }

    public Expression getRight() {
        return right;
    }

    public String getOperator() {
        return Operator;
    }
}
