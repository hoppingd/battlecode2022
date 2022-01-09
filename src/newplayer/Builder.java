package newplayer;

import battlecode.common.*;

import java.awt.*;

public class Builder extends MyRobot {

    static final int P4_START = 800;
    boolean moved = false;
    int watchCount = 0;
    int H, W;
    Team myTeam, enemyTeam;
    int task = 0;
    MapLocation nearestCorner;

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
        task = comm.getTask();
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
                return true;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    void tryDisintegrate() {
        if (rc.getRoundNum() < P4_START || watchCount > 0) {
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
        if(!comm.labIsBuilt()) {
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
            for (Direction dir : Direction.allDirections())
            {
                try {
                    if (rc.canBuildRobot(RobotType.WATCHTOWER, dir) && validTowerLoc(rc.getLocation().add(dir))){
                        watchCount++;
                        rc.buildRobot(RobotType.WATCHTOWER, dir); // we spawn builders based on num needed
                        break;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    void tryMove(){
        MapLocation target = null;
        if (rc.getRoundNum() < P4_START && task !=2) {
            target = getMineProspect();
        }
        else if (!comm.labIsBuilt()) {
            if (!rc.getLocation().isAdjacentTo(nearestCorner)) target = nearestCorner;
        }
        if (target == null) target = getHurtRobot();
        if (target != null) {
            bfs.move(target);
        }
        else {
            bfs.move(explore.getExploreTarget());
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
                if (health < r.getType().health && health > bestHealth) {
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