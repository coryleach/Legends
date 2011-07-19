/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.classes;

import com.coryleach.legends.LegendsPlayer;
import org.bukkit.event.entity.*;

/**
 *
 * @author Cory
 */
public class LegendsWarrior extends LegendsClass {

    public LegendsWarrior(LegendsPlayer player) {
        super(ClassType.WARRIOR,player);
    }

    @Override
    public int getMaxHealth() {
        return 20 + (level * 10);
    }

    @Override
    public float getDamageModifier() {
        return 1.0f + (level * 0.2f);
    }

    /*
    @Override
    public void defend(LegendsCreature creature,EntityDamageByEntityEvent event) {

    }

    @Override
    public void attack(LegendsCreature creature,EntityDamageByEntityEvent event) {

    }

    @Override
    public void defendPvp(LegendsPlayer player,LegendsPlayer enemy,EntityDamageByEntityEvent event) {

    }

    @Override
    public void attackPvp(LegendsPlayer player,LegendsPlayer enemy,EntityDamageByEntityEvent event) {

    }*/

}
