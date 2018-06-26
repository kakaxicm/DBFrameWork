package com.qicode.kakaxicm.dbframework.client;

import com.qicode.kakaxicm.dbframework.db.annotation.DbField;
import com.qicode.kakaxicm.dbframework.db.annotation.DbTable;

/**
 * Created by chenming on 2018/6/25
 */
@DbTable("user")
public class User {
    @DbField("tb_name")
    private String name;

    @DbField("tb_pswd")
    private String password;

    public User() {
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
