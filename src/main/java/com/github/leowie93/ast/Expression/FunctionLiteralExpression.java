package com.github.leowie93.ast.Expression;

import com.github.leowie93.ast.Node;
import com.github.leowie93.ast.Statement.BlockStatement;
import com.github.leowie93.token.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionLiteralExpression implements Expression {
    public final Token token; //fn
    public List<IdentifierExpression> parameters = new ArrayList<>();
    public BlockStatement body;

    public FunctionLiteralExpression(
            Token token
    ) {
        this.token = token;
    }

    @Override
    public String nodeToString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.getTokenLiteral());
        stringBuilder.append("(");
        stringBuilder.append(
                this.parameters.stream()
                        .map(IdentifierExpression::nodeToString)
                        .collect(Collectors.joining(", "))

        );
        stringBuilder.append(") ");
        stringBuilder.append(this.body.nodeToString());

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
