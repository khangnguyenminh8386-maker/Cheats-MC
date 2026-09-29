/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.phys.Vec3
 */
package night.managers;

import java.util.ArrayList;
import lombok.Generated;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.utils.IMinecraft;
import night.utils.minecraft.WorldUtils;

public class WaypointManager
implements IMinecraft {
    private final ArrayList<Waypoint> waypoints = new ArrayList();

    public boolean contains(String name) {
        return this.waypoints.stream().anyMatch(w -> w.getName().equalsIgnoreCase(name));
    }

    public void add(String name, Vec3 pos) {
        this.add(name, pos, WorldUtils.getDimension(), Night.SERVER_MANAGER.getServer());
    }

    public void add(String name, Vec3 pos, String dimension, String server) {
        if (this.contains(name)) {
            return;
        }
        this.waypoints.add(new Waypoint(name, pos, dimension, server));
    }

    public void remove(String name) {
        this.waypoints.removeIf(w -> w.getName().equalsIgnoreCase(name));
    }

    public void clear() {
        this.waypoints.clear();
    }

    @Generated
    public ArrayList<Waypoint> getWaypoints() {
        return this.waypoints;
    }

    public static class Waypoint {
        private final String name;
        private final Vec3 pos;
        private final String dimension;
        private final String server;

        public Waypoint(String name, Vec3 pos, String dimension, String server) {
            this.name = name;
            this.pos = pos;
            this.dimension = dimension;
            this.server = server;
        }

        @Generated
        public String getName() {
            return this.name;
        }

        @Generated
        public Vec3 getPos() {
            return this.pos;
        }

        @Generated
        public String getDimension() {
            return this.dimension;
        }

        @Generated
        public String getServer() {
            return this.server;
        }
    }
}

