package com.igg.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 *
 * @author 阮杰辉
 */
@Component
@ConfigurationProperties(prefix = "project")
@Data
public class ProjectConfig {

    /**
     * 项目名称
     */
    private String name;

    /**
     * 版本
     */
    private String version;

    /**
     * 版权年份
     */
    private String copyrightYear;

    /**
     * 实例演示开关
     */
    private boolean demoEnabled;

    /**
     * 上传路径
     */
    private static String profile;

    /**
     * 获取地址开关
     */
    private static boolean addressEnabled;

    /**
     * 验证码类型
     */
    private static String captchaType;

    public static String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        ProjectConfig.profile = profile;
    }

    public static boolean isAddressEnabled() {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled) {
        ProjectConfig.addressEnabled = addressEnabled;
    }

    public static String getCaptchaType() {
        return captchaType;
    }

    public void setCaptchaType(String captchaType) {
        ProjectConfig.captchaType = captchaType;
    }

    /**
     * 获取导入上传路径
     */
    public static String getImportPath() {
        return getProfile() + "/import";
    }

    /**
     * 获取头像上传路径
     */
    public static String getAvatarPath() {
        return getProfile() + "/avatar";
    }

    /**
     * 获取下载路径
     */
    public static String getDownloadPath() {
        return getProfile() + "/download/";
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath() {
        return getProfile() + "/upload";
    }

}
