package newplayer;

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
    //P1: build scout, miners, and voyage
    //P2: spam soldiers to stop rush
    //P3: disintegrate builders to start lead engine
    //P4: start stockpiling lead for watchtowers and laboratory
    static final int P1_MINERS = 6;
    static final int P2_START = 80;
    static final int P3_START = 400; //survived rush, hopefully
    static final int P4_START = 500;
    static final int P4_SAVINGS = 1000;
    static final int HIGH_LEAD_THRESHOLD = 5; // this is solely based on deposits, may want to update

    int H, W;
    Team myTeam, enemyTeam;
    int birthday;

    MapLocation mirror;

    int minersBuilt = 0;
    int soldiersBuilt = 0;
    int builderCount = 0;
    int depositsDetected = 0;

    boolean arrived = false;
    int currRound = 0;
    int currLead = 0;
    int task = 0;

    public Archon(RobotController rc){
        super(rc);
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        birthday = rc.getRoundNum();
    }

    public void play(){
        currRound = rc.getRoundNum();
        currLead = rc.getTeamLeadAmount(myTeam);
        if (currRound == birthday) {
            comm.writeAllyArchonLocation();
            if (comm.setHQloc(rc.getLocation())) {
                arrived = true;
            }
        };
        checkForAttackers(); //sends emergency to all soldiers
        if (rc.getMode() == RobotMode.TURRET) {
            if (!arrived) {
                if (minersBuilt >= P1_MINERS - comm.numArchons || currRound >= P2_START) { // archon voyage
                    try {
                        if (rc.isTransformReady()) {
                            rc.transform();
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
            tryBuild();
        }
        else {
            if (arrived == false) {
                tryMove();
            }
        }
    }

    void checkForAttackers() {
        if (!arrived) return;
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, enemyTeam);
        for (RobotInfo r : robots) {
            if (r.getType() == RobotType.SOLDIER) {
                comm.setEmergencyLoc(r.getLocation());
                comm.setTask(2);
                return;
            }
        }
        comm.setTask(1);
    }
    void tryMove(){
        if (comm.HQloc == null) {
            comm.readHQloc();
        }
        if (rc.getLocation().isWithinDistanceSquared(comm.HQloc, 4)) {
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
        // PHASE 1
        if (currRound < P2_START) {
            if (!arrived) {
                return true; // archon hasn't started voyaging, build miner
            }
            else if (depositsDetected <= HIGH_LEAD_THRESHOLD && soldiersBuilt < 1) {
                return false; // hq build scout
            }
            else if (task == 2) {
                return false; // emergency
            }
            else if (minersBuilt < P1_MINERS - comm.numArchons) { // hq build miners
                return true;
            }
            else if (builderCount > minersBuilt) { // lots of early builders, so we'll need lots of lead
                return true;
            }
            return false;
        }
        // PHASE 2
        else if (currRound < P3_START) {
            if (task == 2) {
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
        else if (currRound < P4_START) {
            int buildCode = comm.readBuildCode(3);
            if (buildCode == 1) {
                comm.writeBuildCode(3,2);
                return true;
            }
            else {
                return false;
            }
        }
        // PHASE 4
        else {
            int buildCode = comm.readBuildCode(4);
            if (buildCode == 0 && currLead > P4_SAVINGS + RobotType.MINER.buildCostLead) {
                comm.writeBuildCode(4,1);
                return true;
            }
        }
        return false;
    }

    boolean shouldBuildBuilder() {
        // PHASE 1
        if (currRound < P2_START) {
            if (depositsDetected > HIGH_LEAD_THRESHOLD && soldiersBuilt > 0 && builderCount < Math.ceil((depositsDetected - HIGH_LEAD_THRESHOLD)/25.0)) return true; // on high lead maps we will build watchtowers to try and stop soldiers
            return false;
        }
        // PHASE 2
        else if (currRound < P3_START) {
            return false;
        }
        // PHASE 3
        else if (currRound < P4_START) {
            int buildCode = comm.readBuildCode(3);
            if (buildCode == 0) {
                comm.writeBuildCode(3,1);
                if (depositsDetected < 25) { // if lead is low, create some mines
                    return true;
                }
            }
            return false;

        }
        // PHASE 4
        else {
            int buildCode = comm.readBuildCode(4);
            if (buildCode == 1 && currLead > P4_SAVINGS + RobotType.BUILDER.buildCostLead) {
                comm.writeBuildCode(4,2);
                return true;
            }
        }
        return false;
    }

    boolean shouldBuildSoldier() {
        // PHASE 1
        if (currRound < P2_START) {
            if (currLead > comm.numArchons * RobotType.MINER.buildCostLead && depositsDetected < HIGH_LEAD_THRESHOLD) return true;
            if (task == 2) return true;
            if (soldiersBuilt < 1) {
                return arrived;
            }
            return false;
        }
        // PHASE 2
        else if (currRound < P3_START) {
            if (task == 2) {
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
        else if (currRound < P4_START) {
            int buildCode = comm.readBuildCode(3);
            if (buildCode == 2) {
                comm.writeBuildCode(3,0);
                return true;
            }
            else {
                return false;
            }
        }
        // PHASE 4
        else {
            int buildCode = comm.readBuildCode(4);
            if ((buildCode == 2 && currLead > P4_SAVINGS + RobotType.SOLDIER.buildCostLead)) {
                comm.writeBuildCode(4,0);
                return true;
            }
        }
        return false;
    }

    boolean shouldBuildSage() {
        return false;
    }

    void tryBuild(){
        task = comm.getTask(); // check if emergency, if so we'll build soldiers
        getMines(); // update deposits detected
        MapLocation myLoc = rc.getLocation();
        if (currLead >= RobotType.MINER.buildCostLead && shouldBuildMiner()) {
            for (Direction dir : spawnDirections) { //TODO: clean up deciding bestDir
                try {
                    if (rc.canBuildRobot(RobotType.MINER, dir)) {
                        rc.buildRobot(RobotType.MINER, dir); // we simply spam miners
                        minersBuilt++;
                        break;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
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
                        else if (myLoc.add(dir).distanceSquaredTo(comm.HQopposite) < myLoc.add(bestDir).distanceSquaredTo(comm.HQopposite)) {
                            bestDir = dir;
                        }
                    }
                }
                if (bestDir != null) {
                    rc.buildRobot(RobotType.BUILDER, bestDir);
                    builderCount++;
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
                        else if (myLoc.add(dir).distanceSquaredTo(comm.HQopposite) < myLoc.add(bestDir).distanceSquaredTo(comm.HQopposite)) {
                            bestDir = dir;
                        }
                    }
                }
                if (bestDir != null) {
                    rc.buildRobot(RobotType.SOLDIER, bestDir); // we simply spam soldiers
                    soldiersBuilt++;
                    comm.setTask(1); // for now we only build one scout
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else if (currLead >= RobotType.SAGE.buildCostLead && shouldBuildSage()) {

        }
        else { // failed spawn, we'll try to heal
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

    void getMines(){
        depositsDetected = 0;
        MapLocation myLoc = rc.getLocation();
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells) { // interlinked
                if (!rc.canSenseLocation(cell)) continue; // needed?
                int lead = rc.senseLead(cell);
                if (lead > 0) {
                    depositsDetected++;
                }

                int gold = rc.senseGold(cell);
                if (gold > 0) {
                    depositsDetected++;
                }

            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    boolean isHQ() {
        return rc.getLocation() == comm.HQloc;
    }

}