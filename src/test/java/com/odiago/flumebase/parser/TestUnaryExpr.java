/**
 * Licensed to Odiago, Inc. under one or more contributor license
 * agreements.  See the NOTICE.txt file distributed with this work for
 * additional information regarding copyright ownership.  Odiago, Inc.
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.odiago.flumebase.parser;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

import com.odiago.flumebase.exec.HashSymbolTable;

import com.odiago.flumebase.lang.Type;
import com.odiago.flumebase.lang.TypeCheckException;
import com.odiago.flumebase.lang.TypeChecker;

public class TestUnaryExpr extends ExprTestCase {

  @Test
  public void testNot() throws Exception {
    Expr unaryExpr;
    TypeChecker checker;
    Object value;

    unaryExpr = new UnaryExpr(UnaryOp.Not,
        new ConstExpr(Type.getPrimitive(Type.TypeName.BOOLEAN), Boolean.FALSE));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Boolean.TRUE, value);

    unaryExpr = new UnaryExpr(UnaryOp.Not,
        new ConstExpr(Type.getPrimitive(Type.TypeName.BOOLEAN), Boolean.TRUE));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Boolean.FALSE, value);

    unaryExpr = new UnaryExpr(UnaryOp.Not,
        new ConstExpr(Type.getNullable(Type.TypeName.BOOLEAN), null));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(null, value);

    unaryExpr = new UnaryExpr(UnaryOp.Not,
        new UnaryExpr(UnaryOp.Not,
          new ConstExpr(Type.getPrimitive(Type.TypeName.BOOLEAN), Boolean.TRUE)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Boolean.TRUE, value);

    try {
      unaryExpr = new UnaryExpr(UnaryOp.Not,
          new ConstExpr(Type.getPrimitive(Type.TypeName.INT), Integer.valueOf(42)));
      checker = new TypeChecker(new HashSymbolTable());
      unaryExpr.accept(checker);
      fail("Expected typechecker error on NOT(INTEGER)");
    } catch (TypeCheckException tce) {
      // expected this -- ok.
    }
  }

  @Test
  public void testNegate() throws Exception {
    Expr unaryExpr;
    TypeChecker checker;
    Object value;

    unaryExpr = new UnaryExpr(UnaryOp.Minus,
        new ConstExpr(Type.getPrimitive(Type.TypeName.INT), Integer.valueOf(10)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Integer.valueOf(-10), value);

    unaryExpr = new UnaryExpr(UnaryOp.Minus,
        new ConstExpr(Type.getPrimitive(Type.TypeName.INT), Integer.valueOf(-42)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Integer.valueOf(42), value);

    unaryExpr = new UnaryExpr(UnaryOp.Minus,
        new ConstExpr(Type.getPrimitive(Type.TypeName.INT), Integer.valueOf(0)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Integer.valueOf(0), value);

    unaryExpr = new UnaryExpr(UnaryOp.Minus,
        new ConstExpr(Type.getPrimitive(Type.TypeName.BIGINT), Long.valueOf(-42)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Long.valueOf(42), value);

    unaryExpr = new UnaryExpr(UnaryOp.Minus,
        new ConstExpr(Type.getPrimitive(Type.TypeName.FLOAT), Float.valueOf(-42f)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Float.valueOf(42f), value);

    unaryExpr = new UnaryExpr(UnaryOp.Minus,
        new ConstExpr(Type.getPrimitive(Type.TypeName.DOUBLE), Double.valueOf(-42)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Double.valueOf(42), value);

    unaryExpr = new UnaryExpr(UnaryOp.Minus,
        new ConstExpr(Type.getNullable(Type.TypeName.DOUBLE), null));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(null, value);

    unaryExpr = new UnaryExpr(UnaryOp.Minus,
        new UnaryExpr(UnaryOp.Minus,
          new ConstExpr(Type.getPrimitive(Type.TypeName.INT), Integer.valueOf(12))));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Integer.valueOf(12), value);

    try {
      unaryExpr = new UnaryExpr(UnaryOp.Minus,
          new ConstExpr(Type.getPrimitive(Type.TypeName.STRING), "hello"));
      checker = new TypeChecker(new HashSymbolTable());
      unaryExpr.accept(checker);
      fail("Expected typechecker error on -(STRING)");
    } catch (TypeCheckException tce) {
      // expected this -- ok.
    }
  }

  @Test
  public void testIsNull() throws Exception {
    Expr unaryExpr;
    TypeChecker checker;
    Object value;

    unaryExpr = new UnaryExpr(UnaryOp.IsNull,
        new ConstExpr(Type.getPrimitive(Type.TypeName.INT), Integer.valueOf(10)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Boolean.FALSE, value);

    unaryExpr = new UnaryExpr(UnaryOp.IsNull,
        new ConstExpr(Type.getNullable(Type.TypeName.INT), Integer.valueOf(10)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Boolean.FALSE, value);

    unaryExpr = new UnaryExpr(UnaryOp.IsNull,
        new ConstExpr(Type.getNullable(Type.TypeName.INT), null));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Boolean.TRUE, value);

    unaryExpr = new UnaryExpr(UnaryOp.IsNull,
        new ConstExpr(Type.getNullable(Type.TypeName.STRING), null));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Boolean.TRUE, value);
  }

  @Test
  public void testIsNotNull() throws Exception {
    Expr unaryExpr;
    TypeChecker checker;
    Object value;

    unaryExpr = new UnaryExpr(UnaryOp.IsNotNull,
        new ConstExpr(Type.getPrimitive(Type.TypeName.INT), Integer.valueOf(10)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Boolean.TRUE, value);

    unaryExpr = new UnaryExpr(UnaryOp.IsNotNull,
        new ConstExpr(Type.getNullable(Type.TypeName.INT), Integer.valueOf(10)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Boolean.TRUE, value);

    unaryExpr = new UnaryExpr(UnaryOp.IsNotNull,
        new ConstExpr(Type.getNullable(Type.TypeName.INT), null));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Boolean.FALSE, value);

    unaryExpr = new UnaryExpr(UnaryOp.IsNotNull,
        new ConstExpr(Type.getNullable(Type.TypeName.STRING), null));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Boolean.FALSE, value);
  }

  @Test
  public void testPositive() throws Exception {
    Expr unaryExpr;
    TypeChecker checker;
    Object value;

    unaryExpr = new UnaryExpr(UnaryOp.Plus,
        new ConstExpr(Type.getPrimitive(Type.TypeName.INT), Integer.valueOf(10)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Integer.valueOf(10), value);

    unaryExpr = new UnaryExpr(UnaryOp.Plus,
        new ConstExpr(Type.getPrimitive(Type.TypeName.INT), Integer.valueOf(-42)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Integer.valueOf(-42), value);

    unaryExpr = new UnaryExpr(UnaryOp.Plus,
        new ConstExpr(Type.getPrimitive(Type.TypeName.INT), Integer.valueOf(0)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Integer.valueOf(0), value);

    unaryExpr = new UnaryExpr(UnaryOp.Plus,
        new ConstExpr(Type.getPrimitive(Type.TypeName.BIGINT), Long.valueOf(-42)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Long.valueOf(-42), value);

    unaryExpr = new UnaryExpr(UnaryOp.Plus,
        new ConstExpr(Type.getPrimitive(Type.TypeName.FLOAT), Float.valueOf(-42f)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Float.valueOf(-42f), value);

    unaryExpr = new UnaryExpr(UnaryOp.Plus,
        new ConstExpr(Type.getPrimitive(Type.TypeName.DOUBLE), Double.valueOf(-42)));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Double.valueOf(-42), value);

    unaryExpr = new UnaryExpr(UnaryOp.Plus,
        new UnaryExpr(UnaryOp.Plus,
          new ConstExpr(Type.getPrimitive(Type.TypeName.INT), Integer.valueOf(12))));
    checker = new TypeChecker(new HashSymbolTable());
    unaryExpr.accept(checker);
    value = unaryExpr.eval(getEmptyEventWrapper());
    assertEquals(Integer.valueOf(12), value);

    try {
      unaryExpr = new UnaryExpr(UnaryOp.Plus,
          new ConstExpr(Type.getPrimitive(Type.TypeName.STRING), "hello"));
      checker = new TypeChecker(new HashSymbolTable());
      unaryExpr.accept(checker);
      fail("Expected typechecker error on +(STRING)");
    } catch (TypeCheckException tce) {
      // expected this -- ok.
    }
  }
}
