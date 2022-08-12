package com.jayemceekay.terrasniper.brush.property;

import java.util.ArrayList;
import java.util.List;

public class BrushPropertiesBuilder {
    private final List<String> aliases = new ArrayList(1);
    private String name;
    private BrushCreator creator;
    private BrushPatternType brushPatternType;

    public BrushPropertiesBuilder() {
    }

    public BrushPropertiesBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BrushPropertiesBuilder alias(String alias) {
        this.aliases.add(alias);
        return this;
    }

    public BrushPropertiesBuilder creator(BrushCreator creator) {
        this.creator = creator;
        return this;
    }

    public BrushPropertiesBuilder brushPatternType(BrushPatternType brushPatternType) {
        this.brushPatternType = brushPatternType;
        return this;
    }

    public BrushProperties build() {
        if (this.name == null) {
            throw new RuntimeException("Brush name must be specified");
        } else if (this.creator == null) {
            throw new RuntimeException("Brush creator must be specified");
        } else if(this.brushPatternType == null) {
            throw new RuntimeException("Brush pattern type must be specified");
        }
            return new BrushProperties(this.name, this.aliases, this.creator, this.brushPatternType);
    }
}
