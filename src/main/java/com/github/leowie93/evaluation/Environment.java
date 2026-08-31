package com.github.leowie93.evaluation;

import java.util.HashMap;

public class Environment {
    private HashMap<String, ValueObject> store;
    private Environment outer;

    public Environment() {
        this.store = new HashMap<>();

    }

    /**
     * @param env the outer environment this one is wrapping
     */
    public Environment(Environment env) {
        this.store = new HashMap<>();
        this.outer = env;
    }

    public ValueObject get(String name) {
        ValueObject object = this.store.get(name);
        if (object == null && this.outer != null) {
            object = this.outer.get(name);
        }

        return object;
    }

    public ValueObject set(String name, ValueObject value) {
        this.store.put(name, value);
        return value;
    }
}
