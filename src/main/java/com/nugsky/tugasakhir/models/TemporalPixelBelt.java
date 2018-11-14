package com.nugsky.tugasakhir.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TemporalPixelBelt {
    public int frameStart, frameEnd;
    public List<PixelLine> pixelLines;

    public TemporalPixelBelt(List<PixelLine> pixelLines) {
        this.pixelLines = pixelLines;
    }

    public TemporalPixelBelt() {
        pixelLines = new ArrayList<>();
    }

    public TemporalPixelBelt(int frameStart, int frameEnd) {
        this.frameStart = frameStart;
        this.frameEnd = frameEnd;
        pixelLines = new ArrayList<>();
    }

    public void addLines(PixelLine... pixelLines){
        Collections.addAll(this.pixelLines, pixelLines);
    }

    @Override
    public String toString() {
        return "TemporalPixelBelt{" +
                "frameStart=" + frameStart +
                ", frameEnd=" + frameEnd +
                ", pixelLines=" + Arrays.toString(pixelLines.toArray()) +
                '}';
    }
}
