package bradenplayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class Watchtower extends MyRobot {

    RobotController rc;
    Team myTeam, enemyTeam;

    Watchtower(RobotController rc) {
        super(rc);
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
    }

    void play() {
        tryAttack();
    }

    void tryAttack() { // will shoot closest target
        MapLocation myLoc = rc.getLocation();
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, enemyTeam);
        MapLocation bestLoc = null;
        int bestDist = 10000;
        for (RobotInfo r : enemies) {
            MapLocation enemyLoc = r.getLocation();
            if (rc.canAttack(r.getLocation())) {
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
}

