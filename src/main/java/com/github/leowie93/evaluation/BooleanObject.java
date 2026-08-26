package com.github.leowie93.evaluation;


public class BooleanObject implements ValueObject {
    public boolean value;

    BooleanObject(boolean value) {
        this.value = value;
    }

    @Override
    public ObjectType type() {
        return ObjectType.BOOLEAN_OBJECT;
    }

    @Override
    public String inspect() {
        return Boolean.toString(this.value);
    }
}
