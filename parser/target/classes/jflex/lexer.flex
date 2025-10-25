package com.testlang.parser;

import java_cup.runtime.*;

%%

%class Lexer
%unicode
%cup
%line
%column
%yylexthrow LexerException

%{
    private Symbol symbol(int type) {
        return new Symbol(type, yyline + 1, yycolumn + 1);
    }

    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline + 1, yycolumn + 1, value);
    }

    private void error(String message) throws LexerException {
        throw new LexerException(message, yyline + 1, yycolumn + 1, yytext());
    }
%}

/* Regular Expressions */
LineTerminator = \r|\n|\r\n
WhiteSpace = {LineTerminator} | [ \t\f]
Comment = "//" [^\r\n]*

Identifier = [A-Za-z_][A-Za-z0-9_]*
Integer = [0-9]+
StringChar = [^\"\\\r\n]
String = \"({StringChar}|\\[\"\\])*\"
UnterminatedString = \"({StringChar}|\\[\"\\])*

%%

/* Keywords */
"config"      { return symbol(sym.CONFIG); }
"base_url"    { return symbol(sym.BASE_URL); }
"header"      { return symbol(sym.HEADER); }
"let"         { return symbol(sym.LET); }
"test"        { return symbol(sym.TEST); }
"GET"         { return symbol(sym.GET); }
"POST"        { return symbol(sym.POST); }
"PUT"         { return symbol(sym.PUT); }
"DELETE"      { return symbol(sym.DELETE); }
"expect"      { return symbol(sym.EXPECT); }
"status"      { return symbol(sym.STATUS); }
"body"        { return symbol(sym.BODY); }
"contains"    { return symbol(sym.CONTAINS); }

/* Operators and Delimiters */
"{"           { return symbol(sym.LBRACE); }
"}"           { return symbol(sym.RBRACE); }
";"           { return symbol(sym.SEMICOLON); }
"="           { return symbol(sym.EQUALS); }

/* Identifiers and Literals */
{Identifier}  { return symbol(sym.IDENTIFIER, yytext()); }
{Integer}     { return symbol(sym.NUMBER, Integer.parseInt(yytext())); }
{String}      {
    // Remove quotes and handle escape sequences
    String str = yytext();
    str = str.substring(1, str.length() - 1); // Remove quotes
    str = str.replace("\\\"", "\"");
    str = str.replace("\\\\", "\\");
    return symbol(sym.STRING, str);
}

/* Whitespace and Comments */
{WhiteSpace}  { /* ignore */ }
{Comment}     { /* ignore */ }

/* Error handling - Must be in specific order */
{UnterminatedString}  { error("Unterminated string literal. Missing closing quote"); }
[0-9][A-Za-z_][A-Za-z0-9_]*  { error("Identifier cannot start with a digit"); }
[^]           { error("Illegal character '" + yytext() + "' (ASCII " + (int)yycharat(0) + ")"); }
