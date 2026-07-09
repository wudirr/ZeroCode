package com.qysoft.zelin_codez.domain.form.app;

import com.qysoft.zelin_codez.common.PageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用查询请求
 *
 * @author wudi
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 代码生成类型
     */
    private String codeGenType;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 优先级
     */
    private Integer priority;



}
