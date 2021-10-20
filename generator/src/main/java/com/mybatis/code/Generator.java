package com.mybatis.code;

import com.mybatis.code.utils.MybatisGeneratorUtil;

/**
 * @author jichf
 * @Description
 * @create 2021-10-19 17:15
 */
public class Generator {

    private final static String BASE_PACKAGE = "com.mybatis.code.autocode.";
    private final static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private final static String JDBC_URL = "jdbc:mysql://localhost:3306/mdihdb";
    private final static String JDBC_USERNAME = "root";
    private final static String JDBC_PASSWORD = "1234";
    private final static String DATABASE="mdihdb";
    /**
     * 自动代码生成
     */
    public static void main(String[] args) throws Exception {

        /*************!!!!!表名、字段名必须小写!!!!!!***********/

        generate("sys_employee", Module.USER, true);
        generate("sys_organization", Module.ORG, true);

    }

    /**
     * 生成代码
     * @param tableName 表名
     * @param module 所属模块，使用枚举类
     * @param redo 是否生成
     */
    private static void generate(String tableName, Module module, boolean redo) throws Exception {
        if(!redo){
            return ;
        }
        MybatisGeneratorUtil.generatorWithOutVueAndController(JDBC_DRIVER,
                JDBC_URL,
                JDBC_USERNAME,
                JDBC_PASSWORD,
                DATABASE,
                tableName,
                "id",
                false,
                BASE_PACKAGE + module.packageName,
                "jichf");
    }
    private enum Module {
        //USER
        USER("user"),
        ORG("organization"),
        ;

        String packageName;
        Module(String packageName) {
            this.packageName = packageName;
        }
    }
}
