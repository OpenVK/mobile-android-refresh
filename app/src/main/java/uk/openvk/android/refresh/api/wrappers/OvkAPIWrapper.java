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
import java.util.Objects;
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

            @Override
            public void run() {
                request = new Request.Builder()
                        .url(fUrl)
                        .addHeader("User-Agent", generateUserAgent(ctx)).build();
                try {
                    Response response = httpClient.newCall(request).execute();
                    String response_body = Objects.requireNonNull(response.body()).string();
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

    public void sendAPIMethod(final String method) {
        error.description = "";
        String url = "";
        url = String.format("http://%s/method/%s?access_token=%s", server, method, access_token);
        if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s...\r\nMethod: %s\r\nArguments: [without arguments]", server, method));
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                request = new Request.Builder()
                        .url(fUrl)
                        .addHeader("User-Agent", generateUserAgent(ctx)).build();
                try {
                    Response response = httpClient.newCall(request).execute();
                        response_body = Objects.requireNonNull(response.body()).string();
                        response_code = response.code();
                    if (response_body.length() > 0) {
                        if(response_code == 200) {
                            if(logging_enabled) Log.v("OpenVK API", String.format("Getting response from %s (%s): [%s]", server, response_code, response_body));
                            switch (method) {
                                case "Account.getProfileInfo":
                                    sendMessage(HandlerMessages.ACCOUNT_PROFILE_INFO, method, response_body);
                                    break;
                                case "Account.setOnline":
                                    sendMessage(HandlerMessages.ACCOUNT_SET_TO_ONLINE, method, response_body);
                                    break;
                                case "Account.setOffline":
                                    sendMessage(HandlerMessages.ACCOUNT_SET_TO_OFFLINE, method, response_body);
                                    break;
                                case "Account.getCounters":
                                    sendMessage(HandlerMessages.ACCOUNT_COUNTERS, method, response_body);
                                    break;
                                case "Friends.get":
                                    sendMessage(HandlerMessages.FRIENDS_GET, method, response_body);
                                    break;
                                case "Friends.add":
                                    sendMessage(HandlerMessages.FRIENDS_ADD, method, response_body);
                                    break;
                                case "Friends.delete":
                                    sendMessage(HandlerMessages.FRIENDS_DELETE, method, response_body);
                                    break;
                                case "Friends.areFriends":
                                    sendMessage(HandlerMessages.FRIENDS_CHECK, method, response_body);
                                    break;
                                case "Groups.get":
                                    sendMessage(HandlerMessages.GROUPS_GET, method, response_body);
                                    break;
                                case "Groups.getById":
                                    sendMessage(HandlerMessages.GROUPS_GET_BY_ID, method, response_body);
                                    break;
                                case "Groups.search":
                                    sendMessage(HandlerMessages.GROUPS_SEARCH, method, response_body);
                                    break;
                                case "Likes.add":
                                    sendMessage(HandlerMessages.LIKES_ADD, method, response_body);
                                    break;
                                case "Likes.delete":
                                    sendMessage(HandlerMessages.LIKES_DELETE, method, response_body);
                                    break;
                                case "Likes.isLiked":
                                    sendMessage(HandlerMessages.LIKES_CHECK, method, response_body);
                                    break;
                                case "Messages.getById":
                                    sendMessage(HandlerMessages.MESSAGES_GET_BY_ID, method, response_body);
                                    break;
                                case "Messages.send":
                                    sendMessage(HandlerMessages.MESSAGES_SEND, method, response_body);
                                    break;
                                case "Messages.delete":
                                    sendMessage(HandlerMessages.MESSAGES_DELETE, method, response_body);
                                    break;
                                case "Messages.restore":
                                    sendMessage(HandlerMessages.MESSAGES_RESTORE, method, response_body);
                                    break;
                                case "Messages.getConverstaions":
                                    sendMessage(HandlerMessages.MESSAGES_CONVERSATIONS, method, response_body);
                                    break;
                                case "Messages.getConverstaionsByID":
                                    sendMessage(HandlerMessages.MESSAGES_GET_CONVERSATIONS_BY_ID, method, response_body);
                                    break;
                                case "Messages.getHistory":
                                    sendMessage(HandlerMessages.MESSAGES_GET_HISTORY, method, response_body);
                                    break;
                                case "Messages.getLongPollHistory":
                                    sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_HISTORY, method, response_body);
                                    break;
                                case "Messages.getLongPollServer":
                                    sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER, method, response_body);
                                    break;
                                case "Ovk.version":
                                    sendMessage(HandlerMessages.OVK_VERSION, method, response_body);
                                    break;
                                case "Ovk.test":
                                    sendMessage(HandlerMessages.OVK_TEST, method, response_body);
                                    break;
                                case "Ovk.chickenWings":
                                    sendMessage(HandlerMessages.OVK_CHICKEN_WINGS, method, response_body);
                                    break;
                                case "Ovk.aboutInstance":
                                    sendMessage(HandlerMessages.OVK_ABOUTINSTANCE, method, response_body);
                                    break;
                                case "Users.get":
                                    sendMessage(HandlerMessages.USERS_GET, method, response_body);
                                    break;
                                case "Users.getFollowers":
                                    sendMessage(HandlerMessages.USERS_FOLLOWERS, method, response_body);
                                    break;
                                case "Users.search":
                                    sendMessage(HandlerMessages.USERS_SEARCH, method, response_body);
                                    break;
                                case "Wall.get":
                                    sendMessage(HandlerMessages.WALL_GET, method, response_body);
                                    break;
                                case "Wall.getById":
                                    sendMessage(HandlerMessages.WALL_GET_BY_ID, method, response_body);
                                    break;
                                case "Wall.post":
                                    sendMessage(HandlerMessages.WALL_POST, method, response_body);
                                    break;
                                case "Wall.repost":
                                    sendMessage(HandlerMessages.WALL_REPOST, method, response_body);
                                    break;
                                case "Wall.createComment":
                                    sendMessage(HandlerMessages.WALL_CREATE_COMMENT, method, response_body);
                                    break;
                                case "Wall.getComment":
                                    sendMessage(HandlerMessages.WALL_COMMENT, method, response_body);
                                    break;
                                case "Wall.getComments":
                                    sendMessage(HandlerMessages.WALL_ALL_COMMENTS, method, response_body);
                                    break;
                                case "Newsfeed.get":
                                    sendMessage(HandlerMessages.NEWSFEED_GET, method, response_body);
                                    break;
                                case "Polls.addVote":
                                    sendMessage(HandlerMessages.POLL_ADD_VOTE, method, response_body);
                                    break;
                                case "Polls.deleteVote":
                                    sendMessage(HandlerMessages.POLL_DELETE_VOTE, method, response_body);
                                    break;
                            }
                        } else if(response_code == 400) {
                            error = new Error();
                            error.parse(response_body);
                            if(logging_enabled) Log.v("OpenVK API", String.format("Getting response from %s (%s): [%s / Error code: %d]", server, response_code, error.description, error.code));
                            if(error.code == 3) {
                                sendMessage(HandlerMessages.METHOD_NOT_FOUND, method, error.description);
                            } else if(error.code == 5) {
                                sendMessage(HandlerMessages.INVALID_TOKEN, method, error.description);
                            } else if (error.code == 15) {
                                sendMessage(HandlerMessages.ACCESS_DENIED, method, error.description);
                            } else if(error.code == 100) {
                                sendMessage(HandlerMessages.INVALID_USAGE, method, error.description);
                            } else if(error.code == 945) {
                                sendMessage(HandlerMessages.CHAT_DISABLED, method, error.description);
                            }
                        } else if (response_code >= 500 && response_code <= 526) {
                            Log.e("OpenVK API", String.format("Getting response from %s (%s)", server, response_code));
                            sendMessage(HandlerMessages.INTERNAL_ERROR, method, "");
                        }
                    };
                } catch (ConnectException | ProtocolException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                } catch (SocketException e) {
                    if(Objects.requireNonNull(e.getMessage()).contains("ETIMEDOUT")) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, error.description);
                    }
                } catch (SocketTimeoutException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, error.description);
                } catch (UnknownHostException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, method, error.description);
                } catch(SSLException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                } catch (Exception e) {
                    e.printStackTrace();
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.UNKNOWN_ERROR, error.description);
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void sendAPIMethod(final String method, final String args, final String where) {
        error.description = "";
        String url = "";
        url = String.format("http://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
        if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s...\r\nMethod: %s\r\nArguments: %s\r\nWhere: %s", server, method, args, where));
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                request = new Request.Builder()
                        .url(fUrl)
                        .addHeader("User-Agent", generateUserAgent(ctx)).build();
                try {
                    Response response = httpClient.newCall(request).execute();
                    response_body = Objects.requireNonNull(response.body()).string();
                    response_code = response.code();
                    if (response_body.length() > 0) {
                        if (response_code == 200) {
                            if(logging_enabled) Log.v("OpenVK API", String.format("Getting response from %s (%s): [%s]", server, response_code, response_body));
                            switch (method) {
                                case "Account.getProfileInfo":
                                    sendMessage(HandlerMessages.ACCOUNT_PROFILE_INFO, method, args, response_body);
                                    break;
                                case "Account.setOnline":
                                    sendMessage(HandlerMessages.ACCOUNT_SET_TO_ONLINE, method, args, response_body);
                                    break;
                                case "Account.setOffline":
                                    sendMessage(HandlerMessages.ACCOUNT_SET_TO_OFFLINE, method, args, response_body);
                                    break;
                                case "Account.getCounters":
                                    sendMessage(HandlerMessages.ACCOUNT_COUNTERS, method, args, response_body);
                                    break;
                                case "Friends.get":
                                    if (where.equals("friends_list")) {
                                        sendMessage(HandlerMessages.FRIENDS_GET, method, args, response_body);
                                    } else if (where.equals("profile_counter")) {
                                        sendMessage(HandlerMessages.FRIENDS_GET_ALT, method, args, response_body);
                                    }
                                    break;
                                case "Friends.add":
                                    sendMessage(HandlerMessages.FRIENDS_ADD, method, args, response_body);
                                    break;
                                case "Friends.delete":
                                    sendMessage(HandlerMessages.FRIENDS_DELETE, method, args, response_body);
                                    break;
                                case "Friends.areFriends":
                                    sendMessage(HandlerMessages.FRIENDS_CHECK, method, args, response_body);
                                    break;
                                case "Groups.get":
                                    sendMessage(HandlerMessages.GROUPS_GET, method, args, response_body);
                                    break;
                                case "Groups.getById":
                                    sendMessage(HandlerMessages.GROUPS_GET_BY_ID, method, args, response_body);
                                    break;
                                case "Groups.search":
                                    sendMessage(HandlerMessages.GROUPS_SEARCH, method, response_body);
                                    break;
                                case "Likes.add":
                                    sendMessage(HandlerMessages.LIKES_ADD, method, args, response_body);
                                    break;
                                case "Likes.delete":
                                    sendMessage(HandlerMessages.LIKES_DELETE, method, args, response_body);
                                    break;
                                case "Likes.isLiked":
                                    sendMessage(HandlerMessages.LIKES_CHECK, method, args, response_body);
                                    break;
                                case "Messages.getById":
                                    sendMessage(HandlerMessages.MESSAGES_GET_BY_ID, method, args, response_body);
                                    break;
                                case "Messages.send":
                                    sendMessage(HandlerMessages.MESSAGES_SEND, method, args, response_body);
                                    break;
                                case "Messages.delete":
                                    sendMessage(HandlerMessages.MESSAGES_DELETE, method, args, response_body);
                                    break;
                                case "Messages.restore":
                                    sendMessage(HandlerMessages.MESSAGES_RESTORE, method, args, response_body);
                                    break;
                                case "Messages.getConverstaions":
                                    sendMessage(HandlerMessages.MESSAGES_CONVERSATIONS, method, args, response_body);
                                    break;
                                case "Messages.getConverstaionsByID":
                                    sendMessage(HandlerMessages.MESSAGES_GET_CONVERSATIONS_BY_ID, method, args, response_body);
                                    break;
                                case "Messages.getHistory":
                                    sendMessage(HandlerMessages.MESSAGES_GET_HISTORY, method, args, response_body);
                                    break;
                                case "Messages.getLongPollHistory":
                                    sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_HISTORY, method, args, response_body);
                                    break;
                                case "Messages.getLongPollServer":
                                    sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER, method, args, response_body);
                                    break;
                                case "Ovk.version":
                                    sendMessage(HandlerMessages.OVK_VERSION, method, args, response_body);
                                    break;
                                case "Ovk.test":
                                    sendMessage(HandlerMessages.OVK_TEST, method, args, response_body);
                                    break;
                                case "Ovk.chickenWings":
                                    sendMessage(HandlerMessages.OVK_CHICKEN_WINGS, method, args, response_body);
                                    break;
                                case "Ovk.aboutInstance":
                                    sendMessage(HandlerMessages.OVK_ABOUTINSTANCE, method, args, response_body);
                                    break;
                                case "Users.getFollowers":
                                    sendMessage(HandlerMessages.USERS_FOLLOWERS, method, args, response_body);
                                    break;
                                case "Users.search":
                                    sendMessage(HandlerMessages.USERS_SEARCH, method, args, response_body);
                                    break;
                                case "Users.get":
                                    if (where.equals("profile")) {
                                        sendMessage(HandlerMessages.USERS_GET, method, args, response_body);
                                    } else if (where.equals("account_user")) {
                                        sendMessage(HandlerMessages.USERS_GET_ALT, method, args, response_body);
                                    } else if (where.equals("peers")) {
                                        sendMessage(HandlerMessages.USERS_GET_ALT2, method, args, response_body);
                                    }
                                    break;
                                case "Wall.get":
                                    sendMessage(HandlerMessages.WALL_GET, method, args, response_body);
                                    break;
                                case "Wall.getById":
                                    sendMessage(HandlerMessages.WALL_GET_BY_ID, method, args, response_body);
                                    break;
                                case "Wall.post":
                                    sendMessage(HandlerMessages.WALL_POST, method, args, response_body);
                                    break;
                                case "Wall.repost":
                                    sendMessage(HandlerMessages.WALL_REPOST, method, args, response_body);
                                    break;
                                case "Wall.createComment":
                                    sendMessage(HandlerMessages.WALL_CREATE_COMMENT, method, args, response_body);
                                    break;
                                case "Wall.getComment":
                                    sendMessage(HandlerMessages.WALL_COMMENT, method, args, response_body);
                                    break;
                                case "Wall.getComments":
                                    sendMessage(HandlerMessages.WALL_ALL_COMMENTS, method, args, response_body);
                                    break;
                                case "Newsfeed.get":
                                    if (where.equals("more_news")) {
                                        sendMessage(HandlerMessages.NEWSFEED_GET_MORE, method, args, response_body);
                                    } else {
                                        sendMessage(HandlerMessages.NEWSFEED_GET, method, args, response_body);
                                    }
                                    break;
                                case "Polls.addVote":
                                    sendMessage(HandlerMessages.POLL_ADD_VOTE, method, args, response_body);
                                    break;
                                case "Polls.deleteVote":
                                    sendMessage(HandlerMessages.POLL_DELETE_VOTE, method, args, response_body);
                                    break;
                            }
                        } else if (response_code == 400) {
                            error = new Error();
                            error.parse(response_body);
                            if(logging_enabled) Log.v("OpenVK API", String.format("Getting response from %s (%s): [%s / Error code: %d]", server, response_code, error.description, error.code));
                            if (error.code == 3) {
                                sendMessage(HandlerMessages.METHOD_NOT_FOUND, method, args, error.description);
                            } else if (error.code == 5) {
                                sendMessage(HandlerMessages.INVALID_TOKEN, method, args, error.description);
                            } else if (error.code == 15) {
                                sendMessage(HandlerMessages.ACCESS_DENIED, method, args, error.description);
                            } else if (error.code == 100) {
                                sendMessage(HandlerMessages.INVALID_USAGE, method, args, error.description);
                            }
                        } else if (response_code >= 500 && response_code <= 526) {
                            if(logging_enabled) Log.e("OpenVK API", String.format("Getting response from %s (%s)", server, response_code));
                            sendMessage(HandlerMessages.INTERNAL_ERROR, method, "");
                        }
                    };
                } catch (ConnectException | ProtocolException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                } catch (SocketException e) {
                    if(Objects.requireNonNull(e.getMessage()).contains("ETIMEDOUT")) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, args, error.description);
                    }
                } catch (SocketTimeoutException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, args, error.description);
                } catch (UnknownHostException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, method, args, error.description);
                } catch(SSLException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                } catch (OutOfMemoryError | Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void sendAPIMethod(final String method, final String args) {
        error.description = "";
        String url = "";
        url = String.format("http://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
        Log.v("OpenVK API", String.format("Connecting to %s...\r\nMethod: %s\r\nArguments: %s", server, method, args));
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                request = new Request.Builder()
                        .url(fUrl)
                        .addHeader("User-Agent", generateUserAgent(ctx)).build();
                try {
                    Response response = httpClient.newCall(request).execute();
                    response_body = Objects.requireNonNull(response.body()).string();
                    response_code = response.code();
                    if (response_body.length() > 0) {
                        if(response_code == 200) {
                            if(logging_enabled) Log.v("OpenVK API", String.format("Getting response from %s (%s): [%s]", server, response_code, response_body));
                            switch (method) {
                                case "Account.getProfileInfo":
                                    sendMessage(HandlerMessages.ACCOUNT_PROFILE_INFO, method, args, response_body);
                                    break;
                                case "Account.setOnline":
                                    sendMessage(HandlerMessages.ACCOUNT_SET_TO_ONLINE, method, args, response_body);
                                    break;
                                case "Account.setOffline":
                                    sendMessage(HandlerMessages.ACCOUNT_SET_TO_OFFLINE, method, args, response_body);
                                    break;
                                case "Account.getCounters":
                                    sendMessage(HandlerMessages.ACCOUNT_COUNTERS, method, args, response_body);
                                    break;
                                case "Friends.get":
                                    sendMessage(HandlerMessages.FRIENDS_GET, method, args, response_body);
                                    break;
                                case "Friends.add":
                                    sendMessage(HandlerMessages.FRIENDS_ADD, method, args, response_body);
                                    break;
                                case "Friends.delete":
                                    sendMessage(HandlerMessages.FRIENDS_DELETE, method, args, response_body);
                                    break;
                                case "Friends.areFriends":
                                    sendMessage(HandlerMessages.FRIENDS_CHECK, method, args, response_body);
                                    break;
                                case "Groups.get":
                                    sendMessage(HandlerMessages.GROUPS_GET, method, args, response_body);
                                    break;
                                case "Groups.getById":
                                    sendMessage(HandlerMessages.GROUPS_GET_BY_ID, method, args, response_body);
                                    break;
                                case "Groups.search":
                                    sendMessage(HandlerMessages.GROUPS_SEARCH, method, response_body);
                                    break;
                                case "Likes.add":
                                    sendMessage(HandlerMessages.LIKES_ADD, method, args, response_body);
                                    break;
                                case "Likes.delete":
                                    sendMessage(HandlerMessages.LIKES_DELETE, method, args, response_body);
                                    break;
                                case "Likes.isLiked":
                                    sendMessage(HandlerMessages.LIKES_CHECK, method, args, response_body);
                                    break;
                                case "Messages.getById":
                                    sendMessage(HandlerMessages.MESSAGES_GET_BY_ID, method, args, response_body);
                                    break;
                                case "Messages.send":
                                    sendMessage(HandlerMessages.MESSAGES_SEND, method, args, response_body);
                                    break;
                                case "Messages.delete":
                                    sendMessage(HandlerMessages.MESSAGES_DELETE, method, args, response_body);
                                    break;
                                case "Messages.restore":
                                    sendMessage(HandlerMessages.MESSAGES_RESTORE, method, args, response_body);
                                    break;
                                case "Messages.getConversations":
                                    sendMessage(HandlerMessages.MESSAGES_CONVERSATIONS, method, args, response_body);
                                    break;
                                case "Messages.getConverstaionsByID":
                                    sendMessage(HandlerMessages.MESSAGES_GET_CONVERSATIONS_BY_ID, method, args, response_body);
                                    break;
                                case "Messages.getHistory":
                                    sendMessage(HandlerMessages.MESSAGES_GET_HISTORY, method, args, response_body);
                                    break;
                                case "Messages.getLongPollHistory":
                                    sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_HISTORY, method, args, response_body);
                                    break;
                                case "Messages.getLongPollServer":
                                    sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER, method, args, response_body);
                                    break;
                                case "Ovk.version":
                                    sendMessage(HandlerMessages.OVK_VERSION, method, args, response_body);
                                    break;
                                case "Ovk.test":
                                    sendMessage(HandlerMessages.OVK_TEST, method, args, response_body);
                                    break;
                                case "Ovk.chickenWings":
                                    sendMessage(HandlerMessages.OVK_CHICKEN_WINGS, method, args, response_body);
                                    break;
                                case "Ovk.aboutInstance":
                                    sendMessage(HandlerMessages.OVK_ABOUTINSTANCE, method, args, response_body);
                                    break;
                                case "Users.get":
                                    sendMessage(HandlerMessages.USERS_GET, method, args, response_body);
                                    break;
                                case "Users.getFollowers":
                                    sendMessage(HandlerMessages.USERS_FOLLOWERS, method, args, response_body);
                                    break;
                                case "Users.search":
                                    sendMessage(HandlerMessages.USERS_SEARCH, method, args, response_body);
                                    break;
                                case "Wall.get":
                                    sendMessage(HandlerMessages.WALL_GET, method, args, response_body);
                                    break;
                                case "Wall.getById":
                                    sendMessage(HandlerMessages.WALL_GET_BY_ID, method, args, response_body);
                                    break;
                                case "Wall.post":
                                    sendMessage(HandlerMessages.WALL_POST, method, args, response_body);
                                    break;
                                case "Wall.repost":
                                    sendMessage(HandlerMessages.WALL_REPOST, method, args, response_body);
                                    break;
                                case "Wall.createComment":
                                    sendMessage(HandlerMessages.WALL_CREATE_COMMENT, method, args, response_body);
                                    break;
                                case "Wall.getComment":
                                    sendMessage(HandlerMessages.WALL_COMMENT, method, args, response_body);
                                    break;
                                case "Wall.getComments":
                                    sendMessage(HandlerMessages.WALL_ALL_COMMENTS, method, args, response_body);
                                    break;
                                case "Newsfeed.get":
                                    sendMessage(HandlerMessages.NEWSFEED_GET, method, args, response_body);
                                    break;
                                case "Polls.addVote":
                                    sendMessage(HandlerMessages.POLL_ADD_VOTE, method, args, response_body);
                                    break;
                                case "Polls.deleteVote":
                                    sendMessage(HandlerMessages.POLL_DELETE_VOTE, method, args, response_body);
                                    break;
                            }
                        } else if(response_code == 400) {
                            error = new Error();
                            error.parse(response_body);
                            if(logging_enabled) Log.v("OpenVK API", String.format("Getting response from %s (%s): [%s / Error code: %d]", server, response_code, error.description, error.code));
                            if(error.code == 3) {
                                sendMessage(HandlerMessages.METHOD_NOT_FOUND, method, error.description);
                            } else if(error.code == 5) {
                                sendMessage(HandlerMessages.INVALID_TOKEN, method, error.description);
                            } else if (error.code == 15) {
                                sendMessage(HandlerMessages.ACCESS_DENIED, method, args, error.description);
                            } else if(error.code == 100) {
                                sendMessage(HandlerMessages.INVALID_USAGE, method, error.description);
                            } else if(error.code == 945) {
                                sendMessage(HandlerMessages.CHAT_DISABLED, method, error.description);
                            }
                        } else if (response_code >= 500 && response_code <= 526) {
                            if(logging_enabled) Log.e("OpenVK API", String.format("Getting response from %s (%s)", server, response_code));
                            sendMessage(HandlerMessages.INTERNAL_ERROR, method, "");
                        }
                    };
                } catch (ConnectException | ProtocolException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                } catch (SocketException e) {
                    if(Objects.requireNonNull(e.getMessage()).contains("ETIMEDOUT")) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, args, error.description);
                    }
                } catch (SocketTimeoutException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, error.description);
                } catch (UnknownHostException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, method, error.description);
                } catch(SSLException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
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
        }
    }
}
