package com.qicode.kakaxicm.dbframework.client;

import com.qicode.kakaxicm.dbframework.db.dao.BaseDao;

/**
 * Created by chenming on 2018/6/25
 */
public class UserDao extends BaseDao<User>{
    @Override
    protected String getCreateTableSql() {
        return "create table if not exists user(tb_name varchar(20),tb_pswd varchar(10))";
    }
}
