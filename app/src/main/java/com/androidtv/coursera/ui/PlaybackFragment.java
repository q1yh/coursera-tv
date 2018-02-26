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

package com.androidtv.coursera.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v17.leanback.app.VideoFragment;
import android.support.v17.leanback.app.VideoFragmentGlueHost;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.util.Log;

import com.androidtv.coursera.R;
import com.androidtv.coursera.model.Playlist;
import com.androidtv.coursera.model.Course;
import com.androidtv.coursera.player.VideoPlayerGlue;
import com.androidtv.coursera.Utils;
import com.androidtv.coursera.presenter.EpisodePresenter;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Iterator;


/**
 * Plays selected Course, loads playlist and related Courses, and delegates playback to {@link
 * VideoPlayerGlue}.
 */
public class PlaybackFragment extends VideoFragment {

    private static final int UPDATE_DELAY = 16;

    private VideoPlayerGlue mPlayerGlue;
    private LeanbackPlayerAdapter mPlayerAdapter;
    private SimpleExoPlayer mPlayer;
    private TrackSelector mTrackSelector;
    private PlaylistActionListener mPlaylistActionListener;
    private Context mContext;
    private Utils mUtils;
    private Course mCourse;
    private Playlist mPlaylist;
    private SparseArrayObjectAdapter mEpisodeActionAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
        mCourse = getActivity().getIntent().getParcelableExtra("Course");
        mUtils = new Utils(mContext);
        mPlaylist = new Playlist();
        //get playlist if any
        new Thread(netGetLectures).start();
        //mUtils.notify();
        //new Thread(netInitialUtils).start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String vu = data.getString("videourl");
            String js = data.getString("jsonstring");
            String uid = data.getString("userid");
            if (uid!=null) {
                new Thread(netGetLectures).start();
            }
            if (js!=null) {
                Log.d("js",js);
                try {
                    JSONArray ja = new JSONObject(js).getJSONArray("re");
                    for (Integer i=0;i<ja.length();i++) {
                        JSONObject jo = ja.getJSONObject(i);
                        mPlaylist.add(Course.CourseBuilder.buildFromExtra(mCourse.id,mCourse.title,jo.optString("name"),jo.optString("module"),jo.optString("courseitemid"),""));
                    }
                    mEpisodeActionAdapter = setupEpisodesCourses();
                    ArrayObjectAdapter mRowsAdapter = initializeEpisodesRow();
                    setAdapter(mRowsAdapter);
                    play(mCourse);
                } catch (Exception e) {
                    Log.e("FetchEpisodeFailed", "Get episodes failed");
                    e.printStackTrace();
                }
            }
            if (vu!=null) {
                Log.d("vu",vu);
                prepareMediaForPlaying(Uri.parse(vu));
                mPlayerGlue.play();
            }
        }
    };

    Runnable netInitialUtils = new Runnable() {
        @Override
        public void run() {
            //String epCourseUrl = getString(R.string.Courseplayback_url_prefix)+"/"+mCourse.CourseUrl.substring(1).replace('/','_')+".json";
            try {
                //String js = Utils.fetchJSONString(epCourseUrl);
                mUtils= new Utils(mContext);
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("userid","ok");
                //Log.d("uid",mUtils.getUserId());
                msg.setData(data);
                handler.sendMessage(msg);
                //Log.d("fetchResult",js.toString());
            } catch (Exception e) {
                Log.e("Initial Utils", "Failed");
                e.printStackTrace();
            }
        }
    };

    Runnable netGetLectures = new Runnable() {
        @Override
        public void run() {
            //String epCourseUrl = getString(R.string.Courseplayback_url_prefix)+"/"+mCourse.CourseUrl.substring(1).replace('/','_')+".json";
            try {
                //String js = Utils.fetchJSONString(epCourseUrl);
                String js = mUtils.getLecturesByCourse(mContext,mCourse);
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("jsonstring",js);
                msg.setData(data);
                handler.sendMessage(msg);
                //Log.d("fetchResult",js.toString());
            } catch (Exception e) {
                Log.e("Fetch Lectures", "Failed");
                e.printStackTrace();
            }
        }
    };

    class netGetVideoUrl implements Runnable{
        Course vCourse;
        netGetVideoUrl(Course c) {vCourse=c;}
        public void run() {
            try {
                if (vCourse.equals(mCourse)) {
                    vCourse = mPlaylist.getFirstCourse();
                }
                JSONObject jsObj = new JSONObject(mUtils.getVideoUrl(mContext,vCourse));
                String vurl = jsObj.optString("re").split(",")[0];
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("videourl",vurl);
                msg.setData(data);
                handler.sendMessage(msg);
                //Log.d("fetchResult",js.toString());
            } catch (Exception e) {
                Log.e("Get Video Url", "Failed");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || mPlayer == null)) {
            initializePlayer();
        }
    }

    /** Pauses the player. */
    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onPause() {
        super.onPause();

        if (mPlayerGlue != null && mPlayerGlue.isPlaying()) {
            mPlayerGlue.pause();
        }
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void initializePlayer() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory CourseTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        mTrackSelector = new DefaultTrackSelector(CourseTrackSelectionFactory);

        mPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), mTrackSelector);
        mPlayerAdapter = new LeanbackPlayerAdapter(getActivity(), mPlayer, UPDATE_DELAY);
        mPlaylistActionListener = new PlaylistActionListener(mPlaylist);
        mPlayerGlue = new VideoPlayerGlue(getActivity(), mPlayerAdapter, mPlaylistActionListener);
        mPlayerGlue.setHost(new VideoFragmentGlueHost(this));
        mPlayerGlue.playWhenPrepared();

        //play(mCourse);

    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            mTrackSelector = null;
            mPlayerGlue = null;
            mPlayerAdapter = null;
            mPlaylistActionListener = null;
        }
    }

    private void play(Course Course) {
        try {
            new Thread(new netGetVideoUrl(Course)).start();
            mPlayerGlue.setTitle(Course.title);
            //prepareMediaForPlaying(Uri.parse(Utils.getCourseUrl(mContext,Course.CourseUrl)));
            //prepareMediaForPlaying(Uri.parse(getResources().getString(R.string.Courseplayback_url_prefix) + Course.CourseUrl));
            //mPlayerGlue.play();
        } catch (Exception e) {
            mPlayerGlue.pause();
        }
    }

    private void prepareMediaForPlaying(Uri mediaSourceUri) {
        String userAgent = Util.getUserAgent(getActivity(), "VideoPlayerGlue");
        MediaSource mediaSource =
                new HlsMediaSource(
                        mediaSourceUri,
                        new DefaultDataSourceFactory(getActivity(), userAgent),
                        //new DefaultExtractorsFactory(),
                        null,
                        null);

        mPlayer.prepare(mediaSource);
    }

    private ArrayObjectAdapter initializeEpisodesRow() {
        /*
         * To add a new row to the mPlayerAdapter and not lose the controls row that is provided by the
         * glue, we need to compose a new row with the controls row and our related Courses row.
         *
         * We start by creating a new {@link ClassPresenterSelector}. Then add the controls row from
         * the media player glue, then add the related Courses row.
         */
        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        presenterSelector.addClassPresenter(
                mPlayerGlue.getControlsRow().getClass(), mPlayerGlue.getPlaybackRowPresenter());
        presenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(presenterSelector);
        rowsAdapter.add(mPlayerGlue.getControlsRow());
        HeaderItem header = new HeaderItem(getString(R.string.episodes));
        ListRow row = new ListRow(header, mEpisodeActionAdapter);
        rowsAdapter.add(row);
        setOnItemViewClickedListener(new ItemViewClickedListener());

        return rowsAdapter;
    }

    private SparseArrayObjectAdapter setupEpisodesCourses() {
        int playlistPositionBackup = mPlaylist.getCurrentPosition();
        SparseArrayObjectAdapter episodeCoursesAdapter = new SparseArrayObjectAdapter(new EpisodePresenter());
        episodeCoursesAdapter.set(0, mPlaylist.getFirstCourse());
        mPlaylist.setCurrentPosition(0);
        for (int i=1;i < (mPlaylist.size());i++) {
            episodeCoursesAdapter.set(i, mPlaylist.next());
        }
        mPlaylist.setCurrentPosition(playlistPositionBackup);
        return episodeCoursesAdapter;
    };

    public void skipToNext() {
        mPlayerGlue.next();
    }

    public void skipToPrevious() {
        mPlayerGlue.previous();
    }

    public void rewind() {
        mPlayerGlue.rewind();
    }

    public void fastForward() {
        mPlayerGlue.fastForward();
    }

    /** Opens the Course details page when a related Course has been clicked. */
    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {

            if (item instanceof Course) {
                Course Course = (Course) item;
                mPlaylist.setCurrentPosition(mPlaylist.getCoursePosition(Course));
                play(Course);
            }
        }
    }

    class PlaylistActionListener implements VideoPlayerGlue.OnActionClickedListener {

        private Playlist mPlaylist;

        PlaylistActionListener(Playlist playlist) {
            this.mPlaylist = playlist;
        }

        @Override
        public void onPrevious() {
            play(mPlaylist.previous());
        }

        @Override
        public void onNext() {
            play(mPlaylist.next());
        }

        public void onReverse() {
            mPlaylist.reverse();
            mEpisodeActionAdapter = setupEpisodesCourses();
            ArrayObjectAdapter mRowsAdapter = initializeEpisodesRow();
            setAdapter(mRowsAdapter);
            //play(mPlaylist.getFirstCourse());
        }
    }
}
