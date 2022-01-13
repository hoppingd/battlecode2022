package sageplayer;

import battlecode.common.*;

import java.awt.*;

public class Builder extends MyRobot {
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

    static final int ALLY_FORCES_RANGE = 29;

    int watchCount = 0;
    int watchRepairedCount = 0;
    int H, W;
    Team myTeam, enemyTeam;
    int birthday;
    int task = 0;
    MapLocation nearestCorner;
    double mapLeadScore;
    boolean HIGH_LEAD_MAP = false;
    boolean LOW_LEAD_MAP = false;

    boolean moved = false;
    int currRound = 0;

    public Builder(RobotController rc){
        super(rc);
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        birthday = rc.getRoundNum();
        comm.readHQloc();
        nearestCorner = getNearestCorner();
        comm.readLeadScore(); // we also get lead score to determine how much we build before moving, if we should make mines, build towers, etc
        mapLeadScore = (comm.leadScore / (double)comm.numArchons) * (400.0/(H*W));
        HIGH_LEAD_MAP = mapLeadScore > comm.HIGH_LEAD_THRESHOLD;
        LOW_LEAD_MAP = mapLeadScore < comm.LOW_LEAD_THRESHOLD;
    }

    public void play(){
        currRound = rc.getRoundNum();
        task = comm.getTask();
        moved = false;
        if(!tryRepairPrototype()) { //for finishing tower
            tryBuild();
        }
        tryMove();
        tryDisintegrate();
        tryRepairBuilding(); //for healing
    }

