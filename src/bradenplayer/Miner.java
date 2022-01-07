package bradenplayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;

public class Miner extends MyRobot {


    boolean moved = false;

    Team myTeam, enemyTeam;

    public Miner(RobotController rc){
        super(rc);
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
    }

    public void play(){
        moved = false;
        tryMine();
        tryMove();
    }

    void tryMine(){
        MapLocation myLoc = rc.getLocation();
        if (rc.canMineGold(myLoc)) {
            try {
                rc.mineGold(myLoc);
                moved = true; //temp fix to keep mining
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else if (rc.canMineLead(myLoc)) {
            try {
                rc.mineLead(myLoc);
                moved = true; //temp fix to keep mining
            } catch (Throwable t) {
                t.printStackTrace();
            }
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
        int x = (int) (Math.random() * rc.getMapWidth());
        int y = (int) (Math.random() * rc.getMapHeight());
        loc = new MapLocation(x, y);
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
        int bestGold = 0;
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells) { // interlinked
                if (!rc.canSenseLocation(cell)) continue; // needed?
                int lead = rc.senseLead(cell);
                if (lead > 0) {
                    if (bestMine == null && !rc.canSenseRobotAtLocation(cell)) bestMine = cell;
                    // probably check for the closest deposit
                    // consider the amount of lead in the deposit
                    // consider if other bots are going there
                }
                int gold = rc.senseGold(cell);
                if (gold > bestGold) {
                    bestMine = cell;
                    bestGold = gold;
                    // if there is gold, for now we will go for the largest deposit
                }
                // if the cell hasn't been explored, get the rubble info. if we know the map's symmetry, we can update
                // the matching cell as well
                /*
                if ((map[cell.x][cell.y] & EXPLORE_BIT) != EXPLORE_BIT) {
                    map[cell.x][cell.y] = rc.senseRubble(cell);
                }
                else { // set to explored
                    map[cell.x][cell.y] |= EXPLORE_BIT;
                }
                */
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return bestMine;
    }

}