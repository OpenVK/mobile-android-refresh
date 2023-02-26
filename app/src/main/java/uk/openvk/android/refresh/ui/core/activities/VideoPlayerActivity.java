package uk.openvk.android.refresh.ui.core.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.preference.PreferenceManager;

import com.kieronquinn.monetcompat.app.MonetCompatActivity;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.media.VideoView;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.attachments.VideoAttachment;

public class VideoPlayerActivity extends MonetCompatActivity {
    private VideoAttachment video;
    private String url;
    private LibVLC vlc;
    private MediaPlayer mp;
    private VLCVideoLayout vlc_vl;
    private boolean ready;
    private int duration;
    private int pos;
    private boolean seekPressed;
    private SharedPreferences global_prefs;
    private Window window;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"));
        Global.setInterfaceFont(this);
        window = getWindow();
        setContentView(R.layout.video_player);
        resizeControlPanel();
        findViewById(R.id.loading_card).setVisibility(View.GONE);
        loadVideo();
    }

    private void resizeControlPanel() {

    }

    private void loadVideo() {
        Bundle data = getIntent().getExtras();
        showPlayControls();
        findViewById(R.id.view_vlc_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlayControls();
            }
        });
        if(data != null) {
            if(data.containsKey("attachment")) {
                video = data.getParcelable("attachment");
                assert video != null;
            } if(data.containsKey("files")) {
                video.files = data.getParcelable("files");
                assert video.files != null;
                if(video.files.ogv_480 != null && video.files.ogv_480.length() > 0) {
                    url = video.files.ogv_480;
                } if(video.files.mp4_144 != null && video.files.mp4_144.length() > 0) {
                    url = video.files.mp4_144;
                } if(video.files.mp4_240 != null && video.files.mp4_240.length() > 0) {
                    url = video.files.mp4_240;
                } if(video.files.mp4_360 != null && video.files.mp4_360.length() > 0) {
                    url = video.files.mp4_360;
                } if(video.files.mp4_480 != null && video.files.mp4_480.length() > 0) {
                    url = video.files.mp4_480;
                } if(video.files.mp4_720 != null && video.files.mp4_720.length() > 0) {
                    url = video.files.mp4_720;
                } if(video.files.mp4_1080 != null && video.files.mp4_1080.length() > 0) {
                    url = video.files.mp4_1080;
                }

                if(url == null) {
                    url = "";
                }

                Uri uri = Uri.parse(url);

                createMediaPlayer(uri);
                ((ImageButton) findViewById(R.id.play_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playVideo();
                    }
                });
            }
        } else {
            finish();
        }
    }

    private void playVideo() {
        if(mp.isPlaying()) {
            mp.pause();
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            mp.play();
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @SuppressLint("DefaultLocale")
    private void createMediaPlayer(Uri uri) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Global.adjustAlpha(Color.BLACK, 0.5f));
        ((TextView) findViewById(R.id.timecode)).setText(String.format("%d:%02d / %d:%02d", pos / 60, pos % 60, duration / 60, duration % 60));
        vlc = new LibVLC(this);
        mp = new MediaPlayer(vlc);
        showPlayControls();
        mp.setEventListener(new MediaPlayer.EventListener() {
            @SuppressLint({"UseCompatLoadingForDrawables", "DefaultLocale"})
            @Override
            public void onEvent(MediaPlayer.Event event) {
                if(event.type == MediaPlayer.Event.Buffering) {
                    findViewById(R.id.loading_card).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.loading_card).setVisibility(View.GONE);
                }
                if(event.type == MediaPlayer.Event.Paused) {
                    ((ImageButton) findViewById(R.id.play_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_play_large));
                } else if(event.type == MediaPlayer.Event.Playing) {
                    ((ImageButton) findViewById(R.id.play_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_large));
                }
                if(event.type == MediaPlayer.Event.EndReached) {
                    mp.stop();
                    ((ImageButton) findViewById(R.id.play_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_play_large));
                }

                if(event.type == MediaPlayer.Event.LengthChanged) {
                    duration = (int) (mp.getLength() / 1000);
                    ((SeekBar) findViewById(R.id.seekbar)).setMax(duration);
                }

                if(event.type == MediaPlayer.Event.EncounteredError) {
                    mp.release();
                    finish();
                }

            }
        });
        vlc_vl = findViewById(R.id.view_vlc_layout);
        if (uri == null) {
            return;
        }
        try {
            mp.attachViews(vlc_vl, null, false, false);
            Media media = new Media(vlc, uri);
            media.setHWDecoderEnabled(true, false);
            mp.setMedia(media);
            media.release();
            playVideo();
            ((SeekBar) findViewById(R.id.seekbar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUser) {
                        seekPressed = true;
                        mp.setTime(progress * 1000L);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    seekPressed = false;
                }
            });
            new Handler(Looper.myLooper()).post(new Runnable() {
                @SuppressLint("DefaultLocale")
                @Override
                public void run() {
                    try {
                        if (mp.isPlaying()) {
                            pos = (int) (mp.getTime() / 1000);
                            if (!seekPressed) {
                                ((SeekBar) findViewById(R.id.seekbar)).setProgress(pos);
                            }
                            ((TextView) findViewById(R.id.timecode)).setText(String.format("%d:%02d / %d:%02d", pos / 60, pos % 60, duration / 60, duration % 60));
                        }
                        new Handler(Looper.myLooper()).postDelayed(this, 200);
                    } catch (Exception ignored) {
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPlayControls() {
        if(findViewById(R.id.player_controls).getVisibility() == View.VISIBLE) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            findViewById(R.id.player_controls).setVisibility(View.GONE);
            View decorView = getWindow().getDecorView();
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            findViewById(R.id.player_controls).setVisibility(View.VISIBLE);
            View decorView = getWindow().getDecorView();
            new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(findViewById(R.id.player_controls).getVisibility() == View.VISIBLE) {
                        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        findViewById(R.id.player_controls).setVisibility(View.GONE);
                        View decorView = getWindow().getDecorView();
                    }
                }
            }, 5000);
        }
    }

    @Override
    protected void onPause() {
        if(mp.isPlaying()) mp.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mp.stop();
        mp.release();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
