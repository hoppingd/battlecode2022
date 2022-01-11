package advancedplayer;

import battlecode.common.*;

public class Miner extends MyRobot {



    static final int MIN_LEAD_TO_MINE = 6;
    static final int ALLY_FORCES_RANGE = 29;

    int H, W;
    Team myTeam, enemyTeam;

    boolean moved = false;
    RobotInfo[] nearbyEnemies;
    double mapLeadScore = 50;

    public Miner(RobotController rc){
        super(rc);
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        comm.readHQloc();
        mapLeadScore = (comm.leadScore / (double)comm.numArchons) * (400.0/(H*W));
    }

    public void play(){
        nearbyEnemies = rc.senseNearbyRobots(RobotType.MINER.visionRadiusSquared, enemyTeam);
        moved = false;
        tryMine();
        tryMove();
        tryMine();
    }

    //TODO: improve
    MapLocation moveInCombat() {
        for (RobotInfo enemy : nearbyEnemies) {
            //sense enemyArchons
            if (enemy.getType() == RobotType.ARCHON) {
                comm.writeEnemyArchonLocation(enemy);
                try {
                    if (mapLeadScore < comm.HIGH_LEAD_THRESHOLD && rc.getRoundNum() < 500 && rc.senseNearbyLocationsWithLead(RobotType.SOLDIER.visionRadiusSquared).length > 12) { // sense not rush
                        comm.setTask(4); // RUSH!
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
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
                if (comm.HQloc != null) return comm.HQloc; //for now we naively path home
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
                // TODO: improve
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
        loc = explore.getExploreTarget();
        bfs.move(loc);
        return;
    }

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
                    if ((comm.HQloc != null && mine.isAdjacentTo(comm.HQloc)) || rc.senseNearbyRobots(mine, 2, myTeam).length > 2) continue;
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