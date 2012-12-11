/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.malhartech.lib.io;

import com.malhartech.api.Context.OperatorContext;
import com.malhartech.bufferserver.util.Codec;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Locknath Shil <locknath@malhar-inc.com>
 */
public class JDBCNonTransactionOutputOperatorTest
{
  private static final Logger logger = LoggerFactory.getLogger(JDBCNonTransactionOutputOperatorTest.class);
  private static int tupleCount = 0;
  private static final int maxTuple = 20;
  private static int columnCount = 7;
  private static boolean readDone = false;

  public static void createDatabase(String dbName, Connection con)
  {
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      String dropDB = "DROP DATABASE IF EXISTS " + dbName;
      String createDB = "CREATE DATABASE " + dbName;
      String useDB = "USE " + dbName;

      stmt.executeUpdate(dropDB);
      stmt.executeUpdate(createDB);
      stmt.executeQuery(useDB);
    }
    catch (SQLException ex) {
      logger.debug("exception during creating database", ex);
    }
    finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
      }
      catch (SQLException ex) {
      }
    }

    logger.debug("JDBC DB creation Success");
  }

  public static void createTable(String tableName, Connection con)
  {
    Statement stmt = null;

    try {
      stmt = con.createStatement();
      //String table = "CREATE TABLE " + tableName + " (col1 INT, col2 INT, col3 INT, col4 INT, "
      //       + "col5 INT, col6 INT, col7 INT)";
      String table = "CREATE TABLE " + tableName + " (col1 INT, col2 VARCHAR(10), col3 INT, col4 VARCHAR(10), "
              + "col5 INT, col6 VARCHAR(10), col7 INT, winid INT)";

      stmt.executeUpdate(table);
    }
    catch (SQLException ex) {
      logger.debug("exception during creating database", ex);
    }
    finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
      }
      catch (SQLException ex) {
      }
    }
    logger.debug("JDBC Table creation Success");
  }

  public static void readTable(String tableName, Connection con)
  {
    String query = "SELECT * FROM " + tableName;
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);

      while (rs.next()) {
        logger.debug(String.format("%d, %d, %d, %d, %d, %d, %d",
                                   rs.getInt("col1"), rs.getInt("col2"), rs.getInt("col3"),
                                   rs.getInt("col4"), rs.getInt("col5"), rs.getInt("col6"),
                                   rs.getInt("col7")));
        tupleCount++;
      }
    }
    catch (SQLException ex) {
      logger.debug("exception during reading from table", ex);
    }
    finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
      }
      catch (SQLException ex) {
      }
    }
  }

  public static void readTableText(String tableName, Connection con)
  {
    String query = "SELECT * FROM " + tableName;
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);

      while (rs.next()) {
        logger.debug(String.format("%d, %s, %d, %s, %d, %s, %d",
                                   rs.getInt("col1"), rs.getString("col2"),
                                   rs.getInt("col3"), rs.getString("col4"),
                                   rs.getInt("col5"), rs.getString("col6"),
                                   rs.getInt("col7")));
        tupleCount++;
      }
    }
    catch (SQLException ex) {
      logger.debug("exception during reading from table", ex);
    }
    finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
      }
      catch (SQLException ex) {
      }
    }
  }

  public static class MyHashMapOutputOperator extends JDBCNonTransactionOutputOperator<Integer>
  {
    @Override
    public void setup(OperatorContext context)
    {
      super.setup(context);
      createDatabase(getDbName(), getConnection());
      createTable(getTableName(), getConnection());
      initLastWindowInfo(getTableName());
    }

    @Override
    public void beginWindow(long windowId)
    {
      super.beginWindow(windowId);
      logger.debug("beginwindow {}", Codec.getStringWindowId(windowId));
      if (windowId == lastWindowId) {
        try {
          String stmt = "DELETE FROM " + getTableName() + " WHERE winid=" + windowId;
          statement.execute(stmt);
        }
        catch (SQLException ex) {
          logger.debug(ex.toString());
        }
      }
    }

    @Override
    public void endWindow()
    {
      super.endWindow();
      readTable(getTableName(), getConnection());
    }
  }

  @Test
  public void JDBCHashMapOutputOperatorTest() throws Exception
  {
    MyHashMapOutputOperator oper = new MyHashMapOutputOperator();

    oper.setDbUrl("jdbc:mysql://localhost/");
    oper.setDbName("test");
    oper.setDbUser("test");
    oper.setDbPassword("");
    oper.setDbDriver("com.mysql.jdbc.Driver");
    oper.setTableName("Test_Tuple");
    String[] mapping = new String[7];
    mapping[0] = "prop1:col1:INTEGER";
    mapping[1] = "prop2:col2:INTEGER";
    mapping[2] = "prop5:col5:INTEGER";
    mapping[3] = "prop6:col4:INTEGER";
    mapping[4] = "prop7:col7:INTEGER";
    mapping[5] = "prop3:col6:INTEGER";
    mapping[6] = "prop4:col3:INTEGER";

    oper.setOrderedColumnMapping(mapping);


    //oper.setColumnMapping("prop1:col1,prop2:col2,prop5:col5,prop6:col6,prop7:col7,prop3:col3,prop4:col4");
    //columnMapping=prop1:col1,prop2:col2,prop3:col3,prop4:col4,prop5:col5,prop6:col6,prop7:col7
    //columnMapping=prop1:col1,prop2:col2,prop5:col5,prop6:col6,prop7:col7,prop3:col3,prop4:col4
    ///columnMapping=prop1:col1,prop2:col2,prop5:col5,prop6:col4,prop7:col7,prop3:col6,prop4:col3

    oper.setup(new com.malhartech.engine.OperatorContext("irrelevant", null, null));
    oper.beginWindow(1);
    for (int i = 0; i < maxTuple; ++i) {
      HashMap<String, Integer> hm = new HashMap<String, Integer>();
      for (int j = 1; j <= columnCount; ++j) {
        hm.put("prop" + (j), new Integer((columnCount * i) + j));
      }
      oper.inputPort.process(hm);
    }
    oper.endWindow();

    oper.teardown();

    // Check values send vs received
    Assert.assertEquals("Number of emitted tuples", maxTuple, tupleCount);
    logger.debug(String.format("Number of emitted tuples: %d", tupleCount));
  }

  public static class MyArrayListOutputOperator extends JDBCArrayListOutputOperator
  {
    @Override
    public void setup(OperatorContext context)
    {
      super.setup(context);
      createDatabase(getProp().getProperty("dbName"), getConnection());
      createTable(getProp().getProperty("tableName"), getConnection());
    }

    @Override
    public void beginWindow(long windowId)
    {
      super.beginWindow(windowId);
      logger.debug("beginwindow {}", Codec.getStringWindowId(windowId));
    }

    @Override
    public void endWindow()
    {
      super.endWindow();
      //readTable(getProp().getProperty("tableName"), getConnection());
      readTableText(getProp().getProperty("tableName"), getConnection());
    }
  }

  //@Test
  public void JDBCArrayListOutputOperatorTest() throws Exception
  {
    MyArrayListOutputOperator node = new MyArrayListOutputOperator();

    node.setup(new com.malhartech.engine.OperatorContext("irrelevant", null, null));
    node.beginWindow(0);
    for (int i = 0; i < maxTuple; ++i) {
      ArrayList<AbstractMap.SimpleEntry<String, Object>> al = new ArrayList<AbstractMap.SimpleEntry<String, Object>>();
      for (int j = 1; j <= columnCount; ++j) {
        if (j % 2 == 1) {
          al.add(new AbstractMap.SimpleEntry<String, Object>("prop" + j, new Integer(columnCount * i + j)));
        }
        else {
          //al.add(new AbstractMap.SimpleEntry<String, Object>("prop" + j, new Double((columnCount * i + j)/3.0)));
          al.add(new AbstractMap.SimpleEntry<String, Object>("prop" + j, "Test"));
        }
      }

      node.inputPort.process(al);
    }
    node.endWindow();

    node.teardown();

    // Check values send vs received
    Assert.assertEquals("Number of emitted tuples", maxTuple, tupleCount);
    logger.debug(String.format("Number of emitted tuples: %d", tupleCount));
  }
}