/*
 * Copyright (c) 2015 The Android Open Source Project
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

package com.androidtv.coursera;

import com.androidtv.coursera.R;
import com.androidtv.coursera.model.Course;

import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.os.SystemClock.sleep;
import static java.lang.Math.min;

/**
 * A collection of utility methods, all static.
 */
public class Utils extends Application {

    public static CookieManager mCookieManager;
    public static String mUserId;
    public static String cookieString;

    /*
     * Making sure public utility methods remain static
     */
    public Utils(Context ctx) {
        mCookieManager=new CookieManager();
        CookieHandler.setDefault(mCookieManager);
        //mUserId=null;
        //cookieString=null;
        loginAuth(ctx);
    }

    public Utils(Context ctx,String userid,String cookiestring) {
        mCookieManager=new CookieManager();
        CookieHandler.setDefault(mCookieManager);
        setUserId(userid);
        setCookieString(cookiestring);
    }

    public static void setUserId(String uid) {
        mUserId=uid;
    }

    public static String getUserId() {
        return mUserId;
    }

    private static String getStringResourceByName(Context ctx,String aString) {
        String pkgName = ctx.getPackageName();
        int resId = ctx.getResources().getIdentifier(aString, "string", pkgName);
        return ctx.getString(resId);
    }

    /**
     * Get Course episode json from a given page relative URL.
     *
     * @return the epijson representation of the response
     * @throws IOException
     */
    public static String getAllCourses(Context ctx) throws Exception {
        List<String> categories = Arrays.asList("CURRENT","INACTIVE","COMPLETED");
        StringBuilder acjson = new StringBuilder("{\"allCourses\":[");
        try {
            for (String catelem:categories) {
                acjson.append(getCourseByCategory(ctx,catelem));
                acjson.append(",");
            }
        } catch (Exception e) {
            Log.e("GetAllCourses", "Failed");
        } finally {
            return acjson.substring(0,acjson.length()-1)+"]}";
        }
    }

    /**
     * Get category Course list json from a given category list name and sort rule.
     *
     * @return the categoryjson representation of the response
     * @throws IOException
     */
    public static String getCourseByCategory(Context ctx, String categoryName) throws Exception {
        //
        StringBuilder rtjson = new StringBuilder("{\"category\":\""+getStringResourceByName(ctx,categoryName)+"\",\"Courses\":[");
        try {
            //loginAuth(ctx);
            String jsonString = getHTMLWithCookies(ctx,ctx.getString(R.string.provider)+ctx.getString(R.string.api_getCoursesByCategory)+categoryName);
            JSONObject jsObj = new JSONObject(jsonString);
            JSONArray jsArr = jsObj.getJSONObject("linked").getJSONArray("courses.v1");
            for (Integer i=0;i<jsArr.length();i++) {
                JSONObject courseObj = jsArr.getJSONObject(i);
                rtjson.append("{\"title\":\"");
                rtjson.append(courseObj.optString("name").replace("\"",""));
                rtjson.append("\",\"slug\":\"");
                rtjson.append(courseObj.optString("slug"));
                rtjson.append("\",\"courseId\":\"");
                rtjson.append(courseObj.optString("id"));
                rtjson.append("\",\"card\":\"");
                rtjson.append(courseObj.optString("photoUrl"));
                rtjson.append("\"},");
            }
        } catch (Exception e) {
            Log.e("GetCourseByCategory", "Failed");
        } finally {
            return rtjson.toString().substring(0,rtjson.length()-1)+"]}";
        }
    }

