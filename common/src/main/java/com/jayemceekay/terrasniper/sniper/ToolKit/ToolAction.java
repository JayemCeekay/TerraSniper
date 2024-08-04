package com.jayemceekay.terrasniper.sniper.ToolKit;

import javax.annotation.Nullable;
import java.util.Arrays;


public enum ToolAction {
    ARROW,
    WAND;

    ToolAction() {
    }

    @Nullable
    public static ToolAction getToolAction(String name) {
        return Arrays.stream(values()).filter((toolAction) -> name.equalsIgnoreCase(toolAction.name())).findFirst().orElse(null);
    }
}

