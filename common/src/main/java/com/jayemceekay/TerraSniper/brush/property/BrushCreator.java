package com.jayemceekay.TerraSniper.brush.property;

import com.jayemceekay.TerraSniper.brush.Brush;

@FunctionalInterface
public interface BrushCreator {
    Brush create();
}
