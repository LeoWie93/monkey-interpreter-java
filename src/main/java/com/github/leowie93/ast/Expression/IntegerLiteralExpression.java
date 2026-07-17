package com.github.leowie93.ast.Expression;

import com.github.leowie93.ast.Node;
import com.github.leowie93.token.Token;

public class IntegerLiteralExpression implements Expression {

    private final Token token;
    private final int value;

    public IntegerLiteralExpression(Token token, int value) {
        this.token = token;
        this.value = value;
    }

    public String nodeToString() {
        return String.valueOf(this.getValue());
    }

    @Override
    public Node getExpressionNode() {
        return null;
    }

    @Override
    public String getTokenLiteral() {
        return this.token.getLiteral();
    }

    public Token getToken() {
        return this.token;
    }

    public int getValue() {
        return this.value;
    }
}
