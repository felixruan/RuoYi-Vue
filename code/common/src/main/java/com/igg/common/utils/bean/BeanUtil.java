package com.igg.common.utils.bean;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class BeanUtil extends cn.hutool.core.bean.BeanUtil {

    /**
     * Map转换为Bean对象
     *
     * @param <T>           Bean类型
     * @param map           {@link Map}
     * @param beanClass     Bean Class
     * @return Bean
     */
    public static <T> T mapToBean(Map<?, ?> map, Class<T> beanClass) {
        return mapToBean(map, beanClass, true, CopyOptions.create());
    }

    /**
     * 判断source与target的所有公共字段的值是否相同
     *
     * @param source           待检测对象1
     * @param target           待检测对象2
     * @param ignoreNullValue  是否忽略值为空的字段
     * @param ignoreProperties 不需要检测的字段
     * @return 判断结果，如果为true则证明所有字段的值都相同
     */
    public static boolean isCommonFieldsEqual(Object source, Object target, boolean ignoreNullValue, String...ignoreProperties) {
        if (null == source && null == target) return true;
        if (null == source || null == target) return false;
        Map<String, Object> sourceFieldsMap = cn.hutool.core.bean.BeanUtil.beanToMap(source,false, ignoreNullValue);
        Map<String, Object> targetFieldsMap = cn.hutool.core.bean.BeanUtil.beanToMap(target, false, false);
        Set<String> sourceFields = sourceFieldsMap.keySet();
        sourceFields.removeAll(Arrays.asList(ignoreProperties));
        for (String field : sourceFields) {
            if(ObjectUtil.notEqual(sourceFieldsMap.get(field), targetFieldsMap.get(field))){
                return false;
            }
        }
        return true;
    }
}
