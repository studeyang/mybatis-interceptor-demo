package com.oujiong.mapper;

import com.oujiong.entity.TabUser;
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
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void insert() {
        TabUser user = new TabUser();
        user.setName("张三");
        user.setSex("男");
        user.setAge(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setStatus(0);

        userMapper.insert(user);
    }

    @Test
    public void selectByPrimaryKey() {
        System.out.println(userMapper.selectByPrimaryKey(199L));
    }

}