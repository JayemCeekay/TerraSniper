package com.jayemceekay.terrasniper.util.painter;

import com.jayemceekay.terrasniper.util.math.MathHelper;
import com.sk89q.worldedit.math.BlockVector3;

public class SpherePainter implements Painter {
    private static final double TRUE_CIRCLE_ADDITIONAL_RADIUS = 0.5D;
    private BlockVector3 center;
    private int radius;
    private boolean trueCircle;
    private BlockSetter blockSetter;
    private BlockVector3 offsetVector=null;
    private boolean centerBlock=false;
    private String shape="full";

    public SpherePainter() {
    }

    public SpherePainter center(BlockVector3 center) {
        this.center = center;
        return this;
    }

    public SpherePainter radius(int radius) {
        this.radius = radius;
        return this;
    }

    public SpherePainter trueCircle() {
        return this.trueCircle(true);
    }

    public SpherePainter trueCircle(boolean trueCircle) {
        this.trueCircle = trueCircle;
        return this;
    }

    public SpherePainter offsetVector(BlockVector3 offsetVector) {
        this.offsetVector = offsetVector;
        return this;
    }

    public SpherePainter shape(String shape) {
        this.shape = shape;
        return this;
    }

    public SpherePainter centerBlock(boolean centerBlock) {
        this.centerBlock = centerBlock;
        return this;
    }

    public SpherePainter blockSetter(BlockSetter blockSetter) {
        this.blockSetter = blockSetter;
        return this;
    }

    public void paint() {
        if (this.center == null) {
            throw new RuntimeException("Center must be specified");
        } else if (this.blockSetter == null) {
            throw new RuntimeException("Block setter must be specified");
        } else {
            this.paintSphere();
        }
    }

