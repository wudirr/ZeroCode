package com.qysoft.zelin_codez.langgraph4j.node;

import com.qysoft.zelin_codez.common.utils.SpringContextUtil;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.langgraph4j.ai.ImageCollectService;
import com.qysoft.zelin_codez.langgraph4j.model.WorkFlowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片搜集节点
 *
 * @author qysoft
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class ImageCollectNode {

    /**
     * 进行图片搜集
     *
     * @return 异步的结点执行结果
     */
    public static AsyncNodeAction<MessagesState<String>> collectImage() {
        return node_async(state -> {
            try {
                log.info("开始执行搜集图片操作");
                //获取上下文对象
                WorkFlowContext context = WorkFlowContext.getContext(state);
                ImageCollectService imageCollectService = SpringContextUtil.getBean(ImageCollectService.class);
                //使用AI收集图片服务收集图片
                String imageListStr = imageCollectService.collectImage(context.getInitPrompt());
                context.setImageListStr(imageListStr);
                context.setCurrentStep("搜集图片");
                log.info("执行搜集图片成功,图片资源:{}", imageListStr);
                //保存上下文
                return WorkFlowContext.saveContext(context);
            } catch (Exception e) {
                log.error("执行搜集图片失败", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        });
    }
}
