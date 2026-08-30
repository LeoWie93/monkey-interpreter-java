package com.github.leowie93.evaluation;

import java.util.HashMap;

public class Environment {
    private HashMap<String, ValueObject> store;

    public Environment() {
        this.store = new HashMap<>();
    }

    public ValueObject get(String name) {
        return this.store.get(name);
    }

    public void set(String name, ValueObject value) {
        this.store.put(name, value);
    }
}
