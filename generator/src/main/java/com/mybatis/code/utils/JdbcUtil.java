package com.mybatis.code.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jichf
 * @Description
 * @create 2021-10-19 16:18
 */

public class JdbcUtil {
    private static Logger logger = LoggerFactory.getLogger(JdbcUtil.class);
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;

    public JdbcUtil(String driver, String url, String username, String password) {
        try {
            Class.forName(driver);
            this.conn = DriverManager.getConnection(url, username, password);
            logger.info("数据库连接成功，url=：" + url);
        } catch (Exception var6) {
            logger.error("数据库连接失败", var6);
        }

    }

    public List<Map> selectByParams(String sql, List params) throws SQLException {
        List<Map> list = new ArrayList();
        int index = 1;
        this.pstmt = this.conn.prepareStatement(sql);
        if (null != params && !params.isEmpty()) {
            for(int i = 0; i < params.size(); ++i) {
                this.pstmt.setObject(index++, params.get(i));
            }
        }

        this.rs = this.pstmt.executeQuery();
        ResultSetMetaData metaData = this.rs.getMetaData();
        int cols_len = metaData.getColumnCount();

        while(this.rs.next()) {
            Map map = new HashMap();

            for(int i = 0; i < cols_len; ++i) {
                String columnLabel = metaData.getColumnLabel(i + 1);
                Object cols_value = this.rs.getObject(columnLabel);
                if (null == cols_value) {
                    cols_value = "";
                }

                map.put(columnLabel, cols_value);
            }

            list.add(map);
        }

        return list;
    }

    public void release() {
        try {
            if (null != this.rs) {
                this.rs.close();
            }

            if (null != this.pstmt) {
                this.pstmt.close();
            }

            if (null != this.conn) {
                this.conn.close();
            }
        } catch (SQLException var2) {
            var2.printStackTrace();
        }

    }
}
