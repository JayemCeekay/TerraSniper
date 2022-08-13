package com.jayemceekay.terrasniper.performer.property;

import com.jayemceekay.terrasniper.performer.Performer;

@FunctionalInterface
public interface PerformerCreator {

    Performer create();

}