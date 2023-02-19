package uk.openvk.android.refresh.api.attachments;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Attachment implements Parcelable {
    public String type;
    public String status;
    private Object content;
    public Attachment(String type) {
        this.type = type;
        switch (type) {
            case "photo":
                content = new PhotoAttachment();
                break;
            case "poll":
                content = new PollAttachment();
                break;
            default:
                content = null;
                break;
        }
    }

    protected Attachment(Parcel in) {
        type = in.readString();
        status = in.readString();
    }

    public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
        @Override
        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(status);
    }
}
