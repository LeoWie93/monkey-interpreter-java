package com.github.leowie93.ast.Expression;

import com.github.leowie93.ast.Node;
import com.github.leowie93.token.Token;

import java.util.List;
import java.util.stream.Collectors;

public class CallExpression implements Expression {
    public final Token token; // The '(' Token
    public Expression function; //Identifier or FunctionLiteral
    public List<Expression> arguments;

    public CallExpression(
            Token token,
            Expression function
    ) {
        this.token = token;
        this.function = function;
    }

    @Override
    public String nodeToString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.function.nodeToString());
        stringBuilder.append("(");
        stringBuilder.append(
                this.arguments.stream()
                        .map(Expression::nodeToString)
                        .collect(Collectors.joining(", "))

        );
        stringBuilder.append(")");

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
