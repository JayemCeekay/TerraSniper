package com.jayemceekay.TerraSniper.performer.property;

import com.jayemceekay.TerraSniper.performer.Performer;

@FunctionalInterface
public interface PerformerCreator {

    Performer create();

}