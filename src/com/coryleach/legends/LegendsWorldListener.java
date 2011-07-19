/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.world.*;

/**
 *
 * @author Cory
 */
public class LegendsWorldListener extends WorldListener {

    @Override
    public void onChunkLoad(ChunkLoadEvent event) {

        Chunk chunk = event.getChunk();

        Entity[] entities = chunk.getEntities();

        for ( int i = 0; i < entities.length; i++ ) {

            Entity entity = (Entity)(entities[i]);

            if ( (entity instanceof LivingEntity) && !(entity instanceof Player) ) {

                //Legends.instance.log.info("Wrap Creature?");
                LegendsCreature creature = Legends.instance.wrapCreature((LivingEntity)entity);

                //Give the creature a random prefix
                CreaturePrefix prefix = new CreaturePrefix("Chunk");
                prefix.maxHealth = 20;
                prefix.damageBonus = 1;
                prefix.armorBonus = 1;
                prefix.damageMultiplier = 1.5f;
                creature.setPrefix(prefix);

            }

        }


    }

    @Override
    public void onChunkUnload(ChunkUnloadEvent event) {

        //Unwrap any non-player living Entities
        Chunk chunk = event.getChunk();

        Entity[] entities = chunk.getEntities();

        for ( int i = 0; i < entities.length; i++ ) {

            Entity entity = (Entity)(entities[i]);

            if ( (entity instanceof LivingEntity) && !(entity instanceof Player) ) {

                //Legends.instance.log.info("Remove Creature?");
                Legends.instance.removeCreature((LivingEntity)entity);

            }

        }

    }

    @Override
    public void onWorldLoad(WorldLoadEvent event) {


        String worldName = event.getWorld().getName();
        Legends.info("Legends loading world: " + worldName);

    }

    @Override
    public void onWorldSave(WorldSaveEvent event) {

        //Save Map
        String worldName = event.getWorld().getName();
        Legends.info("Legends saving world: " + worldName);
        //Legends.instance.worldMaps.get(worldName);

    }

}
