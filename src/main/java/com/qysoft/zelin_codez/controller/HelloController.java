package com.qysoft.zelin_codez.controller;

import com.qysoft.zelin_codez.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description TODO
 * @Author wudi
 * @Date 2026/4/3 14:55
 **/
@RestController
public class HelloController {

    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Hello World");
    }
}
