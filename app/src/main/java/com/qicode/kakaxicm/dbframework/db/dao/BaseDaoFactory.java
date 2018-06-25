package com.qicode.kakaxicm.dbframework.db.dao;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by chenming on 2018/6/25
 */
public class BaseDaoFactory {
    private String sqliteDatabasePath;
    private SQLiteDatabase sqLiteDatabase;
    private static BaseDaoFactory instance = new BaseDaoFactory();

    private BaseDaoFactory() {
        String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dir = new File(parentPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        sqliteDatabasePath = parentPath + "/demo.db";
        File f = new File(sqliteDatabasePath);
        if(!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.e("DB", "数据库路径:"+sqliteDatabasePath);
        sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(f, null);
        if(sqLiteDatabase != null){
            Log.e("DB", "数据库创建成功");
        }
    }

    public static BaseDaoFactory getInstance() {
        return instance;
    }

    /**
     * 生产DAO
     * T 为BaseDao子类
     * M 为实体类
     */
    public synchronized <T extends BaseDao<M>, M> T getBaseDao(Class<T> daoClass, Class<M> entityClass) {
        BaseDao dao = null;
        try {
            dao = daoClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        dao.init(sqLiteDatabase, entityClass);
       return (T)dao;
    }


}
