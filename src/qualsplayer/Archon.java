package qualsplayer;

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
    static final int MINES_TO_BUILD_LAB = 5;
    static final int MIN_MAP_SIZE_LAB = 625;
    static final int BUILDER_TURNS = 30;

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
    MapLocation[] mines;

    boolean arrived = false;
    int currRound = 0;
    int currLead = 0;
    int currGold = 0;
    int task = 3;

    int P1_MINERS_MODIFIER = 0;

    boolean shouldBuildLab = false;

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
        mines = rc.senseNearbyLocationsWithLead();
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
        shouldBuildLab = comm.getShouldBuildLab();
        // SHOULD BUILD LAB
        if (currRound > birthday + 2) {
            if (!shouldBuildLab) {
                if (currLead > 300 || currRound >= Communication.P4_START) {
                    comm.setShouldBuildLab();
                    shouldBuildLab = true;
                }
                else {
                    try {
                        if (task != 2 && mines.length >= MINES_TO_BUILD_LAB) {
                            comm.setShouldBuildLab();
                            shouldBuildLab = true;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
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
                if (minersBuilt >= P1_MINERS - comm.numArchons - P1_MINERS_MODIFIER || currRound >= Communication.P2_START) { // condition for archons to start moving
                    if (!rc.getLocation().equals(getTransformLocation())) {
                        try {
                            if (rc.isTransformReady()) {
                                rc.transform();
                                if (currRound <= comm.P2_START) comm.incSpawnCounter(); // avoid getting stuck
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
            if (currRound <= comm.P2_START) comm.incSpawnCounter(); // avoid getting stuck
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
        if (task != 2 && shouldBuildLab) {
            if ((currLead < RobotType.LABORATORY.buildCostLead + RobotType.MINER.buildCostLead && !comm.labIsBuilt()) || (currLead < RobotType.BUILDER.buildCostLead + RobotType.MINER.buildCostLead && !comm.builderIsBuilt())) return false;
            if (comm.labIsBuilt() && arrived) {
                if (currLead < 5*BUILDER_TURNS*comm.getDelayModifier() && comm.getSpawnCount() % 6 == 0) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        if (currGold >= RobotType.SAGE.buildCostGold) return false;
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
            if (comm.getSpawnCount() % 3 == 0) {
                return true;
            }
        }
        return false;
    }

    boolean shouldBuildBuilder() {
        if (task != 2 && shouldBuildLab && arrived) {
            //System.err.println(comm.getSpawnCount() + "," + BUILDER_TURNS*comm.getDelayModifier());
            if (!comm.builderIsBuilt()) return true;
            if (comm.labIsBuilt() && currGold > 0 && currLead >= 5*BUILDER_TURNS*comm.getDelayModifier()) {
                return true; // checking if gold is greater than 0 as a means to know lab is repaired
            }
        }
        if (currGold >= RobotType.SAGE.buildCostGold) return false;
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
        if (currGold >= RobotType.SAGE.buildCostGold) return false;
        if (task != 2 && shouldBuildLab) {
            if ((currLead < RobotType.LABORATORY.buildCostLead + RobotType.SOLDIER.buildCostLead && !comm.labIsBuilt()) || (currLead < RobotType.BUILDER.buildCostLead + RobotType.SOLDIER.buildCostLead && !comm.builderIsBuilt())) return false;
            if (currRound >= Communication.P3_START) return false;
        }
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
            return true;
        }
    }

    boolean shouldBuildSage() {
        return  rc.getRoundNum() < Communication.P3_START || (comm.getSpawnCount() % 6 != 0 && currLead < 5*BUILDER_TURNS*comm.getDelayModifier());
    }

    //TODO: cleanup, spawn toward emergency, spawn in safe location, etc
    boolean tryBuild() {
        if (currRound < Communication.P2_START && comm.getSpawnCount() % rc.getArchonCount() != comm.spawnID) return false;
        if (!rc.isActionReady()) return false;
        MapLocation myLoc = rc.getLocation();
        if (currLead >= RobotType.MINER.buildCostLead && shouldBuildMiner()) {
            MapLocation closestMine = getClosestMine();
            MapLocation bestLoc = null;
            int bestRubble = 10000;
            try {
                for (Direction dir : spawnDirections) {
                    if (rc.canBuildRobot(RobotType.MINER, dir)) {
                        MapLocation spawnLoc = myLoc.add(dir);
                        int rubble = rc.senseRubble(spawnLoc);
                        if (bestLoc == null) {
                            bestLoc = spawnLoc;
                            bestRubble = rubble;
                        }
                        else if (closestMine != null) { // if mine in view, tiebreak with distance to that mine. if same distance, tiebreak with distance to center
                            if (rubble < bestRubble) {
                                bestLoc = spawnLoc;
                                bestRubble = rubble;
                            }
                            else if (rubble == bestRubble) {
                                int d1 = spawnLoc.distanceSquaredTo(closestMine);
                                int d2 = bestLoc.distanceSquaredTo(closestMine);
                                if (d1 < d2) {
                                    bestLoc = spawnLoc;
                                }
                                else if (d1 == d2 && spawnLoc.distanceSquaredTo(mapCenter) < bestLoc.distanceSquaredTo(mapCenter)) {
                                    bestLoc = spawnLoc;
                                }
                                // if everything is a tie, good enough
                            }
                        }
                        else if (closestMine == null) { // if no mine in view, tiebreak with distance to mapcenter
                            if (rubble < bestRubble) {
                                bestLoc = spawnLoc;
                                bestRubble = rubble;
                            }
                            else if (rubble == bestRubble) {
                                int d1 = spawnLoc.distanceSquaredTo(mapCenter);
                                int d2 = bestLoc.distanceSquaredTo(mapCenter);
                                if (d1 < d2) {
                                    bestLoc = spawnLoc;
                                }
                                // if everything is a tie, good enough
                            }
                        }
                    }
                }
                if (bestLoc != null) {
                    rc.buildRobot(RobotType.MINER, myLoc.directionTo(bestLoc));
                    comm.incSpawnCounter();
                    minersBuilt++;
                    return true;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else if(currLead >= RobotType.BUILDER.buildCostLead && shouldBuildBuilder())
        {
            MapLocation nearestCorner = getNearestCorner();
            MapLocation bestLoc = null;
            try {
                for (Direction dir : spawnDirections) {
                    if (rc.canBuildRobot(RobotType.BUILDER, dir)) {
                        MapLocation spawnLoc = myLoc.add(dir);
                        if (bestLoc == null) {
                            bestLoc = spawnLoc;
                        }
                        else if (spawnLoc.distanceSquaredTo(nearestCorner) < bestLoc.distanceSquaredTo(nearestCorner)) {
                            bestLoc = spawnLoc;
                        }
                    }
                }
                if (bestLoc != null) {
                    rc.buildRobot(RobotType.BUILDER, myLoc.directionTo(bestLoc));
                    comm.setBuilderBuilt();
                    comm.incSpawnCounter();
                    comm.incDelayModifier();
                    if(comm.getDelayModifier() == 8) comm.setMutatorFlag();
                    builderCount++;
                    return true;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else if (currLead >= RobotType.SOLDIER.buildCostLead && shouldBuildSoldier()) {
            MapLocation bestLoc = null;
            int bestRubble = 10000;
            try {
                for (Direction dir : spawnDirections) {
                    if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                        MapLocation spawnLoc = myLoc.add(dir);
                        int rubble = rc.senseRubble(spawnLoc);
                        if (bestLoc == null) {
                            bestLoc = spawnLoc;
                            bestRubble = rubble;
                        }
                        else {
                            if (rubble < bestRubble) {
                                bestLoc = spawnLoc;
                                bestRubble = rubble;
                            }
                            else if (rubble == bestRubble && spawnLoc.distanceSquaredTo(mapCenter) < bestLoc.distanceSquaredTo(mapCenter)) { // TODO: tiebreak with distance to nearest logged enemy?
                                bestLoc = spawnLoc;
                            }
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
            int bestRubble = 10000;
            try {
                for (Direction dir : spawnDirections) {
                    if (rc.canBuildRobot(RobotType.SAGE, dir)) {
                        MapLocation spawnLoc = myLoc.add(dir);
                        int rubble = rc.senseRubble(spawnLoc);
                        if (bestLoc == null) {
                            bestLoc = spawnLoc;
                            bestRubble = rubble;
                        }
                        else {
                            if (rubble < bestRubble) {
                                bestLoc = spawnLoc;
                                bestRubble = rubble;
                            }
                            else if (rubble == bestRubble && spawnLoc.distanceSquaredTo(mapCenter) < bestLoc.distanceSquaredTo(mapCenter)) { // TODO: tiebreak with distance to nearest logged enemy?
                                bestLoc = spawnLoc;
                            }
                        }
                    }
                }
                if (bestLoc != null) {
                    rc.buildRobot(RobotType.SAGE, myLoc.directionTo(bestLoc));
                    comm.incSpawnCounter();
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
        int lowestHP = 0;
        boolean sageInRange = false;
        // lowest hp under max health, prioritizing attackers
        for (RobotInfo r : allies){
            if (!rc.canRepair(r.getLocation()) || r.getType() == RobotType.MINER) continue; // don't heal miners, since they will sack themselves
            int hp = r.getHealth();
            if (!sageInRange && r.getType() == RobotType.SAGE && hp < r.getType().getMaxHealth(r.getLevel())) {
                sageInRange = true;
                lowestHP = hp;
                bestLoc = r.location;
            }
            if (hp > lowestHP && hp < r.getType().getMaxHealth(r.getLevel())) {
                if (sageInRange && !r.getType().canAttack()) continue;
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