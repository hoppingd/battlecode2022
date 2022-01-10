package newplayer;

import battlecode.common.*;

public class Miner extends MyRobot {



    static final int MIN_LEAD_TO_MINE = 6;
    static final int ALLY_FORCES_RANGE = 25;

    Team myTeam, enemyTeam;

    boolean moved = false;

    public Miner(RobotController rc){
        super(rc);
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        comm.readHQloc();
    }

    public void play(){
        moved = false;
        tryMine();
        tryMove();
        tryMine();
    }

    //TODO: improve
    MapLocation moveInCombat() {
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.MINER.visionRadiusSquared, enemyTeam);
        for (RobotInfo enemy : enemies) {
            // only consider offensive units
            if (!enemy.type.canAttack()) continue;
            //TODO: only consider combat units, with more weight given to watchtowers
            int myForcesCount = 0;
            RobotInfo[] myForces = rc.senseNearbyRobots(enemy.location, ALLY_FORCES_RANGE, myTeam);
            for (RobotInfo r : myForces) {
                if (r.type.canAttack()) {
                    myForcesCount += r.health;
                }
            }
            int enemyForcesCount = enemy.health;
            RobotInfo[] enemyForces = rc.senseNearbyRobots(enemy.location, RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
            for (RobotInfo r : enemyForces) {
                if (r.type.canAttack()) {
                    enemyForcesCount += r.health;
                }
            }
            if (myForcesCount < enemyForcesCount) {
                return comm.HQloc; //for now we naively path home
            }
        }
        return null;
    }

    void tryMine(){
        MapLocation myLoc = rc.getLocation();
        try {
            for (Direction dir : Direction.allDirections()) {
                MapLocation prospect = myLoc.add(dir);
                if (!(rc.onTheMap(prospect))) continue;
                int lead = rc.senseLead(prospect);
                int gold = rc.senseGold(prospect); //adds max of 45 bytecode
                while (lead > MIN_LEAD_TO_MINE) {
                    if (rc.isActionReady()) {
                        rc.mineLead(prospect);
                        lead--;
                    }
                    else {
                        return;
                    }
                }
                while (gold > 0) {
                    if (rc.isActionReady()) {
                        rc.mineGold(prospect);
                        gold--;
                    }
                    else {
                        return;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void tryMove(){
        if (moved) return;
        MapLocation loc = moveInCombat();
        if (loc == null) loc = getClosestMine();
        if (loc != null){
            bfs.move(loc);
            return;
        }
        /*if (rc.getRoundNum() - birthday > exploreRounds){
            if (goToEnemyHQ()) return;
        }*/
        //explore
        //int x = (int) (Math.random() * rc.getMapWidth());
        //int y = (int) (Math.random() * rc.getMapHeight());
        //loc = new MapLocation(x, y);
        loc = explore.getExploreTarget();
        //rc.setIndicatorDot(loc, 0, 0, 255);
        bfs.move(loc);
        return;
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

    MapLocation getClosestMine(){
        MapLocation myLoc = rc.getLocation();
        MapLocation bestMine = null;
        int bestDist = 10000;
        try {
            MapLocation leadMines[] = rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared);
            for (MapLocation mine : leadMines) { // interlinked
                int lead = rc.senseLead(mine);
                // consider gold? would add bytecode
                if (lead > MIN_LEAD_TO_MINE) {
                    if (mine.isAdjacentTo(comm.HQloc) || rc.senseNearbyRobots(mine, 2, myTeam).length > 3) continue;
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

}