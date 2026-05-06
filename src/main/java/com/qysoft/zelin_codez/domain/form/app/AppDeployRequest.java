package com.qysoft.zelin_codez.domain.form.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Data
public class AppDeployRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7087506411089083226L;
    /**
     * 部署的AppId
     */
    private Long appId;
}
