package com.snc.sample.webview.bridge;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.blankj.utilcode.util.StringUtils.getString;
import static com.google.android.material.internal.ContextUtils.getActivity;

import static org.chromium.base.ThreadUtils.runOnUiThread;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.snc.sample.webview.R;
import com.snc.sample.webview.activity.DataProccessor;
import com.snc.sample.webview.activity.LauncherActivity;
import com.snc.sample.webview.activity.RealPathUtil;
import com.snc.sample.webview.webview.WebViewHelper;
import com.snc.zero.dialog.DialogBuilder;
import com.snc.zero.json.JSONHelper;
import com.snc.zero.log.Logger;
import com.snc.zero.util.StringUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * WebView JavaScript Interface Bridge
 *
 * @author mcharima5@gmail.com
 * @since 2018
 */
public class AndroidBridge {
    private static final String TAG = AndroidBridge.class.getSimpleName();

    private static final String SCHEME_BRIDGE = "native";

    // Web -> Native
    private static final String HOST_COMMAND = "callToNative";

    private static final String SCHEME_JAVASCRIPT = "javascript:";

    private final WebView webView;

    private static final Map<String, String> callbackFunctionNames = new HashMap<>();
    private static File extraOutput;
    Activity acc;
    LauncherActivity LaunActivity;

    DataProccessor datapro;

    private String mOutputPath;

    // constructor
    public AndroidBridge(WebView webView, Activity ac) {
        this.webView = webView;
        acc =  ac;
        datapro = new DataProccessor(ac);
    }


    //++ [START] call Web --> Native

    // ex) "native://callToNative?" + btoa(encodeURIComponent(JSON.stringify({ command:\"apiSample\", args{max:1,min:1}, callback:\"callbackNativeResponse\" })))
    @JavascriptInterface
    public boolean callNativeMethod(String urlString) {
        Logger.i(TAG, "[WEBVIEW] callNativeMethod: " + urlString);
        try {
            Uri uri = Uri.parse(urlString);
            JSONObject jsonObject = parse(uri);
            //jsonObject.put("hostCommand", uri.getHost());

            String pluginName = JSONHelper.getString(jsonObject, "plugin", "");

            if (StringUtil.isEmpty(pluginName)) {
                DialogBuilder.with(webView.getContext())
                        .setMessage("Plugin not exist")
                        .show();
                return false;
            }

            return AndroidBridgePlugin.execute(this.webView, jsonObject);

        } catch (Exception e) {
            Logger.e(TAG, e);

            DialogBuilder.with(webView.getContext())
                    .setMessage(e.toString())
                    .show();
        }
        return false;
    }


