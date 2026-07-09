package com.qysoft.zelin_codez.app.controller;

import com.mybatisflex.core.paginate.Page;
import com.qysoft.zelin_codez.app.service.ChatHistoryService;
import com.qysoft.zelin_codez.client.service.InnerUserService;
import com.qysoft.zelin_codez.common.Result;
import com.qysoft.zelin_codez.common.exception.ErrorCode;
import com.qysoft.zelin_codez.common.exception.ThrowUtils;
import com.qysoft.zelin_codez.model.entity.ChatHistory;
import com.qysoft.zelin_codez.model.entity.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 控制层。
 *
 * @author wudi
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private InnerUserService userService;

    /**
     * 保存对话历史。
     *
     * @param chatHistory 对话历史
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody ChatHistory chatHistory) {
        return chatHistoryService.save(chatHistory);
    }

    /**
     * 根据主键删除对话历史。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return chatHistoryService.removeById(id);
    }

    /**
     * 根据主键更新对话历史。
     *
     * @param chatHistory 对话历史
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody ChatHistory chatHistory) {
        return chatHistoryService.updateById(chatHistory);
    }

    /**
     * 查询所有对话历史。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<ChatHistory> list() {
        return chatHistoryService.list();
    }

    /**
     * 根据主键获取对话历史。
     *
     * @param id 对话历史主键
     * @return 对话历史详情
     */
    @GetMapping("getInfo/{id}")
    public ChatHistory getInfo(@PathVariable Long id) {
        return chatHistoryService.getById(id);
    }

    /**
     * 分页查询对话历史。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<ChatHistory> page(Page<ChatHistory> page) {
        return chatHistoryService.page(page);
    }

    /**
     * 游标分页查询聊天记录接口
     *
     * @param appId              应用Id
     * @param pageSize           分页大小
     * @param lastCreateTime     最后一次查询时间
     * @param HttpServletRequest 请求封装对象
     * @return 分页查询结果
     */
    @GetMapping("/app/{appId}")
    public Result<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                                        @RequestParam(value = "lastCreateTime", required = false) LocalDateTime lastCreateTime,
                                                        HttpServletRequest HttpServletRequest) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = InnerUserService.getLoginUser(HttpServletRequest);
        return Result.success(chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser));
    }
}
