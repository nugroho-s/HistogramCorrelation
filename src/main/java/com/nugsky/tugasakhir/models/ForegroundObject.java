package com.nugsky.tugasakhir.models;

import org.opencv.core.Rect;

public class ForegroundObject {
    int noFrame;        //no frame tempat objek muncul
    Rect rect;

    public ForegroundObject(int noFrame, Rect rect) {
        this.noFrame = noFrame;
        this.rect = rect;
    }
}