    @JavascriptInterface
    public boolean go_ext(String href, String value) {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(href));
        acc.startActivity(intent);
        return true;
    }

    @JavascriptInterface
    public int get_api_level() {

        return Build.VERSION.SDK_INT;
    }

    @JavascriptInterface
    public String GoAver(String hh) {
        return Build.VERSION.RELEASE;
    }
    @JavascriptInterface
    public String  GoArch(String hh) {
        return Build.CPU_ABI;
    }
    @JavascriptInterface
    public boolean  setSharP(String name ,String value) {

        datapro.setSharP(name,value);
        return false;
    }



    @JavascriptInterface
    public void DownloadImageURL(String src) {



            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!acc.isFinishing()){

                        String url = src;
                        datapro.setSharP("img_src", url);

                        String filename = url.substring(url.lastIndexOf("/") + 1);
                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_TITLE, filename);
                        acc.startActivityForResult(intent, 1);



                    }

                }
            });



    }

    @JavascriptInterface
    public void copy_text(String type, String def) {

        ClipboardManager clipboard = (ClipboardManager) acc.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copy", type);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(acc, "copyeid", Toast.LENGTH_LONG).show();

    }

    @JavascriptInterface
    public String getSharP(String type, String def) {

        return  datapro.getSharP(type,def);
    }

    @JavascriptInterface
    public void goPrintWebView() {

        /*new Handler().post(new Runnable() {
            @Override
            public void run() {

            }
        });*/

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                 if(!acc.isFinishing()){
                     doWebViewPrint(webView.getContext());

                 }

            }
        });
    }




    @JavascriptInterface
    public void lang_dailog() {
        Intent intent = new Intent(acc, LauncherActivity.class);
        Bundle b = new Bundle();
        b.putString("go_to", "open_lang_dialog"); //Your id
        intent.putExtras(b); //Put your id to your next Intent

        datapro.setSharP("go_to","open_lang_dialog");

        acc.startActivity(intent);
        acc.finish();

    }

    @JavascriptInterface
    public void change_WV() {
        Intent intent = new Intent(acc, LauncherActivity.class);
        Bundle b = new Bundle();
        b.putString("go_to", "open_webviewe_dialog"); //Your id
        intent.putExtras(b); //Put your id to your next Intent

        datapro.setSharP("go_to","open_webviewe_dialog");

        acc.startActivity(intent);
        acc.finish();

    }
    @JavascriptInterface
    public void go_extract() {
        Intent intent = new Intent(acc, LauncherActivity.class);
        Bundle b = new Bundle();
        b.putString("go_to", "open_extract_dialog"); //Your id
        intent.putExtras(b); //Put your id to your next Intent

        datapro.setSharP("go_to","open_extract_dialog");

        acc.startActivity(intent);
        acc.finish();

    }



    @JavascriptInterface
    public void toggFull(String value){


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!acc.isFinishing()){
                   // acc.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);



                   if(datapro.getSharP("fullscreen", "false").equals( "true")){



                           datapro.setSharP("fullscreen", "false");

                           //Toast.makeText(webView.getContext(), "show", Toast.LENGTH_SHORT).show();
                       //this must be  here after datapro.setSharP or it will not work toggle on cross
                       showSystemUI(acc.getWindow());



                   }else{



                           //Toast.makeText(webView.getContext(), "hide", Toast.LENGTH_SHORT).show();
                           datapro.setSharP("fullscreen", "true");
                       //this must be  here after datapro.setSharP or it will not work toggle on cross
                       hideSystemUI(acc.getWindow());
                   }

                }

            }
        });



    }



    @JavascriptInterface
    public void goTost(String value, String def) {

        Toast.makeText(webView.getContext(), value.toString(), Toast.LENGTH_SHORT).show();
    }




    private JSONObject parse(Uri uri) throws IOException {
        Logger.i(TAG, "[WEBVIEW] callNativeMethod: parse() : uri = " + uri);

        if (!SCHEME_BRIDGE.equals(uri.getScheme())) {
            throw new IOException("\"" + uri.getScheme() + "\" scheme is not supported.");
        }
        if (!HOST_COMMAND.equals(uri.getHost())) {
            throw new IOException("\"" + uri.getHost() + "\" host is not supported.");
        }

        String query = uri.getEncodedQuery();
        try {
            query = new String(Base64.decode(query, Base64.DEFAULT));
            query = URLDecoder.decode(query, "utf-8");

            return new JSONObject(query);
        } catch (Exception e) {
            throw new IOException("\"" + query + "\" is not JSONObject.");
        }
    }
    //-- [E N D] call Web --> Native


    //++ [START] call Native --> Web

    public static void callFromNative(WebView webView, String cbId, String resultCode, String jsonString) {
        String param = "'" + cbId + "', '" + resultCode + "', '" + jsonString + "'";

        String buff = "!(function() {\n" +
                "  try {\n" +
                "    NativeBridge.callFromNative(" + param + ");\n" +
                "  } catch(e) {\n" +
                "    return '[JS Error] ' + e.message;\n" +
                "  }\n" +
                "})(window);";
        webView.post(() -> evaluateJavascript(webView, buff));
    }

    public static void callJSFunction(final WebView webView, String functionName, String... params) {
        if (functionName.startsWith("function")
            || functionName.startsWith("(")) {
            String buff = "!(\n" +
                    functionName +
                    ")(" + makeParam(params) + ");";
            webView.post(() -> evaluateJavascript(webView, buff));
        } else {
            String js = makeJavascript(functionName, params);
            String buff = "!(function() {\n" +
                    "  try {\n" +
                    "    " + js + "\n" +
                    "  } catch(e) {\n" +
                    "    return '[JS Error] ' + e.message;\n" +
                    "  }\n" +
                    "})(window);";
            webView.post(() -> evaluateJavascript(webView, buff));
        }
    }

    public static String makeJavascript(String functionName, String... params) {
        return functionName + "(" + makeParam(params) + ");";
    }

    public static String makeParam(String... params) {
        final StringBuilder buff = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];

            // 데이터 설정
            if (null != param) {
                buff.append("'").append(param).append("'");
            } else {
                buff.append("''");
            }

            if (i < params.length - 1) {
                buff.append(", ");
            }
        }
        return buff.toString();
    }

    private static void evaluateJavascript(final WebView webView, final String javascriptString) {
        String jsString = javascriptString;

        if (jsString.startsWith(SCHEME_JAVASCRIPT)) {
            jsString = jsString.substring(SCHEME_JAVASCRIPT.length());
        }

        jsString = jsString.replaceAll("\t", "    ");

        // Android 4.4 (KitKat, 19) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(jsString, value -> Logger.i(TAG, "[WEBVIEW] onReceiveValue: " + value));
        }
        // Android 4.3 or lower (Jelly Bean, 18)
        else {
            webView.loadUrl(SCHEME_JAVASCRIPT + jsString);
        }
    }

    //-- [E N D] call Native --> Web


    //++ [[START] for JS Callback]

    public static void setCallbackJSFunctionName(int requestCode, String functionName) {
        callbackFunctionNames.put(String.valueOf(requestCode), functionName);
    }

    public static String getCallbackJSFunctionName(int requestCode) {
        return callbackFunctionNames.remove(String.valueOf(requestCode));
    }

    public static File getExtraOutput(boolean pop) {
        if (pop) {
            File file = extraOutput;
            extraOutput = null;
            return file;
        }
        return extraOutput;
    }

    public static void setExtraOutput(File file) {
        extraOutput = file;
    }
    //-- [[E N D] for JS Callback]


    private WebView mWebView;

    private void doWebViewPrint(Context con) {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(con.getApplicationContext());

        WebSettings settings = webView.getSettings();

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setInitialScale(0); //
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setBlockNetworkImage(false);
        settings.setBlockNetworkLoads(false);
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setLoadWithOverviewMode(false);
        settings.setSupportMultipleWindows(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setTextZoom(100);  //
        settings.setGeolocationEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");   //
        settings.setNeedInitialFocus(true); //


        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                createWebPrintJob(view,con);
                super.onPageFinished(view, url);
            }
        });



        //String htmlDocument = "<html><body><h1>Test Content</h1><p>Testing, " +
               // "testing, testing...</p></body></html>";
        //webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        webView.addJavascriptInterface(new AndroidBridge(webView, acc), "Androidd");

        File outFile = acc.getExternalFilesDir("ext");
        if (outFile == null || !outFile.exists()) {
            outFile = acc.getFilesDir();
        }
        mOutputPath = outFile.getPath();
        // WebViewHelper.loadUrl(this.webview, "file://"+mOutputPath+ "/app/dir.html");
        ///Logger.i(TAG, "---------------" + WebViewHelper.getLocalBaseUrl(this, "loc") + "app/index.html");

        // WebViewHelper.loadUrl(this.webview, "file://"+mOutputPath+ "/app/index.html");
        //WebViewHelper.loadUrl(this.webview, WebViewHelper.getLocalBaseUrl(this ,"loc")+"app/index.html");


       // webView.loadUrl(datapro.getSharP("current_page","file:///android_asset/www/app/index.html"));
        //Toast.makeText(this, "file:///"+mOutputPath+"/app/index.html",Toast.LENGTH_LONG).show();
