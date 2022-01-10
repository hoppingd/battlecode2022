package newplayer;

import battlecode.common.*;

public class Soldier extends MyRobot {

    static final int LATTICE_CONGESTION = 0;
    static final int ALLY_FORCES_RANGE = 25;

    SoldierScout scout;
    boolean moved = false;
    int birthday;
    int H, W;
    Team myTeam, enemyTeam;

    MapLocation nearestCorner;
    int task = 0;
    int crunchIdx = 0;
    RobotInfo[] nearbyEnemies;

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
        nearbyEnemies = rc.senseNearbyRobots(RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
    }

    public void play(){
        moved = false;
        tryAttack();
        tryMove();
        nearbyEnemies = rc.senseNearbyRobots(RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
        tryAttack();
    }

    // TODO: implement a function to stay at max range from soldiers and flee from losing fights, pursuing winning fights
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

    void tryAttack(){ // shoot lowest health with dist as tiebreaker
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.SOLDIER.actionRadiusSquared, enemyTeam);
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

    // TODO: cleanup
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
                MapLocation target = moveInCombat();
                if (target != null) {
                    bfs.move(comm.HQloc);
                    return;
                }
                MapLocation myLoc = rc.getLocation();
                if (rc.senseNearbyRobots(myLoc, 1, rc.getTeam()).length <= LATTICE_CONGESTION && validLattice(myLoc)) { // some spacing condition
                    return;
                }
                target = getFreeSpace();
                if (target != null) {
                    bfs.move(target);
                    return;
                }
                break;
            }
            case 2: {// emergency
                MapLocation target = moveInCombat();
                if (target == null) {
                    target = comm.getEmergencyLoc();
                }
                bfs.move(comm.getEmergencyLoc());
                break;
            }
            case 3: { // explore
                task = comm.getTask();
                MapLocation target = moveInCombat();
                if (target == null) {
                    target = explore.getExploreTarget();
                }
                bfs.move(target);
                senseEnemyArchons();
                break;
            }
            case 4: { // crunch
                task = comm.getTask();
                MapLocation target = moveInCombat();
                comm.readEnemyArchonLocations();
                if (comm.enemyArchons[crunchIdx] != null) {
                    if (target == null) {
                        target = comm.enemyArchons[crunchIdx];
                    }
                    bfs.move(target);
                    try {
                        if (rc.canSenseRobotAtLocation(comm.enemyArchons[crunchIdx])) {
                            if (rc.senseRobotAtLocation(comm.enemyArchons[crunchIdx]).type != RobotType.ARCHON) crunchIdx = (crunchIdx + 1) % comm.numArchons;
                        }
                        else {
                            crunchIdx = (crunchIdx + 1) % comm.numArchons;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                else {
                    bfs.move(explore.getExploreTarget()); // shouldnt just explore, we should look for hq at specific locations
                }
                senseEnemyArchons();
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

    //TODO: improve
    MapLocation moveInCombat() {
        MapLocation pursuitTarget = null;
        int lowestHealth = 40000;
        for (RobotInfo enemy : nearbyEnemies) {
            // only consider offensive units
            //TODO: only consider combat units, with more weight given to watchtowers
            int myForcesCount = rc.getHealth();
            RobotInfo[] myForces = rc.senseNearbyRobots(enemy.location, ALLY_FORCES_RANGE, myTeam);
            for (RobotInfo r : myForces) {
                if (r.type.canAttack()) {
                    myForcesCount += r.health;
                }
            }
            int enemyForcesCount = 0;
            if (enemy.type.canAttack()) enemyForcesCount = enemy.health;
            RobotInfo[] enemyForces = rc.senseNearbyRobots(enemy.location, RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
            for (RobotInfo r : enemyForces) {
                if (r.type.canAttack()) {
                    enemyForcesCount += r.health;
                }
            }
            if (myForcesCount < enemyForcesCount) {
                return comm.HQloc; //for now we naively path home
            }
            else if (enemy.health < lowestHealth) {
                lowestHealth = enemy.health;
                pursuitTarget = enemy.location;
            }
        }
        return pursuitTarget;
    }

    void senseEnemyArchons() { // check for enemy archon and write
        for (RobotInfo r : nearbyEnemies) {
            if (r.getType() == RobotType.ARCHON) {
                comm.writeEnemyArchonLocation(r);
            }
        }
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

    MapLocation getFreeSpace() { // soldiers will find closest free space
        MapLocation myLoc = rc.getLocation();
        MapLocation target = null;
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells) { // interlinked
                if (!rc.canSenseLocation(cell)) continue; // needed?
                if (rc.senseNearbyRobots(cell, 1, rc.getTeam()).length <= LATTICE_CONGESTION && validLattice(cell)) { // some spacing condition
                    if (target == null) {
                        target = cell;
                    }
                    // closer than target and further from corner than me
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