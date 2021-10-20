package com.mybatis.code.utils;

/**
 * @author jichf
 * @Description
 * @create 2021-10-19 16:13
 */

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.util.Properties;

public class VelocityUtil {
    public static void generate(String inputVmFilePath, String outputFilePath, VelocityContext context) throws Exception {
        try {
            Properties properties = new Properties();
            VelocityEngine ve = new VelocityEngine();

            String basePath = getPath(inputVmFilePath);
            properties.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, basePath);
            ve.init(properties);
            String vmName = getVmName(inputVmFilePath);
            Template template = ve.getTemplate(vmName, "utf-8");

            File outputFile = new File(outputFilePath);
            mkDir(outputFile);
            FileWriterWithEncoding writer = new FileWriterWithEncoding(outputFile, "utf-8");
            template.merge(context, writer);
            writer.close();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static String getVmName(String filePath){
        String vmName = "";
        if (StringUtils.isNotBlank(filePath))
            vmName = filePath.substring(filePath.lastIndexOf("/") + 1);
        return vmName;
    }

    public static String getPath(String filePath) {
        String path = "";
        if (StringUtils.isNotBlank(filePath))
            path = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        return path;
    }

    public static String getFile(String filePath) {
        String file = "";
        if (StringUtils.isNotBlank(filePath))
            file = filePath.substring(filePath.lastIndexOf("/") + 1);
        return file;
    }

    public static void mkDir(File file) {
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
    }
}