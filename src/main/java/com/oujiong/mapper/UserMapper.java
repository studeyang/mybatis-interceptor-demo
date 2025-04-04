package com.oujiong.mapper;

import com.oujiong.entity.UserEntity;
import com.oujiong.plugin.encrypt.QueryMobile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xub
 * @Description: 用户mapper
 * @date 2019/8/24 下午4:11
 */
public interface UserMapper {

    /**
     * 插入一条记录
     *
     * @param record 实体对象
     * @return 更新条目数
     */
    int insert(UserEntity record);

    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 实体对象
     */
    UserEntity selectByPrimaryKey(Long id);

    @QueryMobile
    List<UserEntity> selectByMobile(@Param("mobiles") List<String> mobiles);

}