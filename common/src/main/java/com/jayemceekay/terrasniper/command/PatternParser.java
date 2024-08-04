package com.jayemceekay.terrasniper.command;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.factory.parser.pattern.*;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.registry.AbstractFactory;

/**
 * VoxelSniper pattern parser used for argument completion and parsing.
 *
 * @since 2.6.0
 */
public class PatternParser extends AbstractFactory<Pattern> {

    /**
     * Create a new pattern factory.
     *
     * @param worldEdit the WorldEdit instance
     */
    public PatternParser(final WorldEdit worldEdit) {
        super(worldEdit, new SingleBlockPatternParser(worldEdit));

        register(new RandomPatternParser(worldEdit));

        register(new ClipboardPatternParser(worldEdit));
        register(new TypeOrStateApplyingPatternParser(worldEdit));
        register(new RandomStatePatternParser(worldEdit));
        register(new BlockCategoryPatternParser(worldEdit));
    }

}
