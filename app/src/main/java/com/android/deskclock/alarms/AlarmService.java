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
package com.android.deskclock.alarms;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.android.deskclock.AlarmAlertWakeLock;
import com.android.deskclock.LogUtils;
import com.android.deskclock.R;
import com.android.deskclock.events.Events;
import com.android.deskclock.provider.AlarmInstance;
import com.android.util.WrapperManager;

/**
 * This service is in charge of starting/stopping the alarm. It will bring up and manage the
 * {@link AlarmActivity} as well as {@link AlarmKlaxon}.
 *
 * Registers a broadcast receiver to listen for snooze/dismiss intents. The broadcast receiver
 * exits early if AlarmActivity is bound to prevent double-processing of the snooze/dismiss intents.
 */
public class AlarmService extends Service {
    /**
     * AlarmActivity and AlarmService (when unbound) listen for this broadcast intent
     * so that other applications can snooze the alarm (after ALARM_ALERT_ACTION and before
     * ALARM_DONE_ACTION).
     */
    public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";

    /**
     * AlarmActivity and AlarmService listen for this broadcast intent so that other
     * applications can dismiss the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
     */
    public static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";

    /** A public action sent by AlarmService when the alarm has started. */
    public static final String ALARM_ALERT_ACTION = "com.android.deskclock.ALARM_ALERT";

    /** A public action sent by AlarmService when the alarm has stopped for any reason. */
    public static final String ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE";

    /** Private action used to stop an alarm with this service. */
    public static final String STOP_ALARM_ACTION = "STOP_ALARM";

    /// M: [ALPS03269543] [Power off Alarm] Stop the alarm alert when the device shut down.
    public static final String PRE_SHUTDOWN_ACTION = "android.intent.action.ACTION_PRE_SHUTDOWN";

    /// M: [ALPS03269543] [Power off Alarm] Stop alarm alert when privacy protection lock enable
    public static final String PRIVACY_PROTECTION_CLOCK = "com.mediatek.ppl.NOTIFY_LOCK";

    /// M: [ALPS03269543] [Power off Alarm] start and stop deskclock play ringtone. @{
    private static final String NORMAL_SHUTDOWN_ACTION = "android.intent.action.normal.shutdown";
    private static final String ALARM_REQUEST_SHUTDOWN_ACTION =
      "android.intent.action.ACTION_ALARM_REQUEST_SHUTDOWN";

    private static final String POWER_OFF_ALARM_START_ACITION = "com.android.deskclock.START_ALARM";
    private static final String POWER_OFF_ALARM_POWER_ON_ACITION =
      "com.android.deskclock.POWER_ON_ALARM";
    private static final String POWER_OFF_ALARM_DISMISS_ACITION =
      "com.android.deskclock.DISMISS_ALARM";
    public static final String POWER_OFF_ALARM_SNOOZE_ACITION =
      "com.android.deskclock.SNOOZE_ALARM";
    private static boolean  mStopPlayReceiverRegistered = false;


    /** Binder given to AlarmActivity. */
    private final IBinder mBinder = new Binder();

    /** Whether the service is currently bound to AlarmActivity */
    private boolean mIsBound = false;

    /** Listener for changes in phone state. */
    private final PhoneStateChangeListener mPhoneStateListener = new PhoneStateChangeListener();

    /** Whether the receiver is currently registered */
    private boolean mIsRegistered = false;

    @Override
    public IBinder onBind(Intent intent) {
        mIsBound = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mIsBound = false;
        return super.onUnbind(intent);
    }

    /**
     * Utility method to help stop an alarm properly. Nothing will happen, if alarm is not firing
     * or using a different instance.
     *
     * @param context application context
     * @param instance you are trying to stop
     */
    public static void stopAlarm(Context context, AlarmInstance instance) {
        final Intent intent = AlarmInstance.createIntent(context, AlarmService.class, instance.mId)
                .setAction(STOP_ALARM_ACTION);

        // We don't need a wake lock here, since we are trying to kill an alarm
        context.startService(intent);
    }

