CREATE TABLE `user`
(
    `id`          bigint(20)                        NOT NULL AUTO_INCREMENT COMMENT 'id',
    `name`        varchar(64) CHARACTER SET utf8mb4 NOT NULL DEFAULT '' COMMENT '姓名',
    `name_enc`    varchar(100)                               DEFAULT NULL COMMENT '姓名（密文）',
    `sex`         varchar(16) CHARACTER SET utf8mb4          DEFAULT NULL COMMENT '性别',
    `age`         int(255)                                   DEFAULT NULL COMMENT '年龄',
    `create_time` datetime                          NOT NULL COMMENT '创建时间',
    `update_time` datetime                          NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status`      int(10)                           NOT NULL DEFAULT '0' COMMENT '是否删除 1删除 0未删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='用户表';

