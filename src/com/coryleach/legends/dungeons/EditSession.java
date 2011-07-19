/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.dungeons;

import org.bukkit.entity.*;
import com.coryleach.legends.*;

/**
 *
 * @author cory
 */
public class EditSession {

    protected LegendsPlayer player;
    protected Dungeon dungeon;

    public EditSession(LegendsPlayer player, Dungeon dungeon) {

        this.player = player;
        this.dungeon = dungeon;

    }

    public LegendsPlayer getPlayer() {
        return player;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

}
