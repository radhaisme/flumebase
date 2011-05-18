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

package com.odiago.flumebase.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.odiago.flumebase.util.StringUtils;

/**
 * A representation of an abstract type which can be unified to a specific
 * concrete type on a per-expression basis.
 *
 * <p>This is used by functions to specify arguments and return values of
 * variable type. These are only valid as argument and return types of
 * FnCallExprs.</p>
 *
 * <p>As an expression is typechecked, UniversalType instances will be
 * compared against the actual types of arguments to the function; constraints
 * generated by these comparisons will result in the UniversalType being
 * replaced in the official expression by a concrete type.</p>
 *
 * <p>UniversalType instances may carry constraints that restrict the
 * set of concrete types to which they may be unified. Each constraint
 * is a <i>promotesTo</i> relationship. e.g., adding a constraint of
 * <tt>Type.TypeName.TYPECLASS_NUMERIC</tt> asserts that the final type
 * this takes on must promote to TYPECLASS_NUMERIC.</p>
 *
 * <p>Note that the equals() method operates on the alias and the constraints;
 * if two instances of a UniversalType have the same alias and the same
 * constraints (or no constraints at all), they will be judged "equal;" if a
 * function has two unbound argument types that are unrelated, they should
 * be instantiated with different aliases, such as "'a" and "'b".
 * </p>
 */
public class UniversalType extends Type {
  private static final Logger LOG = LoggerFactory.getLogger(
      UniversalType.class.getName());

  /**
   * The set of type(classes) which constrain the set of values this type can
   * take on.
   */
  private List<Type> mConstraints;

  /**
   * A human-readable alias to distinguish this from other type variables in an expression.
   */
  private String mAlias;

  public UniversalType(String alias) {
    super(TypeName.UNIVERSAL);
    mAlias = alias;
    mConstraints = new ArrayList<Type>();
  }

  /**
   * Adds a type to the list of constraints for this type variable.
   */
  public void addConstraint(Type t) {
    mConstraints.add(t);
  }

  @Override
  public TypeName getPrimitiveTypeName() {
    return null;
  }

  public List<Type> getConstraints() {
    return mConstraints;
  }

  /**
   * We are a primitive type iff one of our constraints forces us to be.
   */
  @Override
  public boolean isPrimitive() {
    for (Type t : mConstraints) {
      if (t.isPrimitive()) {
        return true;
      }
    }

    return false;
  }

  /**
   * We are a numeric type iff one of our constraints forces us to be.
   */
  @Override
  public boolean isNumeric() {
    for (Type t : mConstraints) {
      if (t.isNumeric()) {
        return true;
      }
    }

    return false;
  }

  /**
   * We are a nullable type iff one of our constraints forces us to be.
   */
  @Override
  public boolean isNullable() {
    for (Type t : mConstraints) {
      if (t.isNullable()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("var(");
    sb.append(mAlias);
    if (mConstraints.size() > 0) {
      sb.append(", constraints={");
      StringUtils.formatList(sb, mConstraints);
      sb.append("}");
    }
    sb.append(")");
    return sb.toString();
  }

  /**
   * <p>Given a set of constraints 'actualConstraints' imposed by a specific
   * expression context for this type variable, determine the narrowest
   * type that satisfies all of mConstraints and actualConstraints.</p>
   * <p>All constraints in actualConstraints should be real types (e.g., INT)
   * and not abstract typeclasses, etc.</p>
   *
   * @return the narrowest type to satisfy all such constraints.
   * @throws TypeCheckException if no such type can be found.
   */
  public Type getRuntimeType(List<Type> actualConstraints) throws TypeCheckException {
    if (actualConstraints.size() == 0) {
      // We need a real type to start with.
      throw new TypeCheckException("Cannot make a concrete type from a "
          + "type variable without a binding constraint");
    }

    // Get the narrowest type that satisfies all the actualConstraints.
    Type candidate = actualConstraints.get(0);
    for (int i = 1; i < actualConstraints.size(); i++) {
      candidate = Type.meet(candidate, actualConstraints.get(i));
    }

    if (candidate.equals(Type.getPrimitive(Type.TypeName.NULL))
        || candidate.equals(Type.getNullable(Type.TypeName.NULL))) {
      LOG.debug("Returning NULL-typed implicitly-nullable value (numActuals="
          + actualConstraints.size() + ")");

      if (!candidate.isNullable()) {
        LOG.warn("Returning non-nullable NULL-typed runtime type: numActuals="
            + actualConstraints.size() + "; actuals=["
            + StringUtils.listToStr(actualConstraints) + "]");
      }
      return candidate.asNullable();
    }

    // Ensure that a concrete candidate type exists.
    if (!candidate.isConcrete()) {
      // TODO(aaron): The most concrete example we might get is just a Nullable 'NULL',
      // if we are inferring type from a NULL ConstExpr. This needs to be ok too.
      throw new TypeCheckException("Actual constraints are incompatible."); 
    }

    // Now that we've found the narrowest real type we can use,
    // make sure it handles all our built-in constraints. These may be real
    // or abstract types.
    for (Type constraint : mConstraints) {
      if (!candidate.promotesTo(constraint)) {
        throw new TypeCheckException("Candidate type " + candidate
            + " cannot satisfy constraint: " + constraint);
      }
    }

    // The candidate type passed all our tests.
    return candidate;
  }

  @Override
  public int hashCode() {
    int hash = mAlias.hashCode();
    for (Type constraint : mConstraints) {
      hash ^= constraint.hashCode();
    }

    return hash;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    } else if (other == null) {
      return false;
    } else if (!other.getClass().equals(getClass())) {
      return false;
    }

    UniversalType otherType = (UniversalType) other;
    if (!mAlias.equals(otherType.mAlias)) {
      return false;
    }

    if (mConstraints.size() != otherType.mConstraints.size()) {
      return false;
    }

    for (int i = 0; i < mConstraints.size(); i++) {
      if (!mConstraints.get(i).equals(otherType.mConstraints.get(i))) {
        return false;
      }
    }

    return true;
  }

  /** {@inheritDoc} */
  @Override
  public Type replaceUniversal(Map<Type, Type> universalMapping) throws TypeCheckException {
    Type replacement = universalMapping.get(this);

    if (null == replacement) {
      throw new TypeCheckException("No runtime binding for universal type: " + this);
    }

    LOG.debug("Resolved arg type from " + this + " to " + replacement);
    return replacement;
  }
}