///without this.
         webView.loadUrl("file:///"+mOutputPath+"/app/print.html");
        //webView.loadUrl("file:///android_asset/www/app/print.html");

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }



    @SuppressLint("RestrictedApi")
    private void createWebPrintJob(WebView webView,Context con) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return;

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) con.getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = getString(R.string.app_name) + " Document";
       /* printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());*/

        // Create a print job with name and adapter instance
         printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());




    }


    public void hideSystemUI(Window window) { //pass getWindow();

        //don't use for now
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.getInsetsController().hide(WindowInsets.Type.systemBars());
        }*/

        View decorView = window.getDecorView();

        int uiVisibility = decorView.getSystemUiVisibility();

        uiVisibility |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        uiVisibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE;
            uiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        decorView.setSystemUiVisibility(uiVisibility);

    }

    public void showSystemUI(Window window) { //pass getWindow();

        //don't use for now
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.getInsetsController().show(WindowInsets.Type.systemBars());
        }*/

        View decorView = window.getDecorView();

        int uiVisibility = decorView.getSystemUiVisibility();

        uiVisibility &= ~View.SYSTEM_UI_FLAG_LOW_PROFILE;
        uiVisibility &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiVisibility &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiVisibility &= ~View.SYSTEM_UI_FLAG_IMMERSIVE;
            uiVisibility &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        decorView.setSystemUiVisibility(uiVisibility);

    }





}
