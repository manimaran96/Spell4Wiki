package com.manimarank.spell4wiki.utils;

import android.os.Build;

/**
 * Util class to get any information about the user's device
 * Ensure that any sensitive information like IMEI is not fetched/shared without user's consent
 */
public class DeviceInfoUtil {

    /**
     * Get Device manufacturer
     *
     * @return
     */
    public static String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * Get Device model name
     *
     * @return
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * Get Android version. Eg. 4.4.2
     *
     * @return
     */
    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * Get API Level. Eg. 26
     *
     * @return
     */
    public static String getAPILevel() {
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    /**
     * Get Device.
     *
     * @return
     */
    public static String getDevice() {
        return Build.DEVICE;
    }
}