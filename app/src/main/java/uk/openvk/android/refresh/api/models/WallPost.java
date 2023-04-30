package uk.openvk.android.refresh.api.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.attachments.Attachment;
import uk.openvk.android.refresh.api.counters.PostCounters;

public class WallPost implements Parcelable {

    public long dt_sec;
    private String avatar_url;
    public Bitmap avatar;
    public String name;
    public RepostInfo repost;
    public String info;
    public String text;
    public long owner_id;
    public long post_id;
    public PostCounters counters;
    public long author_id;
    public boolean verified_author;
    public ArrayList<Attachment> attachments;
    public WallPostSource post_source;

    @SuppressLint("SimpleDateFormat")
    public WallPost(String author, long dt_sec, RepostInfo repostInfo, String post_text,
                    PostCounters nICI, String avatar_url, ArrayList<Attachment> attachments,
                    long o_id, long p_id, Context ctx) {
        name = author;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(dt_sec));
        Date dt_midnight = new Date(System.currentTimeMillis() + 86400000);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt_midnight);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        this.dt_sec = dt_sec;
        if((calendar.getTimeInMillis() - (TimeUnit.SECONDS.toMillis(dt_sec))) < 86400000) {
            info = String.format("%s %s", ctx.getResources().getString(R.string.today_at), new SimpleDateFormat("HH:mm").format(dt));
        } else if((calendar.getTimeInMillis() - (TimeUnit.SECONDS.toMillis(dt_sec))) < (86400000 * 2)) {
            info = String.format("%s %s", ctx.getResources().getString(R.string.yesterday_at), new SimpleDateFormat("HH:mm").format(dt));
        } else if((calendar.getTimeInMillis() - (TimeUnit.SECONDS.toMillis(dt_sec))) < 31536000000L) {
            info = String.format("%s %s %s", new SimpleDateFormat("d MMMM").format(dt), ctx.getResources().getString(R.string.date_at),
                    new SimpleDateFormat("HH:mm").format(dt));
        } else {
            info = String.format("%s %s %s", new SimpleDateFormat("d MMMM yyyy").format(dt), ctx.getResources().getString(R.string.date_at),
                    new SimpleDateFormat("HH:mm").format(dt));
        }
        repost = repostInfo;
        counters = nICI;
        text = post_text;
        this.avatar_url = avatar_url;
        owner_id = o_id;
        post_id = p_id;
        this.attachments = attachments;
    }

    public WallPost() {

    }

    protected WallPost(Parcel in) {
        avatar_url = in.readString();
        //avatar = in.readParcelable(Bitmap.class.getClassLoader());
        name = in.readString();
        info = in.readString();
        text = in.readString();
        owner_id = in.readLong();
        post_id = in.readLong();
        author_id = in.readLong();
        verified_author = in.readInt() != 0;
        post_source = in.readParcelable(WallPostSource.class.getClassLoader());
    }

    public static final Creator<WallPost> CREATOR = new Creator<WallPost>() {
        @Override
        public WallPost createFromParcel(Parcel in) {
            return new WallPost(in);
        }

        @Override
        public WallPost[] newArray(int size) {
            return new WallPost[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(avatar_url);
        //parcel.writeParcelable(avatar, i);
        parcel.writeString(name);
        parcel.writeString(info);
        parcel.writeString(text);
        parcel.writeLong(owner_id);
        parcel.writeLong(post_id);
        parcel.writeLong(author_id);
        int verified_author_temp = 0;
        if(verified_author) {
            verified_author_temp = 1;
        }
        parcel.writeInt(verified_author_temp);
        parcel.writeParcelable(post_source, i);
    }
}
