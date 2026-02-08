package uk.openvk.android.refresh.ui.core.activities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutionException;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.attachments.PhotoAttachment;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.wrappers.DownloadManager;
import uk.openvk.android.refresh.ui.core.activities.base.BaseNetworkActivity;
import uk.openvk.android.refresh.ui.core.enumerations.UiMessages;
import uk.openvk.android.refresh.ui.util.glide.GlideApp;
import uk.openvk.android.refresh.ui.view.ZoomableImageView;

public class PhotoViewerActivity extends BaseNetworkActivity {
    private SharedPreferences global_prefs;
    private PhotoAttachment photo;
    private DownloadManager downloadManager;
    private String cache_path;
    public Handler handler;
    private MaterialToolbar toolbar;
    private AppBarLayout appbar;
    private boolean isDarkTheme;
    private BitmapFactory.Options bfOptions;
    private Bitmap bitmap;
    private SharedPreferences instance_prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        instance_prefs = getSharedPreferences("instance", 0);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"),
                getWindow());
        Global.setInterfaceFont(this);
        if(global_prefs.getBoolean("dark_theme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        isDarkTheme = global_prefs.getBoolean("dark_theme", false);
        setContentView(R.layout.activity_photo_viewer);
        setAppBar();
        setStatusBarColorAttribute(androidx.appcompat.R.attr.colorPrimaryDark);

        loadPhoto();
    }

    private void setAppBar() {
        toolbar = findViewById(R.id.app_toolbar);
        appbar = findViewById(R.id.app_bar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setTitle(getResources().getString(R.string.photo_title));
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        toolbar.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.download) {
                savePhotoFromCache();
            } else if(item.getItemId() == R.id.copy_link) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip =
                        android.content.ClipData.newPlainText("Photo URL", photo.original_url);
                clipboard.setPrimaryClip(clip);
            }
            return false;
        });

        toolbar.getBackground().setAlpha(200);
        findViewById(R.id.app_bar).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.app_bar).setOutlineProvider(null);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void loadPhoto() {
        findViewById(R.id.image_view).setVisibility(View.GONE);
        findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
        Bundle data = getIntent().getExtras();
        if(data != null) {
            cache_path = String.format("%s/photos_cache/original_photos/original_photo_a%s_%s",
                    getCacheDir().getAbsolutePath(), data.getLong("author_id"),
                    data.getLong("photo_id"));
            if(data.containsKey("attachment")) {
                photo = data.getParcelable("attachment");
                assert photo != null;
                downloadManager.downloadOnePhotoToCache(photo.original_url,
                        String.format("original_photo_a%s_%s",
                        data.getLong("author_id"), data.getLong("photo_id")), "original_photos");
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void receiveState(int message, Bundle data) {
        if(data.containsKey("address")) {
            String activityName = data.getString("address");
            if(activityName == null) {
                return;
            }
            boolean isCurrentActivity = activityName.equals(getLocalClassName());
            if(!isCurrentActivity) {
                return;
            }
        }
        if(message == HandlerMessages.ACCESS_DENIED_MARSHMALLOW) {
            allowPermissionDialog();
        } else if(message == HandlerMessages.ORIGINAL_PHOTO) {
            try {
                findViewById(R.id.progress_layout).setVisibility(View.GONE);
                findViewById(R.id.image_view).setVisibility(View.VISIBLE);
                GlideApp.with(this).load(cache_path).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(getDrawable(R.drawable.photo_placeholder))
                        .dontAnimate().error(R.drawable.photo_loading_error)
                        .into((ZoomableImageView) findViewById(R.id.image_view));
                findViewById(R.id.image_view).setOnClickListener(
                        new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(appbar.getVisibility() == View.VISIBLE) {
                            appbar.animate().translationY(-appbar.getHeight()).alpha(0.0f)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            appbar.setVisibility(View.GONE);
                                        }
                                    });
                        } else {
                            appbar.setAlpha(1.0f);
                            appbar.animate().translationY(0).alpha(1.0f)
                                    .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    super.onAnimationStart(animation);
                                    appbar.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                });
            } catch (Exception err) {
                finish();
            }
        } else if(message == UiMessages.TOAST_SAVED_TO_MEMORY) {
            Toast.makeText(getApplicationContext(), R.string.photo_save_ok,
                    Toast.LENGTH_LONG).show();
        } else if(message == UiMessages.TOAST_SAVE_PHOTO_ERROR) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void savePhotoFromCache() {
        if(allowPermissionDialog()) {
            bfOptions = new BitmapFactory.Options();
            bfOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                new Thread(() -> {
                    try {
                        savePhotoFromCache(GlideApp.with(PhotoViewerActivity.this).asBitmap()
                                .load(cache_path).diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .submit().get());
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("resource")
    private void savePhotoFromCache(Bitmap bitmap) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        File file = new File(cache_path);
        String[] path_array = cache_path.split("/");
        String dest = String.format("%s/OpenVK/%s", Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(),
                path_array[path_array.length - 1]);
        String dest_dir = String.format("%s/OpenVK", Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        String mime = "image/jpeg";

        if (bitmap != null) {
            FileChannel sourceChannel = null;
            FileChannel destChannel = null;
            //dest = dest + ".jpg";
            try {
                File dirDest = new File(dest_dir);
                if(!dirDest.exists()) dirDest.mkdirs();
                FileInputStream fis = new FileInputStream(file);
                sourceChannel = fis.getChannel();
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                StringBuilder sb = new StringBuilder();
                char[] bytes = new char[64];
                if(br.read(bytes, 0, 32) != 0) {
                    sb.append(bytes);
                }
                fis.close();
                // reset FIS state
                fis = new FileInputStream(file);
                sourceChannel = fis.getChannel();
                // check file signature and returning valid MIME type
                mime = checkFileType(sb);
                if(mime.equals("image/jpeg")) {
                    dest = dest + ".jpg";
                } else if(mime.equals("image/png")) {
                    dest = dest + ".png";
                } else if(mime.equals("image/gif")) {
                    dest = dest + ".gif";
                }
                destChannel = new FileOutputStream(dest).getChannel();
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                handler.sendEmptyMessage(UiMessages.TOAST_SAVED_TO_MEMORY);
            } catch (IOException | SecurityException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(UiMessages.TOAST_SAVE_PHOTO_ERROR);
            } finally {
                try {
                    if (sourceChannel != null && destChannel != null) {
                        sourceChannel.close();
                        destChannel.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(UiMessages.TOAST_SAVE_PHOTO_ERROR);
                }
            }
            bitmap.recycle();
            bitmap = null;
        } else {
            handler.sendEmptyMessage(UiMessages.TOAST_SAVE_PHOTO_ERROR);
        }
    }

    private String checkFileType(StringBuilder sb) {
        if(sb != null) {
            if(!sb.toString().isEmpty()) {
                if (sb.toString().startsWith("ÿØÿÛ")
                        || sb.toString().contains("JFIF")
                        || sb.toString().startsWith("ÿØÿî")
                        || sb.toString().startsWith("ÿØÿà")
                        || sb.toString().startsWith("ÿØÿá")
                ) {
                    return "image/jpeg";
                } else if (sb.toString().contains("PNG")) {
                    return "image/png";
                } else if (sb.toString().startsWith("GIF87a")
                        || sb.toString().startsWith("GIF89a")) {
                    return "image/gif";
                } else {
                    return "";
                }
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    private boolean allowPermissionDialog() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if(!Environment.isExternalStorageManager()) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.setData(Uri.parse(String.format("package:%s",
                                getApplicationContext().getPackageName())));
                        startActivity(intent);
                    } catch (Exception e) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivity(intent);
                    }
                }
                return Environment.isExternalStorageManager();
            } else {
                int perm_r = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
                int perm_w = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
                if(perm_r != PackageManager.PERMISSION_GRANTED
                        && perm_w != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{
                            READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, 1);
                    perm_r = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
                    perm_w = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
                }
                return perm_r == PackageManager.PERMISSION_GRANTED && perm_w ==
                        PackageManager.PERMISSION_GRANTED;
            }
        } else {
            return true;
        }
    }
}
