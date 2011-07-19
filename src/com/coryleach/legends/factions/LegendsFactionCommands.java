/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.factions;

import com.coryleach.legends.*;
import com.coryleach.legends.factions.SettlementManager;
import com.coryleach.legends.factions.FactionsManager;
import org.bukkit.*;
import com.sk89q.minecraft.util.commands.*;
import com.coryleach.util.TextUtil;

/**
 *
 * @author Cory
 */
public class LegendsFactionCommands {

        static boolean limitClaims = false;
        static boolean limitSettles = false;

        @Command(
            aliases = {"factioncreate", "fcreate"},
            usage = "<faction name> <faction prefix>",
            flags = "",
            desc = "Create a new faction.",
            min = 2,
            max = 2
        )
        @CommandPermissions({"legends.faction.create"})
        public static void create(CommandContext args, Legends plugin, LegendsPlayer player) {

            String name = args.getString(0);
            String prefix = args.getString(1);
            //LegendsFaction faction = new LegendsFaction(name,player.getName());

            if ( !FactionsManager.validateName(name) ) {
                player.error("Faction name must contain only letters and be between 4-20 characters.");
                return;
            }

            if ( FactionsManager.instance.factionExists(name) ) {
                player.info("Faction with that name already exists!");
                return;
            }

            if ( !FactionsManager.validatePrefix(prefix) ) {
                player.error("Prefix must be between 2-6 characters and contain only letters and numbers.");
                return;
            }

            if ( FactionsManager.instance.factionPrefixExists(prefix) ) {

                player.info("Faction with that prefix already exists!");
                return;

            }

            FactionSession session = new FactionSession("create",player.getName());
            session.faction = new LegendsFaction(name,prefix,player);
            player.setFaction(session.faction);
           
            FactionsManager.instance.setFactionSessionForPlayer(player, session);
            String playersNeeded = Integer.toString(FactionsManager.playersNeededToStartFaction - 1);
            player.info("You MUST /finvite " + playersNeeded + " other player(s) to finish creating this faction.");

        }

        @Command(
            aliases = {"factionjoin", "fjoin"},
            usage = "<faction name>",
            flags = "",
            desc = "Join a public faction.",
            min = 0,
            max = 1
        )
        @CommandPermissions({"legends.faction.join"})
        public static void join(CommandContext args, Legends plugin, LegendsPlayer player) {

        }

        @Command(
            aliases = {"factioninvite", "finvite"},
            usage = "<player name>",
            flags = "",
            desc = "Invite a player to join your faction.",
            min = 0,
            max = 1
        )
        @CommandPermissions({"legends.faction.invite"})
        public static void invite(CommandContext args, Legends plugin, LegendsPlayer player) {

            FactionSession session = FactionsManager.instance.factionSessionForPlayer(player);

            if ( args.argsLength() == 0 ) {
                //Check on status of invite
                if ( session != null && session.getAction().equals("invite") ) {
                    player.info("You have a pending invite from " + session.faction.getName() + " type /faccept or /freject");
                } else {
                    player.info("/finvite <player name>");
                }
                return;
            }

            String inviteName = args.getString(0);
            LegendsPlayer invitee = Legends.instance.getPlayer(inviteName);

            //Check player exists
            if ( invitee == null ) {
                player.error("No player with that name is currently online.");
                return;
            }

            //Does invitee already have a faction or pending invite?
            FactionSession inviteeSession = FactionsManager.instance.factionSessionForPlayer(invitee);
            if ( inviteeSession != null ) {
                player.info(invitee.getName() + " already has a pending faction invite.");
                return;
            } else if ( invitee.hasFaction() ) {
                player.info(invitee.getName() + " already has a faction.");
                return;
            }

            //First check to see if we have a faction and we have invite privileges
            if ( session == null ) {

                LegendsFaction faction = player.getFaction();
                
                if ( faction == null ) {
                    player.error("You have no faction to invite anyone to! Try /fcreate");
                    return;
                } else {

                    //Check if player has invite permissions
                    FactionPlayer factionPlayer = player.getFactionPlayer();
                    if ( factionPlayer == null ) {
                        player.error("Failed to find player entry in faction member list.");
                        return;
                    }

                    if ( !factionPlayer.getRole().canDo("invite") ) {
                        if ( !factionPlayer.getProperty("invite") ) {
                            player.error("You do not have permission to invite players to this faction!");
                            return;
                        }
                    }

                    //Send Invite
                    inviteeSession = new FactionSession("invite",invitee.getName());
                    inviteeSession.faction = faction;
                    inviteeSession.inviteSender = player.getName();

                    //Notify Invitee of the pending invite
                    player.message("Invite sent to " + invitee.getName());

                    invitee.message(ChatColor.AQUA +
                        player.getName() +
                        " has invited you to join " +
                        faction.getName() +
                        " type " +
                        ChatColor.RED +
                        "/faccept" +
                        ChatColor.AQUA +
                        "or /freject");
                    
                    //Set session
                    FactionsManager.instance.setFactionSessionForPlayer(invitee, inviteeSession);

                }
                
            } else if(session != null) {
                
                //This is a creation invite event
                if ( !session.getAction().equals("create") ) {
                    player.error("You have no faction to invite anyone to! Try /fcreate");
                    return;
                }

                //We can now invite the player
                //Set Session for
                inviteeSession = session.createInviteSession(inviteName);
                FactionsManager.instance.setFactionSessionForPlayer(invitee, inviteeSession);

                invitee.message(ChatColor.AQUA +
                        player.getName() +
                        " has invited you to join " +
                        session.faction.getName() +
                        " type " +
                        ChatColor.RED +
                        "/faccept" +
                        ChatColor.AQUA +
                        "or /freject");

                player.message("Invite sent to " + invitee.getName());

            }

            player.updatePrefix();

        }

