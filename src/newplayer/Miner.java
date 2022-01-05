package newplayer;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.HashSet;

public class Miner extends MyRobot {

    final int EXPLORE_BIT = 0x80;
    final int RUBBLE_BITS = 0x7F;
    RobotController rc;
    Pathfinding path;
    MapLocation target;
    MapLocation bestMine;
    MapLocation myLoc;
    int H;
    int W;
    int map[][]; // 7 bits to represent rubble value, 1 bit to represent if explored (may want other bits to represent deposits

    Miner(RobotController rc){
        this.rc = rc;
        path = new Pathfinding(rc);
        target = null;
        bestMine = null;
        myLoc = rc.getLocation();
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        map = new int[H][W];

    }

    // checks all cells regardless of whether they have been explored
    void checkCells() {
        bestMine = null;
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
    }

    void play() {
        // probably read comms here to make sure we weren't created to mine a specific location
        if (target == null) {
            checkCells(); // look for a mine. right now we check all cells no matter what
            target = bestMine;
        }
        else if (rc.canMineGold(target)) {
            try {
                rc.mineGold(target);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else if (rc.canMineLead(target)) {
            try {
                rc.mineLead(target);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        else if (myLoc == target || rc.canSenseRobotAtLocation(target)) {
            target = null;
            checkCells(); // look for a mine. right now we check all cells no matter what
            target = bestMine;
        }

        // if target is still null, we need to explore
        if (target == null) {
            // explore. we may want to terminate exploration with comms. exploration is currently random
            int x = (int) (Math.random() * rc.getMapWidth());
            int y = (int) (Math.random() * rc.getMapHeight());
            MapLocation newLoc = new MapLocation(x, y);
            //if ((map[newLoc.x][newLoc.y]&EXPLORE_BIT) > 0) continue;
            target = newLoc;
        }

        if (target != null) {
            path.move(target);
            myLoc = rc.getLocation();
        }
    }

}

