package com.nuist.gallery.bean;

/**
 * ->  tah9  2023/5/3 19:38
 */
public class FolderBean {
    String name;
    String firstPath;
    String path;

    int size;
    long time;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstPath() {
        return firstPath;
    }

    public void setFirstPath(String firstPath) {
        this.firstPath = firstPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "FolderBean{" +
                "name='" + name + '\'' +
                ", firstPath='" + firstPath + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", time=" + time +
                '}';
    }

    public FolderBean(String name, String firstPath, String path, int size, long time) {
        this.name = name;
        this.firstPath = firstPath;
        this.path = path;
        this.size = size;
        this.time = time;
    }
}
