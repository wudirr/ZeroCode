package com.qysoft.zelin_codez.ai.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 流式响应消息基类
 * @Author wudi
 * @Date 2026/5/25 11:19
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamMessage {

    protected String type;
}
