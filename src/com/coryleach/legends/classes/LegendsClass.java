/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.classes;

import com.coryleach.legends.LegendsCreature;
import com.coryleach.legends.LegendsPlayer;
import org.bukkit.*;
import org.bukkit.event.entity.*;

/**
 *
 * @author cory
 */
public class LegendsClass {

    protected ClassType type;
    protected int experience;
    protected int level;
    protected transient LegendsPlayer player;

    public LegendsClass(ClassType type, LegendsPlayer player) {
        this.type = type;
        this.player = player;

        level = 1;
        experience = 0;

    }

    public void setPlayer(LegendsPlayer player) {
        this.player = player;
    }

    public String getName() {
        return classTypeToString(this.type);
    }

    public boolean addExperience(int points) {

        experience += points;

        return false;

    }

    public int experienceNeededToNextLevel() {

        int pointsNeeded = 100 + ((level - 1) * 20);
        
        return pointsNeeded;

    }

    public boolean canLevel() {

        if ( experience >= experienceNeededToNextLevel() ) {
            return true;
        }

        return false;

    }

    public void level() {

        if ( !canLevel() ) {
            return;
        }

        level += 1;
        experience = 0;
        player.setHealthToMax();

    }

    public int getMaxHealth() {
        return 20;
    }

    public float getDamageModifier() {
        return 1.0f;
    }

    public static String classTypeToString(ClassType type) {

        switch(type) {
            case WARRIOR:
                return "Warrior";
            case MAGE:
                return "Mage";
            case CLERIC:
                return "Cleric";
            case THIEF:
                return "Thief";
            case MONK:
                return "Monk";
        }

        return null;

    }

    public void sendClassInfoToPlayer(LegendsPlayer player) {

        player.info("Current Class: " + ChatColor.YELLOW + getName() );
        player.info("Level: " + ChatColor.YELLOW + Integer.toString(level));

        player.info("Health: " +
                ChatColor.YELLOW +
                Integer.toString(player.getHealth()) +
                ChatColor.GREEN + 
                "/" +
                ChatColor.YELLOW +
                Integer.toString(getMaxHealth()) +
                " Debug: " + Integer.toString(player.getHealth())
                );

        player.info("Experience: " + ChatColor.YELLOW + Integer.toString(experience));
        player.info("Experience needed to level: " + ChatColor.YELLOW + Integer.toString(experienceNeededToNextLevel()));

    }

    public void defend(LegendsCreature creature,EntityDamageByEntityEvent event) {

        int damage = event.getDamage();

        //Give creature damage adds
        damage = creature.modifyDamage(damage);

        //Give player defense adds

        //Set Damage
        event.setDamage(damage);
        
        //player.setDamage(damage);

        /*if ( player.getHealth() <= damage ) {
            //Player Death
            event.setDamage(player.getHandle().getHealth());
        } else {
            //Compensate for the 1 damage we always deal
            player.getHandle().setHealth(player.getHandle().getHealth()+1);
            event.setDamage(1);
        }*/

        if ( !player.displayCombatText() ) {
            return;
        }

        player.message(
            creature.getName() +
            " hit you for " +
            ChatColor.RED +
            Integer.toString(damage) +
            ChatColor.WHITE +
            " damage. " + Integer.toString(player.getHealth())
            );

    }

    public void attack(LegendsCreature creature,EntityDamageByEntityEvent event) {

        int damage = event.getDamage();
        damage = Math.round(damage * this.getDamageModifier());

        event.setDamage(damage);

        if ( !player.displayCombatText() ) {
            return;
        }

        player.message(
            "You hit " +
            creature.getName() +
            " for " +
            ChatColor.RED +
            Integer.toString(damage) +
            ChatColor.WHITE +
            " damage. "
            );

    }

    public void defendPvp(LegendsPlayer player,LegendsPlayer enemy,EntityDamageByEntityEvent event) {

    }

    public void attackPvp(LegendsPlayer player,LegendsPlayer enemy,EntityDamageByEntityEvent event) {

    }

    public static void NoClassAttack(LegendsPlayer player, LegendsCreature creature,EntityDamageByEntityEvent event) {

        int damage = event.getDamage();

        player.message(
            "You hit " +
            creature.getName() +
            " for " +
            ChatColor.RED +
            Integer.toString(damage) +
            ChatColor.WHITE +
            " damage. "
            );
        
    }

    public static void NoClassDefend(LegendsPlayer player, LegendsCreature creature,EntityDamageByEntityEvent event) {

        int damage = event.getDamage();

        float armorEffect = player.getArmorEffectiveness();

        damage = damage - Math.round(damage * armorEffect);

        if ( damage == 0 ) {
            damage = 1;
        }

        event.setDamage(damage);
        //player.setDamage(damage);

        /*if ( player.getHealth() <= 0 ) {
            //Player Death
            event.setDamage(player.getHandle().getHealth());
        } else {
            //Compensate for the 1 damage we always deal
            player.getHandle().setHealth(player.getHandle().getHealth()+1);
            event.setDamage(1);
        }*/

        if ( !player.displayCombatText() ) {
            return;
        }

        player.message(
            creature.getName() +
            " hit you for " +
            ChatColor.RED +
            Integer.toString(damage) +
            ChatColor.WHITE +
            " damage. ");

    }

}
