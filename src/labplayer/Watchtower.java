package labplayer;

import battlecode.common.*;

import java.awt.*;

public class Watchtower extends MyRobot {

    static final int MAX_CONGESTION = 5;
    static final int ALLY_FORCES_RANGE = 29;

    Team myTeam, enemyTeam;
    int H, W;
    MapLocation nearestCorner;
    int task = 0;

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
        task = comm.getTask();
        switch (task) {
            case 0:
            case 1:
            case 3: {
                if (rc.getMode() == RobotMode.TURRET) {
                    tryAttack();
                    if(shouldGoPortable()) transform();
                }
                if (rc.getMode() == RobotMode.PORTABLE){
                    if(isSafe()) tryMove();
                    if(shouldGoTurret()) transform();
                }
                break;
            }
            case 2: { //TODO: in emergencies, towers should move and try to help
                if (rc.getMode() == RobotMode.PORTABLE){
                    transform();
                }
                tryAttack();
                break;
            }
            case 4:
            {
                //TODO: FIX
                MapLocation target = null;
                comm.readEnemyArchonLocations();
                int crunchIdx = comm.getCrunchIdx();
                if (comm.enemyArchons[crunchIdx] != null) {
                    if (target == null) {
                        target = comm.enemyArchons[crunchIdx];
                    }
                    bfs.move(target);
                    try {
                        if (rc.canSenseLocation(comm.enemyArchons[crunchIdx])) {
                            boolean targetInRange = rc.canSenseRobotAtLocation(comm.enemyArchons[crunchIdx]);
                            if (!targetInRange || rc.senseRobotAtLocation(comm.enemyArchons[crunchIdx]).type != RobotType.ARCHON) { // archon is dead or has moved
                                comm.wipeEnemyArchonLocation(crunchIdx);
                                comm.incCrunchIdx();
                            }
                            // if archon, don't update crunch index
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                else {
                    comm.incCrunchIdx(); // we are checking the wrong index, so increment
                    if (target == null) {
                        target = explore.getExploreTarget();
                    }
                    bfs.move(target);
                }
                senseEnemyArchons();
            }
        }

    }

    void senseEnemyArchons() { // check for enemy archon and write
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.WATCHTOWER.visionRadiusSquared, enemyTeam);
        for (RobotInfo r : enemies) {
            if (r.getType() == RobotType.ARCHON) {
                comm.writeEnemyArchonLocation(r);
            }
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
        return rc.senseNearbyRobots(2, myTeam).length > MAX_CONGESTION && isSafe() && rc.getRoundNum() > comm.P3_START;
    }

    //TODO: consider rubble?
    boolean shouldGoTurret() {
        return validTowerLoc(rc.getLocation()) || !isSafe();
    }

    //TODO: improve
    boolean isSafe() {
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.WATCHTOWER.visionRadiusSquared, enemyTeam);
        for (RobotInfo enemy : enemies) {
            // only consider offensive units
            if (!enemy.type.canAttack()) continue;
            //TODO: only consider combat units, with more weight given to watchtowers
            int myForcesCount = 0;
            if (rc.getMode() == RobotMode.TURRET) myForcesCount = rc.getHealth(); // may not be needed
            RobotInfo[] myForces = rc.senseNearbyRobots(enemy.location, ALLY_FORCES_RANGE, myTeam);
            for (RobotInfo r : myForces) {
                if (r.type.canAttack()) {
                    myForcesCount += r.health;
                }
            }
            int enemyForcesCount = enemy.health;
            RobotInfo[] enemyForces = rc.senseNearbyRobots(enemy.location, RobotType.WATCHTOWER.visionRadiusSquared, enemyTeam);
            for (RobotInfo r : enemyForces) {
                if (r.type.canAttack()) {
                    enemyForcesCount += r.health;
                }
            }
            if (myForcesCount < enemyForcesCount) {
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
        MapLocation nearestCorner = new MapLocation(x,y);
        int d1 = comm.HQloc.distanceSquaredTo(nearestCorner);
        // if not near corner, build around HQ
        if (comm.HQloc.distanceSquaredTo(new MapLocation(x, H1/2)) < d1 || comm.HQloc.distanceSquaredTo(new MapLocation(W1/2, y)) < d1) {
            nearestCorner = comm.HQloc;
        }
        return nearestCorner;
    }

}

