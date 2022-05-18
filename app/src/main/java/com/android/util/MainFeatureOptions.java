/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.util;



import android.content.Context;
import android.util.Log;

import com.ape.util.ApeConfigParser;

public class MainFeatureOptions {
    private static final String TAG="MainFeatureOptions";
    public static final String DEFAULT_PATH = "/etc/apps/Clock/config.xml";

    private static ApeConfigParser mApeConfigParser=null;
    private static MainFeatureOptions mInstance=null;


    private MainFeatureOptions(Context context){
        initConfig(context);

    }

    public static synchronized MainFeatureOptions getInstance(Context context){
        if (null==mInstance){
            mInstance=new MainFeatureOptions(context);
        }
        return mInstance;
    }

    public static void initConfig(Context context){
        if (null==mApeConfigParser){
            mApeConfigParser=new ApeConfigParser(context,DEFAULT_PATH);
        }
    }

    public String getCustomerCountry(){
        String str = mApeConfigParser.getCustomerCountry();
        Log.i(TAG, "getCustomerCountry() str=" + str);
        return str;
    }

//    public static String getCustomerName(){
//        String str = mApeConfigParser.getCustomerName();
//        Log.i(TAG, "getCustomerName() str=" + str);
//        return str;
//    }

    public String getCustomerBrand(){
        String str = mApeConfigParser.getCustomerBrand();
        Log.i(TAG, "getCustomerBrand() str=" + str);
        return str;
    }

    /************************DeskClock config ********************************/
    //auto silence after
    public String getAutoSilenceAfter(){
        String flag=mApeConfigParser.getString("auto_silent_after","10");
        Log.d(TAG,"getAutoSilenceAfter:"+flag);
        return flag;
    }

    //snooze time length
    public String getSnoozeLen(){
        String flag=mApeConfigParser.getString("alarm_snooze_length","10");
        Log.d(TAG,"getSnoozeLen:"+flag);
        return flag;
    }

    //volume button action
    public  int getVolumeButtonAction(){
        int flag=mApeConfigParser.getInteger("volume_button_action",2);
        Log.d(TAG,"getVolumeButtonAction:"+flag);
        return flag;
    }

    //first day of week
    public  int getFirstDayOfWeek(){
        int flag=mApeConfigParser.getInteger("first_day_of_week",1);
        Log.d(TAG,"getFirstDayOfWeek:"+flag);
        return flag;
    }
}
