package com.github.leowie93.ast.Expression;

import com.github.leowie93.ast.Node;
import com.github.leowie93.token.Token;

public class BooleanLiteralExpression implements Expression {
    private Token token;
    private boolean value;

    public BooleanLiteralExpression(Token token, boolean value) {
        this.token = token;
        this.value = value;
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
        return this.token.getLiteral();
    }

    public boolean getValue(){
        return this.value;
    }
}
