package com.coryleach.legends;

import com.coryleach.legends.classes.LegendsClass;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.*;

import java.util.*;
import com.coryleach.legends.factions.*;
import com.coryleach.legends.*;

public class LegendsEntityListener extends EntityListener {
	private final Legends plugin;

        static boolean friendlyFire = false;
        static boolean wildPvP = true;
        static boolean outsiderPvP = false;

	public LegendsEntityListener(final Legends plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onCreatureSpawn(CreatureSpawnEvent event) {

            if ( event.isCancelled() ) {
                return;
            }

            Location loc = event.getLocation();
            World world = loc.getWorld();
            Chunk chunk = world.getChunkAt(loc);

            LegendsChunk legendsChunk = Legends.instance.getMapForWorld(world).getChunk(chunk);

            if ( legendsChunk.getSettlementId() != 0 ) {

                LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(legendsChunk.getSettlementId());
                
                if ( !settlement.spawnCreatures() ) {
                    event.setCancelled(true);
                    return;
                }
                
            }

            /*LegendsCreature creature = plugin.wrapCreature((LivingEntity)event.getEntity());

            //Give the creature a random prefix
            CreaturePrefix prefix = new CreaturePrefix("Legends");
            prefix.maxHealth = 20;
            prefix.damageBonus = 1;
            prefix.armorBonus = 1;
            prefix.damageMultiplier = 1.5f;
            creature.setPrefix(prefix);*/

	}

	@Override
	public void onEntityDeath(EntityDeathEvent event) {
           
           if ( event.getEntity() instanceof Player ) {
               //Not sure if player death is listened for here but just in case
               return;
           }

           if ( event.getEntity() instanceof LivingEntity ) {

              LegendsCreature creature = plugin.wrapCreature((LivingEntity)event.getEntity());

              if ( creature.dungeonSubzone != null ) {
                creature.dungeonSubzone.spawnedMobs.remove(creature);
              }

              if ( creature.getPrefix() != null ) {

                  List<ItemStack> drops = event.getDrops();

                  drops.clear();
                  drops.addAll(creature.getPrefix().drops);

              }

              plugin.removeCreature((LivingEntity)event.getEntity());
              return;
              
           }

	}

        @Override
        public void onEntityExplode(EntityExplodeEvent event) {

            Location location = event.getLocation();
            World world = location.getWorld();
            LegendsChunk chunk = plugin.getMapForWorld(world).getChunk(location);

            //Cancel enitty explosions in all claimed chunks
            if ( chunk.isClaimed() ) {
                event.setCancelled(true);
            }

        }

        public boolean shouldCancelPvP(LegendsPlayer attacker, LegendsPlayer defender) {
            //Check if one player is an outsider
            if ( !outsiderPvP ) {
                //No Outsider PvP
                //If one player doesn't have a faction cancel the attack
                if ( !attacker.hasFaction() || !defender.hasFaction() ) {
                    return true;
                } else {

                    //Do friendly fire test
                    int attackerFaction = attacker.getFaction().getFactionId();
                    int defenderFaction = defender.getFaction().getFactionId();

                    //Cancel event if no friendly fire and factions are the same
                    if ( !friendlyFire && attackerFaction == defenderFaction ) {
                        return true;
                    }

                }

            } else {

                //If both players have a faction do friendly fire test
                if ( attacker.hasFaction() && defender.hasFaction() ) {
                    int attackerFaction = attacker.getFaction().getFactionId();
                    int defenderFaction = defender.getFaction().getFactionId();

                    //Cancel event if no friendly fire and factions are the same
                    if ( !friendlyFire && attackerFaction == defenderFaction ) {
                        return true;
                    }
                }

            }

            return false;
        }

	@Override
	public void onEntityDamage(EntityDamageEvent event) {

            if ( event.isCancelled() ) {
                return;
            }

            if (!(event.getEntity() instanceof LivingEntity) ) {
                return;
            }

            final LivingEntity entity = (LivingEntity)event.getEntity();

            //Check to see if we should even apply damage
            if ( entity.getNoDamageTicks() > 0) {
                //event.setDamage(0);
                event.setCancelled(true);
                return;
            }

            LegendsMap map = plugin.getMapForWorld(entity.getWorld());
            LegendsChunk chunk = map.getChunk(entity.getLocation());

            LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(chunk.getSettlementId());

            //Check if this is a combat event
            if ( event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK ) {

                //Make sure this is a damage by entity event
                if ( !(event instanceof EntityDamageByEntityEvent) ) {
                    return;
                }

                EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent)event;

                //Do nothing if attacker is non-living entity
                if ( !(damageByEntityEvent.getDamager() instanceof LivingEntity) ) {
                    Legends.instance.log.warning("DAMAGE BY NON-LIVING ENTITY");
                    return;
                }

                LivingEntity damagedEntity = (LivingEntity)event.getEntity();
                LivingEntity attackingEntity = (LivingEntity)damageByEntityEvent.getDamager();

                if ( damagedEntity.isDead() || damagedEntity.getHealth() <= 0 ) {
                    return;
                }

                if ( damagedEntity instanceof Player && attackingEntity instanceof Player ) {

                    //PvP
                    LegendsPlayer attacker = plugin.wrapPlayer((Player)attackingEntity);
                    LegendsPlayer defender = plugin.wrapPlayer((Player)damagedEntity);
                    int damage = event.getDamage();

                    if ( settlement != null ) {

                        if ( settlement.getProperty("haven") ) {
                            event.setCancelled(true);
                            return;
                        } else if ( !settlement.getProperty("arena") ) {
                            
                            //What to do if it isn't an arena? Cancel?
                            //Can only attack player from same faction in an arena
                            //If both players are from the same faction, cancel the attack
                            if ( this.shouldCancelPvP(attacker,defender) ) {
                                event.setCancelled(true);
                                return;
                            }
                            
                        } else {
                            //This is an arena and PvP is always on in an arena
                        }

                    } else {

                        //Wilderness Rules?
                        if ( this.shouldCancelPvP(attacker,defender) ) {
                            event.setCancelled(true);
                            return;
                        }
                        
                    }



                    //Check if this is valid combat
                    //i.e. if players are not at war then cancel combatss
                    attacker.message(
                            "You hit " +
                            defender.getName() +
                            " for " +
                            ChatColor.RED +
                            Integer.toString(damage) +
                            ChatColor.WHITE +
                            " damage!");

                    defender.message(
                            attacker.getName() +
                            " hits you for " +
                            ChatColor.RED +
                            Integer.toString(damage) +
                            ChatColor.WHITE +
                            " damage!");
                    
                } else if ( damagedEntity instanceof Player ) {

                    //NPC attacking Player
                    LegendsPlayer damagedPlayer = plugin.wrapPlayer((Player)damagedEntity);
                    LegendsCreature attacker = plugin.wrapCreature(attackingEntity);

                    if ( settlement != null ) {

                        //Cancel attack if player is in a haven
                        if ( settlement.getProperty("haven") ) {
                            event.setCancelled(true);
                            return;
                        }

                    }

                    //Just return if player has no class
                    LegendsClass legendsClass = damagedPlayer.getLegendsClass();

                    //Cant do anything if player has no class
                    if ( legendsClass != null ) {
                        damagedPlayer.getLegendsClass().defend(attacker, damageByEntityEvent);
                    } else {
                        LegendsClass.NoClassDefend(damagedPlayer, attacker, damageByEntityEvent);
                    }

                } else if ( attackingEntity instanceof Player ) {

                    //Player attacking NPC
                    LegendsPlayer player = plugin.wrapPlayer((Player)attackingEntity);
                    LegendsCreature defender = plugin.wrapCreature(damagedEntity);

                    if ( defender.getHealth() <= 0 || defender.getHandle().isDead() ) {
                        return;
                    }

                    LegendsClass legendsClass = player.getLegendsClass();

                    //Use class attack function, else default to no class attack static function
                    if ( legendsClass != null ) {
                        player.getLegendsClass().attack(defender, damageByEntityEvent);
                    } else {
                        LegendsClass.NoClassAttack(player, defender, damageByEntityEvent);
                    }

                    //get damage after attack function modification
                    int damage = damageByEntityEvent.getDamage();

                    //Test if the defender is going to die
                    if ( defender.getHealth() <= damage && !defender.killClaimed() ) {

                        //Claim the kill and gain exp if eligible
                        defender.claimKill();
                        player.info("You killed " + defender.getName());

                        if ( settlement != null ) {

                            //No Exp Gain if we're in a Settlement that is a haven
                            if ( settlement.getProperty("haven") ) {
                                //Also cancel any reward the player might get for killing
                                return;
                            }

                        }

                        /*
                        if ( defender.getPrefix() != null ) {
                            
                            //Add Experience to player or player party for kill
                            //TODO: Add Experience to party
                            int experience = defender.getPrefix().experience;
                            LegendsClass playerClass = player.getLegendsClass();

                            if ( playerClass == null ) {
                                return;
                            }

                            playerClass.addExperience(experience);
                            player.info(
                                    "You gain " +
                                    ChatColor.AQUA +
                                    Integer.toString(experience) +
                                    ChatColor.WHITE + 
                                    " experience!");

                            if ( playerClass.canLevel() ) {
                                player.info("Level up!");
                                playerClass.level();
                                return;
                            }

                        }*/

                    }

                } else {

                    //Neither Entity is a Player, ignore...

                }
                

            } else {

                //Non-Combat Damage Event
                if ( event.getEntity() instanceof Player ) {

                    LegendsPlayer player = plugin.wrapPlayer((Player)event.getEntity());

                    if ( player.getHandle().isDead() ) {
                        return;
                    }

                    if ( settlement != null ) {

                        //Cancel attack if player is in a haven
                        if ( settlement.getProperty("haven") ) {
                            event.setCancelled(true);
                            return;
                        }

                    }

                    //LegendsClass legendsClass = player.getLegendsClass();

                    int damage = event.getDamage();

                    String cause = " from unknown source";

                    switch(event.getCause()) {
                        case BLOCK_EXPLOSION:
                            cause = " from a Block Explosion";
                            break;
                        case CONTACT:
                            cause = " from Contact";
                            break;
                        case CUSTOM:
                            cause = " from Custom source?";
                            break;
                        case DROWNING:
                            cause = " from Drowning";
                            break;
                        case ENTITY_ATTACK:
                            cause = " from an Attack";
                            break;
                        case ENTITY_EXPLOSION:
                            cause = " from an Explosion";
                            damage = Math.round(damage / 2.0f);
                            event.setDamage(damage);
                            break;
                        case FALL:
                            cause = " from Falling";
                            break;
                        case FIRE:
                            cause = " from Fire";
                            break;
                        case FIRE_TICK:
                            cause = " from Fire.";
                            break;
                        case LAVA:
                            cause = " from Lava";
                            break;
                        case VOID:
                            cause = " from (Void)";
                            break;
                    }

                    player.message(
                        "You take " +
                        ChatColor.RED +
                        Integer.toString(damage) +
                        ChatColor.WHITE +
                        " damage" + cause
                        );

                }

            }

	}




}
