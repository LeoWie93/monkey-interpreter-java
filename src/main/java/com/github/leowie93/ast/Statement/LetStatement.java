package com.github.leowie93.ast.Statement;

import com.github.leowie93.ast.Expression.Expression;
import com.github.leowie93.ast.Expression.IdentifierExpression;
import com.github.leowie93.ast.Node;
import com.github.leowie93.token.Token;

public class LetStatement implements Statement {
    //TODO why not "hardcode" the Token?
    private final Token token;
    private IdentifierExpression identifier;
    private Expression value;

    public LetStatement(
            Token token,
            IdentifierExpression identifier,
            Expression value
    ) {
        this.token = token;
        this.identifier = identifier;
        this.value = value;
    }

    public LetStatement(
            Token token,
            IdentifierExpression identifier
    ) {
        this.token = token;
        this.identifier = identifier;
    }

    public LetStatement(Token token) {
        this.token = token;
    }

    public String nodeToString() {
        String output = "";

        output += this.getTokenLiteral() + " ";
        output += this.identifier.nodeToString() + " ";
        output += "= ";

        if (this.getValue() != null) {
            output += this.getValue().nodeToString();
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

    public IdentifierExpression getIdentifier() {
        return identifier;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public void setIdentifier(IdentifierExpression identifier) {
        this.identifier = identifier;
    }
}
