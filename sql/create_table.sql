# 数据库初始化

-- 创建库
create database if not exists my_ai_code_mother;

-- 使用库
use my_ai_code_mother;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(256)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     timestamp    default CURRENT_TIMESTAMP not null comment '更新时间',
    createTime   timestamp    default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment ='用户表' collate = utf8mb4_unicode_ci;