package com.github.leowie93.parser;

import com.github.leowie93.ast.Expression.*;
import com.github.leowie93.ast.Program;
import com.github.leowie93.ast.Statement.*;
import com.github.leowie93.lexer.Lexer;
import com.github.leowie93.token.Token;
import com.github.leowie93.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Parser {

    private final Lexer lexer;
    private final List<String> errors = new ArrayList<>();
    private Token currToken;
    private Token nextToken;

    private Map<TokenType, Supplier<Expression>> prefixParseFns = new HashMap<>();
    private Map<TokenType, Function<Expression, Expression>> infixParseFns = new HashMap<>();

    private final Map<TokenType, ParserPrecedence> precedenceMap = new HashMap<>(Map.of(
            TokenType.GT, ParserPrecedence.LESSGREATER,
            TokenType.LT, ParserPrecedence.LESSGREATER,
            TokenType.PLUS, ParserPrecedence.SUM,
            TokenType.MINUS, ParserPrecedence.SUM,
            TokenType.ASTERISK, ParserPrecedence.PRODUCT,
            TokenType.SLASH, ParserPrecedence.PRODUCT,
            TokenType.EQUAL, ParserPrecedence.EQUALS,
            TokenType.NEQUAL, ParserPrecedence.EQUALS,
            TokenType.LPAREN, ParserPrecedence.CALL
    ));

    public Parser(Lexer lexer) {
        this.lexer = lexer;

        this.registerPrefixFn(TokenType.IDENT, this::parseIdentifier);
        this.registerPrefixFn(TokenType.INT, this::parseIntegerLiteral);
        this.registerPrefixFn(TokenType.BANG, this::parsePrefixExpression);
        this.registerPrefixFn(TokenType.MINUS, this::parsePrefixExpression);
        this.registerPrefixFn(TokenType.TRUE, this::parseBooleanLiteral);
        this.registerPrefixFn(TokenType.FALSE, this::parseBooleanLiteral);
        this.registerPrefixFn(TokenType.LPAREN, this::parseGroupedExpression);
        this.registerPrefixFn(TokenType.IF, this::parseIfExpression);
        this.registerPrefixFn(TokenType.FUNCTION, this::parseFunctionExpression);

        this.registerInfixFn(TokenType.PLUS, this::parseInfixExpression);
        this.registerInfixFn(TokenType.MINUS, this::parseInfixExpression);
        this.registerInfixFn(TokenType.SLASH, this::parseInfixExpression);
        this.registerInfixFn(TokenType.ASTERISK, this::parseInfixExpression);
        this.registerInfixFn(TokenType.EQUAL, this::parseInfixExpression);
        this.registerInfixFn(TokenType.NEQUAL, this::parseInfixExpression);
        this.registerInfixFn(TokenType.LT, this::parseInfixExpression);
        this.registerInfixFn(TokenType.GT, this::parseInfixExpression);
        this.registerInfixFn(TokenType.LPAREN, this::parseCallExpression);

        //Position parser at the start
        this.advanceParser();
        this.advanceParser();
    }

    public void advanceParser() {
        this.currToken = this.nextToken;
        this.nextToken = this.lexer.nextToken();
    }

    public void registerPrefixFn(TokenType tokenType, Supplier<Expression> fn) {
        this.prefixParseFns.put(tokenType, fn);
    }

    public void registerInfixFn(TokenType tokenType, Function<Expression, Expression> fn) {
        this.infixParseFns.put(tokenType, fn);
    }

    public Program parseProgram() {
        List<Statement> statements = new ArrayList<>();

        while (this.currToken.getTokenType() != TokenType.EOF) {
            Statement statement = this.parseStatement();

            if (statement != null) {
                statements.add(statement);
            }

            //this is the place to check for line ending syntax, if this language should have any
            this.advanceParser(); //we should currently be on top of an ";" or at the end
        }

        return new Program(statements);
    }

    private IdentifierExpression parseIdentifier() {
        return new IdentifierExpression(this.currToken, this.currToken.getLiteral());
    }

    private BooleanLiteralExpression parseBooleanLiteral() {
        return new BooleanLiteralExpression(this.currToken, this.currTokenIs(TokenType.TRUE));
    }

    private IntegerLiteralExpression parseIntegerLiteral() {
        try {
            int value = Integer.parseInt(this.currToken.getLiteral());
            return new IntegerLiteralExpression(this.currToken, value);
        } catch (NumberFormatException e) {
            this.errors.add("Could not parse " + this.currToken.getLiteral() + " as integer");
            return null;
        }
    }

    private Statement parseStatement() {
        return switch (this.currToken.getTokenType()) {
            case TokenType.LET -> this.parseLetStatement();
            case TokenType.RETURN -> this.parseReturnStatement();
            default -> this.parseExpressionStatement();
        };
    }

    private Statement parseExpressionStatement() {
        ExpressionStatement statement = new ExpressionStatement(this.currToken);

        statement.setExpression(this.parseExpression(ParserPrecedence.LOWEST));

        if (this.peekTokenIs(TokenType.SEMICOLON)) {
            this.advanceParser();
        }

        return statement;
    }

    private void addNoParseFnError(TokenType tokenType) {
        this.errors.add("no prefix parse function found for TokenType: " + tokenType + " with value: " + tokenType.value());
    }

    /**
     * @param precedence - the "right-binding power" of the caller
     */
    private Expression parseExpression(ParserPrecedence precedence) {
        var prefixFn = this.prefixParseFns.get(this.currToken.getTokenType());
        if (prefixFn == null) {
            addNoParseFnError(this.currToken.getTokenType());
            return null;
        }

        Expression leftExp = prefixFn.get();

        //True if the "left-binding power" is higher. The semicolon check is for readability
        while (!this.peekTokenIs(TokenType.SEMICOLON) && precedence.ordinal() < this.peekPrecedence().ordinal()) {
            var infixFn = this.infixParseFns.get(this.nextToken.getTokenType());
            if (infixFn == null) {
                return leftExp;
            }

            this.advanceParser();
            leftExp = infixFn.apply(leftExp);
        }

        return leftExp;
    }

    private Expression parsePrefixExpression() {
        Token token = this.currToken;
        String operator = this.currToken.getLiteral();

        this.advanceParser(); // move off prefix operator

        return new PrefixExpression(
                token,
                operator,
                this.parseExpression(ParserPrecedence.PREFIX)
        );
    }

    private Expression parseInfixExpression(Expression left) {
        Token token = this.currToken;
        String operator = this.currToken.getLiteral();
        ParserPrecedence precedence = this.currPrecedence();

        this.advanceParser(); // move off infix operator

        return new InfixExpression(
                token,
                left,
                operator,
                this.parseExpression(precedence)
        );
    }

    private Expression parseGroupedExpression() {
        this.advanceParser(); // move off "(" token

        var exp = this.parseExpression(ParserPrecedence.LOWEST);

        // expect closing ")" and move onto it
        if (!this.expectPeek(TokenType.RPAREN)) {
            return null;
        }

        return exp;
    }

    //If statements: only allow a single expression per branch => produces a value
    private IfExpression parseIfExpression() {
        IfExpression ifExpression = new IfExpression(this.currToken);

        // check for begin of condition syntax: check for "(" and move onto first token inside (...)
        if (!this.expectPeek(TokenType.LPAREN)) {
            return null;
        }
        this.advanceParser();

        ifExpression.condition = this.parseExpression(ParserPrecedence.LOWEST);

        if (!this.expectPeek(TokenType.RPAREN)) {
            return null;
        }

        if (!this.expectPeek(TokenType.LBRACE)) {
            return null;
        }

        //Stay on the LBRACE while calling parseBlockStatement. It is part of the block.
        ifExpression.consequence = this.parseBlockStatement();

        if (this.peekTokenIs(TokenType.ELSE)) {
            this.advanceParser();
            if (!this.expectPeek(TokenType.LBRACE)) {
                return null;
            }
            ifExpression.alternative = this.parseBlockStatement();
        }

        return ifExpression;
    }

    private FunctionLiteralExpression parseFunctionExpression() {
        FunctionLiteralExpression functionExpression = new FunctionLiteralExpression(this.currToken);

        if (!this.expectPeek(TokenType.LPAREN)) {
            return null;
        }

        functionExpression.parameters = this.parseFunctionParameters();

        if (!this.expectPeek(TokenType.LBRACE)) {
            return null;
        }

        functionExpression.body = this.parseBlockStatement();
        return functionExpression;
    }

    private List<IdentifierExpression> parseFunctionParameters() {
        List<IdentifierExpression> parameters = new ArrayList<>();
        if (this.peekTokenIs(TokenType.RPAREN)) {
            this.advanceParser();
            return parameters;
        }

        this.advanceParser();// move onto first parameter declaration

        parameters.add(this.parseIdentifier());

        while (this.peekTokenIs(TokenType.COMMA)) {
            this.advanceParser(); // onto comman
            this.advanceParser(); // onto next token
            parameters.add(this.parseIdentifier());
        }

        if (!this.expectPeek(TokenType.RPAREN)) {
            return null;
        }

        return parameters;
    }

    private CallExpression parseCallExpression(Expression function) {
        CallExpression callExpression = new CallExpression(this.currToken, function);
        callExpression.arguments = this.parseCallArguments();
        return callExpression;
    }

    private List<Expression> parseCallArguments() {
        List<Expression> arguments = new ArrayList<>();

        if (this.peekTokenIs(TokenType.RPAREN)) {
            this.advanceParser();
            return arguments;
        }

        this.advanceParser();//move onto first argument

        arguments.add(this.parseExpression(ParserPrecedence.LOWEST));

        while (this.peekTokenIs(TokenType.COMMA)) {
            this.advanceParser(); // onto comma
            this.advanceParser(); // onto next token
            arguments.add(this.parseExpression(ParserPrecedence.LOWEST));
        }

        if (!this.expectPeek(TokenType.RPAREN)) {
            return null;
        }

        return arguments;
    }

    private BlockStatement parseBlockStatement() {
        BlockStatement blockStatement = new BlockStatement(this.currToken);
        this.advanceParser();

        while (!this.currTokenIs(TokenType.RBRACE) && !this.currTokenIs(TokenType.EOF)) {
            Statement statement = this.parseStatement();
            if (statement != null) {
                blockStatement.statementList.add(statement);
            }
            this.advanceParser();
        }

        return blockStatement;
    }

    private Statement parseReturnStatement() {
        ReturnStatement returnStatement = new ReturnStatement(this.currToken);

        this.advanceParser();

        returnStatement.setReturnValue(this.parseExpression(ParserPrecedence.LOWEST));

        if (!expectPeek(TokenType.SEMICOLON)) {
            return null;
        }

        return returnStatement;
    }

    private Statement parseLetStatement() {
        LetStatement statement = new LetStatement(this.currToken);

        if (!this.expectPeek(TokenType.IDENT)) {
            return null;
        }

        statement.setIdentifier(new IdentifierExpression(this.currToken, this.currToken.getLiteral()));

        if (!this.expectPeek(TokenType.ASSIGN)) {
            return null;
        }
        this.advanceParser();

        statement.setValue(this.parseExpression(ParserPrecedence.LOWEST));

        if (!this.expectPeek(TokenType.SEMICOLON)) {
            return null;
        }

        return statement;
    }

    /**
     * @param tokenType The token to expect and advance the parser if true
     */
    private boolean expectPeek(TokenType tokenType) {
        if (this.peekTokenIs(tokenType)) {
            this.advanceParser();
            return true;
        }

        this.peekError(tokenType);
        return false;
    }

    private boolean currTokenIs(TokenType tokenTyp) {
        return this.currToken.getTokenType().equals(tokenTyp);
    }

    private boolean peekTokenIs(TokenType tokenTyp) {
        return this.nextToken.getTokenType().equals(tokenTyp);
    }

    private void peekError(TokenType tokenType) {
        this.setError(String.format("Expected Token of type %s, got %s instead", tokenType.value(), this.nextToken.getTokenType().value()));
    }

    public List<String> getErrors() {
        return this.errors;
    }

    public void setError(String error) {
        this.errors.add(error);
    }

    /**
     * The "left-binding power" of the next token
     */
    private ParserPrecedence peekPrecedence() {
        return this.precedenceMap.getOrDefault(this.nextToken.getTokenType(), ParserPrecedence.LOWEST);
    }

    /**
     * The "left-binding power" of the current token
     */
    private ParserPrecedence currPrecedence() {
        return this.precedenceMap.getOrDefault(this.currToken.getTokenType(), ParserPrecedence.LOWEST);
    }
}
