package me.myxomopx.MonoChest

import me.myxomopx.MonoChest.Chests.MonoChest
import me.myxomopx.MonoChest.Rooms.ChestRoom
import me.myxomopx.MonoChest.Rooms.ChestRoomType
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin

/**
 * Created by OPX on 006 06.08.14.
 */
class Main extends JavaPlugin {
    private static Main instance;
    static Main getInstance(){
        return instance;
    }

    public Main(){
        instance = this
    }


    static List<ChestRoom> rooms = [];
    static List<MonoChest> chests = [];
    static Map<UUID,UUID> playerEnderRoomMap = [:]
    private static WorldCreator worldCreator;
    static Map<UUID,LinkedList<MapEntry>> playerTeleportBackMap = [:]
    static final String itemNameDefault = "§7§lDefault-MonoChest"
    static final String itemNameEnder = "§7§lEnder-MonoChest"
    static final prefix = "§2[MonoChest]§r"
    static private Map<String,Object> ymlConfig;
    static String worldName;
    static int maxEnters;
    static public Map<String,String> messages;
    private EventListener eventListener = null;

    public void onEnable(){
        initializeConfig()
        worldName = ymlConfig.startPosition.world

        initializeWorldCreator()
        def world = Bukkit.createWorld(worldCreator);
        if(ymlConfig.changeRules) {
            world.setGameRuleValue("doFireTick", "false")
            world.setGameRuleValue("mobGriefing", "false")
            world.setGameRuleValue("doMobSpawning", "false")
            world.setGameRuleValue("doDaylightCycle", "false")
            world.time = 5000;
        }

        maxEnters = ymlConfig.maxEnters as Integer

        rooms = FileManager.loadChestRooms();
        rooms.each {
            it.rescanWalls();
        }

        chests = FileManager.loadMonoChests();

        playerEnderRoomMap  = FileManager.loadEnderChests();

        playerTeleportBackMap = FileManager.loadPlayerTeleportBacks();

        messages = FileManager.getMessages()

        // Add command listener

        getCommand("monoExit").executor = { sender, cmd, label, args ->
            if(!(sender instanceof Player)) return false
            if(playerTeleportBackMap[sender.uniqueId] == null
            || playerTeleportBackMap[sender.uniqueId].size() == 0) {
                sendMessage(sender,"§6"+messages.cantTeleportBack)
                return true
            }
            Location l = playerTeleportBackMap[sender.uniqueId].pop().value
            sender.teleport l
            sender.playSound(sender.location, Sound.CHEST_CLOSE,1,1)
            sendMessage(sender,"§6"+messages.returnedBack)
            return true
        }

        // Add event listeners

        eventListener = new EventListener();
        getServer().getPluginManager().registerEvents(eventListener,this)

        // Add Crafting
        def item1 = new ItemStack(Material.ENDER_CHEST)
        def meta = item1.itemMeta
        meta.displayName = itemNameEnder
        item1.itemMeta = meta
        def recipe1 = new ShapedRecipe(item1)
        recipe1.shape("EEE","ECE","EEE")
        recipe1.setIngredient('C' as char,Material.ENDER_CHEST)
        recipe1.setIngredient('E' as char,Material.EYE_OF_ENDER)
        Bukkit.getServer().addRecipe(recipe1)

        def item2 = new ItemStack(Material.CHEST)
        def meta2 = item2.itemMeta
        meta2.displayName = itemNameDefault
        item2.itemMeta = meta2
        def recipe2 = new ShapedRecipe(item2)
        recipe2.shape("OOO","ONO","OOO")
        recipe2.setIngredient('N' as char,Material.CHEST)
        recipe2.setIngredient('O' as char,Material.EYE_OF_ENDER)
        Bukkit.getServer().addRecipe(recipe2)
    }

    static void initializeConfig() {
        ymlConfig = FileManager.getConfigs()
    }

    public void onDisable(){
        FileManager.saveChestRooms(rooms)
        FileManager.saveMonoChests(chests)
        FileManager.saveEnderChests(playerEnderRoomMap)
        FileManager.savePlayerTeleportBacks(playerTeleportBackMap)
    }

    private static void initializeWorldCreator(){
        worldCreator = new WorldCreator(worldName)
        worldCreator.generateStructures(false)
        worldCreator.type(WorldType.FLAT)
    }

    public static Location getLocationForNewChest(){
        if(rooms.size() == 0){
            def location = new Location(Bukkit.getWorld(worldName),
                    ymlConfig.startPosition.x as double,
                    ymlConfig.startPosition.y as double,
                    ymlConfig.startPosition.z as double)
            return location
        }

        def destroyedChestRoom = rooms.find{it.type == ChestRoomType.Destroyed}
        if(destroyedChestRoom != null){
            rooms.remove(destroyedChestRoom);
            return destroyedChestRoom.location
        }

        return  (rooms.clone() as List<ChestRoom>).sort {
            it.location.x
        }.last().location.clone().add(21, 0, 0)
    }

    public static MonoChest getMonoChest(Block b){
        return chests.find{it.block == b}
    }

    public static ChestRoom(Block b){
        return rooms.find{it.blocks.contains(b)}
    }

    static sendMessage(Player player, String str){
        player.sendMessage(prefix+" "+str)
    }

}
