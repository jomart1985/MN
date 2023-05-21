package com.snc.sample.webview.activity;

import static java.lang.String.valueOf;
import static java.security.AccessController.getContext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.snc.sample.webview.BuildConfig;

import java.util.Locale;

public class DataProccessor {
    private  Context context;
    DataProccessor datapro;

    public DataProccessor(Context context) {

        this.context = context;
    }

    public  void setSharP(String type, String value) {

        SharedPreferences.Editor editor = context.getSharedPreferences("MyPrefsFile", context.MODE_PRIVATE).edit();
        editor.putString(type, value);
        //editor.putInt("idName", 12);
        editor.apply();
    }


    public String getSharP(String type, String def) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefsFile", context.MODE_PRIVATE);
        //int idName = prefs.getInt("idName", 0); //0 is the default value.
        return prefs.getString(type, def);//"No name defined" is the default value.
    }

    public  void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        }
        else{
            Resources res = activity.getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = new Configuration(res.getConfiguration());
            conf.locale = locale;
            res.updateConfiguration(conf, dm);
            //activity.recreate();

        }
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        //Toast.makeText(activity,locale.toString() , Toast.LENGTH_LONG).show();

    }

    public void reload(Activity act) {
        Intent intent = act.getIntent();
        act.overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        act.finish();
        act.overridePendingTransition(0, 0);
        act.startActivity(intent);
    }

    public  void restartActivity(Activity activity){

        Intent intent = activity.getIntent();
        activity.overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.finish();
        activity.overridePendingTransition(0, 0);
        activity.startActivity(intent);

    }

    public int get_verion_code(){
       return BuildConfig.VERSION_CODE;
    }

    public String get_verion_name(){
        return  BuildConfig.VERSION_NAME;
    }


    public void set_currnt_option(String value, int key){

        setSharP(value, String.valueOf(key));
    }

    public int get_currnt_option(String value ,int def){

       return Integer.valueOf(getSharP(value, String.valueOf(def)));
    }

}
