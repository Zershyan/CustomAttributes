package io.zershyan.customattributes.attribute.range;

/** 范围端点表达式：支持四则运算与通配符，求值结果参与大小比较后决定属性是否可调。 */
public sealed interface RangeExpression
        permits RangeExpression.Constant, RangeExpression.Variable, RangeExpression.Negate, RangeExpression.Binary {

    double evaluate(RangeContext context);

    record Constant(double value) implements RangeExpression {
        @Override
        public double evaluate(RangeContext context) {
            return value;
        }
    }

    record Variable(Kind kind) implements RangeExpression {
        @Override
        public double evaluate(RangeContext context) {
            return switch (kind) {
                case CURRENT -> context.current();
                case BASE -> context.base();
                case DEFAULT -> context.defaultValue();
                case MIN -> context.min();
                case MAX -> context.max();
                case INFINITY -> Double.POSITIVE_INFINITY;
            };
        }
    }

    record Negate(RangeExpression operand) implements RangeExpression {
        @Override
        public double evaluate(RangeContext context) {
            return -operand.evaluate(context);
        }
    }

    record Binary(RangeExpression left, Operator operator, RangeExpression right) implements RangeExpression {
        @Override
        public double evaluate(RangeContext context) {
            double a = left.evaluate(context);
            double b = right.evaluate(context);
            return switch (operator) {
                case ADD -> a + b;
                case SUBTRACT -> a - b;
                case MULTIPLY -> a * b;
                case DIVIDE -> a / b;
            };
        }
    }

    enum Kind {
        CURRENT, BASE, DEFAULT, MIN, MAX, INFINITY
    }

    enum Operator {
        ADD, SUBTRACT, MULTIPLY, DIVIDE
    }
}
