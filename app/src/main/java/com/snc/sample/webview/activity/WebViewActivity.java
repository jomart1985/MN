package com.snc.sample.webview.activity;

import static com.blankj.utilcode.util.StringUtils.getString;

import static com.snc.zero.util.EnvUtil.getInternalFilesDir;

import static org.chromium.base.ThreadUtils.runOnUiThread;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.documentfile.provider.DocumentFile;

import com.snc.sample.webview.BuildConfig;
import com.snc.sample.webview.R;
import com.snc.sample.webview.bridge.AndroidBridge;
import com.snc.sample.webview.webview.WebViewHelper;
import com.snc.zero.activity.BaseActivity;
import com.snc.zero.dialog.DialogBuilder;
import com.snc.zero.log.Logger;
import com.snc.zero.util.AssetUtil;
import com.snc.zero.util.EnvUtil;
import com.snc.zero.util.PackageUtil;
import com.snc.zero.webview.CSDownloadListener;
import com.snc.zero.webview.CSFileChooserListener;
import com.snc.zero.webview.CSWebChromeClient;
import com.snc.zero.webview.CSWebViewClient;


import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.http.Url;

/**
 * WebView Activity
 *
 * @author mcharima5@gmail.com
 * @since 2018
 */
public class WebViewActivity extends BaseActivity
{
    private static final String TAG = WebViewActivity.class.getSimpleName();

    private WebView webview;
    private CSWebChromeClient webChromeClient;
    private CSFileChooserListener webviewFileChooser;
    private String mOutputPath;
    DataProccessor datapro;
    private WebView.HitTestResult webViewHitTestResult;
    private DownloadManager downloadManager;
    private static final int NEW_FOLDER_REQUEST_CODE = 43;
    private  WebViewActivity WA;


