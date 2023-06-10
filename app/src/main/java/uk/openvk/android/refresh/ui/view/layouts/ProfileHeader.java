package uk.openvk.android.refresh.ui.view.layouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.ui.core.enumerations.PublicPageCounters;

public class ProfileHeader extends LinearLayoutCompat {

    private String name;
    private boolean online;
    private String lastSeen;

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
        ((Button) findViewById(R.id.show_friends_btn)).setTypeface(
                Global.getFlexibleTypeface(context, 500));
        ((TextView) findViewById(R.id.profile_name)).setTypeface(
                Global.getFlexibleTypeface(getContext(), 500));
        lastSeen = "";
    }

    private void setTheme() {
        if(Global.checkMonet(getContext())) {
            MonetCompat monet = MonetCompat.getInstance();
            boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getBoolean("dark_theme", false);
            if(isDarkTheme) {
                ((ImageView) findViewById(R.id.verified_icon)).setImageTintList(
                        ColorStateList.valueOf(
                                Global.getMonetIntColor(monet, "accent", 200)));
            } else {
                ((ImageView) findViewById(R.id.verified_icon)).setImageTintList(
                        ColorStateList.valueOf(
                                Global.getMonetIntColor(monet, "accent", 500)));
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
            this.lastSeen = getResources().getString(R.string.online);
        } else if(date > 0){
            Date dt_midnight = new Date(System.currentTimeMillis() + 86400000);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dt_midnight.getTime());
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            long dt_ms = (TimeUnit.SECONDS.toMillis(date));
            Date dt = new Date(dt_ms);
            if((calendar.getTimeInMillis() - dt_ms) < 60000) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.last_seen))
                            .setText(getResources().getString(R.string.last_seen_profile_f,
                                    getResources().getString(R.string.date_just_now)));
                    this.lastSeen = getResources().getString(R.string.last_seen_profile_f,
                            getResources().getString(R.string.date_just_now));
                } else {
                    ((TextView) findViewById(R.id.last_seen))
                            .setText(getResources().getString(R.string.last_seen_profile_m, getResources().getString(R.string.date_just_now)));
                    this.lastSeen = getResources().getString(R.string.last_seen_profile_m,
                            getResources().getString(R.string.date_just_now));
                }
            } else if((calendar.getTimeInMillis() - dt_ms) < 86400000) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.last_seen))
                            .setText(getResources().getString(R.string.last_seen_profile_f,
                                    new SimpleDateFormat("HH:mm").format(dt)));
                    this.lastSeen = getResources().getString(R.string.last_seen_profile_f,
                            new SimpleDateFormat("HH:mm").format(dt));
                } else {
                    ((TextView) findViewById(R.id.last_seen))
                            .setText(getResources().getString(R.string.last_seen_profile_m,
                                    new SimpleDateFormat("HH:mm").format(dt)));
                    this.lastSeen = getResources().getString(R.string.last_seen_profile_m,
                            new SimpleDateFormat("HH:mm").format(dt));
                }
            } else if((calendar.getTimeInMillis() - dt_ms) < (86400000 * 2)) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.last_seen))
                            .setText(getResources().getString(R.string.last_seen_profile_f,
                                    String.format("%s %s", getResources().getString(R.string.yesterday_at),
                                            new SimpleDateFormat("HH:mm").format(dt))));
                    this.lastSeen = getResources().getString(R.string.last_seen_profile_f,
                            String.format("%s %s", getResources().getString(R.string.yesterday_at),
                                    new SimpleDateFormat("HH:mm").format(dt)));
                } else {
                    ((TextView) findViewById(R.id.last_seen))
                            .setText(getResources().getString(R.string.last_seen_profile_m,
                                    String.format("%s %s", getResources().getString(R.string.yesterday_at),
                                            new SimpleDateFormat("HH:mm").format(dt))));
                    this.lastSeen = getResources().getString(R.string.last_seen_profile_m,
                            String.format("%s %s", getResources().getString(R.string.yesterday_at),
                                    new SimpleDateFormat("HH:mm").format(dt)));
                }
            } else if((calendar.getTimeInMillis() - dt_ms) < 31536000000L) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.last_seen))
                            .setText(getResources().getString(R.string.last_seen_profile_f,
                                    String.format("%s %s %s", new SimpleDateFormat("d MMMM")
                                            .format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
                    this.lastSeen = getResources().getString(R.string.last_seen_profile_f,
                            String.format("%s %s %s", new SimpleDateFormat("d MMMM")
                                    .format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt)));
                } else {
                    ((TextView) findViewById(R.id.last_seen))
                            .setText(getResources().getString(R.string.last_seen_profile_m,
                                    String.format("%s %s %s", new SimpleDateFormat("d MMMM").format(dt),
                                            getResources().getString(R.string.date_at),
                                            new SimpleDateFormat("HH:mm").format(dt))));
                    this.lastSeen = getResources().getString(R.string.last_seen_profile_m,
                            String.format("%s %s %s", new SimpleDateFormat("d MMMM").format(dt),
                                    getResources().getString(R.string.date_at),
                                    new SimpleDateFormat("HH:mm").format(dt)));
                }
            } else {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.last_seen))
                            .setText(getResources().getString(R.string.last_seen_profile_f,
                                    String.format("%s %s %s", new SimpleDateFormat("d MMMM yyyy").format(dt),
                                            getResources().getString(R.string.date_at),
                                    new SimpleDateFormat("HH:mm").format(dt))));
                    this.lastSeen = getResources().getString(R.string.last_seen_profile_f,
                            String.format("%s %s %s", new SimpleDateFormat("d MMMM yyyy").format(dt),
                                    getResources().getString(R.string.date_at),
                                    new SimpleDateFormat("HH:mm").format(dt)));
                } else {
                    ((TextView) findViewById(R.id.last_seen))
                            .setText(getResources().getString(R.string.last_seen_profile_m,
                                    String.format("%s %s %s", new SimpleDateFormat("d MMMM")
                                            .format(dt),
                                            getResources().getString(R.string.date_at),
                                            new SimpleDateFormat("HH:mm").format(dt))));
                    this.lastSeen = getResources().getString(R.string.last_seen_profile_m,
                            String.format("%s %s %s", new SimpleDateFormat("d MMMM")
                                            .format(dt),
                                    getResources().getString(R.string.date_at),
                                    new SimpleDateFormat("HH:mm").format(dt)));
                }
            }
        } else {
            ((TextView) findViewById(R.id.last_seen)).setText("");
            this.lastSeen = "";
        }
