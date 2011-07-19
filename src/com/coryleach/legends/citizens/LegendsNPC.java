/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.citizens;

import org.bukkit.entity.*;

import com.citizens.npctypes.interfaces.*;
import com.citizens.resources.npclib.*;
import com.citizens.interfaces.*;

/**
 *
 * @author Cory
 */
public class LegendsNPC extends Toggleable implements Saveable, NPCPurchaser {

    public LegendsNPC(HumanNPC npc) {
        super(npc);


    }

    @Override
    public String getType() {
        return "Trainer";
    }

    //
    //NPC Purchasser Interface Methods
    //
    public boolean canBuy(Player player, String type) {
        return true;
    }

    public String getNoMoneyMessage(Player player, HumanNPC npc, String type) {
        return "No Money";
    }

    public String getNoPermissionsMessage(Player player, String type) {
        return "No Permission";
    }

    public String getPaidMessage(Player player, HumanNPC npc, double paid, String type) {
        return "You paid $" + paid;
    }

    public boolean hasPermission(Player player, String type) {
        return true;
    }

    public double pay(Player player, String type) {
        return 10;
    }

    //
    //Savable Interface Methods
    //
    public void copy(int j, int k) {
        
    }

    public boolean getEnabled(HumanNPC npc) {
        return true;
    }

    public void loadState(HumanNPC npc) {
        //npc.getNPCData().
        
    }

    public void saveState(HumanNPC npc) {
        
    }

    public void setEnabled(HumanNPC npc, boolean value) {
    }

    public void register(HumanNPC npc) {
        
    }

}
