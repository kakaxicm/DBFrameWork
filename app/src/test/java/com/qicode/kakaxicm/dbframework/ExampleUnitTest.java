package com.qicode.kakaxicm.dbframework;

import com.qicode.kakaxicm.dbframework.client.User;
import com.qicode.kakaxicm.dbframework.client.UserDao;
import com.qicode.kakaxicm.dbframework.db.dao.BaseDaoFactory;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testDao(){
        BaseDaoFactory.getInstance().getBaseDao(UserDao.class, User.class);
    }
}