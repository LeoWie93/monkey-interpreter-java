package com.github.leowie93.evaluation;

public class ErrorObject implements ValueObject {
    public String message;

    ErrorObject(String message) {
        this.message = message;
    }

    @Override
    public ObjectType type() {
        return ObjectType.ERROR;
    }

    @Override
    public String inspect() {
        return "Error: " + message;
    }
}
