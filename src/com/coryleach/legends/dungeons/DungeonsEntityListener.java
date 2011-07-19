/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.dungeons;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;

import com.coryleach.legends.*;

/**
 *
 * @author Cory
 */
public class DungeonsEntityListener extends EntityListener {
        private final Legends plugin;

        public DungeonsEntityListener(final Legends plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onCreatureSpawn(CreatureSpawnEvent event) {

            if ( event.isCancelled() ) {
                return;
            }

	}

	@Override
	public void onEntityDeath(EntityDeathEvent event) {

           if ( event.getEntity() instanceof Player ) {
               //Not sure if player death is listened for here but just in case
               return;
           }

           if ( event.getEntity() instanceof LivingEntity ) {
               plugin.removeCreature((LivingEntity)event.getEntity());
               return;
           }

	}

        @Override
        public void onEntityExplode(EntityExplodeEvent event) {

            Location location = event.getLocation();
            LegendsMap map = plugin.getMapForWorld(location.getWorld());
            LegendsChunk chunk = map.getChunk(location);

            //Cancel enitty explosions in all claimed chunks
            if ( chunk.isClaimed() ) {
                event.setCancelled(true);
            }

        }

}
