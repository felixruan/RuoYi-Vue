package com.igg.framework.mybatis;

import com.baomidou.mybatisplus.extension.service.IService;
import com.igg.common.core.domain.BaseEntity;

import java.io.Serializable;
import java.util.List;

public interface MyBatisPlusService<T, V> extends IService<T> {

    V selectById(Serializable id);

    List<V> selectList(BaseEntity ro);

    boolean insert(BaseEntity ro);

    boolean update(BaseEntity ro);

    boolean deleteById(Serializable id);

    boolean deleteByIds(Serializable[] ids);

}
