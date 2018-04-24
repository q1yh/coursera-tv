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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidtv.coursera.R;
import com.androidtv.coursera.Utils;

/*
 * LoginActivity class.
 */
public class LoginActivity extends Activity {

    //public static Utils mutils;
    private SharedPreferences mSharedPreferences;
    private Utils mUtils;
    private EditText etEmail,etPass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUtils = new Utils(getApplicationContext());
        setContentView(R.layout.activity_login);
        Button login_button = (Button) findViewById(R.id.button_login);
        etEmail = (EditText)findViewById(R.id.editText_username);
        etPass = (EditText)findViewById(R.id.editText_password);
        login_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSharedPreferences.edit().putString("email",etEmail.getText().toString()).apply();
                mSharedPreferences.edit().putString("password",etPass.getText().toString()).apply();
                new Thread(netLoginCoursera).start();
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String loginrt = data.getString("login");
            if (loginrt.equals("0")) {
                etEmail.setText("");
                etPass.setText("");
                mSharedPreferences.edit().remove("email").apply();
                Toast.makeText(getApplicationContext(),"Login failed, please check your username and password.", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("loginUserId",loginrt);
                finish();
            }
        }
    };

    Runnable netLoginCoursera = new Runnable() {
        @Override
        public void run() {
            try {
                String loginrt = mUtils.loginAuth(getApplicationContext());
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("login",loginrt);
                msg.setData(data);
                handler.sendMessage(msg);
            } catch (Exception e) {
                Log.e("Login coursera", "Failed");
            }
        }
    };


}
