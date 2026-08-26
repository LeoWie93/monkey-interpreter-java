package com.github.leowie93.evaluation;


public class ReturnValueObject implements ValueObject {
    public ValueObject value;

    ReturnValueObject(ValueObject value) {
        this.value = value;
    }

    @Override
    public ObjectType type() {
        return ObjectType.RETURN_VALUE_OBJECT;
    }

    @Override
    public String inspect() {
        return this.value.inspect();
    }
}
