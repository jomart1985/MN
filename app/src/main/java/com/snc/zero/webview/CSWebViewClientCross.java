package com.snc.zero.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.internal.AssetHelper;

import com.snc.sample.webview.BuildConfig;
import com.snc.sample.webview.R;
import com.snc.sample.webview.activity.DataProccessor;
import com.snc.zero.log.Logger;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import  org.xwalk.core.XWalkWebResourceResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CSWebViewClientCross extends XWalkResourceClient {
    DataProccessor datapro;
    private final WebViewAssetLoader assetLoader;
    //private final XWalkRuntimeExtensionLoader assetLoadercross;

    public CSWebViewClientCross(Context context ,XWalkView xwalkView) {
        super(xwalkView);
        datapro = new DataProccessor(context.getApplicationContext());


        if (BuildConfig.FEATURE_WEBVIEW_ASSET_LOADER) {
            File publicDir = new File(context.getApplicationContext().getExternalFilesDir(null), "ext");
            this.assetLoader = new WebViewAssetLoader.Builder()
                    .setDomain(BuildConfig.ASSET_BASE_DOMAIN)
                    .addPathHandler(BuildConfig.RES_PATH, new WebViewAssetLoader.ResourcesPathHandler(context))
                    .addPathHandler(BuildConfig.ASSET_PATH, new WebViewAssetLoader.AssetsPathHandler(context))
                    // .addPathHandler("/ext/", new WebViewAssetLoader.InternalStoragePathHandler(context, EnvUtil.getInternalFilesDir(context, "/ext/")))
                    .addPathHandler("/ext/", new CSWebViewClientCross.ExternalStoragePathHandler(context,publicDir))
                    /* .addPathHandler("/ext/", new WebViewAssetLoader.PathHandler() {
                         @Nullable
                         @Override
                         public WebResourceResponse handle(@NonNull String path) {
                             return new WebResourceResponse("text/plain", "utf-8", null);
                         }
                     })*/

                    //.addPathHandler("/ext/", new WebViewAssetLoader.InternalStoragePathHandler(context, publicDir))
                    .build();
        }


    }

    @Override
    public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
        Uri uri = request.getUrl();

        if (BuildConfig.FEATURE_WEBVIEW_ASSET_LOADER) {
            //return this.assetLoader.shouldInterceptRequest(request.getUrl());
        }
        return super.shouldInterceptLoadRequest(view, request);
    }


    @Override
    public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
        return false;

    }
    @Override
    public void onLoadStarted(XWalkView view, String url) {

        super.onLoadStarted(view, url);


    }
    @Override
    public void onLoadFinished(XWalkView view, String url) {
        super.onLoadFinished(view, url);

        datapro.setSharP("current_page",url);
       // Toast.makeText(view.getContext(), url,Toast.LENGTH_LONG).show();


    }
    @Override
    public void onProgressChanged(XWalkView view, int progressInPercent) {
        super.onProgressChanged(view, progressInPercent);

        View v = findProgressBarInTopArea(view);
        if (null != v) {
            if (progressInPercent >= 100) {
                v.setVisibility(View.GONE);
            } else {
                ((ProgressBar) v).setProgress(progressInPercent);
                v.setVisibility(View.VISIBLE);
            }
        }


    }
    private View findProgressBarInTopArea(View view) {
        ViewParent parent = view.getParent();
        View v = null;
        while (null != parent) {
            v = ((ViewGroup) parent).findViewById(R.id.webViewProgressBar);
            if (null != v) {
                break;
            }
            parent = parent.getParent();
        }
        return v;
    }

    public class ExternalStoragePathHandler implements WebViewAssetLoader.PathHandler {
        private static final String TAG = "ExternalStoragePathH";

        @NonNull
        private final File mDirectory;

        public ExternalStoragePathHandler(@NonNull Context context, @NonNull File directory) {
            mDirectory = directory;
        }


        @SuppressLint("RestrictedApi")
        @Override
        public WebResourceResponse handle(String path) {
            try {
                File file = AssetHelper.getCanonicalFileIfChild(mDirectory, path);

                InputStream is = AssetHelper.openFile(file);
                String mimeType = AssetHelper.guessMimeType(path);

                Log.e(TAG,"============================:"+ path);

                return new WebResourceResponse(mimeType, null, is);

            } catch (IOException e) {
                Log.e(TAG, "Error opening the requested path: " + path, e);
            }
            return new WebResourceResponse(null, null, null);
        }
    }
}
