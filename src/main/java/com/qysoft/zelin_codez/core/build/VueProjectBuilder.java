package com.qysoft.zelin_codez.core.build;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @Description vue项目构建器
 * @Author wudi
 * @Date 2026/5/27 11:00
 **/
@Slf4j
public class VueProjectBuilder {

    /**
     * 安装依赖并且构建项目(异步,使用Java21的虚拟线程)
     *
     * @param workFile 执行目录
     */
    public void installAndBuildVueProjectAsync(File workFile){
        Thread.ofVirtual().name("vue-project-build-thread" + System.currentTimeMillis()).start(() -> {
            try {
                installAndBuildVueProject(workFile);
            } catch (Exception e) {
                log.error("异步构建vue项目失败,错误信息:{}", e.getMessage());
            }
        });
    }

    /**
     * 安装依赖并且构建项目
     *
     * @param workFile 执行目录
     * @return 执行结果
     */
    public boolean installAndBuildVueProject(File workFile) {
        try {
            File packJsonFile = new File(workFile, "package.json");
            if (!packJsonFile.exists() || !workFile.isDirectory()) {
                log.error("package.json文件不存在");
                return false;
            }
            boolean installRes = executeInstall(workFile);
            if (!installRes) {
                log.error("项目依赖下载失败");
                return false;
            }
            boolean buildRes = executeBuild(workFile);
            if (!buildRes) {
                log.error("项目构建失败");
                return false;
            }
            //检查是否生成dist目录
            File distFile = new File(workFile, "dist");
            if (!distFile.exists()) {
                log.error("dist目录不存在");
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Vue项目构建失败,错误信息:{}", e.getMessage());
            return false;
        }
    }

    /**
     * 执行前端项目依赖下载命令
     *
     * @param workFile 执行目录
     * @return 执行结果
     */
    public boolean executeInstall(File workFile) {
        log.info("开始下载项目依赖");
        //根据当前系统的环境
        String command = String.format("%s install", buildCommand("npm"));
        final long TIME_OUT = 20L;
        return executeCommand(workFile, command, TIME_OUT);
    }

    /**
     * 执行前端工程化项目的构建命令
     *
     * @param workFile 执行目录
     * @return 执行结果
     */
    public boolean executeBuild(File workFile) {
        log.info("开始构建项目");
        String command = String.format("%s run build", buildCommand("npm"));
        final long TIME_OUT = 20L;
        return executeCommand(workFile, command, TIME_OUT);
    }

    /**
     * 终端执行命令工具
     *
     * @param workFile 执行目录
     * @param command  执行命令
     * @param timeout  超时时间
     * @return 执行结果
     */
    public boolean executeCommand(File workFile, String command, Long timeout) {
        log.info("在文件:{}中,执行命令: {}", workFile, command);
        try {
            if (!workFile.exists()) {
                return false;
            }
            //按照空格分割
            Process process = RuntimeUtil.exec(null, workFile, command.split("\\s+"));
            boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令:{}执行超时,超时时间为:{}", command, timeout);
                process.destroyForcibly();
                return false;
            }
            int exit = process.exitValue();
            if (exit == 0) {
                log.info("命令:{}执行成功", command);
                return true;
            } else {
                log.error("命令:{}执行失败,错误码:{}", command, exit);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败:{},错误信息:{}", command, e.getMessage());
            return false;
        }
    }

    /**
     * 根据当前环境构建 npm install
     *
     * @param baseCommand 基础命令
     * @return 构建之后的命令 windows -> npm.cmd install 其他 -> npm install
     */
    public String buildCommand(String baseCommand) {
        //判断当前系统的环境
        boolean isWindow = System.getProperty("os.name").toLowerCase().contains("window");
        if (isWindow) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }
}
