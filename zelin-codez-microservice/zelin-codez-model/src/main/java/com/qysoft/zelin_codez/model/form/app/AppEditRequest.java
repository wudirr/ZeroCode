package com.qysoft.zelin_codez.model.form.app;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用编辑请求（用户编辑自己的应用，仅允许修改应用名称）
 *
 * @author wudi
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppEditRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "id不能为空")
    private Long id;

    /**
     * 应用名称
     */
    private String appName;
}
