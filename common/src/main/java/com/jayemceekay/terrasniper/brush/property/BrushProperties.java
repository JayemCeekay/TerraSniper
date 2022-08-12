package com.jayemceekay.terrasniper.brush.property;

import java.util.List;

public class BrushProperties {
    private final String name;
    private final List<String> aliases;
    private final BrushCreator creator;
    private final BrushPatternType brushPatternType;

    BrushProperties(String name, List<String> aliases, BrushCreator creator, BrushPatternType brushPatternType) {
        this.name = name;
        this.aliases = aliases;
        this.creator = creator;
        this.brushPatternType = brushPatternType;
    }

    public static BrushPropertiesBuilder builder() {
        return new BrushPropertiesBuilder();
    }

    public String getName() {
        return this.name;
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public BrushCreator getCreator() {
        return this.creator;
    }

    public BrushPatternType getBrushPatternType() {
        return brushPatternType;
    }
}
