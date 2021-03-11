package com.einkaufsheld.help2buy.CustomerApp.home.model;

import android.os.Parcel;

public class Geometry {
    private Location location;
    private Viewport viewport;

    protected Geometry(Parcel in) {
        location = in.readParcelable(Location.class.getClassLoader());
        viewport = in.readParcelable(Viewport.class.getClassLoader());
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }
}
