package dpsplayer;

import battlecode.common.*;

public class Soldier extends MyRobot {

    static final Direction[] moveDirections = {
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
    static final int MAX_TURNS_HEALING = 75;

    int birthday;
    int H, W;
    Team myTeam, enemyTeam;

    MapLocation nearestCorner;
    int task = 0;
    double mapLeadScore;
    MapLocation target;
    MapLocation nearestLoggedEnemy;
    boolean healing = false;
    int turnsHealing = 0;

    public Soldier(RobotController rc){
        super(rc);
        birthday = rc.getRoundNum();
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        comm.readHQloc();
        nearestCorner = getNearestCorner();
        mapLeadScore = (comm.leadScore / (double)comm.numArchons) * (400.0/(H*W));
    }

    public void play() {
        target = null;
        MapLocation newNearest = comm.getLoggedEnemies();
        if (newNearest != null) {
            nearestLoggedEnemy = newNearest;
        }
        comm.readHQloc();
        task = comm.getTask();
        // finished healing
        if (rc.getHealth() == rc.getType().getMaxHealth(rc.getLevel())) {
            healing = false;
            turnsHealing = 0;
        }
        if (!bfs.doMicro()) {
            tryMove();
        }
        tryAttack();
        // if attacking, don't risk disintegration
        if (!rc.isActionReady()) turnsHealing = 0;
    }

    //TODO: prioritize builders over miners
    void tryAttack(){
        if (!rc.isActionReady()) return;
        //RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.SOLDIER.actionRadiusSquared, enemyTeam);
        MapLocation bestLoc = null;
        boolean attackerInRange = false;
        // don't attack miners if soldiers in view
        for (RobotInfo r : enemies) {
            if (r.type.canAttack()) {
                comm.writeEnemyToLog(r.location);
                attackerInRange = true;
            }
            else if (r.getType() == RobotType.BUILDER) {
                comm.setTask(4);
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
        rc.setIndicatorString("tryMove");
        if (!rc.isMovementReady()) return;
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
                RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
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
                            //System.err.println("healing...");
                            turnsHealing++;
                            if (turnsHealing >= MAX_TURNS_HEALING) {
                                target = getMineProspect();
                            }
                            else {
                                target = rc.getLocation();
                            }
                        }
                        else {
                            target = comm.HQloc;
                            //System.err.println("retreating...");
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
                // heal
                RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
                boolean attackerInRange = false;
                for (RobotInfo r : nearbyEnemies) {
                    if (r.type.canAttack()) {
                        attackerInRange = true;
                        break;
                    }
                }
                if (!attackerInRange) {
                    if (rc.getHealth() >= MIN_HEALTH_TO_REINFORCE && !healing) {
                        // do nothing
                    }
                    else {
                        if (!healing) healing = true;
                        if (rc.getLocation().isWithinDistanceSquared(comm.HQloc, RobotType.ARCHON.actionRadiusSquared)) {
                            //System.err.println("healing...");
                            turnsHealing++;
                            if (turnsHealing >= MAX_TURNS_HEALING) {
                                target = getMineProspect();
                            }
                            else {
                                target = rc.getLocation();
                            }
                        }
                        else {
                            target = comm.HQloc;
                            //System.err.println("retreating...");
                        }
                    }
                }
                if (target != null) {
                    bfs.move(target);
                    return;
                }
                // crunch
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

    void senseEnemyArchons() { // check for enemy archon and write
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
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

    MapLocation getMineProspect() {
        MapLocation myLoc = rc.getLocation();
        // consider giving up if too far away
        MapLocation target = null;
        int bestDist = 10000;
        try {
            if (rc.isActionReady() && rc.senseLead(rc.getLocation()) == 0) {
                rc.disintegrate();
            }
            MapLocation[] cells = rc.getAllLocationsWithinRadiusSquared(myLoc, RobotType.SOLDIER.visionRadiusSquared);
            for (MapLocation cell : cells){
                if (rc.senseLead(cell) > 0 || (cell.distanceSquaredTo(comm.HQloc) >  RobotType.ARCHON.visionRadiusSquared)) continue;
                int dist = myLoc.distanceSquaredTo(cell);
                if (dist < bestDist) { // closest spot with no lead within turtle radius
                    bestDist = dist;
                    target = cell;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (target == null) return comm.HQloc;
        return target;
    }

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