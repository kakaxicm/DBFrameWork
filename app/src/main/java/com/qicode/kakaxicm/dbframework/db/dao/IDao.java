package com.qicode.kakaxicm.dbframework.db.dao;

import java.util.List;

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
     * @param where 条件
     * @return
     */
    int update(T item, T where);

    /**
     * 删除数据
     * @param where
     * @return
     */
    int delete(T where);

    /**
     * 查询数据
     */
    List<T> query(T where);


    List<T> query(T where,String orderBy,Integer startIndex,Integer limit);
}
