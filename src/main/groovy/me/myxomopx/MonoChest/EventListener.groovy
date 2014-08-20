package me.myxomopx.MonoChest

import me.myxomopx.MonoChest.Chests.MonoChest
import me.myxomopx.MonoChest.Chests.MonoChestType
import me.myxomopx.MonoChest.Rooms.ChestRoom
import me.myxomopx.MonoChest.Rooms.ChestRoomType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
/**
 * Created by OPX on 015 15.08.14.
 */
class EventListener implements Listener {

    @EventHandler
    public void onChestClick(PlayerInteractEvent event){
        if(event.action != Action.RIGHT_CLICK_BLOCK) return
        Block clickedBlock = event.clickedBlock
        if(!Main.chests.find{it.block == clickedBlock}) return;
        event.cancelled = true
        MonoChest chest = Main.chests.find{it.block == clickedBlock}
        if(Main.playerTeleportBackMap[event.player.uniqueId] == null){
            Main.playerTeleportBackMap[event.player.uniqueId] = [] as LinkedList
        }
        if(Main.playerTeleportBackMap[event.player.uniqueId].size() > Main.maxEnters){
            event.player.sendMessage(Main.prefix + " §6Can't go deeper ;)")
            return
        }
        Main.playerTeleportBackMap[event.player.uniqueId].add(new MapEntry(chest.id, event.player.location))
        event.player.teleport chest.chestRoom.location.clone().add(7.5, 1, 2.5)
        event.player.sendMessage(Main.prefix + " §aYou entered to chest!")
        event.player.playSound(event.player.location, Sound.CHEST_OPEN,1,1)
    }

    @EventHandler
    public void onDestroyChest(BlockBreakEvent event){
        Block clickedBlock = event.block
        if(!Main.chests.find{it.block == clickedBlock}) return;
        MonoChest chest = Main.chests.find{it.block == clickedBlock}

        chest.destroy();
        Main.chests.remove(chest)
    }

    @EventHandler
    public void onDestroyWalls(BlockBreakEvent event) {
        Block clickedBlock = event.block
        if (!Main.rooms.find { it.walls.contains(clickedBlock) }) return
        event.cancelled = true
        event.player.sendMessage(Main.prefix + " §4You can't break walls of MonoChest")
    }

    @EventHandler
    public void onPlaceNewChest(BlockPlaceEvent event) {
        ItemStack item = event.player.itemInHand
        if (item.type != Material.CHEST && item.type != Material.ENDER_CHEST) return

        Block block = event.block
        List<Block> emptyPlace = [block.getRelative(1, 0, 0), block.getRelative(-1, 0, 0), block.getRelative(0, 0, 1), block.getRelative(0, 0, -1)]
        if (emptyPlace.find { Block b ->
            Main.chests.find{it.block == b} != null
        }) {
            event.player.sendMessage(Main.prefix + " §4Can't place chest near MonoChest!")
            event.cancelled = true
            return
        }

        if (!item.hasItemMeta()) return
        String name = item.itemMeta.displayName

        if (name == Main.itemNameDefault || name == Main.itemNameEnder) {
            if(emptyPlace.find {it.type == Material.CHEST}){
                event.player.sendMessage(Main.prefix + " §4Can't place MonoChest near chest!")
                event.cancelled = true
                return
            }
        }
        ChestRoomType rType
        MonoChestType cType
        if (name == Main.itemNameDefault) {
            if (!event.player.hasPermission("monochest.build.default")) {
                event.player.sendMessage(Main.prefix + " §4You has not permissions for build Default-MonoChest")
                event.cancelled = true
                return
            }
            rType = ChestRoomType.Default
            cType = MonoChestType.Default
            ChestRoom room = ChestRoom.createNewRoom(Main.getLocationForNewChest())
            room.build(rType);
            MonoChest chest = new MonoChest(event.block, cType, room);
            Main.rooms.add(room)
            Main.chests.add(chest)

        } else if (name == Main.itemNameEnder) {
            if (!event.player.hasPermission("monochest.build.ender")) {
                event.player.sendMessage(Main.prefix + " §4You has not permissions for build Ender-MonoChest")
                event.cancelled = true
                return
            }
            rType = ChestRoomType.Ender
            cType = MonoChestType.Ender
            UUID enderRoomId = Main.playerEnderRoomMap[event.player.uniqueId]
            if (enderRoomId == null) {
                ChestRoom room = ChestRoom.createNewRoom(Main.getLocationForNewChest())
                room.build(rType);
                MonoChest chest = new MonoChest(event.block, cType, room);
                Main.rooms.add(room)
                Main.chests.add(chest)
                Main.playerEnderRoomMap[event.player.uniqueId] = room.getId()
                return
            }
            MonoChest chest = new MonoChest(event.block, cType, enderRoomId)
            Main.chests.add(chest)
        }
    }

    @EventHandler
    public void onExplodeWalls(EntityExplodeEvent event){
        Main.rooms.each { room ->
            room.walls.each { Block b ->
                event.blockList().remove(b)
            }
        };
    }

    @EventHandler
    public void onPistonExtendWalls(BlockPistonExtendEvent event){
        if(Main.rooms.find { room ->
            room.walls.find { block ->
                event.blocks.contains(block)
            } != null
        }) event.cancelled = true
    }

    @EventHandler
    public void onPistonRetractWalls(BlockPistonRetractEvent event){
        if(Main.rooms.find { room ->
            room.walls.find { block ->
                event.retractLocation.block == block
            } != null
        }) event.cancelled = true
    }

    @EventHandler
    public void onExplodeChest(EntityExplodeEvent event){
        Main.chests.findAll {
            event.blocks.contains(it.block)
        }.each {
            it.destroy()
        }
    }

    @EventHandler
    public void onBurnWalls(BlockBurnEvent event){
        if(Main.rooms.find {
            it.walls.contains(event.block)
        }) event.cancelled = true
    }




}
