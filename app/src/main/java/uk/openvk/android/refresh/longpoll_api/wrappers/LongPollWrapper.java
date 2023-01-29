package uk.openvk.android.refresh.longpoll_api.wrappers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.StatusLine;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.longpoll_api.receivers.LongPollReceiver;
import uk.openvk.android.refresh.user_interface.activities.AppActivity;
import uk.openvk.android.refresh.user_interface.activities.AuthActivity;
//import uk.openvk.android.refresh.user_interface.activities.ConversationActivity;
//import uk.openvk.android.refresh.user_interface.activities.FriendsIntentActivity;
//import uk.openvk.android.refresh.user_interface.activities.GroupIntentActivity;
//import uk.openvk.android.refresh.user_interface.activities.MainSettingsActivity;
//import uk.openvk.android.refresh.user_interface.activities.NewPostActivity;
//import uk.openvk.android.refresh.user_interface.activities.ProfileIntentActivity;
//import uk.openvk.android.refresh.user_interface.activities.QuickSearchActivity;
//import uk.openvk.android.refresh.user_interface.activities.WallPostActivity;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;

/**
 * Created by Dmitry on 29.09.2022.
 */
public class LongPollWrapper {

    public String server;
    public boolean use_https;
    public boolean legacy_mode;
    private String status;
    private uk.openvk.android.refresh.api.models.Error error;
    private Context ctx;
    private Handler handler;
    private String access_token;
    private boolean isActivated;
    private boolean logging_enabled = true;

    private OkHttpClient httpClient = null;


    public LongPollWrapper(Context ctx, boolean use_https) {
        this.ctx = ctx;

        this.use_https = use_https;
        httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
    }

    private String generateUserAgent(Context ctx) {
        String version_name = "";
        String user_agent = "";
        try {
            PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getApplicationContext().getPackageName(), 0);
            version_name = packageInfo.versionName;
        } catch (Exception e) {
            OvkApplication app = ((OvkApplication) ctx.getApplicationContext());
            version_name = app.version;
        } finally {
            user_agent = String.format("OpenVK Refresh/%s (Android %s; SDK %s; %s; %s %s; %s)", version_name,
                    Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.SUPPORTED_ABIS[0], Build.MANUFACTURER, Build.MODEL, System.getProperty("user.language"));
        }
        return user_agent;
    }

    public void log(boolean value) {
        this.logging_enabled = value;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void requireHTTPS(boolean value) {
        this.use_https = value;
    }

    public void longPoll(String lp_server, String key, int ts) {
        this.server = lp_server;
        String url = "";
        url = String.format("%s?act=a_check&key=%s&ts=%s&wait=15", lp_server, key, ts);
        Log.v("OpenVK LPW", String.format("Activating LongPoll via %s...", lp_server));
        final String fUrl = url;
        isActivated = true;
        Thread thread = null;
        Runnable longPollRunnable = new Runnable() {
            private Request request = null;
            StatusLine statusLine = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                request = new Request.Builder()
                        .url(fUrl)
                        .build();
                try {
                    if(isActivated) {
                        Log.v("OpenVK LPW", String.format("LongPoll activated."));
                    }
                    while(isActivated) {
                        Response response = httpClient.newCall(request).execute();
                        response_body = response.body().string();
                        response_code = response.code();
                        if (response_code == 200) {
                            if(logging_enabled) Log.v("OpenVK LPW", String.format("Getting response from %s (%s): [%s]", server, response_code, response_body));
                            sendLongPollMessageToActivity(response_body);
                        } else {
                            if(logging_enabled) Log.v("OpenVK LPW", String.format("Getting response from %s (%s)", server, response_code));
                        }
                        Thread.sleep(2000);
                    }
                } catch(ConnectException | SocketTimeoutException | UnknownHostException ex) {
                    if(logging_enabled) Log.v("OpenVK LPW", String.format("Connection error: %s", ex.getMessage()));
                    try {
                        if(logging_enabled) Log.v("OpenVK LPW", "Retrying in 60 seconds...");
                        Thread.sleep(60000);
                        run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch(SSLProtocolException ex) {
                    if(logging_enabled) Log.v("OpenVK LPW", String.format("Connection error: %s", ex.getMessage()));
                    isActivated = false;
                    if(logging_enabled) Log.v("OpenVK LPW", "LongPoll service stopped.");
                } catch(SSLHandshakeException ex) {
                    if(logging_enabled) Log.v("OpenVK LPW", String.format("Connection error: %s", ex.getMessage()));
                    if(logging_enabled) Log.v("OpenVK LPW", "LongPoll service stopped.");
                    isActivated = false;
                } catch(SSLException ex) {
                    if(logging_enabled) Log.v("OpenVK LPW", String.format("Connection error: %s", ex.getMessage()));
                    Log.v("OpenVK LPW", "LongPoll service stopped.");
                    isActivated = false;
                } catch (Exception ex) {
                    isActivated = false;
                    ex.printStackTrace();
                }
            }
        };
        thread = new Thread(longPollRunnable);
        thread.start();
    }

    public void setProxyConnection(boolean useProxy, String address) {
        try {
            if(useProxy) {
                String[] address_array = address.split(":");
                if (address_array.length == 2) {
                    httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(false).proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(address_array[0],
                                    Integer.valueOf(address_array[1])))).build();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendLongPollMessageToActivity(final String response) {
        handler = new Handler();
        Log.d("OK", "OK! LongPolling 1...");
        Runnable sendLongPoll = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction("uk.openvk.android.refresh.LONGPOLL_RECEIVE");
                intent.putExtra("response", response);
                ctx.sendBroadcast(intent);
                Log.d("OK", "OK! LongPolling 2...");
            }
        };
        handler.post(sendLongPoll);
    }

    public void updateCounters(final OvkAPIWrapper ovk) {
        Thread thread = null;
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ovk.sendAPIMethod("Account.getCounters");
                try {
                    if(error != null && error.description.length() > 0) {
                        handler.postDelayed(this, 5000);
                    } else {
                        handler.postDelayed(this, 60000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 5000);
    }

    public void keepUptime(final OvkAPIWrapper ovk) {
        handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ovk.sendAPIMethod("Account.setOnline");
                try {
                    if(error != null && error.description.length() > 0) {
                        handler.postDelayed(this, 60000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 2000);
    }
}
