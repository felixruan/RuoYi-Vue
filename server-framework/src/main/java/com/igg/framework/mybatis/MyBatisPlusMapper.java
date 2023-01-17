package com.igg.framework.mybatis;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.igg.common.core.domain.BaseEntity;

public interface MyBatisPlusMapper<T> extends BaseMapper<T> {

    CopyOptions COPY_OPTIONS = CopyOptions.create()
            .setIgnoreNullValue(true)
            .setFieldNameEditor(key -> StrUtil.toUnderlineCase(key))
            .setFieldValueEditor((key, value) -> ObjectUtil.isEmpty(value) ? null : value)
            .setIgnoreProperties(BaseEntity::getParams);

}
