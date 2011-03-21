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
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.util.ClassLoadHelper;
import net.sf.sahi.util.Utils;

public class DBClient {

    public String driverName;

    public String jdbcurl;

    public String username;

    public String password;

    public String sql;

    public void execute(final String driverName, final String jdbcurl,
            final String username, final String password, final String sql) {
        Statement stmt = null;
        Connection connection = null;
        try {
            ClassLoadHelper.getClass(driverName);
            connection = DriverManager.getConnection(jdbcurl, username,
                    password);
            stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    stmt.close();
                    connection.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public void execute(final HttpRequest request) {
        init(request);
        execute(driverName, jdbcurl, username, password, sql);
    }

    public String select(final String driverName, final String jdbcurl,
            final String username, final String password, final String sql) {
        try {
            return getJSObject(getResult(driverName, jdbcurl, username, password, sql));
        } catch (Exception e) {
            e.printStackTrace();
            return "exception: " + Utils.getStackTraceString(e);
        }
    }

    public HttpResponse select(final HttpRequest request) {
        init(request);
        String s = select(driverName, jdbcurl, username, password, sql);
        return new SimpleHttpResponse(s);
    }

    ArrayList<ArrayList<String>> getResult(final String driverName, final String jdbcurl,
            final String username, final String password, final String sql)
            throws ClassNotFoundException, SQLException {
        ClassLoadHelper.getClass(driverName);
        Connection connection = DriverManager.getConnection(jdbcurl, username,
                password);
        Statement stmt = connection.createStatement();
        ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
        try {
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<String> columnNames = new ArrayList<String>();
            ResultSetMetaData rsmd = rs.getMetaData();
            int numColumns = rsmd.getColumnCount();
            for (int i = 1; i < numColumns + 1; i++) {
                String columnName = rsmd.getColumnName(i);
                columnNames.add(columnName);
            }
            list.add(columnNames);
            while (rs.next()) {
            	ArrayList<String> record = new ArrayList<String>();
                for (Iterator<String> iterator = columnNames.iterator(); iterator.hasNext();) {
                    String columnName = iterator.next();
                    String value = rs.getString(columnName);
                    record.add(value);
                }
                list.add(record);
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                }
            }
        }
        return list;
    }

    String getJSObject(final ArrayList<ArrayList<String>> list) {
        StringBuffer sb = new StringBuffer();
        sb.append("{result: [[");
        boolean isFirst1 = true;
        for (Iterator<ArrayList<String>> iterator = list.iterator(); iterator.hasNext();) {
        	if (isFirst1)
        		isFirst1 = false;
        	else
        		sb.append(",[");
        	ArrayList<String> record = iterator.next();  
            for (int i = 0; i < record.size(); i++ ) {
                if(i!=0){
                    sb.append(",");
                }
                String value = record.get(i);
                sb.append("\"" + Utils.makeString(value) + "\"");
                //sb.append("\"" + Utils.escapeDoubleQuotesAndBackSlashes(value) + "\"");
            }
            sb.append("]");
        }        
        sb.append("]}");
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
