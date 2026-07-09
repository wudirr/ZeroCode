package com.qysoft.zelin_codez.controller;

import com.mybatisflex.core.paginate.Page;
import com.qysoft.zelin_codez.domain.entity.ChatEventLog;
import com.qysoft.zelin_codez.service.ChatEventLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天事件日志 控制层。
 *
 * @author wudi
 */
@RestController
@RequestMapping("/chatEventLog")
public class ChatEventLogController {

    @Autowired
    private ChatEventLogService chatEventLogService;

    /**
     * 保存聊天事件日志。
     *
     * @param chatEventLog 聊天事件日志
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody ChatEventLog chatEventLog) {
        return chatEventLogService.save(chatEventLog);
    }

    /**
     * 根据主键删除聊天事件日志。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return chatEventLogService.removeById(id);
    }

    /**
     * 根据主键更新聊天事件日志。
     *
     * @param chatEventLog 聊天事件日志
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody ChatEventLog chatEventLog) {
        return chatEventLogService.updateById(chatEventLog);
    }

    /**
     * 查询所有聊天事件日志。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<ChatEventLog> list() {
        return chatEventLogService.list();
    }

    /**
     * 根据主键获取聊天事件日志。
     *
     * @param id 聊天事件日志主键
     * @return 聊天事件日志详情
     */
    @GetMapping("getInfo/{id}")
    public ChatEventLog getInfo(@PathVariable Long id) {
        return chatEventLogService.getById(id);
    }

    /**
     * 分页查询聊天事件日志。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<ChatEventLog> page(Page<ChatEventLog> page) {
        return chatEventLogService.page(page);
    }

}
