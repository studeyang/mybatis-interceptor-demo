package com.oujiong.mapper;

import com.oujiong.entity.UserEntity;
import com.oujiong.plugin.encrypt.EncryptUtils;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @author <a href="mailto:studeyang@gmail.com">studeyang</a>
 * @since 1.0 2024/6/27/027
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserMapperTest extends TestCase {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void encrypt() {
        String text = "你好";
        String encrypt = EncryptUtils.encrypt(text);
        System.out.println(encrypt);
        assertEquals(text, EncryptUtils.decrypt(encrypt));
    }

    @Test
    public void insert() {
        UserEntity user = new UserEntity();
        user.setName("张三");
        user.setIdCard("442222111233322210");
        user.setSex("男");
        user.setAge(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setStatus(0);

        userMapper.insert(user);
    }

    @Test
    public void selectByPrimaryKey() {
        UserEntity user = userMapper.selectByPrimaryKey(682230480968224768L);
        System.out.println(user);
    }

}