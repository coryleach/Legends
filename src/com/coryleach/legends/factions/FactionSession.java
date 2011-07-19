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
public class FactionSession {

    protected String action;

    public String player;
    public String inviteSender;

    public FactionSession parentSession;
    public ArrayList<FactionSession> childSessions;
    public LegendsFaction faction;

    /*public FactionSession() {
        parentSession = null;
        action = null;
        childSessions = new ArrayList<FactionSession>();
    }*/

    public FactionSession(String action,String player) {
        parentSession = null;
        this.action = action;
        childSessions = new ArrayList<FactionSession>();
        this.player = player;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return this.action;
    }


    public FactionSession createInviteSession(String player) {

        FactionSession childSession = new FactionSession("invite",player);
        childSession.parentSession = this;
        childSession.player = player;
        childSession.faction = this.faction;
        this.childSessions.add(childSession);

        return childSession;

    }

    public void broadcast(String message) {

        if ( faction != null ) {
            faction.broadcast(message);
        }
        
    }

    public void cancelAllChildren() {

        Iterator<FactionSession> i = childSessions.iterator();

        while ( i.hasNext() ) {
            
            FactionSession session = i.next();
            //Get the player
            LegendsPlayer legendsPlayer = Legends.instance.getPlayer(session.player);
            //Remove the child session
            FactionsManager.instance.setFactionSessionForPlayer(legendsPlayer, null);
            //notify player of cancelation
            legendsPlayer.message("Faction creation canceled.");

        }

    }

    public void cancelSession() {

        LegendsPlayer legendsPlayer = Legends.instance.getPlayer(player);

        if ( this.getAction().equals("create") ) {

            this.faction.broadcast("Faction creation has been canceled.");

            //Remove all invites
            this.cancelAllChildren();

            //Remove faction from all players who joined
            this.faction.removeAllPlayers();

        } else if ( this.getAction().equals("invite") ) {

            if ( this.parentSession != null ) {

                parentSession.childSessions.remove(this);

            }

        }

        FactionsManager.instance.setFactionSessionForPlayer(legendsPlayer, null);

    }

    public void dissovleAsParent() {

        Iterator<FactionSession> i = childSessions.iterator();

        while ( i.hasNext() ) {

            FactionSession session = i.next();

            session.parentSession = null;

        }

        childSessions.removeAll(childSessions);

    }


}
