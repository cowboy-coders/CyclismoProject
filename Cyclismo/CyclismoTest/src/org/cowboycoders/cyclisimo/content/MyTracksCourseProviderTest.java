/*
*    Copyright (c) 2013, Will Szumski
*    Copyright (c) 2013, Doug Szumski
*
*    This file is part of Cyclismo.
*
*    Cyclismo is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    Cyclismo is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with Cyclismo.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * Copyright 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.cowboycoders.cyclisimo.content;


import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import org.cowboycoders.cyclisimo.content.MyTracksProvider.DatabaseHelper;

/**
 * A unit test for {@link MyTracksProvider}.
 * 
 * @author Youtao Liu
 */
public class MyTracksCourseProviderTest extends AndroidTestCase {

  private SQLiteDatabase db;
  private MyTracksCourseProvider myTracksProvider;
  private String DATABASE_NAME = "mytrackstest.db";

  @Override
  protected void setUp() throws Exception {
    getContext().deleteDatabase(DATABASE_NAME);
    db = (new DatabaseHelper(getContext(), DATABASE_NAME)).getWritableDatabase();

    myTracksProvider = new MyTracksCourseProvider();
    super.setUp();
  }

  /**
   * Tests the method {@link MyTracksProvider.DatabaseHelper#onCreate()}.
   */
  public void testDatabaseHelper_OnCreate() {
    assertTrue(checkTable(CourseTrackPointsColumns.TABLE_NAME));
    assertTrue(checkTable(CourseTracksColumns.TABLE_NAME));
    assertTrue(checkTable(CourseWaypointsColumns.TABLE_NAME));
  }

//  /**
//   * Tests the method
//   * {@link MyTracksProvider.DatabaseHelper#onUpgrade(SQLiteDatabase, int, int)}
//   * when version is less than 17.
//   */
//  public void testDatabaseHelper_onUpgrade_Version16() {
//    DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
//    dropTable(CourseTrackPointsColumns.TABLE_NAME);
//    dropTable(CourseTracksColumns.TABLE_NAME);
//    dropTable(CourseWaypointsColumns.TABLE_NAME);
//    databaseHelper.onUpgrade(db, 16, 20);
//    assertTrue(checkTable(CourseTrackPointsColumns.TABLE_NAME));
//    assertTrue(checkTable(CourseTracksColumns.TABLE_NAME));
//    assertTrue(checkTable(CourseWaypointsColumns.TABLE_NAME));
//  }
//
//  /**
//   * Tests the method
//   * {@link MyTracksProvider.DatabaseHelper#onUpgrade(SQLiteDatabase, int, int)}
//   * when version is 17.
//   */
//  public void testDatabaseHelper_onUpgrade_Version17() {
//    DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
//
//    // Make two table is only contains one normal integer column.
//    dropTable(CourseTrackPointsColumns.TABLE_NAME);
//    dropTable(CourseTracksColumns.TABLE_NAME);
//    createEmptyTable(CourseTrackPointsColumns.TABLE_NAME);
//    createEmptyTable(CourseTracksColumns.TABLE_NAME);
//    databaseHelper.onUpgrade(db, 17, 20);
//    assertTrue(isColumnExisted(CourseTrackPointsColumns.TABLE_NAME, CourseTrackPointsColumns.SENSOR));
//    assertTrue(isColumnExisted(CourseTracksColumns.TABLE_NAME, CourseTracksColumns.TABLEID));
//    assertTrue(isColumnExisted(CourseTracksColumns.TABLE_NAME, CourseTracksColumns.ICON));
//  }
//
//  /**
//   * Tests the method
//   * {@link MyTracksProvider.DatabaseHelper#onUpgrade(SQLiteDatabase, int, int)}
//   * when version is 18.
//   */
//  public void testDatabaseHelper_onUpgrade_Version18() {
//    DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
//
//    // Make two table is only contains one normal integer column.
//    dropTable(CourseTrackPointsColumns.TABLE_NAME);
//    dropTable(CourseTracksColumns.TABLE_NAME);
//    createEmptyTable(CourseTrackPointsColumns.TABLE_NAME);
//    createEmptyTable(CourseTracksColumns.TABLE_NAME);
//    databaseHelper.onUpgrade(db, 18, 20);
//    assertFalse(isColumnExisted(CourseTrackPointsColumns.TABLE_NAME, CourseTrackPointsColumns.SENSOR));
//    assertTrue(isColumnExisted(CourseTracksColumns.TABLE_NAME, CourseTracksColumns.TABLEID));
//    assertTrue(isColumnExisted(CourseTracksColumns.TABLE_NAME, CourseTracksColumns.ICON));
//  }
//
//  /**
//   * Tests the method
//   * {@link MyTracksProvider.DatabaseHelper#onUpgrade(SQLiteDatabase, int, int)}
//   * when version is 19.
//   */
//  public void testDatabaseHelper_onUpgrade_Version19() {
//    DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
//
//    // Make two table is only contains one normal integer column.
//    dropTable(CourseTrackPointsColumns.TABLE_NAME);
//    dropTable(CourseTracksColumns.TABLE_NAME);
//    createEmptyTable(CourseTrackPointsColumns.TABLE_NAME);
//    createEmptyTable(CourseTracksColumns.TABLE_NAME);
//    databaseHelper.onUpgrade(db, 19, 20);
//    assertFalse(isColumnExisted(CourseTrackPointsColumns.TABLE_NAME, CourseTrackPointsColumns.SENSOR));
//    assertFalse(isColumnExisted(CourseTracksColumns.TABLE_NAME, CourseTracksColumns.TABLEID));
//    assertTrue(isColumnExisted(CourseTracksColumns.TABLE_NAME, CourseTracksColumns.ICON));
//  }

  /**
   * Tests the method {@link MyTracksProvider#onCreate()}.
   */
  public void testOnCreate() {
    assertTrue(myTracksProvider.onCreate(getContext()));
  }

  /**
   * Tests the method {@link MyTracksProvider#getType(Uri)}.
   */
  public void testGetType() {
    assertEquals(CourseTrackPointsColumns.CONTENT_TYPE,
        myTracksProvider.getType(CourseTrackPointsColumns.CONTENT_URI));
    assertEquals(CourseTracksColumns.CONTENT_TYPE, myTracksProvider.getType(CourseTracksColumns.CONTENT_URI));
    assertEquals(CourseWaypointsColumns.CONTENT_TYPE,
        myTracksProvider.getType(CourseWaypointsColumns.CONTENT_URI));
  }

  /**
   * Creates an table only contains one column.
   * 
   * @param table the name of table
   */
  private void createEmptyTable(String table) {
    db.execSQL("CREATE TABLE " + table + " (test INTEGER)");
  }

  /**
   * Drops a table in database.
   * 
   * @param table
   */
  private void dropTable(String table) {
    db.execSQL("Drop TABLE " + table);
  }

  /**
   * Checks whether a table is existed.
   * 
   * @param table the name of table
   * @return true means the table has existed
   */
  private boolean checkTable(String table) {
    try {
      db.rawQuery("select count(*) from " + table, null);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Checks whether a column in a table is existed by whether can order by the
   * column.
   * 
   * @param table the name of table
   * @param column the name of column
   * @return true means the column has existed
   */
  private boolean isColumnExisted(String table, String column) {
    try {
      db.execSQL("SElECT count(*) from  " + table + " order by  " + column);
    } catch (Exception e) {
      if (e.getMessage().indexOf("no such column") > -1) {
        return false;
      }
    }
    return true;
  }

}