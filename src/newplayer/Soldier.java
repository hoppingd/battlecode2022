package newplayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Soldier extends MyRobot {

    RobotController rc;
    Pathfinding path;
    MapLocation target;
    MapLocation myLoc;
    boolean inPosition = false;

    Soldier(RobotController rc){
        this.rc = rc;
        path = new Pathfinding(rc);
        target = null;
        myLoc = rc.getLocation();
    }

    void checkCells() {
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells) { // interlinked
                if (!rc.canSenseLocation(cell)) continue; // needed?
                if (rc.senseNearbyRobots(cell, 1, rc.getTeam()).length == 0) { // some spacing condition
                    target = cell;
                    break;
                }
            }
            // no spacing in vision
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void play(){

        if (!inPosition & target == null) {
            checkCells();
        }
        if (target != null) {
            path.move(target);
            myLoc = rc.getLocation();
        }
        if (myLoc == target) {
            inPosition = true;
            target = null;
        }

        // attack anything that comes into range
        if (inPosition) {
            RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
            if (enemies.length > 1) {
                try {
                    if (rc.canAttack(enemies[0].location)) { // don't really care about priority right now
                        rc.attack(enemies[0].location);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

}

