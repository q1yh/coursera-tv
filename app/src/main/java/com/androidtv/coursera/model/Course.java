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

import android.media.MediaDescription;
import android.os.Parcel;
import android.os.Parcelable;

import com.androidtv.coursera.R;

/**
 * Course is an immutable object that holds the various metadata associated with a single Course.
 */
public final class Course implements Parcelable {
    public final long id;
    public final String category;
    public final String title;
    public final String slug;
    public final String cardImageUrl;
    public final String courseId;

    private Course(
            final long id,
            final String category,
            final String title,
            final String slug,
            final String courseId,
            final String cardImageUrl
            ) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.slug = slug;
        this.courseId = courseId;
        this.cardImageUrl = cardImageUrl;
    }

    protected Course(Parcel in) {
        id = in.readLong();
        category = in.readString();
        title = in.readString();
        cardImageUrl = in.readString();
        courseId = in.readString();
        slug = in.readString();
    }

    public static final Creator<Course> CREATOR = new Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };

    @Override
    public boolean equals(Object m) {
        return m instanceof Course && id == ((Course) m).id;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(category);
        dest.writeString(title);
        dest.writeString(cardImageUrl);
        dest.writeString(courseId);
        dest.writeString(slug);
    }

    @Override
    public String toString() {
        String s = "Course{";
        s += "id=" + id;
        s += ", category='" + category + "'";
        s += ", title='" + title + "'";
        s += ", courseId='" + courseId + "'";
        s += ", slug='" + slug + "'";
        s += ", cardImageUrl='" + cardImageUrl + "'";
        s += "}";
        return s;
    }

    // Builder for Course object.
    public static class CourseBuilder {
        private long id;
        private String category;
        private String title;
        private String cardImageUrl;
        private String courseId;
        private String slug;

        public CourseBuilder id(long id) {
            this.id = id;
            return this;
        }

        public CourseBuilder category(String category) {
            this.category = category;
            return this;
        }

        public CourseBuilder title(String title) {
            this.title = title;
            return this;
        }

        public CourseBuilder courseId(String courseId) {
            this.courseId = courseId;
            return this;
        }

        public CourseBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public CourseBuilder cardImageUrl(String cardImageUrl) {
            this.cardImageUrl = cardImageUrl;
            return this;
        }

        public static Course buildFromExtra(long id,String category,String title, String slug, String courseid, String imageurl) {
            return new Course(
                    id,
                    category,
                    title,
                    slug,
                    courseid,
                    imageurl
            );
        }

        public static Course buildFromItemId(Course mCourse,String subtitle,String itemid) {
            return new Course(
                    mCourse.id,
                    mCourse.category,
                    subtitle,
                    mCourse.slug,
                    mCourse.courseId+"~"+itemid,// Media URI - provided by mCourse page.
                    ""
            );
        }

        public Course build() {
            return new Course(
                    id,
                    category,
                    title,
                    slug,
                    courseId,
                    cardImageUrl
            );
        }
    }
}
