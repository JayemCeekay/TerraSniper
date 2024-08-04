package com.jayemceekay.terrasniper;

import com.jayemceekay.terrasniper.brush.BrushRegistry;
import com.jayemceekay.terrasniper.brush.property.BrushPatternType;
import com.jayemceekay.terrasniper.brush.property.BrushProperties;
import com.jayemceekay.terrasniper.brush.type.*;
import com.jayemceekay.terrasniper.brush.type.blend.BlendBallBrush;
import com.jayemceekay.terrasniper.brush.type.blend.BlendDiscBrush;
import com.jayemceekay.terrasniper.brush.type.blend.BlendVoxelBrush;
import com.jayemceekay.terrasniper.brush.type.blend.BlendVoxelDiscBrush;
import com.jayemceekay.terrasniper.brush.type.performer.*;
import com.jayemceekay.terrasniper.brush.type.performer.disc.DiscBrush;
import com.jayemceekay.terrasniper.brush.type.performer.disc.DiscFaceBrush;
import com.jayemceekay.terrasniper.brush.type.performer.disc.VoxelDiscBrush;
import com.jayemceekay.terrasniper.brush.type.performer.disc.VoxelDiscFaceBrush;
import com.jayemceekay.terrasniper.brush.type.performer.splatter.*;
import com.jayemceekay.terrasniper.brush.type.stencil.StencilBrush;
import com.jayemceekay.terrasniper.brush.type.stencil.StencilListBrush;

public class BrushRegistrar {
    public static final BrushProperties DEFAULT_BRUSH_PROPERTIES = BrushProperties.builder().name("Snipe").alias("s").alias("snipe").brushPatternType(BrushPatternType.ANY).creator(SnipeBrush::new).build();
    private final BrushRegistry registry;

    public BrushRegistrar(BrushRegistry registry) {
        this.registry = registry;
        this.registerBrushes();
    }

    public void registerBrushes() {


        this.registerBallBrush();
        this.registerBiomeBrush();
        this.registerBlendBallBrush();
        this.registerBlendDiscBrush();
        this.registerBlendVoxelBrush();
        this.registerSnipeBrush();
        this.registerErodeBrush();
        this.registerCleanSnowBrush();
        this.registerVoxelDiscBrush();
        this.registerVoxelDiscFaceBrush();
        this.registerBlobBrush();
        this.registerBlendVoxelBrush();
        this.registerBlendVoxelDiscBrush();
        this.registerCheckerVoxelDiscBrush();
        this.registerCopyPastaBrush();
        this.registerCylinderBrush();
        this.registerDiscBrush();
        this.registerDiscFaceBrush();
        this.registerDrainBrush();
        this.registerEllipseBrush();
        this.registerEllipsoidBrush();
        this.registerEraserBrush();
        this.registerErodeBlendBrush();
        this.registerExtrudeBrush();
        this.registerFillDownBrush();
        this.registerJaggedLineBrush();
        this.registerLineBrush();
        this.registerOverlayBrush();
        this.registerPullBrush();
        this.registerRandomErodeBrush();
        this.registerRingBrush();
        this.registerSetBrush();
        this.registerSnowConeBrush();
        this.registerSplatterBallBrush();
        this.registerSplatterDiscBrush();
        this.registerSplatterOverlayBrush();
        this.registerSplatterVoxelBrush();
        this.registerSplatterVoxelDiscBrush();
        this.registerSplineBrush();
        this.registerTriangleBrush();
        this.registerUnderlayBrush();
        this.registerVoxelBrush();
        this.registerStencilBrush();
        this.registerStencilListBrush();
    }

