package altshmooveplayer;

// deciding the HQ:
// on high overall lead maps, we should not move the archons
// we can predict the maps lead using nearby lead as a function of avg rubble

// otherwise, we should consider the following factors
// lead, rubble
// distance from map center
// net distance from other archons (least travel time)

// in deciding when to move the archons, we should consider the following
// overall lead
// distance from hq
// map size

// the hq should...
// on high lead maps, invest in early watchtowers
// on low lead maps, invest in builders as a function of mapsize


import battlecode.common.*;

public class Archon extends MyRobot {
    static final Direction[] spawnDirections = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static final int P1_MINERS = 6;
    static final int P1_BUILDERS = 5;
    static final int P2_BUILDERS = 10;
    static final int CRUNCH_ROUND = 1500;
    static final int MIN_LEAD_TO_MINE = 6;

    int H, W;
    Team myTeam, enemyTeam;
    int birthday;

    int minersBuilt = 0;
    int soldiersBuilt = 0;
    int builderCount = 0;

    // nearbyInfo
    int depositsDetected = 0;
    int leadScore = 0; // total lead in vision + depositsDetected * GameConstants.ADD_LEAD * 5;
    int avgRubble = 0;
    int myRubble = 0;
    double mapLeadScore = 50;

    // MAP SCORES
    // NAME                 RAW     WEIGHTED    DESIRED
    // maptestsmall         7954    2989        earlyTower
    // intersection         150     49          none
    // eckleburg            85      9           farm

    MapLocation mapCenter;

    boolean arrived = false;
    int currRound = 0;
    int currLead = 0;
    int currGold = 0;
    int task = 3;

    int P1_MINERS_MODIFIER = 0;

    public Archon(RobotController rc){
        super(rc);
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        birthday = rc.getRoundNum();
        getNearbyInfo();
        comm.writeAllyArchonLocation(leadScore);
        mapCenter = new MapLocation((W - 1)/2, (H - 1)/2);
        comm.setTask(Communication.EXPLORE); // for now, we are ignoring scouting and starting with harass/protecting miners
        if (Math.sqrt(W*H) < 35) P1_MINERS_MODIFIER = 1; // if map is small make less miners
    }

