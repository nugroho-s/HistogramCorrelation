package com.nugsky.tugasakhir.models;

public class PixelLine {
    public boolean isHorizontal;
    public int location;

    public PixelLine(boolean isHorizontal, int location) {
        this.isHorizontal = isHorizontal;
        this.location = location;
    }

    @Override
    public String toString() {
        return "PixelLine{" +
                "isHorizontal=" + isHorizontal +
                ", location=" + location +
                '}';
    }
}
