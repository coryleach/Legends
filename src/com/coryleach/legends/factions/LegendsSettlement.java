/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.factions;
import com.coryleach.legends.*;
import com.coryleach.legends.LegendsChunk;
import java.util.*;
import org.bukkit.*;

/**
 *
 * @author Cory
 */
public class LegendsSettlement {

    protected int settlementId;
    protected String name;
    protected ArrayList<ChunkLocation> chunks;
    protected ArrayList<String> builders;
    protected int factionId;
    protected String owner;
    public boolean isPublic;
    protected HashMap<String,String> properties;
    protected String world;

    /*public LegendsSettlement(String name,String world) {
        this.settlementId = SettlementManager.getInstance().getUnusedSettlementId();
        this.name = name;
        chunks = new ArrayList<ChunkLocation>();
        factionId = 0;
        owner = null;
        this.isPublic = false;
        properties = new HashMap<String,String>();
        this.world = world;
    }*/

    public LegendsSettlement(int factionId,String name,String world) {
        this.settlementId = SettlementManager.getInstance().getUnusedSettlementId();
        this.name = name;
        chunks = new ArrayList<ChunkLocation>();
        this.factionId = factionId;
        this.owner = null;
        this.isPublic = false;
        builders = new ArrayList<String>();
        properties = new HashMap<String,String>();
        this.world = world;
    }

    public LegendsSettlement(String ownerName, String name,String world) {
        this.settlementId = SettlementManager.getInstance().getUnusedSettlementId();
        this.name = name;
        chunks = new ArrayList<ChunkLocation>();
        this.owner = ownerName;
        this.isPublic = false;
        builders = new ArrayList<String>();
        properties = new HashMap<String,String>();
        this.world = world;
    }

    public void checkWorld() {

        if ( this.world == null ) {
            Legends.info("Fixing world for settlement " + this.name);
            this.world = "world";

            //Make sure all chunks are set to this world as well
            Iterator<ChunkLocation> i = chunks.iterator();

            while ( i.hasNext() ) {

                ChunkLocation loc = i.next();
                loc.setWorld(this.world);

            }

        }

    }

    public HashMap<String,String> getProperties() {

        if ( properties == null ) {
            properties = new HashMap<String,String>();
        }

        return properties;

    }

    public void setProperty(String property,String value) {
        getProperties().put(property, value);
    }

    public void setProperty(String property,boolean value) {
        getProperties().put(property, String.valueOf(value));
    }

    public boolean getProperty(String property) {

        String value = getProperties().get(property);

        if ( value == null ) {
            return false;
        }

        return Boolean.parseBoolean(value);
        
    }

    public void settlementLoadedFromDisk() {

        if ( this.builders == null ) {
            builders = new ArrayList<String>();
        }

        if ( this.properties == null ) {
            properties = new HashMap<String,String>();
        }

        checkWorld();

    }

    public int getSettlementId() {
        return this.settlementId;
    }

    public void setSettlementId(int id) {
        this.settlementId = id;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFactionId() {
        return factionId;
    }

    public void setFactionId(int factionId) {

        this.factionId = factionId;

        Iterator<ChunkLocation> i = chunks.iterator();

        while ( i.hasNext() ) {

            ChunkLocation loc = i.next();

            LegendsMap map = Legends.instance.getMapForWorld(loc.getWorld());
            LegendsChunk chunk = map.getChunkAt(loc.getX(), loc.getZ());

            chunk.setFactionId(factionId);

        }

    }

    public String getOwnerName() {
        return owner;
    }

    public void setOwnerName(String name) {
        this.owner = name;
    }

    public void addBuilder(String name) {

        if ( isBuilder(name) ) {
            return;
        }

        if ( builders == null ) {
            Legends.instance.log.info("Builders array is null!!");
            return;
        }
         Legends.instance.log.info("add");

        builders.add(name.toLowerCase());

    }

    public void removeBuilder(String name) {

        builders.remove(name.toLowerCase());

    }

    public ArrayList<String> getBuilders() {
        return builders;
    }

    public boolean isBuilder(String name) {

        if ( isPublic ) {
            return true;
        }

        if ( owner != null && owner.equalsIgnoreCase(name) ) {
            return true;
        }

        Iterator<String> i = builders.iterator();

        while ( i.hasNext() ) {

            String builder = i.next();

            if ( builder.equalsIgnoreCase(name) ) {
                return true;
            }

        }

        return false;

    }

    public void sendSettlerList(LegendsPlayer player) {

        Iterator<String> i = builders.iterator();

        player.info("---=[" + ChatColor.YELLOW + "Settlers" + ChatColor.YELLOW + "]=---");

        while ( i.hasNext() ) {

            String name = i.next();

            player.info(name);

        }

    }

    public int size() {
        return chunks.size();
    }

    public void addChunk(ChunkLocation location) {
        chunks.add(location);
        updateChunk(location.getLegendsChunk());
    }

    public void addChunk(LegendsChunk chunk) {
        chunks.add(new ChunkLocation(chunk));
        updateChunk(chunk);
    }

    public void addChunk(Chunk chunk) {
        chunks.add(new ChunkLocation(chunk));
        LegendsMap map = Legends.instance.getMapForWorld(chunk.getWorld());
        updateChunk(map.getChunk(chunk));
    }

    public boolean removeChunk(ChunkLocation location) {
        return chunks.remove(location);
    }

    public boolean removeChunk(LegendsChunk chunk) {
        return chunks.remove(new ChunkLocation(chunk));
    }

    public boolean removeChunk(Chunk chunk) {
        return chunks.remove(new ChunkLocation(chunk));
    }

    public void removeAllChunks() {

        Iterator<ChunkLocation> i = chunks.iterator();

        while ( i.hasNext() ) {

            ChunkLocation chunkLocation = i.next();

            LegendsChunk chunk = chunkLocation.getLegendsChunk();

            chunk.setFactionId(0);
            chunk.setSettlementId(0);

            chunk.broadcast(chunk.getEnterMessage());

        }

        chunks.clear();

    }

    public boolean isAdjacent(LegendsChunk chunk) {

        ChunkLocation location = new ChunkLocation(chunk);

        Iterator<ChunkLocation> i = chunks.iterator();

        while ( i.hasNext() ) {

            ChunkLocation settlementLocation = i.next();

            if ( settlementLocation.isAdjacent(location) ) {
                return true;
            }

        }

        return false;

    }

    public Location getLocation() {

        ChunkLocation location = chunks.get(0);

        return location.getChunk().getBlock(0, 0, 0).getLocation();

    }

    protected void updateChunk(LegendsChunk chunk) {
        chunk.setFactionId(factionId);
        chunk.setSettlementId(this.settlementId);
    }

    protected void clearChunk(LegendsChunk chunk) {
        chunk.setFactionId(0);
        chunk.setSettlementId(0);
    }

    public boolean spawnCreatures() {

        if ( this.getProperty("haven") ) {
            return false;
        }

        return true;

    }

}