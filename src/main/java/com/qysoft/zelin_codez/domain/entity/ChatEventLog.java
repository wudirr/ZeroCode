package com.qysoft.zelin_codez.domain.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天事件日志 实体类。
 *
 * @author wudi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_event_log")
public class ChatEventLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 应用 id
     */
    @Column("appId")
    private Long appId;

    /**
     * 会话内存 id（appId_codeGenType）
     */
    @Column("memoryId")
    private String memoryId;

    /**
     * 一轮对话标识
     */
    @Column("turnId")
    private String turnId;

    /**
     * 同一轮内事件顺序
     */
    private Integer seq;

    /**
     * 代码生成类型（html/multi_file/vue_project）
     */
    @Column("codeGenType")
    private String codeGenType;

    /**
     * 消息角色（user/assistant/tool/system）
     */
    private String role;

    /**
     * 事件类型
     */
    @Column("eventType")
    private String eventType;

    /**
     * 消息文本内容
     */
    private String content;

    /**
     * 深度思考内容
     */
    @Column("reasoningContent")
    private String reasoningContent;

    /**
     * 工具调用 id
     */
    @Column("toolCallId")
    private String toolCallId;

    /**
     * 工具名称
     */
    @Column("toolName")
    private String toolName;

    /**
     * 工具参数（json）
     */
    @Column("toolArguments")
    private String toolArguments;

    /**
     * 工具执行结果
     */
    @Column("toolResult")
    private String toolResult;

    /**
     * 原始事件 json（审计/排障）
     */
    @Column("rawEventJson")
    private String rawEventJson;

    /**
     * 用户 id
     */
    @Column("userId")
    private Long userId;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
