package net.sf.sahi.plugin;

import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Sahi - Web Automation and Test Tool
 * <p/>
 * Copyright  2006  V Narayan Raman
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class DBClientTest {

  public String driverName;
  public String jdbcurl;
  public String username;
  public String password;
  public String sql;

  @Test
  public void testGetJSObject() throws SQLException, ClassNotFoundException {
    final DBClient dbClient = new DBClient();
    ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
    ArrayList<String> columnNames = new ArrayList<String>();

    columnNames.add("h1");
    columnNames.add("h2");
    columnNames.add("h3");
    list.add(columnNames);

    ArrayList<String> record1 = new ArrayList<String>();
    record1.add("a1");
    record1.add("a2");
    record1.add("a3");
    list.add(record1);

    ArrayList<String> record2 = new ArrayList<String>();
    record2.add("b1");
    record2.add("b2");
    record2.add("b3");
    list.add(record2);

    assertEquals("{result: [[\"h1\",\"h2\",\"h3\"],[\"a1\",\"a2\",\"a3\"],[\"b1\",\"b2\",\"b3\"]]}", dbClient.getJSObject(list));
    assertEquals("{result: [[\"h1\",\"h2\",\"h3\"],[\"a1\",\"a2\",\"a3\"],[\"b1\",\"b2\",\"b3\"]]}", dbClient.getJSObject(list));
  }
}
