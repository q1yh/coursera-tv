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

package com.androidtv.coursera.model;

import android.database.Cursor;
import android.support.v17.leanback.database.CursorMapper;

import com.androidtv.coursera.data.CourseContract;

/**
 * CourseCursorMapper maps a database Cursor to a Course object.
 */
public final class CourseCursorMapper extends CursorMapper {

    private static int idIndex;
    private static int nameIndex;
    private static int slugIndex;
    private static int courseIdIndex;
    private static int cardImageUrlIndex;
    private static int categoryIndex;

    @Override
    protected void bindColumns(Cursor cursor) {
        idIndex = cursor.getColumnIndex(CourseContract.CourseEntry._ID);
        nameIndex = cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_NAME);
        slugIndex = cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_SLUG);
        courseIdIndex = cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_COURSE_ID);
        cardImageUrlIndex = cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_CARD_IMG);
        categoryIndex = cursor.getColumnIndex(CourseContract.CourseEntry.COLUMN_CATEGORY);
    }

    @Override
    protected Object bind(Cursor cursor) {

        // Get the values of the Course.
        long id = cursor.getLong(idIndex);
        String category = cursor.getString(categoryIndex);
        String title = cursor.getString(nameIndex);
        String slug = cursor.getString(slugIndex);
        String courseId = cursor.getString(courseIdIndex);
        String cardImageUrl = cursor.getString(cardImageUrlIndex);

        // Build a Course object to be processed.
        return new Course.CourseBuilder()
                .id(id)
                .title(title)
                .slug(slug)
                .category(category)
                .courseId(courseId)
                .cardImageUrl(cardImageUrl)
                .build();
    }
}
