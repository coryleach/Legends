/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coryleach.legends.dungeons;

import org.bukkit.*;
import com.sk89q.worldedit.bukkit.selections.*;

import com.coryleach.legends.*;
import java.util.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;

import org.bukkit.inventory.*;
import org.bukkit.material.*;


/**
 *
 * @author cory
 */
public class DungeonSubzone {

    public String enterMessage;
    protected CuboidSelection cuboid;
    protected transient Dungeon parent;
    protected ArrayList<CreatureType> creatureTypes;
    protected ArrayList<CreaturePrefix> creaturePrefixes;
    public transient ArrayList<LegendsCreature> spawnedMobs;

    public int population;
    
    //Treasure table
    //Monster table

    public DungeonSubzone(CuboidSelection cuboid, Dungeon parentDungeon) {

        this.cuboid = new CuboidSelection(cuboid.getWorld(),cuboid.getMinimumPoint(),cuboid.getMaximumPoint());
        this.parent = parentDungeon;
        this.spawnedMobs = new ArrayList<LegendsCreature>();
        this.population = 4;

        creatureTypes = new ArrayList<CreatureType>();

        creatureTypes.add(CreatureType.ZOMBIE);
        creatureTypes.add(CreatureType.SKELETON);

        creaturePrefixes = new ArrayList<CreaturePrefix>();
        CreaturePrefix prefix = new CreaturePrefix("Dungeon");
        creaturePrefixes.add(prefix);

        ItemStack stack = new ItemStack(Material.COOKIE);
        prefix.drops.add(stack);

    }

    public Dungeon getParent() {
        return parent;
    }

    public void setParent(Dungeon newParent) {
        this.parent = newParent;
    }

    public boolean containsLocation(Location location) {
        return cuboid.contains(location);
    }

    public Location minimumLocation() {
        return cuboid.getMinimumPoint();
    }

    public Location maximumLocation() {
        return cuboid.getMaximumPoint();
    }

    //X-axis
    public int width() {
       return cuboid.getHeight();
    }

    //Y-axis
    public int height() {
        return cuboid.getHeight();
    }

    //Z-axis
    public int length() {
        return cuboid.getLength();
    }

    public Location randomLocation() {

        //Find the most suitable Y
        double minX = minimumLocation().getX();
        double maxX = maximumLocation().getX();

        double minZ = minimumLocation().getZ();
        double maxZ = maximumLocation().getZ();

        double minY = minimumLocation().getY();
        double maxY = maximumLocation().getY();

        ArrayList<Block> list = new ArrayList<Block>();

        for ( double x = minX; x < maxX; x++ ) {
            for ( double z = minZ; z < maxZ; z++ ) {
                for ( double y = minY; y < maxY; y++ ) {

                    //Check if there is air 2 blocks high here
                    if ( y+1 <= 127 ) {

                        Location loc = new Location(parent.getWorld(),x,y,z);
                        Location above = new Location(parent.getWorld(),x,y+1,z);
                        Block block = parent.getWorld().getBlockAt(loc);
                        Block aboveBlock = parent.getWorld().getBlockAt(above);

                        if ( aboveBlock.getType() != Material.AIR ) {
                            y++;
                            continue;
                        } else if ( block.getType() == Material.AIR ) {
                            //Add Block to Possible Spawn locations
                            list.add(block);
                            break;
                        }

                    }

                }
            }
        }

        if ( list.isEmpty() ) {
            Legends.info("Failed to find spawn location within dungeon subzone");
            return null;
        }

        //Get a random block from the list
        int index = randomRange(0,list.size()-1);

        //Return location of this block
        return list.get(index).getLocation();

    }

    public CreatureType randomType() {

        if ( creatureTypes == null || creatureTypes.isEmpty() ) {
            return CreatureType.CHICKEN;
        }

        int index = randomRange(0,creatureTypes.size());
        
        return creatureTypes.get(index);

    }

    public CreaturePrefix randomPrefix() {

        if ( creaturePrefixes == null || creaturePrefixes.isEmpty() ) {
            return null;
        }

        int index = randomRange(0,creaturePrefixes.size());

        return creaturePrefixes.get(index);

    }

    public int randomRange(int Min, int Max) {

        if ( Min == Max ) {
            return Min;
        }

        return Min + (int)(Math.random() * (Max - Min));

    }

    public void spawnMobs() {

        Legends.info("Spawning Mobs in Subzone");

        while( spawnedMobs.size() < this.population ) {

            Location loc = randomLocation();
            CreatureType type = randomType();
            CreaturePrefix prefix = randomPrefix();

            LivingEntity entity = parent.getWorld().spawnCreature(loc, type);
            LegendsCreature creature = Legends.instance.wrapCreature(entity);

            creature.dungeonSubzone = this;
            creature.spawnLocation = loc;
            creature.setPrefix(prefix);

            spawnedMobs.add(creature);

        }

    }