    /**
     * Get Course video list from a given course info.
     *
     * @return the categoryjson representation of the response
     * @throws IOException
     */
    public static String getLecturesByCourse(Context ctx, Course mCourse) throws Exception {
        //
        StringBuilder rtjson = new StringBuilder("{\"re\":[");
        try {
            //String urlString = ctx.getString(R.string.provider)+ctx.getString(R.string.api_getLectures)+mUserId+"~"+mCourse.slug;
            //Log.d("url",urlString);
            String jsonString = getHTMLWithCookies(ctx,ctx.getString(R.string.provider)+ctx.getString(R.string.api_getLectures)+mUserId+"~"+mCourse.slug);
            //String jsonString = getHTMLWithCookies(ctx,urlString);
            JSONObject jsObj = new JSONObject(jsonString);
            if (jsObj.getJSONArray("elements").length()>0) {
                JSONArray jsArr_weeks = jsObj.getJSONArray("elements").getJSONObject(0).getJSONArray("weeks");
                for (Integer i = 0; i < jsArr_weeks.length(); i++) {
                    JSONObject weekObj = jsArr_weeks.getJSONObject(i);
                    JSONArray jsArr_modules = weekObj.getJSONArray("modules");
                    for (Integer j = 0; j < jsArr_modules.length(); j++) {
                        JSONObject moduleObj = jsArr_modules.getJSONObject(j);
                        JSONArray jsArr_items = moduleObj.getJSONArray("items");
                        for (Integer k = 0; k < jsArr_items.length(); k++) {
                            JSONObject lectureObj = jsArr_items.getJSONObject(k);
                            if (lectureObj.getJSONObject("contentSummary").optString("typeName").equals("lecture")) {
                                rtjson.append("{\"name\":\"");
                                rtjson.append(lectureObj.optString("name").replace("\"", ""));
                                rtjson.append("\",\"module\":\"");
                                rtjson.append(moduleObj.optString("name").replace("\"", ""));
                                rtjson.append("\",\"courseitemid\":\"");
                                rtjson.append(mCourse.courseId);
                                rtjson.append("~");
                                rtjson.append(lectureObj.optString("id"));
                                rtjson.append("\"},");
                            }
                        }
                    }
                }
            } else {
                rtjson.append(",");
            }
        } catch (Exception e) {
            Log.e("GetLecturesByCourse", "Failed");
            rtjson.append(",");
        } finally {
            return rtjson.toString().substring(0,rtjson.length()-1)+"]}";
        }
    }

    /** get video playback url by courseId & itemId **/
    public static String getVideoUrl(Context ctx, Course mCourse) {
        Log.d("getVideoURL",mCourse.courseId);
        StringBuilder rtjson = new StringBuilder("{\"re\":\"");
        try {
            String jsonString = getHTMLWithCookies(ctx,ctx.getString(R.string.provider)+ctx.getString(R.string.api_getVideos_prefix)+mCourse.courseId+"?"+ctx.getString(R.string.api_getVideos_postfix));
            JSONObject jsObj = new JSONObject(jsonString);
            JSONArray jsArr = jsObj.getJSONObject("linked").getJSONArray("onDemandVideos.v1");
            for (Integer i=0;i<jsArr.length();i++) {
                JSONObject videoObj = jsArr.getJSONObject(i);
                rtjson.append(videoObj.getJSONObject("sources").getJSONObject("playlists").optString("hls"));
                rtjson.append("\",\"");
            }
        } catch (Exception e) {
            Log.e("GetVideoUrl", "Failed");
        } finally {
            return rtjson.toString().substring(0,rtjson.length()-2)+"}";
        }
    }

    /**
     * Fetch HTML source code from a given URL.
     *
     * @return the html representation of the response
     * @throws IOException
     */

