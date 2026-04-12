package com.ojplatform.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置类
 * 包含分页插件和自动填充处理器
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 分页插件（题目列表每页 20 条）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 自动填充时间字段处理器
     * 对应 Entity 中 @TableField(fill = FieldFill.INSERT) 等注解
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "submittedAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "startedAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "lastActiveAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "jumpedAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "registeredAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "joinedAt", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
