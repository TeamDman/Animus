package com.teamdman.animus.client.resources;

import com.teamdman.animus.common.util.data.Tuple;

public class SpriteSheetResource {

    private final double uPart, vPart;

    private final int frameCount;
    private final int rows, columns;
    private final BindableResource resource;

    public SpriteSheetResource(BindableResource resource, int rows, int columns) {
        if(rows <= 0 || columns <= 0)
            throw new IllegalArgumentException("Can't instantiate a sprite sheet without any rows or columns!");

        frameCount = rows * columns;
        this.rows = rows;
        this.columns = columns;
        this.resource = resource;

        this.uPart = 1D / ((double) columns);
        this.vPart = 1D / ((double) rows);
    }

    public BindableResource getResource() {
        return resource;
    }

    public double getULength() {
        return uPart;
    }

    public double getVLength() {
        return vPart;
    }

    public Tuple<Double, Double> getUVOffset(int frameTimer) {
        int frame = frameTimer % frameCount;
        return new Tuple<Double, Double>((frame % columns) * uPart, (frame / columns) * vPart);
    }

    public int getFrameCount() {
        return frameCount;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }
}
