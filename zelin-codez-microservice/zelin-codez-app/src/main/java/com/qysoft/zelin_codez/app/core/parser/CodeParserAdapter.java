package com.qysoft.zelin_codez.app.core.parser;

public interface CodeParserAdapter {

    /**
     * 代码代码解析方法
     *
     * @param content 生成的代码内容
     * @return 文件
     */
    Object codeParser(String content);
}
