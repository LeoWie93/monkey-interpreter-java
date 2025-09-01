package com.github.leowie93;

import com.github.leowie93.repl.Repl;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to the repl!!!");
        System.out.println("Use CTRL+C or type 'exit' to terminate.");

        Repl repl = new Repl();
        repl.start();
    }
}