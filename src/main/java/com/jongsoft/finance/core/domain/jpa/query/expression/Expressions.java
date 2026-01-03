package com.jongsoft.finance.core.domain.jpa.query.expression;

import com.jongsoft.finance.core.domain.jpa.query.BooleanExpression;
import com.jongsoft.finance.core.domain.jpa.query.ComputationExpression;
import jakarta.persistence.Query;

import java.security.SecureRandom;

public final class Expressions {
    private Expressions() {}

    public static BooleanExpression fieldLike(String tableAlias, String field, String value) {
        return new FieldCondition(
                tableAlias, generateRandomString(), field, FieldEquation.LIKE, "%" + value + "%");
    }

    public static BooleanExpression fieldNull(String tableAlias, String field) {
        return new FieldNull(tableAlias, field);
    }

    public static <T> BooleanExpression fieldCondition(
            String tableAlias, String field, FieldEquation equation, T value) {
        return new FieldCondition(tableAlias, generateRandomString(), field, equation, value);
    }

    public static <T> BooleanExpression fieldBetween(
            String tableAlias, String field, T start, T end) {
        return new FieldBetween<>(tableAlias, generateRandomString(), field, start, end);
    }

    public static BooleanExpression or(BooleanExpression left, BooleanExpression right) {
        return new OrExpression(left, right);
    }

    public static BooleanExpression and(BooleanExpression left, BooleanExpression right) {
        return new BooleanExpression() {
            @Override
            public String hqlExpression() {
                return "(%s AND %s)".formatted(left.hqlExpression(), right.hqlExpression());
            }

            @Override
            public void addParameters(Query query) {
                left.addParameters(query);
                right.addParameters(query);
            }

            @Override
            public BooleanExpression cloneWithAlias(String alias) {
                return and(left.cloneWithAlias(alias), right.cloneWithAlias(alias));
            }
        };
    }

    /**
     * Returns a computation expression representing the equality comparison between the left and
     * right computation expressions.
     *
     * @param left the left computation expression to compare
     * @param right the right computation expression to compare
     * @return a computation expression representing the equality comparison between the left and
     *     right expressions
     */
    public static ComputationExpression equals(
            ComputationExpression left, ComputationExpression right) {
        return () -> "(%s = %s)".formatted(left.hqlExpression(), right.hqlExpression());
    }

    /**
     * Creates a computation expression with the specified value.
     *
     * @param value the value to be represented in the computation expression
     * @return a computation expression representing the specified value
     */
    public static <T> ComputationExpression value(T value) {
        var encodedKey = "value_%s".formatted(generateRandomString());
        return new ComputationExpression() {
            @Override
            public String hqlExpression() {
                return " :%s ".formatted(encodedKey);
            }

            @Override
            public void addParameters(Query query) {
                query.setParameter(encodedKey, value);
            }
        };
    }

    /**
     * Returns a computation expression representing the specified field.
     *
     * @param field the field to represent in the computation expression
     * @return a computation expression representing the specified field
     */
    public static ComputationExpression field(String field) {
        return new ComputationExpression() {
            @Override
            public String hqlExpression() {
                return " %s ".formatted(field);
            }
            ;
        };
    }

    /**
     * Creates a new computation expression representing a CASE WHEN clause in SQL.
     *
     * @param expression the boolean expression to evaluate in the CASE WHEN clause
     * @param then the computation expression to use when the expression is true
     * @param elseValue the computation expression to use when the expression is false
     * @return a new computation expression representing the CASE WHEN clause
     */
    public static ComputationExpression caseWhen(
            BooleanExpression expression,
            ComputationExpression then,
            ComputationExpression elseValue) {
        return new ComputationExpression() {
            @Override
            public String hqlExpression() {
                return "CASE WHEN %s THEN %s ELSE %s END"
                        .formatted(
                                expression.hqlExpression(),
                                then.hqlExpression(),
                                elseValue.hqlExpression());
            }

            @Override
            public void addParameters(Query query) {
                expression.addParameters(query);
                then.addParameters(query);
                elseValue.addParameters(query);
            }
        };
    }

    /**
     * Creates a new computation expression representing the addition of two computation
     * expressions.
     *
     * @param left the left computation expression to add
     * @param right the right computation expression to add
     * @return a new computation expression representing the addition of the two computation
     *     expressions
     */
    public static ComputationExpression addition(
            ComputationExpression left, ComputationExpression right) {
        return new ComputationExpression() {
            @Override
            public String hqlExpression() {
                return "(%s + %s)".formatted(left.hqlExpression(), right.hqlExpression());
            }

            @Override
            public void addParameters(Query query) {
                left.addParameters(query);
                right.addParameters(query);
            }
        };
    }

    static String generateRandomString() {
        var random = new SecureRandom();
        var sb = new StringBuilder(12);
        var allowedChard = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        for (int i = 0; i < 12; i++) {
            int randomIndex = random.nextInt(allowedChard.length());
            sb.append(allowedChard.charAt(randomIndex));
        }

        return sb.toString();
    }
}
