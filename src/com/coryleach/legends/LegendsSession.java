/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends;

/**
 *
 * @author Cory
 */
public class LegendsSession {

    public String action;
    public int amount;
    public int price;
    public int currentSettlement;

    public boolean isAction(String string) {

        if ( action == null ) {
            return false;
        }

        return action.equalsIgnoreCase(string);

    }

}
