package com.github.leowie93.ast.Statement;

import com.github.leowie93.ast.Node;
import com.github.leowie93.token.Token;

import java.util.ArrayList;
import java.util.List;

public class BlockStatement implements Statement {

    private final Token token;
    public List<Statement> statementList = new ArrayList<>();

    public BlockStatement(Token token) {
        this.token = token;
    }

    public String nodeToString() {
        StringBuilder output = new StringBuilder();

        output.append("{");
        for (Statement statement : this.statementList) {
            output.append(statement.toString());
        }
        output.append("}");

        return output.toString();
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
}
