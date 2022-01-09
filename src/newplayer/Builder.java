package newplayer;

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

    static final int P3_START = 400;
    static final int P4_START = 500;

    int watchCount = 0;
    int watchRepairedCount = 0;
    int H, W;
    Team myTeam, enemyTeam;
    int task = 0;
    MapLocation nearestCorner;

    boolean moved = false;
    int currRound = 0;
    boolean earlyBuilder = true;

    public Builder(RobotController rc){
        super(rc);
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        comm.readHQloc();
        nearestCorner = getNearestCorner();
    }

    public void play(){
        currRound = rc.getRoundNum();
        task = comm.getTask();
        earlyBuilder = currRound < P3_START;
        moved = false;
        if(!tryRepairPrototype()) { //for finishing tower
            tryBuild();
        }
        tryMove();
        tryDisintegrate();
        tryRepairBuilding(); //for healing
    }

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

    void tryDisintegrate() {
        if (!earlyBuilder && (currRound < P4_START || watchRepairedCount > 0)) {
            MapLocation myLoc = rc.getLocation();
            try {
                if (rc.senseLead(myLoc) == 0 && validProspect(myLoc)) {
                    rc.disintegrate();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
    void tryBuild(){
        if (earlyBuilder) { // we built a miner early, so must be high lead map. we'll defend with watchtowers
            Direction bestDir = null;
            int bestRubble = GameConstants.MAX_RUBBLE;
            try {
                for (Direction dir : spawnDirections) {
                    MapLocation prospect = rc.getLocation().add(dir);
                    int d1 = prospect.distanceSquaredTo(nearestCorner);
                    if (rc.canBuildRobot(RobotType.WATCHTOWER, dir) && d1 > comm.HQloc.distanceSquaredTo(nearestCorner) && d1 > Math.sqrt(H * W)) {
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
        else if(!comm.labIsBuilt()) {
            if (rc.getLocation().isAdjacentTo(nearestCorner) && rc.getTeamLeadAmount(myTeam) > RobotType.LABORATORY.buildCostLead) {
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
        else if(watchCount < 1 && rc.getTeamLeadAmount(myTeam) > RobotType.WATCHTOWER.buildCostLead && rc.getRoundNum() >= P4_START)
        {
            Direction bestDir = null;
            int bestRubble = GameConstants.MAX_RUBBLE;
            try {
                for (Direction dir : spawnDirections) {
                    MapLocation prospect = rc.getLocation().add(dir);
                    int d1 = prospect.distanceSquaredTo(nearestCorner);
                    if (rc.canBuildRobot(RobotType.WATCHTOWER, dir) && d1 > comm.HQloc.distanceSquaredTo(nearestCorner) && d1 > Math.sqrt(H * W)) {
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
    }

    void tryMove(){
        MapLocation target = null;
        if (currRound < P4_START && !earlyBuilder && task !=2) {
            target = getMineProspect();
        }
        else if (!comm.labIsBuilt() && !earlyBuilder) {
            if (!rc.getLocation().isAdjacentTo(nearestCorner)) target = nearestCorner;
        }
        if (target == null) target = getHurtRobot();
        //check to see if in danger
        if (target != null) {
            bfs.move(target);
        }
        else {
            if (earlyBuilder) {
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
        for (RobotInfo r : allies){
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
    MapLocation getHurtRobot() {
        MapLocation myLoc = rc.getLocation();
        RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, myTeam);
        MapLocation bestRepair = null;
        int bestHealth = 10000;
        for (RobotInfo r : allies){
            MapLocation allyLoc = r.getLocation();
            int health = r.getHealth();
            if (r.getHealth() < r.getType().health && health < bestHealth) {
                bestHealth = health;
                bestRepair = allyLoc;
            }
        }
        return bestRepair;
    }

    boolean validProspect(MapLocation loc) {
        return loc.distanceSquaredTo(comm.HQloc) > 2 && loc.distanceSquaredTo(nearestCorner) > Math.sqrt(Math.sqrt(H*W));
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
                if (rc.senseLead(cell) > 0) break;
                int dist = myLoc.distanceSquaredTo(cell);
                if (dist < bestDist && validProspect(cell)) { // closest spot with no lead and not too close to corner or hq
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
        int bestDist = 10000;
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells){
                int dist = myLoc.distanceSquaredTo(cell);
                int d1 = cell.distanceSquaredTo(nearestCorner);
                if (dist < bestDist && d1 > comm.HQloc.distanceSquaredTo(nearestCorner) && d1 > Math.sqrt(H*W)) { // validtowerloc disregarding spacing. we need to live!
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
        return new MapLocation(x,y);
    }
}