/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends;


import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.block.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.event.Event.Result;


import com.coryleach.legends.factions.*;

/**
 *
 * @author cory
 */
public class LegendsPlayerListener extends PlayerListener {

    protected Legends plugin;

    public LegendsPlayerListener(Legends plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {

        String[] split = event.getMessage().split(" ");

        if ( plugin.handleCommand(event.getPlayer(), split, event.getMessage()) ) {
            event.setCancelled(true);
        }

    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        LegendsPlayer player = plugin.wrapPlayer(event.getPlayer());
        player.info("Welcome to Legends!");

        if ( player.getHealth() == 0 ) {
            player.setHealthToMax();
        }

        //Print faction motd if there is one
        if ( player.hasFaction() ) {
            player.message(player.getFaction().getMotd());
            player.updatePrefix();
        } else {
            Legends.instance.setPlayerPrefix(event.getPlayer(),null);
        }

        player.message(player.getCurrentLegendsChunk().getFormattedEnterMessage());

        if ( !player.isNewPlayer() ) {
            return;
        }

        //Print welcome and instruction message to new players
        //player.info("Legends is a SMP server with RPG elements added.");
        //player.info("To get started, you need to select your starting class");
        //player.info("Please type /class to get started.");

    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {

        LegendsPlayer player = plugin.wrapPlayer(event.getPlayer());
        plugin.removePlayer(player);

        //Cancel any faction session that was in progress
        FactionSession session = FactionsManager.instance.factionSessionForPlayer(player);
        if ( session != null ) {
            session.cancelSession();
        }
        
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        LegendsPlayer player = plugin.wrapPlayer(event.getPlayer());
        player.getHandle();
                        //plugin.log.info("Set health to max");

        player.died();

        //Check faction respawn point
        if ( player.hasFaction() ) {

            if (player.getFaction().getSpawn() != null ) {
                event.setRespawnLocation(player.getLegendsFaction().getSpawn());
            }

        }

    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {

       LegendsPlayer player = plugin.wrapPlayer(event.getPlayer());

       Location from = event.getFrom();
       Location to = event.getTo();

       Chunk fromChunk = from.getWorld().getChunkAt(from);
       Chunk toChunk = to.getWorld().getChunkAt(to);

       LegendsChunk fromLegendsChunk = plugin.getMapForWorld(from.getWorld()).getChunk(fromChunk);
       LegendsChunk toLegendsChunk = plugin.getMapForWorld(to.getWorld()).getChunk(toChunk);

       this.playerChunkEnterMessage(player, fromLegendsChunk, toLegendsChunk);

    }

    public void playerChunkEnterMessage(LegendsPlayer player, LegendsChunk fromChunk, LegendsChunk toChunk) {

        if ( player.getLegendsSession().currentSettlement != toChunk.getSettlementId() ) {

            player.getLegendsSession().currentSettlement = toChunk.getSettlementId();
            player.message(toChunk.getFormattedEnterMessage());
            return;
            
        }

       //Do nothing if chunks are equal
       if ( fromChunk == toChunk ) {
           return;
       }

       //Do nothing if neither chunk is claimed
       if ( !fromChunk.isClaimed() && !toChunk.isClaimed() ) {
           return;
       }

       //Moved from claimed chunk to non-claimed chunk
       if ( fromChunk.isClaimed() && !toChunk.isClaimed() ) {
           //Notify player they are entering wilderness
           player.message(toChunk.getFormattedEnterMessage());
           return;
       }

       //Move from non-claimed chunk to claimed chunk
       if ( !fromChunk.isClaimed() && toChunk.isClaimed() ) {
           player.getLegendsSession().currentSettlement = toChunk.getSettlementId();
           player.message(toChunk.getFormattedEnterMessage());
           return;
       }

       //Check if chunks have the same settlement
       if ( fromChunk.sameSettlement(toChunk) ) {
           return;
       }

       //Send Chunk Enter Message to Player
       player.getLegendsSession().currentSettlement = toChunk.getSettlementId();
       player.message(toChunk.getFormattedEnterMessage());

    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {

        if ( event.isCancelled() ) {
            return;
        }

        Location loc = event.getTo();
        Location fromLoc = event.getFrom();

        if ( loc == null || loc.getWorld() == null ) {
            return;
        }

        Chunk chunk = loc.getWorld().getChunkAt(loc);
        Chunk fromChunkLoc = loc.getWorld().getChunkAt(fromLoc);

        if ( chunk == null ) {
            return;
        }

        LegendsChunk toChunk = plugin.getMapForWorld(chunk.getWorld()).getChunk(chunk);
        LegendsChunk fromChunk = plugin.getMapForWorld(fromChunkLoc.getWorld()).getChunk(fromChunkLoc);
        LegendsPlayer player = plugin.wrapPlayer(event.getPlayer());

        try {
           this.playerChunkEnterMessage(player, fromChunk, toChunk);
        } catch (Exception e) {
            Legends.instance.log.severe("Failed to send chunk enter message to player.");
            e.printStackTrace();
        }
        
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player bukkitPlayer = event.getPlayer();
        LegendsPlayer player = plugin.wrapPlayer(bukkitPlayer);           
        ItemStack itemStack = event.getItem();

        if ( itemStack != null && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) ) {

            int healAmount = 0;

            if ( itemStack.getType() == Material.PORK ) {
                //player.message("Pork"); //+3
                healAmount = 3;
            } else if ( itemStack.getType() == Material.GRILLED_PORK ) {
                //player.message("Grilled Pork"); //+8
                healAmount = 8;
            } else if ( itemStack.getType() == Material.APPLE ) {
                //player.message("Apple"); //+4
                healAmount = 4;
            } else if ( itemStack.getType() == Material.GOLDEN_APPLE ) {
                //player.message("Golden Apple"); //To Max Health
                healAmount = player.getMaxHealth();
            } else if ( itemStack.getType() == Material.RAW_FISH ) {
                //player.message("Raw Fish"); //+2
                healAmount = 2;
            } else if ( itemStack.getType() == Material.COOKED_FISH ) {
                //player.message("CookedFish");// +5
                healAmount = 5;
            } else if ( itemStack.getType() == Material.COOKIE ) {
                //player.message("Cookie"); //+1
                healAmount = 1;
            } else if ( itemStack.getType() == Material.BREAD ) {
                //player.message("Bread"); //+5
                healAmount = 5;
            } else if ( itemStack.getType() == Material.MUSHROOM_SOUP ) {
                //player.message("Mushroom Soup"); //+10
                healAmount = 10;
            }

            //If we're damaged and healing then do heal event
            if ( player.getHealth() != player.getMaxHealth() && healAmount != 0 ) {

                player.notifyHealDamage(healAmount);                

                return;

            }

        }

        if ( event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR ) {
            return;
        }

        Block block = event.getClickedBlock();

        if ( block == null ) {
            return;
        }

        if ( block.getType() == Material.CAKE_BLOCK ) {
                //player.message("Cake not implemented! Eating cake will cause invalid health display!"); // + 3
                //Byte data = block.getData();
                //player.message("Block data: " + data.toString());
                //event.setCancelled(true);
                return;
        } else if (block.getType() == Material.CHEST) {

            //If we're op or admin we can do whatever we want so just return
            if ( bukkitPlayer.isOp() ) {
                return;
            } else if ( Legends.instance.hasPermission(bukkitPlayer, "legends.admin.interact") ) {
                return;
            }

            //If this is a chest we need to check if we can use it
            LegendsChunk chunk = plugin.getMapForWorld(block.getWorld()).getChunk(block.getChunk());

            //If chunk is claimed let's do a check
            if ( chunk.isFactionOwned() ) {

                //Deny if player doesn't have a faction
                if ( !player.hasFaction() ) {
                    player.error("You don't have permission to use that!");
                    event.setCancelled(true);
                    return;
                }

                //Test if factions are the same
                LegendsFaction faction = FactionsManager.instance.getFactionWithId(chunk.getFactionId());

                if ( faction == null ) {
                    Legends.instance.log.severe("Unable to get faction for chunk with faction!");
                    event.setCancelled(true);
                    return;
                }

                //If factions are not equal then deny permission
                if ( faction.getFactionId() != player.getFaction().getFactionId() ) {
                    player.error("You don't have permission to use that!");
                    event.setCancelled(true);
                    return;
                }

            } else if ( chunk.isClaimed() ) {

                //Check if player is a builder
                LegendsSettlement settlement = chunk.getSettlement();

                if ( !settlement.isBuilder(player.getName()) ) {
                    event.setCancelled(true);
                    player.error("You don't have permission to use that!");
                    return;
                }

            }


        }

        

    }

    
}
