package com.nugsky.tugasakhir.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TemporalPixelBelt {
    public int frameNo;
    public List<PixelLine> pixelLines;

    public TemporalPixelBelt(List<PixelLine> pixelLines) {
        this.pixelLines = pixelLines;
    }

    public TemporalPixelBelt(int frameNo) {
        this.frameNo = frameNo;
        pixelLines = new ArrayList<>();
    }

    public void addLines(PixelLine... pixelLines){
        Collections.addAll(this.pixelLines, pixelLines);
    }
}
