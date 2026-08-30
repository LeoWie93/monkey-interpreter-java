package com.github.leowie93.evaluation;

public class NullObject implements ValueObject {
    @Override
    public ObjectType type() {
        return ObjectType.NULL;
    }

    @Override
    public String inspect() {
        return "null";
    }
}
