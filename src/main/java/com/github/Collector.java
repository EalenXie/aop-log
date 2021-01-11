package com.github;


/**
 * @author EalenXie create on 2021/1/4 16:51
 * 收集器
 */
@FunctionalInterface
public interface Collector<T> {

    /**
     * 收集数据对象
     *
     * @param data 数据对象
     */
    void collect(T data);
}
