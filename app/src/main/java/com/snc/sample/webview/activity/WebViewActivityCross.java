package com.snc.sample.webview.activity;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.snc.sample.webview.BuildConfig;
import com.snc.sample.webview.R;
import com.snc.sample.webview.bridge.AndroidBridge;
import com.snc.sample.webview.bridge.AndroidBridgeCross;
import com.snc.sample.webview.webview.WebViewHelper;
import com.snc.sample.webview.webview.WebViewHelperCross;
import com.snc.zero.activity.BaseActivity;
import com.snc.zero.dialog.DialogBuilder;
import com.snc.zero.webview.CSWebViewClientCross;


import org.xwalk.core.XWalkHitTestResult;
import org.xwalk.core.XWalkInitializer;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkUpdater;
import org.xwalk.core.XWalkView;

import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.http.Url;


public class WebViewActivityCross extends BaseActivity
        implements
        XWalkInitializer.XWalkInitListener,
        XWalkUpdater.XWalkUpdateListener
{
    private XWalkInitializer mXWalkInitializer;
    private XWalkUpdater mXWalkUpdater;
    private XWalkView mXWalkView;
    private String mOutputPath;
    private XWalkHitTestResult webViewHitTestResult;
    DataProccessor datapro;
    private AlertDialog myAlertDialog = null;
    Timer t = null ;
    WebViewActivityCross WA;
    boolean load_done = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_activty_cross);
        mXWalkInitializer = new XWalkInitializer(this, this);
        mXWalkInitializer.initAsync();
        datapro = new DataProccessor(this);
        WA = this;


        //fix exposed beyond app through Intent.getData()
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        datapro.setLocale(this, datapro.getSharP("lang","null"));

    }


    @SuppressLint("AddJavascriptInterface")
    private void init() {
        ViewGroup contentView = findViewById(R.id.contentView_cross);
        if (null == contentView) {
            DialogBuilder.with(getActivity())
                    .setMessage("The contentView does not exist.")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                    .show();
            return;
        }
        File outFile = getExternalFilesDir("ext");
        if (outFile == null || !outFile.exists()) {
            outFile = getFilesDir();
        }
        mOutputPath = outFile.getPath();
        this.mXWalkView = WebViewHelperCross.addWebView(getContext(), contentView);

        this.mXWalkView.addJavascriptInterface(new AndroidBridgeCross(mXWalkView,this), "Androidd");



        CSWebViewClientCross webviewClientCross = new CSWebViewClientCross(getContext(),this.mXWalkView);
        this.mXWalkView.setResourceClient(webviewClientCross);



        datapro.setSharP("SP_WEBVIEW_TYPE", "cross");

        this.registerForContextMenu(this.mXWalkView);

        AndroidBridgeCross ff = new AndroidBridgeCross(this.mXWalkView,this);
        // ff.toggFull("dont_save");

        this.mXWalkView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                set_full_screen();
            }
        });




        load_done = true;






        //WebViewHelperCross.loadUrl(this.mXWalkView, WebViewHelperCross.getLocalBaseUrl(this ,"assets") + "/www/app/index.html");

        //WebViewHelperCross.loadUrl(this.mXWalkView, "file:///android_asset/www/app/index.html");
        //WebViewHelperCross.loadUrl(this.mXWalkView, WebViewHelperCross.getLocalBaseUrl(this ,"loc")+"app/index.html");

        //this.mXWalkView.loadUrl(datapro.getSharP("current_page","file:///android_asset/www/app/index.html"));

        //Toast.makeText(this, "file:///"+mOutputPath+"/app/index.html",Toast.LENGTH_LONG).show();

        this.mXWalkView.loadUrl(datapro.getSharP("current_page","file:///"+mOutputPath+"/app/index.html"));


    }





    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                    ContextMenu.ContextMenuInfo contextMenuInfo){
        super.onCreateContextMenu(contextMenu, view, contextMenuInfo);

        /*MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lovely_context, menu);*/

        //final WebView.HitTestResult webViewHitTestResult = webview.getHitTestResult();
        webViewHitTestResult = ((XWalkView) view).getHitTestResult();

        XWalkHitTestResult.type resultType = webViewHitTestResult.getType();

        if (resultType == XWalkHitTestResult.type.SRC_IMAGE_ANCHOR_TYPE
        || resultType == XWalkHitTestResult.type.IMAGE_TYPE){

            contextMenu.add(0, 0, 0, R.string.Download_image);
        }


        if (resultType == XWalkHitTestResult.type.ANCHOR_TYPE
                || resultType == XWalkHitTestResult.type.SRC_ANCHOR_TYPE){

            contextMenu.add(0, 1, 0, R.string.copy_link);
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {


        datapro = new DataProccessor(this);
        if (item.getItemId() == 0) {
            //Toast.makeText(this, "save", Toast.LENGTH_LONG).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {


                String url= webViewHitTestResult.getExtra();
                datapro.setSharP("img_src", url);

                String filename = url.substring(url.lastIndexOf("/") + 1);
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_TITLE, filename);
                startActivityForResult(intent, 1);


            }
        }


        if(item.getItemId() == 1){
            Toast.makeText(this,String.valueOf(webViewHitTestResult.getExtra()),Toast.LENGTH_LONG).show();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("copy", webViewHitTestResult.getExtra());
            clipboard.setPrimaryClip(clip);
        }
        return true;
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if(resultCode != RESULT_CANCELED) {
            if (requestCode == 1) {
                if (resultCode == RESULT_OK) {
                    runOnUiThread(() -> {
                        if (!isFinishing()) {

                            try {

                                datapro = new DataProccessor(this);
                                Uri currentUri = data.getData();

                                String img_src = datapro.getSharP("img_src", "null");
                                //String someFilepath = img_src;
                                //String extension = someFilepath.substring(someFilepath.lastIndexOf("."));

                                URL url = new URL(img_src);

                                // Uri url = Uri.parse(img_src);


                       /* HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setDoOutput(true);
                        urlConnection.connect();*/

                                InputStream input = url.openStream();
                                //InputStream input =  urlConnection.getInputStream();


                                //InputStream input = getActivity().getApplicationContext().getContentResolver().openInputStream(url);


                                @SuppressLint("Recycle")
                                ParcelFileDescriptor pfd =
                                        getContentResolver().
                                                openFileDescriptor(currentUri, "w");

                                OutputStream output = new FileOutputStream(
                                        pfd.getFileDescriptor());


                                byte[] buffer = new byte[1024];
                                int bytesRead = 0;
                                while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                                    output.write(buffer, 0, bytesRead);
                                }

                                Toast.makeText(WA, R.string.download_image_done, Toast.LENGTH_LONG).show();


                            } catch (Exception e) {
                                e.printStackTrace();

                                Toast.makeText(WA, R.string.download_image_fail, Toast.LENGTH_LONG).show();
                            }


                        }
                    });
                }
            }
        }

    }















    private boolean isPackageInstalled( PackageManager packageManager) {

        try {

            String bits;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bits = TextUtils.join(", ", Build.SUPPORTED_ABIS).contains("64") ? "64-Bit" : "32-Bit";
            } else {
                bits = "32-Bit";
            }

           if(bits == "32-Bit"){
               packageManager.getPackageInfo("org.xwalk.core", 0);
               return true;
           }else{
               packageManager.getPackageInfo("org.xwalk.core64", 0);
               return true;
           }

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }






    @Override
    protected void onResume() {
        super.onResume();


        mXWalkInitializer.initAsync();
       if(load_done){

           if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
               this.mXWalkView.evaluateJavascript("if (typeof afterPrint === 'function') { afterPrint();} ", null);
           } else {
               this.mXWalkView.loadUrl("if (typeof afterPrint === 'function') { afterPrint();}");
           }

           if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
               this.mXWalkView.evaluateJavascript("$(\"#loading\").hide();", null);
           } else {
               this.mXWalkView.loadUrl("$(\"#loading\").hide();");
           }


       }



    }


    @Override
    public void onXWalkInitStarted() {
    }

    @Override
    public void onXWalkInitCancelled() {
        // Perform error handling here

        finish();
    }

    @Override
    public void onXWalkInitFailed() {
        if (mXWalkUpdater == null) {
            mXWalkUpdater = new XWalkUpdater(this, this);
        }
        //mXWalkUpdater.updateXWalkRuntime();

        show_langs_dialog(this,"show");





    }

    @Override
    public void onXWalkInitCompleted() {
        // Do anyting with the embedding API

        if (mXWalkUpdater != null) {
            mXWalkUpdater.dismissDialog();
        }




        init();

    }

    @Override
    public void onXWalkUpdateCancelled() {
        // Perform error handling here

        finish();
    }

    @Override
    public void onBackPressed() {
        if (this.mXWalkView.getNavigationHistory().canGoBack()) {
                this.mXWalkView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);

        } else {
            super.onBackPressed();
        }
    }






    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        set_full_screen();
    }

    public void set_full_screen() {
        AndroidBridgeCross ff = new AndroidBridgeCross(this.mXWalkView,this);
        // ff.toggFull("dont_save");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(datapro.getSharP("fullscreen", "false").equals( "true")){

                    new android.os.Handler(Looper.getMainLooper()).postDelayed(
                            new Runnable() {
                                public void run() {

                                    ff.hideSystemUI(WA.getWindow());

                                    //Toast.makeText(WA.getContext(), datapro.getSharP("fullscreen", "false"), Toast.LENGTH_SHORT).show();

                                }
                            },
                            300);

                }
            }
        });
    }





    private void show_langs_dialog(Activity act ,String value) {
        datapro = new DataProccessor(act);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.download_from);

        //String[] animals = {"horse", "cow", "camel", "sheep", "goat"};
        String[] myArrayList = getResources().getStringArray(R.array.my_webview_runtime_array);

        String[] lang = myArrayList;
        int checkedItem = 0; // cow
        builder.setSingleChoiceItems(lang, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String bits;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bits = TextUtils.join(", ", Build.SUPPORTED_ABIS).contains("64") ? "64-Bit" : "32-Bit";
                } else {
                    bits = "32-Bit";
                }


                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                if(selectedPosition == 0){
                    String appPackageName = null;
                    if(bits == "32-Bit") {
                         appPackageName = "org.xwalk.core"; // getPackageName() from Context or Activity object

                     }
                    else {
                        appPackageName = "org.xwalk.core64"; // getPackageName() from Context or Activity object
                    }
                    Toast.makeText(getApplicationContext(),bits , Toast.LENGTH_LONG).show();

                    try {
                          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                      } catch (android.content.ActivityNotFoundException anfe) {
                          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                      }
                  }


                if(selectedPosition == 1){

                    if(bits == "32-Bit") {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mediafire.com/file/1ec9mstpzpbrbmv/Crosswalk_Project_Runtime_v23.53.589.4_apkpure.com.apk/file"));
                        startActivity(browserIntent);
                    }
                    else {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mediafire.com/file/3f3q4yq594arlvf/Crosswalk_Project_Runtime_v23.53.589.4_64bit_apkpure.com.apk/file"));
                        startActivity(browserIntent);
                    }

                }
                }




        });
        builder.setNegativeButton(R.string.change_webview, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(getContext(), LauncherActivity.class);
                datapro.setSharP("go_to","open_webviewe_dialog");
                startActivity(intent);
                finish();
            }
        });
        myAlertDialog = builder.create();
        //dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        myAlertDialog.setCancelable(false);

        PackageManager pm = getPackageManager();


if(t == null){
    t = new Timer();
}

    t.scheduleAtFixedRate(new TimerTask() {

                              @Override
                              public void run() {

                                  runOnUiThread(() -> {
                                      boolean isInstalled = isPackageInstalled( pm);

                                      if(isInstalled){
                                          //Toast.makeText(this,"tngfnf",Toast.LENGTH_LONG).show();
                                          t.cancel();
                                          //t= null;
                                          hide_dialog(myAlertDialog);
                                          Intent i = new Intent(WebViewActivityCross.this, SplashActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                          startActivity(i);
                                          finish();

                                      } else {
                                          if(!isFinishing()){
                                          myAlertDialog.show();
                                          }

                                      }


                                  });


                              }

                          },
            0,

            1000);


}





  public void hide_dialog(Dialog dialog){
      runOnUiThread(() -> {
          if(myAlertDialog != null && myAlertDialog.isShowing()){
              myAlertDialog.dismiss();
              myAlertDialog =null;
          }





      });

  }









}