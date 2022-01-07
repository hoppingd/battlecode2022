package bradenplayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class Soldier extends MyRobot {

    boolean moved = false;

    Team myTeam, enemyTeam;

    public Soldier(RobotController rc){
        super(rc);
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
    }

    public void play(){
        moved = false;
        tryAttack();
        tryMove();
    }

    void tryAttack(){ // will shoot closest target
        MapLocation myLoc = rc.getLocation();
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, enemyTeam);
        MapLocation bestLoc = null;
        int bestDist = 10000;
        for (RobotInfo r : enemies){
            MapLocation enemyLoc = r.getLocation();
            if (rc.canAttack(r.getLocation())){
                int dist = myLoc.distanceSquaredTo(enemyLoc);
                if (dist < bestDist) {
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

    void tryMove(){
        if (moved) return;
        if (rc.senseNearbyRobots(rc.getLocation(), 1, rc.getTeam()).length == 0) { // some spacing condition
            return;
        }
        MapLocation loc = getFreeSpace();

        if (loc != null){
            bfs.move(loc);
            return;
        }
        /*if (rc.getRoundNum() - birthday > exploreRounds){
            if (goToEnemyHQ()) return;
        }*/
        //soldiers dont explore
        //rc.setIndicatorDot(loc, 0, 0, 255);
        return;
    }

    /*boolean goToEnemyHQ(){
        MapLocation ans = null;
        int minDist = -1;
        for (Communication.RInfo r = comm.firstEC; r != null; r = r.nextInfo){
            if (r.getMapLocation() == null) continue;
            if (r.team != rc.getTeam().opponent().ordinal()) continue;
            if (rc.getRoundNum() - r.turnExplored <= EC_DELAY) continue;
            int dist = r.getMapLocation().distanceSquaredTo(rc.getLocation());
            if (minDist < 0 || minDist > dist){
                minDist = dist;
                ans = r.getMapLocation();
            }
        }
        return moveSafely(ans, Util.SAFETY_DISTANCE_ENEMY_EC);
    }*/

    /*void updateECs(){
        for (Communication.RInfo r = comm.firstEC; r != null; r = r.nextInfo){
            if (r.getMapLocation() == null) continue;
            if (r.team != rc.getTeam().opponent().ordinal()) continue;
            int dist = r.getMapLocation().distanceSquaredTo(rc.getLocation());
            if (dist > Util.MUCKRAKER_DIST_EC) continue;
            r.turnExplored = rc.getRoundNum();
        }
    }*/

    MapLocation getFreeSpace(){ // soldiers will find closest free space
        MapLocation myLoc = rc.getLocation();
        MapLocation target = null;
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells) { // interlinked
                if (!rc.canSenseLocation(cell)) continue; // needed?
                if (rc.senseNearbyRobots(cell, 1, rc.getTeam()).length == 0) { // some spacing condition
                    if (target == null) {
                        target = cell;
                    }
                    else if (myLoc.distanceSquaredTo(cell) < myLoc.distanceSquaredTo(target)) {
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

}