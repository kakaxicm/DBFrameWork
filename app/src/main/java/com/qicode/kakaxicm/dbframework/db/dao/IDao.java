package com.qicode.kakaxicm.dbframework.db.dao;

/**
 * Created by chenming on 2018/6/25
 * DAO顶层接口,泛型为实体类对象
 */
public interface IDao<T> {
    /**
     * 插入数据
     * @param item
     * @return
     */
    Long insert(T item);

    /**
     * 更新
     * @param item 新数据
     * @param from 旧数据
     * @return
     */
    Long update(T item, T from);
}
