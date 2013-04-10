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
 * Copyright 2008 Google Inc.
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

import com.google.common.annotations.VisibleForTesting;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import org.cowboycoders.cyclisimo.R;
import org.cowboycoders.cyclisimo.util.PreferencesUtils;

/**
 * A {@link ContentProvider} that handles access to track points, tracks, and
 * waypoints tables.
 * 
 * @author Leif Hendrik Wilden
 */
public class MyTracksProvider extends ContentProvider {

  private static final String TAG = MyTracksProvider.class.getSimpleName();
  @VisibleForTesting
  static final String DATABASE_NAME = "cyclismo.db";
  private static final int DATABASE_VERSION = 24;

  /**
   * Database helper for creating and upgrading the database.
   */
  @VisibleForTesting
  static class DatabaseHelper extends SQLiteOpenHelper {
  
    public DatabaseHelper(Context context) {
      this(context, DATABASE_NAME);
    }
    
    @VisibleForTesting
    public DatabaseHelper(Context context, String databaseName) {
      super(context, databaseName, null, DATABASE_VERSION);
    }
  
    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(UserInfoColumns.CREATE_TABLE);
      db.execSQL(TrackPointsColumns.CREATE_TABLE);
      db.execSQL(TracksColumns.CREATE_TABLE);
      db.execSQL(WaypointsColumns.CREATE_TABLE);
      db.execSQL(BikeInfoColumns.CREATE_TABLE);
    }
  
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
      if (oldVersion <= 23) {
        Log.w(TAG, "Deleting all old data.");
        db.execSQL("DROP TABLE IF EXISTS " + TrackPointsColumns.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TracksColumns.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WaypointsColumns.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserInfoColumns.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BikeInfoColumns.TABLE_NAME);
        onCreate(db);
      } else {
        // Incremental upgrades. One if statement per DB version.
  
//        // Add track points SENSOR column
//        if (oldVersion <= 17) {
//          Log.w(TAG, "Upgrade DB: Adding sensor column.");
//          db.execSQL("ALTER TABLE " + TrackPointsColumns.TABLE_NAME + " ADD "
//              + TrackPointsColumns.SENSOR + " BLOB");
//        }
//        // Add tracks TABLEID column
//        if (oldVersion <= 18) {
//          Log.w(TAG, "Upgrade DB: Adding tableid column.");
//          db.execSQL("ALTER TABLE " + TracksColumns.TABLE_NAME + " ADD " + TracksColumns.TABLEID
//              + " STRING");
//        }
//        // Add tracks ICON column
//        if (oldVersion <= 19) {
//          Log.w(TAG, "Upgrade DB: Adding icon column.");
//          db.execSQL(
//              "ALTER TABLE " + TracksColumns.TABLE_NAME + " ADD " + TracksColumns.ICON + " STRING");
//        }
        onCreate(db);
      }
    }
  }

  /**
   * Types of url.
   * 
   * @author Jimmy Shih
   */
  @VisibleForTesting
  enum UrlType {
    USER, USER_ID, TRACKPOINTS, TRACKPOINTS_ID, TRACKS, TRACKS_ID, WAYPOINTS, WAYPOINTS_ID, BIKE, BIKE_ID
  }

  private final UriMatcher uriMatcher;
  private SQLiteDatabase db;

  public MyTracksProvider() {
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(MyTracksProviderUtils.AUTHORITY, UserInfoColumns.TABLE_NAME,
        UrlType.USER.ordinal());
    uriMatcher.addURI(MyTracksProviderUtils.AUTHORITY, UserInfoColumns.TABLE_NAME + "/#",
        UrlType.USER_ID.ordinal());
    uriMatcher.addURI(MyTracksProviderUtils.AUTHORITY, TrackPointsColumns.TABLE_NAME,
        UrlType.TRACKPOINTS.ordinal());
    uriMatcher.addURI(MyTracksProviderUtils.AUTHORITY, TrackPointsColumns.TABLE_NAME + "/#",
        UrlType.TRACKPOINTS_ID.ordinal());
    uriMatcher.addURI(
        MyTracksProviderUtils.AUTHORITY, TracksColumns.TABLE_NAME, UrlType.TRACKS.ordinal());
    uriMatcher.addURI(MyTracksProviderUtils.AUTHORITY, TracksColumns.TABLE_NAME + "/#",
        UrlType.TRACKS_ID.ordinal());
    uriMatcher.addURI(
        MyTracksProviderUtils.AUTHORITY, WaypointsColumns.TABLE_NAME, UrlType.WAYPOINTS.ordinal());
    uriMatcher.addURI(MyTracksProviderUtils.AUTHORITY, WaypointsColumns.TABLE_NAME + "/#",
        UrlType.WAYPOINTS_ID.ordinal());
    // bike
    uriMatcher.addURI(
        MyTracksProviderUtils.AUTHORITY, BikeInfoColumns.TABLE_NAME, UrlType.BIKE.ordinal());
    uriMatcher.addURI(MyTracksProviderUtils.AUTHORITY, BikeInfoColumns.TABLE_NAME + "/#",
        UrlType.BIKE_ID.ordinal());
  }

  @Override
  public boolean onCreate() {
    return onCreate(getContext());
  }
  
  /**
   * Helper method to make onCreate is testable.
   * @param context context to creates database
   * @return true means run successfully
   */
  @VisibleForTesting
  boolean onCreate(Context context) {
    if (!canAccess()) {
      return false;
    }
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    try {
      db = databaseHelper.getWritableDatabase();
    } catch (SQLiteException e) {
      Log.e(TAG, "Unable to open database for writing.", e);
    }
    return db != null;
  }

  @Override
  public int delete(Uri url, String where, String[] selectionArgs) {
    if (!canAccess()) {
      return 0;
    }
    String table;
    boolean shouldVacuum = false;
    switch (getUrlType(url)) {
      case TRACKPOINTS:
        table = TrackPointsColumns.TABLE_NAME;
        break;
      case TRACKS:
        table = TracksColumns.TABLE_NAME;
        shouldVacuum = true;
        break;
      case WAYPOINTS:
        table = WaypointsColumns.TABLE_NAME;
        break;
      case USER:
        table = UserInfoColumns.TABLE_NAME;
        break;
      case BIKE:
        table = BikeInfoColumns.TABLE_NAME;
        break;
      default:
        throw new IllegalArgumentException("Unknown URL " + url);
    }
  
    Log.w(MyTracksProvider.TAG, "Deleting table " + table);
    int count;
    try {
      db.beginTransaction();
      count = db.delete(table, where, selectionArgs);
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
    getContext().getContentResolver().notifyChange(url, null, true);
  
    if (shouldVacuum) {
      // If a potentially large amount of data was deleted, reclaim its space.
      Log.i(TAG, "Vacuuming the database.");
      db.execSQL("VACUUM");
    }
    return count;
  }

  @Override
  public String getType(Uri url) {
    if (!canAccess()) {
      return null;
    }
    switch (getUrlType(url)) {
      case TRACKPOINTS:
        return TrackPointsColumns.CONTENT_TYPE;
      case TRACKPOINTS_ID:
        return TrackPointsColumns.CONTENT_ITEMTYPE;
      case TRACKS:
        return TracksColumns.CONTENT_TYPE;
      case TRACKS_ID:
        return TracksColumns.CONTENT_ITEMTYPE;
      case WAYPOINTS:
        return WaypointsColumns.CONTENT_TYPE;
      case WAYPOINTS_ID:
        return WaypointsColumns.CONTENT_ITEMTYPE;
      case USER:
         return UserInfoColumns.CONTENT_TYPE;
      case USER_ID:
         return UserInfoColumns.CONTENT_ITEMTYPE;
      case BIKE:
         return BikeInfoColumns.CONTENT_TYPE;
      case BIKE_ID:
          return BikeInfoColumns.CONTENT_ITEMTYPE;
      default:
        throw new IllegalArgumentException("Unknown URL " + url);
    }
  }

  @Override
  public Uri insert(Uri url, ContentValues initialValues) {
    if (!canAccess()) {
      return null;
    }
    if (initialValues == null) {
      initialValues = new ContentValues();
    }
    Uri result = null;
    try {
      db.beginTransaction();
      result = insertContentValues(url, getUrlType(url), initialValues);
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
    return result;
  }

  @Override
  public int bulkInsert(Uri url, ContentValues[] valuesBulk) {
    if (!canAccess()) {
      return 0;
    }
    int numInserted = 0;
    try {
      // Use a transaction in order to make the insertions run as a single batch
      db.beginTransaction();
  
      UrlType urlType = getUrlType(url);
      for (numInserted = 0; numInserted < valuesBulk.length; numInserted++) {
        ContentValues contentValues = valuesBulk[numInserted];
        if (contentValues == null) {
          contentValues = new ContentValues();
        }
        insertContentValues(url, urlType, contentValues);
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
    return numInserted;
  }

  @Override
  public Cursor query(
      Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
    if (!canAccess()) {
      return null;
    }
    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    String sortOrder = null;
    switch (getUrlType(url)) {
      case TRACKPOINTS:
        queryBuilder.setTables(TrackPointsColumns.TABLE_NAME);
        sortOrder = sort != null ? sort : TrackPointsColumns.DEFAULT_SORT_ORDER;
        break;
      case TRACKPOINTS_ID:
        queryBuilder.setTables(TrackPointsColumns.TABLE_NAME);
        queryBuilder.appendWhere("_id=" + url.getPathSegments().get(1));
        break;
      case TRACKS:
        queryBuilder.setTables(TracksColumns.TABLE_NAME);
        sortOrder = sort != null ? sort : TracksColumns.DEFAULT_SORT_ORDER;
        break;
      case TRACKS_ID:
        queryBuilder.setTables(TracksColumns.TABLE_NAME);
        queryBuilder.appendWhere("_id=" + url.getPathSegments().get(1));
        break;
      case WAYPOINTS:
        queryBuilder.setTables(WaypointsColumns.TABLE_NAME);
        sortOrder = sort != null ? sort : WaypointsColumns.DEFAULT_SORT_ORDER;
        break;
      case WAYPOINTS_ID:
        queryBuilder.setTables(WaypointsColumns.TABLE_NAME);
        queryBuilder.appendWhere("_id=" + url.getPathSegments().get(1));
        break;
      case USER:
        queryBuilder.setTables(UserInfoColumns.TABLE_NAME);
        sortOrder = sort != null ? sort : UserInfoColumns.DEFAULT_SORT_ORDER;
        break;
      case USER_ID:
        queryBuilder.setTables(UserInfoColumns.TABLE_NAME);
        queryBuilder.appendWhere("_id=" + url.getPathSegments().get(1));
        break;
      case BIKE:
        queryBuilder.setTables(BikeInfoColumns.TABLE_NAME);
        sortOrder = sort != null ? sort : BikeInfoColumns.DEFAULT_SORT_ORDER;
        break;
      case BIKE_ID:
        queryBuilder.setTables(BikeInfoColumns.TABLE_NAME);
        queryBuilder.appendWhere("_id=" + url.getPathSegments().get(1));
        break;
      default:
        throw new IllegalArgumentException("Unknown url " + url);
    }
    Cursor cursor = queryBuilder.query(
        db, projection, selection, selectionArgs, null, null, sortOrder);
    cursor.setNotificationUri(getContext().getContentResolver(), url);
    return cursor;
  }

  @Override
  public int update(Uri url, ContentValues values, String where, String[] selectionArgs) {
    if (!canAccess()) {
      return 0;
    }
    String table;
    String whereClause;
    switch (getUrlType(url)) {
      case TRACKPOINTS:
        table = TrackPointsColumns.TABLE_NAME;
        whereClause = where;
        break;
      case TRACKPOINTS_ID:
        table = TrackPointsColumns.TABLE_NAME;
        whereClause = TrackPointsColumns._ID + "=" + url.getPathSegments().get(1);
        if (!TextUtils.isEmpty(where)) {
          whereClause += " AND (" + where + ")";
        }
        break;
      case TRACKS:
        table = TracksColumns.TABLE_NAME;
        whereClause = where;
        break;
      case TRACKS_ID:
        table = TracksColumns.TABLE_NAME;
        whereClause = TracksColumns._ID + "=" + url.getPathSegments().get(1);
        if (!TextUtils.isEmpty(where)) {
          whereClause += " AND (" + where + ")";
        }
        break;
      case WAYPOINTS:
        table = WaypointsColumns.TABLE_NAME;
        whereClause = where;
        break;
      case WAYPOINTS_ID:
        table = WaypointsColumns.TABLE_NAME;
        whereClause = WaypointsColumns._ID + "=" + url.getPathSegments().get(1);
        if (!TextUtils.isEmpty(where)) {
          whereClause += " AND (" + where + ")";
        }
        break;
      case USER:
        table = UserInfoColumns.TABLE_NAME;
        whereClause = where;
        break;
      case USER_ID:
        table = UserInfoColumns.TABLE_NAME;
        whereClause = UserInfoColumns._ID + "=" + url.getPathSegments().get(1);
        if (!TextUtils.isEmpty(where)) {
          whereClause += " AND (" + where + ")";
        }
        break;
      case BIKE:
        table = BikeInfoColumns.TABLE_NAME;
        whereClause = where;
        break;
      case BIKE_ID:
        table = BikeInfoColumns.TABLE_NAME;
        whereClause = BikeInfoColumns._ID + "=" + url.getPathSegments().get(1);
        if (!TextUtils.isEmpty(where)) {
          whereClause += " AND (" + where + ")";
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown url " + url);
    }
    int count;
    try {
      db.beginTransaction();
      count = db.update(table, values, whereClause, selectionArgs);
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
    getContext().getContentResolver().notifyChange(url, null, true);
    return count;
  }

  /**
   * Returns true if the caller can access the content provider.
   */
  private boolean canAccess() {
    if (Binder.getCallingPid() == Process.myPid()) {
      return true;
    } else {
      return PreferencesUtils.getBoolean(
          getContext(), R.string.allow_access_key, PreferencesUtils.ALLOW_ACCESS_DEFAULT);
    }
  }

  /**
   * Gets the {@link UrlType} for a url.
   * 
   * @param url the url
   */
  private UrlType getUrlType(Uri url) {
    return UrlType.values()[uriMatcher.match(url)];
  }

  /**
   * Inserts a content based on the url type.
   * 
   * @param url the content url
   * @param urlType the url type
   * @param contentValues the content values
   */
  private Uri insertContentValues(Uri url, UrlType urlType, ContentValues contentValues) {
    switch (urlType) {
      case TRACKPOINTS:
        return insertTrackPoint(url, contentValues);
      case TRACKS:
        return insertTrack(url, contentValues);
      case WAYPOINTS:
        return insertWaypoint(url, contentValues);
      case USER:
        return insertUser(url,contentValues);
      case BIKE:
        return insertBike(url,contentValues);
      default:
        throw new IllegalArgumentException("Unknown url " + url);
    }
  }
  
  /**
   * Inserts a bike.
   * 
   * @param url the content url
   * @param values the content values
   */
  private Uri insertBike(Uri url, ContentValues values) {
    boolean hasName = values.containsKey(BikeInfoColumns.NAME);
    if (!hasName) {
      throw new IllegalArgumentException("Bike must have name");
    }
    long rowId = db.insert(BikeInfoColumns.TABLE_NAME, BikeInfoColumns._ID, values);
    if (rowId >= 0) {
      Uri uri = ContentUris.appendId(BikeInfoColumns.CONTENT_URI.buildUpon(), rowId).build();
      getContext().getContentResolver().notifyChange(url, null, true);
      return uri;
    }
    throw new SQLiteException("Failed to insert a bike " + url);
  }
  
  /**
   * Inserts a User.
   * 
   * @param url the content url
   * @param values the content values
   */
  private Uri insertUser(Uri url, ContentValues values) {
    boolean hasName = values.containsKey(UserInfoColumns.NAME);
    if (!hasName) {
      throw new IllegalArgumentException("User must have name");
    }
    long rowId = db.insert(UserInfoColumns.TABLE_NAME, UserInfoColumns._ID, values);
    if (rowId >= 0) {
      Uri uri = ContentUris.appendId(UserInfoColumns.CONTENT_URI.buildUpon(), rowId).build();
      getContext().getContentResolver().notifyChange(url, null, true);
      return uri;
    }
    throw new SQLiteException("Failed to insert a user " + url);
  }

  /**
   * Inserts a track point.
   * 
   * @param url the content url
   * @param values the content values
   */
  private Uri insertTrackPoint(Uri url, ContentValues values) {
    boolean hasLatitude = values.containsKey(TrackPointsColumns.LATITUDE);
    boolean hasLongitude = values.containsKey(TrackPointsColumns.LONGITUDE);
    boolean hasTime = values.containsKey(TrackPointsColumns.TIME);
    if (!hasLatitude || !hasLongitude || !hasTime) {
      throw new IllegalArgumentException("Latitude, longitude, and time values are required.");
    }
    long rowId = db.insert(TrackPointsColumns.TABLE_NAME, TrackPointsColumns._ID, values);
    if (rowId >= 0) {
      Uri uri = ContentUris.appendId(TrackPointsColumns.CONTENT_URI.buildUpon(), rowId).build();
      getContext().getContentResolver().notifyChange(url, null, true);
      return uri;
    }
    throw new SQLiteException("Failed to insert a track point " + url);
  }

  /**
   * Inserts a track.
   * 
   * @param url the content url
   * @param contentValues the content values
   */
  private Uri insertTrack(Uri url, ContentValues contentValues) {
    boolean hasStartTime = contentValues.containsKey(TracksColumns.STARTTIME);
    boolean hasStartId = contentValues.containsKey(TracksColumns.STARTID);
    if (!hasStartTime || !hasStartId) {
      throw new IllegalArgumentException("Both start time and start id values are required.");
    }
    long rowId = db.insert(TracksColumns.TABLE_NAME, TracksColumns._ID, contentValues);
    if (rowId >= 0) {
      Uri uri = ContentUris.appendId(TracksColumns.CONTENT_URI.buildUpon(), rowId).build();
      getContext().getContentResolver().notifyChange(url, null, true);
      return uri;
    }
    throw new SQLException("Failed to insert a track " + url);
  }

  /**
   * Inserts a waypoint.
   * 
   * @param url the content url
   * @param contentValues the content values
   */
  private Uri insertWaypoint(Uri url, ContentValues contentValues) {
    long rowId = db.insert(WaypointsColumns.TABLE_NAME, WaypointsColumns._ID, contentValues);
    if (rowId >= 0) {
      Uri uri = ContentUris.appendId(WaypointsColumns.CONTENT_URI.buildUpon(), rowId).build();
      getContext().getContentResolver().notifyChange(url, null, true);
      return uri;
    }
    throw new SQLException("Failed to insert a waypoint " + url);
  }
}