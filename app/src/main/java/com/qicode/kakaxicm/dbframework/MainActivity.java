package com.qicode.kakaxicm.dbframework;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.qicode.kakaxicm.dbframework.client.User;
import com.qicode.kakaxicm.dbframework.client.UserDao;
import com.qicode.kakaxicm.dbframework.db.dao.BaseDaoFactory;
import com.qicode.kakaxicm.dbframework.db.dao.IDao;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int REQUESTCODE = 101;
    IDao<User> baseDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkSelfPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUESTCODE);
            } else {
                baseDao = BaseDaoFactory.getInstance().getBaseDao(UserDao.class, User.class);
            }
        } else {
            baseDao = BaseDaoFactory.getInstance().getBaseDao(UserDao.class, User.class);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUESTCODE) {
            //询问用户权限
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                //用户同意
                baseDao = BaseDaoFactory.getInstance().getBaseDao(UserDao.class, User.class);
            } else {
                //用户不同意
            }
        }
    }

    public void save(View view) {
//        for (int i = 0; i < 10; i++) {
//            User user = new User("teacher", "123456=" + i);
//            baseDao.insert(user);
//        }
        User user = new User();
        user.setName("haha");
        baseDao.insert(user);

//        BaseDao<DownFile> baseDao1 = BaseDaoFactory.getInstance().getDataHelper(DownDao.class, DownFile.class);
//        baseDao1.insert(new DownFile("2013.1.9", "data/data/apth"));
    }

    public void update(View view) {
        User whereUser = new User();
        whereUser.setName("chenming");

        User newUser = new User();
        newUser.setName("cm");
        //修改名字为chenming的条目，用户名为cm
        baseDao.update(newUser, whereUser);
    }

    public void delete(View view) {
        User whereUser = new User();
        whereUser.setName("cm");
        baseDao.delete(whereUser);
    }

    public void query(View view) {
        User whereUser = new User();
        //查询用户名字为teacher的列表
        whereUser.setName("teacher");
        List<User> list = baseDao.query(whereUser);
        Log.e("DB", "查询结果:" + list.size());
    }
}


