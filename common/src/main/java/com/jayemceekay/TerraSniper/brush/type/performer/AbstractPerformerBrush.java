package com.jayemceekay.TerraSniper.brush.type.performer;

import com.jayemceekay.TerraSniper.PerformerRegistrar;
import com.jayemceekay.TerraSniper.brush.PerformerBrush;
import com.jayemceekay.TerraSniper.brush.type.AbstractBrush;
import com.jayemceekay.TerraSniper.performer.Performer;
import com.jayemceekay.TerraSniper.performer.PerformerRegistry;
import com.jayemceekay.TerraSniper.performer.property.PerformerCreator;
import com.jayemceekay.TerraSniper.performer.property.PerformerProperties;
import com.jayemceekay.TerraSniper.sniper.snipe.Snipe;
import com.jayemceekay.TerraSniper.sniper.snipe.performer.PerformerSnipe;

import java.util.Arrays;
import java.util.Random;

public abstract class AbstractPerformerBrush extends AbstractBrush implements PerformerBrush {
    protected static final int SEED_PERCENT_MIN = 1;
    protected static final int SEED_PERCENT_MAX = 9999;
    protected static final int GROWTH_PERCENT_MIN = 1;
    protected static final int GROWTH_PERCENT_MAX = 9999;
    protected static final int SPLATTER_RECURSIONS_MIN = 1;
    protected static final int SPLATTER_RECURSIONS_MAX = 10;
    protected static final int DEFAULT_SEED_PERCENT = 1000;
    protected static final int DEFAULT_GROWTH_PERCENT = 1000;
    protected static final int DEFAULT_SPLATTER_RECURSIONS = 3;
    protected final Random generator = new Random();
    protected Performer performer;
    protected int seedPercentMin;
    protected int seedPercentMax;
    protected int growthPercentMin;
    protected int growthPercentMax;
    protected int splatterRecursionsMin;
    protected int splatterRecursionsMax;
    protected int seedPercent;
    protected int growthPercent;
    protected int splatterRecursions;
    private PerformerProperties performerProperties;

    public AbstractPerformerBrush() {
        this.performerProperties = PerformerRegistrar.DEFAULT_PERFORMER_PROPERTIES;
        PerformerCreator performerCreator = this.performerProperties.getCreator();
        this.performer = performerCreator.create();
        this.performer.setProperties(this.performerProperties);
        this.performer.loadProperties();
    }

    @Override
    public void loadProperties() {
        this.seedPercentMin = 1;
        this.seedPercentMax = 9999;
        this.growthPercentMin = 1;
        this.growthPercentMax = 9999;
        this.splatterRecursionsMin = 1;
        this.splatterRecursionsMax = 10;
        this.seedPercent = 1000;
        this.growthPercent = 1000;
        this.splatterRecursions = 3;
    }

    @Override
    public void handlePerformerCommand(String[] parameters, Snipe snipe, PerformerRegistry performerRegistry) {
        String parameter = parameters[0];
        PerformerProperties performerProperties = performerRegistry.getPerformerProperties(parameter);
        if (performerProperties == null) {
            snipe.getBrush().handleCommand(parameters, snipe);
        } else {
            this.performerProperties = performerProperties;
            PerformerCreator performerCreator = this.performerProperties.getCreator();
            this.performer = performerCreator.create();
            this.performer.setProperties(this.performerProperties);
            this.performer.loadProperties();
            this.sendInfo(snipe);
            PerformerSnipe performerSnipe = new PerformerSnipe(snipe, this.performerProperties, this.performer);
            this.performer.sendInfo(performerSnipe);
            if (parameters.length > 1) {
                String[] additionalArguments = Arrays.copyOfRange(parameters, 1, parameters.length);
                snipe.getBrush().handleCommand(additionalArguments, snipe);
            }

        }
    }

    @Override
    public void initialize(Snipe snipe) {
        PerformerSnipe performerSnipe = new PerformerSnipe(snipe, this.performerProperties, this.performer);
        this.performer.initialize(performerSnipe);
    }

    @Override
    public void sendPerformerInfo(Snipe snipe) {
        PerformerSnipe performerSnipe = new PerformerSnipe(snipe, this.performerProperties, this.performer);
        this.performer.sendInfo(performerSnipe);
    }

    public Performer getPerformer() {
        return this.performer;
    }
}
