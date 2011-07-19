/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.classes;

import com.coryleach.legends.LegendsPlayer;

/**
 *
 * @author Cory
 */
public class LegendsMage extends LegendsClass {

    public LegendsMage(LegendsPlayer player) {
        super(ClassType.MAGE,player);
    }

    @Override
    public float getDamageModifier() {
        return 0.5f + (level * 0.1f);
    }

}
