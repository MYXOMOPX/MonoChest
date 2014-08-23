package me.myxomopx.MonoChest

import me.myxomopx.MonoChest.Chests.MonoChest
import me.myxomopx.MonoChest.Chests.MonoChestType
import me.myxomopx.MonoChest.Rooms.ChestRoom
import me.myxomopx.MonoChest.Rooms.ChestRoomType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
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
            Main.sendMessage(event.player,"§6"+Main.messages.cantGoDeeper)
            return
        }
        Main.playerTeleportBackMap[event.player.uniqueId].add(new MapEntry(chest.id, event.player.location))
        event.player.teleport chest.chestRoom.getLocationForTeleport()
        Main.sendMessage(event.player,"§a"+Main.messages.onEnterChest)
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
        Main.sendMessage(event.player,"§4"+Main.messages.onBreakWalls)
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
            Main.sendMessage(event.player,"§4"+Main.messages.cantPlaceChest)
            event.cancelled = true
            return
        }

        if (!item.hasItemMeta()) return
        String name = item.itemMeta.displayName

        if (name == Main.itemNameDefault || name == Main.itemNameEnder) {
            if(emptyPlace.find {it.type == Material.CHEST}){
                Main.sendMessage(event.player,"§4"+Main.messages.cantPlaceMonoChest)
                event.cancelled = true
                return
            }
        }
        ChestRoomType rType
        MonoChestType cType
        if (name == Main.itemNameDefault) {
            if (!event.player.hasPermission("monochest.build.default")) {
                Main.sendMessage(event.player,"§4"+Main.messages.noPermissionsForDefault)
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
                Main.sendMessage(event.player,"§4"+Main.messages.noPermissionsForEnder)
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

    @EventHandler
    public void onHopToChest(InventoryMoveItemEvent event){
        Inventory destInv =  event.destination
        if(!(destInv.holder instanceof Chest)) return
        Block b = (destInv.holder as Chest).block
        MonoChest chest = Main.getMonoChest(b)
        if(chest == null) return
        event.cancelled = true
    }



}
