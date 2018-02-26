/*
 * Copyright (c) 2014 The Android Open Source Project
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

package com.androidtv.coursera.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.CursorObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.Log;

import com.androidtv.coursera.Utils;
import com.androidtv.coursera.data.FetchCourseService;
import com.androidtv.coursera.data.CourseContract;
import com.androidtv.coursera.model.Course;
import com.androidtv.coursera.model.CourseCursorMapper;
import com.androidtv.coursera.presenter.CardPresenter;

import java.util.HashMap;
import java.util.Map;

import static android.os.SystemClock.sleep;

/*
 * Main class to show BrowseFragment with header and rows of Courses
 */
public class MainFragment extends BrowseFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private ArrayObjectAdapter mCategoryRowAdapter;
    private static final int CATEGORY_LOADER = 123; // Unique ID for Category Loader.
    private static Utils mUtils;
    private static Context mContext;

    // Maps a Loader Id to its CursorObjectAdapter.
    private Map<Integer, CursorObjectAdapter> mCourseCursorAdapters;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Create a list to contain all the CursorObjectAdapters.
        // Each adapter is used to render a specific row of Courses in the MainFragment.
        mCourseCursorAdapters = new HashMap<>();

        // Start loading the categories from the database.
        getLoaderManager().initLoader(CATEGORY_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // Final initialization, modifying UI elements.
        super.onActivityCreated(savedInstanceState);
        mContext=getActivity().getApplication().getApplicationContext();
        mUtils = new Utils(mContext);
        new Thread(netInitialUtils).start();
        setupEventListeners();
        prepareEntranceTransition();

        // Map category results from the database to ListRow objects.
        // This Adapter is used to render the MainFragment sidebar labels.
        mCategoryRowAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mCategoryRowAdapter);

        //updateRecommendations();
    }

    Runnable netInitialUtils = new Runnable() {
        @Override
        public void run() {
            try {
                mUtils.loginAuth(mContext);
            } catch (Exception e) {
                Log.e("Initial Utils", "Failed");
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == CATEGORY_LOADER) {
            return new CursorLoader(
                    getActivity(),   // Parent activity context
                    CourseContract.CourseEntry.CONTENT_URI, // Table to query
                    new String[]{"DISTINCT " + CourseContract.CourseEntry.COLUMN_CATEGORY},
                    // Only categories
                    null, // No selection clause
                    null, // No selection arguments
                    null  // Default sort order
            );
        } else {
            // Assume it is for a Course.
            String category = args.getString(CourseContract.CourseEntry.COLUMN_CATEGORY);

            // This just creates a CursorLoader that gets all Courses.
            return new CursorLoader(
                    getActivity(), // Parent activity context
                    CourseContract.CourseEntry.CONTENT_URI, // Table to query
                    null, // Projection to return - null means return all fields
                    CourseContract.CourseEntry.COLUMN_CATEGORY + " = ?", // Selection clause
                    new String[]{category},  // Select based on the category id.
                    null // Default sort order
            );
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            final int loaderId = loader.getId();

            if (loaderId == CATEGORY_LOADER) {

                // Every time we have to re-get the category loader, we must re-create the sidebar.
                mCategoryRowAdapter.clear();

                // Iterate through each category entry and add it to the ArrayAdapter.
                while (!data.isAfterLast()) {

                    int categoryIndex =
                            data.getColumnIndex(CourseContract.CourseEntry.COLUMN_CATEGORY);
                    String category = data.getString(categoryIndex);

                    // Create header for this category.
                    HeaderItem header = new HeaderItem(category);

                    int CourseLoaderId = category.hashCode(); // Create unique int from category.
                    CursorObjectAdapter existingAdapter = mCourseCursorAdapters.get(CourseLoaderId);
                    if (existingAdapter == null) {

                        // Map Course results from the database to Course objects.
                        CursorObjectAdapter CourseCursorAdapter =
                                new CursorObjectAdapter(new CardPresenter());
                        CourseCursorAdapter.setMapper(new CourseCursorMapper());
                        mCourseCursorAdapters.put(CourseLoaderId, CourseCursorAdapter);

                        ListRow row = new ListRow(header, CourseCursorAdapter);
                        mCategoryRowAdapter.add(row);

                        // Start loading the Courses from the database for a particular category.
                        Bundle args = new Bundle();
                        args.putString(CourseContract.CourseEntry.COLUMN_CATEGORY, category);
                        getLoaderManager().initLoader(CourseLoaderId, args, this);
                    } else {
                        ListRow row = new ListRow(header, existingAdapter);
                        mCategoryRowAdapter.add(row);
                    }

                    data.moveToNext();
                }

                //startEntranceTransition();
                // cursors have loaded.
            } else {
                // The CursorAdapter contains a Cursor pointing to all Courses.
                mCourseCursorAdapters.get(loaderId).changeCursor(data);
            }
        } else {
            // Start an Intent to fetch the Courses.
            Intent serviceIntent = new Intent(getActivity(), FetchCourseService.class);
            try {
                while (mUtils.getUserId()==null) {
                    sleep(1000);
                }
                serviceIntent.putExtra("Cookies", mUtils.getCookieString());
                serviceIntent.putExtra("UserId", mUtils.getUserId());
            } catch (Exception e) {
                //
            }
            getActivity().startService(serviceIntent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int loaderId = loader.getId();
        if (loaderId != CATEGORY_LOADER) {
            mCourseCursorAdapters.get(loaderId).changeCursor(null);
        } else {
            mCategoryRowAdapter.clear();
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Course) {
                Course vCourse = (Course) item;
                Intent intent = new Intent(getActivity(), PlaybackActivity.class);
                intent.putExtra("Course", vCourse);
                while (mUtils.getUserId()==null) {
                    sleep(1000);
                }
                intent.putExtra("Cookies", mUtils.getCookieString());
                intent.putExtra("UserId", mUtils.getUserId());
                getActivity().startActivity(intent);

            }
        }
    }
}
