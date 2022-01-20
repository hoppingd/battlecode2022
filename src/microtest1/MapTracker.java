package microtest1;

import battlecode.common.MapLocation;

public class MapTracker {

    final static int INT_BITS = 32;
    final static int ARRAY_SIZE = 128;

    static int[] visitedLocations = new int[ARRAY_SIZE];


    MapTracker(){
    }

    void reset(){
        visitedLocations = new int[ARRAY_SIZE];
    }

    void add(MapLocation loc){
        int arrayPos = (loc.x)*(1 + (loc.y)/INT_BITS);
        int bitPos = loc.y%INT_BITS;
        visitedLocations[arrayPos] |= (1 << bitPos);
    }

    boolean check(MapLocation loc){
        int arrayPos = (loc.x)*(1 + (loc.y)/INT_BITS);
        int bitPos = loc.y%INT_BITS;
        return ((visitedLocations[arrayPos] & (1 << bitPos)) > 0);
    }

}