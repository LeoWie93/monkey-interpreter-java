package com.github.leowie93.evaluation;

public class IntegerObject implements ValueObject{
    public int value;

    IntegerObject(int value){
        this.value = value;
    }

    @Override
    public ObjectType type() {
        return ObjectType.INTEGER_OBJC;
    }

    @Override
    public String inspect() {
        return String.valueOf(this.value);
    }
}
