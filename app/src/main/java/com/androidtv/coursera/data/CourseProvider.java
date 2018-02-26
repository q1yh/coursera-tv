/*
 * Copyright (c) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidtv.coursera.data;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.HashMap;

/**
 * CourseProvider is a ContentProvider that provides Courses for the rest of applications.
 */
public class CourseProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private CourseDbHelper mOpenHelper;

    // These codes are returned from sUriMatcher#match when the respective Uri matches.
    private static final int Course = 1;
    private static final int Course_WITH_CATEGORY = 2;
    private static final int SEARCH_SUGGEST = 3;
    private static final int REFRESH_SHORTCUT = 4;

    private static final SQLiteQueryBuilder sCoursesContainingQueryBuilder;
    private static final String[] sCoursesContainingQueryColumns;
    private static final HashMap<String, String> sColumnMap = buildColumnMap();
    private ContentResolver mContentResolver;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mContentResolver = context.getContentResolver();
        mOpenHelper = new CourseDbHelper(context);
        return true;
    }

    static {
        sCoursesContainingQueryBuilder = new SQLiteQueryBuilder();
        sCoursesContainingQueryBuilder.setTables(CourseContract.CourseEntry.TABLE_NAME);
        sCoursesContainingQueryBuilder.setProjectionMap(sColumnMap);
        sCoursesContainingQueryColumns = new String[]{
                CourseContract.CourseEntry._ID,
                CourseContract.CourseEntry.COLUMN_NAME,
                CourseContract.CourseEntry.COLUMN_CATEGORY,
                CourseContract.CourseEntry.COLUMN_COURSE_ID,
                CourseContract.CourseEntry.COLUMN_SLUG,
                CourseContract.CourseEntry.COLUMN_CARD_IMG,
        };
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CourseContract.CONTENT_AUTHORITY;

        // For each type of URI to add, create a corresponding code.
        matcher.addURI(authority, CourseContract.PATH_Course, Course);
        matcher.addURI(authority, CourseContract.PATH_Course + "/*", Course_WITH_CATEGORY);

        // Search related URIs.
        matcher.addURI(authority, "search/" + SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(authority, "search/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        return matcher;
    }

    private Cursor getSuggestions(String query) {
        query = query.toLowerCase();
        return sCoursesContainingQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                sCoursesContainingQueryColumns,
                CourseContract.CourseEntry.COLUMN_NAME + " LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"},
                null,
                null,
                null
        );
    }

    private static HashMap<String, String> buildColumnMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put(CourseContract.CourseEntry._ID, CourseContract.CourseEntry._ID);
        map.put(CourseContract.CourseEntry.COLUMN_NAME, CourseContract.CourseEntry.COLUMN_NAME);
        map.put(CourseContract.CourseEntry.COLUMN_CATEGORY, CourseContract.CourseEntry.COLUMN_CATEGORY);
        map.put(CourseContract.CourseEntry.COLUMN_SLUG, CourseContract.CourseEntry.COLUMN_SLUG);
        map.put(CourseContract.CourseEntry.COLUMN_COURSE_ID, CourseContract.CourseEntry.COLUMN_COURSE_ID);
        map.put(CourseContract.CourseEntry.COLUMN_CARD_IMG, CourseContract.CourseEntry.COLUMN_CARD_IMG);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, CourseContract.CourseEntry._ID + " AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
                CourseContract.CourseEntry._ID + " AS " + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case SEARCH_SUGGEST: {
                String rawQuery = "";
                if (selectionArgs != null && selectionArgs.length > 0) {
                    rawQuery = selectionArgs[0];
                }
                retCursor = getSuggestions(rawQuery);
                break;
            }
            case Course: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CourseContract.CourseEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder//"Course_url desc"//
                );
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        retCursor.setNotificationUri(mContentResolver, uri);
        return retCursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            // The application is querying the db for its own contents.
            case Course_WITH_CATEGORY:
                return CourseContract.CourseEntry.CONTENT_TYPE;
            case Course:
                return CourseContract.CourseEntry.CONTENT_TYPE;

            // The Android TV global search is querying our app for relevant content.
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case REFRESH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;

            // We aren't sure what is being asked of us.
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final Uri returnUri;
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case Course: {
                long _id = mOpenHelper.getWritableDatabase().insert(
                        CourseContract.CourseEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = CourseContract.CourseEntry.buildCourseUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        mContentResolver.notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final int rowsDeleted;

        if (selection == null) {
            throw new UnsupportedOperationException("Cannot delete without selection specified.");
        }

        switch (sUriMatcher.match(uri)) {
            case Course: {
                rowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        CourseContract.CourseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        if (rowsDeleted != 0) {
            mContentResolver.notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        final int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case Course: {
                rowsUpdated = mOpenHelper.getWritableDatabase().update(
                        CourseContract.CourseEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        if (rowsUpdated != 0) {
            mContentResolver.notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        switch (sUriMatcher.match(uri)) {
            case Course: {
                final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                int returnCount = 0;

                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(CourseContract.CourseEntry.TABLE_NAME,
                                null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                mContentResolver.notifyChange(uri, null);
                return returnCount;
            }
            default: {
                return super.bulkInsert(uri, values);
            }
        }
    }
}
