package com.shutup.cc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String flag = "openUrl://";
    @InjectView(R.id.info)
    TextView mInfo;
    @InjectView(R.id.webView)
    WebView mWebView;

    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initWebView();
        initSocket();
    }

    private void initWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    private void initSocket() {
        try {
            socket = IO.socket("http://104.224.175.47:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: connect");
            }

        }).on("url message", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                final String msg = (String) args[0];
                Log.d(TAG, "call: " + msg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (msg.startsWith(flag)) {
                            String urlStr = msg.replaceFirst(flag,"");
                            mInfo.setText(urlStr);
                            mWebView.loadUrl(urlStr);
                        }
                    }
                });
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: disconnect");
            }

        });
        socket.connect();
    }
}