    String HTTP_URL = "https://www.google.com";
    public static final int FILEPICKER_PERMISSIONS = 1;
    private static final int CREATE_FILE = 1;
    AlertDialog.Builder builder;
    Bitmap ibitmap;
    public    String loction="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        datapro = new DataProccessor(this);
        WA = this;
        //fix exposed beyond app through Intent.getData()
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());*/


        if (BuildConfig.DEBUG) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        if (!new File(EnvUtil.getInternalFilesDir(getContext(), "public"), "202205091737430060.jpg").exists()) {
            AssetUtil.copyAssetToFile(getContext(),
                    "www/common/img/202205091737430060.jpg",
                    EnvUtil.getInternalFilesDir(getContext(), "public"));
        }

        init();


        datapro.setSharP("SP_WEBVIEW_TYPE", "nocross");
        datapro.setSharP("VERSION_CODE", String.valueOf(datapro.get_verion_code()));
        datapro.setLocale(this, datapro.getSharP("lang","null"));

        set_full_screen();






        /*downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        registerReceiver(onDownloadComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));*/


    }

    @SuppressLint("AddJavascriptInterface")
    private void init() {
        ViewGroup contentView = findViewById(R.id.contentView);
        if (null == contentView) {
            DialogBuilder.with(getActivity())
                    .setMessage("The contentView does not exist.")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                    .show();
            return;
        }

        // add webview
        this.webview = WebViewHelper.addWebView(getContext(), contentView);



        this.webview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
               set_full_screen();
            }
        });









        // options
        //this.webview.getSettings().setSupportMultipleWindows(true);

        // set user-agent
        try {
            String ua = this.webview.getSettings().getUserAgentString();
            if (!ua.endsWith(" ")) {
                ua += " ";
            }
            ua += PackageUtil.getApplicationName(this);
            ua += "/" + PackageUtil.getPackageVersionName(this);
            ua += "." + PackageUtil.getPackageVersionCode(this);
            this.webview.getSettings().setUserAgentString(ua);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, e);
        }

        // set webViewClient
        CSWebViewClient webviewClient = new CSWebViewClient(getContext());
        this.webview.setWebViewClient(webviewClient);

        // set webChromeClient
        this.webChromeClient = new CSWebChromeClient(getContext());
        this.webview.setWebChromeClient(this.webChromeClient);

        // set fileChooser
        //this.webviewFileChooser = new CSFileChooserListener(getContext());
        //this.webChromeClient.setFileChooserListener(this.webviewFileChooser);
        // this.webview.setWebChromeClient(new WebChromeClient());


        // add interface
        this.webview.addJavascriptInterface(new AndroidBridge(webview, this), "Androidd");

        // add download listener
        this.webview.setDownloadListener(new CSDownloadListener(getActivity()));

        this.registerForContextMenu(this.webview);


       /* int REQUEST_CODE = 1;

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_CODE);*/


        // load url
        //WebViewHelper.loadUrl(this.webview, WebViewHelper.getLocalBaseUrl("assets") + "/www/docs/sample/sample.html");
        //WebViewHelper.loadUrl(this.webview, WebViewHelper.getLocalBaseUrl("assets") + "/www/docs/image-provider/image-provider.html");
        //WebViewHelper.loadUrl(this.webview, WebViewHelper.getLocalBaseUrl("assets") + "/www/docs/qrcode-reader/index.html");
        //WebViewHelper.loadUrl(this.webview, "https://snc-project.firebaseapp.com/docs/file/file.html");
        //WebViewHelper.loadUrl(this.webview, "https://www.google.com");
        //WebViewHelper.loadUrl(this.webview, "https://snc-project.firebaseapp.com/docs/google/google-gtm.html");
        //WebViewHelper.loadUrl(this.webview, "https://snc-project.firebaseapp.com/docs/image/image.html");
        //WebViewHelper.loadUrl(this.webview, WebViewHelper.getLocalBaseUrl(this, "assets") + "/www/app/index.html");
        // WebViewHelper.loadUrl(this.webview, WebViewHelper.getLocalBaseUrl(this,"local") + "/www/mn/dir.html");

        // File outFile = getInternalFilesDir(this,"public");
        File outFile = getExternalFilesDir("ext");
        if (outFile == null || !outFile.exists()) {
            outFile = getFilesDir();
        }
        mOutputPath = outFile.getPath();
        // WebViewHelper.loadUrl(this.webview, "file://"+mOutputPath+ "/app/dir.html");
        ///Logger.i(TAG, "---------------" + WebViewHelper.getLocalBaseUrl(this, "loc") + "app/index.html");

        // WebViewHelper.loadUrl(this.webview, "file://"+mOutputPath+ "/app/index.html");
        //WebViewHelper.loadUrl(this.webview, WebViewHelper.getLocalBaseUrl(this ,"loc")+"app/index.html");


       // this.webview.loadUrl(datapro.getSharP("current_page","file:///android_asset/www/app/index.html"));
        //Toast.makeText(this, "file:///"+mOutputPath+"/app/index.html",Toast.LENGTH_LONG).show();

        this.webview.loadUrl(datapro.getSharP("current_page","file:///"+mOutputPath+"/app/index.html"));




    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                    ContextMenu.ContextMenuInfo contextMenuInfo) {
        super.onCreateContextMenu(contextMenu, view, contextMenuInfo);


        String[] PERMISSIONS = {
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        };



        webViewHitTestResult = webview.getHitTestResult();

        /*MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lovely_context, menu);*/

        if (webViewHitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                webViewHitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            contextMenu.add(0, 0, 0, R.string.Download_image);

            // Toast.makeText(this,"Image.",Toast.LENGTH_LONG).show();
        }

        if (webViewHitTestResult.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE ||
                webViewHitTestResult.getType() == WebView.HitTestResult.ANCHOR_TYPE) {

            //Toast.makeText(this,"link.",Toast.LENGTH_LONG).show();

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
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_TITLE, filename);
                startActivityForResult(intent, 1);


            }
            }



        if (item.getItemId() == 1) {
            Toast.makeText(this, String.valueOf(webViewHitTestResult.getExtra()), Toast.LENGTH_LONG).show();
            ClipboardManager clipboard = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("copy", webViewHitTestResult.getExtra());
            clipboard.setPrimaryClip(clip);

        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(resultCode != RESULT_CANCELED){
            if (resultCode == RESULT_OK) {
                if (requestCode == 1) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                        try {

                            datapro = new DataProccessor(getContext());
                            Uri currentUri =
                                    data.getData();
                            String img_src = datapro.getSharP("img_src", "null");
                            String someFilepath = img_src;
                            String extension = someFilepath.substring(someFilepath.lastIndexOf("."));

                            URL url = new URL(img_src);


                            //HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                            //InputStream input = url.openStream();
                            //InputStream input =  urlConnection.getInputStream();

                            //URLConnection connection = url.openConnection();
                            //connection.setConnectTimeout(1000);
                            //InputStream input = connection.getInputStream();

                                HttpsURLConnection connection = null;

                                connection = (HttpsURLConnection) url.openConnection();
                                connection.setConnectTimeout(1000);
                                connection.connect();
                                InputStream input = connection.getInputStream();
                               // InputStream input = downloadUrl(img_src);



                            ParcelFileDescriptor pfd =
                                    WA.getContentResolver().
                                            openFileDescriptor(currentUri, "w");

                            FileOutputStream out =
                                    new FileOutputStream(
                                            pfd.getFileDescriptor());

                            OutputStream output = out;

                            byte[] buffer = new byte[1024];
                            int bytesRead = 0;
                            while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                                output.write(buffer, 0, bytesRead);
                            }

                            Toast.makeText(WA, R.string.download_image_done, Toast.LENGTH_LONG).show();


                        } catch (Exception e) {
                            e.printStackTrace();

                            Toast.makeText(WA,  R.string.download_image_fail, Toast.LENGTH_LONG).show();
                        }


                    }

        });
        }
}
}



}







    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("IntentReset")
    private void createAndSaveFile(String url) {
        datapro = new DataProccessor(this);
        datapro.setSharP("img_src", url);


        String filename = url.substring(url.lastIndexOf("/") + 1);

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        //intent.putExtra(Intent., filename);

        //sendBroadcast(intent);
        startActivityForResult(intent, 40);





    }






    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        set_full_screen();
    }




    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();     // Line 475
        return conn.getInputStream();
    }



    public void set_full_screen() {
        AndroidBridge ff = new AndroidBridge(this.webview, this);
        // ff.toggFull("dont_save");
        if(!isFinishing()) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (datapro.getSharP("fullscreen", "false").equals("true")) {

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
    }




    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                System.out.println("getPath() uri: " + uri.toString());
                System.out.println("getPath() uri authority: " + uri.getAuthority());
                System.out.println("getPath() uri path: " + uri.getPath());

                // ExternalStorageProvider
                if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    System.out.println("getPath() docId: " + docId + ", split: " + split.length + ", type: " + type);

                    // This is for checking Main Memory
                    if ("primary".equalsIgnoreCase(type)) {
                        if (split.length > 1) {
                            return Environment.getExternalStorageDirectory() + "/" + split[1] + "/";
                        } else {
                            return Environment.getExternalStorageDirectory() + "/";
                        }
                        // This is for checking SD Card
                    } else {
                        return "storage" + "/" + docId.replace(":", "/");
                    }

                }
            }
        }
        return null;
    }





    public void outputToFile(String text ,File file) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        try {
            File logFile = new File(file, "abc.txt");
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }







    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(null,src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            input.close();
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




    public void savebitmap(Bitmap bmp, File path) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        File f = new File(path
                + File.separator + "foooo.png");
        //f.mkdirs();
        //f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();

        Toast.makeText(getContext(), path.toString(), Toast.LENGTH_SHORT).show();

    }



    private void downloadImage(String imageUrl) {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);
    }


    public String getFilePathFromUri(Uri uri) {
        String filePath = null;
        if ("content".equals(uri.getScheme())) {
            String[] filePathColumn = { MediaStore.MediaColumns.DATA };
            ContentResolver contentResolver = getContentResolver();

            Cursor cursor = contentResolver.query(uri, filePathColumn, null,
                    null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        } else if ("file".equals(uri.getScheme())) {
            filePath = new File(uri.getPath()).getAbsolutePath();
        }
        return filePath;
    }

    private void saveAsJpeg(Bitmap bitmapImage) {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory,"imageName.jpg");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                    @SuppressLint("Range") String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    File file = new File(getFilePathFromUri(Uri.parse(uriString)));
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                        saveAsJpeg(bitmap);
                    } catch (FileNotFoundException e) {
                        // cant save
                    }
                } else {
                    // downloadFailed, show toast or something..
                }
            }
        }
    };



















    public static String getRealPathFromUri(Context context, Uri uri) {

        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= 19) { // api >= 19
            return getRealPathFromUriAboveApi19(context, uri);
        } else { // api < 19
            return getRealPathFromUriBelowAPI19(context, uri);
        }
    }

    /**
     * التكيف أدناه api19 (باستثناء api19) ، والحصول على المسار المطلق للصورة وفقا ل uri
     *
     * @ سياق سياق كائن
     *param uri صورة أوري
     * @ العودة إذا كانت الصورة المقابلة لـ Uri موجودة ، فقم بإرجاع المسار المطلق للصورة ، وإلا فستكون خالية
     */
    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            data = getDataColumn(context, uri, null, null);
        }
        return data;
    }

    /**
     * التكيف مع api19 وما فوق ، احصل على المسار المطلق للصورة وفقًا لـ uri
     *
     * @ سياق سياق كائن
     *param uri صورة أوري
     * @ العودة إذا كانت الصورة المقابلة لـ Uri موجودة ، فقم بإرجاع المسار المطلق للصورة ، وإلا فستكون خالية
     */
    @SuppressLint("NewApi")
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // إذا كان نوع مستند uri ، تتم معالجته بواسطة معرف المستند
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                final String[] divide = documentId.split(":");
                final String type = divide[0];
                Uri mediaUri = null;
                if ("image".equals(type)) {
                    mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    return null;
                }
                String selection = BaseColumns._ID + "=?";
                String[] selectionArgs = {divide[1]};
                filePath = getDataColumn(context, mediaUri, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }else if(isExternalStorageDocument(uri)) {
                String [] split = documentId.split(":");
                if(split.length >= 2) {
                    String type = split[0];
                    if("primary".equalsIgnoreCase(type)) {
                        filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
            }
        } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())){

                    filePath = getDataColumn(context, uri, null, null);
        } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {

            filePath = uri.getPath();
        }
        return filePath;
    }

    /**
     * احصل على عمود _data في جدول قاعدة البيانات ، والذي يعرض مسار الملف المقابل لـ Uri
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }





    @Override
    public void onResume(){
        super.onResume();
        // put your code here...
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            this.webview.evaluateJavascript("if (typeof afterPrint === 'function') { afterPrint();}", null);
        } else {
            this.webview.loadUrl("if (typeof afterPrint === 'function') { afterPrint();}");
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            this.webview.evaluateJavascript("$(\"#loading\").hide();", null);
        } else {
            this.webview.loadUrl("$(\"#loading\").hide();");
        }

    }

    @Override
    public void onBackPressed() {
        if(webview.canGoBack()) {
            webview.goBack();
        } else {
            super.onBackPressed();
        }
    }






}



