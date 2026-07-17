package com.github.leowie93.ast.Statement;

import com.github.leowie93.ast.Expression.Expression;
import com.github.leowie93.ast.Node;
import com.github.leowie93.token.Token;

public class ExpressionStatement implements Statement {

    private final Token token;
    private Expression expression;

    public ExpressionStatement(Token token, Expression expression) {
        this.token = token; // the first token of the expression
        this.expression = expression;
    }

    public ExpressionStatement(Token token) {
        this.token = token;
    }

    public String nodeToString() {
        String output = "";

        if (this.getExpression() != null) {
            output += this.getExpression().nodeToString();
        }

        return output;
    }

    @Override
    public Node getStatementNode() {
        return null;
    }

    @Override
    public String getTokenLiteral() {
        return this.token.getLiteral();
    }

    public Token getToken() {
        return token;
    }

    public Expression getExpression() {
        return this.expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }
}
