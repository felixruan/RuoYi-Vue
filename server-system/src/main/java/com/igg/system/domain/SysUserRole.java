package com.igg.system.domain;

import lombok.Data;

/**
 * 用户和角色关联 sys_user_role
 *
 * @author 阮杰辉
 */
@Data
public class SysUserRole {

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;

}
