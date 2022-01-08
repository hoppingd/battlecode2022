package newplayer;

import battlecode.common.*;

public class Builder extends MyRobot {


    boolean moved = false;
    int watchCount = 0;
    Team myTeam, enemyTeam;

    public Builder(RobotController rc){
        super(rc);
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
    }

    public void play(){
        moved = false;
        if(!tryRepairPrototype()) { //for finishing tower
            tryBuild();
        }
        tryMove();
        tryRepairBuilding(); //for healing
    }

    boolean tryRepairPrototype() {
        MapLocation myLoc = rc.getLocation();
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

    void tryBuild(){
        if(watchCount < 1 && rc.getTeamLeadAmount(myTeam) > RobotType.WATCHTOWER.buildCostLead)
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
        MapLocation target = getHurtRobot();
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

}