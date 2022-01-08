package newplayer;

import battlecode.common.*;

public class Archon extends MyRobot {

    Team myTeam, enemyTeam;

    int minersBuilt = 0;
    int builderCount = 0;
    int depositsDetected = 0;
    int numBuildersNeed = 1;
    boolean arrived = false;

    public Archon(RobotController rc){
        super(rc);
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
    }

    public void play(){
        if (minersBuilt < 1 || rc.getRoundNum() % 250 == 0) { //update periodically
            getMines();
        };
        if (rc.getMode() == RobotMode.TURRET) {
            if (!arrived) {
                if (minersBuilt > depositsDetected) {
                    try {
                        if (comm.setHQloc(rc.getLocation())) {
                            arrived = true;
                        }
                        else {
                            //System.err.println("hq should be going portable at " + rc.getLocation());
                            if (rc.isTransformReady()) {
                                rc.transform();
                            }
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

    void tryMove(){
        if (comm.HQloc == null) {
            comm.readHQloc();
        }
        if (rc.getLocation().isWithinDistanceSquared(comm.HQloc, 10)) {
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
        /*if (rc.getRoundNum() - birthday > exploreRounds){
            if (goToEnemyHQ()) return;
        }*/
        return;
    }

    void tryBuild(){
        if (minersBuilt <= depositsDetected) {
            for (Direction dir : Direction.allDirections()) {
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
        /*else if(builderCount < numBuildersNeed && rc.getTeamLeadAmount(myTeam) > RobotType.BUILDER.buildCostLead)
        {
            if(rc.getArchonCount() > 2)
            {
                numBuildersNeed = 2; // delay 2nd builder
            }
            for (Direction dir : Direction.allDirections())
            {
                try {
                    if (rc.canBuildRobot(RobotType.BUILDER, dir)){
                        if(builderCount < numBuildersNeed)
                        {
                            builderCount++;
                            rc.buildRobot(RobotType.BUILDER, dir); // we spawn builders based on num needed
                            break;
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        */
        else if (rc.getTeamLeadAmount(myTeam) > RobotType.SOLDIER.buildCostLead) {
            for (Direction dir : Direction.allDirections()) {
                try {
                    if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                        rc.buildRobot(RobotType.SOLDIER, dir); // we simply spam soldiers
                        break;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
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

}