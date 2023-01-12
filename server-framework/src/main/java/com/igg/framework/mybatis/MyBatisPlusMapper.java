package com.igg.framework.mybatis;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.igg.common.core.domain.BaseEntity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public interface MyBatisPlusMapper<T, V> extends BaseMapper<T> {

    CopyOptions copyOptions = CopyOptions.create()
            .setIgnoreNullValue(true)
            .setFieldNameEditor(key -> StrUtil.toUnderlineCase(key))
            .setIgnoreProperties(BaseEntity::getParams);

    default Class<T> currentModelClass() {
        return (Class<T>) ReflectionKit.getSuperClassGenericType(this.getClass(), MyBatisPlusMapper.class, 0);
    }

    default Class<V> currentVoClass() {
        return (Class<V>) ReflectionKit.getSuperClassGenericType(this.getClass(), MyBatisPlusMapper.class, 1);
    }

    default V select(Serializable id) {
        T result = selectById(id);
        return BeanUtil.toBean(result, currentVoClass());
    }

    default List<V> selectList(BaseEntity ro) {
        Map<String, Object> beanMap = BeanUtil.beanToMap(ro, new HashMap<>(), copyOptions);
        List<T> list = selectByMap(beanMap);
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        return list.stream()
                .map(source -> BeanUtil.toBean(source, currentVoClass()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    default int insert(BaseEntity ro) {
        return insert(BeanUtil.toBean(ro, currentModelClass()));
    }

    default int update(BaseEntity ro) {
        return updateById(BeanUtil.toBean(ro, currentModelClass()));
    }

    default int deleteByIds(Serializable[] ids) {
        return deleteBatchIds(Arrays.asList(ids));
    }

}
