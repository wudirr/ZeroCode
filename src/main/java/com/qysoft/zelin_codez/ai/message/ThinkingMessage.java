package com.qysoft.zelin_codez.ai.message;

import dev.langchain4j.model.chat.response.PartialThinking;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 思考消息模型
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ThinkingMessage extends StreamMessage {

    private String data;

    public ThinkingMessage(PartialThinking partialThinking) {
        super(StreamMessageTypeEnum.THINKING_CONTENT.getValue());
        this.data = partialThinking == null ? "" : partialThinking.text();
    }
}
