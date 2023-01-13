package com.igg.system.domain;

import lombok.Data;

/**
 * 用户和岗位关联 sys_user_post
 *
 * @author 阮杰辉
 */
@Data
public class SysUserPost {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 岗位ID
     */
    private Long postId;

}
