package com.jayemceekay.terrasniper.brush.property;

import com.jayemceekay.terrasniper.brush.Brush;

@FunctionalInterface
public interface BrushCreator {
    Brush create();
}
