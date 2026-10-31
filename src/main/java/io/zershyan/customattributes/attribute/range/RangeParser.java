package io.zershyan.customattributes.attribute.range;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 解析 {@code [下界, 上界]} 形式的范围串。两端都是表达式，支持四则运算、括号与通配符。
 */
public final class RangeParser {
    private final String source;
    private int index;

    private RangeParser(String source) {
        this.source = source;
    }

    public static ParsedRange parse(String text) {
        String body = stripBrackets(text);
        List<String> endpoints = splitEndpoints(body);
        if (endpoints.size() != 2) {
            throw new IllegalArgumentException("范围必须写成 [下界, 上界]：" + text);
        }
        return new ParsedRange(parseExpression(endpoints.get(0)), parseExpression(endpoints.get(1)));
    }

    private static RangeExpression parseExpression(String text) {
        RangeParser parser = new RangeParser(text);
        RangeExpression expression = parser.expression();
        parser.skipSpaces();
        if (parser.index < parser.source.length()) {
            throw parser.error("多余的内容");
        }
        return expression;
    }

    /** 容忍全角标点，方便直接在中文输入法下编辑配置。 */
    private static String stripBrackets(String text) {
        String normalized = text.trim()
                .replace('，', ',')
                .replace('［', '[')
                .replace('］', ']')
                .replace('（', '(')
                .replace('）', ')');
        if (normalized.length() < 2 || normalized.charAt(0) != '[' || normalized.charAt(normalized.length() - 1) != ']') {
            throw new IllegalArgumentException("范围必须写在方括号里：" + text);
        }
        return normalized.substring(1, normalized.length() - 1);
    }

    private static List<String> splitEndpoints(String body) {
        List<String> parts = new ArrayList<>(2);
        int depth = 0;
        int start = 0;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                parts.add(body.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(body.substring(start));
        return parts;
    }

    private RangeExpression expression() {
        RangeExpression left = term();
        while (true) {
            if (consume('+')) {
                left = new RangeExpression.Binary(left, RangeExpression.Operator.ADD, term());
            } else if (consume('-')) {
                left = new RangeExpression.Binary(left, RangeExpression.Operator.SUBTRACT, term());
            } else {
                return left;
            }
        }
    }

    private RangeExpression term() {
        RangeExpression left = unary();
        while (true) {
            if (consume('*')) {
                left = new RangeExpression.Binary(left, RangeExpression.Operator.MULTIPLY, unary());
            } else if (consume('/')) {
                left = new RangeExpression.Binary(left, RangeExpression.Operator.DIVIDE, unary());
            } else {
                return left;
            }
        }
    }

    private RangeExpression unary() {
        if (consume('-')) {
            return new RangeExpression.Negate(unary());
        }
        if (consume('+')) {
            return unary();
        }
        return primary();
    }

    private RangeExpression primary() {
        if (consume('(')) {
            RangeExpression inner = expression();
            if (!consume(')')) {
                throw error("缺少 ')'");
            }
            return inner;
        }
        char c = peek();
        if (c == '\0') {
            throw error("内容不完整");
        }
        if (Character.isDigit(c) || c == '.') {
            return number();
        }
        if (isIdentifierStart(c)) {
            return variable();
        }
        throw error("无法解析的字符 '" + c + "'");
    }

    private RangeExpression number() {
        int start = index;
        while (index < source.length() && (Character.isDigit(source.charAt(index)) || source.charAt(index) == '.')) {
            index++;
        }
        if (index < source.length() && (source.charAt(index) == 'e' || source.charAt(index) == 'E')) {
            int mark = index++;
            if (index < source.length() && (source.charAt(index) == '+' || source.charAt(index) == '-')) {
                index++;
            }
            if (index < source.length() && Character.isDigit(source.charAt(index))) {
                while (index < source.length() && Character.isDigit(source.charAt(index))) {
                    index++;
                }
            } else {
                index = mark;
            }
        }
        String literal = source.substring(start, index);
        try {
            return new RangeExpression.Constant(Double.parseDouble(literal));
        } catch (NumberFormatException e) {
            throw error("非法数字 '" + literal + "'");
        }
    }

    private RangeExpression variable() {
        int start = index;
        while (index < source.length() && isIdentifierPart(source.charAt(index))) {
            index++;
        }
        return new RangeExpression.Variable(kindOf(source.substring(start, index)));
    }

    private RangeExpression.Kind kindOf(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "current", "now", "当前值", "现值" -> RangeExpression.Kind.CURRENT;
            case "base", "基础值" -> RangeExpression.Kind.BASE;
            case "default", "默认值" -> RangeExpression.Kind.DEFAULT;
            case "min", "最小值" -> RangeExpression.Kind.MIN;
            case "max", "最大值" -> RangeExpression.Kind.MAX;
            case "inf", "infinity", "无穷", "∞" -> RangeExpression.Kind.INFINITY;
            default -> throw error("未知通配符 '" + name + "'");
        };
    }

    private static boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '∞';
    }

    private static boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '∞';
    }

    private void skipSpaces() {
        while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
    }

    private char peek() {
        return index < source.length() ? source.charAt(index) : '\0';
    }

    private boolean consume(char expected) {
        skipSpaces();
        if (peek() != expected) {
            return false;
        }
        index++;
        return true;
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException("范围表达式 \"" + source + "\" 解析失败：" + message + "（位置 " + index + "）");
    }

    public record ParsedRange(RangeExpression lower, RangeExpression upper) {
    }
}
