package com.github.leowie93.ast.Expression;

import com.github.leowie93.ast.Node;
import com.github.leowie93.ast.Statement.BlockStatement;
import com.github.leowie93.token.Token;

public class IfExpression implements Expression {
    public final Token token; //if
    public Expression condition;
    public BlockStatement consequence;
    public BlockStatement alternative;

    public IfExpression(
            Token token
    ) {
        this.token = token;
    }

    @Override
    public String nodeToString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.getTokenLiteral()).append(" ");
        stringBuilder.append(this.condition.nodeToString());
        stringBuilder.append(" ");
        stringBuilder.append(this.consequence.nodeToString());

        if (this.alternative != null) {
            stringBuilder.append("else ");
            stringBuilder.append(this.alternative.nodeToString());
        }

        return stringBuilder.toString();
    }

    @Override
    public Node getExpressionNode() {
        return null;
    }

    @Override
    public String getTokenLiteral() {
        return this.token.getLiteral();
    }
}
