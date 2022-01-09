package newplayer;

import battlecode.common.*;

public class Soldier extends MyRobot {

    SoldierScout scout;
    boolean moved = false;
    int birthday;
    int H, W;
    Team myTeam, enemyTeam;

    MapLocation nearestCorner;
    int task = 0;

    public Soldier(RobotController rc){
        super(rc);
        scout = new SoldierScout(rc, comm);
        birthday = rc.getRoundNum();
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        comm.readHQloc();
        nearestCorner = getNearestCorner();
    }

    public void play(){
        moved = false;
        tryAttack();
        tryMove();
    }

    MapLocation enemyInSight(){ // soldiers will move toward enemies
        MapLocation myLoc = rc.getLocation();
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, enemyTeam);
        MapLocation bestLoc = null;
        int bestDist = 10000;
        for (RobotInfo r : enemies){
            MapLocation enemyLoc = r.getLocation();
            int dist = myLoc.distanceSquaredTo(enemyLoc);
            if (dist < bestDist && dist > rc.getType().actionRadiusSquared) {
                bestDist = dist;
                bestLoc = enemyLoc;
            }
        }
        return bestLoc;
    }

    void tryAttack(){ // will shoot closest target
        MapLocation myLoc = rc.getLocation();
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, enemyTeam);
        MapLocation bestLoc = null;
        int bestDist = 10000;
        for (RobotInfo r : enemies){
            MapLocation enemyLoc = r.getLocation();
            if (rc.canAttack(enemyLoc)){
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
        if (rc.getRoundNum() == birthday) {
            task = comm.getTask();
        }
        switch (task) {
            case 0: {//scout
                if (comm.readHQloc()) {
                    MapLocation loc = scout.getProspect();
                    bfs.move(loc);
                    if (scout.checkProspect(loc)) {
                        //System.err.println("should be changing tasks");
                        task = comm.getTask(); // get new task
                    }
                } else {
                    MapLocation loc = getFreeSpace();
                    if (loc != null) {
                        bfs.move(loc);
                        return;
                    }
                }
                break;
            }
            case 1: {// defensive lattice
                task = comm.getTask(); // update task in case of emergency or mass attack
                /* chase code
                MapLocation nearbyEnemy = enemyInSight();
                if (nearbyEnemy != null) {
                    bfs.move(nearbyEnemy);
                    return;

                 */
                if (rc.senseNearbyRobots(rc.getLocation(), 1, rc.getTeam()).length == 0) { // some spacing condition
                    return;
                }
                MapLocation loc = getFreeSpace();

                if (loc != null) {
                    bfs.move(loc);
                    return;
                }
                break;
            }
            case 2: {// emergency
                bfs.move(comm.getEmergencyLoc());
            }
            default:
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

    boolean validLattice(MapLocation loc) {
        int d1 = loc.distanceSquaredTo(nearestCorner);
        return d1 > Math.sqrt(Math.sqrt(H*W)) && d1 > comm.HQloc.distanceSquaredTo(nearestCorner);
    }

    MapLocation getFreeSpace(){ // soldiers will find closest free space
        MapLocation myLoc = rc.getLocation();
        MapLocation target = null;
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells) { // interlinked
                if (!rc.canSenseLocation(cell)) continue; // needed?
                if (rc.senseNearbyRobots(cell, 1, rc.getTeam()).length == 0 && validLattice(cell)) { // some spacing condition
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