    private void registerBallBrush() {
        BrushProperties properties = BrushProperties.builder().name("Ball").alias("b").alias("ball").brushPatternType(BrushPatternType.ANY).creator(BallBrush::new).build();
        this.registry.register(properties);
    }
    private void registerBiomeBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Biome")
                .alias("bio")
                .alias("biome")
                .brushPatternType(BrushPatternType.ANY)
                .creator(BiomeBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerBlendBallBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Blend Ball")
                .alias("bb")
                .alias("blendball")
                .alias("blend_ball")
                .brushPatternType(BrushPatternType.ANY)
                .creator(BlendBallBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerBlendDiscBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Blend Disc")
                .alias("bd")
                .alias("blenddisc")
                .alias("blend_disc")
                .brushPatternType(BrushPatternType.ANY)
                .creator(BlendDiscBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerBlendVoxelBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Blend Voxel")
                .alias("bv")
                .alias("blendvoxel")
                .alias("blend_voxel")
                .brushPatternType(BrushPatternType.ANY)
                .creator(BlendVoxelBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerBlendVoxelDiscBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Blend Voxel Disc")
                .alias("bvd")
                .alias("blendvoxeldisc")
                .alias("blend_voxel_disc")
                .brushPatternType(BrushPatternType.ANY)
                .creator(BlendVoxelDiscBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerBlobBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Blob")
                .alias("blob")
                .alias("splatblob")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(BlobBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerCheckerVoxelDiscBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Checker Voxel Disc")
                .alias("cvd")
                .alias("checkervoxeldisc")
                .alias("checker_voxel_disc")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(CheckerVoxelDiscBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerCleanSnowBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Clean Snow")
                .alias("cls")
                .alias("cleansnow")
                .alias("clean_snow")
                .brushPatternType(BrushPatternType.ANY)
                .creator(CleanSnowBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerCopyPastaBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Copy Pasta")
                .alias("cp")
                .alias("copypasta")
                .alias("copy_pasta")
                .brushPatternType(BrushPatternType.ANY)
                .creator(CopyPastaBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerCylinderBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Cylinder")
                .alias("c")
                .alias("cylinder")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(CylinderBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerDiscBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Disc")
                .alias("d")
                .alias("disc")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(DiscBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerDiscFaceBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Disc Face")
                .alias("df")
                .alias("discface")
                .alias("disc_face")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(DiscFaceBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerDrainBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Drain")
                .alias("drain")
                .brushPatternType(BrushPatternType.ANY)
                .creator(DrainBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerEllipseBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Ellipse")
                .alias("el")
                .alias("ellipse")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(EllipseBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerEllipsoidBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Ellipsoid")
                .alias("elo")
                .alias("ellipsoid")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(EllipsoidBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerEraserBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Eraser")
                .alias("erase")
                .alias("eraser")
                .brushPatternType(BrushPatternType.ANY)
                .creator(EraserBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerErodeBlendBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Erode BlendBall")
                .alias("eb")
                .alias("erodeblend")
                .alias("erodeblendball")
                .brushPatternType(BrushPatternType.ANY)
                .creator(ErodeBlendBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerErodeBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Erode")
                .alias("e")
                .alias("erode")
                .brushPatternType(BrushPatternType.ANY)
                .creator(ErodeBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerExtrudeBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Extrude")
                .alias("ex")
                .alias("extrude")
                .brushPatternType(BrushPatternType.ANY)
                .creator(ExtrudeBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerFillDownBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Fill Down")
                .alias("fd")
                .alias("filldown")
                .alias("fill_down")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(FillDownBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerJaggedLineBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Jagged Line")
                .alias("j")
                .alias("jagged")
                .alias("jagged_line")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(JaggedLineBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerLineBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Line")
                .alias("l")
                .alias("line")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(LineBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerOverlayBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Overlay")
                .alias("over")
                .alias("overlay")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(OverlayBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerPullBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Pull")
                .alias("pull")
                .brushPatternType(BrushPatternType.ANY)
                .creator(PullBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerRandomErodeBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Random Erode")
                .alias("re")
                .alias("randomerode")
                .alias("randome_rode")
                .brushPatternType(BrushPatternType.ANY)
                .creator(RandomErodeBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerRingBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Ring")
                .alias("ri")
                .alias("ring")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(RingBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerSetBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Set")
                .alias("set")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(SetBrush::new)
                .build();
        this.registry.register(properties);
    }


    private void registerSnipeBrush() {
        this.registry.register(DEFAULT_BRUSH_PROPERTIES);
    }

    private void registerSnowConeBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Snow Cone")
                .alias("snow")
                .alias("snowcone")
                .alias("snow_cone")
                .brushPatternType(BrushPatternType.ANY)
                .creator(SnowConeBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerSplatterBallBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Splatter Ball")
                .alias("sb")
                .alias("splatball")
                .alias("splatter_ball")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(SplatterBallBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerSplatterDiscBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Splatter Disc")
                .alias("sd")
                .alias("splatdisc")
                .alias("splatter_disc")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(SplatterDiscBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerSplatterOverlayBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Splatter Overlay")
                .alias("sover")
                .alias("splatteroverlay")
                .alias("splatter_overlay")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(SplatterOverlayBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerSplatterVoxelBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Splatter Voxel")
                .alias("sv")
                .alias("splattervoxel")
                .alias("splatter_voxel")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(SplatterVoxelBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerSplatterVoxelDiscBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Splatter Voxel Disc")
                .alias("svd")
                .alias("splatvoxeldisc")
                .alias("splatter_voxel_disc")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(SplatterVoxelDiscBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerSplineBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Spline")
                .alias("sp")
                .alias("spline")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(SplineBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerStencilBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Stencil")
                .alias("st")
                .alias("stencil")
                .brushPatternType(BrushPatternType.ANY)
                .creator(StencilBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerStencilListBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Stencil List")
                .alias("sl")
                .alias("stencillist")
                .alias("stencil_list")
                .brushPatternType(BrushPatternType.ANY)
                .creator(StencilListBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerTriangleBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Triangle")
                .alias("tri")
                .alias("triangle")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(TriangleBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerUnderlayBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Underlay")
                .alias("under")
                .alias("underlay")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(UnderlayBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerVoxelBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Voxel")
                .alias("v")
                .alias("voxel")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(VoxelBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerVoxelDiscBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Voxel Disc")
                .alias("vd")
                .alias("voxeldisc")
                .alias("voxel_disc")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(VoxelDiscBrush::new)
                .build();
        this.registry.register(properties);
    }

    private void registerVoxelDiscFaceBrush() {
        BrushProperties properties = BrushProperties.builder()
                .name("Voxel Disc Face")
                .alias("vdf")
                .alias("voxeldiscface")
                .alias("voxel_disc_face")
                .brushPatternType(BrushPatternType.PATTERN)
                .creator(VoxelDiscFaceBrush::new)
                .build();
        this.registry.register(properties);
    }
}
