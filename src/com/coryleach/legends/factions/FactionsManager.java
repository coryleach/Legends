/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.factions;

import com.coryleach.legends.Legends;
import com.coryleach.legends.LegendsPlayer;
import java.util.*;
import java.io.*;
import java.lang.reflect.Type;
import com.google.gson.reflect.*;

import com.coryleach.util.*;

/**
 *
 * @author Cory
 */
public class FactionsManager {

    public static FactionsManager instance;
    public static int playersNeededToStartFaction = 3;
    private HashMap<Integer,LegendsFaction> factionMap;
    private HashMap<LegendsPlayer,FactionSession> factionSessionMap;

    public FactionsManager() {
        FactionsManager.instance = this;
        factionMap = new HashMap<Integer,LegendsFaction>();
        factionSessionMap = new HashMap<LegendsPlayer,FactionSession>();
    }

    public void addFaction(LegendsFaction faction) {

        factionMap.put(faction.getFactionId(), faction);

    }

    public LegendsFaction getFactionWithId(int factionId) {

        Integer id = new Integer(factionId);

        if ( factionMap.containsKey(id) ) {
            return factionMap.get(id);
        }

        return null;

    }

    public int getUnusedFactoinId() {

        Random random = new Random();

        int newId = random.nextInt();

        while ( newId == 0 || factionMap.get(new Integer(newId)) != null ) {
            //Get another random id until we find one that isn't used
            // We also skip 0 because that is a special value that means no faction
            newId = random.nextInt();
        }

        return newId;

    }

    public boolean factionPrefixExists(String prefix) {

        Iterator<Integer> i = factionMap.keySet().iterator();

        while( i.hasNext() ) {

            Integer id = i.next();

            LegendsFaction faction = factionMap.get(id);

            if ( faction.getRawPrefix().equals(prefix) ) {
                return true;
            }

        }

        Iterator<LegendsPlayer> j = factionSessionMap.keySet().iterator();

        while ( j.hasNext() ) {

            LegendsPlayer player = j.next();

            FactionSession session = this.factionSessionForPlayer(player);

            if ( session.faction.getPrefix().equals(prefix) ) {
                return true;
            }

        }

        return false;

    }

    public boolean factionExists(String name) {

        for ( Integer key : factionMap.keySet() ) {

           LegendsFaction faction = factionMap.get(key);

            if ( faction.getName().equals(name) ) {
                return true;
            }

        }

        Iterator<LegendsPlayer> j = factionSessionMap.keySet().iterator();

        while ( j.hasNext() ) {

            LegendsPlayer player = j.next();

            FactionSession session = this.factionSessionForPlayer(player);

            if ( session.faction.getName().equals(name) ) {
                return true;
            }

        }

        return false;

    }

    public LegendsFaction factionWithPrefix(String prefix) {

        Iterator<Integer> i = factionMap.keySet().iterator();

        while( i.hasNext() ) {

            Integer id = i.next();

            LegendsFaction faction = factionMap.get(id);

            if ( faction.getRawPrefix().equals(prefix) ) {
                return faction;
            }

        }

        return null;

    }

    public LegendsFaction factionWithName(String name) {

        Iterator<Integer> i = factionMap.keySet().iterator();

        while( i.hasNext() ) {

            Integer id = i.next();

            LegendsFaction faction = factionMap.get(id);

            if ( faction.getName().equals(name) ) {
                return faction;
            }

        }

        return null;

    }

    public LegendsFaction factionLookup(String name) {

        LegendsFaction faction = factionWithName(name);

        if ( faction == null ) {
            faction = factionWithPrefix(name);
        }

        return faction;

    }

    public void updateFactions() {

        for ( Integer key : factionMap.keySet() ) {

           LegendsFaction faction = factionMap.get(key);

           faction.updatePlayers();

        }

    }

    public void setFactionSessionForPlayer(LegendsPlayer player,FactionSession session) {

        if ( player == null ) {
            return;
        }

        if ( session == null ) {
            factionSessionMap.remove(player);
            return;
        }

        factionSessionMap.put(player, session);

    }

    public FactionSession factionSessionForPlayer(LegendsPlayer player) {

        return factionSessionMap.get(player);

    }

    public static boolean validatePrefix(String str) {

            if(TextUtil.getComparisonString(str).length() < 2 ) {
                return false;
            }

            if(str.length() > 6) {
                return false;
            }

            for (char c : str.toCharArray()) {
                    if ( ! TextUtil.substanceChars.contains(String.valueOf(c))) {
                        return false;
                    }
            }

            return true;

    }

    public static boolean validateName(String str) {

            if(TextUtil.getComparisonString(str).length() < 4 ) {
                return false;
            }

            if(str.length() > 20) {
                return false;
            }

            for (char c : str.toCharArray()) {
                    if ( ! TextUtil.substanceChars.contains(String.valueOf(c))) {
                        return false;
                    }
            }

            return true;

    }

    public boolean save() {

        File file = new File(Legends.instance.getDataFolder(),"legendsFactions.json");

        try {
            file.createNewFile();
            DiscUtil.write(file, Legends.gson.toJson(this.factionMap));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
        
    }

    public boolean load() {

        File file = new File(Legends.instance.getDataFolder(),"legendsFactions.json");

        try {

            if ( !file.exists() ) {
                return true;
            }

            Type type = new TypeToken<HashMap<Integer,LegendsFaction>>(){}.getType();
            this.factionMap = Legends.gson.fromJson(DiscUtil.read(file), type);

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }

        return true;

    }

    public void sendFactionListToPlayer(LegendsPlayer player) {

        int totalPopulation = 0;
        int count = 0;
        for ( Integer id : this.factionMap.keySet() ) {

            LegendsFaction faction = this.factionMap.get(id);
            player.message(faction.getName() + " (" + Integer.toString(faction.population()) + ")" );

            totalPopulation += faction.population();
            count++;

        }

        player.message("Total " + Integer.toString(count) + " faction(s) with " + Integer.toString(totalPopulation) + " members");

    }

    /*
    public void setAllPublicBuild(boolean value) {

        for ( Integer id : this.factionMap.keySet() ) {

            LegendsFaction faction = this.factionMap.get(id);
            faction.setPublicBuild(value);

        }

    }
    */
}