    private void paintSphere() {
        int xDirection = 0;
        int yDirection = 0;
        int zDirection = 0;
        switch(shape) {
            case "up"   : yDirection= 1; break;
            case "down" : yDirection=-1; break;
            case "east" : xDirection= 1; break;
            case "west" : xDirection=-1; break;
            case "south": zDirection= 1; break;
            case "north": zDirection=-1; break;
        }
        if(centerBlock) {
            double coordOffset = 1.0D/2.0D;
            this.center = this.center.multiply(2).subtract(this.offsetVector);
            // this.center now targets the eighth-sub-block of the target block with highest x/y/z coordinates

            double radiusSquared = MathHelper.square((this.trueCircle ? (double) this.radius + 0.5D : (double) this.radius) + coordOffset);

            for (int first = 1; first <= this.radius + 1; ++first)       { double firstSquared = MathHelper.square(first - coordOffset);
                for (int second = 1; second <= this.radius + 1; ++second)  { double secondSquared = MathHelper.square(second - coordOffset);
                    for (int third = 1; third <= this.radius + 1; ++third)   { double thirdSquared = MathHelper.square(third - coordOffset);
                        if (firstSquared + secondSquared + thirdSquared <= radiusSquared) {
                            if (xDirection>=0 || first==1) {
                                if (yDirection>=0 || second==1) {
                                    if (zDirection>=0 || third==1) {Painters.block(this).at(first, second, third).paint();}
                                    if (zDirection<=0 || third==1) {Painters.block(this).at(first, second, -third+1).paint();}
                                }
                                if (yDirection<=0 || second==1) {
                                    if (zDirection>=0 || third==1) {Painters.block(this).at(first, -second+1, third).paint();}
                                    if (zDirection<=0 || third==1) {Painters.block(this).at(first, -second+1, -third+1).paint();}
                                }
                            }
                            if (xDirection<=0 || first==1) {
                                if (yDirection>=0 || second==1) {
                                    if (zDirection>=0 || third==1) {Painters.block(this).at(-first+1, second, third).paint();}
                                    if (zDirection<=0 || third==1) {Painters.block(this).at(-first+1, second, -third+1).paint();}
                                }
                                if (yDirection<=0 || second==1) {
                                    if (zDirection>=0 || third==1) {Painters.block(this).at(-first+1, -second+1, third).paint();}
                                    if (zDirection<=0 || third==1) {Painters.block(this).at(-first+1, -second+1, -third+1).paint();}
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            Painters.block(this).at(0, 0, 0).paint();
            double radiusSquared = MathHelper.square((this.trueCircle ? (double) this.radius + 0.5D : (double) this.radius));

            for (int first = 1; first <= this.radius; ++first) {
                double firstSquared = MathHelper.square(first);
                if (firstSquared <= radiusSquared) {
                    if(xDirection>=0) {Painters.block(this).at(first, 0, 0).paint();}
                    if(xDirection<=0) {Painters.block(this).at(-first, 0, 0).paint();}
                    if(yDirection>=0) {Painters.block(this).at(0, first, 0).paint();}
                    if(yDirection<=0) {Painters.block(this).at(0, -first, 0).paint();}
                    if(zDirection>=0) {Painters.block(this).at(0, 0, first).paint();}
                    if(zDirection<=0) {Painters.block(this).at(0, 0, -first).paint();}
                }

                for (int second = 1; second <= this.radius; ++second) {
                    double secondSquared = MathHelper.square(second);
                    if (firstSquared + secondSquared <= radiusSquared) {
                        if (xDirection>=0) {
                            if (yDirection>=0) {Painters.block(this).at(first, second, 0).paint();}
                            if (yDirection<=0) {Painters.block(this).at(first, -second, 0).paint();}
                            if (zDirection>=0) {Painters.block(this).at(first, 0, second).paint();}
                            if (zDirection<=0) {Painters.block(this).at(first, 0, -second).paint();}
                        }
                        if (xDirection<=0) {
                            if (yDirection>=0) {Painters.block(this).at(-first, second, 0).paint();}
                            if (yDirection<=0) {Painters.block(this).at(-first, -second, 0).paint();}
                            if (zDirection>=0) {Painters.block(this).at(-first, 0, second).paint();}
                            if (zDirection<=0) {Painters.block(this).at(-first, 0, -second).paint();}
                        }
                        if (yDirection>=0) {
                            if (zDirection>=0) {Painters.block(this).at(0, first, second).paint();}
                            if (zDirection<=0) {Painters.block(this).at(0, first, -second).paint();}
                        }
                        if (yDirection<=0) {
                            if (zDirection>=0) {Painters.block(this).at(0, -first, second).paint();}
                            if (zDirection<=0) {Painters.block(this).at(0, -first, -second).paint();}
                        }
                    }

                    for (int third = 1; third <= this.radius; ++third) {
                        double thirdSquared = MathHelper.square(third);
                        if (firstSquared + secondSquared + thirdSquared <= radiusSquared) {
                            if (xDirection>=0) {
                                if (yDirection>=0) {
                                    if (zDirection>=0) {Painters.block(this).at(first, second, third).paint();}
                                    if (zDirection<=0) {Painters.block(this).at(first, second, -third).paint();}
                                }
                                if (yDirection<=0) {
                                    if (zDirection>=0) {Painters.block(this).at(first, -second, third).paint();}
                                    if (zDirection<=0) {Painters.block(this).at(first, -second, -third).paint();}
                                }
                            }
                            if (xDirection<=0) {
                                if (yDirection>=0) {
                                    if (zDirection>=0) {Painters.block(this).at(-first, second, third).paint();}
                                    if (zDirection<=0) {Painters.block(this).at(-first, second, -third).paint();}
                                }
                                if (yDirection<=0) {
                                    if (zDirection>=0) {Painters.block(this).at(-first, -second, third).paint();}
                                    if (zDirection<=0) {Painters.block(this).at(-first, -second, -third).paint();}
                                }
                            }
                        }
                    }
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

    public boolean isTrueCircle() {
        return this.trueCircle;
    }

    public BlockSetter getBlockSetter() {
        return this.blockSetter;
    }
}
