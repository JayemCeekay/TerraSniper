package com.jayemceekay.TerraSniper.util.painter;

import com.sk89q.worldedit.math.BlockVector3;

public class CubePainter implements Painter {
    private BlockVector3 center;
    private int radius;
    private BlockSetter blockSetter;

    public CubePainter() {
    }

    public CubePainter center(BlockVector3 center) {
        this.center = center;
        return this;
    }

    public CubePainter radius(int radius) {
        this.radius = radius;
        return this;
    }

    public CubePainter blockSetter(BlockSetter blockSetter) {
        this.blockSetter = blockSetter;
        return this;
    }

    public void paint() {
        if (this.center == null) {
            throw new RuntimeException("Center must be specified");
        } else if (this.blockSetter == null) {
            throw new RuntimeException("Block setter must be specified");
        } else {
            this.paintCube();
        }
    }

    private void paintCube() {
        Painters.block(this).at(0, 0, 0).paint();

        for (int first = 1; first <= this.radius; ++first) {
            Painters.block(this).at(first, 0, 0).at(-first, 0, 0).at(0, first, 0).at(0, -first, 0).at(0, 0, first).at(0, 0, -first).paint();

            for (int second = 1; second <= this.radius; ++second) {
                Painters.block(this).at(first, second, 0).at(first, -second, 0).at(-first, second, 0).at(-first, -second, 0).at(first, 0, second).at(first, 0, -second).at(-first, 0, second).at(-first, 0, -second).at(0, first, second).at(0, first, -second).at(0, -first, second).at(0, -first, -second).paint();

                for (int third = 1; third <= this.radius; ++third) {
                    Painters.block(this).at(first, second, third).at(first, second, -third).at(first, -second, third).at(first, -second, -third).at(-first, second, third).at(-first, second, -third).at(-first, -second, third).at(-first, -second, -third).paint();
                }
            }
        }

    }

    public BlockVector3 getCenter() {
        return this.center;
    }

    public int getRadius() {
        return this.radius;
    }

    public BlockSetter getBlockSetter() {
        return this.blockSetter;
    }
}
