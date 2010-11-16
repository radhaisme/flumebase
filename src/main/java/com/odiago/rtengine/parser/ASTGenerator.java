// (c) Copyright 2010 Odiago, Inc.

package com.odiago.rtengine.parser;

import java.io.PrintStream;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main interface to the parsing system.
 * The ASTGenerator accepts a string and returns the AST for the command
 * to execute.
 */
public class ASTGenerator {
  private static final Logger LOG = LoggerFactory.getLogger(
      ASTGenerator.class.getName());

  public ASTGenerator() {
  }

  /**
   * Parse the given 'input', which must be exactly one statement.
   * Return a SQLStatement representing the object, or null if there
   * is a parse error.
   * @param input the query string to parse into a SQLStatement.
   * @param errStream the stream to which error messages from ANTLR should be piped.
   */
  public SQLStatement parse(String input, PrintStream errStream) throws RecognitionException {
    SqlLexer lex = new SqlLexer(new ANTLRStringStream(input));
    lex.setErrorStream(errStream);
    CommonTokenStream tokens = new CommonTokenStream(lex);
    SqlGrammar parser = new SqlGrammar(tokens);
    parser.setErrorStream(errStream);

    SQLStatement parsedStmt = parser.top().val;

    if (parser.getNumberOfSyntaxErrors() > 0 || lex.getNumberOfSyntaxErrors() > 0) {
      // Syntax error; don't return any partial parse tree.
      return null;
    }

    return parsedStmt;
  }
}
