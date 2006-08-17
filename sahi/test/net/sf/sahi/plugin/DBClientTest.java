package net.sf.sahi.plugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import junit.framework.TestCase;

public class DBClientTest extends TestCase {
    public String driverName;
    public String jdbcurl;
    public String username;
    public String password;
    public String sql;

    public void testGetJSObject() throws SQLException, ClassNotFoundException {
        final DBClient dbClient = new DBClient();
        ArrayList list = new ArrayList();

        HashMap map1 = new LinkedHashMap();
        map1.put("map1_k1", "map1_v1");
        map1.put("map1_k2", "map1_v2");
        list.add(map1);

        HashMap map2 = new HashMap();
        map2.put("map2_k1", "map2_v1");
        map2.put("map2_k2", "map2_v2");
        list.add(map2);

        assertEquals("var a=[{map1_k1:\"map1_v1\",map1_k2:\"map1_v2\"},{map2_k2:\"map2_v2\",map2_k1:\"map2_v1\"}];a", dbClient.getJSObject(list));
    }
}
