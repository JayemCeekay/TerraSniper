package com.jayemceekay.TerraSniper.brush;

import com.jayemceekay.TerraSniper.brush.property.BrushProperties;
import com.jayemceekay.TerraSniper.sniper.ToolKit.ToolAction;
import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.BlockVector3;

import java.util.HashMap;
import java.util.List;

public interface Brush {
    void handleCommand(String[] parameters, Snipe snipe);

    List<String> handleCompletions(String[] var1, Snipe snipe);

    void perform(Snipe snipe, ToolAction action, EditSession editSession, BlockVector3 clickedBlock, BlockVector3 LastBlock);

    void handleArrowAction(Snipe snipe);

    void handleGunpowderAction(Snipe snipe);

    void sendInfo(Snipe snipe);

    BrushProperties getProperties();

    void setProperties(BrushProperties brushProperties);

    void loadProperties();

    HashMap<String, String> getSettings();
}
