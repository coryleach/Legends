/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends;

import com.coryleach.legends.classes.LegendsMonk;
import com.coryleach.legends.classes.LegendsThief;
import com.coryleach.legends.classes.LegendsWarrior;
import com.coryleach.legends.classes.LegendsCleric;
import com.coryleach.legends.classes.ClassType;
import com.coryleach.legends.classes.LegendsMage;
import com.coryleach.legends.classes.LegendsClass;
import com.coryleach.legends.factions.*;
import com.coryleach.util.*;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.*;

/**
 *
 * @author cory
 */
public class LegendsPlayer {

    protected transient Player bukkitPlayer;
    protected String name;

    //Class variables, Every player has all classes
    protected ClassType classType;
    protected LegendsWarrior warrior;
    protected LegendsMage mage;
    protected LegendsMonk monk;
    protected LegendsThief thief;
    protected LegendsCleric cleric;
    protected boolean newPlayer; //Player should lose newPlayer status upon reaching level 10?

    //Profession Data
    protected LegendsProfession legendsProfession;

    //Faction Info
    protected int factionId;
    protected transient LegendsFaction legendsFaction;
    protected transient FactionSession factionSession;
    public transient LegendsSession legendsSession;

    //Legends Health is tracked separately from minecraft health
    public transient int factionManageId;

    //Skills
    protected int swordSkill;
    protected int axeSkill;
    protected int staffSkill;
    protected int bowSkill;
    protected int handSkill;
    protected int shovelSkill;
    protected int hoeSkill;
    protected int pickSkill;

    protected int maxHealth;
    protected int armorRating;

    protected int purchasedClaims;
    protected int purchasedSettles;
    protected ArrayList<Integer> settlements;

    protected boolean displayCombatText;

    public LegendsPlayer(Player player) {

        name = player.getName();
        bukkitPlayer = player;
        legendsProfession = null;
        legendsFaction = null;
        warrior = new LegendsWarrior(this);
        mage = new LegendsMage(this);
        monk = new LegendsMonk(this);
        thief = new LegendsThief(this);
        cleric = new LegendsCleric(this);
        classType = ClassType.NO_CLASS;
        displayCombatText = true;
        factionId = 0;
        legendsSession = new LegendsSession();
        settlements = new ArrayList<Integer>();

    }

    public LegendsPlayer() {

        name = null;
        bukkitPlayer = null;
        legendsProfession = null;
        legendsFaction = null;
        warrior = new LegendsWarrior(this);
        mage = new LegendsMage(this);
        monk = new LegendsMonk(this);
        thief = new LegendsThief(this);
        cleric = new LegendsCleric(this);
        classType = ClassType.NO_CLASS;
        displayCombatText = true;
        factionId = 0;
        purchasedClaims = 0;
        purchasedSettles = 0;
        
        settlements = new ArrayList<Integer>();

    }

    public void playerWasLoadedFromDisk() {

        if ( settlements == null ) {
            
            purchasedClaims = 0;
            purchasedSettles = 0;
            settlements = new ArrayList<Integer>();

        } else {

            Iterator<Integer> i = settlements.iterator();

            ArrayList<Integer> newList = new ArrayList<Integer>();

            //Check to make sure all settlments still exist
            while ( i.hasNext() ) {

                Integer integer = i.next();
                int settlementId = integer.intValue();

                LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(settlementId);

                if ( settlement != null ) {
                    newList.add(integer);
                }

            }

            settlements = newList;

        }

    }

    public LegendsFaction getFaction() {

        //If we're managing a faction, return that faction instead
        if ( factionManageId != 0 ) {
            return FactionsManager.instance.getFactionWithId(this.factionManageId);
        }

        if ( legendsFaction != null ) {
            return legendsFaction;
        }

        if ( factionId == 0 ) {
            return null;
        }

        legendsFaction = FactionsManager.instance.getFactionWithId(factionId);

        if ( legendsFaction.getFactionPlayerWithName(this.getName()) != null ) {
            return legendsFaction;
        } else {
            //Player was removed from faction
            factionManageId = 0;
            legendsFaction = null;
            return null;
        }

        //return legendsFaction;

    }

    public boolean hasFaction() {

        if ( getFaction() == null ) {
            return false;
        }
        
        return true;

    }

    public void setFaction(LegendsFaction faction) {

        if ( faction == null ) {
            factionId = 0;
            legendsFaction = null;
            return;
        }

        factionId = faction.getFactionId();
        legendsFaction = faction;
        
    }

    public void updatePrefix() {

        if ( hasFaction() ) {
            Legends.instance.log.info("faction prefix set");
            Legends.instance.setPlayerPrefix(getHandle(), getFaction().getPrefix());
        } else {
            Legends.instance.setPlayerPrefix(getHandle(), null);
        }

    }

