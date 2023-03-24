package com.nuist.cropscan;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author tah9  2021/3/14 20:58
 */
public class PcPathBean implements Parcelable {
    public String path;
    public long time;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "PcPathBean{" +
                "path='" + path + '\'' +
                ", time=" + time +
                '}';
    }

    public PcPathBean(String path, long time) {
        this.path = path;
        this.time = time;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeLong(this.time);
    }

    public PcPathBean() {
    }

    protected PcPathBean(Parcel in) {
        this.path = in.readString();
        this.time = in.readLong();
    }

    public static final Creator<PcPathBean> CREATOR = new Creator<PcPathBean>() {
        @Override
        public PcPathBean createFromParcel(Parcel source) {
            return new PcPathBean(source);
        }

        @Override
        public PcPathBean[] newArray(int size) {
            return new PcPathBean[size];
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
