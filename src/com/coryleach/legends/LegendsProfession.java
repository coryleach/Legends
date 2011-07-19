/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends;

/**
 *
 * @author cory
 */
public class LegendsProfession {

    protected ProfessionType type;

    public LegendsProfession(ProfessionType type) {
        
        this.type = type;

    }

    //Get name of profession
    public String getName() {
        return stringForProfessionType(this.type);
    }

    //String for type value
    public static String stringForProfessionType(ProfessionType type) {

        switch(type) {
            case MINER:
                return "Miner";
            case FARMER:
                return "Farmer";
            case LUMBERJACK:
                return "Lumberjack";
            case SMITH:
                return "Smith";
        }

        return null;

    }



}
