/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.factions;

/**
 *
 * @author Cory
 */
public enum FactionRole {
    CHIEF_ELDER(8, "ChiefElder"),
    ELDER(7, "Elder"),
    LEADER(6, "Leader"),
    COMMANDER(5,"Commander"),
    WARCHIEF(4, "WarChief"),
    RECRUITER(3, "Recruiter"),
    ARCHITECT(2, "Architect"),
    GENERAL(1, "General"),
    RESIDENT(0, "Resident");

    public final int value;
    public final String nicename;

    private FactionRole(final int value, final String nicename) {
        this.value = value;
        this.nicename = nicename;
    }

    @Override
    public String toString() {
            return this.nicename;
    }

    public String getPrefix() {

            /*if (this == Role.ADMIN) {
                    return Conf.prefixAdmin;
            }

            if (this == Role.MODERATOR) {
                    return Conf.prefixMod;
            }*/

            return "";
            
    }

    public boolean canDo(String action) {

        if ( this == ELDER || this == CHIEF_ELDER ) {
            return true;
        }

        if ( action.equals("build") ) {

            switch( this ) {
                case ARCHITECT:
                case COMMANDER:
                case LEADER:
                    return true;
                default:
                    return false;
            }

        } else if ( action.equals("invite") || action.equals("kick") ) {

            switch( this ) {
                case RECRUITER:
                case WARCHIEF:
                case LEADER:
                    return true;
                default:
                    return false;
            }

        } else if ( action.equals("claim") || action.equals("abandon") ) {
            
            switch( this ) {
                case ARCHITECT:
                case COMMANDER:
                case LEADER:
                    return true;
                default:
                    return false;
            }
            
        } else if ( action.equals("war") ) {

            switch( this ) {
                case GENERAL:
                case COMMANDER:
                case WARCHIEF:
                    return true;
                default:
                    return false;
            }

        }

        return false;

    }
    
}
