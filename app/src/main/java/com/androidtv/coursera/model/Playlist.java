/*
 * Copyright (C) 2017 The Android Open Source Project
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages a playlist of Courses.
 */
public class Playlist {

    private List<Course> playlist;
    private int currentPosition;

    public Playlist() {
        playlist = new ArrayList<>();
        currentPosition = 0;
    }

    /**
     * Clears the Courses from the playlist.
     */
    public void clear() {
        playlist.clear();
    }

    /**
     * Adds a Course to the end of the playlist.
     *
     * @param Course to be added to the playlist.
     */
    public void add(Course Course) {
        playlist.add(Course);
    }

    /**
     * Sets current position in the playlist.
     *
     * @param currentPosition
     */
    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    /**
     * Returns the size of the playlist.
     *
     * @return The size of the playlist.
     */
    public int size() {
        return playlist.size();
    }

    /**
     * Moves to the next Course in the playlist. If already at the end of the playlist, null will
     * be returned and the position will not change.
     *
     * @return The next Course in the playlist.
     */
    public Course next() {
        if ((currentPosition + 1) < size()) {
            currentPosition++;
            return playlist.get(currentPosition);
        }
        return null;
    }

    /**
     * Moves to the previous Course in the playlist. If the playlist is already at the beginning,
     * null will be returned and the position will not change.
     *
     * @return The previous Course in the playlist.
     */
    public Course previous() {
        if (currentPosition - 1 >= 0) {
            currentPosition--;
            return playlist.get(currentPosition);
        }
        return null;
    }

    /**
     * Reverse order of the playlist.
     */
    public void reverse() {
        Collections.reverse(playlist);
        currentPosition = size()-currentPosition-1;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public Course getFirstCourse() {return playlist.get(0);}

    public Course getCurrentCourse() {
        return playlist.get(currentPosition);
    }

    public int getCoursePosition(Course Course){
        for (int i=0;i<playlist.size();i++) {
            if (playlist.get(i).courseId==Course.courseId) {
                return i;
            }
        }
        return 0;
    }
}