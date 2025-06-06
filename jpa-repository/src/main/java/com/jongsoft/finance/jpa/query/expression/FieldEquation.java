package com.jongsoft.finance.jpa.query.expression;

/**
 * Enumeration representing various field equations that can be used in expressions. The options
 * include EQ for equality, GTE for greater than or equal to, LT for less than, LTE for less than or
 * equal to, IN for inclusion, NIN for exclusion, and LIKE for string pattern matching.
 */
public enum FieldEquation {

  /** Enumeration representing the field equation option for equality comparison in expressions. */
  EQ,
  /**
   * Represents the field equation option for greater than or equal to comparison in expressions.
   */
  GTE,
  /** Represents the field equation option for less than to comparison in expressions. */
  LT,
  /**
   * Enumeration representing the field equation option for less than or equal to comparison in
   * expressions.
   */
  LTE,
  /** Represents the "IN" field equation option for inclusion checks in expressions. */
  IN,
  /** Represents exclusion field equation for expressions. */
  NIN,
  /** Represents the field equation option for string pattern matching in expressions. */
  LIKE;
}
