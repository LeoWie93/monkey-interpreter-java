package com.github.leowie93.ast;

import com.github.leowie93.ast.Statement.Statement;

import java.util.List;

/**
 * Takes a list of statements and outputs them as a String
 */
public class Program implements Node {
    private List<Statement> statementList;

    public Program(List<Statement> statementList) {
        this.statementList = statementList;
    }

    @Override
    public String getTokenLiteral() {
        if (this.statementList.size() > 0) {
            return this.statementList.get(0).getTokenLiteral();
        }

        return "";
    }

    public String nodeToString() {
        StringBuilder output = new StringBuilder();

        for (Statement statement : this.statementList) {
            output.append(statement.nodeToString());
        }

        return String.valueOf(output);
    }

    public List<Statement> getStatements() {
        return this.statementList;
    }
}