    public void updateHealth() {

        //getHandle().setHealth(this.getDisplayHealth());

    }

    public Player getHandle() {
        bukkitPlayer = Legends.instance.server.getPlayer(getName());
        return bukkitPlayer;
        //return bukkitPlayer;
    }

    //This is called after loading from file
    public void setHandle(Player player) {
        bukkitPlayer = player;
        warrior.setPlayer(this);
        mage.setPlayer(this);
        monk.setPlayer(this);
        thief.setPlayer(this);
        cleric.setPlayer(this);
    }

    public void error(String string) {
        bukkitPlayer.sendMessage(ChatColor.RED + string);
    }

    public void info(String string) {
        bukkitPlayer.sendMessage(ChatColor.GREEN + string);
    }

    public void message(String string) {
        bukkitPlayer.sendMessage(string);
    }

    public void title(String string) {
        bukkitPlayer.sendMessage(TextUtil.titleize(string,ChatColor.GREEN,ChatColor.YELLOW));
    }

    public void died() {
        
        //this.health = this.getMaxHealth();

    }

    public LegendsClass getLegendsClass() {

        LegendsClass legendsClass = null;

        switch(classType) {
            case WARRIOR:
                legendsClass = this.warrior;
                break;
            case MAGE:
                legendsClass = this.mage;
                break;
            case CLERIC:
                legendsClass = this.cleric;
                break;
            case THIEF:
                legendsClass = this.thief;
                break;
            case MONK:
                legendsClass = this.monk;
                break;
            default:
                legendsClass = null;
                classType = ClassType.NO_CLASS;
        }

        return legendsClass;
        
    }

    public void setLegendsClass(ClassType type) {

        this.classType = type;

        //Set max health on class change
        setHealthToMax();

    }

    public LegendsFaction getLegendsFaction() {

        return getFaction();

    }

    public FactionPlayer getFactionPlayer() {

        if ( !hasFaction() ) {
            return null;
        }

        //If we're just managing a faction return a bogus faction player
        if ( this.factionManageId != 0 ) {

            FactionPlayer player = new FactionPlayer();
            player.setRole(FactionRole.CHIEF_ELDER);
            player.setPlayerName(this.getName());
            return player;

        }

        return getFaction().getFactionPlayerWithName(getName());

    }

    public LegendsSession getLegendsSession() {
        
        if ( legendsSession == null ) {
            legendsSession = new LegendsSession();
        }

        return legendsSession;

    }

    public void setLegendsFaction(LegendsFaction faction) {

        if ( faction == null ) {
            this.legendsFaction = null;
            this.factionId = 0;
            return;
        }

        this.legendsFaction = faction;
        this.factionId = faction.getFactionId();

    }

    public void clearFaction() {
        this.legendsFaction = null;
    }

    public ArrayList<Integer> getSettlements() {

        if ( settlements == null ) {
            settlements = new ArrayList<Integer>();
        }

        return settlements;

    }

    public int getHealth() {

        return getHandle().getHealth();

    }

    public Chunk getCurrentChunk() {
        return bukkitPlayer.getWorld().getChunkAt(bukkitPlayer.getLocation());
    }

    public LegendsChunk getCurrentLegendsChunk() {
        Chunk bukkitChunk = getCurrentChunk();
        World world = bukkitChunk.getWorld();
        return Legends.instance.getMapForWorld(world).getChunk(bukkitChunk);
    }

    public void setHealth(int health) {

        bukkitPlayer.setHealth(health);

    }

    public void notifyHealDamage(int healDamage) {
        this.message("You gain " + ChatColor.YELLOW + Integer.toString(healDamage) + ChatColor.WHITE + " health");
    }

    public int getMaxHealth() {

        if ( classType == ClassType.NO_CLASS || getLegendsClass() == null ) {
            return 20;
        }

        return getLegendsClass().getMaxHealth();

    }

    public void setHealthToMax() {

        bukkitPlayer.setHealth(getMaxHealth());
        
    }

    /*public int getDisplayHealth() {

        int displayHealth = (int)Math.floor(((float)this.getHealth() * 20.0f) / (float)this.getMaxHealth());

        //Never display less than 1 health
        //We leave killing the player and setting display health to 0 up to the damage event
        if ( displayHealth <= 0 ) {
            return 1;
        }

        return displayHealth;

    }*/

    public boolean isNewPlayer() {

        if ( classType == ClassType.NO_CLASS ) {
            return true;
        }

        return false;

    }

    public String getName() {

        return name;

    }

    public void setDisplayCombatText(boolean value) {

        this.displayCombatText = value;

    }

    public int getMaxArmorValue() {

        /*int sum = 0;

        ItemStack[] armor = getHandle().getInventory().getArmorContents();

        for ( int i = 0; i < armor.length; i++ ) {

            ItemStack item = armor[i];

            sum += item.getType().getMaxDurability();

        }*/

        return 1340;

    }

