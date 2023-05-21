package com.snc.zero.application;
import android.os.Build;

import com.hzy.lib7z.Z7Extractor;
import com.getkeepsafe.relinker.ReLinker;
import com.blankj.utilcode.util.Utils;
import com.snc.zero.log.Logger;

import androidx.multidex.MultiDexApplication;

/**
 * Application
 *
 * @author mcharima5@gmail.com
 * @since 2018
 */
public class SNCApplication extends MultiDexApplication {
    private static final String TAG = SNCApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        Logger.i(TAG, ">>>>>>>>>> onCreate <<<<<<<<<<");
        super.onCreate();

        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion < 23) {
            Z7Extractor.init(libName ->
                    ReLinker.loadLibrary(SNCApplication.this, libName));
            Utils.init(this);
        }



        //throw new RuntimeException("Test Crash"); // Force a crash
    }

    @Override
    public void onLowMemory() {
        Logger.i(TAG, ">>>>>>>>>> onLowMemory <<<<<<<<<<");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Logger.i(TAG, ">>>>>>>>>> onTrimMemory(" + level + ") <<<<<<<<<<");
        super.onTrimMemory(level);
    }

}
