package newplayer;

import battlecode.common.*;

public class Miner extends MyRobot {

    static final int MIN_LEAD_TO_MINE = 10;
    Team myTeam, enemyTeam;

    boolean moved = false;
    boolean mined = false;

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
    }

    void tryMine(){
        if (mined) return;
        MapLocation bestMine = null;
        int bestAmount = 0;
        int bestGold = 0;
        try {
            MapLocation leadMines[] = rc.senseNearbyLocationsWithLead(RobotType.MINER.actionRadiusSquared);
            for (MapLocation mine : leadMines) {
                int lead = rc.senseLead(mine);
                if (lead > MIN_LEAD_TO_MINE) {
                    if (bestMine == null) {
                        bestMine = mine;
                        bestAmount = lead;
                    }
                    if (lead > bestAmount) {
                        bestMine = mine;
                        bestAmount = lead;
                    }
                }
            }
            MapLocation goldMines[] = rc.senseNearbyLocationsWithLead(RobotType.MINER.actionRadiusSquared);
            for (MapLocation mine : goldMines) {
                int gold = rc.senseGold(mine);
                if (gold > bestGold) {
                    bestMine = mine;
                    bestGold = gold;
                }
                // probably check for the closest deposit
                // consider the amount of gold in the deposit
                // consider if other bots are going there
            }
            if (bestMine != null) {
                if (rc.canMineGold(bestMine)) {
                    rc.mineGold(bestMine);
                }
                else if (rc.canMineLead(bestMine)) {
                    rc.mineLead(bestMine);
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void tryMove(){
        if (moved) return;
        MapLocation loc = getClosestMine();
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
        int bestGold = 0;
        try {
            MapLocation leadMines[] = rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared);
            for (MapLocation mine : leadMines) { // interlinked
                int lead = rc.senseLead(mine);
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
                    // probably check for the closest deposit
                    // consider the amount of lead in the deposit
                    // consider if other bots are going there
                }
            }
            MapLocation goldMines[] = rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared);
            for (MapLocation mine : goldMines) { // interlinked
                int gold = rc.senseGold(mine);
                if (gold > bestGold) {
                    bestMine = mine;
                    bestGold = gold;
                }
                // probably check for the closest deposit
                // consider the amount of gold in the deposit
                // consider if other bots are going there
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return bestMine;
    }

}