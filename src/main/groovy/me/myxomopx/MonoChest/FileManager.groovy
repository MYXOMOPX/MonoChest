package me.myxomopx.MonoChest

import me.myxomopx.MonoChest.Rooms.ChestRoom
import me.myxomopx.MonoChest.Rooms.ChestRoomType
import me.myxomopx.MonoChest.Chests.MonoChestType
import me.myxomopx.MonoChest.Chests.MonoChest
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

import java.util.logging.Level

/**
 * Created by OPX on 006 06.08.14.
 */
class FileManager {
    static Yaml yaml = new Yaml(new DumperOptions(defaultFlowStyle: DumperOptions.FlowStyle.BLOCK))
    static final File chestRoomsFile = new File("plugins/MonoChest/svc/chestRooms.yml")
    static final File monoChestsFile = new File("plugins/MonoChest/svc/monoChests.yml")
    static final File enderChestsFile = new File("plugins/MonoChest/svc/playerEnderRoomMap.yml")
    static final File playerTeleportBackFile = new File("plugins/MonoChest/svc/playerTeleportStack.yml")
    static final File configFile = new File("plugins/MonoChest/Config.yml")

    static List<ChestRoom> loadChestRooms(){
        if(!enderChestsFile.exists()) return []
        List<ChestRoom> result = [];
        try {
            chestRoomsFile.withInputStream { InputStream stream ->
                List loadedList = yaml.load(stream) as List
                loadedList.each{ HashMap map ->
                    double x = map.x;
                    double y = map.y;
                    double z = map.z;
                    String world = map.world;
                    Location loc = new Location(Bukkit.getWorld(world),x,y,z);
                    UUID id = UUID.fromString(map.id)
                    ChestRoomType type = ChestRoomType.valueOf(map.type as String)
                    result.add(new ChestRoom(type,loc,id))
                }

            }
        } catch (ignored) {
            Bukkit.getLogger().log(Level.WARNING,"[MonoChest] Can't load chestRoomsFile",ignored)
            return null
        }
        return result;
    }

    static void saveChestRooms(List<ChestRoom> rooms){
        chestRoomsFile.getParentFile().mkdirs()
        chestRoomsFile.createNewFile()
        List<HashMap<String,Object>> list = [];
        rooms.each { ChestRoom room ->
            list.add([
                    x:room.location.x,
                    y:room.location.y,
                    z:room.location.z,
                    world:room.location.world.name,
                    id:room.id.toString(),
                    type:room.type.toString(),
            ])
        }
        chestRoomsFile.withOutputStream {
            String dump = yaml.dump(list);
            try{
                it.write(dump.getBytes("UTF8"))
            }catch (ignored){
                Bukkit.getLogger().log(Level.WARNING,"[MonoChest] Can't save chestRoomsFile",ignored)
            }
        }
    }

    static List<MonoChest> loadMonoChests(){
        if(!enderChestsFile.exists()) return []
        List<MonoChest> result = []
        try {
            monoChestsFile.withInputStream { InputStream stream ->
                List loadedList = yaml.load(stream) as List
                loadedList.each{ HashMap map ->
                    double x = map.x;
                    double y = map.y;
                    double z = map.z;
                    String world = map.world;
                    Block block = new Location(Bukkit.getWorld(world),x,y,z).block;
                    MonoChestType type = MonoChestType.valueOf(map.type)
                    UUID id = UUID.fromString(map.id)
                    result.add(new MonoChest(block,type,id))
                }

            }
        } catch (ignored) {
            Bukkit.getLogger().log(Level.WARNING,"[MonoChest] Can't load monoChestsFile",ignored)
            return null
        }
        return result;
    }

    static void saveMonoChests(List<MonoChest> chests){
        monoChestsFile.getParentFile().mkdirs()
        monoChestsFile.createNewFile()
        List<HashMap<String,Object>> list = [];
        chests.each { room ->
            list.add([
                    x:room.block.x,
                    y:room.block.y,
                    z:room.block.z,
                    world:room.block.world.name,
                    id:room.id.toString(),
                    type:room.type.toString(),
            ])
        }
        monoChestsFile.withOutputStream {
            String dump = yaml.dump(list);
            try{
                it.write(dump.getBytes("UTF8"))
            }catch (ignored){
                Bukkit.getLogger().log(Level.WARNING,"[MonoChest] Can't save monoChestsFile",ignored)
            }
        }
    }


