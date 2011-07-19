/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends;

import com.coryleach.legends.classes.LegendsClassCommands;
import com.coryleach.legends.factions.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import org.bukkit.plugin.*;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldedit.bukkit.selections.*;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.UnhandledCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.google.gson.*;
import com.google.gson.reflect.*;

import com.coryleach.util.DiscUtil;
import com.coryleach.legends.factions.*;
import com.coryleach.legends.dungeons.*;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.data.*;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;

/**
 * Legends Lair plugin for Bukkit
 *
 * @author coryleach
 */
public class Legends extends JavaPlugin {

    //Class public
    public static Legends instance;
    public final static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT,Modifier.VOLATILE)
            .registerTypeAdapter(Location.class, new MyLocationTypeAdapter())
            .create();

    //Private Members
    private final LegendsEntityListener entityListener = new LegendsEntityListener(this);
    private final LegendsPlayerListener playerListener = new LegendsPlayerListener(this);
    private final LegendsWorldListener worldListener = new LegendsWorldListener();
    private final LegendsBlockListener blockListener = new LegendsBlockListener();
    private CommandsManager<Player> commandMap;

    private FactionsManager factionManager;

    private HashMap<String,LegendsPlayer> playerMap;
    private HashMap<LivingEntity,LegendsCreature> creatureMap;

    //Public Members
    public Server server;
    public Logger log;
    public LegendsSettings settings;
    public WorldsHolder worldsHolder;

    public LegendsDungeons dungeons;

    public HashMap<String,LegendsMap> worldMaps;

    public Legends() {
        Legends.instance = this;
    }

    public void onDisable() {
        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        //Unload Dungeons
        //dungeons.onDisable();
        //dungeons = null;

        log.info("Doing New Save");
        this.saveWorldMaps();

        if ( !SettlementManager.getInstance().save() ) {
            log.severe("Failed to save settlement data!");
        }

        if ( !this.factionManager.save() ) {
            log.severe("Failed to save faction data!");
        }

        if ( !this.saveAllPlayerData() ) {
            log.severe("Failed to save all player data!");
        }

        if ( !this.saveSettings() ) {
            log.severe("Failed to save settings!");
        }

        //Do some cleanup
        SettlementManager.cleanup();
        FactionsManager.instance = null;

    	//Say Goodbye
        System.out.println("Legends Goodbye!");

    }

    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events

    	//Setup Plugin Member Variables
    	server = getServer();
    	log = server.getLogger();
        
        worldMaps = new HashMap<String,LegendsMap>();

        settings = new LegendsSettings();
        playerMap = new HashMap<String,LegendsPlayer>();
        creatureMap = new HashMap<LivingEntity,LegendsCreature>();
        factionManager = new FactionsManager();

        //Create directory in case it doesn't exist yet
        makeDirectory();
        log.info("Loading map data.");
        
        //Load map data
        this.loadWorldMaps();
        
        log.info("Loading settlement data.");
        //Load settlements
        if ( !SettlementManager.getInstance().load() ) {
            log.severe("Failed to load settlements!");
        }
        log.info("Loading faction data.");
        //Load Factions
        if ( !this.factionManager.load() ) {
            log.severe("Failed to load faction data!");
        }
        log.info("Loading settings data.");
        if ( !this.loadSettings() ) {
            log.severe("Failed to load settings!");
        }

        PluginManager pm = getServer().getPluginManager();

        Plugin test = pm.getPlugin("GroupManager");

        if ( test != null ) {
            log.info("Legends found Group Manager plugin!");
            worldsHolder = ((GroupManager)test).getWorldsHolder();
        }

        commandMap = new CommandsManager<Player>() {
            @Override
            public boolean hasPermission(Player player, String perm) {

                if ( worldsHolder != null ) {
                    OverloadedWorldHolder world = worldsHolder.getWorldData(player);
                    User user = world.getUser(player.getName());
                    return world.getPermissionsHandler().checkUserPermission(user, perm);
                } else  {
                    return player.isOp();
                }
                
            }
        };

    	try {
            
            // Register our events
            pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
            pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
            //pm.registerEvent(Event.Type.ENTITY_COMBUST, entityListener, Priority.Normal, this);
            pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
            //pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, Priority.Normal, this);
            pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.Normal, this);

            //pm.registerEvent(Event.Type.CHUNK_LOAD, worldListener, Priority.Normal, this);
            pm.registerEvent(Event.Type.CHUNK_UNLOAD, worldListener, Priority.Normal, this);

            //Setup PlayerListener class to preprocess command events
            pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
            pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
            pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
            pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
            pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
            pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
            pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Normal, this);

            pm.registerEvent(Event.Type.BLOCK_BREAK,blockListener,Priority.Normal,this);
            pm.registerEvent(Event.Type.BLOCK_PLACE,blockListener,Priority.Normal,this);
            
    	} catch ( Exception e ) {

            log.info("Exception while registering events.");
            log.info(e.getMessage());

        }

    	try {

            //Register Commands to the command map
            commandMap.register(LegendsCommands.class);
            commandMap.register(LegendsClassCommands.class);
            commandMap.register(LegendsFactionCommands.class);
            commandMap.register(LairDungeonCommand.class);
            //commandMap.register(WarriorCommands.class);

    	} catch ( Exception e ) {

            log.info("Exception Registering Commands: ");
            log.info(e.getMessage());

    	}

        FactionsManager.instance.updateFactions();
        //wrapAllEntities();

        //Say Hello
        log.info("Legends Hello!");

        //Load Dungeons
        dungeons = new LegendsDungeons();
        dungeons.onEnable(this);

    }

    public void loadWorldMaps() {

        List<World> list = server.getWorlds();

        Iterator<World> i = list.iterator();

        worldMaps.clear();

        while ( i.hasNext() ) {

            World world = i.next();
            String name = world.getName();

            LegendsMap aMap = new LegendsMap(name);
            
            if ( !aMap.load() ) {
                Legends.info("Building a new map for [" + name + "]");
            }

            aMap.setWorld(name);

            Legends.info("[worldMaps]" + name + "->" + aMap.getWorld());

            worldMaps.put(name, aMap);

        }

    }

    public void saveWorldMaps() {

        Iterator<String> i = worldMaps.keySet().iterator();

        while ( i.hasNext() ) {

            String key = i.next();
            LegendsMap aMap = worldMaps.get(key);

            Legends.info("[worldMaps]" + key + "->" + aMap.getWorld());

            Legends.info("Saving [" + key + "]");
            if ( !aMap.save() ) {
                Legends.info("Failed to save map for [" + key + "]");
            }

        }

    }

    public LegendsMap getMapForWorld(World world) {
        
        return getMapForWorld(world.getName());

    }

    public LegendsMap getMapForWorld(String worldName) {

        LegendsMap map = worldMaps.get(worldName);

        if ( map == null ) {

            map = new LegendsMap(worldName);

            //Attempt to load from disk
            if ( !map.load() ) {
                Legends.info("Building a new map for [" + worldName + "]");
            }

            worldMaps.put(worldName, map);

        }

        return map;

    }

    public boolean saveSettings() {

        String filename = "legendsSettings.json";

        File file = new File(this.getDataFolder(),filename);

        try {
            file.createNewFile();
            DiscUtil.write(file, gson.toJson(this.settings));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public boolean loadSettings() {

        String filename = "legendsSettings.json";
        File file = new File(this.getDataFolder(),filename);

        try {

            Type type = new TypeToken<LegendsSettings>(){}.getType();
            this.settings = gson.fromJson(DiscUtil.read(file), type);

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }

        return true;

    }

    public boolean savePlayerData(LegendsPlayer player) {

        String filename = player.getHandle().getName() + ".json";

        File file = new File(this.getDataFolder(),filename);

        try {
            file.createNewFile();
            DiscUtil.write(file, gson.toJson(player));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public boolean saveAllPlayerData() {

        Iterator<String> i = this.playerMap.keySet().iterator();

        while ( i.hasNext() ) {

            String player = i.next();
            if ( !savePlayerData(this.playerMap.get(player)) ) {
                return false;
            }

        }

        return true;

    }

    public void makeDirectory() {

        File file = getDataFolder();

        if ( !file.exists() ) {
            file.mkdir();
        }
        
    }

    public boolean loadPlayerData(Player player) {

        String filename = player.getName() + ".json";
        File file = new File(this.getDataFolder(),filename);

        try {

            Type type = new TypeToken<LegendsPlayer>(){}.getType();
            LegendsPlayer legendsPlayer = gson.fromJson(DiscUtil.read(file), type);
            legendsPlayer.setHandle(player);
            legendsPlayer.playerWasLoadedFromDisk();
            playerMap.put(player.getName(), legendsPlayer);

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }

        return true;

    }

    public boolean hasPermission(Player player, String perm) {

        if ( worldsHolder != null ) {
            OverloadedWorldHolder world = worldsHolder.getWorldData(player);
            User user = world.getUser(player.getName());
            return world.getPermissionsHandler().checkUserPermission(user, perm);
        } else  {
            return player.isOp();
        }

    }

    public void setPlayerPrefix(Player player, String prefix) {

        if ( worldsHolder == null ) {
            log.info("No Group Permissions!?!?!");
            return;
        }

        OverloadedWorldHolder world = worldsHolder.getWorldData(player);
        User user = world.getUser(player.getName());
        UserVariables variables = user.getVariables();

        if ( prefix == null || prefix.equals("") ) {
            variables.removeVar("prefix");
        } else {
            variables.addVar("prefix",prefix);
        }

    }

    public boolean playerDataExists(Player player) {
        
        String filename = player.getName() + ".json";
        File file = new File(this.getDataFolder(),filename);

        if ( file.exists() ) {
            return true;
        }
        
        return false;
        
    }
    
    public LegendsPlayer getPlayer(String name) {

        Player player = getServer().getPlayer(name);

        if ( player == null ) {
            return null;
        }

        return wrapPlayer(player);

    }

    public void wrapAllEntities() {
        //Check for pre-existing monsters and pre-wrap them
        World world = this.getServer().getWorlds().get(0);

        List<Entity> entities = world.getEntities();

        Iterator<Entity> itter = entities.iterator();

        while( itter.hasNext() ) {
            
            Entity entity = itter.next();

            if ( entity instanceof LivingEntity ) {

                if ( !(entity instanceof Player) ) {

                    LegendsCreature creature = wrapCreature((LivingEntity)entity);

                    if ( creature.getPrefix() == null ) {

                        CreaturePrefix  prefix = new CreaturePrefix("Mystery");
                        creature.setPrefix(prefix);

                    }

                }

            }
            
        }

    }

    public LegendsPlayer wrapPlayer(Player player) {

        if ( player == null ) {
            log.info("Can't wrap null player");
            return null;
        }

        //First try to get from loaded player map data
        LegendsPlayer wrappedPlayer = playerMap.get(player.getName());

        if ( wrappedPlayer != null ) {
            wrappedPlayer.setHandle(wrappedPlayer.getHandle());
            return wrappedPlayer;
        }

        //Second try, attempt to load data from file
        if ( playerDataExists(player) ) {

            if ( !loadPlayerData(player) ) {
                log.severe("Failed to load player data!");
            }
        
            wrappedPlayer = playerMap.get(player.getName());

            if ( wrappedPlayer != null ) {
                wrappedPlayer.setHandle(wrappedPlayer.getHandle());
                return wrappedPlayer;
            }

        }

        //Finally just make a new wrap around player
        wrappedPlayer = new LegendsPlayer(player);
        playerMap.put(player.getName(), wrappedPlayer);
        wrappedPlayer.setHandle(wrappedPlayer.getHandle());

        return wrappedPlayer;

    }

    public void removePlayer(LegendsPlayer player) {

        if ( player == null ) {
            log.info("Cannot remove null player");
            return;
        }

        //TODO: Save Player Info Here
        if ( !savePlayerData(player) ) {
            log.severe("Failed to save player legends data!");
        }

        playerMap.remove(player.getHandle().getName());

    }

    public void removePlayer(Player player) {

        if ( player == null ) {
            log.info("Cannot remove null player");
            return;
        }

        LegendsPlayer legendsPlayer = playerMap.remove(player.getName());

        //TODO: Save Player Info Here
        if ( !savePlayerData(legendsPlayer) ) {
            log.severe("Failed to save player legends data!");
        }
        
    }

    public LegendsCreature wrapCreature(LivingEntity creature) {

        if ( creature == null ) {
            log.info("Can't wrap null creature");
            return null;
        }

        LegendsCreature wrappedCreature = creatureMap.get(creature);

        if ( wrappedCreature == null ) {
            wrappedCreature = new LegendsCreature(creature);
            creatureMap.put(creature, wrappedCreature);
        }

        return wrappedCreature;

    }

    public void removeCreature(LegendsCreature creature) {

        if ( creature == null ) {
            log.info("Cannot remove null creature");
            return;
        }

        //TODO: Save Player Info Here?

        creatureMap.remove(creature.getHandle());

    }

    public void removeCreature(LivingEntity creature) {

        if ( creature == null ) {
            log.info("Cannot remove null creature");
            return;
        }

        creatureMap.remove(creature);

    }

    public void fixPlayers() {

        Player[] players = this.server.getOnlinePlayers();

        for ( int i = 0; i < players.length; i++ ) {

            LegendsPlayer legendsPlayer = this.wrapPlayer(players[i]);

            legendsPlayer.fix();

        }

    }

    //This method can be removed after testing
    public static void main(String[] args) {
        //Do Nothing
    }

    public CuboidSelection getSelection(Player player) {

        PluginManager pm = getServer().getPluginManager();

        if ( !pm.isPluginEnabled("WorldEdit") ) {
            player.sendMessage("This plugin requres the WorldEdit plugin");
            return null;
        }

        WorldEditPlugin worldEdit = (WorldEditPlugin)pm.getPlugin("WorldEdit");

        Selection selection = worldEdit.getSelection(player);

        //We only work with Cuboid selections
        if ( !(selection instanceof CuboidSelection) ) {
            return null;
        }

        return (CuboidSelection)selection;

    }

    public boolean handleCommand(Player player, String[] split, String command) {

        try {

            split[0] = split[0].substring(1);

            // Quick script shortcut
            if (split[0].matches("^[^/].*\\.js$")) {
                String[] newSplit = new String[split.length + 1];
                System.arraycopy(split, 0, newSplit, 1, split.length);
                newSplit[0] = "cs";
                newSplit[1] = newSplit[1];
                split = newSplit;
            }

            // No command found!
            if (!commandMap.hasCommand(split[0])) {
                return false;
            }

            try {
                commandMap.execute(split, player, this, wrapPlayer(player));
                String logString = "[Legends Command] " + command;
                log.info(logString);
            } catch (CommandPermissionsException e) {
                player.sendMessage("You don't have permission to do this.");
            } catch (MissingNestedCommandException e) {
                player.sendMessage(e.getUsage());
            } catch (CommandUsageException e) {
                player.sendMessage(e.getMessage());
                player.sendMessage(e.getUsage());
            } catch (WrappedCommandException e) {
                player.sendMessage(ChatColor.RED + "There is a glitch in the matrix! WHAT DID YOU DO!? Happy andf?");
                player.sendMessage(ChatColor.RED + "But no, serriously, please report this error.");
                player.sendMessage(e.getMessage());
                e.printStackTrace();
                throw e.getCause();
            } catch (UnhandledCommandException e) {
                player.sendMessage(ChatColor.RED + "There is a glitch in the matrix! WHAT DID YOU DO!? Happy andf?");
                player.sendMessage(ChatColor.RED + "But no, serriously, please report this error.");
                return false;
            } finally {

            }

        } catch (Throwable excp) {

            player.sendMessage("Problem handling command: " + command);
            player.sendMessage(excp.getMessage());
            excp.printStackTrace();
            return false;

        }

        return true;

    }

    public static void info(String info) {

        instance.log.info(info);

    }

    public static void warning(String info) {

        instance.log.warning(info);

    }

    public static void severe(String info) {

        instance.log.severe(info);

    }

}
