package uk.openvk.android.refresh.api.wrappers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import org.droidparts.util.Strings;

import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.StatusLine;
import uk.openvk.android.refresh.BuildConfig;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.Error;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;
import uk.openvk.android.refresh.ui.core.activities.AuthActivity;
import uk.openvk.android.refresh.ui.core.activities.ConversationActivity;
import uk.openvk.android.refresh.ui.core.activities.FriendsIntentActivity;
import uk.openvk.android.refresh.ui.core.activities.GroupIntentActivity;
import uk.openvk.android.refresh.ui.core.activities.MainSettingsActivity;
import uk.openvk.android.refresh.ui.core.activities.NewPostActivity;
import uk.openvk.android.refresh.ui.core.activities.ProfileIntentActivity;
import uk.openvk.android.refresh.ui.core.activities.QuickSearchActivity;
import uk.openvk.android.refresh.ui.core.activities.WallPostActivity;

public class OvkAPIWrapper {

    public String server;
    private boolean use_https;
    public boolean proxy_connection;
    public String proxy_type;
    private String status;
    public Error error;
    private Context ctx;
    private String access_token;
    private OkHttpClient httpClient = null;
    private boolean logging_enabled = true; // default for beta releases
    private String client_name = "openvk_refresh_android";

    public OvkAPIWrapper(Context ctx) {
        if(BuildConfig.BUILD_TYPE.equals("release")) {
            logging_enabled = false;
        }
        this.ctx = ctx;
        error = new Error();
        try {
            httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setProxyConnection(boolean useProxy, String address) {
        try {
            if(useProxy) {
                String[] address_array = address.split(":");
                if (address_array.length == 2) {
                    if (use_https) {
                        httpClient = new OkHttpClient.Builder()
                                .connectTimeout(30, TimeUnit.SECONDS)
                                .writeTimeout(15, TimeUnit.SECONDS)
                                .readTimeout(30, TimeUnit.SECONDS)
                                .retryOnConnectionFailure(false).build();
                    } else {
                        httpClient = new OkHttpClient.Builder()
                                .connectTimeout(30, TimeUnit.SECONDS)
                                .writeTimeout(15, TimeUnit.SECONDS)
                                .readTimeout(30, TimeUnit.SECONDS)
                                .retryOnConnectionFailure(false)
                                .followSslRedirects(false).build();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String generateUserAgent(Context ctx) {
        String version_name = "";
        String user_agent;
        try {
            PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.
                    getApplicationContext().getPackageName(), 0);
            version_name = packageInfo.versionName;
        } catch (Exception e) {
            OvkApplication app = ((OvkApplication) ctx.getApplicationContext());
            version_name = app.version;
        } finally {
            user_agent = String.format("OpenVK Legacy/%s (Android %s; SDK %s; %s; %s %s; %s)",
                    version_name,
                    Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.SUPPORTED_ABIS[0],
                    Build.MANUFACTURER, Build.MODEL, System.getProperty("user.language"));
        }
        return user_agent;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void log(boolean value) {
        this.logging_enabled = value;
    }

    public void authorize(String username, String password) {
        error.description = "";
        String url;
        url = String.format("http://%s/token?username=%s&password=%s&grant_type=password&client_name=%s&2fa_supported=1",
                server, Strings.urlEncode(username), Strings.urlEncode(password), client_name);
        if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s... (Secured)", server));
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() throws OutOfMemoryError {
                try {
                    request = new Request.Builder()
                            .url(fUrl)
                            .addHeader("User-Agent", generateUserAgent(ctx)).build();
                    try {
                        Response response = httpClient.newCall(request).execute();
                        response_body = Objects.requireNonNull(response.body()).string();
                        response_code = response.code();
                        if (response_body.length() > 0) {
                            if (logging_enabled)
                                Log.v("OpenVK API", String.format("Connected (%d)", response_code));
                            if (response_code == 400) {
                                sendMessage(HandlerMessages.OVKAPI_INVALID_USERNAME_OR_PASSWORD, response_body);
                            } else if (response_code == 401) {
                                sendMessage(HandlerMessages.OVKAPI_TWOFACTOR_CODE_REQUIRED, response_body);
                            } else if (response_code == 404) {
                                sendMessage(HandlerMessages.OVKAPI_NOT_OPENVK_INSTANCE, response_body);
                            } else if (response_code == 200) {
                                sendMessage(HandlerMessages.OVKAPI_AUTHORIZED, response_body);
                            } else if (response_code == 502) {
                                sendMessage(HandlerMessages.OVKAPI_INSTANCE_UNAVAILABLE, response_body);
                            } else if (response_code == 503) {
                                sendMessage(HandlerMessages.OVKAPI_INSTANCE_UNAVAILABLE, response_body);
                            } else {
                                sendMessage(HandlerMessages.OVKAPI_UNKNOWN_ERROR, response_body);
                            }
                        }
                    } catch (ProtocolException | UnknownHostException | ConnectException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketTimeoutException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_CONNECTION_TIMEOUT, error.description);
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendMessage(HandlerMessages.OVKAPI_UNKNOWN_ERROR, "");
                    }
                } catch (Exception ex) {
                    sendMessage(HandlerMessages.OVKAPI_UNKNOWN_ERROR, "");
                    ex.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void authorize(String username, String password, String code) {
        error.description = "";
        String url;
        url = String.format("http://%s" +
                "/token?username=%s&password=%s&grant_type=password&code=%s&" +
                "client_name=%s&2fa_supported=1", server, Strings.urlEncode(username),
                Strings.urlEncode(password), code, client_name);
        if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s...", server));
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            StatusLine statusLine = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                try {
                    request = new Request.Builder()
                            .url(fUrl)
                            .addHeader("User-Agent", generateUserAgent(ctx)).build();
                    try {
                        Response response = httpClient.newCall(request).execute();
                        response_body = Objects.requireNonNull(response.body()).string();
                        response_code = response.code();
                        if (response_body.length() > 0) {
                            if (logging_enabled)
                                Log.v("OpenVK API", String.format("Connected (%d)", response_code));
                            if (response_code == 400) {
                                sendMessage(HandlerMessages.OVKAPI_INVALID_USERNAME_OR_PASSWORD, response_body);
                            } else if (response_code == 401) {
                                sendMessage(HandlerMessages.OVKAPI_TWOFACTOR_CODE_REQUIRED, response_body);
                            } else if (response_code == 404) {
                                sendMessage(HandlerMessages.OVKAPI_NOT_OPENVK_INSTANCE, response_body);
                            } else if (response_code == 200) {
                                sendMessage(HandlerMessages.OVKAPI_AUTHORIZED, response_body);
                            } else if (response_code == 503) {
                                sendMessage(HandlerMessages.OVKAPI_INSTANCE_UNAVAILABLE, response_body);
                            } else {
                                sendMessage(HandlerMessages.OVKAPI_UNKNOWN_ERROR, response_body);
                            }
                        }
                    } catch (ProtocolException | ConnectException
                             | javax.net.ssl.SSLProtocolException
                             | UnknownHostException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketTimeoutException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_CONNECTION_TIMEOUT, error.description);
                    } catch (javax.net.ssl.SSLException | OutOfMemoryError e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_BROKEN_SSL_CONNECTION, error.description);
                    } catch (Exception e) {
                        sendMessage(HandlerMessages.OVKAPI_UNKNOWN_ERROR, "");
                        e.printStackTrace();
                    }
                } catch (Exception ex) {
                    sendMessage(HandlerMessages.OVKAPI_UNKNOWN_ERROR, "");
                    ex.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void sendAPIMethod(final String method, final String args, final String where) {
        error.description = "";
        String url;
        url = String.format("http://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
        if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s...\r\nMethod: %s" +
                "\r\nArguments: %s\r\nWhere: %s", server, method, args, where));
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            StatusLine statusLine = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                try {
                    request = new Request.Builder()
                            .url(fUrl)
                            .addHeader("User-Agent", generateUserAgent(ctx)).build();
                    try {
                        Response response = httpClient.newCall(request).execute();
                        response_body = Objects.requireNonNull(response.body()).string();
                        response_code = response.code();
                        if (response_body.length() > 0) {
                            if (response_code == 200) {
                                if(logging_enabled) Log.v("OpenVK API", String.format("" +
                                        "Getting response from %s (%s): [%s]", server, response_code,
                                        response_body));
                                switch (method) {
                                    case "Account.getProfileInfo":
                                        sendMessage(HandlerMessages.OVKAPI_ACCOUNT_PROFILE_INFO, method,
                                                args, response_body);
                                        break;
                                    case "Account.setOnline":
                                        sendMessage(HandlerMessages.OVKAPI_ACCOUNT_SET_TO_ONLINE, method,
                                                args, response_body);
                                        break;
                                    case "Account.setOffline":
                                        sendMessage(HandlerMessages.OVKAPI_ACCOUNT_SET_TO_OFFLINE, method,
                                                args, response_body);
                                        break;
                                    case "Account.getCounters":
                                        sendMessage(HandlerMessages.OVKAPI_ACCOUNT_COUNTERS, method,
                                                args, response_body);
                                        break;
                                    case "Friends.get":
                                        switch (where) {
                                            case "friends_list":
                                                sendMessage(HandlerMessages.OVKAPI_FRIENDS_GET, method,
                                                        args, response_body);
                                                break;
                                            case "profile_counter":
                                                sendMessage(HandlerMessages.OVKAPI_FRIENDS_GET_ALT, method,
                                                        args, response_body);
                                                break;
                                            case "more_friends":
                                                sendMessage(HandlerMessages.OVKAPI_FRIENDS_GET_MORE, method,
                                                        args, response_body);
                                                break;
                                        }
                                        break;
                                    case "Friends.add":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_ADD, method, args,
                                                response_body);
                                        break;
                                    case "Friends.delete":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_DELETE, method, args,
                                                response_body);
                                        break;
                                    case "Friends.areFriends":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_CHECK, method, args,
                                                response_body);
                                        break;
                                    case "Friends.getRequests":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_REQUESTS, method, args,
                                                response_body);
                                        break;
                                    case "Groups.get":
                                        if (where.equals("more_groups")) {
                                            sendMessage(HandlerMessages.OVKAPI_GROUPS_GET_MORE, method, args,
                                                    response_body);
                                        } else {
                                            sendMessage(HandlerMessages.OVKAPI_GROUPS_GET, method, args,
                                                    response_body);
                                        }
                                        break;
                                    case "Groups.getById":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_GET_BY_ID, method, args,
                                                response_body);
                                        break;
                                    case "Groups.search":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_SEARCH, method,
                                                response_body);
                                        break;
                                    case "Groups.join":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_JOIN, method,
                                                response_body);
                                        break;
                                    case "Groups.leave":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_LEAVE, method,
                                                response_body);
                                        break;
                                    case "Likes.add":
                                        sendMessage(HandlerMessages.OVKAPI_LIKES_ADD, method, args,
                                                response_body);
                                        break;
                                    case "Likes.delete":
                                        sendMessage(HandlerMessages.OVKAPI_LIKES_DELETE, method, args,
                                                response_body);
                                        break;
                                    case "Likes.isLiked":
                                        sendMessage(HandlerMessages.OVKAPI_LIKES_CHECK, method, args,
                                                response_body);
                                        break;
                                    case "Messages.getById":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_BY_ID, method, args,
                                                response_body);
                                        break;
                                    case "Messages.send":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_SEND, method, args,
                                                response_body);
                                        break;
                                    case "Messages.delete":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_DELETE, method, args,
                                                response_body);
                                        break;
                                    case "Messages.restore":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_RESTORE, method, args,
                                                response_body);
                                        break;
                                    case "Messages.getConverstaions":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_CONVERSATIONS, method, args,
                                                response_body);
                                        break;
                                    case "Messages.getConverstaionsByID":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_CONVERSATIONS_BY_ID,
                                                method, args, response_body);
                                        break;
                                    case "Messages.getHistory":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_HISTORY, method, args,
                                                response_body);
                                        break;
                                    case "Messages.getLongPollHistory":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_LONGPOLL_HISTORY,
                                                method, args, response_body);
                                        break;
                                    case "Messages.getLongPollServer":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_LONGPOLL_SERVER,
                                                method, args, response_body);
                                        break;
                                    case "Ovk.version":
                                        sendMessage(HandlerMessages.OVKAPI_OVK_VERSION, method, args,
                                                response_body);
                                        break;
                                    case "Ovk.test":
                                        sendMessage(HandlerMessages.OVKAPI_OVK_TEST, method, args,
                                                response_body);
                                        break;
                                    case "Ovk.chickenWings":
                                        sendMessage(HandlerMessages.OVKAPI_OVK_CHICKEN_WINGS, method, args,
                                                response_body);
                                        break;
                                    case "Ovk.aboutInstance":
                                        sendMessage(HandlerMessages.OVKAPI_OVK_ABOUTINSTANCE, method, args,
                                                response_body);
                                        break;
                                    case "Users.getFollowers":
                                        sendMessage(HandlerMessages.OVKAPI_USERS_FOLLOWERS, method, args,
                                                response_body);
                                        break;
                                    case "Users.search":
                                        sendMessage(HandlerMessages.OVKAPI_USERS_SEARCH, method, args,
                                                response_body);
                                        break;
                                    case "Users.get":
                                        switch (where) {
                                            case "profile":
                                                sendMessage(HandlerMessages.OVKAPI_USERS_GET, method, args,
                                                        response_body);
                                                break;
                                            case "account_user":
                                                sendMessage(HandlerMessages.OVKAPI_USERS_GET_ALT, method, args,
                                                        response_body);
                                                break;
                                            case "peers":
                                                sendMessage(HandlerMessages.OVKAPI_USERS_GET_ALT2, method, args,
                                                        response_body);
                                                break;
                                        }
                                        break;
                                    case "Wall.get":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_GET, method, args,
                                                response_body);
                                        break;
                                    case "Wall.getById":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_GET_BY_ID, method, args,
                                                response_body);
                                        break;
                                    case "Wall.post":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_POST, method, args,
                                                response_body);
                                        break;
                                    case "Wall.repost":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_REPOST, method, args,
                                                response_body);
                                        break;
                                    case "Wall.createComment":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_DELETE_COMMENT, method, args,
                                                response_body);
                                        break;
                                    case "Wall.getComment":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_COMMENT, method, args,
                                                response_body);
                                        break;
                                    case "Wall.getComments":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_ALL_COMMENTS, method, args,
                                                response_body);
                                        break;
                                    case "Newsfeed.get":
                                        if (where.equals("more_news")) {
                                            sendMessage(HandlerMessages.OVKAPI_NEWSFEED_GET_MORE, method, args,
                                                    response_body);
                                        } else {
                                            sendMessage(HandlerMessages.OVKAPI_NEWSFEED_GET, method, args,
                                                    response_body);
                                        }
                                        break;
                                    case "Newsfeed.getGlobal":
                                        if (where.equals("more_news")) {
                                            sendMessage(HandlerMessages.OVKAPI_NEWSFEED_GET_MORE_GLOBAL,
                                                    method, args, response_body);
                                        } else {
                                            sendMessage(HandlerMessages.OVKAPI_NEWSFEED_GET_GLOBAL,
                                                    method, args, response_body);
                                        }
                                        break;
                                    case "Polls.addVote":
                                        sendMessage(HandlerMessages.OVKAPI_POLL_ADD_VOTE, method, args,
                                                response_body);
                                        break;
                                    case "Polls.deleteVote":
                                        sendMessage(HandlerMessages.OVKAPI_POLL_DELETE_VOTE, method, args,
                                                response_body);
                                        break;
                                }
                            } else if (response_code == 400) {
                                error = new Error();
                                error.parse(response_body);
                                if(logging_enabled) Log.v("OpenVK API",
                                        String.format("Getting response from %s (%s): [%s / Error code: %d]",
                                                server, response_code, error.description, error.code));
                                if (error.code == 3) {
                                    sendMessage(HandlerMessages.OVKAPI_METHOD_NOT_FOUND, method, args,
                                            error.description);
                                } else if (error.code == 5) {
                                    sendMessage(HandlerMessages.OVKAPI_INVALID_TOKEN, method, args,
                                            error.description);
                                } else if (error.code == 15) {
                                    sendMessage(HandlerMessages.OVKAPI_ACCESS_DENIED, method, args,
                                            error.description);
                                } else if (error.code == 100) {
                                    sendMessage(HandlerMessages.OVKAPI_INVALID_USAGE, method, args,
                                            error.description);
                                }
                            } else if (response_code == 503) {
                                sendMessage(HandlerMessages.OVKAPI_INSTANCE_UNAVAILABLE,
                                        method, args, response_body);
                            } else if (response_code >= 500 && response_code <= 526) {
                                if(logging_enabled) Log.e("OpenVK API",
                                        String.format("Getting response from %s (%s)",
                                                server, response_code));
                                sendMessage(HandlerMessages.OVKAPI_INTERNAL_ERROR, method, "");
                            }
                        }
                    } catch (ConnectException | ProtocolException e) {
                        if(logging_enabled) Log.e("OpenVK API",
                                String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketException e) {
                        if(Objects.requireNonNull(e.getMessage()).contains("ETIMEDOUT")) {
                            if(logging_enabled) Log.e("OpenVK API",
                                    String.format("Connection error: %s", e.getMessage()));
                            error.description = e.getMessage();
                            sendMessage(
                                    HandlerMessages.OVKAPI_CONNECTION_TIMEOUT, method, args, error.description);
                        }
                    } catch (SocketTimeoutException e) {
                        if(logging_enabled) Log.e("OpenVK API",
                                String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_CONNECTION_TIMEOUT, method, args, error.description);
                    } catch (UnknownHostException e) {
                        if(logging_enabled) Log.e("OpenVK API",
                                String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_NO_INTERNET_CONNECTION, method, args, error.description);
                    } catch(javax.net.ssl.SSLException e) {
                        if(logging_enabled) Log.e("OpenVK API",
                                String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_BROKEN_SSL_CONNECTION, error.description);
                    } catch (OutOfMemoryError | Exception e) {
                        sendMessage(HandlerMessages.OVKAPI_UNKNOWN_ERROR, "");
                        e.printStackTrace();
                    }
                } catch (Exception ex) {
                    sendMessage(HandlerMessages.OVKAPI_UNKNOWN_ERROR, "");
                    ex.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void sendAPIMethod(final String method, final String args) {
        error.description = "";
        String url;
        url = String.format("http://%s/method/%s?%s&access_token=%s",
                server, method, args, access_token);
        Log.v("OpenVK API", String.format("Connecting to %s..." +
                "\r\nMethod: %s\r\nArguments: %s", server, method, args));
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            StatusLine statusLine = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                try {
                    request = new Request.Builder()
                            .url(fUrl)
                            .addHeader("User-Agent", generateUserAgent(ctx)).build();
                    try {
                        Response response = httpClient.newCall(request).execute();
                        response_body = Objects.requireNonNull(response.body()).string();
                        response_code = response.code();
                        if (response_body.length() > 0) {
                            if(response_code == 200) {
                                if(logging_enabled) Log.v("OpenVK API",
                                        String.format("Getting response from %s (%s): [%s]",
                                                server, response_code, response_body));
                                switch (method) {
                                    case "Account.getProfileInfo":
                                        sendMessage(HandlerMessages.OVKAPI_ACCOUNT_PROFILE_INFO, method,
                                                args, response_body);
                                        break;
                                    case "Account.setOnline":
                                        sendMessage(HandlerMessages.OVKAPI_ACCOUNT_SET_TO_ONLINE, method,
                                                args, response_body);
                                        break;
                                    case "Account.setOffline":
                                        sendMessage(HandlerMessages.OVKAPI_ACCOUNT_SET_TO_OFFLINE, method,
                                                args, response_body);
                                        break;
                                    case "Account.getCounters":
                                        sendMessage(HandlerMessages.OVKAPI_ACCOUNT_COUNTERS, method,
                                                args, response_body);
                                        break;
                                    case "Friends.get":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_GET, method,
                                                args, response_body);
                                        break;
                                    case "Friends.add":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_ADD, method,
                                                args, response_body);
                                        break;
                                    case "Friends.delete":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_DELETE, method,
                                                args, response_body);
                                        break;
                                    case "Friends.areFriends":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_CHECK, method,
                                                args, response_body);
                                        break;
                                    case "Groups.get":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_GET, method,
                                                args, response_body);
                                        break;
                                    case "Groups.getById":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_GET_BY_ID, method,
                                                args, response_body);
                                        break;
                                    case "Groups.search":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_SEARCH, method,
                                                args, response_body);
                                        break;
                                    case "Groups.join":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_JOIN, method,
                                                args, response_body);
                                        break;
                                    case "Groups.leave":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_LEAVE, method,
                                                args, response_body);
                                        break;
                                    case "Friends.getRequests":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_REQUESTS, method,
                                                args, response_body);
                                        break;
                                    case "Likes.add":
                                        sendMessage(HandlerMessages.OVKAPI_LIKES_ADD, method,
                                                args, response_body);
                                        break;
                                    case "Likes.delete":
                                        sendMessage(HandlerMessages.OVKAPI_LIKES_DELETE, method,
                                                args, response_body);
                                        break;
                                    case "Likes.isLiked":
                                        sendMessage(HandlerMessages.OVKAPI_LIKES_CHECK, method,
                                                args, response_body);
                                        break;
                                    case "Messages.getById":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_BY_ID, method,
                                                args, response_body);
                                        break;
                                    case "Messages.send":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_SEND, method,
                                                args, response_body);
                                        break;
                                    case "Messages.delete":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_DELETE, method,
                                                args, response_body);
                                        break;
                                    case "Messages.restore":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_RESTORE, method,
                                                args, response_body);
                                        break;
                                    case "Messages.getConversations":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_CONVERSATIONS, method,
                                                args, response_body);
                                        break;
                                    case "Messages.getConverstaionsByID":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_CONVERSATIONS_BY_ID,
                                                method, args, response_body);
                                        break;
                                    case "Messages.getHistory":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_HISTORY,
                                                method, args, response_body);
                                        break;
                                    case "Messages.getLongPollHistory":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_LONGPOLL_HISTORY,
                                                method, args, response_body);
                                        break;
                                    case "Messages.getLongPollServer":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_LONGPOLL_SERVER,
                                                method, args, response_body);
                                        break;
                                    case "Ovk.version":
                                        sendMessage(HandlerMessages.OVKAPI_OVK_VERSION,
                                                method, args, response_body);
                                        break;
                                    case "Ovk.test":
                                        sendMessage(HandlerMessages.OVKAPI_OVK_TEST,
                                                method, args, response_body);
                                        break;
                                    case "Ovk.chickenWings":
                                        sendMessage(HandlerMessages.OVKAPI_OVK_CHICKEN_WINGS,
                                                method, args, response_body);
                                        break;
                                    case "Ovk.aboutInstance":
                                        sendMessage(HandlerMessages.OVKAPI_OVK_ABOUTINSTANCE,
                                                method, args, response_body);
                                        break;
                                    case "Users.getFollowers":
                                        sendMessage(HandlerMessages.OVKAPI_USERS_FOLLOWERS,
                                                method, args, response_body);
                                        break;
                                    case "Users.search":
                                        sendMessage(HandlerMessages.OVKAPI_USERS_SEARCH,
                                                method, args, response_body);
                                        break;
                                    case "Users.get":
                                        sendMessage(HandlerMessages.OVKAPI_USERS_GET,
                                                method, args, response_body);
                                        break;
                                    case "Wall.get":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_GET,
                                                method, args, response_body);
                                        break;
                                    case "Wall.getById":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_GET_BY_ID,
                                                method, args, response_body);
                                        break;
                                    case "Wall.post":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_POST,
                                                method, args, response_body);
                                        break;
                                    case "Wall.repost":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_REPOST,
                                                method, args, response_body);
                                        break;
                                    case "Wall.createComment":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_DELETE_COMMENT,
                                                method, args, response_body);
                                        break;
                                    case "Wall.getComment":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_COMMENT,
                                                method, args, response_body);
                                        break;
                                    case "Wall.getComments":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_ALL_COMMENTS,
                                                method, args, response_body);
                                        break;
                                    case "Newsfeed.get":
                                        sendMessage(HandlerMessages.OVKAPI_NEWSFEED_GET,
                                                method, args, response_body);
                                        break;
                                    case "Newsfeed.getGlobal":
                                        sendMessage(HandlerMessages.OVKAPI_NEWSFEED_GET_GLOBAL,
                                                method, args, response_body);
                                        break;
                                    case "Polls.addVote":
                                        sendMessage(HandlerMessages.OVKAPI_POLL_ADD_VOTE,
                                                method, args, response_body);
                                        break;
                                    case "Polls.deleteVote":
                                        sendMessage(HandlerMessages.OVKAPI_POLL_DELETE_VOTE,
                                                method, args, response_body);
                                        break;
                                }
                            } else if(response_code == 400) {
                                error = new Error();
                                error.parse(response_body);
                                if(logging_enabled) Log.v("OpenVK API",
                                        String.format("Getting response from %s (%s): [%s / Error code: %d]",
                                                server, response_code, error.description, error.code));
                                if(error.code == 3) {
                                    sendMessage(HandlerMessages.OVKAPI_METHOD_NOT_FOUND, method, args,
                                            error.description);
                                } else if(error.code == 5) {
                                    sendMessage(HandlerMessages.OVKAPI_INVALID_TOKEN, method, args,
                                            error.description);
                                } else if (error.code == 15) {
                                    sendMessage(HandlerMessages.OVKAPI_ACCESS_DENIED, method, args,
                                            error.description);
                                } else if(error.code == 100) {
                                    sendMessage(HandlerMessages.OVKAPI_INVALID_USAGE, method, args,
                                            error.description);
                                } else if(error.code == 945) {
                                    sendMessage(HandlerMessages.OVKAPI_CHAT_DISABLED, method, args,
                                            error.description);
                                }
                            } else if (response_code == 503) {
                                sendMessage(HandlerMessages.OVKAPI_INSTANCE_UNAVAILABLE, method, args, response_body);
                            }  else if (response_code >= 500 && response_code <= 526) {
                                if(logging_enabled) Log.e("OpenVK API",
                                        String.format("Getting response from %s (%s)",
                                                server, response_code));
                                sendMessage(HandlerMessages.OVKAPI_INTERNAL_ERROR, method, "");
                            }
                        }
                    } catch (ConnectException | ProtocolException | UnknownHostException e) {
                        if(logging_enabled) Log.e("OpenVK API",
                                String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_NO_INTERNET_CONNECTION, method, args,
                                error.description);
                    } catch (SocketException e) {
                        if(Objects.requireNonNull(e.getMessage()).contains("ETIMEDOUT")) {
                            if(logging_enabled) Log.e("OpenVK API",
                                    String.format("Connection error: %s", e.getMessage()));
                            error.description = e.getMessage();
                            sendMessage(HandlerMessages.OVKAPI_CONNECTION_TIMEOUT, method, args,
                                    error.description);
                        }
                    } catch (SocketTimeoutException e) {
                        if(logging_enabled) Log.e("OpenVK API",
                                String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_CONNECTION_TIMEOUT, method, args,
                                error.description);
                    } catch(javax.net.ssl.SSLException e) {
                        if(logging_enabled) Log.e("OpenVK API",
                                String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_BROKEN_SSL_CONNECTION, method,
                                args, error.description);
                    } catch (OutOfMemoryError | Exception e) {
                        sendMessage(HandlerMessages.OVKAPI_UNKNOWN_ERROR, method, args, "");
                        e.printStackTrace();
                    }
                } catch (Exception ex) {
                    sendMessage(HandlerMessages.OVKAPI_UNKNOWN_ERROR, method, args, "");
                    ex.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void sendAPIMethod(final String method) {
        error.description = "";
        String url;
        url = String.format("http://%s/method/%s?access_token=%s", server, method, access_token);
        if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s..." +
                "\r\nMethod: %s\r\nArguments: [without arguments]", server, method));
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            StatusLine statusLine = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                try {
                    request = new Request.Builder()
                            .url(fUrl)
                            .addHeader("User-Agent", generateUserAgent(ctx)).build();
                    try {
                        Response response = httpClient.newCall(request).execute();
                        response_body = Objects.requireNonNull(response.body()).string();
                        response_code = response.code();
                        if (response_body.length() > 0) {
                            if(response_code == 200) {
                                if(logging_enabled) Log.v("OpenVK API", String.format("Getting " +
                                        "response from %s (%s): [%s]", server, response_code, response_body));
                                switch (method) {
                                    case "Account.getProfileInfo":
                                        sendMessage(HandlerMessages.OVKAPI_ACCOUNT_PROFILE_INFO, method,
                                                response_body);
                                        break;
                                    case "Account.setOnline":
                                        sendMessage(HandlerMessages.OVKAPI_ACCOUNT_SET_TO_ONLINE, method,
                                                response_body);
                                        break;
                                    case "Account.setOffline":
                                        sendMessage(HandlerMessages.OVKAPI_ACCOUNT_SET_TO_OFFLINE, method,
                                                response_body);
                                        break;
                                    case "Account.getCounters":
                                        sendMessage(HandlerMessages.OVKAPI_ACCOUNT_COUNTERS, method,
                                                response_body);
                                        break;
                                    case "Friends.get":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_GET, method,
                                                response_body);
                                        break;
                                    case "Friends.add":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_ADD, method,
                                                response_body);
                                        break;
                                    case "Friends.delete":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_DELETE, method,
                                                response_body);
                                        break;
                                    case "Friends.areFriends":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_CHECK, method,
                                                response_body);
                                        break;
                                    case "Friends.getRequests":
                                        sendMessage(HandlerMessages.OVKAPI_FRIENDS_REQUESTS, method,
                                                response_body);
                                        break;
                                    case "Groups.get":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_GET, method,
                                                response_body);
                                        break;
                                    case "Groups.getById":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_GET_BY_ID, method,
                                                response_body);
                                        break;
                                    case "Groups.search":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_SEARCH, method,
                                                response_body);
                                        break;
                                    case "Groups.join":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_JOIN, method,
                                                response_body);
                                        break;
                                    case "Groups.leave":
                                        sendMessage(HandlerMessages.OVKAPI_GROUPS_LEAVE, method,
                                                response_body);
                                        break;
                                    case "Likes.add":
                                        sendMessage(HandlerMessages.OVKAPI_LIKES_ADD, method,
                                                response_body);
                                        break;
                                    case "Likes.delete":
                                        sendMessage(HandlerMessages.OVKAPI_LIKES_DELETE, method,
                                                response_body);
                                        break;
                                    case "Likes.isLiked":
                                        sendMessage(HandlerMessages.OVKAPI_LIKES_CHECK, method,
                                                response_body);
                                        break;
                                    case "Messages.getById":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_BY_ID, method,
                                                response_body);
                                        break;
                                    case "Messages.send":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_SEND, method,
                                                response_body);
                                        break;
                                    case "Messages.delete":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_DELETE, method,
                                                response_body);
                                        break;
                                    case "Messages.restore":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_RESTORE, method,
                                                response_body);
                                        break;
                                    case "Messages.getConverstaions":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_CONVERSATIONS, method,
                                                response_body);
                                        break;
                                    case "Messages.getConverstaionsByID":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_CONVERSATIONS_BY_ID,
                                                method, response_body);
                                        break;
                                    case "Messages.getHistory":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_HISTORY,
                                                method, response_body);
                                        break;
                                    case "Messages.getLongPollHistory":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_LONGPOLL_HISTORY,
                                                method, response_body);
                                        break;
                                    case "Messages.getLongPollServer":
                                        sendMessage(HandlerMessages.OVKAPI_MESSAGES_GET_LONGPOLL_SERVER,
                                                method, response_body);
                                        break;
                                    case "Ovk.version":
                                        sendMessage(HandlerMessages.OVKAPI_OVK_VERSION, method, response_body);
                                        break;
                                    case "Ovk.test":
                                        sendMessage(HandlerMessages.OVKAPI_OVK_TEST, method, response_body);
                                        break;
                                    case "Ovk.chickenWings":
                                        sendMessage(HandlerMessages.OVKAPI_OVK_CHICKEN_WINGS, method, response_body);
                                        break;
                                    case "Ovk.aboutInstance":
                                        sendMessage(HandlerMessages.OVKAPI_OVK_ABOUTINSTANCE, method, response_body);
                                        break;
                                    case "Users.getFollowers":
                                        sendMessage(HandlerMessages.OVKAPI_USERS_FOLLOWERS, method, response_body);
                                        break;
                                    case "Users.search":
                                        sendMessage(HandlerMessages.OVKAPI_USERS_SEARCH, method, response_body);
                                        break;
                                    case "Users.get":
                                        sendMessage(HandlerMessages.OVKAPI_USERS_GET, method, response_body);
                                        break;
                                    case "Wall.get":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_GET, method, response_body);
                                        break;
                                    case "Wall.getById":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_GET_BY_ID, method, response_body);
                                        break;
                                    case "Wall.post":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_POST, method, response_body);
                                        break;
                                    case "Wall.repost":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_REPOST, method, response_body);
                                        break;
                                    case "Wall.createComment":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_DELETE_COMMENT, method, response_body);
                                        break;
                                    case "Wall.getComment":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_COMMENT, method, response_body);
                                        break;
                                    case "Wall.getComments":
                                        sendMessage(HandlerMessages.OVKAPI_WALL_ALL_COMMENTS, method, response_body);
                                        break;
                                    case "Newsfeed.get":
                                        sendMessage(HandlerMessages.OVKAPI_NEWSFEED_GET, method, response_body);
                                        break;
                                    case "Newsfeed.getGlobal":
                                        sendMessage(HandlerMessages.OVKAPI_NEWSFEED_GET_GLOBAL, method, response_body);
                                        break;
                                    case "Polls.addVote":
                                        sendMessage(HandlerMessages.OVKAPI_POLL_ADD_VOTE, method, response_body);
                                        break;
                                    case "Polls.deleteVote":
                                        sendMessage(HandlerMessages.OVKAPI_POLL_DELETE_VOTE, method, response_body);
                                        break;
                                }
                            } else if(response_code == 400) {
                                error = new Error();
                                error.parse(response_body);
                                if(logging_enabled) Log.v("OpenVK API",
                                        String.format("Getting response from %s (%s): [%s / Error code: %d]",
                                                server, response_code, error.description, error.code));
                                if(error.code == 3) {
                                    sendMessage(HandlerMessages.OVKAPI_METHOD_NOT_FOUND, method, error.description);
                                } else if(error.code == 5) {
                                    sendMessage(HandlerMessages.OVKAPI_INVALID_TOKEN, method, error.description);
                                } else if (error.code == 15) {
                                    sendMessage(HandlerMessages.OVKAPI_ACCESS_DENIED, method, error.description);
                                } else if(error.code == 100) {
                                    sendMessage(HandlerMessages.OVKAPI_INVALID_USAGE, method, error.description);
                                } else if(error.code == 945) {
                                    sendMessage(HandlerMessages.OVKAPI_CHAT_DISABLED, method, error.description);
                                }
                            } else if (response_code == 503) {
                                sendMessage(HandlerMessages.OVKAPI_INSTANCE_UNAVAILABLE, method, response_body);
                            }  else if (response_code >= 500 && response_code <= 526) {
                                Log.e("OpenVK API", String.format("Getting response from %s (%s)", server, response_code));
                                sendMessage(HandlerMessages.OVKAPI_INTERNAL_ERROR, method, "");
                            }
                        }
                    } catch (ConnectException | ProtocolException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error:" +
                                " %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketException e) {
                        if(Objects.requireNonNull(e.getMessage()).contains("ETIMEDOUT")) {
                            if(logging_enabled) Log.e("OpenVK API", String.format("Connection error:" +
                                    " %s", e.getMessage()));
                            error.description = e.getMessage();
                            sendMessage(HandlerMessages.OVKAPI_CONNECTION_TIMEOUT, method, error.description);
                        }
                    } catch (SocketTimeoutException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error:" +
                                " %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_CONNECTION_TIMEOUT, method, error.description);
                    } catch (UnknownHostException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error:" +
                                " %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_NO_INTERNET_CONNECTION, method, error.description);
                    } catch(javax.net.ssl.SSLException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error:" +
                                " %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.OVKAPI_BROKEN_SSL_CONNECTION, error.description);
                    } catch (Exception e) {
                        sendMessage(HandlerMessages.OVKAPI_UNKNOWN_ERROR, "");
                        e.printStackTrace();
                        error.description = e.getMessage();
                    }
                } catch (Exception ex) {
                    sendMessage(HandlerMessages.OVKAPI_UNKNOWN_ERROR, "");
                    ex.printStackTrace();
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
        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
            ((ProfileIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
            ((FriendsIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
            ((GroupIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ConversationActivity")) {
            ((ConversationActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("NewPostActivity")) {
            ((NewPostActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("QuickSearchActivity")) {
            ((QuickSearchActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("WallPostActivity")) {
           ((WallPostActivity) ctx).handler.sendMessage(msg);
        }
    }

    private void sendMessage(int message, String method, String response) {
        Message msg = new Message();
        msg.what = message;
        Bundle bundle = new Bundle();
        bundle.putString("response", response);
        bundle.putString("method", method);
        msg.setData(bundle);
        if(ctx.getClass().getSimpleName().equals("AuthActivity")) {
            ((AuthActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("AppActivity")) {
            ((AppActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
            ((ProfileIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
            ((FriendsIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
            ((GroupIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("MainSettingsActivity")) {
            ((MainSettingsActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ConversationActivity")) {
            ((ConversationActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("NewPostActivity")) {
            ((NewPostActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("QuickSearchActivity")) {
            ((QuickSearchActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("WallPostActivity")) {
            ((WallPostActivity) ctx).handler.sendMessage(msg);
        }
    }

    private void sendMessage(int message, String method, String args, String response) {
        Message msg = new Message();
        msg.what = message;
        Bundle bundle = new Bundle();
        bundle.putString("response", response);
        bundle.putString("method", method);
        bundle.putString("args", args);
        msg.setData(bundle);
        if(ctx.getClass().getSimpleName().equals("AuthActivity")) {
            ((AuthActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("AppActivity")) {
            ((AppActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
            ((ProfileIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
            ((GroupIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
            ((FriendsIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("MainSettingsActivity")) {
            ((MainSettingsActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ConversationActivity")) {
            ((ConversationActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("NewPostActivity")) {
            ((NewPostActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("QuickSearchActivity")) {
            ((QuickSearchActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("WallPostActivity")) {
            ((WallPostActivity) ctx).handler.sendMessage(msg);
        }
    }

    public void checkHTTPS() {
        httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).followRedirects(false)
                .followSslRedirects(true).build();
        String url;
        url = String.format("http://%s", server);
        if(logging_enabled) Log.v("OpenVK API", String.format("Checking %s...", server));
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
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
                    Response response = httpClient.newCall(request).execute();
                    response_body = Objects.requireNonNull(response.body()).string();
                    response_code = response.code();
                    if(response_code == 200) {
                        sendMessage(HandlerMessages.OVKAPI_OVK_CHECK_HTTP, response_body);
                    } else if(response_code == 301) {
                        sendMessage(HandlerMessages.OVKAPI_OVK_CHECK_HTTPS, response_body);
                    }
                    httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
                } catch (SocketTimeoutException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s",
                            e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.OVKAPI_CONNECTION_TIMEOUT, error.description);
                } catch (UnknownHostException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s",
                            e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.OVKAPI_NO_INTERNET_CONNECTION, error.description);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public String getStatus() {
        return status;
    }

    public Error getError() {
        return error;
    }

    public void setAccessToken(String token) {
        this.access_token = token;
    }

}
