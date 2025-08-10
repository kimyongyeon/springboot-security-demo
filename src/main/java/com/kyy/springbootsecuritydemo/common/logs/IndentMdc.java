package com.kyy.springbootsecuritydemo.common.logs;

import org.slf4j.MDC;

import java.util.ArrayDeque;
import java.util.Deque;

public class IndentMdc {
    private static final String KEY_STACK = "indentStack";
    private static final String KEY_INDENT = "indent";

    public static void push(String label) {
        Deque<String> stack = get();
        stack.push(label);
        MDC.put(KEY_STACK, String.join(">", stack)); // 필요하면 추적용
        MDC.put(KEY_INDENT, renderIndent(stack.size()));
    }

    public static void pop() {
        Deque<String> stack = get();
        if (!stack.isEmpty()) stack.pop();
        MDC.put(KEY_STACK, String.join(">", stack));
        MDC.put(KEY_INDENT, renderIndent(stack.size()));
    }

    public static void clear() {
        MDC.remove(KEY_STACK);
        MDC.remove(KEY_INDENT);
    }

    private static Deque<String> get() {
        @SuppressWarnings("unchecked")
        Deque<String> stack = (Deque<String>) LocalStack.HOLDER.get();
        if (stack == null) {
            stack = new ArrayDeque<>();
            LocalStack.HOLDER.set(stack);
        }
        return stack;
    }

    private static String renderIndent(int depth) {
        // 예: ├─, │  로 트리 느낌; 단순 공백 원하면 "  ".repeat(depth-1) 로 변경
        if (depth <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < depth; i++) sb.append("│  ");
        sb.append("├─ ");
        return sb.toString();
    }

    /** 요청 lifecycle 끝에 반드시 clear 필요 */
    private static final class LocalStack {
        static final ThreadLocal<Deque<String>> HOLDER = new ThreadLocal<>();
    }
}