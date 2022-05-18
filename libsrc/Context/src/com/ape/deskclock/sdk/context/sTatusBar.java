/**
 * sdk context
 *
 */

package com.ape.deskclock.sdk.context;

import android.app.StatusBarManager;
import android.content.Context;

public class sTatusBar {

    public static StatusBarManager getStatusBar(Context context) {
        StatusBarManager statusBar = (StatusBarManager)
                context.getSystemService(Context.STATUS_BAR_SERVICE);
        return statusBar;
    }


}