//        ((ImageView) findViewById(R.id.profile_api_indicator))
//        .setVisibility(VISIBLE);
//        if(ls_platform == 4) {
//            ((ImageView) findViewById(R.id.profile_api_indicator))
//            .setImageDrawable(getResources().getDrawable(R.drawable.ic_api_android_app_indicator));
//        } else if(ls_platform == 2) {
//            ((ImageView) findViewById(R.id.profile_api_indicator))
//            .setImageDrawable(getResources().getDrawable(R.drawable.ic_api_ios_app_indicator));
//        } else if(ls_platform == 1) {
//            ((ImageView) findViewById(R.id.profile_api_indicator))
//            .setImageDrawable(getResources().getDrawable(R.drawable.ic_api_mobile_indicator));
//        } else {
//            ((ImageView) findViewById(R.id.profile_api_indicator))
//            .setVisibility(GONE);
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

    public void hideSendMessageButton() {
        findViewById(R.id.send_msg_btn).setVisibility(GONE);
    }

    public void setCountersVisibility(int counter, boolean value) {
        if(counter == PublicPageCounters.FRIENDS) {
            if(value) {
                findViewById(R.id.show_friends_btn).setVisibility(VISIBLE);
            } else {
                findViewById(R.id.show_friends_btn).setVisibility(GONE);
            }
        } else if(counter == PublicPageCounters.MEMBERS) {
            if(value) {
                findViewById(R.id.show_members_btn).setVisibility(VISIBLE);
            } else {
                findViewById(R.id.show_members_btn).setVisibility(GONE);
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setAddToFriendsButtonVisibility(int status) {
        if(status == 3) {
            findViewById(R.id.add_to_btn).setVisibility(GONE);
        } else if(status == 2) {
            ((MaterialButton) findViewById(R.id.add_to_btn)).setIcon(
                    getResources().getDrawable(R.drawable.ic_person_add));
            findViewById(R.id.add_to_btn).setVisibility(VISIBLE);
        } else if(status == 1) {
            ((MaterialButton) findViewById(R.id.add_to_btn)).setIcon(
                    getResources().getDrawable(R.drawable.ic_person_remove));
            findViewById(R.id.add_to_btn).setVisibility(VISIBLE);
        } else {
            ((MaterialButton) findViewById(R.id.add_to_btn)).setIcon(
                    getResources().getDrawable(R.drawable.ic_person_add));
            findViewById(R.id.add_to_btn).setVisibility(VISIBLE);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setJoinButtonVisibility(int status) {
        if(status > 0) {
            findViewById(R.id.add_to_btn).setVisibility(GONE);
        } else {
            ((MaterialButton) findViewById(R.id.add_to_btn)).setIcon(getResources().getDrawable(R.drawable.ic_person_add));
            findViewById(R.id.add_to_btn).setVisibility(VISIBLE);
        }
    }

    public void setJoinButtonOnClickListener(OnClickListener onClickListener) {
        findViewById(R.id.add_to_btn).setOnClickListener(onClickListener);
    }

    public String getOnline() {
        return lastSeen;
    }
}
