package com.igg.system.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.igg.common.utils.StringUtils;

/**
 * 缓存信息
 *
 * @author 阮杰辉
 */
@Data
@NoArgsConstructor
public class SysCache {

    /** 缓存名称 */
    private String cacheName = "";

    /** 缓存键名 */
    private String cacheKey = "";

    /** 缓存内容 */
    private String cacheValue = "";

    /** 备注 */
    private String remark = "";

    public SysCache(String cacheName, String remark) {
        this.cacheName = cacheName;
        this.remark = remark;
    }

    public SysCache(String cacheName, String cacheKey, String cacheValue) {
        this.cacheName = StringUtils.replace(cacheName, ":", "");
        this.cacheKey = StringUtils.replace(cacheKey, cacheName, "");
        this.cacheValue = cacheValue;
    }

}