    // TODO: cleanup
    boolean tryRepairPrototype() {
        RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, myTeam);
        MapLocation bestRepair = null;
        int bestHealth = 0;
        for (RobotInfo r : allies){
            MapLocation allyLoc = r.getLocation();
            if (r.getMode() == RobotMode.PROTOTYPE && rc.canRepair(allyLoc)){
                int health = r.getHealth();
                if (health < r.getType().health && health > bestHealth) {
                    bestHealth = health;
                    bestRepair = allyLoc;
                }
            }
        }
        try {
            if (bestRepair != null) {
                rc.repair(bestRepair);
                RobotInfo repairTarget = rc.senseRobotAtLocation(bestRepair);
                if (repairTarget.getHealth() == repairTarget.getType().health) {
                    watchRepairedCount++;
                }
                return true;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    // TODO: disintegrate if crowded and been around long
    void tryDisintegrate() {
        if ((LOW_LEAD_MAP && currRound < comm.P4_START)
                || (currRound > comm.P4_START && watchRepairedCount > 0)) {
            MapLocation myLoc = rc.getLocation();
            try {
                if (rc.senseLead(myLoc) == 0) {
                    rc.disintegrate();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    // TODO: should be way smarter with where we build watchtowers
    void tryBuild(){
        if (HIGH_LEAD_MAP) { // we built a miner early, so must be high lead map. we'll defend with watchtowers
            Direction bestDir = null;
            int bestRubble = GameConstants.MAX_RUBBLE;
            MapLocation myLoc = rc.getLocation();
            try {
                for (Direction dir : spawnDirections) {
                    MapLocation prospect = myLoc.add(dir);
                    int d1 = prospect.distanceSquaredTo(nearestCorner);
                    if (rc.canBuildRobot(RobotType.WATCHTOWER, dir) && d1 > comm.HQloc.distanceSquaredTo(nearestCorner) && d1 > Math.sqrt(H * W)) { //TODO: check if toeer is in unsafe location
                        int r = rc.senseRubble(prospect);
                        if (bestDir == null) {
                            bestDir = dir;
                            bestRubble = r;
                        }
                        else if (r < bestRubble) {
                            bestDir = dir;
                            bestRubble = r;
                        }
                    }
                }
                if (bestDir != null) {
                    rc.buildRobot(RobotType.WATCHTOWER, bestDir);
                    watchCount++;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else if(!comm.labIsBuilt() && currRound > comm.P4_START) {
            if (rc.getLocation().isAdjacentTo(nearestCorner) && rc.getTeamLeadAmount(myTeam) > RobotType.LABORATORY.buildCostLead) {
                if (comm.HQloc.equals(nearestCorner)) { // edge case if archon is in corner
                    for (Direction dir : spawnDirections) {
                        try {
                            if (rc.canBuildRobot(RobotType.LABORATORY, dir)){
                                rc.buildRobot(RobotType.LABORATORY, dir);
                                comm.setLabBuilt();
                                return;
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
                Direction dir = rc.getLocation().directionTo(nearestCorner);
                try {
                    if (rc.canBuildRobot(RobotType.LABORATORY, dir)){
                        rc.buildRobot(RobotType.LABORATORY, dir);
                        comm.setLabBuilt();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        /*else if(watchCount < 1 && rc.getTeamLeadAmount(myTeam) > RobotType.WATCHTOWER.buildCostLead && currRound > comm.P4_START)
        {
            Direction bestDir = null;
            int bestRubble = GameConstants.MAX_RUBBLE;
            MapLocation myLoc = rc.getLocation();
            try {
                for (Direction dir : spawnDirections) {
                    MapLocation prospect = myLoc.add(dir);
                    if (rc.canBuildRobot(RobotType.WATCHTOWER, dir) && validTowerLoc(prospect)) { //TODO: check if toeer is in unsafe location
                        int r = rc.senseRubble(prospect);
                        if (bestDir == null) {
                            bestDir = dir;
                            bestRubble = r;
                        }
                        else if (r < bestRubble) {
                            bestDir = dir;
                            bestRubble = r;
                        }
                    }
                }
                if (bestDir != null) {
                    rc.buildRobot(RobotType.WATCHTOWER, bestDir);
                    watchCount++;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }*/
    }

    // TODO: disintegrate if not emergency and havent built in x turns
    void tryMove(){
        if (!rc.isMovementReady()) return;
        MapLocation target = null;
        if (task !=2 && ((LOW_LEAD_MAP && currRound < comm.P4_START) || (currRound > comm.P4_START && watchRepairedCount > 0))) {
            target = getMineProspect();
        }

        if (target == null && !comm.labIsBuilt() && currRound > comm.P4_START) {
            if (!rc.getLocation().isAdjacentTo(nearestCorner)) target = nearestCorner;
        }
        if (target == null) target = getHurtRobot();
        //check to see if in danger
        if (target != null) {
            MapLocation myLoc = rc.getLocation();
            bfs.move(target);
            if (myLoc == rc.getLocation()) { // avoiding intersection bug
                bfs.path.move(target);
            }
        }
        else {
            if (HIGH_LEAD_MAP) {
                bfs.move(getEarlyTowerLoc());
            }
            else {
                bfs.move(explore.getExploreTarget()); // TODO: if early builder be smarter with movement and finding ideal tower locations
            }
        }
    }

    void tryRepairBuilding() {
        RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, myTeam);
        MapLocation bestRepair = null;
        int bestHealth = 0;
        for (RobotInfo r : allies) {
            MapLocation allyLoc = r.getLocation();
            if (rc.canRepair(allyLoc)){
                int health = r.getHealth();
                if (health < r.getType().getMaxHealth(r.level) && health > bestHealth) {
                    bestHealth = health;
                    bestRepair = allyLoc;
                }
            }
        }
        try {
            if (bestRepair != null) rc.repair(bestRepair);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    //TODO: use directions to save bytecode
    MapLocation getHurtRobot() {
        RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, myTeam);
        MapLocation bestRepair = null;
        int bestHealth = 10000;
        for (RobotInfo r : allies){
            MapLocation allyLoc = r.getLocation();
            int health = r.getHealth();
            if (r.getMode() == RobotMode.PROTOTYPE) return r.location; // TODO: IMPROVE
            if (r.getHealth() < r.getType().health && health < bestHealth) {
                bestHealth = health;
                bestRepair = allyLoc;
            }
        }
        return bestRepair;
    }

    boolean validTowerLoc(MapLocation loc) {
        RobotInfo[] robots = rc.senseNearbyRobots(loc,1, myTeam);
        for (RobotInfo r : robots) {
            if (r.getType() == RobotType.WATCHTOWER && r.getMode() == RobotMode.TURRET) return false;
        }
        int d1 = loc.distanceSquaredTo(nearestCorner);
        return d1 > comm.HQloc.distanceSquaredTo(nearestCorner) && d1 > Math.sqrt(H*W);
    }

    MapLocation getMineProspect() {
        MapLocation myLoc = rc.getLocation();
        MapLocation target = null;
        int bestDist = 10000;
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells){
                if (rc.senseLead(cell) > 0 || cell.distanceSquaredTo(nearestCorner) > Math.sqrt(Math.sqrt(H*W))) break;
                int dist = myLoc.distanceSquaredTo(cell);
                if (dist < bestDist) { // closest spot with no lead and not too close to corner
                    bestDist = dist;
                    target = cell;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return target;
    }

    MapLocation getEarlyTowerLoc() {
        MapLocation myLoc = rc.getLocation();
        MapLocation target = null;
        int d2 = comm.HQloc.distanceSquaredTo(nearestCorner);
        double d3 = Math.sqrt(H*W);
        int bestDist = 10000;
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells){
                int dist = cell.distanceSquaredTo(comm.HQloc);
                int d1 = cell.distanceSquaredTo(nearestCorner);
                if (dist < bestDist && d1 > d2 && d1 > d3) { // validtowerloc disregarding spacing. we need to live!
                    bestDist = dist;
                    target = cell;
                }
            }
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