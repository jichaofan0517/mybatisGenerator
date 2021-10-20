package com.mybatis.code.utils;

/**
 * @author jichf
 * @Description
 * @create 2021-10-19 15:43
 */

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.velocity.VelocityContext;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

public class MybatisGeneratorUtil {
    private static String generatorConfig_vm = "/template/generatorConfig.vm";

    private static String service_vm = "/template/Service.vm";

    private static String serviceImpl_vm = "/template/ServiceImpl.vm";

//
//    private static String result_vm = "/template/Result.vm";
//
//    private static String resultConstant_vm = "/template/ResultConstant.vm";
//


    public static void generator(String jdbc_driver, String jdbc_url, String jdbc_username, String jdbc_password, String database, String table_name, String table_key, boolean is_auto_inc, String package_name, String author) throws Exception {
        generator(jdbc_driver, jdbc_url, jdbc_username, jdbc_password, database, table_name, table_key, is_auto_inc, package_name, author, DbType.MySQL);
    }

    public static void generatorWithOutVueAndController(String jdbc_driver, String jdbc_url, String jdbc_username, String jdbc_password, String database, String table_name, String table_key, boolean is_auto_inc, String package_name, String author) throws Exception {
        generator(jdbc_driver, jdbc_url, jdbc_username, jdbc_password, database, table_name, table_key, is_auto_inc, package_name, author, DbType.MySQL);
    }

    @Deprecated
    public static void generator(String jdbc_driver, String jdbc_url, String jdbc_username, String jdbc_password, String database, String table_name, String table_key, boolean is_auto_inc, String package_name, String author, int type) throws Exception {
        DbType dbType = null;
        if (type == 1) {
            dbType = DbType.Oracle;
        } else if (type == 2) {
            dbType = DbType.DB2;
        }
        generator(jdbc_driver, jdbc_url, jdbc_username, jdbc_password, database, table_name, table_key, is_auto_inc, package_name, author, dbType);
    }

