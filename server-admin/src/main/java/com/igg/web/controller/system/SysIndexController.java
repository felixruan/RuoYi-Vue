package com.igg.web.controller.system;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.igg.common.config.RuoYiConfig;
import com.igg.common.utils.StringUtils;

/**
 * 首页
 *
 * @author 阮杰辉
 */
@RestController
public class SysIndexController {
    /** 系统基础配置 */
    @Resource
    private RuoYiConfig serverConfig;

    /**
     * 访问首页，提示语
     */
    @RequestMapping("/")
    public String index() {
        return StringUtils.format("欢迎使用{}后台管理框架，当前版本：v{}，请通过前端地址访问。", serverConfig.getName(), serverConfig.getVersion());
    }
}
