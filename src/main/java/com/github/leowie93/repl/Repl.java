package com.github.leowie93.repl;

import com.github.leowie93.lexer.Lexer;
import com.github.leowie93.token.Token;
import com.github.leowie93.token.TokenType;

import java.util.Scanner;

public class Repl {

    public void start() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            String input = sc.nextLine();

            if (input.equals("exit")) {
                return;
            }

            Lexer lexer = new Lexer(input);

            Token token = lexer.nextToken();
            while (token.getTokenType() != TokenType.EOF) {
                System.out.println(token.getTokenType() + " " + token.getLiteral());
                token = lexer.nextToken();
            }
        }
    }
}
