/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.factions;

import com.coryleach.legends.Legends;
import java.util.*;
import java.io.*;
import java.lang.reflect.Type;
import com.google.gson.reflect.*;

import com.coryleach.util.*;

/**
 *
 * @author Cory
 */
public class SettlementManager {

    protected static SettlementManager instance;

    protected HashMap<Integer,LegendsSettlement> settlements;

    public static SettlementManager getInstance() {

        if ( SettlementManager.instance == null ) {

            try {
                SettlementManager.instance = new SettlementManager();
            } catch(Exception e) {
                Legends.instance.log.severe(e.getMessage());
            }

        }

        return SettlementManager.instance;

    }

    public static void cleanup() {
        SettlementManager.instance = null;
    }

    public SettlementManager() throws Exception {

        if ( SettlementManager.instance != null ) {
            //this.instance = this;
            Exception e = new Exception("Cannot create a multiple instances of SettlementManager!");
            throw(e);
        }

        settlements = new HashMap<Integer,LegendsSettlement>();

    }

    public int getUnusedSettlementId() {

        Random random = new Random();

        int newId = random.nextInt();

        while ( newId == 0 || settlements.get(new Integer(newId)) != null ) {
            //Get another random id until we find one that isn't used
            // We also skip 0 because that is a special value that means no settlement
            newId = random.nextInt();
        }

        return newId;
        
    }

    public void addSettlement(LegendsSettlement settlement) {
        settlements.put(new Integer(settlement.getSettlementId()), settlement);
    }

    public LegendsSettlement getSettlementForId(int id) {

        if ( id == 0 ) {
            return null;
        }

        Integer settlementId = new Integer(id);
        return settlements.get(settlementId);

    }

    public boolean settlementExistsWithName(String name) {

        Iterator<Integer> i = settlements.keySet().iterator();

        while ( i.hasNext() ) {

            Integer id = i.next();
            LegendsSettlement settlement = settlements.get(id);
            if ( settlement.getName().equalsIgnoreCase(name) ) {
                return true;
            }

        }

        return false;

    }

    public LegendsSettlement getSettlementWithName(String name) {

        Iterator<Integer> i = settlements.keySet().iterator();

        while ( i.hasNext() ) {

            Integer id = i.next();
            LegendsSettlement settlement = settlements.get(id);
            if ( settlement.getName().equalsIgnoreCase(name) ) {
                return settlement;
            }

        }

        return null;
        
    }

    public LegendsSettlement removeSettlement(LegendsSettlement settlement) {

        return settlements.remove(new Integer(settlement.getSettlementId()));

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

        File file = new File(Legends.instance.getDataFolder(),"legendsSettlements.json");

        try {
            file.createNewFile();
            DiscUtil.write(file, Legends.gson.toJson(this.settlements));
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

        File file = new File(Legends.instance.getDataFolder(),"legendsSettlements.json");

        try {

            if ( !file.exists() ) {
                return true;
            }

            Type type = new TypeToken<HashMap<Integer,LegendsSettlement>>(){}.getType();
            this.settlements = Legends.gson.fromJson(DiscUtil.read(file), type);

            Iterator<Integer> i = this.settlements.keySet().iterator();

            while ( i.hasNext() ) {

                Integer id = i.next();

                LegendsSettlement settlement = this.settlements.get(id);
                settlement.settlementLoadedFromDisk();

            }

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }

        return true;

    }

}
