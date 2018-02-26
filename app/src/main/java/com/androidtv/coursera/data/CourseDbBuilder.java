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

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;

import com.androidtv.coursera.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The CourseDbBuilder is used to grab a JSON file from a server and parse the data
 * to be placed into a local database
 */
public class CourseDbBuilder {
    public static final String TAG_MEDIA = "Courses";
    public static final String TAG_All_CourseS = "allCourses";
    public static final String TAG_CATEGORY = "category";
    public static final String TAG_SLUG = "slug";
    public static final String TAG_CARD_THUMB = "card";
    public static final String TAG_TITLE = "title";
    public static final String TAG_COURSEID = "courseId";

    private static final String TAG = "CourseDbBuilder";

    private Context mContext;

    /**
     * Default constructor that can be used for tests
     */
    public CourseDbBuilder() {

    }

    public CourseDbBuilder(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Fetches JSON data representing Courses from a server and populates that in a database
     * The location of the Course list
     */
    public @NonNull List<ContentValues> fetch()
            throws Exception {
        JSONObject CourseData = fetchJSON();
        return buildMedia(CourseData);
    }

    /**
     * Takes the contents of a JSON object and populates the database
     * @param jsonObj The JSON object of Courses
     * @throws JSONException if the JSON object is invalid
     */
    public List<ContentValues> buildMedia(JSONObject jsonObj) throws JSONException {

        JSONArray categoryArray = jsonObj.getJSONArray(TAG_All_CourseS);
        List<ContentValues> CoursesToInsert = new ArrayList<>();

        for (int i = 0; i < categoryArray.length(); i++) {
            JSONArray CourseArray;

            JSONObject category = categoryArray.getJSONObject(i);
            String categoryName = category.getString(TAG_CATEGORY);
            CourseArray = category.getJSONArray(TAG_MEDIA);

            for (int j = 0; j < CourseArray.length(); j++) {
                JSONObject Course = CourseArray.getJSONObject(j);

                String title = Course.optString(TAG_TITLE);
                String courseSlug = Course.optString(TAG_SLUG);
                String courseId = Course.optString(TAG_COURSEID);
                String cardImageUrl = Course.optString(TAG_CARD_THUMB);

                ContentValues CourseValues = new ContentValues();
                CourseValues.put(CourseContract.CourseEntry.COLUMN_CATEGORY, categoryName);
                CourseValues.put(CourseContract.CourseEntry.COLUMN_NAME, title);
                CourseValues.put(CourseContract.CourseEntry.COLUMN_SLUG, courseSlug);
                CourseValues.put(CourseContract.CourseEntry.COLUMN_COURSE_ID, courseId);
                CourseValues.put(CourseContract.CourseEntry.COLUMN_CARD_IMG, cardImageUrl);
                CoursesToInsert.add(CourseValues);
            }
        }
        return CoursesToInsert;
    }

    /**
     * Fetch JSON object from a given URL.
     *
     * @return the JSONObject representation of the response
     * @throws JSONException
     * @throws IOException
     */
    private JSONObject fetchJSON() throws Exception {
        try {
            Utils mUtils = new Utils(mContext);
            String json = mUtils.getAllCourses(mContext);
            return new JSONObject(json);
        } finally {
            //
        }
    }
}