    public int getArmorValue() {

        int sum = 0;

        ItemStack[] armor = getHandle().getInventory().getArmorContents();

        for ( int i = 0; i < armor.length; i++ ) {
            
            ItemStack item = armor[i];
            switch ( item.getType() ) {
                case LEATHER_BOOTS:
                case LEATHER_CHESTPLATE:
                case LEATHER_HELMET:
                case LEATHER_LEGGINGS:
                    sum += 1;
                    break;
                case CHAINMAIL_BOOTS:
                case CHAINMAIL_CHESTPLATE:
                case CHAINMAIL_HELMET:
                case CHAINMAIL_LEGGINGS:
                    sum += 2;
                    break;
                case GOLD_BOOTS:
                case GOLD_CHESTPLATE:
                case GOLD_HELMET:
                case GOLD_LEGGINGS:
                    sum += 3;
                    break;
                case IRON_BOOTS:
                case IRON_CHESTPLATE:
                case IRON_HELMET:
                case IRON_LEGGINGS:
                    sum += 4;
                    break;
                case DIAMOND_BOOTS:
                case DIAMOND_CHESTPLATE:
                case DIAMOND_HELMET:
                case DIAMOND_LEGGINGS:
                    sum += 5;
                    break;
            }

        }

        return sum;

    }

    public float getArmorEffectiveness() {

        float maximumArmorValue = 20.0f;
        float armorValue = this.getArmorValue();
        float maxEffectiveness = 0.75f;

        if ( armorValue > maximumArmorValue ) {
            armorValue = maximumArmorValue;
        }
        
        float effectiveness = maxEffectiveness * (armorValue / maximumArmorValue);

        return effectiveness;
        
    }

    public boolean displayCombatText() {

        return this.displayCombatText;

    }

    public int getClaimPoints() {

        int points = getMaxClaimPoints() - getClaimedChunks();

        return points;

    }

    public int getMaxClaimPoints() {

        return 5 + purchasedClaims;

    }

    public int getClaimedChunks() {

        //Claimed chunks is the sum of the settlement sizes
        Iterator<Integer> i = getSettlements().iterator();

        int sum = 0;
        while ( i.hasNext() ) {

            Integer id = i.next();
            LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(id.intValue());
            sum += settlement.size();

        }

        return sum;

    }

    public int getPurchasedClaimPoints() {
        return purchasedClaims;
    }

    public int getPurchasedSettlePoints() {
        return purchasedSettles;
    }

    public void addPruchasedClaimPoints(int points) {
        purchasedClaims += points;
    }

    public void addPurchasedSettlementPoints(int points) {
        purchasedSettles += points;
    }

    public int getSettlementPoints() {
        return getMaxSettlementPoints() - getSettlements().size();
    }

    public int getMaxSettlementPoints() {
        return 1 + purchasedSettles;
    }

    public void addSettlementPoints(int value) {
        purchasedSettles += value;
    }

    public void addClaimPoints(int value) {
        purchasedClaims += value;
    }

    public void addSettlement(LegendsSettlement settlement) {

        getSettlements().add(settlement.getSettlementId());

    }

    public void removeSettlement(LegendsSettlement settlement) {

        Integer id = new Integer(settlement.getSettlementId());

        if ( !getSettlements().remove(id) ) {
            Legends.warning("Failed to remove settlement from player!");
        }

    }

    public LegendsSettlement getAdjacentSettlement(LegendsChunk chunk) {

        Iterator<Integer> i = getSettlements().iterator();

        while ( i.hasNext() ) {

            Integer id = i.next();

            LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(id.intValue());

            if ( settlement.isAdjacent(chunk) ) {

                return settlement;

            }

        }

        return null;

    }

    public void sendSettlements(LegendsPlayer player) {

        player.title(this.getName() + "'s Settlements");

        Iterator<Integer> i = getSettlements().iterator();

        while ( i.hasNext() ) {

            Integer id = i.next();

            LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(id.intValue());

            if ( settlement == null ) {
                player.error("(NULL) Settlement! - tell admin to run /fixmaps command.");
            } else {
                Location loc = settlement.getLocation();
                player.info(settlement.getName() + " (" + loc.getBlockX() + "," + loc.getBlockZ() + ")");
            }

        }

    }

    public void fix() {

        Iterator<Integer> i = getSettlements().iterator();

        while ( i.hasNext() ) {

            Integer id = i.next();
            LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(id.intValue());

            if ( settlement == null ) {

                i.remove();

            }

        }

    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if ( o instanceof LegendsPlayer ) {
            LegendsPlayer player = (LegendsPlayer)o;
            if ( this.name.equals(player.getName()) ) {
                return true;
            }
        }

        return false;
        
    }

}