    public void play() {
        task = comm.getTask(); // check if emergency, if so we'll build soldiers
        if (comm.spawnID >= rc.getArchonCount()) comm.fixSpawnID(); // TODO: FIX spawn id if archon dies...
        currRound = rc.getRoundNum();
        currLead = rc.getTeamLeadAmount(myTeam);
        currGold = rc.getTeamGoldAmount(myTeam);
        // HQ SETUP
        if (currRound == birthday + 1) { //write scores on 2nd round now that we have location
            comm.writeScore(avgRubble, myRubble);
            comm.readLeadScore(); // we also get lead score to determine how much we build before moving, if we should make mines, build towers, etc
            mapLeadScore = (comm.leadScore / (double)comm.numArchons) * (400.0/(H*W));
        }
        else if (currRound == birthday + 2) { // HQ is decided on 3rd round
            if (!comm.decideHQ()) comm.readHQloc();
            if (rc.getLocation().equals(comm.HQloc)) arrived = true;
        }
        // CRUNCH TIME
        if (currRound >= CRUNCH_ROUND && rc.getArchonCount() < comm.numArchons) { // if we lost an archon, we need to try to get theirs
            comm.setTask(Communication.CRUNCH);
        }
        // CHECKING / PATHING TO HQ
        if (rc.getMode() == RobotMode.TURRET) {
            checkForAttackers(); //sends emergency to all soldiers if x enemies in archon vision TODO: only if arrived?
            if (currRound > birthday + 2) {
                comm.readHQloc();
                if (minersBuilt >= P1_MINERS - comm.numArchons - P1_MINERS_MODIFIER || currRound > Communication.P2_START) { // condition for archons to start moving
                    if (!rc.getLocation().equals(getTransformLocation())) {
                        try {
                            if (rc.isTransformReady()) {
                                rc.transform();
                                comm.incSpawnCounter(); // avoid getting stuck
                                //System.err.println("transforming in favor of: " + getTransformLocation());
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                    else {
                        arrived = true;
                    }
                }
            }
            if (!tryBuild()) tryRepair();
        }
        else {
            comm.readHQloc();
            tryMove();
            comm.incSpawnCounter(); // avoid getting stuck;
        }
    }

    // TODO: only call emergency if troops are really needed
    void checkForAttackers() {
        if (!arrived) return;
        RobotInfo[] robots = rc.senseNearbyRobots(RobotType.ARCHON.visionRadiusSquared, enemyTeam);
        for (RobotInfo r : robots) {
            if (r.getType().canAttack()) {
                comm.setEmergencyLoc(r.location);
                comm.setTask(Communication.EMERGENCY);
                return;
            }
        }
        if (task == Communication.EMERGENCY && rc.canSenseLocation(comm.getEmergencyLoc())) comm.setTask(Communication.EXPLORE);
    }

    void tryMove(){
        if (!rc.isMovementReady()) return;
        if (comm.HQloc == null) {
            comm.readHQloc();
        }
        MapLocation target = getTransformLocation();
        // TODO: consider rubble and danger
        if (target != null) {
            if (rc.getLocation().equals(target)) {
                try {
                    if (rc.isTransformReady()) {
                        //System.err.println("settling at: " + target);
                        rc.transform();
                        arrived = true;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            else {
                if (rc.getLocation().equals(comm.HQloc)) {
                    bfs.move(target);
                    comm.updateHQ();
                }
                else {
                    bfs.move(target);
                }

            }
        }
        else {
            bfs.move(comm.HQloc);
        }
    }

    MapLocation getTransformLocation() {
        MapLocation myLoc = rc.getLocation();
        MapLocation bestLoc = null;
        int bestDist = 10000;
        int bestRubble = 10000;
        try {
            MapLocation[] cells = rc.getAllLocationsWithinRadiusSquared(comm.HQloc, RobotType.ARCHON.actionRadiusSquared);
            for (MapLocation cell : cells) {
                if (!rc.canSenseLocation(cell) || (rc.isLocationOccupied(cell) && !myLoc.equals(cell))) continue;
                int rubble = rc.senseRubble(cell);
                if (bestLoc == null) {
                    bestLoc = cell;
                    bestDist = myLoc.distanceSquaredTo(cell);
                    bestRubble = rubble;
                }
                else if (rubble < bestRubble) {
                    bestLoc = cell;
                    bestDist = myLoc.distanceSquaredTo(cell);
                    bestRubble = rubble;
                }
                else if (rubble == bestRubble) {
                    int d1 = myLoc.distanceSquaredTo(cell);
                    if (d1 < bestDist) {
                        bestLoc = cell;
                        bestDist = d1;
                    }
                }

            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return bestLoc;
    }

    boolean shouldBuildMiner() {
        if (task == Communication.CRUNCH) return false; //crunch
        // PHASE 1
        if (currRound < Communication.P2_START) {
            if (!arrived) {
                return true; // archon hasn't started voyaging, build miner
            }
            else if (task == Communication.EMERGENCY) {
                return false; // emergency
            }
            else if (minersBuilt < P1_MINERS - comm.numArchons - P1_MINERS_MODIFIER) { // hq build miners
                return true;
            }
            else if (builderCount < P1_BUILDERS && (mapLeadScore > Communication.HIGH_LEAD_THRESHOLD)) { // need more deposits or need to rush towers
                return false;
            }
            return false;
        }
        // PHASE 2
        else if (currRound < Communication.P3_START) {
            int buildCode = comm.readBuildCode(2); // alternate soldiers and miners
            if (buildCode == 0) {
                comm.writeBuildCode(2, 1);
                return true;
            }
            else {
                return false;
            }
        }
        // PHASE 3
        else if (currRound < Communication.P4_START) {
            int buildCode = comm.readBuildCode(3);
            if (buildCode == 1) {
                comm.writeBuildCode(3,2);
                return true;
            }
            return false;
        }
        // PHASE 4
        else {
            if (currGold > RobotType.SAGE.buildCostGold && task == 2) return false;
            if (comm.getSpawnCount() % 3 == 0) {
                return true;
            }
        }
        return false;
    }

    boolean shouldBuildBuilder() {
        if (task == Communication.CRUNCH) return false; //crunch
        // PHASE 1
        if (currRound < Communication.P2_START) {
            if (arrived && builderCount < P1_BUILDERS && (mapLeadScore > Communication.HIGH_LEAD_THRESHOLD)) return true; // early towers
            return false;
        }
        // PHASE 2
        else if (currRound < Communication.P3_START) { //TODO: disintegrate miners if lead is low
            if (builderCount < minersBuilt && builderCount < P2_BUILDERS && mapLeadScore > Communication.HIGH_LEAD_THRESHOLD ) return true; // early towers
            return false;
        }
        // PHASE 3
        else if (currRound < Communication.P4_START) {
            return false;

        }
        // PHASE 4
        else {
            return false;
        }
    }

    boolean shouldBuildSoldier() {
        if (currGold > RobotType.SAGE.buildCostGold) return false;
        if (task == 4) return true; //crunch
        // PHASE 1
        if (currRound < Communication.P2_START) {
            if (mapLeadScore < Communication.HIGH_LEAD_THRESHOLD) return true;
            return false;
        }
        // PHASE 2
        else if (currRound < Communication.P3_START) {
            if (currLead > 200) {
                return true;
            }
            int buildCode = comm.readBuildCode(2);
            if (buildCode == 1) {
                comm.writeBuildCode(2, 0);
                return true;
            }
            else {
                return false;
            }
        }
        // PHASE 3
        else if (currRound < Communication.P4_START) {
            int buildCode = comm.readBuildCode(3);
            if (buildCode == 2) {
                comm.writeBuildCode(3,0);
                return true;
            }
            if (buildCode == 0) {
                comm.writeBuildCode(3,1);
                return true;
            }
            else {
                return false;
            }
        }
        // PHASE 4
        else {
            if (currGold > RobotType.SAGE.buildCostGold && task == 2) return false;
            return true;
        }
    }

    boolean shouldBuildSage() {
        return task == 2 || currGold > RobotType.SAGE.buildCostGold;
    }

    //TODO: cleanup, spawn toward emergency, spawn in safe location, etc
    boolean tryBuild() {
        if (currRound < Communication.P3_START && comm.getSpawnCount() % rc.getArchonCount() != comm.spawnID) return false;
        if (!rc.isActionReady()) return false;
        MapLocation myLoc = rc.getLocation();
        if (currLead >= RobotType.MINER.buildCostLead && shouldBuildMiner()) {
            MapLocation closestMine = getClosestMine();
            MapLocation bestLoc = null;
            try {
                // TODO: if there are no mines in proximity, should we consider spawn location?
                for (Direction dir : spawnDirections) {
                    if (rc.canBuildRobot(RobotType.MINER, dir)) {
                        MapLocation spawnLoc = myLoc.add(dir);
                        if (bestLoc == null) {
                            bestLoc = spawnLoc;
                        }
                        else if (closestMine != null && spawnLoc.distanceSquaredTo(closestMine) < bestLoc.distanceSquaredTo(closestMine)) {
                            bestLoc = spawnLoc;
                        }
                    }
                }
                if (bestLoc != null) {
                    rc.buildRobot(RobotType.MINER, myLoc.directionTo(bestLoc)); // we simply spam soldiers
                    comm.incSpawnCounter();
                    minersBuilt++;
                    return true;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        // TODO: spawn toward best prospect?
        else if(currLead >= RobotType.BUILDER.buildCostLead && shouldBuildBuilder())
        {
            MapLocation bestLoc = null;
            try {
                for (Direction dir : spawnDirections) {
                    if (rc.canBuildRobot(RobotType.BUILDER, dir)) {
                        MapLocation spawnLoc = myLoc.add(dir);
                        if (bestLoc == null) {
                            bestLoc = spawnLoc;
                        }
                        else if (spawnLoc.distanceSquaredTo(mapCenter) < bestLoc.distanceSquaredTo(mapCenter)) {
                            bestLoc = spawnLoc;
                        }
                    }
                }
                if (bestLoc != null) {
                    rc.buildRobot(RobotType.BUILDER, myLoc.directionTo(bestLoc));
                    comm.incSpawnCounter();
                    builderCount++;
                    return true;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else if (currLead >= RobotType.SOLDIER.buildCostLead && shouldBuildSoldier()) {
            MapLocation bestLoc = null;
            try {
                for (Direction dir : spawnDirections) {
                    if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                        MapLocation spawnLoc = myLoc.add(dir);
                        if (bestLoc == null) {
                            bestLoc = spawnLoc;
                        }
                        else if (spawnLoc.distanceSquaredTo(mapCenter) < bestLoc.distanceSquaredTo(mapCenter)) {
                            bestLoc = spawnLoc;
                        }
                    }
                }
                if (bestLoc != null) {
                    rc.buildRobot(RobotType.SOLDIER, myLoc.directionTo(bestLoc));
                    comm.incSpawnCounter();
                    soldiersBuilt++;
                    return true;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else if (currGold >= RobotType.SAGE.buildCostGold && shouldBuildSage()) {
            MapLocation bestLoc = null;
            try {
                for (Direction dir : spawnDirections) {
                    if (rc.canBuildRobot(RobotType.SAGE, dir)) {
                        MapLocation spawnLoc = myLoc.add(dir);
                        if (bestLoc == null) {
                            bestLoc = spawnLoc;
                        }
                        else if (spawnLoc.distanceSquaredTo(mapCenter) < bestLoc.distanceSquaredTo(mapCenter)) {
                            bestLoc = spawnLoc;
                        }
                    }
                }
                if (bestLoc != null) {
                    comm.incSpawnCounter();
                    rc.buildRobot(RobotType.SAGE, myLoc.directionTo(bestLoc));
                    return true;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return false;
    }

    void tryRepair() {
        if (!rc.isActionReady()) return;
        RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, myTeam);
        MapLocation bestLoc = null;
        int lowestHP = 10000;
        boolean attackerInRange = false;
        // lowest hp under max health, prioritizing attackers
        for (RobotInfo r : allies){
            if (!rc.canRepair(r.getLocation()) || r.getType() == RobotType.MINER) continue; // don't heal miners, since they will sack themselves
            int hp = r.getHealth();
            if (!attackerInRange && r.getType().canAttack() && hp < r.getType().getMaxHealth(r.getLevel())) {
                attackerInRange = true;
                lowestHP = r.getHealth();
                bestLoc = r.location;
            }
            if (hp < lowestHP && hp < r.getType().getMaxHealth(r.getLevel())) {
                if (attackerInRange && !r.getType().canAttack()) break;
                lowestHP = hp;
                bestLoc = r.location;
            }
        }
        try {
            if (bestLoc != null) {
                //System.err.println("repairing at " + bestLoc);
                rc.repair(bestLoc);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    MapLocation getClosestMine(){
        MapLocation myLoc = rc.getLocation();
        MapLocation bestMine = null;
        int bestDist = 10000;
        try {
            MapLocation[] leadMines = rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared);
            for (MapLocation mine : leadMines) {
                int lead = rc.senseLead(mine);
                if (lead > MIN_LEAD_TO_MINE) {
                    int dist = myLoc.distanceSquaredTo(mine);
                    if (bestMine == null) {
                        bestMine = mine;
                        bestDist = dist;
                    }
                    if (dist < bestDist) {
                        bestMine = mine;
                        bestDist = dist;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return bestMine;
    }

    void getNearbyInfo(){
        MapLocation myLoc = rc.getLocation();
        try {
            myRubble = rc.senseRubble(myLoc);
            MapLocation[] cells = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells) { // interlinked
                int rubble = rc.senseRubble(cell);
                int lead = rc.senseLead(cell);
                if (lead > 0) {
                    leadScore += lead;
                    depositsDetected++;
                }
                avgRubble += rubble;
            }
            avgRubble /= cells.length;
            leadScore += depositsDetected * GameConstants.ADD_LEAD * 5;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}