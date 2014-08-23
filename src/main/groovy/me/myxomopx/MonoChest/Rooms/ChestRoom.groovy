package me.myxomopx.MonoChest.Rooms

import me.myxomopx.MonoChest.Chests.MonoChest
import me.myxomopx.MonoChest.Main
import me.myxomopx.MonoChest.Utils
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import static me.myxomopx.MonoChest.TeleportUtil.teleport

/**
 * Created by OPX on 006 06.08.14.
 */
class ChestRoom {
    ChestRoomType type;
    Location location;
    final UUID id;
    List<Block> walls = [];

    public ChestRoom(ChestRoomType t, Location l, UUID i){
        type = t
        location = l
        id = i
        rescanWalls()
    }

    static ChestRoom createNewRoom(Location l){
        new ChestRoom(ChestRoomType.NotReady,l,UUID.randomUUID())
    }

    public Location getLocationForTeleport(){
        return  location.clone().add(7.5, 1, 2.5)
    }

    public boolean isDestroyed(){
        return type == ChestRoomType.Destroyed
    }

    public boolean isReady(){
        return type != ChestRoomType.NotReady
    }

    public void rescanWalls(){
        List<Block> b = Utils.cube(location,location.clone().add(14,14,14))
        b.removeAll Utils.cube(location.clone().add(1,1,1),location.clone().add(13,13,13))
        walls = b;
    }

    public List<Block> getBlocks(){
        return Utils.cube(location,location.clone().add(14,14,14))
    }

    public void destroy(){
        if(type == ChestRoomType.Ender) return
//        HashSet<Chunk> chunks = [] as HashSet
//        walls.each {
//            chunks.add it.chunk
//        }
//        chunks.each {
//            it.world.loadChunk(it)
//        }



        List<Block> blocks = Utils.cube(location,location.clone().add(14,14,14))
        MonoChest monoChest = Main.chests.find{it.id == id}

        // Destroy all chests inside room
        blocks.each { Block bl ->
            MonoChest chest = Main.getMonoChest(bl)
            if(chest != null){
                chest.destroy()
                Main.chests.remove(chest)
            }
        }

        // teleport all entities from chest
        blocks[0].world.getEntities().findAll {
            blocks.contains(it.location.block)
        }.findAll {
            !(it instanceof Player)
        }.each {
            teleport it, monoChest.block.location.add(0.5,0,0.5)
        }
        // drop all blocks from room
        Utils.cube(location.clone().add(1,1,1),location.clone().add(13,13,13)).each {
            ItemStack item = getItemStackFromBlock(it)
            if(item == null) return
            monoChest.block.world.dropItem(monoChest.block.location.add(0.5,0,0.5),item)
        }
        // drop all items from chests//hoppers and etc...
        blocks.findAll{it.state instanceof InventoryHolder }.each { Block b ->
            InventoryHolder holder = b.state as InventoryHolder
            holder.inventory.each {ItemStack item ->
                if(item == null) return
                monoChest.block.world.dropItem(monoChest.block.location.add(0.5,0,0.5),item)
            }
        }
        // DESTROY ALL!!! MUAHAHAHAHAH
        blocks.sort{0-it.y}.each {
            it.typeId = 0
        }
        type = ChestRoomType.Destroyed
    }

    public boolean build(ChestRoomType t){
        if(ready) return false
        if(t == ChestRoomType.Default || t == null){
            buildDefaultRoom()
        } else {
            buildEnderRoom()
        }
        type = t;
        return true
    }

    private void buildDefaultRoom(){
        getWalls().each {it.typeId = 5}
        location.clone().add(7.5,1,2.5).block.type = Material.TORCH
    }

    private void buildEnderRoom(){
        getWalls().each {it.typeId = 49}
        location.clone().add(7.5,1,2.5).block.type = Material.TORCH
    }

    private static ItemStack getItemStackFromBlock(Block block){
        switch (block.type){
            case Material.PORTAL:
            case Material.ENDER_PORTAL:
            case Material.WATER:
            case Material.LAVA:
            case Material.STATIONARY_WATER:
            case Material.STATIONARY_LAVA:
            case Material.BED:
            case Material.LONG_GRASS:
            case Material.DEAD_BUSH:
            case Material.PISTON_EXTENSION:
            case Material.PISTON_MOVING_PIECE:
            case Material.FIRE:
            case Material.MOB_SPAWNER:
            case Material.SNOW:
            case Material.HUGE_MUSHROOM_1:
            case Material.HUGE_MUSHROOM_2:
            case Material.PUMPKIN_STEM:
            case Material.MELON_STEM:
            case Material.VINE:
            case Material.NETHER_WARTS:
            case Material.COCOA:
            case Material.CARROT:
            case Material.POTATO:
            case Material.AIR:
                return null;
            case Material.GRASS:
                return new ItemStack(Material.DIRT,1)
            case Material.DOUBLE_STEP:
            case Material.WOOD_DOUBLE_STEP:
                return new ItemStack(block.type,2,block.data)
            case Material.REDSTONE_WIRE:
                return new ItemStack(Material.REDSTONE,1)
            case Material.BURNING_FURNACE:
                return new ItemStack(Material.FURNACE,1)
            case Material.SIGN_POST:
            case Material.WALL_SIGN:
                return new ItemStack(Material.SIGN,1)
            case Material.GLOWING_REDSTONE_ORE:
                return new ItemStack(Material.REDSTONE_ORE,1)
            case Material.REDSTONE_TORCH_OFF:
                return new ItemStack(Material.REDSTONE_TORCH_ON,1)
            case Material.SUGAR_CANE_BLOCK:
                return new ItemStack(Material.SUGAR_CANE,1)
            case Material.REDSTONE_COMPARATOR_OFF:
            case Material.REDSTONE_COMPARATOR_ON:
                return new ItemStack(Material.REDSTONE_COMPARATOR,1)
            case Material.DIODE_BLOCK_ON:
            case Material.DIODE_BLOCK_OFF:
                return new ItemStack(Material.DIODE,1)
            case Material.REDSTONE_LAMP_OFF:
                return new ItemStack(Material.REDSTONE_LAMP_OFF,1)
            case Material.TRIPWIRE:
                return new ItemStack(Material.STRING,1)
            default:
                return new ItemStack(block.type,1, block.data)
        }
    }
}
