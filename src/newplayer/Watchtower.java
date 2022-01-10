package newplayer;

import battlecode.common.*;

import java.awt.*;

public class Watchtower extends MyRobot {

    static final int MAX_CONGESTION = 5;
    static final int P3_START = 400;
    Team myTeam, enemyTeam;
    int H, W;
    MapLocation nearestCorner;

    Watchtower(RobotController rc) {
        super(rc);
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        comm.readHQloc();
        nearestCorner = getNearestCorner();
    }

    void play() {
        if (rc.getMode() == RobotMode.TURRET) {
            tryAttack();
            if(shouldGoPortable()) transform();
        }
        if (rc.getMode() == RobotMode.PORTABLE){
            if(isSafe()) tryMove();
            if(shouldGoTurret()) transform();
        }
    }

    void tryMove() {
       MapLocation target = getFreeSpace();
       if (target == null) target = explore.getExploreTarget();
       if (target != null) bfs.move(target);

    }

    void transform() {
        try {
            if (rc.isTransformReady()) rc.transform();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    boolean shouldGoPortable() {
        return rc.senseNearbyRobots(2, myTeam).length > MAX_CONGESTION && isSafe() && rc.getRoundNum() > P3_START;
    }

    //TODO: consider rubble?
    boolean shouldGoTurret() {
        return validTowerLoc(rc.getLocation()) || !isSafe();
    }
    boolean isSafe() {
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.WATCHTOWER.visionRadiusSquared, enemyTeam); // no enemies nearby
        for (RobotInfo r : enemies) {
            if (r.getType() == RobotType.SOLDIER || r.getType() == RobotType.WATCHTOWER || r.getType() == RobotType.SAGE) {
                return false;
            }
        }
        return true;
    }

    void tryAttack() { // shoot lowest health with dist to hq as tiebreaker
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.WATCHTOWER.actionRadiusSquared, enemyTeam);
        MapLocation bestLoc = null;
        int bestHealth = 10000;
        int bestDist = 10000;
        for (RobotInfo r : enemies) {
            MapLocation enemyLoc = r.getLocation();
            if (rc.canAttack(enemyLoc)) {
                int dist = comm.HQloc.distanceSquaredTo(enemyLoc);
                if (r.health < bestHealth) {
                    bestHealth = r.health;
                    bestDist = dist;
                    bestLoc = enemyLoc;
                }
                else if (r.health == bestHealth && dist < bestDist) {
                    bestDist = dist;
                    bestLoc = enemyLoc;
                }
            }
        }
        try {
            if (bestLoc != null) rc.attack(bestLoc);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    boolean validTowerLoc(MapLocation loc) {
        RobotInfo[] robots = rc.senseNearbyRobots(loc,1, myTeam);
        for (RobotInfo r : robots) {
            if (r.getType() == RobotType.WATCHTOWER) return false;
        }
        int d1 = loc.distanceSquaredTo(nearestCorner);
        return d1 >= comm.HQloc.distanceSquaredTo(nearestCorner) && d1 > Math.sqrt(H*W) && rc.senseNearbyRobots(loc, 2, rc.getTeam()).length < 3;
    }

    //TODO: consider rubble?
    MapLocation getFreeSpace() {
        MapLocation myLoc = rc.getLocation();
        MapLocation target = null;
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells) { // interlinked
                if (!rc.canSenseLocation(cell)) continue; // needed?
                if (rc.senseNearbyRobots(cell, 2, rc.getTeam()).length < 3 && validTowerLoc(cell)) { // some spacing condition
                    if (target == null) {
                        target = cell;
                    }
                    else if (cell.distanceSquaredTo(myLoc) < target.distanceSquaredTo(myLoc))
                    { // should try to build lattice away from wall/toward enemy
                        target = cell;
                    }
                }
            }
            // no spacing in vision
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return target;
    }

    MapLocation getNearestCorner() {
        int x;
        int y;
        int W1 = W - 1;
        int H1 = H - 1;
        if(W1 - comm.HQloc.x > comm.HQloc.x) {
            x = 0;
        }
        else {
            x = W1;
        }
        if(H1 - comm.HQloc.y > comm.HQloc.y) {
            y = 0;
        }
        else {
            y = H1;
        }
        return new MapLocation(x,y);
    }

}

