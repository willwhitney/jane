package com.willwhitney.jane;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class FoursquareKeyFetcher extends Activity {
    private static final String TAG = "ActivityWebView";

    /**
     * Get these values after registering your oauth app at: https://foursquare.com/oauth/
     */
    public static final String CALLBACK_URL = "http://jane-node-server.herokuapp.com";
    public static final String CLIENT_ID = "YVKWGFTTKUXKI1GZ3V212WBZMOTXBOJS2GHOZ3DD4SX2SNDW";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foursquare_auth);

        String url =
            "https://foursquare.com/oauth2/authenticate" +
                "?client_id=" + CLIENT_ID +
                "&response_type=token" +
                "&redirect_uri=" + CALLBACK_URL;

        // If authentication works, we'll get redirected to a url with a pattern like:
        //
        //    http://YOUR_REGISTERED_REDIRECT_URI/#access_token=ACCESS_TOKEN
        //
        // We can override onPageStarted() in the web client and grab the token out.
        WebView webview = (WebView)findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
                String fragment = "#access_token=";
                int start = url.indexOf(fragment);
                if (start > -1) {
                    // You can use the accessToken for api calls now.
                    String accessToken = url.substring(start + fragment.length(), url.length());

                    Log.v(TAG, "OAuth complete, token: [" + accessToken + "].");

                    Toast.makeText(FoursquareKeyFetcher.this, "Token: " + accessToken, Toast.LENGTH_SHORT).show();

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", accessToken);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
        webview.loadUrl(url);
    }
}