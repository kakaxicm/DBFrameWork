package com.qicode.kakaxicm.dbframework.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.qicode.kakaxicm.dbframework.db.annotation.DbField;
import com.qicode.kakaxicm.dbframework.db.annotation.DbTable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by chenming on 2018/6/25
 * 持有
 * 1.数据库
 * 2操作的类Class对象
 * 3.表名
 * 4.从数据库中的列名到Class的Field的映射表，用于生成ContentValue键值对
 */
public abstract class BaseDao<T> implements IDao<T> {
    /**
     * 操作的数据库
     */
    private SQLiteDatabase database;

    /**
     * 操作的类对象信息
     */
    private Class<T> entityClazz;

    /**
     * 操作的数据库表名，根据DbTable注解拿到
     */
    private String tableName;

    /**
     * key 为列名
     * value 为对应field
     */
    private HashMap<String, Field> tableToFieldMap;

    private boolean isInited;//只初始化一次

    /**
     * 初始化方法，只执行一次
     *
     * @param database
     * @param entityClazz
     */
    public synchronized void init(SQLiteDatabase database, Class<T> entityClazz) {
        this.database = database;
        this.entityClazz = entityClazz;
        if (!isInited) {
            isInited = true;
            //拿到表名
            DbTable tableAnno = entityClazz.getAnnotation(DbTable.class);
            if (tableAnno != null) {
                tableName = tableAnno.value();
            } else {
                tableName = entityClazz.getSimpleName();
            }
            Log.e("DB", "数据库表名" + tableName);
            if (!database.isOpen()) {
                Log.e("DB", "数据库未打开");
                return;
            }

            String createTbSql = getCreateTableSql();
            if (!TextUtils.isEmpty(createTbSql)) {
                database.execSQL(createTbSql);
            }

            //构建映射
            buildTbToFieldMap();
        }
    }

    private void buildTbToFieldMap() {
        tableToFieldMap = new HashMap<>();
        //找到
        String sql = "select * from " + this.tableName + " limit 1 , 0";
        Cursor cursor = null;
        cursor = database.rawQuery(sql, null);
        String[] columnNames = cursor.getColumnNames();//列名

        Field[] fields = entityClazz.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
        }

        //查找每一个列名对应的field
        for (String columName : columnNames) {
            Field columField = null;
            for (Field f : fields) {
                String colNameOnField = null;
                //获得field上的列名
                DbField annotation = f.getAnnotation(DbField.class);
                if (annotation != null) {
                    colNameOnField = annotation.value();
                } else {
                    colNameOnField = f.getName();
                }

                if (columName.equals(colNameOnField)) {
                    columField = f;
                    break;
                }
            }
            if (columField != null) {
                tableToFieldMap.put(columName, columField);
            }
        }
    }

    private ContentValues getTbContentValuesFromMap(T entity) {
        ContentValues contentValues = new ContentValues();
        Iterator<Field> iterator = tableToFieldMap.values().iterator();
        while (iterator.hasNext()){
            Field field = iterator.next();
            String key = null;
            String value = null;
            DbField annotation = field.getAnnotation(DbField.class);
            if(annotation != null){
                key = annotation.value();
            }else{
                key = field.getName();
            }
            Object result = null;
            try {
                result = field.get(entity);
                if(result == null){
                    continue;
                }else{
                    value = result.toString();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            contentValues.put(key,value);
        }
        return contentValues;
    }

    /**
     * 构造创建表的语句,子类实现，这部分可以写入由框架实现，后面再改进
     *
     * @return
     */
    abstract protected String getCreateTableSql();


    @Override
    public Long insert(T item) {
        ContentValues cv = getTbContentValuesFromMap(item);
        Long res = database.insert(tableName, null, cv);
        return res;
    }

    @Override
    public Long update(T item, T from) {
        return null;
    }
}
