package uk.openvk.android.refresh.api.attachments;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;


public class PhotoAttachment implements Parcelable {
    public long id;
    public String url;
    public String original_url;
    public String filename;
    public boolean loaded;
    public String error;
    public Bitmap photo;
    public int displayW;
    public int displayH;

    public PhotoAttachment() {
    }

    protected PhotoAttachment(Parcel in) {
        url = in.readString();
        filename = in.readString();
        original_url = in.readString();
    }

    public static final Creator<PhotoAttachment> CREATOR = new Creator<PhotoAttachment>() {
        @Override
        public PhotoAttachment createFromParcel(Parcel in) {
            return new PhotoAttachment(in);
        }

        @Override
        public PhotoAttachment[] newArray(int size) {
            return new PhotoAttachment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(filename);
        parcel.writeString(original_url);
    }
}
