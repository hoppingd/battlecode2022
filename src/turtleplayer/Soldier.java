package turtleplayer;

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

    static final int ALLY_FORCES_RANGE = 29;
    static final int MIN_HEALTH_TO_FIGHT = 7;

    int birthday;
    int H, W;
    Team myTeam, enemyTeam;

    MapLocation nearestCorner;
    MapLocation reinforceLoc;
    MapLocation lastReinforceLoc;

    int task = 0;
    RobotInfo[] nearbyEnemies;
    double mapLeadScore;
    double turtleRadius;
    int LATTICE_MOD = 0;
    int currRound = 0;


    public Soldier(RobotController rc){
        super(rc);
        birthday = rc.getRoundNum();
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        comm.readHQloc();
        nearestCorner = getNearestCorner();
        lastReinforceLoc = comm.getHQOpposite();
        nearbyEnemies = rc.senseNearbyRobots(RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
        mapLeadScore = (comm.leadScore / (double)comm.numArchons) * (400.0/(H*W));
        turtleRadius = Math.pow(H*W, .6);
        if ((nearestCorner.x + nearestCorner.y) % 2 == 1) {
            LATTICE_MOD = 1;
        }
    }

    public void play() {
        currRound = rc.getRoundNum();
        reinforceLoc = comm.readReinforcements();
        if (reinforceLoc != null) {
            lastReinforceLoc = reinforceLoc;
        }
        tryAttack();
        tryMove();
        tryDisintegrate(); //will only disintegrate if valid location and health too low
        nearbyEnemies = rc.senseNearbyRobots(RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
        tryAttack();

    }

    void tryAttack(){
        if (!rc.isActionReady()) return;
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.SOLDIER.actionRadiusSquared, enemyTeam);
        MapLocation myLoc = rc.getLocation();
        MapLocation bestLoc = null;
        boolean attackerInRange = false;
        // don't attack miners if soldiers in view
        for (RobotInfo r : nearbyEnemies) {
            if (r.type.canAttack()) {
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
        if (currRound == birthday) {
            task = comm.getTask();
        }
        switch (task) {
            case 0: {// scout

                break;
            }
            case 1: {// defensive lattice
                //TODO: if haven't seen an attack in ~100 turns, crunch / lead farm
                task = comm.getTask();
                latticeCombat();
                MapLocation target = null;
                if (rc.getHealth() < MIN_HEALTH_TO_FIGHT || !validGuard(rc.getLocation())) target = comm.HQloc;
                if (target != null) {
                    bfs.move(target);
                    break;
                }
                target = getFreeSpace();
                greedyMove(target);
                break;
            }
            case 2: {// emergency
                task = comm.getTask();
                MapLocation target = null;
                if (currRound < comm.P3_START) target = moveInCombat();
                if (target == null) {
                    target = comm.getEmergencyLoc();
                }
                bfs.move(target);
                break;
            }
            case 3: { // explore
                task = comm.getTask();
                MapLocation target = moveInCombat();
                // early disintegration?
                if (target == null) {
                    target = reinforceLoc;
                }
                if (target == null) {
                    target = explore.getExplore3Target();
                }
                bfs.move(target);
                senseEnemyArchons();
                break;
            }
            case 4: { // crunch TODO: improve. get lowest index or nearest non null archon location. bug when archon is destroyed but not crunching.
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
                break;
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

    void greedyMove(MapLocation target) {
        MapLocation myLoc = rc.getLocation();
        int bestRubble = GameConstants.MAX_RUBBLE;
        MapLocation bestLoc = null;
        int d1 = myLoc.distanceSquaredTo(target);
        try {
            for (Direction dir: moveDirections) {
                MapLocation prospect = myLoc.add(dir);
                if (!validGuard(prospect) || !rc.canMove(dir)) continue;
                int r = rc.senseRubble(prospect);
                if (prospect.distanceSquaredTo(target) < d1 && r < bestRubble) {
                    bestLoc = prospect;
                    bestRubble = r;
                }
            }
            if (bestLoc != null) rc.move(myLoc.directionTo(bestLoc));

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void tryDisintegrate() {
        if (rc.getHealth() >= MIN_HEALTH_TO_FIGHT || !rc.isActionReady() || task == comm.EMERGENCY) return;
        MapLocation myLoc = rc.getLocation();
        try {
            if ((myLoc.distanceSquaredTo(comm.HQloc) <= turtleRadius || myLoc.distanceSquaredTo(nearestCorner) <= turtleRadius) && rc.senseLead(myLoc) == 0) {
                rc.disintegrate();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    //TODO: improve
    MapLocation moveInCombat() {
        MapLocation pursuitTarget = null;
        MapLocation myLoc = rc.getLocation();
        int lowestHealth = 40000;
        for (RobotInfo enemy : nearbyEnemies) {
            //TODO: improve logic, don't just flee from first enemy seen
            int myForcesCount = rc.getHealth();
            RobotInfo[] myForces = rc.senseNearbyRobots(enemy.location, ALLY_FORCES_RANGE, myTeam);
            for (RobotInfo r : myForces) {
                if (r.type.canAttack()) {
                    myForcesCount += r.health;
                }
            }
            int enemyForcesCount = 0;
            int enemyNum = 0; // for calling reinforcements
            RobotInfo[] enemyForces = rc.senseNearbyRobots(enemy.location, RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
            for (RobotInfo r : enemyForces) {
                if (r.type.canAttack()) {
                    enemyForcesCount += r.health;
                    enemyNum++;
                }
            }
            if (myForcesCount < enemyForcesCount) {
                explore.reset();
                // don't call for overly aggressive reinforcements
                if (myForcesCount*1.5 > enemyForcesCount || myLoc.distanceSquaredTo(comm.getHQOpposite()) <= myLoc.distanceSquaredTo(comm.HQloc)) {
                    comm.callReinforcements(enemyNum, enemy.location);
                }
                //return flee(enemy.location); // lowest rubble tile away from enemy
                return comm.HQloc; // TODO: consider enemies
            }
            else if (enemy.type.canAttack() && enemy.health < lowestHealth) {
                lowestHealth = enemy.health;
                // if vulnerable enemy out of range, pursue
                if(myForcesCount > 1.5*enemyForcesCount && myLoc.distanceSquaredTo(enemy.location) > RobotType.SOLDIER.actionRadiusSquared) {
                    pursuitTarget = enemy.location;
                }
            }
            else if (enemyForcesCount == 0) {
                pursuitTarget = enemy.location; // no nearby forces? harass.
            }

            //wipe reinforcements if winning fight
            if (reinforceLoc != null && myForcesCount > enemyForcesCount && myLoc.distanceSquaredTo(reinforceLoc) <= RobotType.SOLDIER.visionRadiusSquared) {
                comm.clearReinforcements();
            }
        }
        if (pursuitTarget != null) {
            pursuitTarget = getGreedyAttackTile(pursuitTarget);
        }
        return pursuitTarget;
    }

    MapLocation getGreedyAttackTile(MapLocation pursuitTarget) {
        MapLocation myLoc = rc.getLocation();
        MapLocation bestLoc = myLoc;
        try {
            int bestRubble = rc.senseRubble(myLoc);
            for (Direction dir : moveDirections) {
                MapLocation prospect = myLoc.add(dir);
                if (!rc.canMove(dir)) continue; // reduce bytecode?
                if (prospect.distanceSquaredTo(pursuitTarget) <= RobotType.SOLDIER.visionRadiusSquared) {
                    int r = rc.senseRubble(prospect);
                    if (r < bestRubble) {
                        bestLoc = prospect;
                        bestRubble = r;
                    }
                    // in case of tie, try to stay at farther range
                    else if (r == bestRubble && prospect.distanceSquaredTo(pursuitTarget) > myLoc.distanceSquaredTo(pursuitTarget)) {
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

    void latticeCombat() {
        MapLocation myLoc = rc.getLocation();
        for (RobotInfo enemy : nearbyEnemies) {
            //TODO: improve logic, don't just flee from first enemy seen
            int myForcesCount = rc.getHealth();
            RobotInfo[] myForces = rc.senseNearbyRobots(enemy.location, ALLY_FORCES_RANGE, myTeam);
            for (RobotInfo r : myForces) {
                if (r.type.canAttack()) {
                    myForcesCount += r.health;
                }
            }
            int enemyForcesCount = 0;
            int enemyNum = 0; // for calling reinforcements
            RobotInfo[] enemyForces = rc.senseNearbyRobots(enemy.location, RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
            for (RobotInfo r : enemyForces) {
                if (r.type.canAttack()) {
                    enemyForcesCount += r.health;
                    enemyNum++;
                }
            }
            if (myForcesCount < enemyForcesCount) { // losing or battle or health too low (better to make mine)
                explore.reset();
                // don't call for overly aggressive reinforcements
                comm.callReinforcements(enemyNum, enemy.location);
                //return flee(enemy.location); // lowest rubble tile away from enemy
            }

            //wipe reinforcements if winning fight
            if (reinforceLoc != null && myForcesCount > enemyForcesCount && myLoc.distanceSquaredTo(reinforceLoc) <= RobotType.SOLDIER.visionRadiusSquared) {
                comm.clearReinforcements();
            }
        }
    }

    void senseEnemyArchons() { // check for enemy archon and write
        for (RobotInfo r : nearbyEnemies) {
            if (r.getType() == RobotType.ARCHON) {
                comm.writeEnemyArchonLocation(r);
                try {
                    if ((mapLeadScore < comm.HIGH_LEAD_THRESHOLD && currRound <= 200 && rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared).length > 9))  { // sense should crunch
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

    boolean validGuard(MapLocation loc) {
        int d1 = loc.distanceSquaredTo(nearestCorner);
        int d2 = loc.distanceSquaredTo(comm.HQloc);
        return d1 <= turtleRadius || d2 <= turtleRadius;
    }

    //TODO: hard code lattice locations
    MapLocation getFreeSpace() { // if not on outer radius, form a lattice so that the outer guards can path back and heal/disintegrate.
        MapLocation myLoc = rc.getLocation();
        MapLocation target = null;
        if (validGuard(myLoc)) {
            // outer ring of base
            if (!validGuard(myLoc.add(myLoc.directionTo(comm.HQloc).opposite()))) {
                target = myLoc;
            }
            // inner ring lattice
            else if ((myLoc.x + myLoc.y) % 2 == LATTICE_MOD) {
                target = myLoc;
            }
        }
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, RobotType.SOLDIER.visionRadiusSquared);
            for (MapLocation cell : cells) { // interlinked
                if (validGuard(cell) && !rc.canSenseRobotAtLocation(cell)) {
                    // closest to last reinforceloc
                    if (target == null || cell.distanceSquaredTo(lastReinforceLoc) < target.distanceSquaredTo(lastReinforceLoc))
                    {
                        // outer ring of base  TODO: CONSIDER EDGE CASE OF CORNER AND DIAGONAL MOVEMENT
                        if (!validGuard(cell.add(cell.directionTo(comm.HQloc).opposite()))) {
                            target = cell;
                        }
                        // inner ring lattice
                        else if ((cell.x + cell.y) % 2 == LATTICE_MOD) {
                            target = cell;
                        }
                    }
                }
            }
            // no spacing in vision
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (target == null) return comm.HQloc;
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
        // if not near corner, build around HQ TODO: if close to wall, builder should build near wall, not HQ
        if (comm.HQloc.distanceSquaredTo(new MapLocation(x, H1/2)) < d1) {
            nearestCorner = new MapLocation(x, comm.HQloc.y);
        }
        else if (comm.HQloc.distanceSquaredTo(new MapLocation(W1/2, y)) < d1) {
            nearestCorner = new MapLocation(comm.HQloc.x, y);
        }
        return nearestCorner;
    }
}