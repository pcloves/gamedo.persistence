package org.gamedo.persistence.logging;


import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Arrays;

public final class Markers
{
    public static final Marker GamedoPersistence = of("gamedo.persistence");
    public static final Marker MongoDB = of("gamedo.mongoDB", GamedoPersistence);

    private Markers() {
    }

    public static Marker of(String name, Marker ... parents) {
        final Marker marker = MarkerFactory.getMarker(name);
        Arrays.stream(parents).forEach(parent -> marker.add(parent));
        return marker;
    }
}
