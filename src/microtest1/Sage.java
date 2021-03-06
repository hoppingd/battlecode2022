package microtest1;

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
    static final int MIN_HEALTH_TO_REINFORCE = 11;

    int birthday;
    int H, W;
    Team myTeam, enemyTeam;

    MapLocation nearestCorner;
    int task = 0;
    RobotInfo[] nearbyEnemies;
    double mapLeadScore;
    MapLocation target;
    MapLocation nearestLoggedEnemy;
    boolean healing = false;

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
        task = comm.getTask();
        nearestLoggedEnemy = comm.getLoggedEnemies();
        comm.readHQloc();
        if (rc.getHealth() == rc.getType().getMaxHealth(rc.getLevel())) {
            healing = false;
        }
        if (!bfs.doMicro()) {
            tryMove();
        }
        tryAttack();
    }

    // TODO: optimize health targeting for sage
    void tryAttack(){
        if (!rc.isActionReady()) return;
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.SAGE.actionRadiusSquared, enemyTeam);
        MapLocation bestLoc = null;
        boolean attackerInRange = false;
        // don't attack miners if soldiers in view
        for (RobotInfo r : nearbyEnemies) {
            if (r.type.canAttack()) {
                comm.writeEnemyToLog(r.location);
                attackerInRange = true;
                break;
            }
        }
        int bestHealth = 10000;
        int bestRubble = GameConstants.MAX_RUBBLE;
        for (RobotInfo r : enemies) {
            MapLocation enemyLoc = r.getLocation();
            boolean isAttacker = r.type.canAttack();
            // if there are attackers, ignore all non-attackers and reset variables
            if (!isAttacker && attackerInRange) continue;
            int rubble = GameConstants.MAX_RUBBLE;
            try {
                rubble = rc.senseRubble(r.location);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            if (isAttacker && !attackerInRange) {
                bestHealth = 10000;
                bestRubble = rubble;
                attackerInRange = true;
            }
            // shoot lowest health with rubble as tiebreaker
            if (r.health < bestHealth) {
                bestHealth = r.health;
                bestRubble = rubble;
                bestLoc = enemyLoc;
            }
            else if (r.health == bestHealth && rubble < bestRubble) {
                bestRubble = rubble;
                bestLoc = enemyLoc;
            }
        }
        try {
            if (bestLoc != null) {
                rc.attack(bestLoc);
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
            case 0: {// scout

                break;
            }
            case 1: {// defensive lattice
                /* chase code
                MapLocation nearbyEnemy = enemyInSight();
                if (nearbyEnemy != null) {
                    bfs.move(nearbyEnemy);
                    return;

                 */
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
                if (target == null) {
                    target = comm.getEmergencyLoc();
                }
                bfs.move(target);
                break;
            }
            case 3: { // explore
                RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, enemyTeam);
                boolean attackerInRange = false;
                for (RobotInfo r : nearbyEnemies) {
                    if (r.type.canAttack()) {
                        attackerInRange = true;
                        break;
                    }
                }
                if (!attackerInRange) {
                    if (rc.getHealth() >= MIN_HEALTH_TO_REINFORCE && !healing) {
                        target = nearestLoggedEnemy;
                    }
                    else {
                        if (!healing) healing = true;
                        if (rc.getLocation().isWithinDistanceSquared(comm.HQloc, RobotType.ARCHON.actionRadiusSquared)) {
                            target = rc.getLocation();
                        }
                        else {
                            target = comm.HQloc;
                        }
                    }
                }
                if (target == null) {
                    target = explore.getExplore3Target();
                }
                bfs.move(target);
                senseEnemyArchons();
                break;
            }
            case 4: { // crunch TODO: improve. get lowest index or nearest non null archon location. bug when archon is destroyed but not crunching.
                task = comm.getTask();
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
    }

    MapLocation getGreedyAttackTile(MapLocation pursuitTarget) {
        MapLocation myLoc = rc.getLocation();
        MapLocation bestLoc = myLoc;
        try {
            int bestRubble = rc.senseRubble(myLoc);
            for (Direction dir : fleeDirections) {
                MapLocation prospect = myLoc.add(dir);
                if (!rc.canMove(dir)) continue; // reduce bytecode?
                if (prospect.distanceSquaredTo(pursuitTarget) <= RobotType.SAGE.visionRadiusSquared) {
                    int r = rc.senseRubble(prospect);
                    // in case of tie, stay at farther range
                    if (r < bestRubble || (r == bestRubble && prospect.distanceSquaredTo(pursuitTarget) > myLoc.distanceSquaredTo(pursuitTarget))) {
                        bestLoc = prospect;
                        bestRubble = r;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.err.println("pursuit target: " + pursuitTarget + ", greedily moved to " + bestLoc);
        return bestLoc;
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
                    if (mapLeadScore < Communication.HIGH_LEAD_THRESHOLD && rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared).length > 9) { // sense should crunch
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

    MapLocation getFreeSpace() { // soldiers will find closest free space
        MapLocation myLoc = rc.getLocation();
        MapLocation space = null;
        try {
            MapLocation[] cells = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells) { // interlinked
                if (!rc.canSenseLocation(cell)) continue; // needed?
                if (rc.senseNearbyRobots(cell, 1, rc.getTeam()).length <= LATTICE_CONGESTION && validLattice(cell)) { // some spacing condition
                    if (space == null) {
                        space = cell;
                    }
                    // closer than target and further from corner than me
                    else if (cell.distanceSquaredTo(myLoc) < space.distanceSquaredTo(myLoc))
                    { // should try to build lattice away from wall/toward enemy
                        space = cell;
                    }
                }
            }
            // no spacing in vision
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return space;
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