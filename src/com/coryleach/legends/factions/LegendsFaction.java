/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.factions;

import com.coryleach.legends.Legends;
import com.coryleach.legends.LegendsChunk;
import com.coryleach.legends.LegendsPlayer;
import com.coryleach.legends.factions.SettlementManager;
import java.io.*;
import java.util.*;
import org.bukkit.*;

//Factions can be open to anyone or invite only
//Need a browsable list of factions + open/closed status
//3 relationships Ally, Enemy, Neutral
//Prevent enemies/neutrals from entering faction zone?
//Player can create a faction but it will not receive any claim points
// till member count is 4 or greater

/**
 *
 * @author Cory
 */
public class LegendsFaction {

    protected int factionId;
    protected String name;
    protected String prefix;

    protected ArrayList<FactionPlayer> members;

    //Claimed Chunks
    protected int specialClaimPoints;

    //Settlements
    protected int specialSettlementPoints;

    protected ArrayList<Integer> settlements;

    protected int level;
    protected int experience;

    protected String leader;

    protected Location spawn;
    protected ChatColor color;

    protected HashMap<String,String> properties;

    public String motd;

    public LegendsFaction(String name, String prefix, String leader) {

            this.factionId = FactionsManager.instance.getUnusedFactoinId();
            this.name = name;
            this.prefix = prefix;
            this.leader = leader;
            this.members = new ArrayList<FactionPlayer>();
            this.members.add(new FactionPlayer(leader,FactionRole.CHIEF_ELDER));
            this.color = ChatColor.WHITE;
            this.motd = "Welcome to " + name + "! This is the default faction MOTD(message of the day) use /fsetmotd to change it.";
            this.specialClaimPoints = 0;
            this.settlements = new ArrayList<Integer>();
            this.properties = new HashMap<String,String>();

            level = 1;
            experience = 0;

    }

