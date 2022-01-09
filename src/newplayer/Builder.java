package newplayer;

import battlecode.common.*;

public class Builder extends MyRobot {

    static final int P4_START = 800;
    boolean moved = false;
    int watchCount = 0;
    Team myTeam, enemyTeam;
    int task = 0;
    MapLocation nearestCorner;

    public Builder(RobotController rc){
        super(rc);
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
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
            if (bestRepair != null) rc.repair(bestRepair);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    void tryDisintegrate() {
        if (rc.getRoundNum() < 800) {
            try {
                if (rc.senseLead(rc.getLocation()) == 0) {
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
        else if(watchCount < 1 && rc.getTeamLeadAmount(myTeam) > RobotType.WATCHTOWER.buildCostLead && rc.getRoundNum() > 800)
        {
            for (Direction dir : Direction.allDirections())
            {
                try {
                    if (rc.canBuildRobot(RobotType.WATCHTOWER, dir)){
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
        if (rc.getRoundNum() < 800 && task !=2) {
            target = getMineProspect();
        }
        else if (!comm.labIsBuilt()) {
            target = nearestCorner;
        }
        if (target == null) target = getHurtRobot();
        if (target != null) bfs.move(target);
    }

    void tryRepairBuilding() {
        MapLocation myLoc = rc.getLocation();
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

    MapLocation getMineProspect() {
        MapLocation myLoc = rc.getLocation();
        MapLocation target = null;
        int bestDist = 10000;
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells){
                if (rc.senseLead(cell) > 0) break;
                int dist = myLoc.distanceSquaredTo(cell);
                if (dist < bestDist) {
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
        int W = rc.getMapWidth();
        int H = rc.getMapHeight();
        MapLocation myLoc = rc.getLocation();
        if(W - myLoc.x > myLoc.x) {
            x = 0;
        }
        else {
            x = W;
        }
        if(H - myLoc.x > myLoc.x) {
            y = 0;
        }
        else {
            y = H;
        }
        return new MapLocation(x,y);
    }
}