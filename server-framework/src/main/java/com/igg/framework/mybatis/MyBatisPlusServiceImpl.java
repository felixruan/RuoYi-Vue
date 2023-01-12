package com.igg.framework.mybatis;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.igg.common.core.domain.BaseEntity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MyBatisPlusServiceImpl<M extends BaseMapper<T>, T, V> extends ServiceImpl<M, T> {

    private final static CopyOptions copyOptions = CopyOptions.create()
            .setIgnoreNullValue(true)
            .setFieldNameEditor(key -> StrUtil.toUnderlineCase(key))
            .setIgnoreProperties(BaseEntity::getParams);

    private Class<V> voClass = currentVoClass();

    private Class<V> currentVoClass() {
        return (Class<V>) ReflectionKit.getSuperClassGenericType(this.getClass(), MyBatisPlusServiceImpl.class, 2);
    }

    public V selectById(Serializable id) {
        T result = getById(id);
        return BeanUtil.toBean(result, currentVoClass());
    }

    public List<V> selectList(BaseEntity ro) {
        Map<String, Object> beanMap = BeanUtil.beanToMap(ro, new HashMap<>(), copyOptions);
        List<T> list = listByMap(beanMap);
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        return list.stream()
                .map(source -> BeanUtil.toBean(source, voClass))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean insert(BaseEntity ro) {
        T bean = BeanUtil.toBean(ro, entityClass);
        return save(bean);
    }

    public boolean update(BaseEntity ro) {
        T bean = BeanUtil.toBean(ro, entityClass);
        return updateById(bean);
    }

    public boolean deleteById(Serializable id) {
        return removeById(id);
    }

    public boolean deleteByIds(Serializable[] ids) {
        return removeByIds(Arrays.asList(ids));
    }

}
