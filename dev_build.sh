#!/bin/bash

# Exit the script if any step fails
set -e

./gradlew installDebug

adb shell am force-stop org.cranst0n.dogleg.android

adb shell am start -n org.cranst0n.dogleg.android/org.cranst0n.dogleg.android.activity.HomeActivity

adb logcat | grep -i dogleg
