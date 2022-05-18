# Copyright 2007-2008 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)

# Static library with some common classes for the phone apps.
# To use it add this line in your Android.mk
#  LOCAL_STATIC_JAVA_LIBRARIES := ape-dialer-sdk-encapsulation

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_MODULE := MTKPowerOffAlarm

LOCAL_JACK_ENABLED := disabled
LOCAL_JAVA_LANGUAGE_VERSION := 1.7

include $(BUILD_STATIC_JAVA_LIBRARY)
