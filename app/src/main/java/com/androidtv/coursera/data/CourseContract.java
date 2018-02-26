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
import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * CourseContract represents the contract for storing Courses in the SQLite database.
 */
public final class CourseContract {

    // The name for the entire content provider.
    public static final String CONTENT_AUTHORITY = "com.androidtv.coursera";

    // Base of all URIs that will be used to contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // The content paths.
    public static final String PATH_Course = "Course";

    public static final class CourseEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_Course).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "." + PATH_Course;

        // Name of the Course table.
        public static final String TABLE_NAME = "Course";

        // Column with the foreign key into the category table.
        public static final String COLUMN_CATEGORY = "category";

        // Name of the Course.
        public static final String COLUMN_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;

        // The id to the Course content.
        public static final String COLUMN_COURSE_ID = "courseId";

        // The slug to the Course content.
        public static final String COLUMN_SLUG = "slug";

        // The card image for the Course.
        public static final String COLUMN_CARD_IMG = SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE;

        // Returns the Uri referencing a Course with the specified id.
        public static Uri buildCourseUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
