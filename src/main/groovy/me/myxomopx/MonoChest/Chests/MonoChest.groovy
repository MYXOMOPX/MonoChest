package me.myxomopx.MonoChest.Chests

import me.myxomopx.MonoChest.Rooms.ChestRoom
import me.myxomopx.MonoChest.Main
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block

/**
 * Created by OPX on 006 06.08.14.
 */
class MonoChest {
    Block block;
    ChestRoom chestRoom;
    MonoChestType type;
    final UUID id;

    public void destroy(){
        block.type = Material.AIR
        if(type != MonoChestType.Ender) {
            chestRoom.destroy();
        }
        (Main.playerTeleportBackMap.clone() as Map<UUID,List<MapEntry>>).each {k,v ->
            MapEntry entry = v.find {it.key == id}
            if(entry == null) return
            Location l = entry.value
            Bukkit.getOnlinePlayers().findAll{it.uniqueId == k}.each {
                it.teleport l
                int size = Main.playerTeleportBackMap[k].size()
                int index = Main.playerTeleportBackMap[k].findIndexOf {it == entry}
                for(int i = size; i > index; i--){
                    Main.playerTeleportBackMap[k].remove(i-1)
                }
            }
        }
    }

    public MonoChest(Block b, MonoChestType t, ChestRoom r){
        block = b
        chestRoom = r
        type = t
        id = r.id
    }

    public MonoChest(Block b, MonoChestType t, UUID i){
        block = b
        type = t
        id = i
        chestRoom = Main.rooms.find{it.id == i}
    }
}
