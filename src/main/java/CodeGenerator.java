           import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        // 1. 配置数据库连接 (请改成你 DataGrip 里用的账号密码)
        String url = "jdbc:mysql://localhost:3306/cloud_hospital?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "123456"; // TODO: 替换为真实密码

        FastAutoGenerator.create(url, username, password)
                // 2. 全局配置
                .globalConfig(builder -> {
                    builder.author("Allen") // 设置作者
                            .enableSpringdoc() // 开启 Swagger，方便后期生成接口文档
                            // 指定输出目录为当前项目的 src/main/java
                            .outputDir(System.getProperty("user.dir") + "/src/main/java");
                })
                // 3. 包配置
                .packageConfig(builder -> {
                    builder.parent("com.cloud.hospital") // 设置父包名
                            .moduleName("system") // 设置模块名
                            // 配置 Mapper XML 文件的输出路径 (放到 resources/mapper 下)
                            .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir") + "/src/main/resources/mapper"));
                })
                // 4. 策略配置 (核心！)
                .strategyConfig(builder -> {
                    builder.addInclude(
                                    "patient", 
                                    "department", 
                                    "doctor", 
                                    "schedule", 
                                    "registration_order"
                            ) // 设置需要生成的表名
                            // Entity 实体类策略配置
                            .entityBuilder()
                            .enableLombok() // 开启 Lombok
                            .logicDeleteColumnName("is_deleted") // 说明逻辑删除字段是哪个
                            .enableTableFieldAnnotation() // 开启字段注解
                            // Controller 策略配置
                            .controllerBuilder()
                            .enableRestStyle() // 开启 @RestController
                            // Mapper 策略配置
                            .mapperBuilder()
                            .enableMapperAnnotation(); // 开启 @Mapper 注解
                })
                // 使用 Freemarker 模板引擎
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
                
        System.out.println("🎉 代码生成完毕！请刷新 IDEA 目录。");
    }
}