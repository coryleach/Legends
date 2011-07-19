/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.dungeons;

//import java.util.*;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.worldedit.bukkit.selections.*;
import org.bukkit.entity.Player;

import com.coryleach.legends.*;

/**
 *
 * @author cory
 */
public class LairDungeonCommand {
        @Command(
            aliases = {"dungeoncreate", "dcreate"},
            usage = "<name> [properties]",
            flags = "",
            desc = "Create a new Dungeon.",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.dungeon.create"})
        public static void dungeonCreate(CommandContext args, Legends plugin, LegendsPlayer player) {

            //This command should create a dungeon and open an editing session
            String name = args.getString(0);
            player.message("Creating dungeon with name " + name);
            String worldName = player.getCurrentChunk().getWorld().getName();
            Dungeon dungeon = new Dungeon(worldName,name);

            Legends.instance.dungeons.addDungeon(worldName, dungeon);
            Legends.instance.dungeons.createSessionForPlayer(player, dungeon);

        }

        @Command(
            aliases = {"dungeonstart", "dstart"},
            usage = "",
            flags = "",
            desc = "Start dungeon mob spawning",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.dungeon.admin.start"})
        public static void dungeonStart(CommandContext args, Legends plugin, LegendsPlayer player) {

            plugin.dungeons.startDungeons();
            player.info("Dungeons Started.");

        }

        @Command(
            aliases = {"dungeonstop", "dstop"},
            usage = "",
            flags = "",
            desc = "Stop dungeon mob spawning",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.dungeon.admin.stop"})
        public static void dungeonStop(CommandContext args, Legends plugin, LegendsPlayer player) {

            plugin.dungeons.stopDungeons();
            player.info("Dungeons Stopped.");

        }

        @Command(
            aliases = {"dungeonedit", "dedit"},
            usage = "<name> [properties]",
            flags = "",
            desc = "Edit a Dungeon.",
            min = 1,
            max = 3
        )
        @CommandPermissions({"legends.dungeon.edit"})
        public static void dungeonEdit(CommandContext args, Legends plugin, LegendsPlayer player) {

            LegendsDungeons ldungeons = LegendsDungeons.instance;

            //This function should start an editing session for the specified dungeon
            String name = args.getString(0);
            String worldName = player.getHandle().getWorld().getName();
            Dungeon dungeon = ldungeons.getDungeonWithName(worldName,name);

            if ( dungeon == null ) {
                player.error("Dungeon does not exist.");
            } else {
                ldungeons.createSessionForPlayer(player,dungeon);
                player.info("Now editing dungeon " + name);
            }
            
        }

        @Command(
            aliases = {"dungeonsubzone", "dsubzone"},
            usage = "<subcommand> [properties]",
            flags = "",
            desc = "Edit a Dungeon.",
            min = 0,
            max = 3
        )
        @CommandPermissions({"legends.dungeon.edit"})
        public static void dungeonSubzone(CommandContext args, Legends plugin, LegendsPlayer player) {

            LegendsDungeons ldungeons = LegendsDungeons.instance;

            EditSession session = ldungeons.getSessionForPlayer(player);

            if ( session == null ) {
                player.error("You must first start editing a dungeon!");
                return;
            }

            Dungeon dungeon = session.getDungeon();

            String subcommand = null;

            if ( args.length() < 1 ) {
                subcommand = "add";
            } else {
                subcommand = args.getString(0);
            }

            //This function should start an editing session for the specified dungeon
            player.info("Dungeon Add Subzone");

            //subcommands:
            // add
            // remove
            // list
            // current
            // select

            if ( subcommand.equals("add") ) {

                //Add current subzone to dungeon
                //Can only add a subzone that contains no part of any other subzone
                CuboidSelection selection = plugin.getSelection(player.getHandle());

                if ( selection == null ) {
                    player.error("You must select something first!");
                    return;
                }

                DungeonSubzone subzone = new DungeonSubzone(selection,dungeon);
                plugin.log.info("Zone with player selection:");
                subzone.print();

                //Check to see if this selection intersects with any existing
                if ( !dungeon.isValidSubzone(subzone) ) {
                    player.error("Subzone collision. Subzones must not intersect.");
                    return;
                }

                //If Not then we can add it
                dungeon.addSubzone(subzone);
                player.info("Subzone added.");
                
            }

        }

        @Command(
            aliases = {"dungeonlist", "dlist"},
            usage = "[page]",
            flags = "",
            desc = "List Dungeons.",
            min = 0,
            max = 1
        )
        @CommandPermissions({"legends.dungeon.list"})
        public static void dungeonList(CommandContext args, Legends plugin, LegendsPlayer player) {

            LegendsDungeons ldungeons = LegendsDungeons.instance;
            String worldName = player.getHandle().getWorld().getName();

            String[] dungeons = ldungeons.getDungeonList(worldName);

            //***Magickal Pagination!!!!!***
            int totalDungeons = dungeons.length;
            int dungeonsPerPage = 10;
            int totalPages = (int)Math.ceil((double)totalDungeons/(double)dungeonsPerPage);
            int page = 1;

            //Set the page # if there is an extra argument
            if ( args.argsLength() > 0 ) {
                page = args.getInteger(0);
            }

            //If page is higher than max set to last page
            if ( page > totalPages ) {
                page = totalPages;
            }

            //If page number is negative set to first page
            if ( page <= 0 ) {
                page = 1;
            }

            //Print Page Info
            player.message("Page " + Integer.toString(page) + " of " + Integer.toString(totalPages));

            //Print Dungeon Names
            int startIndex = (page - 1) * dungeonsPerPage;
            int stopIndex = startIndex + 10;

            for ( int i = 0; i < stopIndex && i < dungeons.length; i++ ) {

                player.message(dungeons[i].toString());

            }

            //Print Total Dungeons
            player.message("Total Dungeon(s): " + Integer.toString(totalDungeons));

        }
        
}

//If you could only see the beast you made of me
