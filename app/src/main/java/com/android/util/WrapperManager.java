/**
 * wrapper manager
 * liujia, date20151126
 */

package com.android.util;

import android.os.SystemProperties;
import android.util.Log;


public class WrapperManager {

    private static final String TAG = "WrapperManager";


    private static final String PLATEFORM_INFO_HARDWARE = "ro.hardware";
    private static final String MTK_HARDWARE = "mt";
    private static final String QUALCOMM_HARDWARE = "qcom";

    private static final String PLATEFORM_INFO_RIL = "gsm.version.ril-impl";
    private static final String MTK_RIL = "mtk";
    private static final String QUALCOMM_RIL = "qualcomm";

    private enum PLATEFORM {
        QUALCOMM,
        MTK,
        OTHER
    }

    private static PLATEFORM plateform = null;


    private static PLATEFORM getPlatform() {
        if (plateform == null) {
            final String plateformInfoHardware = SystemProperties.get(PLATEFORM_INFO_HARDWARE);
            final String plateformInfoRIL = SystemProperties.get(PLATEFORM_INFO_RIL);
            Log.i(TAG, "getPlatform, plateformInfoHardware : " + plateformInfoHardware
                    + " , plateformInfoRIL : " + plateformInfoRIL);

            if (plateformInfoRIL != null) {
                if (plateformInfoRIL.toLowerCase().contains(QUALCOMM_RIL)) {
                    plateform = PLATEFORM.QUALCOMM;
                } else if (plateformInfoRIL.toLowerCase().contains(MTK_RIL)) {
                    plateform = PLATEFORM.MTK;
                } else {
                    plateform = PLATEFORM.OTHER;
                }
            }

            if ((plateform == PLATEFORM.OTHER || plateform == null)
                    && plateformInfoHardware != null) {
                if (plateformInfoHardware.toLowerCase().contains(QUALCOMM_HARDWARE)) {
                    plateform = PLATEFORM.QUALCOMM;
                } else if (plateformInfoHardware.toLowerCase().contains(MTK_HARDWARE)) {
                    plateform = PLATEFORM.MTK;
                } else {
                    plateform = PLATEFORM.OTHER;
                }
            }
            Log.i(TAG, "getPlatform, plateform : " + plateform);
        }
        return plateform;
    }

    public static boolean isQualcommPlatform() {
        return (PLATEFORM.QUALCOMM == getPlatform());
    }

    public static boolean isMtkPlatform() {
        return (PLATEFORM.MTK == getPlatform());
    }
}
