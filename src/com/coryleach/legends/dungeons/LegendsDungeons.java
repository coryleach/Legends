package com.coryleach.legends.dungeons;

import java.util.*;
import java.util.logging.*;
//import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
//import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldedit.bukkit.selections.*;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.UnhandledCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.minecraft.util.commands.CommandsManager;
//import com.sk89q.worldedit.*;
//import com.sk89q.worldedit.commands.InsufficientArgumentsException;
//import org.bukkit.command.*;

import com.coryleach.legends.*;

/**
 * Legends Lair plugin for Bukkit
 *
 * @author coryleach
 */
public class LegendsDungeons {

    //Private Members
    private HashMap<String,HashMap<String,Dungeon>> dungeons;
    private HashMap<LegendsPlayer,EditSession> editSessions;

    private Set<DungeonSubzone> activeDungeons;

    public static LegendsDungeons instance = null;

    //Public Members
    public Server server;
    public Logger log;
    public Legends plugin;

    public DungeonsEntityListener entityListener;
    public DungeonsWorldListener worldListener;
    public DungeonsPlayerListener playerListener;

    public int dungeonTaskId;
    

    public void LegendsDungeons() {

        if ( instance == null ) {
            instance = this;
        } else {
            Legends.instance.log.warning("Allocating two instances of LegendsDungeons");
        }

        instance = this;

    }

    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        // EXAMPLE: Custom code, here we just output some info so we can check all is well

        server = null;
        log = null;
        plugin = null;

        activeDungeons = null;

