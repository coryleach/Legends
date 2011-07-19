/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends;

import org.bukkit.*;
import org.bukkit.event.block.*;
import com.coryleach.legends.factions.*;

/**
 *
 * @author Cory
 */
public class LegendsBlockListener extends BlockListener {

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

        if ( event.isCancelled() ) {
            return;
        }

        Chunk chunk = event.getBlock().getChunk();
        World world = chunk.getWorld();
        LegendsMap map = Legends.instance.getMapForWorld(world);
        LegendsChunk legendsChunk = map.getChunkAt(chunk.getX(), chunk.getZ());

        //Check if this chunk is claimed
        if ( !legendsChunk.isClaimed() ) {
            return;
        }

        LegendsPlayer player = Legends.instance.wrapPlayer(event.getPlayer());

        //Do nothing if the player is OP
        if ( player.getHandle().isOp() ) {
            return;
        } else if ( Legends.instance.hasPermission(player.getHandle(), "legends.admin.build") ) {
            return;
        }

        LegendsSettlement settlement = legendsChunk.getSettlement();

        if ( legendsChunk.isFactionOwned() ) {

            if ( player.hasFaction() ) {
                if ( legendsChunk.getFactionId() == player.getFaction().getFactionId() ) {

                    if ( legendsChunk.build ) {
                        //This chunk is public
                        return;
                    } else if (player.getFaction().getProperty("build")) {
                        //Faction building is public
                        return;
                    } else if ( settlement.getProperty("build") ) {
                        //Settlement is public so player can build
                        return;
                    } else {
                        //Check if this player is a faction builder
                        FactionPlayer factionPlayer = player.getFactionPlayer();
                        if ( factionPlayer.getProperty("build") ) {
                            return;
                        } else if ( factionPlayer.getRole().canDo("build") ) {
                            return;
                        }
                    }

                }
            }

            //Check if this player has been added as a settler
            if ( settlement.isBuilder(player.getName()) ) {
                return;
            }

            event.setCancelled(true);
            player.error("You don't have permission to build here!");
            return;

        } else {

            //Check if player is a builder
            if ( !settlement.isBuilder(player.getName()) ) {
                event.setCancelled(true);
                player.error("You don't have permission to build here!");
                return;
            }

        }

    }

    /*@Override
    public void onBlockCanBuild(BlockCanBuildEvent event) {

        Chunk chunk = event.getBlock().getChunk();
        LegendsChunk legendsChunk = Legends.instance.map.getChunkAt(chunk.getX(), chunk.getZ());

        //Default to buildable
        event.setBuildable(true);

        //Check if this chunk is claimed
        if ( !legendsChunk.isClaimed() ) {
            return;
        }

        LegendsPlayer player = Legends.instance.wrapPlayer(event.getPlayer());

        if ( legendsChunk.isFactionOwned() ) {
            //Check if player is part of owning faction

            if ( !player.hasFaction() ) {
                event.setBuildable(false);
                player.error("You don't have permission to build here!");
                return;
            }


            if ( !legendsChunk.getFaction().equals(player.getFaction().getName()) ) {
                event.setBuildable(false);
                player.error("You don't have permission to build here!");
                return;
            }

        } else {

            //Check if player is owner
            if ( !legendsChunk.getOwner().equals(player.getName()) ) {
                event.setBuildable(false);
                player.error("You don't have permission to build here!");
                return;
            }

        }

    }*/

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {

        if ( event.isCancelled() ) {
            return;
        }

        Chunk chunk = event.getBlock().getChunk();
        World world = chunk.getWorld();
        LegendsMap map = Legends.instance.getMapForWorld(world);
        LegendsChunk legendsChunk = map.getChunkAt(chunk.getX(), chunk.getZ());

        //Check if this chunk is claimed
        if ( !legendsChunk.isClaimed() ) {
            return;
        }

        LegendsPlayer player = Legends.instance.wrapPlayer(event.getPlayer());

        LegendsSettlement settlement = legendsChunk.getSettlement();

        //Do nothing if the player is OP
        if ( !player.getHandle().isOp() ) {
            if ( !Legends.instance.hasPermission(player.getHandle(), "legends.admin.build") ) {

                if ( legendsChunk.isFactionOwned() ) {
                    //Check if player is part of owning faction

                    if ( player.hasFaction() ) {

                        if ( legendsChunk.getFactionId() == player.getFaction().getFactionId() ) {

                            if ( legendsChunk.build ) {
                                //This chunk is public
                                return;
                            } else if(player.getFaction().getProperty("build")) {
                                //Faction building is public
                                return;
                            } else if ( settlement.getProperty("build") ) {
                                //Settlement is public so player can build
                                return;
                            } else {
                                //Check if this player is a faction builder
                                FactionPlayer factionPlayer = player.getFactionPlayer();
                                if ( factionPlayer.getProperty("build") ) {
                                    return;
                                } else if ( factionPlayer.getRole().canDo("build") ) {
                                    return;
                                }
                            }
                        }
                    }


                    //Check if this player has been added as a settler
                    if ( settlement.isBuilder(player.getName()) ) {
                        return;
                    }

                    event.setCancelled(true);
                    player.error("You don't have permission to build here!");
                    return;

                } else {

                    //Check if player is a builder
                    if ( !settlement.isBuilder(player.getName()) ) {
                        event.setCancelled(true);
                        player.error("You don't have permission to build here!");
                        return;
                    }

                }

            }
        } 


        
    }

    @Override
    public void onBlockFromTo(BlockFromToEvent event) {
        
    }

    /*
    @Override
    public void onBlockDamage(BlockDamageEvent event) {

        if ( event.isCancelled() ) {
            return;
        }

        Chunk chunk = event.getBlock().getChunk();
        LegendsChunk legendsChunk = Legends.instance.map.getChunkAt(chunk.getX(), chunk.getZ());

        //Check if this chunk is claimed
        if ( !legendsChunk.isClaimed() ) {
            return;
        }

        LegendsPlayer player = Legends.instance.wrapPlayer(event.getPlayer());

        if ( legendsChunk.isFactionOwned() ) {
            //Check if player is part of owning faction

            if ( !player.hasFaction() ) {
                event.setCancelled(true);
                player.error("You don't have permission to build here!");
                return;
            }


            if ( !legendsChunk.getFaction().equals(player.getFaction().getName()) ) {
                event.setCancelled(true);
                player.error("You don't have permission to build here!");
                return;
            }

        } else {

            //Check if player is owner
            if ( !legendsChunk.getOwner().equals(player.getName()) ) {
                event.setCancelled(true);
                player.error("You don't have permission to build here!");
                return;
            }

        }

    }*/

}
