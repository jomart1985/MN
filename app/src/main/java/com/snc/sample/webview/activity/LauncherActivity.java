package com.snc.sample.webview.activity;

import static com.snc.zero.util.EnvUtil.getInternalFilesDir;

import static java.lang.String.valueOf;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.hzy.lib7z.Z7Extractor;
import com.snc.sample.webview.BuildConfig;
import com.snc.sample.webview.R;
import com.snc.sample.webview.webview.WebViewHelperCross;
import com.snc.zero.activity.BaseActivity;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.xwalk.core.XWalkInitializer;
import org.xwalk.core.XWalkUpdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import  org.apache.commons.compress.archivers.sevenz.SevenZFile;




public class LauncherActivity extends BaseActivity
        implements
        XWalkInitializer.XWalkInitListener,
        XWalkUpdater.XWalkUpdateListener
{

    private String mOutputPath;
    private ProgressDialog mProgressDialog;
    private ExecutorService mExecutor;
    private LauncherActivity mainActivity;
    DataProccessor datapro;

    private XWalkInitializer mXWalkInitializer;
    private XWalkUpdater mXWalkUpdater;

    Bundle b;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            savedInstanceState.clear();
        }
        setContentView(R.layout.activity_launcher);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
        }

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            mXWalkInitializer = new XWalkInitializer(this, this);
            mXWalkInitializer.initAsync();


        }
        else{
            try {
                redirect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


    private void redirect() throws IOException {
        mProgressDialog = new ProgressDialog(this);
        mExecutor = Executors.newSingleThreadExecutor();
        datapro = new DataProccessor(this);

        String VERSION_CODE_STRING = datapro.getSharP("VERSION_CODE","0");
        int VERSION_CODE_INT  = Integer.parseInt(VERSION_CODE_STRING);
        File checkoutFile = new File(getExternalFilesDir(null), "ext");
        //File checkoutFile = new File(getInternalFilesDir(this), "ext");



         datapro.setLocale(this, datapro.getSharP("lang","null"));
        if(datapro.getSharP("go_to","null").equals("open_lang_dialog")) {

                 show_langs_dialog(this);

        }
        else
        if(datapro.getSharP("go_to","null").equals("open_extract_dialog")) {

            File outFile = getExternalFilesDir("ext");
            mOutputPath = outFile.getPath();
            doExtractFile(this);

        }
        else
        if(datapro.getSharP("go_to","null").equals("open_webviewe_dialog")) {

            show_webview_dialog(this);

        }
       else if(datapro.getSharP("lang","null") == "null"){

            show_langs_dialog(this);
        }else
        if (checkoutFile == null || !checkoutFile.exists() || VERSION_CODE_INT != datapro.get_verion_code() ) {
            File outFile = getExternalFilesDir("ext");
            //File outFile = getInternalFilesDir(this,"ext");
           /* if (outFile == null || !outFile.exists()) {
                outFile = getFilesDir();
            }*/

            mOutputPath = outFile.getPath();
            doExtractFile(this);
        }
        else if(datapro.getSharP("SP_WEBVIEW_TYPE", "null").equals("null")){
            if(android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                show_webview_dialog(this);
            }
            else{
                Intent i = new Intent(LauncherActivity.this, WebViewActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                finish();
            }

        }
        else {

           if(datapro.getSharP("SP_WEBVIEW_TYPE", "null").equals("nocross")){
               Intent i = new Intent(LauncherActivity.this, WebViewActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
               startActivity(i);
               finish();

            }
          else{
               Intent i = new Intent(LauncherActivity.this, WebViewActivityCross.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
               startActivity(i);
               finish();
          }

            //Toast.makeText(this.getContext(), "done", Toast.LENGTH_LONG).show();


        }





        //throw new RuntimeException("Test Crash"); // Force a crash



        //Intent i = new Intent(LauncherActivity.this, WebViewActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        //startActivity(i);
        //finish();
    }

    private void show_webview_dialog(Activity act) {
        datapro = new DataProccessor(act);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(act.getString(R.string.webview_choose));

        //String[] animals = {"horse", "cow", "camel", "sheep", "goat"};
        String[] myArrayList = getResources().getStringArray(R.array.my_webview_array);

        String[] lang = myArrayList;
        int checkedItem = datapro.get_currnt_option("show_webview_dialog_choos",0);
        builder.setSingleChoiceItems(lang, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                // Toast.makeText(getApplicationContext(),valueOf(selectedPosition)  , Toast.LENGTH_LONG).show();
                if(selectedPosition == 0){

                    datapro.set_currnt_option("show_webview_dialog_choos",0);

                    Intent i = new Intent(LauncherActivity.this, WebViewActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(i);
                    finish();
                }

                if(selectedPosition == 1){

                    datapro.set_currnt_option("show_webview_dialog_choos",1);
                    Intent i = new Intent(LauncherActivity.this, WebViewActivityCross.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(i);
                    finish();
                }




                // datapro.reload(act);
                //datapro.restartActivity(act);
                datapro.setSharP("go_to","null");


            }



        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        //dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        dialog.setCancelable(false);
        dialog.show();

    }
    private void show_langs_dialog(Activity act) {
        datapro = new DataProccessor(act);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(" اختر اللغة:\n Choose languages:");

        //String[] animals = {"horse", "cow", "camel", "sheep", "goat"};
        String[] myArrayList = getResources().getStringArray(R.array.my_string_array);

        String[] lang = myArrayList;
        int checkedItem = datapro.get_currnt_option("show_langs_dialog_choos",0); // cow
        builder.setSingleChoiceItems(lang, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
               // Toast.makeText(getApplicationContext(),valueOf(selectedPosition)  , Toast.LENGTH_LONG).show();
                if(selectedPosition == 0){

                    datapro.setLocale(act, "ar");
                    datapro.setSharP("lang", "ar");
                    datapro.set_currnt_option("show_langs_dialog_choos",0);

                }

                if(selectedPosition == 1){

                    datapro.setLocale(act, "en");
                    datapro.setSharP("lang", "en");
                    datapro.set_currnt_option("show_langs_dialog_choos",1);
                }



                //b.clear();
                datapro.setSharP("go_to","null");
                datapro.restartActivity(act);
            }
        });
       // builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        //dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        dialog.setCancelable(false);
        dialog.show();

    }

    public static File getAssetFile(Context context, String asset_name, String name)
            throws IOException {
        File cacheFile = new File(context.getCacheDir(), name);
        try {
            InputStream inputStream = context.getAssets().open(asset_name);
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new IOException("Could not open file" + asset_name, e);
        }
        return cacheFile;
    }


    private void doExtractFile(Activity act) throws IOException {
    /*    File f= new File("file:///android_asset/","app.7z");
        //InputStream is = getAssets().open("app.7z");
        String filename = "app.7z";
        try {
            SevenZFile sevenZFile = new SevenZFile(f);
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            File cache_dir = new File(getCacheDir() + "/" + filename.substring(0, filename.indexOf(".")));
            if (!cache_dir.exists()) cache_dir.mkdirs();
            while (entry != null) {
                FileOutputStream out = new FileOutputStream(cache_dir + "/" + entry.getName());
                byte[] content = new byte[(int) entry.getSize()];
                sevenZFile.read(content, 0, content.length);
                out.write(content);
                out.close();
                entry = sevenZFile.getNextEntry();
            }
            sevenZFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

*/


        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mExecutor.submit(() ->
                Z7Extractor.extractAsset(getAssets(), "app.7z",
                        mOutputPath, new UnzipCallback() {
                            @Override
                            public void onProgress(String name, long size) {
                                runOnUiThread(() -> mProgressDialog.setMessage(act.getString(R.string.extract_pro)+"\n name: "
                                        + name + "\nsize: " + size));
                            }

                            @Override
                            public void onError(int errorCode, String message) {
                                runOnUiThread(() -> {
                                    mProgressDialog.dismiss();
                                });
                            }

                            @Override
                            public void onSucceed() {
                                runOnUiThread(() -> {
                                    mProgressDialog.dismiss();

                                    datapro.setSharP("VERSION_CODE", String.valueOf(BuildConfig.VERSION_CODE));

                                    datapro.restartActivity(act);
                                    datapro.setSharP("go_to","null");
                                    Toast.makeText(act, R.string.extract_done, Toast.LENGTH_LONG).show();



                                });
                            }
                        }));


    }








    @Override
    protected void onResume() {
            super.onResume();

           // mXWalkInitializer.initAsync();

        }
    @Override
    public void onXWalkInitStarted() {

        //Toast.makeText(this.getContext(), "ppppppppp", Toast.LENGTH_SHORT).show();
        //redirect();

    }
    @Override
    public void onXWalkInitCompleted() {


        //Toast.makeText(this.getContext(), "ppppppppp", Toast.LENGTH_SHORT).show();
        try {
            redirect();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    @Override
    public void onXWalkInitCancelled() {

    }
    @Override
    public void onXWalkInitFailed() {
        //Toast.makeText(this.getContext(), "ppppppppp", Toast.LENGTH_SHORT).show();
        try {
            redirect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onXWalkUpdateCancelled() {

    }




}

