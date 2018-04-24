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


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.CookieSyncManager;

import com.androidtv.coursera.R;
import com.androidtv.coursera.Utils;

import java.net.CookieHandler;
import java.net.CookieManager;

/*
 * MainActivity class that loads MainFragment.
 */
public class MainActivity extends LeanbackActivity {

    //public static Utils mutils;
    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public void onResume() {
        super.onResume();
        if (mSharedPreferences.getString("email",null)==null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            //mutils = new Utils(getApplicationContext());
            //CookieSyncManager.createInstance(this);
            setContentView(R.layout.main);
        }
    }

}
