package com.qysoft.zelin_codez.ai.monitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 观测上下文对象
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorContext implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long appId;
}
