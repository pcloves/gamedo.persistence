package org.gamedo.persistence.logging;


import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public final class Markers
{
    public static final Marker GamedoPersistence = of("gamedo.persistence");
    public static final Marker MongoDB = of("gamedo.mongoDB", GamedoPersistence);

    private Markers() {
    }

    public static org.apache.logging.log4j.Marker of(String name, org.apache.logging.log4j.Marker... parents) {
        return MarkerManager.getMarker(name).setParents(parents);
    }

}
