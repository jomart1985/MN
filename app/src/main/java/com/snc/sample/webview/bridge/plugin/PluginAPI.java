package com.snc.sample.webview.bridge.plugin;

import android.webkit.WebView;

import com.snc.sample.webview.bridge.AndroidBridge;
import com.snc.sample.webview.bridge.plugin.interfaces.Plugin;
import com.snc.zero.json.JSONHelper;
import com.snc.zero.log.Logger;

import org.json.JSONObject;

/**
 * Plugin
 *
 * @author mcharima5@gmail.com
 * @since 2022
 */
@SuppressWarnings({"InstantiationOfUtilityClass", "unused", "RedundantSuppression"})
public class PluginAPI implements Plugin {
    private static final String TAG = PluginAPI.class.getSimpleName();

    private static final PluginAPI mInstance = new PluginAPI();
    public static PluginAPI getInstance() {
        return mInstance;
    }

    /////////////////////////////////////////////////

    public static void recommended(final WebView webview, final JSONObject args, final String cbId) {
        Logger.i(TAG, "[WEBVIEW] recommended : args[" + args + "], cbId[" + cbId + "]");

        new Thread(() -> {

            // test code...
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                Logger.e(TAG, e);
            }
            //--

            JSONObject jsonObject = new JSONObject();
            JSONHelper.put(jsonObject, "result", "success");

            // send result
            AndroidBridge.callFromNative(webview, cbId, "00000", jsonObject.toString());
        }).start();
    }
}
