package advancedplayer;

// deciding the HQ:
// on high overall lead maps, we should not move the archons
// we can predict the maps lead using nearby lead as a function of avg rubble

// otherwise, we should consider the following factors
// lead, rubble
// distance from map center
// net distance from other archones (least travel time)

// in deciding when to move the archons, we should consider the following
// overall lead
// distance from hq
// map size

// the hq should...
// on high lead maps, invest in early watchtowers
// on low lead maps, invest in builders as a function of mapsize


import battlecode.common.*;
import scala.collection.Map;

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
        comm.setTask(comm.EXPLORE); // for now we are ignoring scouting and starting with harass/protecting miners
    }

    public void play() {
        if (comm.spawnID >= rc.getArchonCount()) comm.fixSpawnID(); // avoid getting stuck if an archon dies
        currRound = rc.getRoundNum();
        currLead = rc.getTeamLeadAmount(myTeam);
        currGold = rc.getTeamGoldAmount(myTeam);

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
        if (currRound >= CRUNCH_ROUND && (rc.getArchonCount() < comm.numArchons || rc.getTeamGoldAmount(myTeam) < rc.getTeamGoldAmount(enemyTeam))) { // if we lost an archon, we need to try to get theirs
            comm.setTask(comm.CRUNCH);
        }

        checkForAttackers(); //sends emergency to all soldiers if x enemies in archon vision

        if (rc.getMode() == RobotMode.TURRET) {
            if (!arrived && currRound > birthday + 2) {
                if (minersBuilt >= P1_MINERS - comm.numArchons) { // archon voyage
                    try {
                        if (rc.isTransformReady()) {
                            rc.transform();
                            comm.incSpawnCounter(); // avoid getting stuck;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
            if (!tryBuild()) tryRepair();
        }
        else {
            if (arrived == false) {
                tryMove();
            }
            comm.incSpawnCounter(); // avoid getting stuck;
        }
    }

    void checkForAttackers() {
        if (!arrived) return;
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, enemyTeam);
        if (robots.length > 1) {
            comm.setEmergencyLoc(robots[0].getLocation());
            comm.setTask(comm.EMERGENCY);
            return;
        }
        if (task == comm.EMERGENCY) comm.setTask(comm.LATTICE);
    }

    void tryMove(){
        if (comm.HQloc == null) {
            comm.readHQloc();
        }
        // TODO: consider rubble
        if (rc.getLocation().isWithinDistanceSquared(comm.HQloc, (int) Math.round(Math.sqrt(H*W)))) {
            try {
                if (rc.isTransformReady()) {
                    rc.transform();
                    arrived = true;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else {
            bfs.move(comm.HQloc);
        }
        return;
    }

    boolean shouldBuildMiner() {
        if (task == comm.CRUNCH) return false; //crunch
        // PHASE 1
        if (currRound < comm.P2_START) {
            if (!arrived) {
                return true; // archon hasn't started voyaging, build miner
            }
            else if (task == comm.EMERGENCY) {
                return false; // emergency
            }
            else if (minersBuilt < P1_MINERS - comm.numArchons) { // hq build miners
                return true;
            }
            else if (builderCount < P1_BUILDERS && (mapLeadScore > comm.HIGH_LEAD_THRESHOLD || mapLeadScore < comm.LOW_LEAD_THRESHOLD)) { // need more deposits or need to rush towers
                return false;
            }
            return false;
        }
        // PHASE 2
        else if (currRound < comm.P3_START) {
            if (task == comm.EMERGENCY) {
                return false; // emergency, build soldiers
            }
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
        else if (currRound < comm.P4_START) {
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
            int buildCode = comm.readBuildCode(4);
            if (buildCode == 0 && currLead > comm.P4_SAVINGS + RobotType.MINER.buildCostLead) {
                comm.writeBuildCode(4,1);
                return true;
            }
        }
        return false;
    }

    boolean shouldBuildBuilder() {
        if (task == comm.CRUNCH) return false; //crunch
        // PHASE 1
        if (currRound < comm.P2_START) {
            if (arrived && builderCount < P1_BUILDERS && (mapLeadScore > comm.HIGH_LEAD_THRESHOLD || mapLeadScore < comm.LOW_LEAD_THRESHOLD)) return true; // early towers or early farm
            return false;
        }
        // PHASE 2
        else if (currRound < comm.P3_START) { //TODO: disintegrate miners if lead is low
            if (builderCount < minersBuilt && builderCount < P2_BUILDERS && mapLeadScore > comm.HIGH_LEAD_THRESHOLD ) return true; // early towers only
            return false;
        }
        // PHASE 3
        else if (currRound < comm.P4_START) {
            return false;

        }
        // PHASE 4
        else {
            if (currGold > RobotType.SAGE.buildCostGold && task == 2) return false;
            int buildCode = comm.readBuildCode(4);
            if (buildCode == 1 && currLead > comm.P4_SAVINGS + RobotType.BUILDER.buildCostLead) {
                comm.writeBuildCode(4,2);
                return true;
            }
        }
        return false;
    }

    boolean shouldBuildSoldier() {
        if (task == 4) return true; //crunch
        // PHASE 1
        if (currRound < comm.P2_START) {
            if (mapLeadScore < comm.HIGH_LEAD_THRESHOLD) return true;
            return false;
        }
        // PHASE 2
        else if (currRound < comm.P3_START) {
            if (task == 2) {
                return true;
            }
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
        else if (currRound < comm.P4_START) {
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
            int buildCode = comm.readBuildCode(4);
            if ((buildCode == 2 && currLead > comm.P4_SAVINGS + RobotType.SOLDIER.buildCostLead)) {
                comm.writeBuildCode(4,0);
                return true;
            }
        }
        return false;
    }

    boolean shouldBuildSage() {
        return task == 2 || currGold - RobotType.SAGE.buildCostGold > rc.getTeamGoldAmount(enemyTeam);
    }

    //TODO: cleanup, spawn toward emergency, spawn in safe location, etc
    boolean tryBuild() {
        if (currRound < comm.P3_START && comm.getSpawnCount() % rc.getArchonCount() != comm.spawnID) return false;
        task = comm.getTask(); // check if emergency, if so we'll build soldiers
        MapLocation myLoc = rc.getLocation();
        if (currLead >= RobotType.MINER.buildCostLead && shouldBuildMiner()) {
            MapLocation closestMine = getClosestMine();
            Direction bestDir = null;
            try {
                for (Direction dir : spawnDirections) {
                    if (rc.canBuildRobot(RobotType.MINER, dir)) {
                        if (bestDir == null) {
                            bestDir = dir;
                        }
                        else if (myLoc.add(dir).distanceSquaredTo(closestMine) < myLoc.add(bestDir).distanceSquaredTo(closestMine)) {
                            bestDir = dir;
                        }
                    }
                }
                if (bestDir != null) {
                    rc.buildRobot(RobotType.MINER, bestDir); // we simply spam soldiers
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
            Direction bestDir = null;
            try {
                for (Direction dir : spawnDirections) {
                    if (rc.canBuildRobot(RobotType.BUILDER, dir)) {
                        if (bestDir == null) {
                            bestDir = dir;
                        }
                        else if (myLoc.add(dir).distanceSquaredTo(mapCenter) < myLoc.add(bestDir).distanceSquaredTo(mapCenter)) {
                            bestDir = dir;
                        }
                    }
                }
                if (bestDir != null) {
                    rc.buildRobot(RobotType.BUILDER, bestDir);
                    comm.incSpawnCounter();
                    builderCount++;
                    return true;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else if (currLead >= RobotType.SOLDIER.buildCostLead && shouldBuildSoldier()) {
            Direction bestDir = null;
            try {
                for (Direction dir : spawnDirections) {
                    if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                        if (bestDir == null) {
                            bestDir = dir;
                        }
                        else if (myLoc.add(dir).distanceSquaredTo(mapCenter) < myLoc.add(bestDir).distanceSquaredTo(mapCenter)) {
                            bestDir = dir;
                        }
                    }
                }
                if (bestDir != null) {
                    rc.buildRobot(RobotType.SOLDIER, bestDir); // we simply spam soldiers
                    comm.incSpawnCounter();
                    soldiersBuilt++;
                    return true;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else if (currGold >= RobotType.SAGE.buildCostGold && shouldBuildSage()) {
            Direction bestDir = null;
            try {
                for (Direction dir : spawnDirections) {
                    if (rc.canBuildRobot(RobotType.SAGE, dir)) {
                        if (bestDir == null) {
                            bestDir = dir;
                        }
                        else if (myLoc.add(dir).distanceSquaredTo(mapCenter) < myLoc.add(bestDir).distanceSquaredTo(mapCenter)) {
                            bestDir = dir;
                        }
                    }
                }
                if (bestDir != null) {
                    comm.incSpawnCounter();
                    rc.buildRobot(RobotType.SAGE, bestDir);
                    return true;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return false;
    }

    void tryRepair() {
        RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, myTeam);
        MapLocation bestLoc = null;
        int lowestHP = 10000;
        for (RobotInfo r : allies){
            MapLocation allyLoc = r.getLocation();
            if (rc.canRepair(r.getLocation())){
                int hp = r.getHealth();
                if (hp < lowestHP) {
                    lowestHP = hp;
                    bestLoc = allyLoc;
                }
            }
        }
        try {
            if (bestLoc != null) rc.repair(bestLoc);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    MapLocation getClosestMine(){
        MapLocation myLoc = rc.getLocation();
        MapLocation bestMine = null;
        int bestDist = 10000;
        try {
            MapLocation leadMines[] = rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared);
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
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
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