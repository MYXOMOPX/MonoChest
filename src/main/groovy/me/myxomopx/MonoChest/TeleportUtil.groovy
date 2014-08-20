package me.myxomopx.MonoChest

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player


/**
 * Created by OPX on 018 18.08.14.
 */
class TeleportUtil {
    static boolean teleport(Entity entity, Location location){
        def entityHandle = entity.handle
        def passenger = entityHandle.passenger;
        def newWorld = location.world.handle
//        WorldUtil.loadChunks(location, 3);
        if (entityHandle.world != newWorld && !(entity instanceof Player)) {
            if (passenger != null) {
                entityHandle.passenger = null;
                passenger.vehicle = null;
                if (teleport(passenger.getBukkitEntity() as Entity, location)) {
                    Bukkit.scheduler.runTask(Main.instance){
                        passenger.setPassengerOf(entityHandle);
                    }
                }
            }

            // teleport this entity
            entityHandle.world.removeEntity(entityHandle);
            entityHandle.dead = false;
            entityHandle.world = newWorld;
            entityHandle.setLocation(location.x, location.y, location.z, location.yaw, location.pitch);
            entityHandle.world.addEntity(entityHandle);
            return true;
        } else {
            // If in a vehicle, make sure we eject first
            if (entityHandle.vehicle != null) {
                entityHandle.setPassengerOf(null);
            }
            // If vehicle, eject the passenger first
            if (passenger != null) {
                passenger.vehicle = null;
                entityHandle.passenger = null;
            }
            final boolean success = entity.teleport(location);
            // If there was a passenger, let passenger enter again
            if (passenger != null) {
                passenger.vehicle = entityHandle;
                entityHandle.passenger = passenger;
            }
            return success;
        }
    }
}
