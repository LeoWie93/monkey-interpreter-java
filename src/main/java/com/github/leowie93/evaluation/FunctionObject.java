package com.github.leowie93.evaluation;


import com.github.leowie93.ast.Expression.IdentifierExpression;
import com.github.leowie93.ast.Statement.BlockStatement;

import java.util.ArrayList;
import java.util.List;

public class FunctionObject implements ValueObject {

    public final List<IdentifierExpression> params;
    public final BlockStatement body;
    public final Environment env;

    FunctionObject(List<IdentifierExpression> params, BlockStatement body, Environment env) {
        this.params = params;
        this.body = body;
        this.env = env;
    }

    @Override
    public ObjectType type() {
        return ObjectType.FUNCTION;
    }

    @Override
    public String inspect() {
        StringBuilder out = new StringBuilder();

        List<String> params = new ArrayList<String>();
        for (IdentifierExpression ie : this.params) {
            params.add(ie.nodeToString());
        }

        out.append("fn");
        out.append("(");
        out.append(String.join(", ", params));
        out.append(") {\n");
        out.append(this.body.nodeToString());
        out.append("};");

        return out.toString();
    }
}