    private static String fetchHTMLString(Context ctx, String urlString) throws IOException {
        BufferedReader reader = null;
        String ua = ctx.getString(R.string.ua);
        String html = "";
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("User-Agent",ua);
        urlConnection.connect();
        try {
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),
                    "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            html = sb.toString();
            return html;
        } finally {
            urlConnection.disconnect();
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("FetchHTML", "Fetch HTML failed");
                }
            }
        }
    }

    /**
     * return stored cookies.
     *
     * @return the html representation of the response
     */
    public static void setCookieString() throws IOException {
        if (mCookieManager.getCookieStore().getCookies().size()>0) {
            cookieString=TextUtils.join(";",mCookieManager.getCookieStore().getCookies());
            //return cookieString;
        }
    }

    public void setCookieString(String extCookieString) {
        String[] cl = extCookieString.split(";");
        for (String c:cl) {
            String[] ca=c.replace("\"","").split("=");
            mCookieManager.getCookieStore().add(null, new HttpCookie(ca[0],ca[1]));
        }
        cookieString=extCookieString.replace("\"","");
    }

    public static String getCookieString() {
        return cookieString;
    }

    /**
     * return stored cookie value with given cookiename.
     *
     * @return cookie value string
     */
    public static String getCookieValue(String cookieName) throws IOException {
        List<HttpCookie> cookieLst=mCookieManager.getCookieStore().getCookies();
        if (cookieLst.size()>0) {
            for (HttpCookie eCookie:cookieLst) {
                if (eCookie.getName().equals(cookieName)) {
                    return eCookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * process login auth.
     */
    public static void loginAuth(Context ctx) {
        try {
            if (getCookieValue("CAUTH") == null) {
                if (getCookieValue("CSRF3-Token") == null) {
                    getHTMLWithCookies(ctx, ctx.getString(R.string.provider)+"eventing/info.v2");
                }
                postHTMLWithCookies(ctx, ctx.getString(R.string.provider) + ctx.getString(R.string.api_login) + getCookieValue("CSRF3-Token"), "email=" + ctx.getString(R.string.email) + "&password=" + ctx.getString(R.string.password));
            }
            if (mUserId==null) {
                JSONObject jsObj = new JSONObject(getHTMLWithCookies(ctx, ctx.getString(R.string.provider) +ctx.getString(R.string.api_showme)));
                setUserId(jsObj.getJSONArray("elements").getJSONObject(0).optString("userId"));
            }
        } catch (Exception e) {
            //
        } finally {
            //
        }
    }

    /**
     * post HTML from a given URL with stored cookie.
     *
     * @return the html representation of the response
     */
    public static void postHTMLWithCookies(Context ctx, String urlString, String data) throws IOException {
    //public static String postHTMLWithCookies(String urlString, String data) throws IOException {
        Log.d("post",urlString);
        //BufferedReader reader = null;
        //CookieManager mCookieManager = new CookieManager();
        //CookieHandler.setDefault(mCookieManager);
        String ua = ctx.getString(R.string.ua);
        //String ua = "Mozilla/5.0";
        //String html = "";
        URL url = new URL(urlString);
        byte[] postData = data.getBytes(StandardCharsets.UTF_8);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent",ua);
        urlConnection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty( "charset", "utf-8");
        urlConnection.setRequestProperty( "Content-Length", String.valueOf( postData.length ));
        if (cookieString!=null) {
            urlConnection.setRequestProperty("Cookie", getCookieString());
        }
        urlConnection.setUseCaches( false );
        urlConnection.setDoOutput(true);
        urlConnection.getOutputStream().write(postData);
        //urlConnection.connect();

        try {
            //get response cookie and store locally
            Map<String, List<String>> responseHeaderFields = urlConnection.getHeaderFields();
            List<String> cookiesHeader = responseHeaderFields.get("Set-Cookie");
            if (cookiesHeader != null) {
                for (String cookie:cookiesHeader) {
                    mCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }
            setCookieString();
            /*
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),
                    "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            html = sb.toString();
            return html;
            */
        } catch (Exception e) {
            Log.e("Post","Failed");
        } finally {
            urlConnection.disconnect();
            /*
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("getHTMLWithCookies", "Failed");
                }
            }*/
        }
    }

    /**
     * get HTML from a given URL with stored cookie.
     *
     * @return the html representation of the response
     */
    public static String getHTMLWithCookies(Context ctx, String urlString) throws IOException {
    //public static String getHTMLWithCookies(String urlString) throws IOException {
        BufferedReader reader = null;
        //CookieManager mCookieManager = new CookieManager();
        //CookieHandler.setDefault(mCookieManager);
        String ua = ctx.getString(R.string.ua);
        //String ua = "Mozilla/5.0";
        String html = "";
        //Log.d("url",urlString);
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        //URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("User-Agent",ua);
        if (cookieString!=null) {
            urlConnection.setRequestProperty("Cookie", cookieString);
        }
        urlConnection.connect();

        try {
            //get response cookie and store locally
            Map<String, List<String>> responseHeaderFields = urlConnection.getHeaderFields();
            List<String> cookiesHeader = responseHeaderFields.get("Set-Cookie");
            if (cookiesHeader != null) {
                for (String cookie:cookiesHeader) {
                    mCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    //Log.d("cookie",cookie);
                }
            }
            setCookieString();
            //get response body string
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),
                    "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            html = sb.toString();
            return html;
        } catch (Exception e) {
            Log.e("Get","Failed"+e.toString());
            return null;
        } finally {
            urlConnection.disconnect();
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("getHTMLWithCookies", "Failed");
                }
            }
        }
    }

}
