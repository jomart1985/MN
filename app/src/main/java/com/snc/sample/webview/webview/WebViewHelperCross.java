package com.snc.sample.webview.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.snc.sample.webview.BuildConfig;
import com.snc.zero.log.Logger;
import com.snc.zero.util.PackageUtil;

import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkView;

import java.util.HashMap;
import java.util.Map;


public class WebViewHelperCross {


    private static final String TAG = WebViewHelperCross.class.getSimpleName();

    private static final String SCHEME_HTTP = "http://";
    private static final String SCHEME_HTTPS = "https://";
    private static final String SCHEME_FILE = "file://";
    private static final String SCHEME_ASSET = "file:///android_asset";
    private static final String SCHEME_ASSET_API30 = SCHEME_HTTPS + BuildConfig.ASSET_BASE_DOMAIN + BuildConfig.ASSET_PATH;
    private static final String SCHEME_RES = "file:///android_res";
    private static final String SCHEME_RES_API30 = SCHEME_HTTPS + BuildConfig.ASSET_BASE_DOMAIN + BuildConfig.RES_PATH;
    private static final String SCHEME_LOC_API30 = SCHEME_HTTPS + BuildConfig.ASSET_BASE_DOMAIN + "/ext/";

    public static XWalkView addWebView(Context context, ViewGroup parentView) {
        XWalkView xwebView = newWebView(context);
        parentView.addView(xwebView);
        return xwebView;
    }

    private static XWalkView newWebView(Context context) {
        XWalkView xwebView = new XWalkView(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        xwebView.setLayoutParams(params);
        //webView.setBackgroundColor(Color.TRANSPARENT);
        //webView.setBackgroundResource(android.R.color.white);

        // setup
        setup(xwebView);

        return xwebView;
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface"})
    private static void setup(XWalkView xwebView) {
        XWalkSettings xwebSettings =  xwebView.getSettings();

        xwebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        xwebView.setInitialScale(0); //
        xwebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        xwebView.setScrollbarFadingEnabled(true);

        xwebSettings.setAllowFileAccess(true);
        xwebSettings.setAllowContentAccess(true);
        xwebSettings.setAllowFileAccessFromFileURLs(true);
        xwebSettings.setAllowUniversalAccessFromFileURLs(true);
        xwebSettings.setBlockNetworkImage(false);
        xwebSettings.setBlockNetworkLoads(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
           // settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           // settings.setSafeBrowsingEnabled(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
           // settings.setDisabledActionModeMenuItems(WebSettings.MENU_ITEM_NONE);
        }
        xwebSettings.setJavaScriptEnabled(true);
        xwebSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        if (Build.VERSION.SDK_INT < 30) {  // Build.VERSION_CODES.R
            //settings.setAppCacheEnabled(false);
        }
        xwebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        xwebSettings.setDatabaseEnabled(true);
        xwebSettings.setDomStorageEnabled(true);
        xwebSettings.setLoadsImagesAutomatically(true);
        xwebSettings.setLoadWithOverviewMode(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           // xwebSettings.setOffscreenPreRaster(false);
        }
        xwebSettings.setSupportMultipleWindows(true);
        xwebSettings.setUseWideViewPort(true);
        xwebSettings.setSupportZoom(true);
        xwebSettings.setBuiltInZoomControls(true);
        //xwebSettings.setDisplayZoomControls(false);
        xwebSettings.setTextZoom(100);  //
       // xwebSettings.setGeolocationEnabled(true);
        //xwebSettings.DefaultTextEncodingName("utf-8");   //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            xwebSettings.setMediaPlaybackRequiresUserGesture(true);    // The default is true. Added in API level 17
        }
        //xwebSettings.setNeedInitialFocus(true); //
        xwebSettings.setUserAgentString(makeUserAgent(xwebView));
    }

    public static String makeUserAgent(XWalkView xwebView) {
        String ua = xwebView.getSettings().getUserAgentString();
        try {
            ua += !ua.endsWith(" ") ? " " : "";
            ua += PackageUtil.getApplicationName(xwebView.getContext());
            ua += "/" + PackageUtil.getPackageVersionName(xwebView.getContext());
            ua += "." + PackageUtil.getPackageVersionCode(xwebView.getContext());
            return ua;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, e);
        }
        return ua;
    }


    public static String getLocalBaseUrl(Activity a, String type) {
        if ("assets".equals(type)) {
            if (BuildConfig.FEATURE_WEBVIEW_ASSET_LOADER) {
                return SCHEME_ASSET_API30;
            }
            return SCHEME_ASSET;
        }
        else if ("res".equals(type)) {
            if (BuildConfig.FEATURE_WEBVIEW_ASSET_LOADER) {
                return SCHEME_RES_API30;
            }
            return SCHEME_RES;
        }
        else if ("loc".equals(type)) {

            if (BuildConfig.FEATURE_WEBVIEW_ASSET_LOADER) {
                return SCHEME_LOC_API30;
            }



        }

        return "";
    }

    public static void loadUrl(final XWalkView xwebView, final String uriString) {
        final Map<String, String> extraHeaders = new HashMap<>();
        //extraHeaders.put("Platform", "A");

        if (uriString.startsWith(SCHEME_HTTP) || uriString.startsWith(SCHEME_HTTPS)
                || uriString.startsWith(SCHEME_ASSET)
                || uriString.startsWith(SCHEME_ASSET_API30)
                || uriString.startsWith(SCHEME_LOC_API30)) {
            xwebView.load(uriString, null);
        }
        else if (uriString.startsWith(SCHEME_FILE)) {

            xwebView.loadUrl(uriString, extraHeaders);

            /*List<String> permissions = new ArrayList<>();
            // Dangerous Permission
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);

            RPermission.with(webView.getContext())
                    .setPermissions(permissions)
                    .setPermissionListener(new RPermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            Logger.i(TAG, "[WEBVIEW] onPermissionGranted()");
                            webView.loadUrl(uriString, extraHeaders);
                        }

                        @Override
                        public void onPermissionDenied(List<String> deniedPermissions) {
                            Logger.e(TAG, "[WEBVIEW] onPermissionDenied()..." + deniedPermissions.toString());
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<String> deniedPermissions) {
                            Logger.e(TAG, "[WEBVIEW] onPermissionRationaleShouldBeShown()..." + deniedPermissions.toString());
                        }
                    })
                    .check();*/
        }
    }
}
