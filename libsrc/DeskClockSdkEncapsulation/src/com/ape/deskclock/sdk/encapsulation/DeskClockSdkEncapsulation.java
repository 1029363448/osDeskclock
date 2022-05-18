/**
 * sdk encapsulation
 *
 */

package com.ape.deskclock.sdk.encapsulation;

import android.os.UserHandle;
import android.os.UserManager;
import android.content.Context;

public class DeskClockSdkEncapsulation {

    public static int getMyUserId() {
        return UserHandle.myUserId();
    }

    public static boolean isMyUserIdIsOwner() {
        return (UserHandle.myUserId() == UserHandle.USER_OWNER);
    }

    /** @hide */
    public static UserManager get(Context context) {
        return (UserManager) context.getSystemService(Context.USER_SERVICE);
    }
}
