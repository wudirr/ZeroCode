package com.qysoft.zelin_codez.ai.guardrails;

import com.qysoft.zelin_codez.common.constant.GlobalConstant;
import com.qysoft.zelin_codez.exception.BusinessException;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Description 提示词输入护轨
 * @Author wudi
 * @Date 2026/6/16 17:00
 **/
public class PromptSafetyInputGuardrail implements InputGuardrail {
    // 敏感词列表
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "忽略之前的指令", "ignore previous instructions", "ignore above",
            "破解", "hack", "绕过", "bypass", "越狱", "jailbreak"
    );

    // 注入攻击模式
    private static final List<Pattern> INJECTION_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)ignore\\s+(?:previous|above|all)\\s+(?:instructions?|commands?|prompts?)"),
            Pattern.compile("(?i)(?:forget|disregard)\\s+(?:everything|all)\\s+(?:above|before)"),
            Pattern.compile("(?i)(?:pretend|act|behave)\\s+(?:as|like)\\s+(?:if|you\\s+are)"),
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),
            Pattern.compile("(?i)new\\s+(?:instructions?|commands?|prompts?)\\s*:")
    );

    /**
     * 输入护轨校验机制
     *
     * @param userMessage 用户消息
     * @return 验证结果
     */
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String input = userMessage.singleText();
        if (input.length() > 1000) {
            return fatal(GlobalConstant.INPUT_TOO_LONG, new BusinessException(GlobalConstant.INPUT_TOO_LONG));
        }
        //检查内容
        if (input.trim().isEmpty()) {
            return fatal(GlobalConstant.INPUT_EMPTY);
        }
        //检查是否存在敏感词
        if (SENSITIVE_WORDS.contains(input.trim().toLowerCase())) {
            return fatal(GlobalConstant.INPUT_EXCEPTION, new BusinessException(GlobalConstant.INPUT_EXCEPTION));
        }
        //检查是否存在注入攻击
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return fatal(GlobalConstant.INPUT_EXCEPTION, new BusinessException(GlobalConstant.INPUT_EXCEPTION));
            }
        }
        return success();
    }
}
