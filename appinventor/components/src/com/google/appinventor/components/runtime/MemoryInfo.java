// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

package com.google.appinventor.components.runtime;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Contains various helper methods
 *
 */
@DesignerComponent(version = YaVersion.MEMORY_INFO_COMPONENT_VERSION,
        description = "xxxxxxxx Add description to ODE ",
        category = ComponentCategory.SENSORS,
        nonVisible = true,
        iconName = "images/memoryinfo.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.READ_EXTERNAL_STORAGE")
public class MemoryInfo extends AndroidNonvisibleComponent implements Component {
    private static final String LOG_TAG = "MemoryInfo";
    private final Context context;
    /**
     * Creates a new MemoryInfo component.
     *
     * @param container the Form that this component is contained in.
     */
    public MemoryInfo(ComponentContainer container) {
        super(container.$form());
        context = (Context) container.$context();
    }

    @SimpleFunction(description = "Total free RAM size in Gigabytes")
    public long MemoryFree() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;

        return availableMegs;
    }

    @SimpleFunction(description = "Total RAM size in Gigabytes")
    public long MemoryTotal() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.totalMem / 1048576L;
        return availableMegs;
    }

    @SimpleFunction(description = "Returns size of used-memory in Gigabytes")
    public long MemoryUsed() {
        long totalRamValue = MemoryTotal();
        long freeRamValue = MemoryFree();
        long usedRamValue = totalRamValue - freeRamValue;
        return usedRamValue;
    }

    // ==================== internal
    @SimpleFunction(description = "Returns size of available internal storage in Gigabytes")
    public long InternalStorageAvailable() {
        java.io.File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return (availableBlocks * blockSize) / 1048576L;
    }

    @SimpleFunction(description = "Returns total size of internal storage in Gigabytes")
    public long InternalStorageTotal() {
        java.io.File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return (totalBlocks * blockSize) / 1048576L;
    }

    @SimpleFunction(description = "Returns size of used-internal-storage in Gigabytes")
    public long InternalStorageUsed() {
        long totalInternalValue = InternalStorageTotal();
        long freeInternalValue = InternalStorageAvailable();
        long usedInternalValue = totalInternalValue - freeInternalValue;
        return usedInternalValue;
    }

    //=============== external
    @SimpleFunction(description = "Returns available size of external storage in Gigabytes")
    public long ExternalStorageAvailable() {
        if (externalStorageAvailable()) {
            java.io.File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return (availableBlocks * blockSize) / 1048576L;
        } else {
            return 0;
        }
    }

    @SimpleFunction(description = "Returns total external storage size in Gigabytes")
    public long ExternalStorageTotal() {
        if (externalStorageAvailable()) {
            java.io.File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return (totalBlocks * blockSize) / 1048576L;
        } else {
            return 0;
        }
    }
    @SimpleFunction(description = "Returns size of used-external-storage in Gigabytes")
    public long ExternalStorageUsed() {
        long totalExternalValue = ExternalStorageTotal();
        long freeExternalValue = ExternalStorageAvailable();
        long usedExternalValue = totalExternalValue - freeExternalValue;
        return usedExternalValue;
    }

    public boolean externalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

}



