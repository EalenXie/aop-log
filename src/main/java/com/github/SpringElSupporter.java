package com.github;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by EalenXie on 2021/7/12 13:45
 * 支持SpringEL
 */
public class SpringElSupporter {

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final Map<String, Expression> expressions = new HashMap<>();
    private final Pattern pattern = Pattern.compile("^[^/:*+?|<>=#${}\\[\\]-]+$");

    public Object getByExpression(Method method, Object target, Object[] args, String expressionString) {
        try {
            if (StringUtils.hasText(expressionString)) {
                Matcher matcher = pattern.matcher(expressionString);
                if (!matcher.find()) {
                    Expression expression;
                    MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(new ExpressionRootObject(target, args), method, args, paramNameDiscoverer);
                    if (expressions.containsKey(expressionString)) {
                        return expressions.get(expressionString).getValue(evaluationContext);
                    } else {
                        expression = parser.parseExpression(expressionString);
                        Object value = expression.getValue(evaluationContext);
                        expressions.put(expressionString, expression);
                        return value;
                    }
                }
            }
        } catch (Exception e) {
            // ignore any exception
        }
        return expressionString;
    }

    static class ExpressionRootObject {

        private Object target;

        private Object[] args;

        public ExpressionRootObject(Object target, Object[] args) {
            this.target = target;
            this.args = args;
        }

        public Object getTarget() {
            return target;
        }

        public void setTarget(Object target) {
            this.target = target;
        }

        public Object[] getArgs() {
            return args;
        }

        public void setArgs(Object[] args) {
            this.args = args;
        }
    }
}
