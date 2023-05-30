package uk.openvk.android.refresh.api.wrappers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.StatusLine;
import uk.openvk.android.refresh.BuildConfig;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.api.attachments.PhotoAttachment;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;
import uk.openvk.android.refresh.ui.core.activities.AuthActivity;
import uk.openvk.android.refresh.ui.core.activities.FriendsIntentActivity;
import uk.openvk.android.refresh.ui.core.activities.GroupIntentActivity;
import uk.openvk.android.refresh.ui.core.activities.PhotoViewerActivity;
import uk.openvk.android.refresh.ui.core.activities.ProfileIntentActivity;

/**
 * File created by Dmitry on 27.09.2022.
 */

@SuppressWarnings({"ResultOfMethodCallIgnored", "StatementWithEmptyBody"})
public class DownloadManager {
    public String server;
    public boolean use_https;
    public boolean legacy_mode;
    private Context ctx;
    public ArrayList<PhotoAttachment> photoAttachments;
    private boolean logging_enabled = true; // default for beta releases

    private OkHttpClient httpClient = null;

    public DownloadManager(Context ctx) {
        this.ctx = ctx;
        if(BuildConfig.BUILD_TYPE.equals("release")) {
            logging_enabled = false;
        }
        try {
                SSLContext sslContext = null;
                try {
                    sslContext = SSLContext.getInstance("SSL");
                    @SuppressLint("CustomX509TrustManager") TrustManager[] trustAllCerts =
                            new TrustManager[]{
                            new X509TrustManager() {
                                @SuppressLint("TrustAllX509TrustManager")
                                @Override
                                public void checkClientTrusted(java.security.cert.X509Certificate[]
                                                                       chain, String authType) {
                                }

                                @SuppressLint("TrustAllX509TrustManager")
                                @Override
                                public void checkServerTrusted(java.security.cert.X509Certificate[]
                                                                       chain, String authType) {
                                }

                                @Override
                                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                    return new java.security.cert.X509Certificate[]{};
                                }
                            }
                    };
                    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                    javax.net.ssl.SSLSocketFactory ssf = (javax.net.ssl.SSLSocketFactory) sslContext
                            .getSocketFactory();
                    httpClient = new OkHttpClient.Builder().sslSocketFactory(sslContext.getSocketFactory())
                            .connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30,
                                    TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(false).build();
                } catch (Exception e) {
                    httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS).readTimeout(30,
                                    TimeUnit.SECONDS).retryOnConnectionFailure(false).build();
                }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setProxyConnection(boolean useProxy, String address) {
        try {
            if(useProxy) {
                String[] address_array = address.split(":");
                if (address_array.length == 2) {
                    httpClient = new OkHttpClient.Builder().connectTimeout(30,
                                    TimeUnit.SECONDS)
                            .writeTimeout(15, TimeUnit.SECONDS).readTimeout(30,
                                    TimeUnit.SECONDS)
                            .retryOnConnectionFailure(false).proxy(new Proxy(Proxy.Type.HTTP,
                                    new InetSocketAddress(address_array[0],
                                    Integer.parseInt(address_array[1])))).build();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String generateUserAgent(Context ctx) {
        String version_name = "";
        String user_agent = "";
        try {
            PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(
                    ctx.getApplicationContext().getPackageName(), 0);
            version_name = packageInfo.versionName;
        } catch (Exception e) {
            OvkApplication app = ((OvkApplication) ctx.getApplicationContext());
            version_name = app.version;
        } finally {
            user_agent = String.format("OpenVK Refresh/%s (Android %s; SDK %s; %s; %s %s; %s)",
                    version_name,
                    Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.SUPPORTED_ABIS[0],
                    Build.MANUFACTURER, Build.MODEL, System.getProperty("user.language"));
        }
        return user_agent;
    }


    public void downloadPhotosToCache(final ArrayList<PhotoAttachment> photoAttachments,
                                      final String where) {
        Log.v("DownloadManager", String.format("Downloading %d photos...",
                photoAttachments.size()));
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            StatusLine statusLine = null;
            int response_code = 0;
            long filesize = 0;
            long content_length = 0;
            private InputStream response_in;
            private String url = "";
            private String filename = "";

            @Override
            public void run() {
                try {
                    File directory = new File(String.format("%s/photos_cache",
                            ctx.getCacheDir().getAbsolutePath()), where);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                for (int i = 0; i < photoAttachments.size(); i++) {
                    filesize = 0;
                    filename = photoAttachments.get(i).filename;
                    File downloadedFile = new File(String.format("%s/photos_cache/%s",
                            ctx.getCacheDir(), where), filename);
                    PhotoAttachment photoAttachment = photoAttachments.get(i);
                    if(photoAttachment.url == null) {
                        photoAttachment.url = "";
                    }
                    Date lastModDate;
                    if(downloadedFile.exists()) {
                        lastModDate = new Date(downloadedFile.lastModified());
                    } else {
                        lastModDate = new Date(0);
                    }
                    long time_diff = System.currentTimeMillis() - lastModDate.getTime();
                    TimeUnit timeUnit = TimeUnit.MILLISECONDS;
                    if(downloadedFile.exists() && downloadedFile.length() >= 5120 &&
                            timeUnit.convert(time_diff,TimeUnit.MILLISECONDS) >= 60000L &&
                            timeUnit.convert(time_diff,TimeUnit.MILLISECONDS) < 259200000L) {
                        if(logging_enabled) Log.e("OVK DL", "Duplicated filename. Skipping..." +
                                "\r\nTimeDiff: " + timeUnit.convert(time_diff,TimeUnit.MILLISECONDS)
                                + " ms | Filesize: " + downloadedFile.length() + " bytes");
                    } else if (photoAttachment.url.length() == 0) {
                        filename = photoAttachment.filename;
                        //Log.e("DownloadManager", "Invalid address. Skipping...");
                        try {
                            if(downloadedFile.exists()) {
                                FileOutputStream fos = new FileOutputStream(downloadedFile);
                                byte[] bytes = new byte[1];
                                fos.write(bytes);
                                fos.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            filename = photoAttachment.filename;
                            String short_address = "";
                            if(photoAttachments.get(i).url.length() > 40) {
                                short_address = photoAttachments.get(i).url.substring(0, 39);
                            } else {
                                short_address = photoAttachments.get(i).url;
                            }
                            //Log.v("DownloadManager", String.format("Downloading %s (%d/%d)...",
                            // short_address, i + 1, photoAttachments.size()));
                            url = photoAttachments.get(i).url;
                            request = new Request.Builder()
                                    .url(url)
                                    .build();

                            Response response = httpClient.newCall(request).execute();
                            response_code = response.code();
                            content_length = Objects.requireNonNull(response.body()).contentLength();
                            if(!downloadedFile.exists() || content_length != downloadedFile.length()) {
                                FileOutputStream fos = new FileOutputStream(downloadedFile);
                                int inByte;
                                while ((inByte = Objects.requireNonNull(response.body()).byteStream().read()) != -1) {
                                    fos.write(inByte);
                                    filesize++;
                                }
                                fos.close();
                            } else {
                                if(logging_enabled) Log.w("DownloadManager",
                                        "Filesizes match, skipping...");
                            }
                            response.body().byteStream().close();
                            if(logging_enabled) Log.d("DownloadManager",
                                    String.format("Downloaded from %s (%s): %d kB (%d/%d)",
                                            short_address, response_code, (int) (filesize / 1024), i + 1,
                                            photoAttachments.size()));
                        } catch (IOException e) {
                            if(logging_enabled) Log.e("DownloadManager",
                                    String.format("Download error: %s (%d/%d)",
                                            e.getMessage(), i + 1, photoAttachments.size()));
                        } catch (Exception e) {
                            photoAttachment.error = e.getClass().getSimpleName();
                            if(logging_enabled) Log.e("DownloadManager",
                                    String.format("Download error: %s (%d/%d)",
                                            e.getMessage(), i + 1, photoAttachments.size()));
                        }
                    }
                }
                Log.v("DownloadManager", "Downloaded!");
                switch (where) {
                    case "account_avatar":
                        sendMessage(HandlerMessages.DLM_ACCOUNT_AVATAR, where);
                        break;
                    case "profile_avatars":
                        sendMessage(HandlerMessages.DLM_PROFILE_AVATARS, where);
                        break;
                    case "newsfeed_avatars":
                        sendMessage(HandlerMessages.DLM_NEWSFEED_AVATARS, where);
                        break;
                    case "group_avatars":
                        sendMessage(HandlerMessages.DLM_GROUP_AVATARS, where);
                        break;
                    case "newsfeed_photo_attachments":
                        sendMessage(HandlerMessages.DLM_NEWSFEED_ATTACHMENTS, where);
                        break;
                    case "wall_photo_attachments":
                        sendMessage(HandlerMessages.DLM_WALL_ATTACHMENTS, where);
                        break;
                    case "wall_avatars":
                        sendMessage(HandlerMessages.DLM_WALL_AVATARS, where);
                        break;
                    case "friend_avatars":
                        sendMessage(HandlerMessages.DLM_FRIEND_AVATARS, where);
                        break;
                    case "comment_avatars":
                        sendMessage(HandlerMessages.DLM_COMMENT_AVATARS, where);
                        break;
                    case "conversations_avatars":
                        sendMessage(HandlerMessages.DLM_CONVERSATIONS_AVATARS, where);
                        break;
                }
            }
        };

        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void downloadOnePhotoToCache(final String url, final String filename, final String where) {
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            StatusLine statusLine = null;
            int response_code = 0;
            long filesize = 0;
            long content_length = 0;
            private InputStream response_in;

            @Override
            public void run() {
                Log.v("DownloadManager", String.format("Downloading %s...", url));
                try {
                    File directory = new File(String.format("%s/photos_cache",
                            ctx.getCacheDir().getAbsolutePath()), where);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                filesize = 0;
                if (url.length() == 0) {
                    if(logging_enabled) Log.e("DownloadManager", "Invalid address. Skipping...");
                    try {
                        File downloadedFile = new File(String.format("%s/photos_cache/%s",
                                ctx.getCacheDir(), where), filename);
                        if(downloadedFile.exists()) {
                            FileOutputStream fos = new FileOutputStream(downloadedFile);
                            byte[] bytes = new byte[1];
                            fos.write(bytes);
                            fos.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    String short_address = "";
                    if(url.length() > 40) {
                        short_address = url.substring(0, 39);
                    } else {
                        short_address = url;
                    }
                    try {
                        if(logging_enabled) Log.v("DownloadManager",
                                String.format("Downloading %s...", short_address));
                        request = new Request.Builder()
                                .url(url)
                                .build();
                        Response response = httpClient.newCall(request).execute();
                        response_code = response.code();
                        File downloadedFile = new File(String.format("%s/photos_cache/%s",
                                ctx.getCacheDir(), where), filename);
                        if(!downloadedFile.exists() || content_length != downloadedFile.length()) {
                            FileOutputStream fos = new FileOutputStream(downloadedFile);
                            int inByte;
                            while ((inByte = Objects.requireNonNull(response.body()).byteStream().read()) != -1) {
                                fos.write(inByte);
                                filesize++;
                            }
                            fos.close();
                        } else {
                            if(logging_enabled) Log.w("DownloadManager", "Filesizes match, skipping...");
                        }
                        Objects.requireNonNull(response.body()).byteStream().close();
                        response.close();
                        if(logging_enabled) Log.v("DownloadManager",
                                String.format("Downloaded from %s (%s): %d kB", short_address,
                                        response_code, (int) (filesize / 1024)));
                    } catch (Exception e) {
                        if(logging_enabled) Log.e("DownloadManager",
                                String.format("Download error: %s", e.getMessage()));
                    }
                }
                Log.v("DownloadManager", String.format("Downloaded!"));
                switch (where) {
                    case "account_avatar":
                        sendMessage(HandlerMessages.DLM_ACCOUNT_AVATAR, where);
                        break;
                    case "profile_avatars":
                        sendMessage(HandlerMessages.DLM_PROFILE_AVATARS, where);
                        break;
                    case "newsfeed_avatars":
                        sendMessage(HandlerMessages.DLM_NEWSFEED_AVATARS, where);
                        break;
                    case "newsfeed_photo_attachments":
                        sendMessage(HandlerMessages.DLM_NEWSFEED_ATTACHMENTS, where);
                        break;
                    case "group_avatars":
                        sendMessage(HandlerMessages.DLM_GROUP_AVATARS, where);
                        break;
                    case "wall_photo_attachments":
                        sendMessage(HandlerMessages.DLM_WALL_ATTACHMENTS, where);
                        break;
                    case "wall_avatars":
                        sendMessage(HandlerMessages.DLM_WALL_AVATARS, where);
                        break;
                    case "friend_avatars":
                        sendMessage(HandlerMessages.DLM_FRIEND_AVATARS, where);
                        break;
                    case "comment_avatars":
                        sendMessage(HandlerMessages.DLM_COMMENT_AVATARS, where);
                        break;
                    case "original_photos":
                        sendMessage(HandlerMessages.DLM_ORIGINAL_PHOTO, where);
                        break;
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
//        } else if(ctx.getClass().getSimpleName().equals("WallPostActivity")) {
//            ((WallPostActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("PhotoViewerActivity")) {
            ((PhotoViewerActivity) ctx).handler.sendMessage(msg);
        }
    }

    private void sendMessage(int message, String response, int id) {
        Message msg = new Message();
        msg.what = message;
        Bundle bundle = new Bundle();
        bundle.putString("response", response);
        bundle.putInt("id", id);
        msg.setData(bundle);
        if(ctx.getClass().getSimpleName().equals("AuthActivity")) {
            ((AuthActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("AppActivity")) {
            ((AppActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
            ((ProfileIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
            ((FriendsIntentActivity) ctx).handler.sendMessage(msg);
//        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
//            ((GroupIntentActivity) ctx).handler.sendMessage(msg);
//        } else if(ctx.getClass().getSimpleName().equals("CommentsIntentActivity")) {
//            ((WallPostActivity) ctx).handler.sendMessage(msg);
        }
    }

    public boolean clearCache(File dir) {
        if (dir == null) {
            dir = ctx.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < Objects.requireNonNull(children).length; i++) {
                    boolean success = clearCache(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
                return dir.delete();
            } else if (dir != null && dir.isFile()) {
                return dir.delete();
            } else {
                return false;
            }
        } else if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < Objects.requireNonNull(children).length; i++) {
                boolean success = clearCache(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public long getCacheSize() {
        final long[] size = {0};
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long foldersize = 0;
                File[] filelist = new File(ctx.getCacheDir().getAbsolutePath()).listFiles();
                for (int i = 0; i < Objects.requireNonNull(filelist).length; i++) {
                    if (filelist[i].isDirectory()) {
                        File[] filelist2 = new File(filelist[i].getAbsolutePath()).listFiles();
                        for(int file_index = 0; file_index < Objects.requireNonNull(filelist2).length;
                            file_index++) {
                            foldersize += filelist2.length;
                        }
                    } else {
                        foldersize += filelist[i].length();
                    }
                }
                size[0] = foldersize;
            }
        };
        new Thread(runnable).start();
        return size[0];
    }
}
