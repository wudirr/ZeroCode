package com.qysoft.zelin_codez.common.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.qysoft.zelin_codez.exception.BusinessException;
import com.qysoft.zelin_codez.exception.ErrorCode;
import com.qysoft.zelin_codez.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

/**
 * @Description 网页截图工具类
 * @Author wudi
 * @Date 2026/5/28 11:46
 **/
@Slf4j
public class WebScreenshotUtils {

    private static final ThreadLocal<WebDriver> webDriverThreadLocal = new ThreadLocal<>();

    private static final int DEFAULT_WIDTH = 1600;

    private static final int DEFAULT_HEIGHT = 900;

    /**
     * 保存网页截图
     *
     * @param url 网页url
     */
    public static String saveWebPageScreenShot(String url) {
        try {
            //获取驱动
            WebDriver webDriver = getWebDriver();
            //打开网页
            webDriver.get(url);
            //等待页面加载完成
            waitPageLoad(webDriver);
            //执行网页截图
            byte[] imageBytes = executeScreenshot(webDriver);
            //保存图片
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screen_shots";
            final String SUFFIX = ".png";
            String savePath = rootPath + File.separator + RandomUtil.randomString(5) + SUFFIX;
            saveImage(imageBytes, savePath);
            //执行并且保存压缩图片
            final String COMPRESS_SUFFIX = "_compress.png";
            String compressRootPath = rootPath + File.separator + "compress_image";
            File compressRootFile = new File(compressRootPath);
            if (!compressRootFile.exists()) {
                boolean flag = compressRootFile.mkdirs();
                ThrowUtils.throwIf(!flag, ErrorCode.SYSTEM_ERROR, "创建压缩图片文件夹失败");
            }
            String compressPath = compressRootPath + File.separator + RandomUtil.randomString(5) + COMPRESS_SUFFIX;
            compressImage(savePath, compressPath);
            //删除原图片
            clearFile(savePath);
            log.info("保存网页截图成功,保存路径:{}", compressPath);
            return compressPath;
        } catch (Exception e) {
            log.error("保存网页截图失败,失败原因:{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存网页截图失败");
        }
    }

    /**
     * 根据当前的线程将获取对应的网页驱动
     *
     * @return 网页驱动对象
     */
    private static WebDriver getWebDriver() {
        WebDriver webDriver = webDriverThreadLocal.get();
        if (webDriver == null) {
            //这个时候需要初始化一个网页驱动对象
            webDriver = initEdgeDriver();
            webDriverThreadLocal.set(webDriver);
        }
        return webDriver;
    }

    /**
     * 保存图片
     *
     * @param imageByte 图片字节数组
     * @param savePath  图片保存路径
     * @return 保存路径
     */
    public static String saveImage(byte[] imageByte, String savePath) {
        try {
            File file = FileUtil.writeBytes(imageByte, savePath);
            String res = file.getAbsolutePath();
            log.info("保存图片成功,保存路径:{}", res);
            return res;
        } catch (Exception e) {
            log.error("保存图片失败,失败原因:{},保存路径:{}", e.getMessage(), savePath);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     */
    public static void clearFile(String filePath) {
        boolean flag = FileUtil.del(FileUtil.file(filePath));
        ThrowUtils.throwIf(!flag, ErrorCode.SYSTEM_ERROR, "删除文件失败");
    }

    /**
     * 压缩图片(原质量的30%)
     *
     * @param imagePath    原图片路径
     * @param compressPath 压缩图片路径
     * @return 压缩后的图片路径
     */
    public static String compressImage(String imagePath, String compressPath) {
        try {
            final float QUALITY = 0.3f;
            ImgUtil.compress(FileUtil.file(imagePath),
                    FileUtil.file(compressPath),
                    QUALITY);
            log.info("压缩图片成功,压缩路径:{}", compressPath);
            return compressPath;
        } catch (Exception e) {
            log.error("压缩图片失败,失败原因:{},路径:{}", e.getMessage(), imagePath);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待页面加载完毕
     *
     * @param webDriver 网页驱动
     */
    public static void waitPageLoad(WebDriver webDriver) {
        //创建驱动加载对象
        try {
            WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10L));
            webDriverWait.until(driver -> "complete".equals(((JavascriptExecutor) driver).executeScript("return document.readyState")));
            Thread.sleep(2000L);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载失败,失败原因:{}", e.getMessage());
        }
    }

    /**
     * 执行网页截图操作
     *
     * @param webDriver 网页驱动
     * @return 图片字节数组
     */
    public static byte[] executeScreenshot(WebDriver webDriver) {
        try {
            byte[] screenshotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            log.info("执行网页截图成功");
            return screenshotBytes;
        } catch (Exception e) {
            log.error("执行网页截图失败,失败原因:{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "执行网页截图失败");
        }
    }

    /**
     * 使用网页驱动管理器自动下载edge驱动并且初始化一个网页驱动对象
     *
     * @return 网页驱动对象
     */
    private static WebDriver initEdgeDriver() {
        try {
            // 关键：手动指定 Edge 驱动路径，绕过 Selenium Manager
            String edgeDriverPath = getEdgeDriverPath();
            System.setProperty("webdriver.edge.driver", edgeDriverPath);

            EdgeOptions options = new EdgeOptions();

            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", DEFAULT_WIDTH, DEFAULT_HEIGHT));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0");

            // 使用本地代理（Clash Verge）
            options.addArguments("--proxy-server=http://127.0.0.1:7897");

            // 忽略证书错误
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--allow-running-insecure-content");

            // 创建驱动（此时会使用手动指定的路径）
            WebDriver driver = new EdgeDriver(options);

            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));

            log.info("Edge浏览器初始化成功，驱动路径: {}", edgeDriverPath);
            return driver;

        } catch (Exception e) {
            log.error("初始化Edge浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始 Edge 浏览器失败");
        }
    }

    /**
     * 获取 Edge 驱动路径
     */
    private static String getEdgeDriverPath() {
        // 方案1：项目目录下的 drivers 文件夹
        String projectPath = System.getProperty("user.dir");
        String driverPath = projectPath + File.separator + "drivers" + File.separator + "msedgedriver.exe";

        File driverFile = new File(driverPath);
        if (driverFile.exists()) {
            log.info("找到 Edge 驱动: {}", driverPath);
            return driverPath;
        }

        // 方案2：常见的安装路径
        String[] commonPaths = {
                "C:/drivers/msedgedriver.exe",
                "D:/drivers/msedgedriver.exe",
                System.getProperty("user.home") + "/drivers/msedgedriver.exe",
                "C:/webdrivers/msedgedriver.exe"
        };

        for (String path : commonPaths) {
            driverFile = new File(path);
            if (driverFile.exists()) {
                log.info("找到 Edge 驱动: {}", path);
                return path;
            }
        }

        // 如果都没找到，抛出明确错误
        throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                "未找到 Edge 驱动，请手动下载并放置在: " + projectPath + File.separator + "drivers/msedgedriver.exe");
    }
}
