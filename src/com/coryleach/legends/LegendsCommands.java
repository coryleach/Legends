/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends;

import org.bukkit.*;
import java.util.*;
import com.sk89q.minecraft.util.commands.*;
import com.coryleach.legends.factions.*;
import org.bukkit.inventory.*;
import org.bukkit.entity.Player;
import com.earth2me.essentials.api.*;

/**
 *
 * @author cory
 */
public class LegendsCommands {

    static Random rand = new Random();

        @Command(
            aliases = {"health", "hp"},
            usage = "",
            flags = "",
            desc = "Print info about player class.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.common.health"})
        public static void health(CommandContext args, Legends plugin, LegendsPlayer player) {

            //Send player info about their current class
            player.info("Health: " 
                    + ChatColor.YELLOW
                    + Integer.toString(player.getHealth())
                    + " / "
                    + Integer.toString(player.getMaxHealth())
                    );
            player.info("Armor: " + player.getArmorValue());

        }    

        @Command(
            aliases = {"tutorial","tutor","newbie","noob"},
            usage = "",
            flags = "",
            desc = "Teleports you to the tutorial area.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.common.tutorial"})
        public static void tutorial(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( plugin.settings.tutorialLocation == null ) {
                player.error("There is no tutorial warp set!");
                return;
            }

            player.getHandle().teleport(plugin.settings.tutorialLocation);

        }

        @Command(
            aliases = {"settutorial","settutor","setnewbie","setnoob"},
            usage = "",
            flags = "",
            desc = "Sets tutorial spawn point.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.admin.settutorial"})
        public static void settutorial(CommandContext args, Legends plugin, LegendsPlayer player) {

            plugin.settings.tutorialLocation = player.getHandle().getLocation();

        }

        static String lastPersonToWhore = "";

        @Command(
            aliases = {"whore", "tramp","streetwalk","cunt","fuck"},
            usage = "",
            flags = "",
            desc = "Print info about player class.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.common.whore"})
        public static void whore(CommandContext args, Legends plugin, LegendsPlayer player) {

            if ( lastPersonToWhore.equals(player.getName()) ) {
                player.info("You've whored enough for one day!!!");
                return;
            }

            int x = rand.nextInt();

            x = x % 7;

            ChatColor color = ChatColor.AQUA;

            switch(x) {
                case 0:
                    color = ChatColor.AQUA;
                    break;
                case 1:
                    color = ChatColor.LIGHT_PURPLE;
                    break;
                case 2:
                    color = ChatColor.YELLOW;
                    break;
                case 3:
                    color = ChatColor.RED;
                    break;
                case 4: 
                    color = ChatColor.GREEN;
                    break;
                case 5:
                    color = ChatColor.GOLD;
                    break;
                case 6:
                    color = ChatColor.BLUE;
                    break;
            }

            String string = player.getName() + "is a WHORE! a FILHTY, FILTHY WHORE!!!";

            x = rand.nextInt(Math.abs(x));
            x = x % 7;

            

            switch(x) {
                case 0:
                    string = player.getName() + " is a WHORE! a FILHTY, FILTHY WHORE!!!";
                    break;
                case 1:
                    string = player.getName() + " is a slut ass bitch whore that always walks like she has something stuck up her ass.";
                    break;
                case 2:
                    string = player.getName() + ", You say you’re not a ho? Alright, no doubt. Next time you wanna say that don’t forget to wipe your mouth.";
                    break;
                case 3:
                    string = "I don't know of a bigger slut ass bitch whore than " + player.getName() + "!";
                    break;
                case 4:
                    string = "Who here wants to stick it in " + player.getName() + "'s slut oven? Anyone?";
                    break;
                case 5:
                    string = player.getName() + "is a WHORE! a FILHTY, FILTHY WHORE!!!";
                    break;
                case 6:
                    string = player.getName() + "will make you holla for a dolla";
                    break;
                case 7:
                    break;
            }


            plugin.server.broadcastMessage(color + string);

        }

        @Command(
            aliases = {"map"},
            usage = "",
            flags = "",
            desc = "Print a map showing faction owned land.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.common.map"})
        public static void map(CommandContext args, Legends plugin, LegendsPlayer player) {

            LegendsFaction faction = player.getFaction();

            World world = player.getCurrentChunk().getWorld();

            LegendsMap legendsMap = Legends.instance.getMapForWorld(world);

            ArrayList<String> map = legendsMap.getMap(player, player.getHandle().getLocation());

            for ( String line : map ) {

                player.message(line);
                
            }

        }

        @Command(
            aliases = {"identify","ident","id"},
            usage = "",
            flags = "",
            desc = "Identify the currently held item.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.common.identify"})
        public static void identify(CommandContext args, Legends plugin, LegendsPlayer player) {

            ItemStack itemStack = player.getHandle().getItemInHand();

            if ( itemStack == null ) {
                player.error("You are not holding any items");
                return;
            }

            int typeId = itemStack.getTypeId();

            player.message("Material: " + itemStack.getType().name());
            player.message("Type Id:" + Integer.toString(typeId));
            player.message("Max Stack Size: " + Integer.toString(itemStack.getMaxStackSize()));
            player.message("Durability: " + Integer.toString(itemStack.getDurability()));

        }

        @Command(
            aliases = {"lsave"},
            usage = "",
            flags = "",
            desc = "save.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.admin.save"})
        public static void save(CommandContext args, Legends plugin, LegendsPlayer player) {

            Legends.instance.saveWorldMaps();

        }

        @Command(
            aliases = {"lload"},
            usage = "",
            flags = "",
            desc = "load.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.admin.load"})
        public static void load(CommandContext args, Legends plugin, LegendsPlayer player) {

            Legends.instance.loadWorldMaps();

        }

        /*
        @Command(
            aliases = {"lloadold"},
            usage = "",
            flags = "",
            desc = "load.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.admin.load"})
        public static void oldload(CommandContext args, Legends plugin, LegendsPlayer player) {

            Legends.instance.map.oldload();

        }

        @Command(
            aliases = {"lsaveold"},
            usage = "",
            flags = "",
            desc = "save.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.admin.save"})
        public static void oldsave(CommandContext args, Legends plugin, LegendsPlayer player) {

            Legends.instance.map.oldsave();

        }*/

        @Command(
            aliases = {"setLegendsSpawn","lsetspawn"},
            usage = "",
            flags = "",
            desc = "Set Spawn.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.admin.setspawn"})
        public static void setSpawn(CommandContext args, Legends plugin, LegendsPlayer player) {

            Player handle = player.getHandle();
            Location loc = handle.getLocation();

            handle.getLocation().getWorld().setSpawnLocation(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());

            player.info("New spawn location set!");

        }

        @Command(
            aliases = {"settlers"},
            usage = "",
            flags = "",
            desc = "List all the builders in the settlement",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.common.settlerslist"})
        public static void listSettlers(CommandContext args, Legends plugin, LegendsPlayer player) {

            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( !chunk.isClaimed() ) {
                player.error("This command must be used inside of a settlement");
                return;
            }

            LegendsSettlement settlement = chunk.getSettlement();

            settlement.sendSettlerList(player);

        }

        @Command(
            aliases = {"addbuilder","uaddbuilder","uabuilder","uaddsettler","uasettler","addsettler"},
            usage = "<player name>",
            flags = "",
            desc = "Add builder to a settlement",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.common.uaddbuilder"})
        public static void addBuilder(CommandContext args, Legends plugin, LegendsPlayer player) {

            String name = args.getString(0);
            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( !chunk.isClaimed() ) {
                player.error("This command must be used inside of a settlement");
                return;
            }

            if ( chunk.isFactionOwned() ) {
                player.error("This command is only used for user settlements.");
                return;
            }

            LegendsSettlement settlement = chunk.getSettlement();

            if ( !settlement.getOwnerName().equalsIgnoreCase(player.getName()) ) {
                if ( !plugin.hasPermission(player.getHandle(), "legends.admin.uaddbuilder") ) {
                    player.error("Only the owner of this settlement can add and remove builders.");
                    return;
                }
            }

            settlement.addBuilder(name);
            player.message("Settler added.");

        }

        @Command(
            aliases = {"removeBuilder","urbuilder","removesettler","ursettler"},
            usage = "<player name>",
            flags = "",
            desc = "Add builder to a settlement",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.common.uremovebuilder"})
        public static void removeBuilder(CommandContext args, Legends plugin, LegendsPlayer player) {

            String name = args.getString(0);
            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( !chunk.isClaimed() ) {
                player.error("This command must be used inside of a settlement");
                return;
            }

            if ( chunk.isFactionOwned() ) {
                player.error("This command is only used for user settlements.");
                return;
            }

            LegendsSettlement settlement = chunk.getSettlement();

            if ( !settlement.getOwnerName().equalsIgnoreCase(player.getName()) ) {
                if ( !plugin.hasPermission(player.getHandle(), "legends.admin.uremovebuilder") ) {
                    player.error("Only the owner of this settlement can add and remove builders.");
                    return;
                }
            }

            if ( !settlement.isBuilder(name) ) {
                player.error("Player is not a builder for this settlement");
                return;
            }

            settlement.removeBuilder(name);
            player.message("settler removed.");

        }

        @Command(
            aliases = {"uinfo","stats","claims","claiminfo","uclaims","settles","usettles"},
            usage = "",
            flags = "",
            desc = "View Player Stats.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.common.claimstats"})
        public static void uinfo(CommandContext args, Legends plugin, LegendsPlayer player) {

            player.title("Claim Info");

            player.message(
                    ChatColor.GREEN +
                    "Claims: " +
                    ChatColor.YELLOW +
                    Integer.toString(player.getClaimPoints()) +
                    ChatColor.GREEN +
                    " Settles: " +
                    ChatColor.YELLOW +
                    Integer.toString(player.getSettlementPoints())
                    );

            player.message(
                    ChatColor.GREEN +
                    "Max Claims: " +
                    ChatColor.YELLOW +
                    Integer.toString(player.getMaxClaimPoints()) +
                    ChatColor.GREEN +
                    " MaxSettles: " +
                    ChatColor.YELLOW +
                    Integer.toString(player.getMaxSettlementPoints())
                    );

        }

       @Command(
            aliases = {"usettlements","listsettlements","mysettlements"},
            usage = "<Player>",
            flags = "",
            desc = "View Player settlements.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.common.usettlements"})
        public static void usettlements(CommandContext args, Legends plugin, LegendsPlayer player) {

            player.sendSettlements(player);

        }

       @Command(
            aliases = {"settlement","where","whereami","here","sinfo","settlementinfo"},
            usage = "",
            flags = "",
            desc = "View current location info",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.common.settlementinfo"})
        public static void settlementInfo(CommandContext args, Legends plugin, LegendsPlayer player) {

            LegendsChunk chunk = player.getCurrentLegendsChunk();

            player.message(chunk.getFormattedEnterMessage());

            //Check if there is a settlement here
            if ( chunk.getSettlementId() == 0 ) {

                player.message("This chunk is unclaimed. Type /map to view a larger area.");
                return;

            }

            LegendsSettlement settlement = chunk.getSettlement();

            if ( settlement.getFactionId() != 0 ) {

            } else if ( settlement.getOwnerName() != null ) {

                player.info("Owner: " + settlement.getOwnerName());

            } else {
                
                player.error("Something fishy about this settlement. Please report to admin.");
                
            }

            ArrayList<String> builders = settlement.getBuilders();
            Iterator<String> i = builders.iterator();

            if ( builders.size() <= 0 ) {
                return;
            }

            player.info("Other Builders:");

            while ( i.hasNext() ) {

                String name = i.next();
                player.info(name);

            }


        }

        @Command(
            aliases = {"usettle"},
            usage = "<settlement name>",
            flags = "",
            desc = "Create a Settlement with the given name",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.common.usettle"})
        public static void settle(CommandContext args, Legends plugin, LegendsPlayer player) {

            String name = args.getString(0);

            //Check for valid name
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

            if ( chunk.isClaimed() ) {
                player.error("This chunk has already been claimed!");
                return;
            }

            if ( player.getSettlementPoints() <= 0 ) {
                player.error("You don't have enough settlement points to do that!");
                return;
            }

            if ( player.getClaimPoints() <= 0 ) {
                player.error("You don't have enough claim points to do that!");
                player.error("You must have 1 settle point and 1 claim point to start a new settlement!");
                return;
            }

            //Claim the settlement
            String worldName = player.getHandle().getWorld().getName();
            LegendsSettlement settlement = new LegendsSettlement(player.getName(),name,worldName);
            settlement.addChunk(chunk);

            //Add to settlement manager
            SettlementManager.getInstance().addSettlement(settlement);
            player.addSettlement(settlement);

            Legends.instance.server.broadcastMessage(
                    ChatColor.AQUA +
                    player.getName() + 
                    ChatColor.WHITE +
                    " has founded the new settlement " +
                    ChatColor.AQUA +
                    settlement.getName() +
                    ChatColor.WHITE +
                    "!");

            chunk.broadcast(chunk.getFormattedEnterMessage());

        }

        @Command(
            aliases = {"uclaim", "userclaim"},
            usage = "<settlement name>",
            flags = "",
            desc = "Claim a chunk of land adjacent to a settlement and adds it to that settlement.",
            min = 0,
            max = 1
        )
        @CommandPermissions({"legends.common.uclaim"})
        public static void claim(CommandContext args, Legends plugin, LegendsPlayer player) {

            Location loc = player.getHandle().getLocation();

            //Check to see if the chunk has already been claimed
            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( chunk.isClaimed() ) {
                player.error("This chunk has already been claimed!");
                return;
            }

            //Do we have enough allocation points?
            if ( player.getClaimPoints() <= 0 ) {
                player.error("You don't have any claim points!");
                return;
            }

            //Claim the current chunk
            LegendsSettlement settlement = player.getAdjacentSettlement(chunk);

            if ( settlement == null ) {
                player.error("You can only claim chunks adjacent to an existing settlement!");
                return;
            }

            settlement.addChunk(chunk);
            chunk.broadcast("This chunk has been claimed by " + player.getName() + "!");
            chunk.broadcast(chunk.getFormattedEnterMessage());

        }

        @Command(
            aliases = {"uabandon", "userabandon"},
            usage = "",
            flags = "",
            desc = "Abandon a chunk of previously claimed land.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.common.uabandon"})
        public static void abandon(CommandContext args, Legends plugin, LegendsPlayer player) {

            Location loc = player.getHandle().getLocation();

            //Check world
            if ( !loc.getWorld().getName().equals("world") ) {
                player.error("Chunks cannot be claimed/abandoned in this world.");
                return;
            }

            //Check to see if the chunk has already been claimed
            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( chunk.isFactionOwned() ) {
                player.error("You cannot use this command to abandon this chunk. Use /fabandon");
                return;
            }

            //Get settlement
            LegendsSettlement settlement = SettlementManager.getInstance().getSettlementForId(chunk.getSettlementId());

            if ( settlement == null ) {
                player.error("This chunk doesn't belong to a settlement.");
                return;
            }

            //Check owner permission
            if ( !settlement.getOwnerName().equalsIgnoreCase(player.getName()) ) {
                player.error("You do not own this settlement!");
                return;
            }

            //Abandon last chunk
            if ( settlement.size() == 1 ) {

                Legends.instance.server.broadcastMessage("Settlement " + settlement.getName() + " has been dissolved.");
                SettlementManager.getInstance().removeSettlement(settlement);
                player.removeSettlement(settlement);

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
            aliases = {"rsettlement", "removesettlement"},
            usage = "",
            flags = "",
            desc = "Delete a settlement and free all its chunks",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.admin.removesettlement"})
        public static void removeSettlement(CommandContext args, Legends plugin, LegendsPlayer player) {

            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( !chunk.isClaimed() ) {
                player.error("This is not a settled chunk");
                return;
            }

            if ( chunk.isFactionOwned() ) {
                player.error("This function does not support faction owned settlements");
                return;
            }

            LegendsSettlement settlement = chunk.getSettlement();

            SettlementManager.getInstance().removeSettlement(settlement);

            settlement.removeAllChunks();

            player.message("Settlement " + settlement.getName() + " has been dissolved.");

        }

        @Command(
            aliases = {"renamesettlement"},
            usage = "<New Name>",
            flags = "",
            desc = "Rename a settlement",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.common.usettlementrename"})
        public static void renameSettlement(CommandContext args, Legends plugin, LegendsPlayer player) {

            String name = args.getString(0);
            LegendsChunk chunk = player.getCurrentLegendsChunk();

            if ( !chunk.isClaimed() ) {
                player.error("This is not a settled chunk");
                return;
            }

            if ( chunk.isFactionOwned() ) {
                player.error("This function does not support faction owned settlements");
                return;
            }

            //Check for valid name
            if ( !SettlementManager.validateName(name) ) {
                player.error("Name must be 4-20 characters, letters and numbers only.");
                return;
            }

            //Check if name is in use already
            if ( SettlementManager.getInstance().settlementExistsWithName(name) ) {
                player.error("Settlement with that name already exists");
                return;
            }

            LegendsSettlement settlement = chunk.getSettlement();

            String oldName = settlement.getName();
            settlement.setName(name);

            plugin.server.broadcastMessage(oldName + " will now be known as " + name);

        }

        @Command(
            aliases = {"uaddsettles"},
            usage = "<player name> <number of settles>",
            flags = "",
            desc = "Add a number of settlement points to a user",
            min = 2,
            max = 2
        )
        @CommandPermissions({"legends.admin.uaddsettles"})
        public static void addSettles(CommandContext args, Legends plugin, LegendsPlayer player) {

            String name = args.getString(0);
            int value = args.getInteger(1);

            Player bukkitPlayer = plugin.server.getPlayer(name);
            
            if ( bukkitPlayer == null ) {

                player.error("Could not find player with that name.");
                return;

            }

            LegendsPlayer playerToAddTo = plugin.wrapPlayer(bukkitPlayer);
            playerToAddTo.addSettlementPoints(value);
            
            player.info("Added " + Integer.toString(value) + " settlement point(s) to player");

        }

        @Command(
            aliases = {"uaddclaims"},
            usage = "<player name> <number of claims>",
            flags = "",
            desc = "Add a number of claim points to a user",
            min = 2,
            max = 2
        )
        @CommandPermissions({"legends.admin.removesettlement"})
        public static void addClaims(CommandContext args, Legends plugin, LegendsPlayer player) {

            String name = args.getString(0);
            int value = args.getInteger(1);

            Player bukkitPlayer = plugin.server.getPlayer(name);

            if ( bukkitPlayer == null ) {
                player.error("Could not find player with that name.");
                return;
            }

            LegendsPlayer playerToAddTo = plugin.wrapPlayer(bukkitPlayer);
            playerToAddTo.addSettlementPoints(value);

            player.info("Added " + Integer.toString(value) + " claim point(s) to player");

        }

        @Command(
            aliases = {"prunemaps"},
            usage = "",
            flags = "",
            desc = "",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.admin.prune"})
        public static void mapPruning(CommandContext args, Legends plugin, LegendsPlayer player) {


            World world = player.getCurrentChunk().getWorld();

            LegendsMap map = plugin.getMapForWorld(world);

            player.message("Pruning unused chunks from maps. Current size: " + Integer.toString(map.size()));

            map.prune();
            
            player.message("Pruning complete. New map size: " + Integer.toString(map.size()));

        }

        @Command(
            aliases = {"fixmaps"},
            usage = "",
            flags = "",
            desc = "",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.admin.fixmaps"})
        public static void mapFix(CommandContext args, Legends plugin, LegendsPlayer player) {

            player.message("Fixing map cunks");

            World world = player.getCurrentChunk().getWorld();

            //Fix Map
            plugin.getMapForWorld(world).fix();

            //Fix Players
            plugin.fixPlayers();

            player.message("Fixing complete");

        }

        @Command(
            aliases = {"buyclaims"},
            usage = "<number of claims to buy>",
            flags = "",
            desc = "",
            min = 0,
            max = 1
        )
        @CommandPermissions({"legends.common.buyclaims"})
        public static void buyClaims(CommandContext args, Legends plugin, LegendsPlayer player) {

            plugin.log.info("buyClaims");

            //Total Purchased Poitns
            int purchasedPoints = player.getPurchasedClaimPoints();

            //Base price and growth
            int basePrice = 10;

            //Calculate amount already paid for currently owned claims
            double totalPaid = purchasedPoints * (10 + (5 * (purchasedPoints - 1)));

            try {

                if ( player.getLegendsSession().isAction("buyclaims") && args.argsLength() <= 0 ) {

                    int numberToPurchase = player.getLegendsSession().amount;

                    //Calcuate cost of total claims including purchase
                    int totalClaimsAfterPurchase = purchasedPoints + numberToPurchase;
                    double totalCost = totalClaimsAfterPurchase * (10 + (5 * (totalClaimsAfterPurchase - 1)));

                    //Adjusted cost is how much the player will have to pay to purchase an additional numberToPurchase claims
                    long adjustedCost = Math.round(totalCost - totalPaid);

                    if ( Economy.hasEnough(player.getName(), adjustedCost) ) {

                        try {
                            player.info("Total Cost: $" + adjustedCost);
                            Economy.subtract(player.getName(), adjustedCost);
                            player.addPruchasedClaimPoints(numberToPurchase);
                            player.info("Purchased " + numberToPurchase + " claim(s) Remaining Balance $" + Economy.getMoney(player.getName()));
                        } catch ( NoLoanPermittedException e ) {
                            player.error("No loan permitted!");
                            plugin.log.info("NO loan permitted for " + player.getName());
                        }

                    } else {
                         player.error("You do not have enough money!");
                    }

                    player.getLegendsSession().action = "";

                } else {

                    int numberToPurchase = 1;

                    if ( args.argsLength() > 0 ) {
                        numberToPurchase = args.getInteger(0);
                    }

                    //Calcuate cost of total claims including purchase
                    int totalClaimsAfterPurchase = purchasedPoints + numberToPurchase;
                    double totalCost = totalClaimsAfterPurchase * (10 + (5 * (totalClaimsAfterPurchase - 1)));

                    //Adjusted cost is how much the player will have to pay to purchase an additional numberToPurchase claims
                    long adjustedCost = Math.round(totalCost - totalPaid);

                    player.info("Total Cost: $" + adjustedCost + " type /buyclaims again to confirm purchase");
                    player.legendsSession.action = "buyclaims";
                    player.legendsSession.amount = numberToPurchase;

                }

            } catch (UserDoesNotExistException e) {

                player.error("Error, Player does not exist!");
                String logInfo = "Player " + player.getName() + " does not exist!";
                plugin.log.info(logInfo);

            }

        }

        @Command(
            aliases = {"buysettles"},
            usage = "",
            flags = "",
            desc = "",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.common.buysettles"})
        public static void buySettles(CommandContext args, Legends plugin, LegendsPlayer player) {

            plugin.log.info("buySettles");

            //Total Purchased Poitns
            int purchasedPoints = player.getPurchasedClaimPoints();

            //Base price and growth
            int basePrice = 20;

            //Calculate amount already paid for currently owned claims
            double totalPaid = purchasedPoints * (20 + (10 * (purchasedPoints - 1)));


            try {

            if ( player.getLegendsSession().isAction("buysettles") && args.argsLength() <= 0 ) {

                int numberToPurchase = player.legendsSession.amount;

                //Calcuate cost of total claims including purchase
                int totalClaimsAfterPurchase = purchasedPoints + numberToPurchase;
                double totalCost = totalClaimsAfterPurchase * (20 + (10 * (totalClaimsAfterPurchase - 1)));

                //Adjusted cost is how much the player will have to pay to purchase an additional numberToPurchase claims
                long adjustedCost = Math.round(totalCost - totalPaid);

                if ( Economy.hasEnough(player.getName(), adjustedCost) ) {

                    try {
                        player.info("Total Cost: $" + adjustedCost);
                        Economy.subtract(player.getName(), adjustedCost);
                        player.addPurchasedSettlementPoints(numberToPurchase);
                        player.info("Remaining Balance $" + Economy.getMoney(player.getName()));
                    } catch ( NoLoanPermittedException e ) {
                        player.error("No loan permitted!");
                        plugin.log.info("NO loan permitted for " + player.getName());
                    }

                } else {
                     player.error("You do not have enough money!");
                }

            } else {

                int numberToPurchase = 1;

                if ( args.argsLength() > 0 ) {
                    numberToPurchase = args.getInteger(0);
                }

                //Calcuate cost of total claims including purchase
                int totalClaimsAfterPurchase = purchasedPoints + numberToPurchase;
                double totalCost = totalClaimsAfterPurchase * (20 + (10 * (totalClaimsAfterPurchase - 1)));

                //Adjusted cost is how much the player will have to pay to purchase an additional numberToPurchase claims
                long adjustedCost = Math.round(totalCost - totalPaid);

                player.info("Total Cost: $" + adjustedCost + " type /buysettles again to confirm purchase");
                player.legendsSession.action = "buysettles";
                player.legendsSession.amount = numberToPurchase;

            }

            } catch (UserDoesNotExistException e) {

                player.error("Error, Player does not exist!");
                String logInfo = "Player " + player.getName() + " does not exist!";
                plugin.log.info(logInfo);

            }

        }

}
