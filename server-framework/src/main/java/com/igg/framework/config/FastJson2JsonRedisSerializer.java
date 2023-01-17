package com.igg.framework.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;

/**
 * Redis使用FastJson序列化
 *
 * @author 阮杰辉
 */
public class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private Class<T> clazz;

    public FastJson2JsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        if (t instanceof String) return (((String) t).replaceAll("\"", "\\\\\"")).getBytes(DEFAULT_CHARSET);
        else if (t instanceof java.util.Set) return JSON.toJSONString(t, "millis").getBytes(DEFAULT_CHARSET);
        else if (t instanceof java.util.Date) return String.valueOf(((java.util.Date) t).getTime()).getBytes(DEFAULT_CHARSET);
        return JSON.toJSONString(t, "millis", JSONWriter.Feature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);
        if (bytes[0] != '{' && bytes[0] != '[') str = "\"" + str + "\"";
        return JSON.parseObject(str, clazz, "millis", JSONReader.Feature.SupportAutoType);
    }

}