    public static void generator(String jdbc_driver, String jdbc_url, String jdbc_username, String jdbc_password, String database, String table_name, String table_key, boolean is_auto_inc, String package_name, String author, DbType dbType) throws Exception {
        if (dbType == DbType.MySQL) {
            int index = jdbc_url.indexOf("?");
            if (jdbc_url.indexOf("nullCatalogMeansCurrent") < 0) {
                jdbc_url = jdbc_url + ((index < 0) ? "?" : "&amp;quot;");
                jdbc_url = jdbc_url + "nullCatalogMeansCurrent=true";
            } else if (jdbc_url.indexOf("nullCatalogMeansCurrent=false") > 0) {
                jdbc_url = jdbc_url.replace("nullCatalogMeansCurrent=false", "nullCatalogMeansCurrent=true");
            }
        }
        if (author == null || "".equals(author))
            author = "lambo";

        String os = System.getProperty("os.name");
        String ctime = (new SimpleDateFormat("yyyy/M/d")).format(new Date());
        String basePath = MybatisGeneratorUtil.class.getResource("/").getPath().replace("/target/classes/", "");
        //临时路径
        String temp_generator_vm_path = "";
        String temp_service_vm_path = "";
        String temp_serviceImpl_vm_path = "";

        if (os.toLowerCase().startsWith("win")) {
            basePath = basePath.replaceFirst("/", "");
            temp_generator_vm_path = MybatisGeneratorUtil.class.getResource(generatorConfig_vm).getPath().replaceFirst("/", "");
            temp_service_vm_path = MybatisGeneratorUtil.class.getResource(service_vm).getPath().replaceFirst("/", "");
            temp_serviceImpl_vm_path = MybatisGeneratorUtil.class.getResource(serviceImpl_vm).getPath().replaceFirst("/", "");

        } else {
            temp_generator_vm_path = MybatisGeneratorUtil.class.getResource(generatorConfig_vm).getPath();
            temp_service_vm_path = MybatisGeneratorUtil.class.getResource(service_vm).getPath();
            temp_serviceImpl_vm_path = MybatisGeneratorUtil.class.getResource(serviceImpl_vm).getPath();
        }
//        String generatorConfig_xml = (MybatisGeneratorUtil.class.getResource("/").getPath().replace("/target/classes/", "") + "/src/main/resources/generatorConfig.xml").replaceFirst("/", "");
        String generatorConfig_xml = (MybatisGeneratorUtil.class.getResource("/").getPath().replace("/target/classes/", "") + "/src/main/resources/generatorConfig.xml");
        String sql = "";
        if (dbType.equals(DbType.MySQL)) {
            sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "' AND TABLE_NAME = '" + table_name + "';";
        } else if (dbType.equals(DbType.Oracle)) {
            sql = "SELECT TABLE_NAME FROM USER_TAB_COMMENTS WHERE TABLE_TYPE='TABLE' AND  TABLE_NAME = '" + table_name + "'";
        } else if (dbType.equals(DbType.H2)) {
            sql = "SELECT  TABLE_NAME FROM INFORMATION_SCHEMA.TABLES  where TABLE_NAME = '" + table_name + "'";
        } else if (dbType.equals(DbType.DB2)) {
            sql = "SELECT TABNAME AS TABLE_NAME FROM SYSCAT.TABLES WHERE TABSCHEMA = '" + database + "' AND TABNAME = '" + table_name + "' ";
        }
        System.out.println(sql);
        System.out.println("========== ==========");
        System.out.println("========== 开始生成generatorConfig.xml文件 ==========");
        List<Map<String, Object>> tables = new ArrayList<>();
        try {
            VelocityContext context = new VelocityContext();
            JdbcUtil jdbcUtil = new JdbcUtil(jdbc_driver, jdbc_url, jdbc_username, jdbc_password);
            List<Map> result = jdbcUtil.selectByParams(sql, null);
            for (Map map : result) {
                Map<String, Object> table = new HashMap<>();
                Object object = map.get("TABLE_NAME");
                String moduleName = StringUtils.lineToHump(ObjectUtils.toString(object));
                table.put("table_name", object);
                table.put("model_name", moduleName);
                if (is_auto_inc)
                    table.put("table_key", table_key);
                String columnSql = "";
                if (dbType.equals(DbType.MySQL)) {
                    columnSql = "SELECT COLUMN_NAME,DATA_TYPE,IS_NULLABLE,COLUMN_DEFAULT FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + database + "' AND TABLE_NAME='" + map.get("TABLE_NAME") + "'";
                } else if (dbType.equals(DbType.Oracle)) {
                    columnSql = " SELECT  A.COLUMN_NAME,A.COMMENTS ,B.DATA_TYPE,B.NULLABLE IS_NULLABLE,B.DATA_DEFAULT COLUMN_DEFAULT  from USER_COL_COMMENTS A,USER_TAB_COLUMNS B  where   A.TABLE_NAME =B.TABLE_NAME AND A.COLUMN_NAME=B.COLUMN_NAME    AND A.TABLE_NAME='" + table_name + "'";
                } else if (dbType.equals(DbType.H2)) {
                    columnSql = "SELECT COLUMN_NAME,DATA_TYPE,IS_NULLABLE,COLUMN_DEFAULT FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME= '" + map.get("TABLE_NAME") + "'";
                } else if (dbType.equals(DbType.DB2)) {
                    columnSql = "SELECT COLNAME COLUMN_NAME,TYPENAME DATA_TYPE,NULLS IS_NULLABLE,DEFAULT COLUMN_DEFAULT FROM SYSCAT.COLUMNS WHERE TABNAME='" + table_name + "' AND TABSCHEMA='" + database + "'";
                }
                List<Map> columnResult = jdbcUtil.selectByParams(columnSql, null);
                List<Map> columns = new ArrayList<>();
                for (Map<String, String> column : columnResult) {
                    String lineColumnName = column.get("COLUMN_NAME").toString().toLowerCase();
                    String humpColumnName = StringUtils.toLowerCaseFirstOne(StringUtils.lineToHump(lineColumnName));
                    String allHumpColumnName = StringUtils.lineToHump(lineColumnName);
                    column.put("COLUMN_NAME_HUMP", humpColumnName);
                    column.put("COLUMN_NAME_HUMP_ALL", allHumpColumnName);
                    columns.add(column);
                }
                table.put("columns", columns);
                tables.add(table);
                deleteDir(new File(basePath + "/src/main/java/" + package_name.replaceAll("\\.", "/") + "/dao/model/" + moduleName + ".java"));
                deleteDir(new File(basePath + "/src/main/java/" + package_name.replaceAll("\\.", "/") + "/dao/model/" + moduleName + "Example.java"));
                deleteDir(new File(basePath + "/src/main/java/" + package_name.replaceAll("\\.", "/") + "/dao/mapper/" + moduleName + "Mapper.java"));
                deleteDir(new File(basePath + "/src/main/java/" + package_name.replaceAll("\\.", "/") + "/dao/mapper/" + moduleName + "Mapper.xml"));
            }
            jdbcUtil.release();
            context.put("tables", tables);
            context.put("generator_javaModelGenerator_targetPackage", package_name + ".model");
            context.put("generator_sqlMapGenerator_targetPackage", package_name + ".dao.mapper");
            context.put("generator_javaClientGenerator_targetPackage", package_name + ".dao.mapper");
            context.put("targetProject", basePath);
            context.put("generator_jdbc_driver", jdbc_driver);
            context.put("generator_jdbc_url", jdbc_url);
            context.put("generator_jdbc_username", jdbc_username);
            context.put("generator_jdbc_password", jdbc_password);
            VelocityUtil.generate(temp_generator_vm_path, generatorConfig_xml, context);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        System.out.println("========== 结束生成generatorConfig.xml文件 ==========");
        System.out.println("========== ==========");
        System.out.println("========== ==========");
        System.out.println("========== 开始运行MybatisGenerator ==========");
        List<String> warnings = new ArrayList<>();
        File configFile = new File(generatorConfig_xml);
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(configFile);
        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, (ShellCallback) callback, warnings);
        myBatisGenerator.generate(null);
        for (String warning : warnings)
            System.out.println(warning);
        deleteDir(new File(generatorConfig_xml));
        System.out.println("========== ==========");
        System.out.println("========== ==========");

        String servicePath = basePath + "/src/main/java/" + package_name.replaceAll("\\.", "/") + "/service/api";
        String serviceImplPath = basePath + "/src/main/java/" + package_name.replaceAll("\\.", "/") + "/service/impl";

        for (int i = 0; i < tables.size(); i++) {
            String model = StringUtils.lineToHump(ObjectUtils.toString(((Map) tables.get(i)).get("table_name")));
            String service = servicePath + "/" + model + "Service.java";
            String serviceImpl = serviceImplPath + "/" + model + "ServiceImpl.java";
            VelocityContext serviceFile = new VelocityContext();
            serviceFile.put("package_name", package_name);
            serviceFile.put("model", model);
            serviceFile.put("ctime", ctime);
            serviceFile.put("author", author);
            VelocityUtil.generate(temp_service_vm_path, service, serviceFile);
            System.out.println(service);
            VelocityContext serviceImplFile = new VelocityContext();
            serviceImplFile.put("package_name", package_name);
            serviceImplFile.put("model", model);
            serviceImplFile.put("mapper", StringUtils.toLowerCaseFirstOne(model));
            serviceImplFile.put("ctime", ctime);
            serviceImplFile.put("author", author);
            VelocityUtil.generate(temp_serviceImpl_vm_path, serviceImpl, serviceImplFile);
            System.out.println(serviceImpl);

        }
        System.out.println("========== ==========");
        System.out.println("========== ==========");
        String tableName = StringUtils.lineToHump(ObjectUtils.toString(((Map) tables.get(0)).get("table_name")));
        addSuppressWarning(new File(basePath + "/src/main/java/" + package_name.replaceAll("\\.", "/") + "/model/" + tableName + ".java"));
        addSuppressWarning(new File(basePath + "/src/main/java/" + package_name.replaceAll("\\.", "/") + "/model/" + tableName + "Example.java"));
        addSuppressWarning(new File(basePath + "/src/main/java/" + package_name.replaceAll("\\.", "/") + "/dao/mapper/" + tableName + "Mapper.java"));
        System.out.println("========== ==========");
        System.out.println("========== 结束运行MybatisGenerator ==========");
    }

    private static void addSuppressWarning(File file) {
        if (file == null)
            return;
        try {
            String content = FileUtils.readFileToString(file);
            content = content.replace("public class", "@SuppressWarnings(\"all\")\npublic class");
            content = content.replace("public interface", "@SuppressWarnings(\"all\")\npublic interface");
            FileUtils.writeStringToFile(file, content);
        } catch (IOException iOException) {
        }
    }

    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++)
                deleteDir(files[i]);
        }
        dir.delete();
    }
}
