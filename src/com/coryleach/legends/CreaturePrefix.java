/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends;

import java.util.*;
import org.bukkit.inventory.*;

/**
 *
 * @author Cory
 */
public class CreaturePrefix {

    protected String name;
    
    public int experience;
    public int maxHealth;
    public float damageMultiplier;
    public int damageBonus;
    public int armorBonus;
    public boolean lair;
    public List<ItemStack> drops;


    public CreaturePrefix(String name) {
        this.name = name;
        experience = 10;
        maxHealth = 10;
        damageMultiplier = 1.0f;
        damageBonus = 1;
        armorBonus = 1;
        lair = false;
        drops = new ArrayList<ItemStack>();
    }

    public String getName() {
        return name;
    }


}
