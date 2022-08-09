package com.jayemceekay.TerraSniper;


import com.jayemceekay.TerraSniper.performer.PerformerRegistry;
import com.jayemceekay.TerraSniper.performer.property.PerformerProperties;
import com.jayemceekay.TerraSniper.performer.type.combo.*;
import com.jayemceekay.TerraSniper.performer.type.material.*;

public class PerformerRegistrar {
    public static final PerformerProperties DEFAULT_PERFORMER_PROPERTIES = PerformerProperties.builder().name("Material").alias("m").alias("material").creator(MaterialPerformer::new).build();
    private final PerformerRegistry registry;

    public PerformerRegistrar(PerformerRegistry registry) {
        this.registry = registry;
        this.registerPerformers();
    }

    public void registerPerformers() {
        this.registerMaterialPerformers();
        this.registerComboPerformers();
    }

    private void registerMaterialPerformers() {
        this.registerMaterialPerformer();
        this.registerMaterialMaterialPerformer();
        this.registerMaterialComboPerformer();
        registerMaterialComboNoPhysicsPerformer();
        registerMaterialNoPhysicsPerformer();
        registerMaterialMaterialNoPhysicsPerformer();
    }

    private void registerMaterialPerformer() {
        this.registry.register(DEFAULT_PERFORMER_PROPERTIES);
    }

    private void registerMaterialNoPhysicsPerformer() {
        PerformerProperties properties = PerformerProperties.builder().name("Material No Physics").alias("mp").alias("mat-nophys").creator(MaterialNoPhysicsPerformer::new).build();
        this.registry.register(properties);
    }

    private void registerMaterialMaterialPerformer() {
        PerformerProperties properties = PerformerProperties.builder().name("Material Material").usingReplaceMaterial().alias("mm").alias("mat-mat").creator(MaterialMaterialPerformer::new).build();
        this.registry.register(properties);
    }

    private void registerMaterialMaterialNoPhysicsPerformer() {
        PerformerProperties properties = PerformerProperties.builder().name("Material Material No Physics").usingReplaceMaterial().alias("mmp").alias("mat-mat-nophys").creator(MaterialMaterialNoPhysicsPerformer::new).build();
        this.registry.register(properties);
    }

    private void registerMaterialComboPerformer() {
        PerformerProperties properties = PerformerProperties.builder().name("Material Combo").usingReplaceMaterial().alias("mc").alias("mat-combo").creator(MaterialComboPerformer::new).build();
        this.registry.register(properties);
    }

    private void registerMaterialComboNoPhysicsPerformer() {
        PerformerProperties properties = PerformerProperties.builder().name("Material Combo No Physics").usingReplaceMaterial().alias("mcp").alias("mat-combo-nophys").creator(MaterialComboNoPhysicsPerformer::new).build();
        this.registry.register(properties);
    }

    /*
        private void registerExcludeMaterialPerformer() {
            PerformerProperties properties = PerformerProperties.builder().name("Exclude Material").alias("xm").alias("exclude-mat").creator(ExcludeMaterialPerformer::new).build();
            this.registry.register(properties);
        }

        private void registerIncludeMaterialPerformer() {
            PerformerProperties properties = PerformerProperties.builder().name("Include Material").alias("nm").alias("include-mat").creator(IncludeMaterialPerformer::new).build();
            this.registry.register(properties);
        }
    */
    private void registerComboPerformers() {
        this.registerComboPerformer();
        this.registerComboMaterialPerformer();
        this.registerComboComboPerformer();
        registerComboComboNoPhysicsPerformer();
        registerComboNoPhysicsPerformer();
        registerComboMaterialNoPhysicsPerformer();
    }

    private void registerComboPerformer() {
        PerformerProperties properties = PerformerProperties.builder().name("Combo").alias("c").alias("combo").creator(ComboPerformer::new).build();
        this.registry.register(properties);
    }

    private void registerComboNoPhysicsPerformer() {
        PerformerProperties properties = PerformerProperties.builder().name("Combo No Physics").alias("cp").alias("combo-nophys").creator(ComboNoPhysicsPerformer::new).build();
        this.registry.register(properties);
    }

    private void registerComboMaterialPerformer() {
        PerformerProperties properties = PerformerProperties.builder().name("Combo Material").usingReplaceMaterial().alias("cm").alias("combo-mat").creator(ComboMaterialPerformer::new).build();
        this.registry.register(properties);
    }

    private void registerComboMaterialNoPhysicsPerformer() {
        PerformerProperties properties = PerformerProperties.builder().name("Combo Material No Physics").usingReplaceMaterial().alias("cmp").alias("combo-mat-nophys").creator(ComboMaterialNoPhysicsPerformer::new).build();
        this.registry.register(properties);
    }

    private void registerComboComboPerformer() {
        PerformerProperties properties = PerformerProperties.builder().name("Combo Combo").usingReplaceMaterial().alias("cc").alias("combo-combo").creator(ComboComboPerformer::new).build();
        this.registry.register(properties);
    }

    private void registerComboComboNoPhysicsPerformer() {
        PerformerProperties properties = PerformerProperties.builder().name("Combo Combo No Physics").usingReplaceMaterial().alias("ccp").alias("combo-combo-nophys").creator(ComboComboNoPhysicsPerformer::new).build();
        this.registry.register(properties);
    }

    /*
    private void registerExcludeComboPerformer() {
        PerformerProperties properties = PerformerProperties.builder().name("Exclude Combo").alias("xc").alias("exclude-combo").creator(ExcludeComboPerformer::new).build();
        this.registry.register(properties);
    }

    private void registerIncludeComboPerformer() {
        PerformerProperties properties = PerformerProperties.builder().name("Include Combo").alias("nc").alias("include-combo").creator(IncludeComboPerformer::new).build();
        this.registry.register(properties);
    }*/
}
