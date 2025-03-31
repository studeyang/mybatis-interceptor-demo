/*
 * Copyright (c) 2018 Wantu, All rights reserved.
 */
package com.oujiong.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.oujiong.plugin.autoid.AutoIdInterceptor;
import com.oujiong.plugin.encrypt.ReadEncryptInterceptor;
import com.oujiong.plugin.encrypt.WriteEncryptInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Collections;

/**
 * @author xub
 * @Description: 连接数据库信息 包括添加插件信息
 * @date 2019/8/19 下午12:31
 */
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
@ComponentScan(basePackageClasses = DalModule.class)
@MapperScan(basePackages = "com.oujiong.mapper")
public class DalModule {

    /**
     * 连接数据库数据源信息
     */
    @Bean
    public DataSource dataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl("jdbc:mysql://10.204.209.137:3306/send2?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&useSSL=false");
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUsername("send2");
        druidDataSource.setPassword("send2123.com");
        druidDataSource.setMaxActive(20);
        druidDataSource.setInitialSize(1);
        druidDataSource.setMaxWait(60000);
        druidDataSource.setMinIdle(1);
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
        druidDataSource.setMinEvictableIdleTimeMillis(300000);
        druidDataSource.setValidationQuery("SELECT 'x'");
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTestOnBorrow(false);
        druidDataSource.setTestOnReturn(false);
        druidDataSource.setPoolPreparedStatements(true);
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        druidDataSource.setConnectionInitSqls(Collections.singletonList("SET NAMES utf8mb4"));
        return druidDataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public WriteEncryptInterceptor writeEncryptInterceptor() {
        return new WriteEncryptInterceptor();
    }

    @Bean
    public ReadEncryptInterceptor readEncryptInterceptor() {
        return new ReadEncryptInterceptor();
    }

    /**
     * SqlSessionFactory 实体
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(WriteEncryptInterceptor writeEncryptInterceptor,
                                               ReadEncryptInterceptor readEncryptInterceptor) throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource());
        sessionFactoryBean.setFailFast(true);
        sessionFactoryBean.setMapperLocations(resolver.getResources("classpath:/mapper/*Mapper.xml"));
        /**
         * 添加插件信息(因为插件采用责任链模式所有可以有多个，所以采用数组
         */
//        Interceptor[] interceptors = new Interceptor[1];
//        interceptors[0] = autoIdInterceptor();
//        sessionFactory.setPlugins(interceptors);

        SqlSessionFactory sessionFactory = sessionFactoryBean.getObject();
        sessionFactory.getConfiguration().addInterceptor(new AutoIdInterceptor());
        sessionFactory.getConfiguration().addInterceptor(writeEncryptInterceptor);
        sessionFactory.getConfiguration().addInterceptor(readEncryptInterceptor);
//        sessionFactory.getConfiguration().addInterceptor(new ReadMobileInterceptor());

        return sessionFactory;
    }

}