    private TelephonyManager mTelephonyManager;

    /// M: [ALPS03269543] [Power off Alarm]
    private AlarmInstance mCurrentAlarm = null;
    private Context mContext = null;
    /// @}

    private AlarmInstance mInstance = null;


    private void startAlarm(AlarmInstance instance) {
        LogUtils.v("AlarmService.start with instance: " + instance.mId);
        if (mCurrentAlarm != null) {
            AlarmStateManager.setMissedState(this, mCurrentAlarm);
            stopCurrentAlarm();
        }

        AlarmAlertWakeLock.acquireCpuWakeLock(this);

        mCurrentAlarm = instance;

        /// M: [ALPS03269543] [Power off Alarm]  Power off check
        if(WrapperManager.isMtkPlatform()) {
            if (!PowerOffAlarm.bootFromPoweroffAlarm()) {
                /// @}
                AlarmNotifications.showAlarmNotification(this, mCurrentAlarm);
            }
        }

        if (WrapperManager.isQualcommPlatform()) {
            AlarmNotifications.showAlarmNotification(this, mCurrentAlarm);
        }

        mTelephonyManager.listen(mPhoneStateListener.init(), PhoneStateListener.LISTEN_CALL_STATE);
        AlarmKlaxon.start(this, mCurrentAlarm);
        sendBroadcast(new Intent(ALARM_ALERT_ACTION));
    }

    private void stopCurrentAlarm() {
        if (mCurrentAlarm == null) {
            LogUtils.v("There is no current alarm to stop");
            return;
        }

        final long instanceId = mCurrentAlarm.mId;
        LogUtils.v("AlarmService.stop with instance: %s", instanceId);

        AlarmKlaxon.stop(this);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        sendBroadcast(new Intent(ALARM_DONE_ACTION));

        stopForeground(true /* removeNotification */);

        mCurrentAlarm = null;
        AlarmAlertWakeLock.releaseCpuLock();
    }

    private final BroadcastReceiver mActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            LogUtils.i("AlarmService received intent %s", action);
            if (mCurrentAlarm == null || mCurrentAlarm.mAlarmState != AlarmInstance.FIRED_STATE) {
                LogUtils.i("No valid firing alarm");
                return;
            }

            if (mIsBound) {
                LogUtils.i("AlarmActivity bound; AlarmService no-op");
                return;
            }

