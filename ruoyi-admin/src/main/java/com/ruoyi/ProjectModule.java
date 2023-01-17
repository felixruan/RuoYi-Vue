package com.ruoyi;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;

import static java.io.File.separator;

public class ProjectModule {

    private static Logger log = LoggerFactory.getLogger(ProjectModule.class);

    private static final String projectBaseDir = "D:\\code\\ruoyi\\RuoYi-Vue-New"; // 一键改名后，“新”项目所在的目录

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        log.info("[main][新项目路径地址 ({})]", projectBaseDir);
        // 写入文件
        String filePath = projectBaseDir + separator + "pom.xml";
        String content = replaceFileContent(filePath);
        FileUtils.write(new File(filePath), content, Charset.forName("UTF-8"));
        // 写入文件
        filePath = projectBaseDir + separator + "server-admin" + separator + "pom.xml";
        content = replaceFileContent(filePath);
        FileUtils.write(new File(filePath), content, Charset.forName("UTF-8"));
        log.info("[main][重写完成]共耗时：{} 毫秒", (System.currentTimeMillis() - start));
    }

    private static String replaceFileContent(String filePath) throws Exception {
        String content = FileUtils.readFileToString(new File(filePath), Charset.forName("UTF-8"));
        // 去除模块引入的pom
        if (filePath.equals(projectBaseDir + separator + "pom.xml")) {
            // System.out.println(file.getPath());
            if (content.contains("server-cloudtest")) {
                return content;
            }
            content = content
                    // 去除ruoyi相关名称
                    .replaceFirst("<artifactId>server-common</artifactId>\r\n" +
                                    "                <version>\\$\\{server.version}</version>\r\n" +
                                    "            </dependency>",
                            "<artifactId>server-common</artifactId>\r\n" +
                                    "                <version>\\$\\{server.version}</version>\r\n" +
                                    "            </dependency>\r\n\r\n" +
                                    "            <!-- 群控模块 -->\r\n" +
                                    "            <dependency>\r\n" +
                                    "                <groupId>com.igg.cloudtest</groupId>\r\n" +
                                    "                <artifactId>server-cloudtest</artifactId>\r\n" +
                                    "                <version>\\$\\{server.version}</version>\r\n" +
                                    "            </dependency>")
                    .replaceFirst("</modules>", "<module>server-cloudtest</module>\r\n    </modules>")
            ;
        } else if (filePath.equals(projectBaseDir + separator + "server-admin" + separator + "pom.xml")) {
            if (content.contains("server-cloudtest")) {
                return content;
            }
            content = content
                    .replaceFirst("</dependencies>",
                            "    <dependency>\r\n" +
                                    "            <groupId>com.igg.cloudtest</groupId>\r\n" +
                                    "            <artifactId>server-cloudtest</artifactId>\r\n" +
                                    "        </dependency>\r\n\r\n" +
                                    "    </dependencies>"
                    )
            ;
        }
        return content;
    }

}
