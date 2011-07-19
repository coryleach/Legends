/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.factions;

import com.coryleach.legends.Legends;
import com.coryleach.legends.LegendsPlayer;

import java.util.*;

/**
 *
 * @author Cory
 */
public class FactionPlayer {

    protected FactionRole role;
    protected String playerName;
    protected String factionName;
    protected String title;
    protected HashMap<String,String> properties;

    public FactionPlayer() {
        role = FactionRole.RESIDENT;
        properties = new HashMap<String,String>();
    }

    public FactionPlayer(String name, FactionRole role) {
        this.role = role;
        this.playerName = name;
        properties = new HashMap<String,String>();
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public LegendsPlayer getPlayer() {

        if ( this.playerName == null ) {
            return null;
        }

        return Legends.instance.getPlayer(this.playerName);

    }

    public HashMap<String,String> getProperties() {

        if ( properties == null ) {
            properties = new HashMap<String,String>();
        }

        return properties;

    }

    public void setProperty(String property,String value) {
        getProperties().put(property, value);
    }

    public void setProperty(String property,boolean value) {
        getProperties().put(property, String.valueOf(value));
    }

    public boolean getProperty(String property) {

        String value = getProperties().get(property);

        if ( value == null ) {
            return false;
        }

        return Boolean.parseBoolean(value);

    }

    public FactionRole getRole() {
        return role;
    }

    public void setRole(FactionRole role) {
        this.role = role;
    }

    public boolean isOnline() {

        if ( this.getPlayer() == null ) {
            return false;
        }

        return true;

    }

    public boolean isOffline() {
        return !isOnline();
    }

    public String getTitle() {
        return role.toString();
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
