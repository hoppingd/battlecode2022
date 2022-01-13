package sageplayer;

import battlecode.common.*;

public class Sage extends MyRobot {

    static final Direction[] fleeDirections = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static final int LATTICE_CONGESTION = 0;

    int birthday;
    int H, W;
    Team myTeam, enemyTeam;

    MapLocation nearestCorner;
    int task = 0;
    RobotInfo[] nearbyEnemies;
    boolean attacked = false;
    double mapLeadScore;

    public Sage(RobotController rc){
        super(rc);
        birthday = rc.getRoundNum();
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        comm.readHQloc();
        nearestCorner = getNearestCorner();
        nearbyEnemies = rc.senseNearbyRobots(RobotType.SAGE.visionRadiusSquared, enemyTeam);
        mapLeadScore = (comm.leadScore / (double)comm.numArchons) * (400.0/(H*W));
    }

    public void play() {
        attacked = false;
        tryAttack();
        tryMove();
        nearbyEnemies = rc.senseNearbyRobots(RobotType.SAGE.visionRadiusSquared, enemyTeam);
        tryAttack();

    }

    void tryAttack(){ // shoot lowest health with dist as tiebreaker
        if (attacked) return;
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.SAGE.actionRadiusSquared, enemyTeam);
        MapLocation myLoc = rc.getLocation();
        MapLocation bestLoc = null;
        int bestHealth = 10000;
        int bestDist = 10000;
        for (RobotInfo r : enemies) {
            if (!r.type.canAttack()) continue; // ignore non offensive units-- waste of 200 cd
            MapLocation enemyLoc = r.getLocation();
            if (rc.canAttack(enemyLoc)) {
                int dist = myLoc.distanceSquaredTo(enemyLoc);
                //TODO: fix
                if (r.health > RobotType.SAGE.damage && r.health < bestHealth) {
                    bestHealth = r.health;
                    bestDist = dist;
                    bestLoc = enemyLoc;
                }
                else if (r.health == bestHealth && dist < bestDist) {
                    bestDist = dist;
                    bestLoc = enemyLoc;
                }
                else if (r.health < RobotType.SAGE.damage) {
                    bestHealth = r.health;
                    bestDist = dist;
                    bestLoc = enemyLoc;
                }
            }
        }
        try {
            if (bestLoc != null) {
                rc.attack(bestLoc);
                attacked = true;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }


    }

    // TODO: cleanup
    void tryMove(){
        if (!rc.isMovementReady()) return;
        if (rc.getRoundNum() == birthday) {
            task = comm.getTask();
        }
        switch (task) {
            case 0:
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
                    bfs.move(target);
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
                bfs.move(target);
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
                MapLocation target = moveInCombat();
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
            default:
        }

        /*if (rc.getRoundNum() - birthday > exploreRounds){
            if (goToEnemyHQ()) return;
        }*/
        //SAGEs dont explore
        //rc.setIndicatorDot(loc, 0, 0, 255);
        return;
    }

    //TODO: improve
    MapLocation moveInCombat() {
        MapLocation pursuitTarget = null;
        MapLocation myLoc = rc.getLocation();
        int lowestHealth = 40000;
        for (RobotInfo enemy : nearbyEnemies) {
            // only consider offensive units
            //TODO: consider sage range
            if (enemy.type.canAttack()) {
                if (!rc.isActionReady() && myLoc.distanceSquaredTo(enemy.location) <= enemy.type.visionRadiusSquared) {
                    explore.reset();
                    return flee(enemy.location); // flee;
                }
            }
            // TODO: fix
            if ((lowestHealth > RobotType.SAGE.damage && enemy.health < lowestHealth) || (lowestHealth < RobotType.SAGE.damage && enemy.health > lowestHealth)) { //consider staying put if winning
                lowestHealth = enemy.health;
                pursuitTarget = enemy.location;
            }
        }
        return pursuitTarget;
    }

    // flees to the lowest rubble tile away from enemy
    MapLocation flee(MapLocation enemy) {
        MapLocation myLoc = rc.getLocation();
        int bestRubble = GameConstants.MAX_RUBBLE;
        MapLocation bestLoc = null;
        int d1 = myLoc.distanceSquaredTo(enemy);
        try {
            for (Direction dir : fleeDirections) {
                MapLocation prospect = myLoc.add(dir);
                if (!(rc.onTheMap(prospect))) continue; // reduce bytecode?
                if (prospect.distanceSquaredTo(enemy) > d1) {
                    int r = rc.senseRubble(prospect);
                    if (r < bestRubble) {
                        bestLoc = prospect;
                        bestRubble = r;
                    }
                    //TODO: tiebreak with distance
                }

            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return bestLoc;
    }

    void senseEnemyArchons() { // check for enemy archon and write
        for (RobotInfo r : nearbyEnemies) {
            if (r.getType() == RobotType.ARCHON) {
                comm.writeEnemyArchonLocation(r);
                try {
                    if (mapLeadScore < comm.HIGH_LEAD_THRESHOLD && rc.getRoundNum() < 500 && rc.senseNearbyLocationsWithLead(RobotType.SAGE.visionRadiusSquared).length > 12) { // sense not rush
                        comm.setTask(4); // RUSH!
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }

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

    MapLocation getFreeSpace() { // SAGEs will find closest free space
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
        MapLocation nearestCorner = new MapLocation(x,y);
        int d1 = comm.HQloc.distanceSquaredTo(nearestCorner);
        // if not near corner, build around HQ
        if (comm.HQloc.distanceSquaredTo(new MapLocation(x, H1/2)) < d1 || comm.HQloc.distanceSquaredTo(new MapLocation(W1/2, y)) < d1) {
            nearestCorner = comm.HQloc;
        }
        return nearestCorner;
    }
}

