package com.oujiong.entity;

import com.oujiong.plugin.autoid.AutoId;
import com.oujiong.plugin.encrypt.EncryptField;
import lombok.Data;

import java.util.Date;

/**
 * user表
 */
@Data
public class UserEntity {
    /**
     * id(添加自定义主键ID)
     */
    @AutoId
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 身份证
     */
    @EncryptField
    private String idCard;

    /**
     * 性别
     */
    private String sex;

    /**
     * 年龄
     */
    private Integer age;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 是否删除 1删除 0未删除
     */
    private Integer status;
}