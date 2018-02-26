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

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.androidtv.coursera.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * FetchCourseService is responsible for fetching the Courses from the Internet and inserting the
 * results into a local SQLite database.
 */
public class FetchCourseService extends IntentService {
    private static final String TAG = "FetchCourseService";

    /**
     * Creates an IntentService with a default name for the worker thread.
     */
    public FetchCourseService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        Context mContext = getApplication();
        CourseDbBuilder builder = new CourseDbBuilder(mContext);
        Utils mUtils = new Utils(mContext,workIntent.getStringExtra("UserId"),workIntent.getStringExtra("Cookies"));

        try {
            JSONObject allCoursesJson = new JSONObject(mUtils.getAllCourses(mContext));
            List<ContentValues> contentValuesList = builder.buildMedia(allCoursesJson);
                    //builder.fetch();
            ContentValues[] downloadedCourseContentValues =
                    contentValuesList.toArray(new ContentValues[contentValuesList.size()]);
            getApplicationContext().getContentResolver().bulkInsert(CourseContract.CourseEntry.CONTENT_URI,
                    downloadedCourseContentValues);
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in downloading Courses");
            e.printStackTrace();
        }
    }
}