        @Command(
            aliases = {"factionaccept", "faccept","fyes"},
            usage = "",
            flags = "",
            desc = "Accept invitation to join faction",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.faction.accept"})
        public static void accept(CommandContext args, Legends plugin, LegendsPlayer player) {

            FactionSession session = FactionsManager.instance.factionSessionForPlayer(player);

            if ( session == null || !session.getAction().equals("invite") ) {
                player.info("You have no invite to accept!");
                return;
            }

            if ( session.parentSession == null ) {

                LegendsPlayer inviter = Legends.instance.getPlayer(session.inviteSender);
                inviter.message(player.getName() + " has accepted your invitation");
                player.message("You accept " + inviter.getName() + "'s invitation.");

                //Join pre-existing faction
                session.faction.addMember(player);
                player.message(player.getFaction().getMotd());
                session.faction.broadcast(player.getName() + " has joined " + session.faction.getName() + "!");
                FactionsManager.instance.setFactionSessionForPlayer(player, null);
                player.updatePrefix();

                return;

            } else {

                //Join a faction being created
                session.faction.addMember(player);

                LegendsPlayer inviter = Legends.instance.getPlayer(session.parentSession.player);
                inviter.message(player.getName() + " has accepted your invitation");

                int playersNeeded = FactionsManager.playersNeededToStartFaction;

                if ( session.faction.population() >= playersNeeded ) {

                    //Create Faction
                    FactionsManager.instance.addFaction(session.faction);
                    Legends.instance.server.broadcastMessage(
                            inviter.getName() +
                            " has founded new faction " +
                            session.faction.getDisplayName() +
                            "!");

                    session.faction.broadcast(session.faction.getMotd());
                    session.faction.updatePlayers();
                    
                    session.parentSession.dissovleAsParent();
                    FactionsManager.instance.setFactionSessionForPlayer(inviter, null);
                    FactionsManager.instance.setFactionSessionForPlayer(player, null);

                } else {

                    session.faction.broadcast(player.getName() + " has joined " + session.faction.getName() + "!");

                    String remainingPlayersNeeded = "" +
                            Integer.toString(playersNeeded - session.faction.population()) +
                            " more player(s) needed to finish creating faction.";

                    session.faction.broadcast(remainingPlayersNeeded);

                    //Remove this session
                    FactionsManager.instance.setFactionSessionForPlayer(player, null);

                }

            }

            player.updatePrefix();


        }

        @Command(
            aliases = {"factionreject", "freject","fno","fdecline"},
            usage = "",
            flags = "",
            desc = "Reject invitation to join faction",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.faction.reject"})
        public static void reject(CommandContext args, Legends plugin, LegendsPlayer player) {

            FactionSession session = FactionsManager.instance.factionSessionForPlayer(player);

            if ( session == null ) {
                player.info("You have no invite to reject!");
            }

            if ( session.parentSession != null ) {
                //This is an invite to create a faction

                //Notify inviter that invite was rejected
                LegendsPlayer inviter = Legends.instance.getPlayer(session.parentSession.player);
                inviter.message(player.getName() + " has rejected your invitation.");
                player.message("You reject " + inviter.getName() + "'s invitation.");

                //Remove this session from the parent session
                session.parentSession.childSessions.remove(session);

                //Remove the session
                FactionsManager.instance.setFactionSessionForPlayer(player, null);

            } else {
                //This is an invite to an existing faction
                //TODO: Notify player who did the inviting that their invite has been rejected

                if ( session.inviteSender != null ) {
                    LegendsPlayer inviter = Legends.instance.getPlayer(session.inviteSender);
                    if ( inviter != null ) {
                        inviter.message(player.getName() + " declined your invite.");
                    }
                }

                player.message("You decline the invite.");

                FactionsManager.instance.setFactionSessionForPlayer(player, null);

            }

        }

        @Command(
            aliases = {"factionclaim", "fclaim","fc"},
            usage = "<settlement name>",
            flags = "",
            desc = "Claim a chunk of land adjacent to a faction settlement and adds it to that settlement.",
            min = 0,
            max = 1
        )
        @CommandPermissions({"legends.faction.claim"})
        public static void claim(CommandContext args, Legends plugin, LegendsPlayer player) {

            String settlementName = null;

            //Get Settlement name if it exists
            if ( args.argsLength() == 1 ) {
                settlementName = args.getString(0);
            }

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            if ( !player.getFactionPlayer().getRole().canDo("claim") ) {
                if ( !player.getFactionPlayer().getProperty("claim") ) {
                    player.error("You don't have permission to do that!");
                    return;
                }
            }

            //Check to see if the chunk has already been claimed
            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( chunk.isClaimed() ) {
                player.error("This chunk has already been claimed!");
                return;
            }

            if ( limitClaims ) {
                //Do we have enough allocation points?
                if ( player.getFaction().getClaimPoints() <= 0 ) {
                    player.error("Your faction doesn't have any claim points!");
                    return;
                }
            }

            //Claim the current chunk
            //SettlementManager.get
            LegendsSettlement settlement = null;

            if ( settlementName != null ) {

                //Search for settlement with this name
                settlement = SettlementManager.instance.getSettlementWithName(settlementName);

                if ( settlement == null ) {
                    player.error("Unable to find settlement named " + settlementName);
                    return;
                }

                
                if ( !plugin.hasPermission(player.getHandle(), "legends.admin.anyclaim") ) {

                    //Check for adjacency
                    if ( !settlement.isAdjacent(chunk) ) {
                        player.error("You can only claim chunks adjacent to an existing settlement!");
                        return;
                    }

                }

            } else {

                //Search for nearby settlement
                settlement = player.getFaction().getAdjacentSettlement(chunk);
                
                if ( settlement == null ) {
                    player.error("You can only claim chunks adjacent to an existing settlement!");
                    return;
                }

            }

            settlement.addChunk(chunk);
            player.getFaction().chunkWasClaimed();
            chunk.broadcast("This chunk has been claimed by " + player.getFaction().getDisplayName() + "!");
            chunk.broadcast(chunk.getFormattedEnterMessage());

        }

        @Command(
            aliases = {"factionabandon", "fabandon","fab"},
            usage = "",
            flags = "",
            desc = "Abandon the chunk of faction land you are currently standing on.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.faction.abandon"})
        public static void abandon(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            if ( !player.getFactionPlayer().getRole().canDo("abandon") ) {
                if ( !player.getFactionPlayer().getProperty("abandon") ) {
                    player.error("You don't have permission to do that!");
                    return;
                }
            }

            //Check to see if the chunk has already been claimed
            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( !chunk.isFactionOwned() ) {
                player.error("You don't own this chunk!");
                return;
            }

            if ( chunk.getFactionId() == 0 && chunk.getSettlementId() != 0 ) {
                player.error("You cannot use this command to abandnon this chunk. Use /uabandon");
                return;
            }

            if ( chunk.getFactionId() != player.getFaction().getFactionId() ) {
                player.error("You don't own this chunk!");
                return;
            }

            //Get settlement
            LegendsSettlement settlement = SettlementManager.instance.getSettlementForId(chunk.getSettlementId());

            if ( settlement == null ) {
                player.error("This chunk doesn't belong to a settlement.");
                return;
            }

            //Abandon last chunk
            if ( settlement.size() == 1 ) {

                Legends.instance.server.broadcastMessage("Settlement " + settlement.getName() + " has been dissolved.");
                SettlementManager.instance.removeSettlement(settlement);
                FactionsManager.instance.getFactionWithId(settlement.getFactionId()).removeSettlementId(settlement.getSettlementId());

            }

            //Abandon the current chunk
            settlement.removeChunk(chunk);
            chunk.setFactionId(0);
            chunk.setSettlementId(0);

            //Tell everyone what happened
            chunk.broadcast("This chunk has been abandoned by " + player.getFaction().getDisplayName() + "!");
            chunk.broadcast(chunk.getFormattedEnterMessage());

        }

        @Command(
            aliases = {"factioncalimsettlement", "fclaimsettlement","fsettle"},
            usage = "<name>",
            flags = "",
            desc = "Claim the chunk of land you're standing on, and begin a new faction settlement.",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.faction.settle"})
        public static void claimSettlement(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            String name = args.getString(0);

            if ( !SettlementManager.validateName(name) ) {
                player.error("Name must be 4-20 characters, letters and numbers only.");
                return;
            }

            //Check if name is in use already
            if ( SettlementManager.getInstance().settlementExistsWithName(name) ) {
                player.error("Settlement with that name already exists");
                return;
            }

            Location loc = player.getHandle().getLocation();
            if ( !loc.getWorld().getName().equals("world") ) {
                player.error("Settlements cannot be claimed in this world.");
                return;
            }

            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( chunk.isClaimed() ) {
                player.error("This chunk has already been claimed!");
                return;
            }

            LegendsFaction faction = player.getFaction();

            if ( limitSettles ) {

                if ( faction.getSettlementPoints() <= 0 ) {
                    player.error("Your faction doesn't have enough settlement points to do that!");
                    return;
                }

            }

            FactionPlayer fplayer = player.getFactionPlayer();

            if ( !fplayer.getRole().canDo("claim") ) {
                if ( !player.getFactionPlayer().getProperty("settle") ) {
                    player.error("You don't have permission to do that!");
                    return;
                }
            }

            World world = chunk.getChunk().getWorld();

            //Claim the settlement
            LegendsSettlement settlement = new LegendsSettlement(faction.getFactionId(),name,world.getName());
            settlement.addChunk(chunk);

            //Add to settlement manager
            SettlementManager.getInstance().addSettlement(settlement);
            faction.addSettlementId(settlement.getSettlementId());
            faction.settlementWasClaimed();
            
            Legends.instance.server.broadcastMessage(
                    faction.getColor() +
                    faction.getName() +
                    ChatColor.WHITE + 
                    " has founded the new settlement " +
                    faction.getColor() + 
                    settlement.getName() +
                    ChatColor.WHITE + 
                    "!");

            chunk.broadcast(chunk.getFormattedEnterMessage());

        }

        @Command(
            aliases = {"faddsettler","fasettler"},
            usage = "<name>",
            flags = "",
            desc = "Add a user to a factions settlement",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.faction.addSettler"})
        public static void addSettler(CommandContext args, Legends plugin, LegendsPlayer player) {

            String name = args.getString(0);
            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( !chunk.isClaimed() ) {
                player.error("This command must be used inside of a settlement");
                return;
            }

            if ( !player.hasFaction() ) {
                player.error("You must have a faction to use this command.");
                return;
            }

            if ( !player.getFactionPlayer().getRole().canDo("Elder") ) {
                player.error("You must be an elder to set builders.");
                return;
            }

            LegendsSettlement settlement = chunk.getSettlement();

            plugin.log.info("Getting Faction ID");

            if ( settlement.getFactionId() != player.getFaction().getFactionId() ) {
                player.error("Settlement is not part of your faction!");
                return;
            }

            plugin.log.info("Adding builder");
            settlement.addBuilder(name);

            plugin.log.info("Sending text to player");
            player.info("Added settler to current settlement.");

        }

        @Command(
            aliases = {"fremovesettler", "frsettler"},
            usage = "<name>",
            flags = "",
            desc = "Remove a settler from a user faction",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.faction.removeSettler"})
        public static void removeSettler(CommandContext args, Legends plugin, LegendsPlayer player) {

            String name = args.getString(0);
            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( !chunk.isClaimed() ) {
                player.error("This command must be used inside of a settlement");
                return;
            }

            if ( !player.hasFaction() ) {
                player.error("You must have a faction to use this command.");
                return;
            }

            if ( !player.getFactionPlayer().getRole().canDo("Elder") ) {
                player.error("You must be an elder to set builders.");
                return;
            }

            LegendsSettlement settlement = chunk.getSettlement();

            if ( settlement.getFactionId() != player.getFaction().getFactionId() ) {
                player.error("Settlement is not part of your faction!");
                return;
            }

            if ( !settlement.isBuilder(name) ) {
                player.error("Player is not a settler");
                return;
            }

            settlement.removeBuilder(name);

            player.info("Player is no longer a faction builder.");

        }

        /*
        @Command(
            aliases = {"fsetbuilder", "fpromotebuilder","faddbuilder"},
            usage = "<name>",
            flags = "",
            desc = "Promote a user to faction builder",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.faction.addBuilder"})
        public static void addBuilder(CommandContext args, Legends plugin, LegendsPlayer player) {

            String name = args.getString(0);

            if ( !player.hasFaction() ) {
                player.error("You must have a faction to use this command.");
                return;
            }

            if ( player.getFactionPlayer().getRole().value < FactionRole.ELDER.value ) {
                player.error("You must be an elder to set builders.");
                return;
            }

            FactionPlayer factionPlayer = player.getFaction().getFactionPlayerWithName(name);

            if ( factionPlayer == null ) {
                player.error("User could not be found in that faction.");
                return;
            }

            factionPlayer.setBuilder(true);
            player.info("Player set as a faction builder.");

        }

        @Command(
            aliases = {"fremovebuilder", "fdemotebuilder","funsetbuilder"},
            usage = "<name>",
            flags = "",
            desc = "Demote a user from faction builder",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.faction.removeBuilder"})
        public static void removeBuilder(CommandContext args, Legends plugin, LegendsPlayer player) {

            String name = args.getString(0);
            
            if ( !player.hasFaction() ) {
                player.error("You must have a faction to use this command.");
                return;
            }

            if ( player.getFactionPlayer().getRole().value < FactionRole.ELDER.value ) {
                player.error("You must be an elder to set builders.");
                return;
            }

            FactionPlayer factionPlayer = player.getFaction().getFactionPlayerWithName(name);

            if ( factionPlayer == null ) {
                player.error("User could not be found in that faction.");
                return;
            }

            factionPlayer.setBuilder(false);
            player.info("Player is no longer a faction builder.");

        }*/

        @Command(
            aliases = {"frenamesettlement", "fsrename","fsettlerename"},
            usage = "<name>",
            flags = "",
            desc = "rename a settlement",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.faction.settlementrename"})
        public static void renameSettlement(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            String name = args.getString(0);

            if ( !SettlementManager.validateName(name) ) {
                player.error("Name must be 4-20 characters, letters and numbers only.");
                return;
            }

            //Check if name is in use already
            if ( SettlementManager.getInstance().settlementExistsWithName(name) ) {
                player.error("Settlement with that name already exists");
                return;
            }

            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( chunk.getFactionId() != player.getFaction().getFactionId() ) {
                player.error("This chunk is not owned by your faction");
                return;
            }

            LegendsFaction faction = player.getFaction();
            FactionPlayer fplayer = player.getFactionPlayer();

            if ( !fplayer.getRole().canDo("claim") ) {
                player.error("You don't have permission to do that!");
                return;
            }
            
            LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(chunk.getSettlementId());
            String oldName = settlement.getName();
            settlement.setName(name);

            Legends.instance.server.broadcastMessage(
                    faction.getColor() + 
                    oldName +
                    ChatColor.WHITE + 
                    " has been renamed to " +
                    faction.getColor() +
                    settlement.getName()
                    );


        }

        @Command(
            aliases = {"factionmotd", "fmotd"},
            usage = "",
            flags = "",
            desc = "Read the faction message of the day.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.faction.motd"})
        public static void motd(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            if ( player.getFaction().motd != null ) {
                player.message(player.getFaction().getMotd());
            }

        }

        @Command(
            aliases = {"setfactionmotd", "setfmotd","fsetmotd"},
            usage = "<message>",
            flags = "",
            desc = "Set faction message of the day",
            min = 1,
            max = 100
        )
        @CommandPermissions({"legends.faction.setmotd"})
        public static void setmotd(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            FactionPlayer factionPlayer = player.getFactionPlayer();
            FactionRole role = factionPlayer.getRole();

            if ( role == FactionRole.ELDER || role == FactionRole.CHIEF_ELDER ) {
                player.getFaction().motd = args.getJoinedStrings(0);
                player.getFaction().broadcast(player.getFaction().motd);
                return;
            } else {
                player.error("You are not of sufficient rank to do that!");
                return;
            }

        }

        @Command(
            aliases = {"factioninfo", "finfo"},
            usage = "<faction name/prefix>",
            flags = "",
            desc = "Get faction info.",
            min = 0,
            max = 1
        )
        @CommandPermissions({"legends.faction.info"})
        public static void info(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( args.length() > 1 ) {

                String name = args.getString(0);

                LegendsFaction faction = FactionsManager.instance.factionWithName(name);

                if ( faction == null ) {
                    faction = FactionsManager.instance.factionWithPrefix(name);

                    if ( faction == null ) {
                        player.error("Faction with that name does not exist!");
                        return;
                    }
                    
                }

                faction.sendInfoToPlayer(player);
                return;

            }

            if ( !player.hasFaction() ) {
                player.error("You don't have a faction!");
                return;
            }

            player.getFaction().sendInfoToPlayer(player);

        }

        @Command(
            aliases = {"factionchat","fchat", "f"},
            usage = "<message>",
            flags = "",
            desc = "Chat with faction memebers.",
            min = 1,
            max = 100
        )
        @CommandPermissions({"legends.faction.chat"})
        public static void chat(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You don't have a faction!");
                return;
            }

            ChatColor color = player.getFaction().getColor();
            //Do faction chat
            player.getFaction().broadcast(
                    color +
                    //"[" + player.getFaction().getRawPrefix() + "] " +
                    player.getName() + ": " +
                    ChatColor.GRAY +
                    args.getJoinedStrings(0));
            String logString = "[" + player.getFaction().getRawPrefix() + "] " + player.getName() + ": " + args.getJoinedStrings(0);
            Legends.instance.log.info(logString);

        }

        @Command(
            aliases = {"factionlist","flist", "fwho","factionmembers"},
            usage = "<page>",
            flags = "",
            desc = "List faction memebers.",
            min = 0,
            max = 1
        )
        @CommandPermissions({"legends.faction.list"})
        public static void list(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You don't have a faction!");
                return;
            }

            player.getFaction().sendFactionListToPlayer(player);
        
        }

        @Command(
            aliases = {"factionquit","fquit"},
            usage = "",
            flags = "",
            desc = "Quit current faction.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.faction.quit"})
        public static void quit(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( player.factionManageId != 0 ) {
                return;
            }

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            LegendsFaction faction = player.getFaction();

            /*if ( player.getFactionPlayer().role == FactionRole.CHIEF_ELDER ) {
                player.info("You cannot quit this faction until you appoint a new chief elder.");
                return;
            }*/

            player.getFaction().removePlayer(player.getFactionPlayer());
            player.setFaction(null);

            faction.broadcast(player.getName() + " has quit the faction.");
            player.info("You have quit your faction.");
            player.updatePrefix();

        }

        @Command(
            aliases = {"factionkick","fkick"},
            usage = "<Player Name>",
            flags = "",
            desc = "Kick a member out of the faction.",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.faction.kick"})
        public static void kick(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You have no faction! STUPID!");
                return;
            }

            String kickedPlayerName = args.getString(0);

            LegendsFaction faction = player.getFaction();
            LegendsPlayer kickedPlayer = Legends.instance.getPlayer(kickedPlayerName);

            FactionPlayer factionKickedPlayer = null;
            FactionPlayer factionKickingPlayer = faction.getFactionPlayerWithName(player.getName());

            if ( kickedPlayer == null ) {

                //Offline Player
                factionKickedPlayer = player.getFaction().getFactionPlayerWithName(kickedPlayerName);
                
                if ( factionKickedPlayer == null ) {
                    player.error("Couldn't find a player with the name " + kickedPlayerName);
                    return;
                }

                player.getFaction().removePlayer(factionKickedPlayer);

                player.info("Player " + kickedPlayerName + " has been removed from the faction.");
                return;

            } else  {
            
                //Online Player
                if ( kickedPlayer.getFaction() != player.getFaction() ) {
                    player.error("That player is not in your faction!");
                    return;
                }
                
                kickedPlayerName = kickedPlayer.getName();
                
                factionKickedPlayer = faction.getFactionPlayerWithName(kickedPlayer.getName());

            }


            if ( !factionKickingPlayer.getRole().canDo("kick") ) {
                player.error("Your rank is not high enough to kick that player.");
                return;
            }

            if ( factionKickingPlayer.getRole().value <= factionKickedPlayer.getRole().value ) {

                //Rank is night high enough to kick that player
                player.error("Your rank is not high enough to kick that player.");
                return;

            } else {
                
                //Kick Player
                faction.removePlayer(factionKickedPlayer);
                faction.broadcast(kickedPlayer.getName() + " has been kicked out of the faction.");

                if ( kickedPlayer != null ) {
                    kickedPlayer.message("You have been kicked out of your faction.");
                    kickedPlayer.updatePrefix();
                }

                return;

            }

        }

        @Command(
            aliases = {"factionsetrole","fsetrole"},
            usage = "<Player Name> <role>",
            flags = "",
            desc = "Set the role of a member of the faction",
            min = 2,
            max = 2
        )
        @CommandPermissions({"legends.faction.kick"})
        public static void role(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            if ( player.getFactionPlayer().getRole().value < FactionRole.ELDER.value ) {
                player.error("Your rank isn't high enough!");
                return;
            }

            Legends.instance.log.info("1");
            String playerName = args.getString(0);
            String role = args.getString(1).toLowerCase();

            LegendsPlayer legendsPlayer = Legends.instance.getPlayer(playerName);
            FactionPlayer fplayer;
                        Legends.instance.log.info("2");

            if ( legendsPlayer != null ) {

                if ( !legendsPlayer.hasFaction() ) {
                    player.error("That player doesn't have a faction!");
                    return;
                }
                Legends.instance.log.info("faction player");
               fplayer = legendsPlayer.getFactionPlayer();
            } else {
                            Legends.instance.log.info("offline faction player");
               fplayer = player.getFaction().getFactionPlayerWithName(playerName);
            }

            if ( fplayer == null ) {
                player.error("There is no such player in this faction!");
                return;
            }

            if ( fplayer.getRole() == FactionRole.CHIEF_ELDER ) {
                player.error("This member's role cannot be set with this command.");
                return;
            }

            if ( role.equals("elder") ) {
                fplayer.setRole(FactionRole.ELDER);
            } else if ( role.equals("leader") ) {
                fplayer.setRole(FactionRole.LEADER);
            } else if ( role.equals("commander") ) {
                fplayer.setRole(FactionRole.COMMANDER);
            } else if ( role.equals("warchief") ) {
                fplayer.setRole(FactionRole.WARCHIEF);
            } else if ( role.equals("recruiter") ) {
                fplayer.setRole(FactionRole.RECRUITER);
            } else if ( role.equals("architect") ) {
                fplayer.setRole(FactionRole.ARCHITECT);
            } else if ( role.equals("general") ) {
                fplayer.setRole(FactionRole.GENERAL);
            } else if ( role.equals("resident") ) {
                fplayer.setRole(FactionRole.RESIDENT);
            } else {
                player.error("Role is invalid or can't be set");
                return;
            }

            player.message(fplayer.getPlayerName() + "'s role has been set to " + fplayer.getRole().toString());

            if ( fplayer.isOnline() ) {
                fplayer.getPlayer().message("Your faction role has been set to " + fplayer.getRole().toString());
            }

        }

        @Command(
            aliases = {"fresign"},
            usage = "<Player Name>",
            flags = "",
            desc = "Resigns Chief Elder role and gives it to someone else",
            min = 0,
            max = 1
        )
        @CommandPermissions({"legends.faction.kick"})
        public static void resign(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( player.factionManageId != 0 ) {
                return;
            }

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            if ( player.getFactionPlayer().role != FactionRole.CHIEF_ELDER ) {
                player.error("You are not chief elder! Use /fquit to leave the faction");
                return;
            }

            if ( player.getFaction().population() == 1 ) {

                //Last player to leave the faction
                player.getFaction().removePlayer(player.getFactionPlayer());
                player.setFaction(null);

                player.message("You quit your faction.");
                player.updatePrefix();
                
                return;

            } else if ( args.argsLength() == 0 ) {

                player.error("You must resign by specifying a player name to give the ChiefElder role to.");
                player.error("Use /flist to see a list of available player names");
                return;

            }

            String playerName = args.getString(0);

            FactionPlayer resignToPlayer = player.getFaction().getFactionPlayerWithName(playerName);

            if ( resignToPlayer == null ) {
                player.error("Player not found.");
                return;
            }

            //Give up ChiefElder and become normal elder
            resignToPlayer.setRole(FactionRole.CHIEF_ELDER);
            player.getFactionPlayer().setRole(FactionRole.ELDER);

            player.getFaction().broadcast(resignToPlayer.getPlayerName() + " is now ChiefElder.");
            player.info("You have been downgraded to Elder. You may now use /fquit to leave the faction.");

        }

        @Command(
            aliases = {"fperm"},
            usage = "<username> <property> <value>",
            flags = "",
            desc = "Sets certain faction properties",
            min = 0,
            max = 3
        )
        @CommandPermissions({"legends.faction.fperm"})
        public static void factionPermission(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( args.argsLength() < 3 ) {
                player.message(TextUtil.titleize("Faction Permissions"));
                player.info("/fperm <username> <property> <value>");
                player.info("Faction Permissions: { invite | build | claim | settle }");
                player.info("value = { true | false }");
                return;
            }

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            if ( !player.getFactionPlayer().getRole().canDo("permissions") ) {
                player.error("Only faction elders can use this command.");
                return;
            }

            String name = args.getString(0);
            String property = args.getString(1).toLowerCase();
            String stringValue = args.getString(2);

            FactionPlayer targetFactionPlayer = null;
            LegendsPlayer target = plugin.getPlayer(name);

            int factionId = player.getFaction().getFactionId();

            if ( target != null ) {

                name = target.getName();

                if ( !target.hasFaction() || (target.getFaction().getFactionId() != factionId) ) {
                    player.error("That player is not part of your faction!");
                    return;
                }

                targetFactionPlayer = target.getFactionPlayer();

            } else {

                targetFactionPlayer = player.getFaction().getFactionPlayerWithName(name);

                if ( targetFactionPlayer == null ) {
                    player.error("Player with that name not found!");
                    return;
                }

            }

            boolean value = Boolean.parseBoolean(stringValue);
            if ( value ) {
                stringValue = "true";
            } else {
                stringValue = "false";
            }

            targetFactionPlayer.setProperty(property, value);
            player.info(name + "'s " + property + " value set to " + stringValue);

        }

        @Command(
            aliases = {"fset"},
            usage = "<type> <property> <value>",
            flags = "",
            desc = "Sets certain faction properties",
            min = 0,
            max = 3
        )
        @CommandPermissions({"legends.faction.fset"})
        public static void factionSet(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( args.argsLength() < 3 ) {
                player.message(TextUtil.titleize("Faction Properties"));
                player.info("/fset <type> <property> <value>");
                player.info("type = { faction | settlement | chunk }");
                player.info("value = { true | false }");
                player.info("Faction Properties: { public | build }");
                player.info("Settlement Properties: { build | arena | haven }");
                player.info("Chunk Properties: { build }");
                return;
            }

            if ( !player.hasFaction() ) {
                player.error("You must be in a faction to do that!");
                return;
            }

            String type = args.getString(0);
            String property = args.getString(1).toLowerCase();
            String stringValue = args.getString(2);

            boolean value = Boolean.parseBoolean(stringValue);

            //Set string value to true or false
            if ( value ) {
                stringValue = "true";
            } else {
                stringValue = "false";
            }

            if ( !player.getFactionPlayer().getRole().canDo("setproperties") ) {
                player.error("Only faction elders can use this command.");
                return;
            }

            if ( type.equalsIgnoreCase("faction") ) {

                player.getFaction().setProperty(property, value);

            } else if ( type.equalsIgnoreCase("settlement") || type.equalsIgnoreCase("chunk") ) {

                LegendsSettlement settlement = player.getCurrentLegendsChunk().getSettlement();

                if ( settlement == null ) {
                    player.error("You must be standing inside a settlement to set property values");
                    return;
                }

                if ( settlement.getFactionId() != player.getFaction().getFactionId() ) {
                    player.error("This settlement does not belong to your faction");
                    return;
                }

                if ( type.equalsIgnoreCase("settlement") ) {
                    settlement.setProperty(property, value);
                } else {
                    player.getCurrentLegendsChunk().build = value;
                }

            } else {
                player.error("Unknown Type.");
                player.info("/fset faction <property> <value>");
                player.info("or");
                player.info("/fset settlement <property> <value>");
            }

            player.info(type + " property " + property + " set to " + stringValue);

        }

        @Command(
            aliases = {"fsetspawn"},
            usage = "",
            flags = "",
            desc = "sets faction spawn to the current location",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.faction.setspawn"})
        public static void setSpawn(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            if ( player.getFactionPlayer().getRole().value < FactionRole.ELDER.value ) {
                player.error("Your rank isn't high enough!");
                return;
            }

            LegendsMap map = plugin.getMapForWorld(player.getHandle().getWorld());
            LegendsChunk chunk = map.getChunk(player.getHandle().getLocation());

            if ( !chunk.isFactionOwned() ) {
                player.error("This is not faction territory.");
                return;
            }

            if ( chunk.getFactionId() != player.getFaction().getFactionId() ) {
                player.error("This is not faction territory.");
                return;
            }

            player.getFaction().setSpawn(player.getHandle().getLocation());
            player.getFaction().broadcast("New faction spawn set!");
            
        }

        @Command(
            aliases = {"fspawn"},
            usage = "",
            flags = "",
            desc = "Teleport to faction spawn location",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.faction.spawn"})
        public static void spawn(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            if ( player.getFaction().getSpawn() == null ) {
                player.error("Your faction leader has not yet set spawn!");
                return;
            }

            player.getHandle().teleport(player.getFaction().getSpawn());

            /*
            if ( player.getFactionPlayer().getRole().value < FactionRole.ELDER.value ) {
                player.error("Your rank isn't high enough!");
                return;
            }

            player.getFaction().setSpawn(player.getHandle().getLocation());
            player.getFaction().broadcast("New faction spawn set!");*/

        }


        @Command(
            aliases = {"faddclaims"},
            usage = "<faction> <number>",
            flags = "",
            desc = "Add special claim points to a faction",
            min = 2,
            max = 2
        )
        @CommandPermissions({"legends.admin.faction.addclaims"})
        public static void addClaims(CommandContext args, Legends plugin, LegendsPlayer player) {

            String name = args.getString(0);
            int points = args.getInteger(1);

            /*if ( points <= 0 ) {
                player.error("Invalid Number!");
                return;
            }*/

            LegendsFaction faction = null;

            faction = FactionsManager.instance.factionWithName(name);

            if ( faction == null ) {

                faction = FactionsManager.instance.factionWithPrefix(name);

                if ( faction == null ) {
                    player.error("Couldn't find faction " + name);
                    return;
                }

            }

            faction.addSpecialClaimPoints(points);
            player.info("Added " + Integer.toString(points) + " claim points to faction " + faction.getName());
        
        }

        @Command(
            aliases = {"faddsettles"},
            usage = "<faction> <number>",
            flags = "",
            desc = "Add special settlement points to a faction",
            min = 2,
            max = 2
        )
        @CommandPermissions({"legends.admin.faction.addsettles"})
        public static void addSettlements(CommandContext args, Legends plugin, LegendsPlayer player) {

            String name = args.getString(0);
            int points = args.getInteger(1);

            /*if ( points <= 0 ) {
                player.error("Invalid Number!");
                return;
            }*/

            LegendsFaction faction = null;

            faction = FactionsManager.instance.factionWithName(name);

            if ( faction == null ) {

                faction = FactionsManager.instance.factionWithPrefix(name);

                if ( faction == null ) {
                    player.error("Couldn't find faction " + name);
                    return;
                }

            }

            faction.addSpecialSettlementPoints(points);
            player.info("Added " + Integer.toString(points) + " settlement points to faction " + faction.getName());

        }

        @Command(
            aliases = {"fsetname"},
            usage = "<name>",
            flags = "",
            desc = "Set faction name.",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.faction.setname"})
        public static void setName(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            if ( player.getFactionPlayer().getRole().value < FactionRole.CHIEF_ELDER.value ) {
                player.error("Your rank isn't high enough!");
                return;
            }

            String name = args.getString(0);

            if ( !FactionsManager.validateName(name) ) {
                player.error("Factoin name must be 4-20 and only letters and numbers!");
                return;
            }

            if ( FactionsManager.instance.factionExists(name) ) {
                player.error("Faction with that name already exists!");
                return;
            }

            LegendsFaction faction = player.getFaction();
            String oldName = faction.getName();
            faction.setName(name);
            Legends.instance.server.broadcastMessage(
                    faction.getColor() +
                    oldName +
                    ChatColor.WHITE +
                    " is now known as " +
                    faction.getColor() +
                    faction.getName());


        }

        @Command(
            aliases = {"fsetprefix"},
            usage = "<prefix>",
            flags = "",
            desc = "Set faction prefix",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.faction.setname"})
        public static void setPrefix(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            if ( player.getFactionPlayer().getRole().value < FactionRole.CHIEF_ELDER.value ) {
                player.error("Your rank isn't high enough!");
                return;
            }

            String prefix = args.getString(0);

            if ( !FactionsManager.validatePrefix(prefix)) {
                player.error("Faction prefix must be 2-6 and only letters and numbers!");
                return;
            }

            if ( FactionsManager.instance.factionPrefixExists(prefix) ) {
                player.error("Faction with that name already exists!");
                return;
            }

            LegendsFaction faction = player.getFaction();
            //String oldPrefix = faction.getRawPrefix();
            faction.setPrefix(prefix);
            Legends.instance.server.broadcastMessage(
                    faction.getColor() +
                    faction.getName() +
                    ChatColor.WHITE +
                    " will now use the prefix " +
                    faction.getColor() +
                    faction.getRawPrefix());

            faction.updatePlayers();


        }

        @Command(
            aliases = {"fsetcolor"},
            usage = "<aqua|black|blue|darkAqua|darkBlue|darkGrey|darkGreen|darkPurple|darkRed|gold|gray|green|lightPurple|red|white|yellow>",
            flags = "",
            desc = "Set the faction color.",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.faction.setColor"})
        public static void setColor(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( !player.hasFaction() ) {
                player.error("You have no faction!");
                return;
            }

            if ( player.getFactionPlayer().getRole().value < FactionRole.CHIEF_ELDER.value ) {
                player.error("Your rank isn't high enough!");
                return;
            }

            String colorString = args.getString(0).toLowerCase();

            LegendsFaction faction = player.getFaction();

            ChatColor color = null;

            if ( colorString.equals("aqua") ) {
                color = ChatColor.AQUA;
            } else if ( colorString.equals("black") ) {
                color = ChatColor.BLACK;
            } else if ( colorString.equals("blue") ) {
                color = ChatColor.BLUE;
            } else if ( colorString.equals("darkaqua") ) {
                color = ChatColor.DARK_AQUA;
            } else if ( colorString.equals("darkblue") ) {
                color = ChatColor.DARK_BLUE;
            } else if ( colorString.equals("darkgrey") ) {
                color = ChatColor.DARK_GRAY;
            } else if ( colorString.equals("darkgreen") ) {
                color = ChatColor.DARK_GREEN;
            } else if ( colorString.equals("darkpurple") ) {
                color = ChatColor.DARK_PURPLE;
            } else if ( colorString.equals("darkred") ) {
                color = ChatColor.DARK_RED;
            } else if ( colorString.equals("gold") ) {
                color = ChatColor.GOLD;
            } else if ( colorString.equals("gray") ) {
                color = ChatColor.GRAY;
            } else if ( colorString.equals("green") ) {
                color = ChatColor.GREEN;
            } else if ( colorString.equals("lightpurple") ) {
                color = ChatColor.LIGHT_PURPLE;
            } else if ( colorString.equals("red") ) {
                color = ChatColor.RED;
            } else if ( colorString.equals("white") ) {
                color = ChatColor.WHITE;
            } else if ( colorString.equals("yellow") ) {
                color = ChatColor.YELLOW;
            }

            if ( color == null ) {
                player.error("No Such Color");
                return;
            }

            faction.setColor(color);
            faction.broadcast("Faction color has been changed!");

            faction.updatePlayers();

        }

        @Command(
            aliases = {"factions","factionlist","listfactions"},
            usage = "",
            flags = "",
            desc = "List all factions",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.faction.factions"})
        public static void factionsList(CommandContext args, Legends plugin, LegendsPlayer player) {

            FactionsManager.instance.sendFactionListToPlayer(player);

        }

        @Command(
            aliases = {"sethaven","togglehaven"},
            usage = "",
            flags = "",
            desc = "Designate a Settlement as a safe haven",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.admin.haven"})
        public static void setHaven(CommandContext args, Legends plugin, LegendsPlayer player) {

            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( chunk == null ) {
                player.error("Unable to get chunk.");
                return;
            }

            if ( chunk.getSettlementId() == 0 ) {
                player.error("Chunk is not part of settlement.");
                return;
            }

            LegendsSettlement settlement = SettlementManager.instance.getSettlementForId(chunk.getSettlementId());

            if ( settlement == null ) {
                player.error("Chunk is not part of settlement.");
                return;
            }

            settlement.setProperty("haven", !settlement.getProperty("haven"));

            if ( settlement.getProperty("haven") ) {
                player.info("Settlement has been set as haven.");
            } else {
                player.info("Settlement is no longer a haven.");
            }

            chunk.broadcast(chunk.getFormattedEnterMessage());

        }

        @Command(
            aliases = {"fsetarena","ftogglearena"},
            usage = "",
            flags = "",
            desc = "Designate a Settlement as an arena",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.faction.arena"})
        public static void setArena(CommandContext args, Legends plugin, LegendsPlayer player) {

            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( chunk == null ) {
                player.error("Unable to get chunk.");
                return;
            }

            if ( chunk.getSettlementId() == 0 ) {
                player.error("Chunk is not part of settlement.");
                return;
            }

            if ( !player.hasFaction() ) {
                player.error("You don't have a faction!");
                return;
            }

            if ( chunk.getFactionId() != player.getFaction().getFactionId() ) {
                player.error("You are not part of that faction!");
                return;
            }

            if ( !player.getFactionPlayer().getRole().canDo("claim") ) {
                player.error("You don't have permission to do that!");
                return;
            }

            LegendsSettlement settlement = SettlementManager.instance.getSettlementForId(chunk.getSettlementId());

            if ( settlement == null ) {
                player.error("Chunk is not part of settlement.");
                return;
            }

            settlement.setProperty("arena", !settlement.getProperty("arena"));

            if ( settlement.getProperty("arena") ) {
                player.info("Settlement has been set as an arena.");
            } else {
                player.info("Settlement is no longer an arena.");
            }

            chunk.broadcast(chunk.getFormattedEnterMessage());

        }

        @Command(
            aliases = {"fsetSettlementFaction","fchangeSettlementFaction","fssf","fcsf"},
            usage = "<Faction Name/Prefix>",
            flags = "",
            desc = "Change the faction owner of a settlement.",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.admin.faction.changeSettlementFaction"})
        public static void changeSettlementFaction(CommandContext args, Legends plugin, LegendsPlayer player) {

            String factionName = args.getString(0);

            //Try to get faction with name
            LegendsFaction faction = FactionsManager.instance.factionWithName(factionName);

            if ( faction == null ) {

                //Try to get faction using prefix instead
                faction = FactionsManager.instance.factionWithPrefix(factionName);

                if ( faction == null ) {
                    player.error("No faction found with given name or prefix.");
                }

            }

            //Get Current Settlement
            LegendsChunk chunk = player.getCurrentLegendsChunk();

            int settlementId = chunk.getSettlementId();

            if ( settlementId == 0 ) {
                player.error("There is no settlement here to change.");
                return;
            }

            LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(settlementId);

            int oldFactionId = settlement.getFactionId();

            //If factions are the same do nothing
            if ( oldFactionId == faction.getFactionId() ) {
                player.error("Settlement already belongs to that faction");
                return;
            }

            LegendsFaction oldFaction = FactionsManager.instance.getFactionWithId(oldFactionId);

            oldFaction.removeSettlementId(settlementId);
            faction.addSettlementId(settlementId);

            settlement.setFactionId(faction.getFactionId());

            Legends.instance.server.broadcastMessage(settlement.getName() + " now belongs to " + faction.getName());

        }

        @Command(
            aliases = {"faddplayer"},
            usage = "<PlayerName> <Faction Name/Prefix>",
            flags = "",
            desc = "Add a player to a faction even if that player is offline",
            min = 2,
            max = 2
        )
        @CommandPermissions({"legends.admin.faction.addPlayer"})
        public static void addPlayer(CommandContext args, Legends plugin, LegendsPlayer player) {



        }

        @Command(
            aliases = {"fmanage"},
            usage = "<Faction Name/Prefix>",
            flags = "",
            desc = "Temporarily Become the Leader of a faction",
            min = 0,
            max = 1
        )
        @CommandPermissions({"legends.admin.faction.manage"})
        public static void manage(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( args.length() <= 1 ) {
                
                player.factionManageId = 0;
                player.info("No longer managing any faction.");
                return;

            }

            LegendsFaction faction = FactionsManager.instance.factionLookup(args.getString(0));

            if ( faction == null ) {
                player.error("Faction with that name or prefix couldn't be found.");
                return;
            }

            player.factionManageId = faction.getFactionId();

            player.info("Now managing faction: " +  faction.getDisplayName());

        }


        /*
        @Command(
            aliases = {"fallpublicbuild"},
            usage = "",
            flags = "",
            desc = "Make all public build",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.admin.faction.allpublicbuild"})
        public static void allPublicBuild(CommandContext args, Legends plugin, LegendsPlayer player) {

            FactionsManager.instance.setAllPublicBuild(true);
            player.message("All factions are now public build.");
            
        }
         
         */

}
