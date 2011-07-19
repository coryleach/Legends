/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends;

import java.util.*;
import org.bukkit.*;

/**
 *
 * @author Cory
 */
public class LegendsSettings {

    protected HashMap<String,String> settings;

    public Location tutorialLocation;

    public LegendsSettings() {
        settings = new HashMap<String,String>();
    }

    public void set(String key,String value) {
        this.settings.put(key, value);
    }

    public String get(String key) {
        return this.settings.get(key);
    }

}
