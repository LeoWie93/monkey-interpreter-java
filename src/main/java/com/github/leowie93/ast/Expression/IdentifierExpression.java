package com.github.leowie93.ast.Expression;

import com.github.leowie93.ast.Node;
import com.github.leowie93.token.Token;

public class IdentifierExpression implements Expression {

    private final Token token;
    private final String value;

    public IdentifierExpression(Token token, String value){
        this.token = token;
        this.value = value;
    }

    @Override
    public String nodeToString() {
        return this.getValue();
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
        return token;
    }

    public String getValue() {
        return value;
    }
}