    public void print() {

        Location min = this.minimumLocation();

        int minX = (int)min.getX();
        int minX2 = (int)min.getX() + this.width();

        int minY = (int)min.getY();
        int minY2 = (int)min.getY() + this.height();

        int minZ = (int)min.getZ();
        int minZ2 = (int)min.getZ() + this.length();

        System.out.print("MinX: " + Integer.toString(minX) + " " + Integer.toString(minX2));
        System.out.print("MinY: " + Integer.toString(minY) + " " + Integer.toString(minY2));
        System.out.print("MinZ: " + Integer.toString(minZ) + " " + Integer.toString(minZ2));

    }

    public boolean intersects(DungeonSubzone intersectZone) {

        return this.intersects(intersectZone.cuboid);

    }

    public boolean intersects(CuboidSelection intersectZone) {

        Location min = intersectZone.getMinimumPoint();
        Location max = intersectZone.getMaximumPoint();

        Location localMin = this.minimumLocation();
        Location localMax = this.maximumLocation();


        if ( min.getX() > localMax.getX() ) {
            //minimum X face is greater than the local max X face so no collision
            return false;
        }
        
        if ( max.getX() < localMin.getX() ) {
            //maximum X face is less than the local min X face so no collision possible
            return false;
        }

        if ( min.getY() > localMax.getY() ) {
            //minimum Y face is greater than the local max Y face so no collision
            return false;
        }

        if ( max.getY() < localMin.getY() ) {
            //maximum Y face is less than the local min Y face so no collision possible
            return false;
        }

        if ( min.getZ() > localMax.getZ() ) {
            //minimum Z face is greater than the local max Z face so no collision
            return false;
        }

        if ( max.getZ() < localMin.getZ() ) {
            //maximum Z face is less than the local min Z face so no collision possible
            return false;
        }

        /*Location min = intersectZone.minimumLocation();
        Location localmin = this.minimumLocation();

        int minX = (int)min.getX();
        int minX2 = (int)min.getX() + intersectZone.width();

        int minY = (int)min.getY();
        int minY2 = (int)min.getY() + intersectZone.height();

        int minZ = (int)min.getZ();
        int minZ2 = (int)min.getZ() + intersectZone.length();

        int localMinX = (int)localmin.getX();
        int localMinX2 = (int)localmin.getX() + this.width();

        int localMinY = (int)localmin.getY();
        int localMinY2 = (int)localmin.getY() + this.height();

        int localMinZ = (int)localmin.getZ();
        int localMinZ2 = (int)localmin.getZ() + this.length();

        Legends.info("MinX: " + Integer.toString(minX) + " " + Integer.toString(minX2));
        Legends.info("LocalMinX: " + Integer.toString(localMinX) + " " + Integer.toString(localMinX2));

        Legends.info("MinY: " + Integer.toString(minY) + " " + Integer.toString(minY2));
        Legends.info("LocalMinY: " + Integer.toString(localMinY) + " " + Integer.toString(localMinY2));

        Legends.info("MinZ: " + Integer.toString(minZ) + " " + Integer.toString(minZ2));
        Legends.info("LocalMinZ: " + Integer.toString(localMinZ) + " " + Integer.toString(localMinZ2));

        boolean xIntersect = false;
        //Check x direction intersect
        if ( minX >= localMinX && minX <= localMinX2 ) {
            Legends.info("X Intersect");
            xIntersect = true;
        } else if ( localMinX >= minX && localMinX <= minX2 ) {
            Legends.info("X Intersect");
            xIntersect = true;
        } else {
            Legends.info("No X Intersect");
            return xIntersect;
        }

        boolean yIntersect = false;
        //Check y direction intersect
        if ( minY >= localMinY && minY <= localMinY2 ) {
            Legends.info("Y Intersect");
            yIntersect = true;
        } else if ( localMinY >= minY && localMinY <= minY2 ) {
            Legends.info("Y Intersect");
            yIntersect = true;
        } else {
            Legends.info("No Y Intersect");
            return yIntersect;
        }

        boolean zIntersect = false;
        //Check z direction intersect
        if ( minZ >= localMinZ && minZ <= localMinZ2 ) {
            Legends.info("Z Intersect");
            zIntersect = true;
        } else if ( localMinZ >= minZ && localMinZ <= minZ2 ) {
            Legends.info("Z Intersect");
            zIntersect = true;
        } else {
            Legends.info("No Z Intersect");
            return zIntersect;
        }

        Legends.info("Intersect on all axis");*/
        return true;
        
    }

}
