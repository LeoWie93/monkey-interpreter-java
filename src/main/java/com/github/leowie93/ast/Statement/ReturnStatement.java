package com.github.leowie93.ast.Statement;

import com.github.leowie93.ast.Expression.Expression;
import com.github.leowie93.ast.Node;
import com.github.leowie93.token.Token;

public class ReturnStatement implements Statement {

    private final Token token;
    private Expression returnValue;

    public ReturnStatement(Token token, Expression returnValue) {
        this.token = token;
        this.returnValue = returnValue;
    }

    public ReturnStatement(Token token) {
        this.token = token;
    }

    @Override
    public String nodeToString() {
        String output = "";

        output += this.getTokenLiteral() + " ";

        if (this.getReturnValue() != null) {
            output += this.getReturnValue().nodeToString();
        }

        output += ";";

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

    public Expression getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Expression returnValue) {
        this.returnValue = returnValue;
    }
}
