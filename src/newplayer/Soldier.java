package newplayer;

import battlecode.common.*;

public class Soldier extends MyRobot {


    final int EXPLORER_1_TYPE = 0;
    final int EXPLORER_2_TYPE = 1;
    final int ATTACKER_TYPE = 2;
    final int EXPLORE_2_BYTECODE_REMAINING = 2000;

    int myType;
    boolean moved = false;

    int exploreRounds;

    Team myTeam, enemyTeam;

    int birthday;

    final static int EC_DELAY = 100;

    public Soldier(RobotController rc){
        super(rc);
        myType = EXPLORER_1_TYPE;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        birthday = rc.getRoundNum();
    }

    public void play(){
        moved = false;
        tryAttack();
        tryMove();
        //updateECs();
    }

    void tryAttack(){
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, enemyTeam);
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

    MapLocation getFreeSpace(){
        MapLocation myLoc = rc.getLocation();
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells) { // interlinked
                if (!rc.canSenseLocation(cell)) continue; // needed?
                if (rc.senseNearbyRobots(cell, 1, rc.getTeam()).length == 0) { // some spacing condition
                    return cell;
                }
            }
            // no spacing in vision
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

}