    	//Say Goodbye
        System.out.println("Legends.Dungeons Goodbye!");
        
    }

    public void onEnable(Legends plugin) {
        // TODO: Place any custom enable code here including the registration of any events
    	
        instance = this;

    	//Setup Plugin Member Variables
        this.plugin = plugin;
    	server = plugin.getServer();
    	log = server.getLogger();

        dungeonTaskId = -1;

        dungeons = new HashMap<String,HashMap<String,Dungeon>>();
        editSessions = new HashMap<LegendsPlayer,EditSession>();

        activeDungeons = new HashSet<DungeonSubzone>();

        entityListener = new DungeonsEntityListener(plugin);
        worldListener = new DungeonsWorldListener(plugin);
        playerListener = new DungeonsPlayerListener(plugin);

	PluginManager pm = plugin.getServer().getPluginManager();
        
    	try {
            
            //Setup PlayerListener class to preprocess command events
            pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.High, plugin);
            pm.registerEvent(Event.Type.WORLD_LOAD, worldListener, Priority.Normal, plugin);
            pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, plugin);

    	} catch ( Exception e ) {

            log.info("Exception Registering Commands: ");
            log.info(e.getMessage());

    	}
    	
        //Say Hello
        log.info("Legends.Dungeons Hello!");

    }

    public void addDungeon(String world, Dungeon dungeon) {

        if ( world == null ) {
            Legends.warning("World is null in addDungeon!");
            return;
        }

        HashMap<String,Dungeon> map = this.dungeons.get(world);

        if ( map == null ) {

            map = new HashMap<String,Dungeon>();
            this.dungeons.put(world, map);

        }

        String name = dungeon.getName();
        map.put(name, dungeon);

    }

    public Dungeon getDungeonWithName(String world, String name) {

        Dungeon dungeon = this.dungeons.get(world).get(name);
        return dungeon;

    }

    public EditSession getSessionForPlayer(LegendsPlayer player) {

        return this.editSessions.get(player);

    }

    public EditSession createSessionForPlayer(LegendsPlayer player, Dungeon dungeon) {

        EditSession session = new EditSession(player,dungeon);
        this.editSessions.put(player, session);

        return session;

    }

    public String[] getDungeonList(String world) {

        Set<String> set = this.dungeons.get(world).keySet();

        return set.toArray(new String[set.size()]);

    }

    public Dungeon getDungeonForLocation(Location loc) {

        String[] keys = getDungeonList(loc.getWorld().getName());
        HashMap<String,Dungeon> map = this.dungeons.get(loc.getWorld().getName());

        for ( int i = 0; i < keys.length; i++ ) {

            String name = keys[i];

            Dungeon dungeon = map.get(name);

            if ( dungeon == null ) {
                continue;
            }

            if ( dungeon.containsLocation(loc) ) {
                return dungeon;
            }
            
        }

        return null;
        
    }

    //This method can be removed after testing
    public static void main(String[] args) {
        //Do Nothing
    }

    public CuboidSelection getSelection(LegendsPlayer player) {
        
        PluginManager pm = plugin.getServer().getPluginManager();

        if ( !pm.isPluginEnabled("WorldEdit") ) {
            player.message("This plugin requres the WorldEdit plugin");
            return null;
        }

        WorldEditPlugin worldEdit = (WorldEditPlugin)pm.getPlugin("WorldEdit");

        Selection selection = worldEdit.getSelection(player.getHandle());

        //We only work with Cuboid selections
        if ( !(selection instanceof CuboidSelection) ) {
            return null;
        }

        return (CuboidSelection)selection;

    }

    public void updateActiveDungeons() {

        List<World> worlds = plugin.server.getWorlds();

        Iterator<World> i = worlds.iterator();

        activeDungeons.clear();

        while ( i.hasNext() ) {

            World world = i.next();

            Chunk[] chunks = world.getLoadedChunks();

            for ( int j = 0; j < chunks.length; j++ ) {

                Chunk chunk = chunks[j];

                //Check for active dungeons in this chunk
                Location loc1 = chunk.getBlock(0, 0, 0).getLocation();
                Location loc2 = chunk.getBlock(15, 127, 15).getLocation();
                
                //Get Chunk Cuboid
                CuboidSelection chunkCube = new CuboidSelection(world,loc1,loc2);

                try {

                    ArrayList<DungeonSubzone> list = null;
                    
                    try {
                        
                        list = this.getIntersectingDungeons(world.getName(), chunkCube);

                        if ( activeDungeons == null ) {
                            Legends.warning("Active Dungeons list is null");
                        } else {
                            activeDungeons.addAll( list );
                        }

                    } catch ( NullPointerException e) {
                        Legends.info("NPE doing getIntersectingDungeons / addAll()");
                    }

                } catch ( NullPointerException e ) {
                    Legends.info("Null Pointer Exception adding intersecting dungeons to active dungeon list");
                    break;
                }
            }

        }

        Legends.info("Active Dungeon Subzones: " + activeDungeons.size());

    }

    public ArrayList<DungeonSubzone> getIntersectingDungeons(String world, CuboidSelection selection) {

        ArrayList<DungeonSubzone> list = new ArrayList<DungeonSubzone>();

        HashMap<String,Dungeon> dungeonMap = this.dungeons.get(world);

        if ( dungeonMap == null ) {
            return list;
        }

        if ( selection == null ) {
            Legends.warning("Selection is null!");
            return list;
        }

        Iterator<String> i = dungeonMap.keySet().iterator();

        int size = dungeonMap.size();

        while ( i.hasNext() ) {

            String key = i.next();
            Dungeon dungeon = dungeonMap.get(key);

            if ( dungeon == null ) {
                Legends.info("No Dungeon for Key " + key);
                continue;
            }

            try {

                ArrayList<DungeonSubzone> subzoneList = dungeon.getIntersectingSubzones(selection);

                if ( list.addAll(subzoneList) ) {
                    Legends.info("Added Subzone to list");
                }

            } catch ( NullPointerException e ) {
                Legends.warning("Collection returned by getIntersectingSubzones is null!");
            }

        }

        return list;

    }
    
    public void startDungeons() {

        if ( dungeonTaskId != -1 ) {
            stopDungeons();
        }

        //Run Task After Event
        dungeonTaskId = plugin.server.getScheduler().scheduleSyncRepeatingTask(plugin,
            new Runnable() {

                public void run() {

                    Legends.info("Doing Dungeon Task");
                    try {
                        
                        updateActiveDungeons();

                        try {
                            spawnDungeonMobs();
                        } catch ( NullPointerException e ) {
                            Legends.info("Null Pointer Exception while spawnDungeonMobs()");
                        }

                    } catch ( NullPointerException e ) {
                        Legends.info("Null Pointer Exception while updating active dungeons");
                    }

                }

            }
         ,20L, 600L);

         if ( dungeonTaskId == -1 ) {
             Legends.warning("Failed to start Dungeon Spawn Task!");
         }

    }

    public void stopDungeons() {

        if ( dungeonTaskId != -1 ) {

            plugin.server.getScheduler().cancelTask(dungeonTaskId);
            dungeonTaskId = -1;
        
        }

    }

    public void spawnDungeonMobs() {

        Iterator<DungeonSubzone> i = activeDungeons.iterator();

        try {

            while ( i.hasNext() ) {

                DungeonSubzone subzone = i.next();
                subzone.spawnMobs();

            }

        } catch( NullPointerException e ) {
            Legends.info("NullPointerException Spawning Mobs");
            e.printStackTrace();
        }

    }

}
