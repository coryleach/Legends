/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.dungeons;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.block.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

import com.coryleach.legends.*;

/**
 *
 * @author Cory
 */
public class DungeonsPlayerListener extends PlayerListener {
    protected Legends plugin;

    public DungeonsPlayerListener(Legends plugin) {
        this.plugin = plugin;
    }
    

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {

       LegendsPlayer player = Legends.instance.wrapPlayer(event.getPlayer());

       

    }

}
