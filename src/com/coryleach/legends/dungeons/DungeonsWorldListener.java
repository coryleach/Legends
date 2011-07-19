/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.dungeons;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.world.*;

import com.coryleach.legends.*;

/**
 *
 * @author Cory
 */
public class DungeonsWorldListener extends WorldListener {
    protected Legends plugin;

    public DungeonsWorldListener(Legends plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onChunkLoad(ChunkLoadEvent event) {

        //Check for dungeons in this chunk
        Chunk chunk = event.getChunk();

        //
        Legends.info("Chunk loaded in world: " + event.getWorld().getName());

    }

    @Override
    public void onChunkUnload(ChunkUnloadEvent event) {

    }

    @Override
    public void onWorldLoad(WorldLoadEvent event) {

        Legends.info("Loaded world: " + event.getWorld().getName());

    }

    @Override
    public void onWorldSave(WorldSaveEvent event) {
        Legends.info("Will Save world: " + event.getWorld().getName());
    }

}
