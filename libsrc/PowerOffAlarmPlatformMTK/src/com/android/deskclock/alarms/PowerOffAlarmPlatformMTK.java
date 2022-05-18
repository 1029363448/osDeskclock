/*
 * Copyright Statement:
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2013. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 */

package com.android.deskclock.alarms;

import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
//import android.os.storage.IMountService;
import android.os.storage.IStorageManager;
import android.os.storage.StorageManager;
import android.util.Log;

@SuppressWarnings("PMD")
public class PowerOffAlarmPlatformMTK {
    private static final String TAG = "AlarmClock";

    // FIXME: 5/9/17, Ji.yang, DESKCLOCK-82, begin
    //public static final UserHandle USER_SYSTEM = UserHandle.SYSTEM;
    public static final UserHandle USER_SYSTEM = new UserHandle(0);
    // FIXME: 5/9/17, Ji.yang, DESKCLOCK-82, end
    public static final int CRYPT_TYPE_DEFAULT = StorageManager.CRYPT_TYPE_DEFAULT;
    
    /**M: @{
     * Whether this boot is from power off alarm or schedule power on or normal boot.
     * @return
     */
    static boolean bootFromPoweroffAlarm() {
        String bootReason = SystemProperties.get("sys.boot.reason");
        boolean ret = (bootReason != null && bootReason.equals("1")) ? true : false;
        return ret;
    }

   /* private static IMountService getMountService() {
        final IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return IMountService.Stub.asInterface(service);
        }
        return null;
    }

    // Determines the type of the encryption password
    public static int getPasswordType() {
        int type = StorageManager.CRYPT_TYPE_PASSWORD;
        IMountService service = getMountService();
        try {
            type = service.getPasswordType();
        } catch (RemoteException e) {
            Log.e(TAG,"Error getPasswordType " + e);
        }
        return type;
    }*/

    private static IStorageManager getMountService() {
        final IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return IStorageManager.Stub.asInterface(service);
        }
        return null;
    }

    // Determines the type of the encryption password
    public static int getPasswordType() {
        int type = StorageManager.CRYPT_TYPE_PASSWORD;
        IStorageManager service = getMountService();
        try {
            type = service.getPasswordType();
        } catch (RemoteException e) {
            Log.e(TAG,"Error getPasswordType " + e);
        }
        return type;
    }

    // Whether the device was Unencrypted.
    static boolean deviceUnencrypted() {
        /// M: [ALPS03410273->ALPS02843690] [Power off Alarm] Improved the check
        /// as per suggestions by SytemProperties owner @{
        Log.i(TAG,"DeviceUnencrypted State = " + SystemProperties.get("ro.crypto.state"));
        Log.i(TAG,"deviceUnencrypted Type = " + SystemProperties.get("ro.crypto.type"));
        if(("encrypted".equals(SystemProperties.get("ro.crypto.state"))
                && "block".equals(SystemProperties.get("ro.crypto.type")) )){
            /// Phone is encrypted, can't enable alarm if password is set
            return false;
        } else {
            /// Phone is unencrypted
            return true;
        }
        /// @}
    }

    /**
     * Only the following two conditions should enable power off alarm:
     * 1.If the device is tablet and the user is tablet owner, enable power off alarm.
     * 2.If property ro.crypto.state is unencrypted or getPasswordType() == CRYPT_TYPE_DEFAULT.
     *
     * Other conditions should disable power off alarm.
     */
    public static boolean canEnablePowerOffAlarm() {
        boolean enabled = (UserHandle.myUserId() == UserHandle.USER_OWNER)
                && (deviceUnencrypted() || StorageManager.CRYPT_TYPE_DEFAULT == getPasswordType());
        Log.e(TAG,"Power Off Alarm enabled: " + enabled);
        return enabled;

    }

    public static void cancelPoweroffAlarm(Context context) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.cancelPoweroffAlarm(context.getPackageName());
    }
}
