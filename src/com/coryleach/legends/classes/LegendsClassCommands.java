/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.classes;

//import java.util.*;
import com.coryleach.legends.Legends;
import com.coryleach.legends.LegendsPlayer;
import com.sk89q.minecraft.util.commands.*;

/**
 *
 * @author cory
 */
public class LegendsClassCommands {
        @Command(
            aliases = {"setclass", "sc"},
            usage = "<Warrior|Mage|Cleric|Thief|Monk>",
            flags = "",
            desc = "Set player class.",
            min = 1,
            max = 1
        )
        @CommandPermissions({"legends.class.set"})
        public static void setClass(CommandContext args, Legends plugin, LegendsPlayer player) {

            String classString = args.getString(0).toLowerCase();

            if ( classString.equals("warrior") ) {
                
                player.setLegendsClass(ClassType.WARRIOR);
                player.info("Class set to Warrior.");

            } else if ( classString.equals("mage") ) {

                player.setLegendsClass(ClassType.MAGE);
                player.info("Class set to Mage.");

            } else if ( classString.equals("cleric") ) {

                player.setLegendsClass(ClassType.CLERIC);
                player.info("Class set to Cleric.");
                
            } else if ( classString.equals("thief") ) {

                player.setLegendsClass(ClassType.THIEF);
                player.info("Class set to Thief.");

            } else if ( classString.equals("monk") ) {

                player.setLegendsClass(ClassType.MONK);
                player.info("Class set to Monk.");

            } else {

                player.error("Class Type Invalid.");

            }

        }

        @Command(
            aliases = {"classinfo", "class"},
            usage = "",
            flags = "",
            desc = "Print info about player class.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.class.info"})
        public static void classInfo(CommandContext args, Legends plugin, LegendsPlayer player) {

            //Send player info about their current class
            LegendsClass legendsClass = player.getLegendsClass();

            if ( legendsClass != null ) {
                legendsClass.sendClassInfoToPlayer(player);
                return;
            }

            //No Class Set Yet, Give Class Tutorial
            player.info("Legends has 5 player classes: Warrior,Mage,Cleric,Thief,Monk");
            player.info("Class can be set or changed at any time using the command '/setclass <classname>'");
            player.info("For more info on a class type '/<classname> info' (ex: '/monk info').");


        }


        @Command(
            aliases = {"displayCombatText", "combatText"},
            usage = "",
            flags = "",
            desc = "Toggles combat text on/off.",
            min = 0,
            max = 0
        )
        @CommandPermissions({"legends.class.displayCombatText"})
        public static void displayCombatText(CommandContext args, Legends plugin, LegendsPlayer player) {

           player.setDisplayCombatText(!player.displayCombatText());

           if ( player.displayCombatText() ) {
               player.info("Toggled Dispaly Combat Text ON");
           } else {
               player.info("Toggled Dispaly Combat Text OFF");
           }

        }

}


