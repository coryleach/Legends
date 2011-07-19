/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends;

import org.bukkit.*;
import org.bukkit.entity.*;

import com.coryleach.legends.dungeons.*;

/**
 *
 * @author Cory
 */
public class LegendsCreature {

    protected LivingEntity bukkitCreature;
    protected CreaturePrefix prefix;
    protected String nickname;
    protected boolean killClaimed;

    public Location spawnLocation;
    public DungeonSubzone dungeonSubzone;

    public LegendsCreature(LivingEntity bukkitCreature) {
        this.bukkitCreature = bukkitCreature;
    }

    public LivingEntity getHandle() {
        return bukkitCreature;
    }

    public boolean isAnimal() {

        if ( bukkitCreature instanceof Animals) {
            return true;
        }

        return false;

    }

    public boolean isMonster() {

        if ( bukkitCreature instanceof Monster ) {
            return true;
        }

        return false;

    }

    public boolean killClaimed() {
        return killClaimed;
    }

    public void claimKill () {
        killClaimed = true;
    }

    public CreaturePrefix getPrefix() {
        return prefix;
    }

    public void setPrefix(CreaturePrefix prefix) {
        this.prefix = prefix;
        bukkitCreature.setHealth(prefix.maxHealth);
    }

    public String getName() {

        String name = null;

        //Use nickname if it exists
        if ( nickname == null ) {
            name = getTypeName();
        } else {
            name = nickname;
        }

        if ( prefix != null ) {
            //Add Prefix only if this isn't a lair critter
            if ( !prefix.lair ) {
                name = prefix.getName() + " " + name;
            }
        }

        return name;

    }

    public String getTypeName() {

        if ( isMonster() ) {
            
            if ( bukkitCreature instanceof Creeper ) {
                return "Creeper";
            } else if ( bukkitCreature instanceof Zombie ) {

                if ( bukkitCreature instanceof PigZombie ) {
                    return "Pig Zombie";
                }

                return "Zombie";

            } else if ( bukkitCreature instanceof Spider ) {
                return "Spider";
            } else if ( bukkitCreature instanceof Giant ) {
                return "Giant";
            } else if ( bukkitCreature instanceof Skeleton ) {
                return "Skeleton";
            }
            
            return "<Bugged Monster>";

        } else if ( isAnimal() ) {

            if ( bukkitCreature instanceof Chicken ) {
                return "Chicken";
            } else if ( bukkitCreature instanceof Sheep ) {
                return "Sheep";
            } else if ( bukkitCreature instanceof Cow ) {
                return "Cow";
            } else if ( bukkitCreature instanceof Pig ) {
                return "Pig";
            } else if ( bukkitCreature instanceof Wolf ) {
                return "Wolf";
            }

            return "<Bugged Animal>";

        } else if ( bukkitCreature instanceof WaterMob ) {
            return "Squid";
        } else if ( bukkitCreature instanceof Flying ) {
            return "Ghast";
        } else if ( bukkitCreature instanceof Slime ) {
            return "Slime";
        } else {
            return "<Bugged Creature>";
        }

    }

    public int getHealth() {
        return bukkitCreature.getHealth();
    }

    public int modifyDamage(int damage) {
        
        if (prefix == null) {
            return damage;
        }

        return Math.round((float)damage * prefix.damageMultiplier) + prefix.damageBonus;

    }

}
