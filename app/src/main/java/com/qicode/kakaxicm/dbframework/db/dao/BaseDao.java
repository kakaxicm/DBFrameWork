package com.qicode.kakaxicm.dbframework.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.qicode.kakaxicm.dbframework.db.annotation.DbField;
import com.qicode.kakaxicm.dbframework.db.annotation.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private ContentValues getDBContentValuesFrom(T entity) {
        ContentValues contentValues = new ContentValues();
        Iterator<Field> iterator = tableToFieldMap.values().iterator();
        while (iterator.hasNext()) {
            Field field = iterator.next();
            String key = null;
            String value = null;
            DbField annotation = field.getAnnotation(DbField.class);
            if (annotation != null) {
                key = annotation.value();
            } else {
                key = field.getName();
            }
            Object result = null;
            try {
                result = field.get(entity);
                if (result == null) {
                    continue;
                } else {
                    value = result.toString();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            contentValues.put(key, value);
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
        ContentValues cv = getDBContentValuesFrom(item);
        Long res = database.insert(tableName, null, cv);
        return res;
    }

    @Override
    public int update(T item, T where) {
        //参数 表名,
//        update(String table, ContentValues values, String whereClause, String[] whereArgs)
        ContentValues newCv = getDBContentValuesFrom(item);
        ContentValues whereCv = getDBContentValuesFrom(where);
        //构建whereClause和whereArgs
        Condition condition = new Condition(whereCv);
        String whereClause = condition.getWhereClause();
        String[] whereArgs = condition.getWhereArgs();

        int result = database.update(tableName, newCv, whereClause, whereArgs);


        return result;
    }

    @Override
    public int delete(T where) {
//        delete(String table, String whereClause, String[] whereArgs)
        ContentValues whereCv = getDBContentValuesFrom(where);
        Condition condition = new Condition(whereCv);
        String whereClause = condition.getWhereClause();
        String[] whereArgs = condition.getWhereArgs();

        int result = database.delete(tableName, whereClause, whereArgs);
        return result;
    }

    @Override
    public List<T> query(T where) {
        return query(where, null, null, null);
    }

    @Override
    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit) {
        String limitString = null;
        if (startIndex != null && limit != null) {
            limitString = startIndex + " , " + limit;
        }

        ContentValues whereCv = getDBContentValuesFrom(where);
        Condition condition = new Condition(whereCv);
        String selection = condition.getWhereClause();
        String[] selectionArgs = condition.getWhereArgs();
        Cursor cursor = database.query(tableName, null, selection, selectionArgs, null, null, orderBy, limitString);
        List<T> result = getItemList(cursor, where);
        return result;
    }

    private List<T> getItemList(Cursor cursor, T where) {
        LinkedList<T> list = new LinkedList<>();
        //每一行,拿到列名，然后根据列名拿到Field对象获得值
        T item = null;
        while (cursor.moveToNext()) {
            try {
                item = (T) where.getClass().newInstance();
                //拿到列名，查找列名对应的Field
                Iterator<Map.Entry<String, Field>> entryIterator = tableToFieldMap.entrySet().iterator();
                while (entryIterator.hasNext()) {
                    Map.Entry<String, Field> next = entryIterator.next();
                    String columName = next.getKey();
                    //得到游标中的索引
                    int columnIndex = cursor.getColumnIndex(columName);

                    Field field = next.getValue();
                    field.setAccessible(true);
                    //通过Field设置将数据库中的值设置进去
                    Class type = field.getType();
                    if (columnIndex != -1) {
                        //根据不同类型设置Field
                        if (type == String.class) {
                            field.set(item, cursor.getString(columnIndex));
                        } else if (type == Double.class) {
                            field.set(item, cursor.getDouble(columnIndex));
                        } else if (type == Integer.class) {
                            field.set(item, cursor.getInt(columnIndex));
                        } else if (type == Long.class) {
                            field.set(item, cursor.getLong(columnIndex));
                        } else if (type == Float.class) {
                            field.set(item, cursor.getFloat(columnIndex));
                        } else if (type == byte[].class) {
                            field.set(item, cursor.getBlob(columnIndex));
                        } else {
                            /*
                            不支持的类型
                             */
                            continue;
                        }
                    }
                }
                list.add(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 查询条件
     * name=? && password =?
     * 查询参数 String[] whereArgs
     */
    class Condition {
        String whereClause;

        String[] whereArgs;

        public Condition(ContentValues whereCv) {
            StringBuilder whereClauseBuilder = new StringBuilder();
            whereClauseBuilder.append(" 1=1 ");//为了简便连接字串
            ArrayList<String> whereArgsList = new ArrayList();

            Set<String> keySet = whereCv.keySet();
            Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = whereCv.get(key).toString();
                if (value != null) {
                    whereClauseBuilder.append(" and ").append(key).append(" =?");
                    whereArgsList.add(value);
                }
            }
            this.whereClause = whereClauseBuilder.toString();
            this.whereArgs = whereArgsList.toArray(new String[whereArgsList.size()]);
        }

        public String getWhereClause() {
            return whereClause;
        }

        public String[] getWhereArgs() {
            return whereArgs;
        }
    }
}
