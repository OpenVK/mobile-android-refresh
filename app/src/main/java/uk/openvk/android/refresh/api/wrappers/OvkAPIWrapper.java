package uk.openvk.android.refresh.api.wrappers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.droidparts.util.Strings;

import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.Error;
import uk.openvk.android.refresh.user_interface.activities.AppActivity;
import uk.openvk.android.refresh.user_interface.activities.AuthActivity;

public class OvkAPIWrapper {
    public String server;
    public boolean proxy_connection;
    public String proxy_type;
    private String status;
    public Error error;
    private Context ctx;
    private Handler handler;
    private String access_token;

    private OkHttpClient httpClient = null;
    private boolean logging_enabled = true;

    public OvkAPIWrapper(Context ctx) {
        httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).
                writeTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).retryOnConnectionFailure(false)
                .build();
        error = new Error();
        this.ctx = ctx;
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
            user_agent = String.format("OpenVK Modern/%s (Android %s; SDK %s; %s; %s %s; %s)", version_name,
                    Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.CPU_ABI, Build.MANUFACTURER, Build.MODEL, System.getProperty("user.language"));
        }
        return user_agent;
    }

    public OkHttpClient getOkHttp() {
        return httpClient;
    }

    public void log(boolean value) {
        this.logging_enabled = value;
    }

    public void setAccessToken(String token) {
        this.access_token = token;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void authorize(String username, String password) {
        error.description = "";
        String url = "";
        url = String.format("http://%s/token?username=%s&password=%s&grant_type=password&2fa_supported=1", server, username, Strings.urlEncode(password));
        if (logging_enabled)
            Log.v("OpenVK API", String.format("Connecting to %s...", server));
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            int response_code = 0;
            boolean isHttps = false;
            private String response_body = "";
            @Override
            public void run() {
                request = new Request.Builder()
                        .url(fUrl)
                        .addHeader("User-Agent", generateUserAgent(ctx)).build();
                try {
                    Response response = httpClient.newCall(request).execute();
                    response_body = response.body().string();
                    response_code = response.code();
                    if(logging_enabled) Log.v("OpenVK API", String.format("Connected (%d)", response_code));
                    if (response_body.length() > 0) {
                        if(logging_enabled) Log.v("OpenVK API", String.format("Connected (%d)", response_code));
                        if (response_code == 400) {
                            sendMessage(HandlerMessages.INVALID_USERNAME_OR_PASSWORD, response_body);
                        } else if (response_code == 401) {
                            sendMessage(HandlerMessages.TWOFACTOR_CODE_REQUIRED, response_body);
                        } else if(response_code == 404) {
                            sendMessage(HandlerMessages.NOT_OPENVK_INSTANCE, response_body);
                        } else if (response_code == 200) {
                            sendMessage(HandlerMessages.AUTHORIZED, response_body);
                        } else {
                            sendMessage(HandlerMessages.UNKNOWN_ERROR, response_body);
                        }
                    };
                } catch (UnknownHostException | ConnectException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                } catch (SocketTimeoutException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.CONNECTION_TIMEOUT, error.description);
                } catch(SSLException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                } catch(ProtocolException | SocketException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.UNKNOWN_ERROR, error.description);
                } catch (OutOfMemoryError | Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void authorize(String username, String password, String code) {
        error.description = "";
        String url = "";
        url = String.format("http://%s/token?username=%s&password=%s&code=%s&grant_type=password&2fa_supported=1", server, username, Strings.urlEncode(password), code);
        if (logging_enabled)
            Log.v("OpenVK API", String.format("Connecting to %s...", server));
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            int response_code = 0;
            boolean isHttps = false;
            private String response_body = "";
            @Override
            public void run() {
                request = new Request.Builder()
                        .url(fUrl)
                        .addHeader("User-Agent", generateUserAgent(ctx)).build();
                try {
                    Response response = httpClient.newCall(request).execute();
                    response_body = response.body().string();
                    response_code = response.code();
                    if(logging_enabled) Log.v("OpenVK API", String.format("Connected (%d)", response_code));
                    if (response_body.length() > 0) {
                        if(logging_enabled) Log.v("OpenVK API", String.format("Connected (%d)", response_code));
                        if (response_code == 400) {
                            sendMessage(HandlerMessages.INVALID_USERNAME_OR_PASSWORD, response_body);
                        } else if (response_code == 401) {
                            sendMessage(HandlerMessages.TWOFACTOR_CODE_REQUIRED, response_body);
                        } else if(response_code == 404) {
                            sendMessage(HandlerMessages.NOT_OPENVK_INSTANCE, response_body);
                        } else if (response_code == 200) {
                            sendMessage(HandlerMessages.AUTHORIZED, response_body);
                        } else {
                            sendMessage(HandlerMessages.UNKNOWN_ERROR, response_body);
                        }
                    };
                } catch (UnknownHostException | ConnectException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                } catch (SocketTimeoutException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.CONNECTION_TIMEOUT, error.description);
                } catch(SSLException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                } catch(ProtocolException | SocketException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.UNKNOWN_ERROR, error.description);
                } catch (OutOfMemoryError | Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    private void sendMessage(int message, String response) {
        Message msg = new Message();
        msg.what = message;
        Bundle bundle = new Bundle();
        bundle.putString("response", response);
        msg.setData(bundle);
        if(ctx.getClass().getSimpleName().equals("AuthActivity")) {
            ((AuthActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("AppActivity")) {
            ((AppActivity) ctx).handler.sendMessage(msg);
        }
    }
}
