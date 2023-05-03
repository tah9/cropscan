package com.nuist.cropscan.ActPicture.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author tah9  2021/3/14 20:58
 */
public class PictureBean implements Parcelable {
    public String path;
    public long time;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "PictureBean{" +
                "path='" + path + '\'' +
                ", time=" + time +
                '}';
    }

    public PictureBean(String path, long time) {
        this.path = path;
        this.time = time;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeLong(this.time);
    }

    public PictureBean() {
    }

    protected PictureBean(Parcel in) {
        this.path = in.readString();
        this.time = in.readLong();
    }

    public static final Creator<PictureBean> CREATOR = new Creator<PictureBean>() {
        @Override
        public PictureBean createFromParcel(Parcel source) {
            return new PictureBean(source);
        }

        @Override
        public PictureBean[] newArray(int size) {
            return new PictureBean[size];
        }
    };

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