    public LegendsFaction(String name, String prefix, LegendsPlayer leader) {

            this.factionId = FactionsManager.instance.getUnusedFactoinId();
            this.name = name;
            this.prefix = prefix;
            this.leader = leader.getHandle().getName();
            this.members = new ArrayList<FactionPlayer>();
            this.members.add(new FactionPlayer(this.leader,FactionRole.CHIEF_ELDER));
            this.color = ChatColor.WHITE;
            this.motd = "Welcome to " + name + "! This is the default faction MOTD(message of the day) use /fsetmotd to change it.";
            this.specialClaimPoints = 0;
            this.settlements = new ArrayList<Integer>();
            this.properties = new HashMap<String,String>();

            level = 1;
            experience = 0;

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
    
    public void setPublic(boolean value) {
        this.setProperty("public",String.valueOf(value));
    }

    public boolean isPublic() {
        return this.getProperty("public");
    }

    public int getFactionId() {
        return factionId;
    }

    public void addSpecialClaimPoints(int points) {
        this.specialClaimPoints += points;

        if ( this.specialClaimPoints < 0 ) {
            this.specialClaimPoints = 0;
        }

    }

    public void setSpecialClaimPoints(int points) {
        this.specialClaimPoints = points;
    }

    public int getSpecialClaimPoints() {
        return specialClaimPoints;
    }

    public void addSpecialSettlementPoints(int points) {
        this.specialSettlementPoints += points;

        if ( this.specialSettlementPoints < 0 ) {
            this.specialSettlementPoints = 0;
        }
        
    }

    public void setSpecialSettlementPoints(int points) {
        this.specialSettlementPoints = points;
    }

    public int getSpecialSettlementPoints() {
        return specialSettlementPoints;
    }

    public int getClaimedChunks() {

        //Claimed chunks is the sum of the settlement sizes
        Iterator<Integer> i = settlements.iterator();

        int sum = 0;
        while ( i.hasNext() ) {

            Integer id = i.next();
            LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(id.intValue());
            sum += settlement.size();

        }

        return sum;
        
    }

    public void chunkWasClaimed() {

        if ( specialClaimPoints > 0 ) {
            specialClaimPoints -= 1;
        }

    }

    public void settlementWasClaimed() {

        if ( specialSettlementPoints > 0 ) {
            specialSettlementPoints -= 1;
        }

    }

    public LegendsSettlement getAdjacentSettlement(LegendsChunk chunk) {

        Iterator<Integer> i = settlements.iterator();

        while ( i.hasNext() ) {

            Integer id = i.next();

            LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(id.intValue());

            if ( settlement.isAdjacent(chunk) ) {

                return settlement;

            }

        }

        return null;

    }

    public void addSettlementId(int id) {
        Integer settlementId = new Integer(id);
        settlements.add(settlementId);
    }

    public boolean removeSettlementId(int id) {
        Integer settlementId = new Integer(id);
        return settlements.remove(settlementId);
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getMotd() {
        return color + this.motd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return (color + name + ChatColor.WHITE);
    }

    public int population() {
        return this.members.size();
    }

    public int getMaxClaimPoints() {

        if ( population() < 3 ) {
            return 0;
        }

        return (population() * 5);

    }

    public int getClaimPoints() {

        int points = (getMaxClaimPoints() - getClaimedChunks());

        if ( points < 0 ) {
            points = 0;
        }

        return specialClaimPoints + points;
        
    }

    public int getSettlements() {
        return settlements.size();
    }

    public int getSettlementPoints() {

        int points = (getMaxSettlementPoints() - settlements.size());

        if ( points < 0 ) {
            points = 0;
        }

        return specialSettlementPoints + points;
        
    }

    public int getMaxSettlementPoints() {

        if ( population() < 3 ) {
            return 0;
        }

        return population() / 3;

    }

    public FactionPlayer getFactionPlayerWithName(String player) {

        Iterator<FactionPlayer> i = members.iterator();

        while ( i.hasNext() ) {
            FactionPlayer factionPlayer = i.next();

            if ( factionPlayer.isOnline() ) {

                if ( factionPlayer.getPlayer().getName().equals(player) ) {
                    return factionPlayer;
                }

            } else if(factionPlayer.getPlayerName().equals(player)) {
                return factionPlayer;
            }

        }

        return null;

    }

    public void addMember(LegendsPlayer player) {

        this.members.add(new FactionPlayer(player.getHandle().getName(),FactionRole.RESIDENT));
        player.setLegendsFaction(this);
        
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {

        String displayPrefix = color + prefix + ChatColor.GRAY + "-";
        return displayPrefix.replaceAll("\u00A7", "&");
        
    }

    public String getRawPrefix() {
        return prefix;
    }

    public void setSpawn(Location loc) {
        this.spawn = loc;
    }

    public Location getSpawn() {
        return this.spawn;
    }
    
    public void broadcast(String message) {

        Iterator<FactionPlayer> i = members.iterator();

        while ( i.hasNext() ) {

            FactionPlayer factionPlayer = i.next();
            LegendsPlayer legendsPlayer = factionPlayer.getPlayer();

            //For every online player in the list
            if ( legendsPlayer != null ) {
                legendsPlayer.message(color + message);
            }

        }

    }

    public void updatePlayers() {

        Iterator<FactionPlayer> i = members.iterator();

        while ( i.hasNext() ) {

            FactionPlayer factionPlayer = i.next();
            LegendsPlayer legendsPlayer = factionPlayer.getPlayer();

            //For every online player in the list
            if ( legendsPlayer != null ) {
                legendsPlayer.updatePrefix();
            }

        }

    }

    public void removeAllPlayers() {

        Iterator<FactionPlayer> i = members.iterator();

        while ( i.hasNext() ) {

            FactionPlayer factionPlayer = i.next();
            LegendsPlayer legendsPlayer = factionPlayer.getPlayer();

            if ( legendsPlayer != null ) {
                legendsPlayer.clearFaction();
            }

        }

        members.removeAll(members);

    }

    public void removePlayer(FactionPlayer player) {

        //Make sure we actually have this player
        if ( !members.contains(player) ) {
            return;
        }

        //Remove
        if ( player.getPlayer() != null ) {
            player.getPlayer().setLegendsFaction(null);
        }

        members.remove(player);

    }

    public void sendInfoToPlayer(LegendsPlayer player) {

        player.info("Faction: " + this.name + "(" + color + prefix + ChatColor.WHITE + ")");

        player.info("Level " +
                ChatColor.YELLOW +
                Integer.toString(level) +
                ChatColor.WHITE + " Exp: " +
                ChatColor.YELLOW +
                Integer.toString(experience));

        player.info("Population: " + ChatColor.YELLOW + Integer.toString(this.population()));

        player.info("Claimed: " +
                ChatColor.YELLOW +
                Integer.toString(getClaimedChunks()) +
                ChatColor.GREEN +
                "/" +
                ChatColor.YELLOW + 
                Integer.toString(this.getMaxClaimPoints()) +
                ChatColor.GREEN +
                " Settlements: " +
                ChatColor.YELLOW +
                Integer.toString(this.getSettlements()) +
                ChatColor.GREEN +
                "/" +
                ChatColor.YELLOW +
                Integer.toString(this.getMaxSettlementPoints())
                );

        player.info("This faction can claim " + this.getClaimPoints() + " more chunk(s).");
        player.info("This faction can create " + this.getSettlementPoints() + " more settlement(s).");

    }

    public void sendFactionListToPlayer(LegendsPlayer player) {

        Iterator<FactionPlayer> i = members.iterator();

        while ( i.hasNext() ) {

            FactionPlayer fplayer = i.next();

            player.message(
                    color +
                    fplayer.getPlayerName() + " " +
                    "(" + fplayer.getRole().toString() + ")"
                    );

        }

    }


}
