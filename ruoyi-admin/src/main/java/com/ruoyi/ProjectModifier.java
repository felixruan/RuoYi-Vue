package com.ruoyi;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.io.File.separator;

public class ProjectModifier {

    private static Logger log = LoggerFactory.getLogger(ProjectModifier.class);

    private static final String GROUP_ID = "com.ruoyi";
    private static final String ARTIFACT_ID = "ruoyi";
    private static final String PACKAGE_NAME = "com.ruoyi";
    private static final String TITLE = "若依管理系统";
    private static final String VERSION = "3.8.5";
    private static final String projectBaseDir = "D:\\environment\\project\\RuoYi-Vue";

    private static final String groupIdNew = "com.igg.cloudtest";
    private static final String artifactIdNew = "server";
    private static final String packageNameNew = "com.igg";
    private static final String titleNew = "IGG云测平台";
    private static final String versionNew = "3.0.0";
    private static final String projectBaseDirNew = projectBaseDir + "-New"; // 一键改名后，“新”项目所在的目录

    /**
     * 白名单文件，不进行重写，避免出问题
     */
    private static final Set<String> WHITE_FILE_TYPES = asSet("gif", "jpg", "svg", "png", // 图片
            "eot", "woff2", "ttf", "woff"); // 字体

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        log.info("[main][原项目路劲地址 ({})]", projectBaseDir);
        log.info("[main][新项目路径地址 ({})]", projectBaseDirNew);
        // 获得需要复制的文件
        log.info("[main][开始获得需要重写的文件，预计需要 10-20 秒]");
        Collection<File> files = listFiles();
        log.info("[main][需要重写的文件数量：{}，预计需要 15-30 秒]", files.size());
        // 写入文件
        for (File file : files) {
            String filePath = file.getPath();
            String newPath = buildNewFilePath(file);
            // 如果是白名单的文件类型，不进行重写，直接拷贝
            if (WHITE_FILE_TYPES.contains(filePath.lastIndexOf("."))) {
                FileUtils.copyFile(new File(filePath), new File(newPath), StandardCopyOption.REPLACE_EXISTING);
                return;
            }
            // 如果非白名单的文件类型，重写内容，在生成文件
            String content;
            if (filePath.endsWith("sql")) {
                content = replaceSqlContent(filePath);
            } else if (filePath.endsWith("yml")) {
                // 多拷贝一个application-test.yml
                if (filePath.endsWith("application-druid.yml")) {
                    String devPath = newPath.replace("application-druid.yml", "application-dev.yml");
                    String devContent = replaceYmlContent(filePath, devPath);
                    FileUtils.write(new File(devPath), devContent, Charset.forName("UTF-8"));
                    String testPath = newPath.replace("application-druid.yml", "application-test.yml");
                    String testContent = replaceYmlContent(filePath, testPath);
                    FileUtils.write(new File(testPath), testContent, Charset.forName("UTF-8"));
                    newPath = newPath.replace("application-druid.yml", "application-prod.yml");
                }
                content = replaceYmlContent(filePath, newPath);
            } else if (filePath.endsWith("SwaggerConfig.java")) {
                String mvcPath = newPath.replace("SwaggerConfig.java", "WebMvcConfiguration.java");
                String mvcContent = "package com.igg.web.core.config;\r\n\r\n" +
                        "import org.springframework.context.annotation.Configuration;\r\n" +
                        "import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;\r\n\r\n" +
                        "@Configuration\r\n" +
                        "public class WebMvcConfiguration extends DelegatingWebMvcConfiguration {\r\n" +
                        "}\r\n";
                FileUtils.write(new File(mvcPath), mvcContent, Charset.forName("UTF-8"));
                content = replaceFileContent(filePath);
            } else {
                content = replaceFileContent(filePath);
            }
            FileUtils.write(new File(newPath), content, Charset.forName("UTF-8"));
        }
        // 拷贝代码文件
        FileUtils.copyDirectory(new File(projectBaseDir + separator + "code" + separator + "common"),
                new File(projectBaseDirNew + separator + artifactIdNew + "-common"));
        FileUtils.copyDirectory(new File(projectBaseDir + separator + "code" + separator + "framework"),
                new File(projectBaseDirNew + separator + artifactIdNew + "-framework"));
        FileUtils.copyDirectory(new File(projectBaseDir + separator + "code" + separator + "generator"),
                new File(projectBaseDirNew + separator + artifactIdNew + "-generator"));
        // System.out.println(files.size());
        log.info("[main][重写完成]共耗时：{} 毫秒", (System.currentTimeMillis() - start));
    }

    private static Collection<File> listFiles() {
        Collection<File> files = FileUtils.listFiles(new File(projectBaseDir), null, true);
        // 移除 IDEA、Git 自身的文件、Node 编译出来的文件
        files = files.stream()
                .filter(file -> !file.getPath().contains(separator + "target" + separator)
                        && !file.getPath().contains(separator + "node_modules" + separator)
                        && !file.getPath().contains(separator + ".idea" + separator)
                        && !file.getPath().contains(separator + ".git" + separator)
                        && !file.getPath().contains(separator + ".github" + separator)
                        && !file.getPath().contains(separator + ".gitee" + separator)
                        && !file.getPath().contains(separator + "dist" + separator)
                        && !file.getPath().contains(".iml")
                        && !file.getPath().contains(".html.gz"))
                .collect(Collectors.toList());
        // 去除不需要的文件夹
        files = files.stream()
                .filter(file -> !file.getPath().endsWith(".md")
                        && !file.getPath().endsWith("ProjectModule.java")
                        && !file.getPath().endsWith("ProjectModifier.java")
                        && !file.getPath().endsWith("MyBatisConfig.java")  // 去除mybatis配置
                        && !file.getPath().endsWith("BeanUtils.java")  // 去除BeanUtils工具类
                        && !file.getPath().contains(separator + "vue" + separator + "v3" + separator)
                        && !file.getPath().startsWith(projectBaseDir + separator + "code")  // code不引入
                        && !file.getPath().startsWith(projectBaseDir + separator + "ruoyi-ui" + separator)  // UI暂时不导入
                        && !file.getPath().startsWith(projectBaseDir + separator + "ruoyi-generator" + separator +
                        "src" + separator + "main" + separator + "resources" + separator + "vm" + separator)  // vm不导入
                        && !file.getPath().startsWith(projectBaseDir + separator + "ruoyi-quartz" + separator +
                        "src" + separator + "main" + separator + "java" + separator + "com" + separator +
                        "ruoyi" + separator + "quartz" + separator + "task" + separator)  // task的测试代码不导入
                        && !file.getPath().startsWith(projectBaseDir + separator + "ruoyi-quartz" + separator +
                        "src" + separator + "main" + separator + "java" + separator + "com" + separator +
                        "ruoyi" + separator + "quartz" + separator + "config" + separator)  // 单机模式删除
                        && !file.getPath().startsWith(projectBaseDir + separator + "bin")
                        && !file.getPath().startsWith(projectBaseDir + separator + "doc")
                        && !file.getPath().equals(projectBaseDir + separator + "ry.bat")
                        && !file.getPath().equals(projectBaseDir + separator + "ry.sh")
                        && !file.getPath().equals(projectBaseDir + separator + "LICENSE"))
                .collect(Collectors.toList());
        return files;
    }

    private static String replaceSqlContent(String filePath) throws Exception {
        List<String> contents = FileUtils.readLines(new File(filePath), Charset.forName("UTF-8"));
        StringBuffer stringBuffer = new StringBuffer();
        for (String item : contents) {
            if (item.startsWith("insert into sys_dept values")) {
                if (item.startsWith("insert into sys_dept values(100")) {
                    item = item.replace("'若依科技'", "'IGG集团'")
                            .replace("'若依'", "'系统管理员'")
                            .replace("'15888888888'", "'18850498845'")
                            .replace("'ry@qq.com'", "'jiehui.ruan@igg.com'");
                } else {
                    continue;
                }
            }
            if (item.startsWith("insert into sys_user values")) {
                if (item.startsWith("insert into sys_user values(1")) {
                    item = item.replace("'若依'", "'阮杰辉'")
                            .replace("'ry@163.com'", "'jiehui.ruan@igg.com'")
                            .replace("'15888888888'", "'18850498845'")
                            .replace("103,", "100,");
                } else {
                    continue;
                }
            }
            if (item.startsWith("insert into sys_menu values('1'")) {
                item = item.replace("'1', '系统管理', '0', '1'", "'1', '系统管理', '0', '100'");
            }
            if (item.startsWith("insert into sys_menu values('2'")) {
                item = item.replace("'2', '系统监控', '0', '2'", "'2', '系统监控', '0', '200'");
            }
            if (item.startsWith("insert into sys_menu values('3'")) {
                item = item.replace("'3', '系统工具', '0', '3'", "'3', '系统工具', '0', '300'");
            }
            if (item.startsWith("insert into sys_config values(4")) {
                item = item.replace("'true'", "'false'");  // 验证码开关关闭
            }
            if (item.startsWith("insert into sys_menu values('4'")) continue;
            if (item.startsWith("insert into sys_user_role values ('2'")) continue;
            if (item.startsWith("insert into sys_role_dept values")) continue;
            if (item.startsWith("insert into sys_user_post values ('2'")) continue;
            if (item.startsWith("insert into sys_notice values")) continue;
            stringBuffer.append(item + "\r\n");
        }
        return stringBuffer.toString();
    }

    private static String replaceYmlContent(String filePath, String newPath) throws Exception {
        String content = FileUtils.readFileToString(new File(filePath), Charset.forName("UTF-8"));
        content = content
                .replaceAll("login-username: ruoyi", "login-username: admin")
                .replaceAll("login-password: 123456", "login-password: igg123456")
                // 替换空格
                .replaceAll("params: count=countSql ", "params: count=countSql")
                .replaceAll(" \\s+\r\n", "\r\n")  // 去除末尾的空格
                .replaceAll(" \r\n", "\r\n")  // 去除末尾的空格
                .replaceAll(PACKAGE_NAME, packageNameNew);
        if (!content.endsWith("\r\n")) content += "\r\n";
        // 替换yml代码
        if (newPath.endsWith("application-dev.yml")) {
            content = content + "\r\nspringfox:\r\n  documentation:\r\n    enabled: true\r\n";
            return content
                    .replaceAll("    ", "  ")  // 去除末尾的空格
                    //.replaceAll("enabled: true", "enabled: false")
                    .replaceAll("url: jdbc:mysql://localhost:3306/ry-vue", "url: jdbc:mysql://10.0.16.219:3306/igg_cloud_test_v3")
                    // .replaceAll("username: root", "username: root")
                    .replaceAll("password: password", "password: igg123456")
                    .replaceAll("host: localhost", "host: 10.0.16.88")
                    .replaceAll("database: 0", "database: 3");
        }
        if (newPath.endsWith("application-prod.yml")) {
            content = content + "\r\nspringfox:\r\n  documentation:\r\n    enabled: false\r\n";
            return content
                    .replaceAll("    ", "  ")  // 去除末尾的空格
                    .replaceAll("url: jdbc:mysql://localhost:3306/ry-vue", "url: jdbc:mysql://10.0.16.116:3306/igg_cloud_test_v3")
                    // .replaceAll("username: root", "username: root")
                    .replaceAll("password: password", "password: igg123456")
                    .replaceAll("host: localhost", "host: 10.0.3.25")
                    .replaceAll("database: 0", "database: 3");
        }
        if (newPath.endsWith("application-test.yml")) {
            content = content + "\r\nspringfox:\r\n  documentation:\r\n    enabled: false\r\n";
            return content
                    .replaceAll("    ", "  ")  // 去除末尾的空格
                    //.replaceAll("enabled: true", "enabled: false")
                    .replaceAll("url: jdbc:mysql://localhost:3306/ry-vue", "url: jdbc:mysql://10.0.16.87:3306/igg_cloud_test_v3")
                    // .replaceAll("username: root", "username: root")
                    .replaceAll("password: password", "password: igg123456")
                    .replaceAll("host: localhost", "host: 10.0.16.87")
                    .replaceAll("database: 0", "database: 3");
        }
        return content
                // 项目配置处理
                .replaceAll("ruoyi:", "project:")
                .replaceAll("name: RuoYi", "name: " + artifactIdNew)
                .replaceAll("version: " + VERSION, "version: \\$\\{server.version}")
                .replaceAll("active: druid", "active: @profiles.active@")
                // spring配置缩进处理
                .replaceAll("     multipart:", "    multipart:")
                .replaceAll("       # 单个文件大小", "      # 单个文件大小")
                .replaceAll("       max-file-size:  10MB", "      max-file-size: 5GB")
                .replaceAll("       # 设置总上传的文件大小", "      # 设置总上传的文件大小")
                .replaceAll("       max-request-size:  20MB", "      max-request-size: 5GB")
                // tokeng配置缩进处理
                .replaceAll("    # 令牌自定义标识", "  # 令牌自定义标识")
                .replaceAll("    header: Authorization", "  header: Authorization")
                .replaceAll("    # 令牌密钥", "  # 令牌密钥")
                .replaceAll("    secret: abcdefghijklmnopqrstuvwxyz", "  secret: abcdefghijklmnopqrstuvwxyz")
                .replaceAll("    # 令牌有效期（默认30分钟）", "  # 令牌有效期（默认30分钟）")
                .replaceAll("    expireTime: 30", "  expireTime: 30")
                // mybatis配置改为mybatis-plus配置
                .replaceAll("mybatis:", "mybatis-plus:")
                .replaceAll("    # 搜索指定包别名", "  # 搜索指定包别名")
                .replaceAll("    typeAliasesPackage:", "  typeAliasesPackage:")
                .replaceAll("    # 配置mapper的扫描", "  # 配置mapper的扫描")
                .replaceAll("    mapperLocations:", "  mapperLocations:")
                .replaceAll("    # 加载全局的配置文件", "  # 加载全局的配置文件")
                .replaceAll("    configLocation: classpath:mybatis/mybatis-config.xml",
                        "  global-config:\r\n    dbConfig:\r\n      # 主键类型\r\n      idType: AUTO\r\n" +
                                "  configuration:\r\n    # 自动驼峰命名规则映射\r\n    mapUnderscoreToCamelCase: true")
                // 缓存配置处理
                .replaceAll("  # redis 配置", "  # cache 配置\r\n  cache:\r\n    type: REDIS\r\n" +
                        "    redis:\r\n      time-to-live: 1h\r\n  # redis 配置")
                // redis配置处理
                .replaceAll("host: localhost", "host: 10.0.16.87")
                .replaceAll("database: 0", "database: 3")
                // 其他配置
                .replaceFirst("author: ruoyi", "author: ruanjiehui")
                .replaceAll("tablePrefix: sys_", "tablePrefix: cloud_test_");
    }

    private static String replaceContent(String content, String start, String end) {
        Pattern pattern = Pattern.compile("(?<=" + start + ").*(?=" + end + ")");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String str = matcher.group();
            return content.replace(str, str.trim());
        }
        return content;
    }

    private static String replaceFileContent(String filePath) throws Exception {
        String content;
        // 注释格式化及换行替换
        if (filePath.endsWith(".java")) {
            List<String> contents = FileUtils.readLines(new File(filePath), Charset.forName("UTF-8"));
            StringBuffer stringBuffer = new StringBuffer();
            for (String item : contents) {
                String text = item.trim();
                if (text.startsWith("/**") && text.endsWith("*/")) {
                    int index = item.indexOf("/**");
                    String space = "";
                    for (int i = 0; i < index; i++) {
                        space += " ";
                    }
                    String comment = text.substring(3, text.length() - 2).trim();
                    item = space + "/**\r\n" + space + " * " + comment + "\r\n" + space + " */";
                }
                stringBuffer.append(item + "\r\n");
            }
            content = stringBuffer.toString();
        } else {
            content = FileUtils.readFileToString(new File(filePath), Charset.forName("UTF-8"));
        }
        // 统一处理空格
        content = content
                .replaceAll("\t", "    ")  // 去除末尾的空格
                .replaceAll(" \\s+\r\n", "\r\n")  // 去除末尾的空格
                .replaceAll(" \r\n", "\r\n")  // 去除末尾的空格
        ;
        // 去除模块引入的pom
        if (filePath.equals(projectBaseDir + separator + "pom.xml")) {
            // System.out.println(file.getPath());
            content = content
                    // 去除ruoyi相关名称
                    .replaceFirst("    <name>ruoyi</name>\r\n", "")
                    .replaceFirst("    <url>http://www.ruoyi.vip</url>\r\n", "")
                    // 替换ruoyi版本
                    .replaceAll("    <artifactId>ruoyi</artifactId>\r\n    <version>.*?</version>\r\n",
                            "    <artifactId>ruoyi</artifactId>\r\n    <version>" + versionNew + "</version>\r\n")
                    .replaceFirst("<ruoyi.version>.*?</ruoyi.version>",
                            "<ruoyi.version>" + versionNew + "</ruoyi.version>")
                    // 增加版本属性
                    .replaceFirst("</properties>",
                            "    <lombok.version>1.18.24</lombok.version>\r\n" +
                                    "        <hutool.version>5.8.10</hutool.version>\r\n" +
                                    "        <mybatis-plus.version>3.5.2</mybatis-plus.version>\r\n" +
                                    "        <spring-boot.version>2.7.6</spring-boot.version>\r\n    </properties>")
                    // 新增依赖管理
                    .replaceFirst("</dependencies>\r\n    </dependencyManagement>",
                            "    <dependency>\r\n" +
                                    "                <groupId>cn.hutool</groupId>\r\n" +
                                    "                <artifactId>hutool-all</artifactId>\r\n" +
                                    "                <version>\\$\\{hutool.version\\}</version>\r\n" +
                                    "            </dependency>\r\n\r\n" +
                                    "            <dependency>\r\n" +
                                    "                <groupId>com.baomidou</groupId>\r\n" +
                                    "                <artifactId>mybatis-plus-boot-starter</artifactId>\r\n" +
                                    "                <version>\\$\\{mybatis-plus.version\\}</version>\r\n" +
                                    "            </dependency>\r\n\r\n" +
                                    "            <dependency>\r\n" +
                                    "                <groupId>com.baomidou</groupId>\r\n" +
                                    "                <artifactId>mybatis-plus-extension</artifactId>\r\n" +
                                    "                <version>\\$\\{mybatis-plus.version\\}</version>\r\n" +
                                    "            </dependency>\r\n\r\n" +
                                    "        </dependencies>\r\n" +
                                    "    </dependencyManagement>")
                    // 新增lombok依赖及profile
                    .replaceFirst("\r\n    <dependencies>\r\n\r\n    </dependencies>",
                            "    <dependencies>\r\n" +
                                    "        <dependency>\r\n" +
                                    "            <groupId>org.projectlombok</groupId>\r\n" +
                                    "            <artifactId>lombok</artifactId>\r\n" +
                                    "            <version>\\$\\{lombok.version\\}</version>\r\n" +
                                    "            <scope>provided</scope>\r\n" +
                                    "        </dependency>\r\n" +
                                    "    </dependencies>\r\n\r\n" +
                                    "    <profiles>\r\n" +
                                    "        <profile>\r\n" +
                                    "            <id>test</id>\r\n" +
                                    "            <properties>\r\n" +
                                    "                <profiles.active>test</profiles.active>\r\n" +
                                    "            </properties>\r\n" +
                                    "        </profile>\r\n" +
                                    "        <profile>\r\n" +
                                    "            <id>dev</id>\r\n" +
                                    "            <properties>\r\n" +
                                    "                <profiles.active>dev</profiles.active>\r\n" +
                                    "            </properties>\r\n" +
                                    "            <activation>\r\n" +
                                    "                <activeByDefault>true</activeByDefault>\r\n" +
                                    "            </activation>\r\n" +
                                    "        </profile>\r\n" +
                                    "        <profile>\r\n" +
                                    "            <id>prod</id>\r\n" +
                                    "            <properties>\r\n" +
                                    "                <profiles.active>prod</profiles.active>\r\n" +
                                    "            </properties>\r\n" +
                                    "        </profile>\r\n" +
                                    "    </profiles>")
                    // 替换spring-boot版本
                    .replaceFirst("    <artifactId>spring-boot-dependencies</artifactId>\r\n" +
                                    "                <version>.*?</version>",
                            "    <artifactId>spring-boot-dependencies</artifactId>\r\n" +
                                    "                <version>\\$\\{spring-boot.version\\}</version>")
                    // 替换插件版本
                    .replaceFirst("<artifactId>maven-compiler-plugin</artifactId>\r\n" +
                                    "                <version>.*?</version>",
                            "<artifactId>maven-compiler-plugin</artifactId>\r\n" +
                                    "                <version>3.9.0</version>")
                    // 新增资源配置
                    .replaceFirst("</plugins>",
                            "    <plugin>\n" +
                                    "                <groupId>org.apache.maven.plugins</groupId>\n" +
                                    "                <artifactId>maven-surefire-plugin</artifactId>\n" +
                                    "                <version>2.22.2</version>\n" +
                                    "                <configuration>\n" +
                                    "                    <argLine>-Dfile.encoding=UTF-8</argLine>\n" +
                                    "                    <groups>\\$\\{profiles.active\\}</groups>\n" +
                                    "                    <excludedGroups>exclude</excludedGroups>\n" +
                                    "                </configuration>\n" +
                                    "            </plugin>\n" +
                                    "        </plugins>\r\n" +
                                    "        <resources>\r\n" +
                                    "            <resource>\r\n" +
                                    "                <directory>src/main/resources</directory>\r\n" +
                                    "                <filtering>false</filtering>\r\n" +
                                    "            </resource>\r\n" +
                                    "            <resource>\r\n" +
                                    "                <directory>src/main/resources</directory>\r\n" +
                                    "                <includes>\r\n" +
                                    "                    <include>application*</include>\r\n" +
                                    "                    <include>bootstrap*</include>\r\n" +
                                    "                    <include>banner*</include>\r\n" +
                                    "                </includes>\r\n" +
                                    "                <filtering>true</filtering>\r\n" +
                                    "            </resource>\r\n" +
                                    "        </resources>"
                    )
            ;
        } else if (filePath.equals(projectBaseDir + separator + "ruoyi-admin" + separator + "pom.xml")) {
            content = content
                    // 替换插件版本
                    .replaceFirst("<artifactId>spring-boot-maven-plugin</artifactId>\r\n" +
                                    "                <version>.*?</version>",
                            "<artifactId>spring-boot-maven-plugin</artifactId>\r\n" +
                                    "                <version>\\$\\{spring-boot.version\\}</version>"
                    )
                    .replaceFirst("<artifactId>maven-war-plugin</artifactId>\r\n" +
                                    "                <version>.*?</version>",
                            "<artifactId>maven-war-plugin</artifactId>\r\n" +
                                    "                <version>3.2.2</version>"
                    )
            ;
        } else if (filePath.equals(projectBaseDir + separator + "ruoyi-common" + separator + "pom.xml")) {
            content = content
                    // 新增依赖
                    .replaceFirst("</dependencies>",
                            "    <dependency>\r\n" +
                                    "            <groupId>cn.hutool</groupId>\r\n" +
                                    "            <artifactId>hutool-all</artifactId>\r\n" +
                                    "        </dependency>\r\n\r\n" +
                                    "        <dependency>\r\n" +
                                    "            <groupId>com.baomidou</groupId>\r\n" +
                                    "            <artifactId>mybatis-plus-boot-starter</artifactId>\r\n" +
                                    "        </dependency>\r\n\r\n" +
                                    "        <dependency>\r\n" +
                                    "            <groupId>com.baomidou</groupId>\r\n" +
                                    "            <artifactId>mybatis-plus-extension</artifactId>\r\n" +
                                    "        </dependency>\r\n\r\n" +
                                    "    </dependencies>");
        } else if (filePath.equals(projectBaseDir + separator + "ruoyi-framework" + separator + "pom.xml")) {
            content = content
                    // 新增依赖
                    .replaceFirst("</dependencies>",
                            "    <!-- Websocket -->\r\n" +
                                    "        <dependency>\n" +
                                    "            <groupId>org.springframework.boot</groupId>\r\n" +
                                    "            <artifactId>spring-boot-starter-websocket</artifactId>\r\n" +
                                    "        </dependency>\r\n\r\n" +
                                    "        <!-- Cache -->\r\n" +
                                    "        <dependency>\r\n" +
                                    "            <groupId>org.springframework.boot</groupId>\r\n" +
                                    "            <artifactId>spring-boot-starter-cache</artifactId>\r\n" +
                                    "        </dependency>\r\n\r\n" +
                                    "    </dependencies>");
        }
        // 替换pom文件
        if (filePath.endsWith("pom.xml")) {
            content = content
                    // 错误缩进处理
                    .replaceFirst("\r\n         \\<", "\r\n        \\<")
                    .replaceFirst("\r\n           \\<", "\r\n           \\<")
                    // 替换ruoyi版本
                    .replaceAll("<groupId>com.ruoyi</groupId>\r\n        <version>.*?</version>\r\n",
                            "<groupId>com.ruoyi</groupId>\r\n        <version>" + versionNew + "</version>\r\n")
                    .replaceAll(GROUP_ID, groupIdNew);
            content = content.substring(0, content.lastIndexOf(">") + 1) + "\r\n";
        }
        // 处理banner文本
        else if (filePath.endsWith("banner.txt")) {
            content = content.replaceFirst("永无BUG               //", "永无BUG                   //");
        }
        // 替换java代码
        else if (filePath.endsWith(".java") || filePath.endsWith(".java.vm")) {
            // 去掉接口的public声明
            if (content.contains("public interface") || content.contains("public @interface")) {
                content = content.replaceAll("    public ", "    ");
            }
            // 去掉启动类的ruoyi打印
            if (filePath.endsWith("RuoYiApplication.java")) {
                content = content.substring(0, content.indexOf("    System.out.println")) + "}\r\n}\r\n";
            }
            // 处理BaseEntity的数据库字段
            else if (filePath.endsWith("BaseEntity.java")) {
                content = content
                        .replaceFirst("import com.fasterxml.jackson.annotation.JsonFormat;",
                                "import com.baomidou.mybatisplus.annotation.FieldFill;\r\n" +
                                        "import com.baomidou.mybatisplus.annotation.TableField;\r\n" +
                                        "import com.fasterxml.jackson.annotation.JsonFormat;")
                        .replaceFirst("private String searchValue;", "@TableField(exist = false)\r\n    private String searchValue;")
                        .replaceFirst("private Date createBy;", "@TableField(fill = FieldFill.INSERT)\r\n    private Date createBy;")
                        .replaceFirst("private String createTime;", "@TableField(fill = FieldFill.INSERT)\r\n    private String createTime;")
                        .replaceFirst("private String updateBy;", "@TableField(fill = FieldFill.INSERT_UPDATE)\r\n    private String updateBy;")
                        .replaceFirst("private Date updateTime;", "@TableField(fill = FieldFill.INSERT_UPDATE)\r\n    private Date updateTime;")
                        .replaceFirst("private Map<String, Object> params;", "@TableField(exist = false)\r\n    private Map<String, Object> params;");
            }
            // 处理BaseEntity的数据库字段
            else if (filePath.endsWith("VelocityUtils.java")) {
                content = content
                        .replace("templates.add(\"vm/java/domain.java.vm\");",
                                "templates.add(\"vm/java/domain.java.vm\");\r\n" +
                                        "        templates.add(\"vm/java/vo.java.vm\");\r\n" +
                                        "        templates.add(\"vm/java/ro.java.vm\");")
                        .replaceFirst("templates.add\\(\"vm/vue/index.vue.vm\"\\);",
                                "// templates.add(\"vm/vue/index.vue.vm\");\r\n" +
                                        "            templates.add(\"vm/vue/antdv/index.vue.vm\");\r\n" +
                                        "            templates.add(\"vm/vue/antdv/modules/CreateForm.vue.vm\");")
                        .replaceFirst("templates.add\\(\"vm/vue/index-tree.vue.vm\"\\);",
                                "// templates.add(\"vm/vue/index-tree.vue.vm\");\r\n" +
                                        "            templates.add(\"vm/vue/antdv/index.vue.vm\");\r\n" +
                                        "            templates.add(\"vm/vue/antdv/modules/CreateForm.vue.vm\");")
                        .replaceFirst("templates.add\\(\"vm/vue/index.vue.vm\"\\);\r\n" +
                                        "            templates.add\\(\"vm/java/sub-domain.java.vm\"\\);",
                                "// templates.add(\"vm/vue/index.vue.vm\");\r\n" +
                                        "            templates.add(\"vm/vue/antdv/index.vue.vm\");\r\n" +
                                        "            templates.add(\"vm/vue/antdv/modules/CreateForm.vue.vm\");\r\n" +
                                        "            templates.add(\"vm/vue/antdv/modules/SubTable.vue.vm\");\r\n" +
                                        "            templates.add(\"vm/vue/antdv/modules/CreateSubForm.vue.vm\");")
                        .replace("fileName = StringUtils.format(\"{}/domain/{}.java\", javaPath, className);\r\n        }\r\n        ",
                                "fileName = StringUtils.format(\"{}/domain/{}.java\", javaPath, className);\r\n" +
                                        "        } else if (template.contains(\"vo.java.vm\")) {\r\n" +
                                        "            fileName = StringUtils.format(\"{}/domain/vo/{}VO.java\", javaPath, className);\r\n" +
                                        "        } else if (template.contains(\"ro.java.vm\")) {\r\n" +
                                        "            fileName = StringUtils.format(\"{}/domain/ro/{}RO.java\", javaPath, className);\r\n" +
                                        "        } else if (template.contains(\"CreateForm.vue.vm\")) {\r\n" +
                                        "            fileName = StringUtils.format(\"{}/views/{}/{}/modules/CreateForm.vue\", vuePath, moduleName, businessName);\r\n" +
                                        "        } else if (template.contains(\"SubTable.vue.vm\")) {\r\n" +
                                        "            fileName = StringUtils.format(\"{}/views/{}/{}/modules/SubTable.vue\", vuePath, moduleName, businessName);\r\n" +
                                        "        } else if (template.contains(\"CreateSubForm.vue.vm\")) {\r\n" +
                                        "            fileName = StringUtils.format(\"{}/views/{}/{}/modules/CreateSubForm.vue\", vuePath, moduleName, businessName);\r\n" +
                                        "        } else ");
            }
            // 处理FastJson2JsonRedisSerializer类型处理
            else if (filePath.endsWith("FastJson2JsonRedisSerializer.java")) {
                content = content
                        .replaceFirst("return JSON.toJSONString\\(t, JSONWriter\\.Feature\\.WriteClassName\\)\\.getBytes\\(DEFAULT_CHARSET\\);",
                                "if (t instanceof String) return (((String) t).replaceAll(\"\\\\\"\", \"\\\\\\\\\\\\\\\\\\\\\"\")).getBytes(DEFAULT_CHARSET);\r\n" +
                                        "        else if (t instanceof java.util.Set) return JSON.toJSONString(t, \"millis\").getBytes(DEFAULT_CHARSET);\r\n" +
                                        "        else if (t instanceof java.util.Date) return String.valueOf(((java.util.Date) t).getTime()).getBytes(DEFAULT_CHARSET);\r\n" +
                                        "        return JSON.toJSONString(t, \"millis\", JSONWriter.Feature.WriteClassName).getBytes(DEFAULT_CHARSET);")
                        .replaceFirst("\r\n        return JSON.parseObject\\(str, clazz, JSONReader\\.Feature\\.SupportAutoType\\);",
                                "        if (bytes[0] != '{' && bytes[0] != '[') str = \"\\\\\"\" + str + \"\\\\\"\";\r\n" +
                                        "        return JSON.parseObject(str, clazz, \"millis\", JSONReader.Feature.SupportAutoType);");
            }
            // 替换@Autowired为@Resource
            if (content.contains("import javax.annotation.Resource;")) {
                content = content
                        .replaceAll("@Autowired", "@Resource")
                        .replaceAll("import org.springframework.beans.factory.annotation.Autowired;\r\n", "");
            } else {
                content = content
                        .replaceAll("@Autowired", "@Resource")
                        .replaceAll("import org.springframework.beans.factory.annotation.Autowired;", "import javax.annotation.Resource;");
            }
            // 全局java文件替换
            content = content
                    .replaceAll("@author ruoyi", "@author 阮杰辉")
                    .replaceAll("RuoYi首创 ", "")
                    // 名称包含ruoyi的文件名处理
                    .replaceAll("RuoYiConfig", "ProjectConfig")
                    .replaceAll("ruoyiConfig", "projectConfig")
                    .replaceAll("@ConfigurationProperties\\(prefix = \"ruoyi\"\\)", "@ConfigurationProperties\\(prefix = \"project\"\\)")
                    .replaceAll("RuoYiApplication", "AdminApplication")
                    .replaceAll("RuoYiServletInitializer", "AdminServletInitializer")
                    // 泛型类尖括号内容去除
                    .replaceAll("new HashSet\\<.*?\\>", "new HashSet\\<\\>")
                    .replaceAll("new ArrayList\\<.*?\\>", "new ArrayList\\<\\>")
                    .replaceAll("new HashMap\\<.*?\\>", "new HashMap\\<\\>")
                    .replaceAll("new LinkedHashMap\\<.*?\\>", "new LinkedHashMap\\<\\>")
                    .replaceAll("new ExcelUtil\\<.*?\\>", "new ExcelUtil\\<\\>")
                    // 大括号格式化处理
                    .replaceAll("\r\n\\{", " \\{")
                    .replaceAll("\r\n\\s+\\{", " \\{")
                    .replaceAll("}\r\n\\s+else", "} else")
                    .replaceAll("}\r\n\\s+catch", "} catch")
                    .replaceAll("}\r\n\\s+finally", "} finally")
                    // 修正错误的括号替换
                    .replaceAll("// last 2 bits should be zero \\{", " \\{ // last 2 bits should be zero")
                    .replaceAll("// last 4 bits should be zero \\{", " \\{ // last 4 bits should be zero")
            ;
            // 替换logger
            if (content.contains("LoggerFactory.getLogger")) {
                content = content
                        .replaceAll("\r\npublic abstract class", "\r\n@Slf4j\r\npublic abstract class")
                        .replaceAll("\r\npublic class", "\r\n@Slf4j\r\npublic class")
                        .replaceAll("import org.slf4j.LoggerFactory;\r\n", "")
                        .replaceAll("import org.slf4j.Logger;", "import lombok.extern.slf4j.Slf4j;");
                if (content.contains("Logger logger =")) {
                    content = content
                            .replaceAll("    p.*? Logger logger =.*?;\r\n", "")
                            .replaceAll(" logger\\.", " log\\.");
                } else if (content.contains("Logger LOGGER =")) {
                    content = content
                            .replaceAll("    p.*? Logger LOGGER =.*?;\r\n", "")
                            .replaceAll(" LOGGER\\.", " log\\.");
                } else if (content.contains("Logger sys_user_logger =")) {
                    content = content
                            .replaceAll("    p.*? Logger sys_user_logger =.*?;\r\n", "")
                            .replaceAll(" sys_user_logger\\.", " log\\.");
                } else if (content.contains("Logger log =")) {
                    content = content
                            .replaceAll("    p.*? Logger log =.*?;\r\n", "");
                }
            }
            // 替换BeanUtil工具
            if (content.contains("common.utils.bean.BeanUtils;")) {
                content = content
                        .replaceAll("common\\.utils\\.bean\\.BeanUtils;", "common.utils.bean.BeanUtil;")
                        .replaceAll("BeanUtil\\.copyBeanProp\\(sysJob, context.getMergedJobDataMap\\(\\)\\.get\\(ScheduleConstants.TASK_PROPERTIES\\)\\);",
                                "BeanUtil.copyProperties(context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES), sysJob);");
            }
            // 类大括号之间的换行处理
            int firstPublic = content.indexOf("\r\npublic ");
            if (firstPublic > 0) {
                int firstBracket = content.substring(firstPublic).indexOf("{\r\n");
                if (firstBracket > 0) {
                    String beforeContent = content.substring(0, firstPublic + firstBracket + 3);
                    String afterContent = content.substring(firstPublic + firstBracket + 3);
                    if (afterContent.startsWith(" ")) {
                        content = beforeContent + "\r\n" + afterContent;
                    }
                }
            }
            // 结尾换行处理
            content = content.substring(0, content.lastIndexOf("}"));
            if (!content.endsWith("\r\n\r\n") && !content.endsWith("{\r\n")) {
                content += "\r\n";
            }
            if (content.endsWith("\r\n\r\n\r\n")) {
                content = content.substring(0, content.length() - 2);
            }
            content += "}\r\n";
            // @Data替换get和set
            if (filePath.contains(separator + "domain" + separator) || filePath.endsWith("RuoYiConfig.java")) {
                // 获取包名
                Pattern pattern = Pattern.compile("(?<=package).*(?=;)");
                Matcher matcher = pattern.matcher(content);
                matcher.find();
                String packageName = matcher.group().trim();
                String claasName = new File(filePath).getName();
                claasName = claasName.substring(0, claasName.lastIndexOf("."));
                // 构造函数替换
                String noConstruct1 = "    public " + claasName + "() {\r\n    }\r\n\r\n";
                String noConstruct2 = "    public " + claasName + "() {\r\n\r\n    }\r\n\r\n";
                // 是否包含import，不包含则需要换行
                String importLine = content.contains("\r\nimport ") ? "" : "\r\n";
                if (content.contains(noConstruct1) || content.contains(noConstruct2)) {
                    content = content.replace(noConstruct1, "")
                            .replace(noConstruct2, "");
                    content = content
                            .replaceFirst(";\r\n", ";\r\n\r\nimport lombok.Data;\r\nimport lombok.NoArgsConstructor;" + importLine)
                            .replaceAll("\r\npublic class", "\r\n@Data\r\n@NoArgsConstructor\r\npublic class");
                } else {
                    content = content
                            .replaceFirst(";\r\n", ";\r\n\r\nimport lombok.Data;" + importLine)
                            .replaceAll("\r\npublic class", "\r\n@Data\r\npublic class");
                }

                // 使用反射获取所有的属性
                Class clazz = Class.forName(packageName + "." + claasName);
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    String fieldName = field.getName();
                    String fieldType = field.getType().getSimpleName();
                    // System.out.println(fieldName + "=" + fieldType);
                    String methodName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    String getMethodName;
                    if (fieldType.equals("boolean")) {
                        getMethodName = "is" + methodName;
                    } else {
                        getMethodName = "get" + methodName;
                    }
                    if (fieldType.equals("List") || fieldType.equals("Set") || fieldType.equals("Map")) {
                        // 替换get方法
                        content = content.replaceAll("\r\n\r\n    public " + fieldType + "\\<[^\\>]+\\> " + getMethodName + "\\(\\) \\{\r\n" +
                                "        return " + fieldName + ";\r\n" +
                                "    \\}\r\n\r\n", "\r\n\r\n");
                        // 替换set方法
                        content = content.replaceAll("    public void set" + methodName + "\\(" + fieldType + "\\<[^\\>]+\\> " + fieldName + "\\) \\{\r\n" +
                                "        this." + fieldName + " = " + fieldName + ";\r\n" +
                                "    \\}\r\n\r\n", "");
                    } else {
                        // 替换get方法
                        content = content.replace("\r\n\r\n    public " + fieldType + " " + getMethodName + "() {\r\n" +
                                "        return " + fieldName + ";\r\n" +
                                "    }\r\n\r\n", "\r\n\r\n");
                        // 替换set方法
                        content = content.replace("    public void set" + methodName + "(" + fieldType + " " + fieldName + ") {\r\n" +
                                "        this." + fieldName + " = " + fieldName + ";\r\n" +
                                "    }\r\n\r\n", "");
                    }
                }
                // toString
                if (content.contains("public String toString()")) {
                    content = content
                            .replaceAll("import org.apache.commons.lang3.builder.ToStringBuilder;\r\n", "")
                            .replaceAll("import org.apache.commons.lang3.builder.ToStringStyle;\r\n", "")
                            .replaceAll("    @Override\r\n    public String toString[(][)] \\{[^\\}]+\\}\r\n\r\n", "")
                            .replaceAll("    public String toString[(][)] \\{[^\\}]+\\}\r\n\r\n", "");
                }
            }
        }
        // import导入和注释处理
        if (filePath.endsWith(".java")) {
            // 替换格式
            content = content
                    // 替换注释换行格式
                    .replace("*\r\n     */", "*/")
                    .replace("*\r\n */", "*/")
                    .replace("** @return", "* @return")
                    // .replace("*\r\n     * @return", "* @return")
                    // 替换大括号格式
                    .replace("{ \"", "{\"")
                    .replace("\" }", "\"}")
                    // 替换对象new
                    .replace("[] {", "[]{")
            ;
            // 按行读取
            String[] contents = content.split("\r\n");
            StringBuffer stringBuffer = new StringBuffer();
            StringBuffer beforeImport = new StringBuffer();
            StringBuffer afterImport = new StringBuffer();
            List<String> repImports = new ArrayList<>();
            List<String> javaImports = new ArrayList<>();
            List<String> javaxImports = new ArrayList<>();
            for (int i = 0; i < contents.length; i++) {
                String item = contents[i];
                if (item.startsWith("import")) {
                    item = item.replaceAll("\\.", "1111").replaceAll(";", "0000");
                    if (item.startsWith("import javax")) {
                        javaxImports.add(item);
                    } else if (item.startsWith("import java")) {
                        javaImports.add(item);
                    } else {
                        repImports.add(item);
                    }
                    continue;
                }
                // 替换大括号空格
                item = replaceContent(item, " = \\{", "\\}\\)");
                item = replaceContent(item, "@Target\\(\\{", "\\}\\)");
                item = replaceContent(item, "\\[\\]\\{", "\\}");
                // 处理注释
                String text = item.trim();
                if (text.startsWith("* @param ")) {
                    String paramComment = text.substring(9);
                    int paramIndex = paramComment.indexOf(" ");
                    if (paramIndex < 0) {
                        // 不包含注释直接返回
                        afterImport.append(item + "\r\n");
                        continue;
                    }
                    String param = paramComment.substring(0, paramIndex);
                    String comment = paramComment.substring(paramIndex).trim();
                    int paramLength = param.length();
                    int maxLength = paramLength;
                    // 往前找所有的参数
                    for (int j = i - 1; j > 0; j--) {
                        String beforeText = contents[j].trim();
                        if (beforeText.startsWith("* @param ")) {
                            String beforeParamComment = beforeText.substring(9);
                            String beforeParam = beforeParamComment.substring(0, beforeParamComment.indexOf(" "));
                            int beforeLength = beforeParam.length();
                            if (beforeLength > maxLength) {
                                maxLength = beforeLength;
                            }
                        } else {
                            if (j == i - 1) {
                                // 参数前面要换行
                                if (!beforeText.equals("*")) {
                                    afterImport.append("     *\r\n");
                                }
                            }
                            break;
                        }
                    }
                    // 往后找所有的参数
                    for (int j = i + 1; j < contents.length; j++) {
                        String afterText = contents[j].trim();
                        if (afterText.startsWith("* @param ")) {
                            String afterParamComment = afterText.substring(9);
                            String afterParam = afterParamComment.substring(0, afterParamComment.indexOf(" "));
                            int afterLength = afterParam.length();
                            if (afterLength > maxLength) {
                                maxLength = afterLength;
                            }
                        } else {
                            break;
                        }
                    }
                    String space = " ";
                    if (paramLength != maxLength) {
                        for (int j = 0; j < maxLength - paramLength; j++) {
                            space += " ";
                        }
                    }
                    // item = item.replaceFirst(text, "\\* @param " + param + space + comment);
                    item = "     * @param " + param + space + comment;
                } else if (text.equals("*")) {
                    String lastText = contents[i - 1].trim();
                    String nextText = contents[i + 1].trim();
                    if ((lastText.startsWith("* @param") && nextText.startsWith("* @return")) ||
                            (lastText.startsWith("* @return") && nextText.startsWith("* @throws"))) {
                        continue;
                    }
                }
                if (repImports.size() > 0 || javaxImports.size() > 0 || javaImports.size() > 0) {
                    afterImport.append(item + "\r\n");
                } else {
                    beforeImport.append(item + "\r\n");
                }
            }
            Collections.sort(repImports);
            Collections.sort(javaxImports);
            Collections.sort(javaImports);
            for (String item : repImports) {
                item = item.replaceAll("1111", ".").replaceAll("0000", ";");
                stringBuffer.append(item + "\r\n");
            }
            if (repImports.size() > 0 && (javaxImports.size() > 0 || javaImports.size() > 0)) {
                stringBuffer.append("\r\n");
            }
            for (String item : javaxImports) {
                item = item.replaceAll("1111", ".").replaceAll("0000", ";");
                stringBuffer.append(item + "\r\n");
            }
            for (String item : javaImports) {
                item = item.replaceAll("1111", ".").replaceAll("0000", ";");
                stringBuffer.append(item + "\r\n");
            }
            content = beforeImport + stringBuffer.toString() + afterImport;
        }
        return content
                .replaceAll(PACKAGE_NAME, packageNameNew)
                .replaceAll(ARTIFACT_ID, artifactIdNew)  // 非pom文件也要替换
                .replaceAll(TITLE, titleNew)
                .replaceAll("若依", "云测平台");
    }

    private static String buildNewFilePath(File file) {
        return file.getPath().replace(projectBaseDir, projectBaseDirNew) // 新目录
                .replace("ruoyi-", artifactIdNew + "-")
                .replace("sql" + separator + "quartz.sql", "script" + separator + "sql" + separator + "quartz.sql")
                .replace("sql" + separator + "ry_20220822.sql", "script" + separator + "sql" + separator + "mysql.sql")
                .replace("RuoYiConfig", "ProjectConfig")
                .replace("RuoYiApplication", "AdminApplication")
                .replace("RuoYiServletInitializer", "AdminServletInitializer")
                .replace(PACKAGE_NAME.replaceAll("\\.", Matcher.quoteReplacement(separator)),
                        packageNameNew.replaceAll("\\.", Matcher.quoteReplacement(separator)));
    }

    public static <T> Set<T> asSet(T... objs) {
        return new HashSet<>(Arrays.asList(objs));
    }

}
