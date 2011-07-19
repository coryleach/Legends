/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.factions;

import com.coryleach.legends.Legends;
import com.coryleach.legends.LegendsChunk;
import com.coryleach.legends.LegendsMap;
import org.bukkit.*;

/**
 *
 * @author Cory
 */
public class ChunkLocation {

    protected int x;
    protected int z;
    protected String world;

    public ChunkLocation(int x, int z, String world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }


    public ChunkLocation(Chunk chunk) {
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.world = chunk.getWorld().getName();
    }

    public ChunkLocation(Location location) {
        this(location.getWorld().getChunkAt(location));
    }

    public ChunkLocation(LegendsChunk chunk) {
        this(chunk.getChunk());
    }

    public String getWorld() {

        if ( world == null ) {
            world = "world";
        }

        return world;

    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public LegendsChunk getLegendsChunk() {
        LegendsMap map = Legends.instance.getMapForWorld(world);
        return map.getChunkAt(x, z);
    }

    public Chunk getChunk() {
        return getLegendsChunk().getBukkitChunk();
    }

    public boolean isAdjacent(ChunkLocation location) {

        //Check if world
        if ( !location.getWorld().equals(this.world) ) {
            return false;
        }

        if ( location.x == this.x ) {

            if ( location.z == this.z - 1 || location.z == this.z + 1 ) {
                return true;
            }

        } else if ( location.z == this.z ) {

            if ( location.x == this.x - 1 || location.x == this.x + 1 ) {
                return true;
            }

        }

        return false;

    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 19 * hash + x;
        hash = 19 * hash + z;
        return hash;

    }

    @Override
    public boolean equals(Object o) {

        if ( o instanceof ChunkLocation ) {

            ChunkLocation loc = (ChunkLocation)o;

            if ( loc.x == this.x && loc.z == this.z ) {
                return true;
            }

        }

        return false;

    }

    @Override
    public String toString() {
        return "(" + Integer.toString(x) + "," + Integer.toString(z) + ")";
    }

}
