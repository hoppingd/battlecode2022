package bradenplayer;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.HashSet;

public class Pathfinding {

    RobotController rc;
    MapLocation target = null;
    double avgRubble = 100;

    BugNav bugNav = new BugNav();
    Exploration explore;

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.CENTER
    };

    boolean[] impassable = null;

    void setImpassable(boolean[] impassable){
        this.impassable = impassable;
    }

    void initTurn(){
        impassable = new boolean[directions.length];
    }

    boolean canMove(Direction dir){
        if (!rc.canMove(dir)) return false;
        if (impassable[dir.ordinal()]) return false;
        return true;
    }


    Pathfinding(RobotController rc, Exploration explore){
        this.rc = rc;
        this.explore = explore;
    }

    double getEstimation (MapLocation loc){
        try {
            if (loc.distanceSquaredTo(target) == 0) return 0;
            int d = Util.distance(target, loc);
            double r = rc.senseRubble(loc);
            return r + (d - 1)*avgRubble;
        } catch (Throwable e){
            e.printStackTrace();
        }
        return 1e9;
    }

    public void move(MapLocation loc){
        if (rc.getMovementCooldownTurns() >= 1) return;
        target = loc;

        //rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 0);

        if (!bugNav.move()) greedyPath();
        bugNav.move();
    }

    final double eps = 1e-5;

    void greedyPath(){
        try {
            MapLocation myLoc = rc.getLocation();
            Direction bestDir = null;
            double bestEstimation = 0;
            double firstStep = rc.senseRubble(myLoc);
            int contRubble = 0;
            int bestEstimationDist = 0;
            double avgR = 0;
            for (Direction dir : directions) {
                MapLocation newLoc = myLoc.add(dir);
                if (!rc.onTheMap(newLoc)) continue;

                //pass
                avgR += rc.senseRubble(newLoc);
                ++contRubble;


                if (!canMove(dir)) continue;
                if (!strictlyCloser(newLoc, myLoc, target)) continue;

                int newDist = newLoc.distanceSquaredTo(target);

                double estimation = firstStep + getEstimation(newLoc);
                if (bestDir == null || estimation < bestEstimation - eps || (Math.abs(estimation - bestEstimation) <= 2*eps && newDist < bestEstimationDist)) {
                    bestEstimation = estimation;
                    bestDir = dir;
                    bestEstimationDist = newDist;
                }
            }
            if (contRubble != 0) {
                avgRubble = avgR / contRubble;
            }
            if (bestDir != null) rc.move(bestDir);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    boolean strictlyCloser(MapLocation newLoc, MapLocation oldLoc, MapLocation target){
        int dOld = Util.distance(target, oldLoc), dNew = Util.distance(target, newLoc);
        if (dOld < dNew) return false;
        if (dNew < dOld) return true;
        return target.distanceSquaredTo(newLoc) < target.distanceSquaredTo(oldLoc);

    }

    class BugNav{

        BugNav(){}

        final int INF = 1000000;

        boolean rotateRight = true; //if I should rotate right or left
        MapLocation lastObstacleFound = null; //latest obstacle I've found in my way
        int minDistToEnemy = INF; //minimum distance I've been to the enemy while going around an obstacle
        MapLocation prevTarget = null; //previous target
        HashSet<Integer> visited = new HashSet<>();

        boolean move() {
            try{

                //different target? ==> previous data does not help!
                if (prevTarget == null || target.distanceSquaredTo(prevTarget) > 0) resetPathfinding();

                //If I'm at a minimum distance to the target, I'm free!
                MapLocation myLoc = rc.getLocation();
                int d = myLoc.distanceSquaredTo(target);
                if (d <= minDistToEnemy) resetPathfinding();

                int code = getCode();

                if (visited.contains(code)) resetPathfinding();
                visited.add(code);

                //Update data
                prevTarget = target;
                minDistToEnemy = Math.min(d, minDistToEnemy);

                //If there's an obstacle I try to go around it [until I'm free] instead of going to the target directly
                Direction dir = myLoc.directionTo(target);
                if (lastObstacleFound != null) dir = myLoc.directionTo(lastObstacleFound);
                if (canMove(dir)){
                    resetPathfinding();
                }

                //I rotate clockwise or counterclockwise (depends on 'rotateRight'). If I try to go out of the map I change the orientation
                //Note that we have to try at most 16 times since we can switch orientation in the middle of the loop. (It can be done more efficiently)
                for (int i = 8; i-- > 0;) {
                    if (canMove(dir)) {
                        rc.move(dir);
                        return true;
                    }
                    MapLocation newLoc = myLoc.add(dir);
                    if (!rc.onTheMap(newLoc)) rotateRight = !rotateRight;
                        //If I could not go in that direction and it was not outside of the map, then this is the latest obstacle found
                    else lastObstacleFound = myLoc.add(dir);
                    if (rotateRight) dir = dir.rotateRight();
                    else dir = dir.rotateLeft();
                }

                if (canMove(dir)) rc.move(dir);
            } catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }

        //clear some of the previous data
        void resetPathfinding(){
            lastObstacleFound = null;
            minDistToEnemy = INF;
            visited.clear();
        }

        int getCode(){
            int x = rc.getLocation().x;
            int y = rc.getLocation().y;
            Direction obstacleDir = rc.getLocation().directionTo(target);
            if (lastObstacleFound != null) obstacleDir = rc.getLocation().directionTo(lastObstacleFound);
            int bit = rotateRight ? 1 : 0;
            return (((((x << 6) | y) << 4) | obstacleDir.ordinal()) << 1) | bit;
        }
    }


}