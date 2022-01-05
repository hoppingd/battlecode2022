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
    MapLocation destination;
    MapLocation bestMine;
    MapLocation myLoc;
    int H;
    int W;
    int map[][]; // 7 bits to represent rubble value, 1 bit to represent if explored (may want other bits to represent deposits
    Queue<Direction> path; // not sure if queue is best to use here

    Miner(RobotController rc){
        this.rc = rc;
        myLoc = rc.getLocation();
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        map = new int[H][W];
        path = new PriorityQueue<Direction>();
    }

    // checks all cells regardless of whether they have been explored
    void checkCells() {
        bestMine = null;
        int bestGold = 0;
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells) {
                if (!rc.canSenseLocation(cell)) continue;
                int lead = rc.senseLead(cell);
                if (lead > 0) {
                    if (bestMine == null) bestMine = cell;
                    // probably check if there is already a miner near the deposit
                    if (!rc.canSenseRobotAtLocation(cell)) bestMine = cell;
                    // probably check for the closest deposit
                    // consider the amount of lead in the deposit
                }
                int gold = rc.senseGold(cell);
                if (gold > bestGold) {
                    bestMine = cell;
                    bestGold = gold;
                    // if there is gold, for now we will go for the largest deposit
                }
                // if the cell hasn't been explored, get the rubble info. if we know the map's symmetry, we can update
                // the matching cell as well
                if ((map[cell.x][cell.y] & EXPLORE_BIT) != EXPLORE_BIT) {
                    map[cell.x][cell.y] = rc.senseRubble(cell);
                }
                else { // set to explored
                    map[cell.x][cell.y] |= EXPLORE_BIT;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void play() {
        // mine
        try {
            while (rc.canMineGold(myLoc)) { // right now we only mine if we are directly above the deposit
                rc.mineGold(myLoc); // we also deplete the entire mine, and if at any point we are above a resource we mine it
            }
            while (rc.canMineLead(myLoc)) {
                rc.mineLead(myLoc);
            }
            bestMine = null;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // probably read comms here to make sure we weren't created to mine a specific location
        if (destination == null) {
            if (bestMine == null) checkCells(); // look for a mine. right now we check all cells no matter what
            destination = bestMine;
        }
        // if destination is still null, we need to explore
        if (destination == null) {
            // explore. we may want to terminate exploration with comms
            while (destination == null) {
                int x = (int) (Math.random() * rc.getMapWidth());
                int y = (int) (Math.random() * rc.getMapHeight());
                MapLocation newLoc = new MapLocation(x, y);
                if ((map[newLoc.x][newLoc.y]&EXPLORE_BIT) > 0) continue;
                destination = newLoc;
            }
        }
        else if (path.isEmpty()) {
            // as long as there is no vortex anomaly, we can use saved rubble values to navigate to destination
            // if there is an anomaly, we will need to update the map accordingly
            // we can use dijkstra's to find the best path around rubble
            // however, for now we ignore rubble and simply move in the direction of the destination
            Direction dir = myLoc.directionTo(destination);
            if (rc.canMove(dir)) { // we can get stuck
                try {
                    rc.move(dir);
                    myLoc = rc.getLocation();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            if (myLoc == destination) destination = null;
        }
        else if (rc.canMove(path.peek())){
            try {
                rc.move(path.poll());
                myLoc = rc.getLocation();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

}

