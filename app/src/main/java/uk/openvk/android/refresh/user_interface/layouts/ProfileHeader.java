package uk.openvk.android.refresh.user_interface.layouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.preference.PreferenceManager;

import com.kieronquinn.monetcompat.core.MonetCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.user_interface.text.CenteredImageSpan;

public class ProfileHeader extends LinearLayoutCompat {

    private String name;
    private boolean online;

    public ProfileHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        @SuppressLint("InflateParams") View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.profile_header, null);
        this.addView(view);
        LinearLayoutCompat.LayoutParams layoutParams = (LinearLayoutCompat.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
        setTheme();
        ((TextView) findViewById(R.id.profile_name)).setTypeface(Global.getFlexibleTypeface(getContext(), 500));
    }

    private void setTheme() {
        if(Global.checkMonet(getContext())) {
            MonetCompat monet = MonetCompat.getInstance();
            boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("dark_theme", false);
            if(isDarkTheme) {
                ((ImageView) findViewById(R.id.verified_icon)).setImageTintList(
                        ColorStateList.valueOf(Objects.requireNonNull(
                                monet.getMonetColors().getAccent1().get(100)).toLinearSrgb().toSrgb().quantize8()));
            } else {
                ((ImageView) findViewById(R.id.verified_icon)).setImageTintList(
                        ColorStateList.valueOf(Objects.requireNonNull(
                                monet.getMonetColors().getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8()));
            }
        }
    }

    public void setProfileName(String name) {
        this.name = name;
        ((TextView) findViewById(R.id.profile_name)).setText(name);
    }

    public void setStatus(String status) {
        ((TextView) findViewById(R.id.profile_status)).setText(status);
    }

    @SuppressLint("SimpleDateFormat")
    public void setLastSeen(int sex, long date, int ls_platform) {
        if(online) {
            ((TextView) findViewById(R.id.last_seen)).setText(getResources().getString(R.string.online));
        } else {
            Date dt_midnight = new Date(System.currentTimeMillis() + 86400000);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dt_midnight);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            long dt_sec = (calendar.getTimeInMillis());
            Date dt = new Date(dt_sec);
            if((calendar.getTimeInMillis() - dt_sec) < 60000) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.last_seen)).setText(getResources().getString(R.string.last_seen_profile_f, getResources().getString(R.string.date_just_now)));
                } else {
                    ((TextView) findViewById(R.id.last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, getResources().getString(R.string.date_just_now)));
                }
            } else if((calendar.getTimeInMillis() - dt_sec) < 86400000) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.last_seen)).setText(getResources().getString(R.string.last_seen_profile_f, new SimpleDateFormat("HH:mm").format(dt)));
                } else {
                    ((TextView) findViewById(R.id.last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, new SimpleDateFormat("HH:mm").format(dt)));
                }
            } else if((calendar.getTimeInMillis() - dt_sec) < (86400000 * 2)) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.last_seen)).setText(getResources().getString(R.string.last_seen_profile_f, String.format("%s %s",
                            getResources().getString(R.string.yesterday_at), new SimpleDateFormat("HH:mm").format(dt))));
                } else {
                    ((TextView) findViewById(R.id.last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, String.format("%s %s",
                            getResources().getString(R.string.yesterday_at), new SimpleDateFormat("HH:mm").format(dt))));
                }
            } else if((calendar.getTimeInMillis() - dt_sec) < 31536000000L) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.last_seen)).setText(getResources().getString(R.string.last_seen_profile_f, String.format("%s %s %s",
                            new SimpleDateFormat("d MMMM").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
                } else {
                    ((TextView) findViewById(R.id.last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, String.format("%s %s %s",
                            new SimpleDateFormat("d MMMM").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
                }
            } else {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.last_seen)).setText(getResources().getString(R.string.last_seen_profile_f, String.format("%s %s %s",
                            new SimpleDateFormat("d MMMM yyyy").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
                } else {
                    ((TextView) findViewById(R.id.last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, String.format("%s %s %s",
                            new SimpleDateFormat("d MMMM").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
                }
            }
        }
//        ((ImageView) findViewById(R.id.profile_api_indicator)).setVisibility(VISIBLE);
//        if(ls_platform == 4) {
//            ((ImageView) findViewById(R.id.profile_api_indicator)).setImageDrawable(getResources().getDrawable(R.drawable.ic_api_android_app_indicator));
//        } else if(ls_platform == 2) {
//            ((ImageView) findViewById(R.id.profile_api_indicator)).setImageDrawable(getResources().getDrawable(R.drawable.ic_api_ios_app_indicator));
//        } else if(ls_platform == 1) {
//            ((ImageView) findViewById(R.id.profile_api_indicator)).setImageDrawable(getResources().getDrawable(R.drawable.ic_api_mobile_indicator));
//        } else {
//            ((ImageView) findViewById(R.id.profile_api_indicator)).setVisibility(GONE);
//        }
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setVerified(boolean verified, Context ctx) {
        if(verified) {
            findViewById(R.id.verified_icon).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.verified_icon).setVisibility(View.GONE);
        }
    }
}
