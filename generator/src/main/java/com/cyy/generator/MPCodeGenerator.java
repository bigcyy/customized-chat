package com.cyy.generator;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 代码生成器,用于生成指定表的 entity, dao, xxxMapper.xml, service, controller
 */
public class MPCodeGenerator {
    /**
     * 交互式生成
     * @return 控制台的输入
     */
    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/cchat", "root", "root")
                // 全局配置
                .globalConfig(builder -> builder.author("CYY")
                        .enableSpringdoc()
                        .outputDir(System.getProperty("user.dir") + "/web/src/main/java"))
                // 包配置
                .packageConfig(builder ->
                        builder.parent("com.cyy.chat")
                                .entity("model")
                                .mapper("dao")
                                .service("service")
                                .serviceImpl("service.impl")
                                .xml("mapper")
                                .controller("controller"))
                // 策略配置
                .strategyConfig((scanner, builder) -> builder.addInclude(getTables(scanner.apply("请输入表名，多个英文逗号分隔？所有输入 all")))
                        .entityBuilder()
                        .enableLombok()
                        .addTableFills(
                                new Column("create_time", FieldFill.INSERT)
                        )
                        .build())
                // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    // 处理 all 情况
    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }
}