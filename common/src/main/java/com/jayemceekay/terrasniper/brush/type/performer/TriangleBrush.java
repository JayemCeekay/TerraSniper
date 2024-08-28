package com.jayemceekay.terrasniper.brush.type.performer;

import com.jayemceekay.terrasniper.sniper.snipe.Snipe;
import com.jayemceekay.terrasniper.sniper.snipe.message.SnipeMessenger;
import com.jayemceekay.terrasniper.util.math.MathHelper;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import net.minecraft.ChatFormatting;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class TriangleBrush extends AbstractPerformerBrush {
    private final double[] coordinatesOneFullBlock = new double[3];
    private final double[] coordinatesTwoFullBlock = new double[3];
    private final double[] coordinatesThreeFullBlock = new double[3];
    private final double[] coordinatesOffsetOne = new double[3];
    private final double[] coordinatesOffsetTwo = new double[3];
    private final double[] coordinatesOffsetThree = new double[3];
    private final double[] coordinatesOne = new double[3];
    private final double[] coordinatesTwo = new double[3];
    private final double[] coordinatesThree = new double[3];
    private final double[] currentCoordinates = new double[3];
    private final double[] vectorOne = new double[3];
    private final double[] vectorTwo = new double[3];
    private final double[] vectorThree = new double[3];
    private final double[] normalVector = new double[3];
    private int cornerNumber = 1;

    public TriangleBrush() {
    }

    public void loadProperties() {
    }

    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        String firstParameter = parameters[0];
        if (firstParameter.equalsIgnoreCase("info")) {
            messenger.sendMessage(ChatFormatting.GOLD + "Triangle Brush instructions: Select three corners with the arrow brush, then generate the triangle with the gunpowder brush.");
        }

    }

    public void handleArrowAction(Snipe snipe) {
        this.triangleA(snipe);
    }

    public void handleGunpowderAction(Snipe snipe) {
        if(this.useSmallBlocks) {
            double[] offset = new double[3];
            offset[0] = this.offsetVector.getX();
            offset[1] = this.offsetVector.getY();
            offset[2] = this.offsetVector.getZ();
            for (int i=0; i<3; i++) {
                this.coordinatesOne[i]   = this.coordinatesOneFullBlock[i]   + this.coordinatesOffsetOne[i]   - offset[i];
                this.coordinatesTwo[i]   = this.coordinatesTwoFullBlock[i]   + this.coordinatesOffsetTwo[i]   - offset[i];
                this.coordinatesThree[i] = this.coordinatesThreeFullBlock[i] + this.coordinatesOffsetThree[i] - offset[i];
            }
        }
        else {
            for (int i=0; i<3; i++) {
                this.coordinatesOne[i]   = this.coordinatesOneFullBlock[i];
                this.coordinatesTwo[i]   = this.coordinatesTwoFullBlock[i];
                this.coordinatesThree[i] = this.coordinatesThreeFullBlock[i];
            }
        }
        for (int i=0; i<=2; i+=2) {
            this.coordinatesOne[i]   += 0.5D * coordinatesOne[i]   / Math.abs(coordinatesOne[i]);
            this.coordinatesTwo[i]   += 0.5D * coordinatesTwo[i]   / Math.abs(coordinatesTwo[i]);
            this.coordinatesThree[i] += 0.5D * coordinatesThree[i] / Math.abs(coordinatesThree[i]);
        }
        this.coordinatesOne[1]   += 0.5D;
        this.coordinatesTwo[1]   += 0.5D;
        this.coordinatesThree[1] += 0.5D;
        this.triangleP(snipe);
    }

    private void triangleA(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        BlockVector3 targetBlock = this.getTargetBlock();
        int targetBlockX = targetBlock.getX();
        int targetBlockY = targetBlock.getY();
        int targetBlockZ = targetBlock.getZ();
        switch (this.cornerNumber) {
            case 1:
                this.coordinatesOneFullBlock[0] = targetBlockX;
                this.coordinatesOneFullBlock[1] = targetBlockY;
                this.coordinatesOneFullBlock[2] = targetBlockZ;
                this.setOffsetVector(snipe);
                this.coordinatesOffsetOne[0] = this.offsetVector.getX();
                this.coordinatesOffsetOne[1] = this.offsetVector.getY();
                this.coordinatesOffsetOne[2] = this.offsetVector.getZ();
                this.cornerNumber = 2;
                messenger.sendMessage(ChatFormatting.GRAY + "First Corner set.");
                break;
            case 2:
                this.coordinatesTwoFullBlock[0] = targetBlockX;
                this.coordinatesTwoFullBlock[1] = targetBlockY;
                this.coordinatesTwoFullBlock[2] = targetBlockZ;
                this.setOffsetVector(snipe);
                this.coordinatesOffsetTwo[0] = this.offsetVector.getX();
                this.coordinatesOffsetTwo[1] = this.offsetVector.getY();
                this.coordinatesOffsetTwo[2] = this.offsetVector.getZ();
                this.cornerNumber = 3;
                messenger.sendMessage(ChatFormatting.GRAY + "Second Corner set.");
                break;
            case 3:
                this.coordinatesThreeFullBlock[0] = targetBlockX;
                this.coordinatesThreeFullBlock[1] = targetBlockY;
                this.coordinatesThreeFullBlock[2] = targetBlockZ;
                this.setOffsetVector(snipe);
                this.coordinatesOffsetThree[0] = this.offsetVector.getX();
                this.coordinatesOffsetThree[1] = this.offsetVector.getY();
                this.coordinatesOffsetThree[2] = this.offsetVector.getZ();
                this.cornerNumber = 1;
                messenger.sendMessage(ChatFormatting.GRAY + "Third Corner set.");
        }

    }

    private void triangleP(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();

        for (int index = 0; index < 3; ++index) {
            this.vectorOne[index] = this.coordinatesTwo[index] - this.coordinatesOne[index];
            this.vectorTwo[index] = this.coordinatesThree[index] - this.coordinatesOne[index];
            this.vectorThree[index] = this.coordinatesThree[index] - this.coordinatesTwo[index];
        }

        this.normalVector[0] = this.vectorOne[1] * this.vectorTwo[2] - this.vectorOne[2] * this.vectorTwo[1];
        this.normalVector[1] = this.vectorOne[2] * this.vectorTwo[0] - this.vectorOne[0] * this.vectorTwo[2];
        this.normalVector[2] = this.vectorOne[0] * this.vectorTwo[1] - this.vectorOne[1] * this.vectorTwo[0];
        double lengthOne = Math.sqrt(IntStream.of(0, 1, 2).mapToDouble((number) -> {
            return MathHelper.square(this.vectorOne[number]);
        }).sum());
        double lengthTwo = Math.sqrt(IntStream.of(0, 1, 2).mapToDouble((number) -> {
            return MathHelper.square(this.vectorTwo[number]);
        }).sum());
        double lengthThree = Math.sqrt(IntStream.of(0, 1, 2).mapToDouble((number) -> {
            return MathHelper.square(this.vectorThree[number]);
        }).sum());
        int brushSize = (int) Math.ceil(Math.max(lengthOne, lengthTwo));
        double planeConstant = this.normalVector[0] * this.coordinatesOne[0] + this.normalVector[1] * this.coordinatesOne[1] + this.normalVector[2] * this.coordinatesOne[2];
        double heronBig = 0.25D * Math.sqrt(MathHelper.square(DoubleStream.of(new double[]{lengthOne, lengthTwo, lengthThree}).map(MathHelper::square).sum()) - 2.0D * DoubleStream.of(new double[]{lengthOne, lengthTwo, lengthThree}).map((number) -> {
            return Math.pow(number, 4.0D);
        }).sum());
        if (lengthOne != 0.0D && lengthTwo != 0.0D && !IntStream.of(0, 1, 2).allMatch((number) -> {
            return this.coordinatesOne[number] == 0.0D;
        }) && !IntStream.of(0, 1, 2).allMatch((number) -> {
            return this.coordinatesTwo[number] == 0.0D;
        }) && !IntStream.of(0, 1, 2).allMatch((number) -> {
            return this.coordinatesThree[number] == 0.0D;
        })) {
            double[] cVectorOne = new double[3];
            double[] cVectorTwo = new double[3];
            double[] cVectorThree = new double[3];
            this.perform(brushSize, planeConstant, heronBig, cVectorOne, cVectorTwo, cVectorThree, 1, 2, 0);
            this.perform(brushSize, planeConstant, heronBig, cVectorOne, cVectorTwo, cVectorThree, 0, 2, 1);
            this.perform(brushSize, planeConstant, heronBig, cVectorOne, cVectorTwo, cVectorThree, 0, 1, 2);
        } else {
            messenger.sendMessage(ChatFormatting.RED + "ERROR: Invalid corners, please try again.");
        }

        this.coordinatesOne[0] = 0.0D;
        this.coordinatesOne[1] = 0.0D;
        this.coordinatesOne[2] = 0.0D;
        this.coordinatesTwo[0] = 0.0D;
        this.coordinatesTwo[1] = 0.0D;
        this.coordinatesTwo[2] = 0.0D;
        this.coordinatesThree[0] = 0.0D;
        this.coordinatesThree[1] = 0.0D;
        this.coordinatesThree[2] = 0.0D;
        this.cornerNumber = 1;
    }

    private void perform(int brushSize, double planeConstant, double heronBig, double[] cVectorOne, double[] cVectorTwo, double[] cVectorThree, int i, int i2, int i3) {
        for (int y = -brushSize; y <= brushSize; ++y) {
            for (int z = -brushSize; z <= brushSize; ++z) {
                this.currentCoordinates[i] = this.coordinatesOne[i] + (double) y;
                this.currentCoordinates[i2] = this.coordinatesOne[i2] + (double) z;
                this.currentCoordinates[i3] = (planeConstant - this.normalVector[i] * this.currentCoordinates[i] - this.normalVector[i2] * this.currentCoordinates[i2]) / this.normalVector[i3];
                double heronOne = this.calculateHeron(cVectorOne, cVectorTwo, cVectorThree, this.coordinatesOne, this.coordinatesTwo);
                double heronTwo = this.calculateHeron(cVectorOne, cVectorTwo, cVectorThree, this.coordinatesThree, this.coordinatesTwo);
                double heronThree = this.calculateHeron(cVectorOne, cVectorTwo, cVectorThree, this.coordinatesThree, this.coordinatesOne);
                double barycentric = (heronOne + heronTwo + heronThree) / heronBig;
                if (barycentric <= 1.1D) {
                    try {
                        this.performer.perform(this.getEditSession(), (int) this.currentCoordinates[0], this.clampY((int) this.currentCoordinates[1]), (int) this.currentCoordinates[2], this.clampY((int) this.currentCoordinates[0], (int) this.currentCoordinates[1], (int) this.currentCoordinates[2]));
                    } catch (MaxChangedBlocksException var23) {
                        var23.printStackTrace();
                    }
                }
            }
        }

    }

    private double calculateHeron(double[] cVectorOne, double[] cVectorTwo, double[] cVectorThree, double[] coordinatesOne, double[] coordinatesTwo) {
        for (int i = 0; i < 3; ++i) {
            cVectorOne[i] = coordinatesTwo[i] - coordinatesOne[i];
            cVectorTwo[i] = this.currentCoordinates[i] - coordinatesOne[i];
            cVectorThree[i] = this.currentCoordinates[i] - coordinatesTwo[i];
        }

        double cLengthOne = Math.sqrt(IntStream.of(0, 1, 2).mapToDouble((number) -> {
            return MathHelper.square(cVectorOne[number]);
        }).sum());
        double cLengthTwo = Math.sqrt(IntStream.of(0, 1, 2).mapToDouble((number) -> {
            return MathHelper.square(cVectorTwo[number]);
        }).sum());
        double cLengthThree = Math.sqrt(IntStream.of(0, 1, 2).mapToDouble((number) -> {
            return MathHelper.square(cVectorThree[number]);
        }).sum());
        return 0.25D * Math.sqrt(MathHelper.square(DoubleStream.of(new double[]{cLengthOne, cLengthTwo, cLengthThree}).map(MathHelper::square).sum()) - 2.0D * DoubleStream.of(new double[]{cLengthOne, cLengthTwo, cLengthThree}).map((number) -> {
            return Math.pow(number, 4.0D);
        }).sum());
    }

    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
    }
}