            switch (action) {
                case ALARM_SNOOZE_ACTION:
                    // Set the alarm state to snoozed.
                    // If this broadcast receiver is handling the snooze intent then AlarmActivity
                    // must not be showing, so always show snooze toast.
                    AlarmStateManager.setSnoozeState(context, mCurrentAlarm, true /* showToast */);
                    Events.sendAlarmEvent(R.string.action_snooze, R.string.label_intent);
                    break;
                case ALARM_DISMISS_ACTION:
                    // Set the alarm state to dismissed.
                    AlarmStateManager.deleteInstanceAndUpdateParent(context, mCurrentAlarm);
                    Events.sendAlarmEvent(R.string.action_dismiss, R.string.label_intent);
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        // Register the broadcast receiver
        final IntentFilter filter = new IntentFilter(ALARM_SNOOZE_ACTION);
        filter.addAction(ALARM_DISMISS_ACTION);
        registerReceiver(mActionsReceiver, filter);
        mIsRegistered = true;
        /// M: [ALPS03269543] [Power off Alarm]
        mContext = this;
        /// @}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.v("AlarmService.onStartCommand() with %s", intent);
        if (intent == null) {
            return Service.START_NOT_STICKY;
        }

        long instanceId = -1;
        boolean isAlarmBoot = false;
        /// M: [ALPS03269543] [Power off Alarm] check if it's boot from power off alarm or not
        if (WrapperManager.isMtkPlatform()) {
            isAlarmBoot = intent.getBooleanExtra("isAlarmBoot", false);
            IntentFilter filter = new IntentFilter();
            if (PowerOffAlarm.bootFromPoweroffAlarm()) {
                /// M: [ALPS03269543] [Power off Alarm] add the power off alarm
                /// snooze\dismiss\power_on action @{
                filter.addAction(POWER_OFF_ALARM_POWER_ON_ACITION);
                filter.addAction(POWER_OFF_ALARM_SNOOZE_ACITION);
                filter.addAction(POWER_OFF_ALARM_DISMISS_ACITION);
            } else {
                /// M: [ALPS03269543] [Power off Alarm] add for Clock to dismiss alarm when preShutDown
                filter.addAction(PRE_SHUTDOWN_ACTION);
                /// M: [ALPS03269543] [Power off Alarm] add for privacy protection lock
                filter.addAction(PRIVACY_PROTECTION_CLOCK);
            }
            registerReceiver(mStopPlayReceiver, filter);
            mStopPlayReceiverRegistered = true;
            if (!isAlarmBoot) {
                /// @}
                if (intent != null && intent.getData() != null) {
                    instanceId = AlarmInstance.getId(intent.getData());
                }
            }
        }

        if (WrapperManager.isQualcommPlatform()) {
            //final long instanceId = AlarmInstance.getId(intent.getData());
            if (intent != null && intent.getData() != null) {
                instanceId = AlarmInstance.getId(intent.getData());
            }
        }

        switch (intent.getAction()) {
            case AlarmStateManager.CHANGE_STATE_ACTION:
            case POWER_OFF_ALARM_START_ACITION:
                AlarmStateManager.handleIntent(this, intent);
                /// M: [ALPS03269543] [Power off Alarm] check if boot from poweroff alarm or not @{
                if (isAlarmBoot) {
                    LogUtils.v("AlarmService isAlarmBoot = " + isAlarmBoot);
                    mInstance = AlarmStateManager.getNextFiringAlarm(mContext);
                    if (mInstance != null) {
                        AlarmStateManager.setFiredState(mContext, mInstance);
                    }
                /// @}
                }else {
                    // If state is changed to firing, actually fire the alarm!
                    final int alarmState = intent.getIntExtra(
                        AlarmStateManager.ALARM_STATE_EXTRA, -1);
                    if (alarmState == AlarmInstance.FIRED_STATE) {
                        final ContentResolver cr = this.getContentResolver();
                        mInstance = AlarmInstance.getInstance(cr, instanceId);
                    }
                }
                LogUtils.v("AlarmService instance[%s]", mInstance);
                    if (mInstance == null) {
                        LogUtils.e("No instance found to start alarm: %d", instanceId);
                        if (mCurrentAlarm != null) {
                            // Only release lock if we are not firing alarm
                            AlarmAlertWakeLock.releaseCpuLock();
                        }
                        break;
                    }

                    if (mCurrentAlarm != null && mCurrentAlarm.mId == mInstance.mId) {
                        LogUtils.e("Alarm already started for instance: %d", instanceId);
                        break;
                    }
                /// M: [ALPS03269543] [Power off Alarm] start and change label of Poweroff Alarm @{
                if (WrapperManager.isMtkPlatform()) {
                    if (PowerOffAlarm.bootFromPoweroffAlarm()) {
                        updatePoweroffAlarmLabel(this, mInstance.mLabel);
                    }
                }
                /// @}
                startAlarm(mInstance);
                break;
            case STOP_ALARM_ACTION:
                if (mCurrentAlarm != null && mCurrentAlarm.mId != instanceId) {
                    LogUtils.e("Can't stop alarm for instance: %d because current alarm is: %d",
                            instanceId, mCurrentAlarm.mId);
                    break;
                }
                stopCurrentAlarm();
                stopSelf();
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        LogUtils.v("AlarmService.onDestroy() called");
        /// M: [ALPS03269543] [Power off Alarm] Ordering corrected,
        ///onDestroy should be the last method being called
        if (WrapperManager.isMtkPlatform()) {
            //super.onDestroy();
        } else if (WrapperManager.isQualcommPlatform()) {
            super.onDestroy();
        }
        if (mCurrentAlarm != null) {
            stopCurrentAlarm();
        }
        /// M: [ALPS03269543] [Power off Alarm] unregister the power off alarm
        ///snooze\dismiss\power_on receiver @{
        if (WrapperManager.isMtkPlatform()) {
            if (mStopPlayReceiverRegistered == true) {
                unregisterReceiver(mStopPlayReceiver);
                mStopPlayReceiverRegistered = false;
            }
        }
        /// @}

        if (mIsRegistered) {
            unregisterReceiver(mActionsReceiver);
            mIsRegistered = false;
        }
        if (WrapperManager.isMtkPlatform()) {
            super.onDestroy();
        }
    }

    private final class PhoneStateChangeListener extends PhoneStateListener {

        private int mPhoneCallState;

        PhoneStateChangeListener init() {
            mPhoneCallState = -1;
            return this;
        }

        @Override
        public void onCallStateChanged(int state, String ignored) {
            if (mPhoneCallState == -1) {
                mPhoneCallState = state;
            }

            if (state != TelephonyManager.CALL_STATE_IDLE && state != mPhoneCallState) {
                startService(AlarmStateManager.createStateChangeIntent(AlarmService.this,
                        "AlarmService", mCurrentAlarm, AlarmInstance.MISSED_STATE));
            }
        }
    }

    /// M: [ALPS03269543] [Power off Alarm] Stop/snooze receiver @ {
    private final BroadcastReceiver mStopPlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.v("AlarmService mStopPlayReceiver: " + intent.getAction());
            if (mCurrentAlarm == null) {
                LogUtils.v("mStopPlayReceiver mCurrentAlarm is null, just return");
                return;
            }
            /// M: [ALPS03269543] [Power off Alarm] Send by the PowerOffAlarm
            /// AlarmAlertFullScreen, user drag the icon or time out
            if (intent.getAction().equals(POWER_OFF_ALARM_SNOOZE_ACITION)) {
                AlarmStateManager.setSnoozeState(context, mCurrentAlarm, false);
                /// M: Now it is time to delete the unused backup ringtone
                PowerOffAlarm.deleteRingtone(context, mCurrentAlarm);
                shutDown(context);
            } else {
                /// M: [ALPS03269543, ALPS02673782] [Power off Alarm] Power on action
                /// or pre_shutdown, so set dismiss state and don't shut down
                AlarmStateManager.deleteInstanceAndUpdateParent(context, mCurrentAlarm);
                /// M: [ALPS03269543] [Power off Alarm] Now it is time to delete the unused
                /// backup ringtone
                PowerOffAlarm.deleteRingtone(context, mCurrentAlarm);
                /// M: [ALPS03269543] [Power off Alarm]  Send by the PowerOffAlarm
                /// AlarmAlertFullScreen,set dismiss state  shut down
                if (intent.getAction().equals(POWER_OFF_ALARM_DISMISS_ACITION)) {
                    shutDown(context);
                }
            }
        }
    };
    /// @}

    /// M: [ALPS03269543] [Power off Alarm] update power off alarm label. @ {
    private void updatePoweroffAlarmLabel(Context context, String label) {
        Intent intent = new Intent("update.power.off.alarm.label");
        intent.putExtra("label", (label == null ? "" : label));
        context.sendBroadcast(intent);
    }
    /// @}

    /// M: [ALPS03269543] [Power off Alarm] shut down the device. @ {
    private void shutDown(Context context) {
        // send normal shutdown broadcast
        Intent shutdownIntent = new Intent(NORMAL_SHUTDOWN_ACTION);
        context.sendBroadcast(shutdownIntent);

        // shutdown the device
        Intent intent = new Intent(ALARM_REQUEST_SHUTDOWN_ACTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    /// @}

}
