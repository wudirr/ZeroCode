package com.qysoft.zelin_codez.core;

import com.qysoft.zelin_codez.ai.AiCodeGeneratorService;
import com.qysoft.zelin_codez.common.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
    }

    @Test
    void generateAndSaveCodeStream() throws InterruptedException {
        Flux<String> result = aiCodeGeneratorFacade.generateAndSaveCodeStream("帮我生成一个前端登录界面,不超过20行", CodeGenTypeEnum.HTML,0L);
        result.doOnNext(System.out::println).doOnComplete(() -> {
            System.out.println("完成");
        });
        Thread.sleep(10000);
    }
}