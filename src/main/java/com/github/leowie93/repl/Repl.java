package com.github.leowie93.repl;

import com.github.leowie93.ast.Program;
import com.github.leowie93.evaluation.Evaluator;
import com.github.leowie93.evaluation.ValueObject;
import com.github.leowie93.lexer.Lexer;
import com.github.leowie93.parser.Parser;

import java.util.Scanner;

public class Repl {

    public void start() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            String input = sc.nextLine();

            if (input.equals("exit")) {
                System.out.println("Quitting");
                return;
            }

            Lexer lexer = new Lexer(input);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();

            if (!parser.getErrors().isEmpty()) {
                parser.getErrors().forEach(System.out::println);
                continue;
            }

            ValueObject evaluated = new Evaluator().eval(program);
            if (evaluated != null) {
                System.out.println(evaluated.inspect());
            }
        }
    }
}
