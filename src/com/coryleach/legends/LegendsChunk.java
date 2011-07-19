/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends;

import com.coryleach.legends.factions.*;
import org.bukkit.*;
import org.bukkit.entity.*;

/**
 *
 * @author Cory
 */
public class LegendsChunk {

    int x;
    int z;
    String world;
    int factionId;
    int settlementId;
    public boolean build;
    
    public LegendsChunk(int x, int z,String world) {
        this.x = x;
        this.z = z;
        this.world = world;
        boolean build = false;
    }

    public LegendsChunk(Chunk chunk) {
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.world = chunk.getWorld().getName();
        boolean build = false;
    }

    public LegendsChunk(Location loc) {
        Chunk chunk = loc.getWorld().getChunkAt(loc);
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.world = chunk.getWorld().getName();
        boolean build = false;
    }

    public boolean shouldSave() {

        if ( factionId != 0 || settlementId != 0 ) {
            return true;
        }

        return false;

    }

    public ChunkLocation getLocation() {
        return new ChunkLocation(this);
    }

    public Chunk getChunk() {
        return Legends.instance.getServer().getWorld(world).getChunkAt(x, z);
    }

    public void setFactionId(int factionId) {
        this.factionId = factionId;
    }

    public int getFactionId() {
        return factionId;
    }

    public int getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(int settlementId) {
        this.settlementId = settlementId;
    }

    public boolean isFactionOwned() {

        if ( factionId == 0 ) {
            return false;
        }
        
        return true;

    }

    public LegendsSettlement getSettlement() {

        if ( settlementId == 0 ) {
            return null;
        }

        return SettlementManager.getInstance().getSettlementForId(settlementId);

    }

    public LegendsFaction getFaction() {

        if ( factionId == 0 ) {
            return null;
        }

        return FactionsManager.instance.getFactionWithId(factionId);

    }

    public boolean isClaimed() {

        if ( settlementId == 0 ) {
            return false;
        }

        return true;

    }

    public boolean sameOwner(LegendsChunk chunk) {

        //Both chunks must be claimed for this to return true
        if ( !chunk.isClaimed() || !this.isClaimed() ) {
            return false;
        }

        //Check if we're faction or player owned
        if ( chunk.isFactionOwned() ) {

            if ( chunk.factionId == this.factionId ) {
                return true;
            }

        } else {

            if ( this.settlementId == chunk.settlementId ) {
                return true;
            }

        }

        return false;

    }

    public boolean sameSettlement(LegendsChunk chunk) {

        if ( this.getSettlementId() == 0 ) {
            return false;
        }

        if ( chunk.getSettlementId() == this.getSettlementId() ) {
            return true;
        }

        return false;

    }

    public String getEnterMessage() {

        if ( settlementId != 0 ) {
            LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(settlementId);
            return settlement.getDisplayName();
        } else {
            return "Wilderness~";
        }

    }

    public String getFormattedEnterMessage() {

        ChatColor color = ChatColor.YELLOW;

        if ( settlementId != 0 ) {
            
            LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(settlementId);

            if ( settlement.getProperty("haven") ) {
                color = ChatColor.AQUA;
            } else if ( settlement.getProperty("arena") ) {
                color = ChatColor.RED;
            }
            
            return color + settlement.getDisplayName();

        } else {
            return color + "~Wilderness~";
        }

    }

    public Chunk getBukkitChunk() {
        return Legends.instance.server.getWorld(world).getChunkAt(x, z);
    }

    public void broadcast(String message) {

        Entity[] entities = getBukkitChunk().getEntities();

        for ( int i = 0; i < entities.length; i++ ) {

            if ( entities[i] instanceof Player ) {

                Player player = (Player)entities[i];
                player.sendMessage(message);

            }

        }


    }

}