    static Map<UUID,UUID> loadEnderChests(){
        if(!enderChestsFile.exists()) return [:]
        Map<UUID,UUID> result = [:]
        try {
            enderChestsFile.withInputStream { InputStream stream ->
                Map<String,String> loadedMap = yaml.load(stream) as Map<String,String>
                loadedMap.each{ k, v ->
                    UUID playerId = UUID.fromString(k)
                    UUID chestId = UUID.fromString(v)
                    result[playerId] = chestId
                }

            }
        } catch (ignored) {
            Bukkit.getLogger().log(Level.WARNING,"[MonoChest] Can't load enderChestsFile",ignored)
            return null
        }
        return result;
    }

    static void saveEnderChests(Map<UUID,UUID> enderChests){
        enderChestsFile.getParentFile().mkdirs()
        enderChestsFile.createNewFile()
        Map<String,String> saveMap = [:];
        enderChests.each { k,v ->
            saveMap[k.toString()] = v.toString()
        }
        enderChestsFile.withOutputStream {
            String dump = yaml.dump(saveMap);
            try{
                it.write(dump.getBytes("UTF8"))
            }catch (ignored){
                Bukkit.getLogger().log(Level.WARNING,"[MonoChest] Can't save enderChestsFile",ignored)
            }
        }
    }

    static void savePlayerTeleportBacks(Map<UUID,LinkedList<MapEntry>> teleports){
        playerTeleportBackFile.getParentFile().mkdirs()
        playerTeleportBackFile.createNewFile()
        Map saveMap = [:];
        List<List<Object>> locs = []
        teleports.each { k,v ->
            v.each {MapEntry entry ->
                locs.add([entry.key.toString(),[
                        x:entry.value.x,
                        y:entry.value.y,
                        z:entry.value.z,
                        world:entry.value.world.name
                ]])
            }
            saveMap[k.toString()] = locs;

        }
        playerTeleportBackFile.withOutputStream {
            String dump = yaml.dump(saveMap);
            try{
                it.write(dump.getBytes("UTF8"))
            }catch (ignored){
                Bukkit.getLogger().log(Level.WARNING,"[MonoChest] Can't save playerTeleportBack file",ignored)
            }
        }
    }

    static  Map<UUID,LinkedList<MapEntry>> loadPlayerTeleportBacks(){
        if(!playerTeleportBackFile.exists()) return [:]
        Map<UUID,LinkedList<MapEntry>> result = [:]
        try {
            playerTeleportBackFile.withInputStream { InputStream stream ->
                Map<String,List> loadedMap = yaml.load(stream) as Map<String,List>
                loadedMap.each{ k, v ->
                    UUID playerId = UUID.fromString(k)
                    result[playerId] = [] as LinkedList
                    // v is LIST<LIST>
                    v.each { List list ->
                        MapEntry entry = new MapEntry(
                                UUID.fromString(list[0] as String),
                                new Location(
                                        Bukkit.getWorld(list[1].world as String),list[1].x as double,list[1].y as double,list[1].z as double
                                )
                        );

                        result[playerId].add(entry)
                    }
                }

            }
        } catch (ignored) {
            Bukkit.getLogger().log(Level.WARNING,"[MonoChest] Can't load playerTeleportBack file",ignored)
            return null
        }
        return result;
    }

    public static Map<String,Object> getConfigs(){
        if(!configFile.exists()) return [:]
        configFile.withInputStream { InputStream stream ->
            return  yaml.load(stream) as Map<String,Object>
        }
    }

    public static void createConfigIfNotExists(){
        if(!configFile.exists()){
            InputStream is = FileManager.class.getClassLoader().getResourceAsStream("config.yml");
            configFile.getParentFile().mkdirs();
            OutputStream os;
            try {
                os = new FileOutputStream(configFile)
                byte[] buffer = new byte[0x100];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    os.write(buffer, 0, len);
                }
            } finally {
                os.close();
            }
        }
    }
}
