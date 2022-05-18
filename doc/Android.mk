#
# Copyright (C) 2008 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#TODO: write a build template file to do this, and/or fold into multi_prebuilt.

LOCAL_PATH := $(my-dir)

ifeq ($(strip $(DESKCLOCK60_SUPPORT)), yes)
    ifneq ($(strip $(DESKCLOCK60_NAME)),)

    include $(CLEAR_VARS)
    LOCAL_MODULE_TAGS := optional
    LOCAL_MODULE := $(DESKCLOCK60_NAME)
    LOCAL_SRC_FILES := $(DESKCLOCK60_NAME).apk
    LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
    LOCAL_MODULE_CLASS := APPS
    LOCAL_CERTIFICATE := platform
    LOCAL_PRIVILEGED_MODULE := false

    ifneq ($(strip $(DESKCLOCK60_OVERRIDES_PACKAGES)),)
        LOCAL_OVERRIDES_PACKAGES := $(DESKCLOCK60_OVERRIDES_PACKAGES)
    else
        LOCAL_OVERRIDES_PACKAGES := DeskClock
    endif

    include $(BUILD_PREBUILT)
    endif
endif

