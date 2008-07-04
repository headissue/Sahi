/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.sahi.plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.util.Utils;
import net.sf.sahi.util.ClassLoadHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class DBClient {

    public String driverName;
    public String jdbcurl;
    public String username;
    public String password;
    public String sql;

    public void execute(final HttpRequest request) {
        try {
            init(request);
            ClassLoadHelper.getClass(driverName);
            Connection connection = DriverManager.getConnection(jdbcurl, username, password);
            Statement stmt = connection.createStatement();
            try {
                stmt.executeUpdate(sql);
            } catch (Exception e) {
                stmt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HttpResponse select(final HttpRequest request) {
        try {
            init(request);
            String s = getJSObject(getResult(driverName, jdbcurl, username, password));
//            System.out.println(s.replaceAll("\\},\\{", "},\n{"));
            return new SimpleHttpResponse(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    ArrayList getResult(final String driverName, final String jdbcurl, final String username, final String password) throws ClassNotFoundException, SQLException {
        ClassLoadHelper.getClass(driverName);
        Connection connection = DriverManager.getConnection(jdbcurl, username, password);
        Statement stmt = connection.createStatement();
        ArrayList list = new ArrayList();
        try {
            ResultSet rs = stmt.executeQuery(sql);

            ArrayList columnNames = new ArrayList();
            ResultSetMetaData rsmd = rs.getMetaData();
            int numColumns = rsmd.getColumnCount();
            for (int i = 1; i < numColumns + 1; i++) {
                String columnName = rsmd.getColumnName(i);
                columnNames.add(columnName);
            }
            list = new ArrayList();
            while (rs.next()) {
                HashMap map = new LinkedHashMap();
                for (Iterator iterator = columnNames.iterator(); iterator.hasNext();) {
                    String columnName = (String) iterator.next();
                    String value = rs.getString(columnName);
                    map.put(columnName, value);
                }
                list.add(map);
            }
        } catch (SQLException e) {
            stmt.close();
        }
        return list;
    }

    String getJSObject(final ArrayList list) {
        StringBuffer sb = new StringBuffer();
        sb.append("var a=[");
        boolean isFirst1 = true;
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            if (isFirst1) {
                isFirst1 = false;
            } else {
                sb.append(",");
            }
            sb.append("{");
            boolean isFirst2 = true;
            HashMap map = (HashMap) iterator.next();
            Set keys = map.keySet();
            for (Iterator iterator1 = keys.iterator(); iterator1.hasNext();) {
                if (isFirst2) {
                    isFirst2 = false;
                } else {
                    sb.append(",");
                }
                String columnName = (String) iterator1.next();
                String value = (String) map.get(columnName);
                sb.append(columnName + ":\"" + Utils.escapeDoubleQuotesAndBackSlashes(value) + "\"");
            }
            sb.append("}");
        }
        sb.append("];a");
        return sb.toString();
    }

    private void init(final HttpRequest request) {
        driverName = request.getParameter("driver");
        jdbcurl = request.getParameter("jdbcurl");
        username = request.getParameter("username");
        password = request.getParameter("password");
        sql = request.getParameter("sql");
        System.out.println("Driver: " + driverName);
        System.out.println("JDBC URL: " + jdbcurl);
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        System.out.println("SQL: " + sql);
    }
}
