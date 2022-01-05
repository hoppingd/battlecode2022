package newplayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Direction;
import battlecode.common.RobotType;

public class Archon extends MyRobot {

    RobotController rc;
    MapLocation myLoc;
    int minersBuilt;
    int depositsDetected;

    Archon(RobotController rc){
        this.rc = rc;
        myLoc = rc.getLocation();
        minersBuilt = 0;
        depositsDetected = 0;
    }

    void checkCells() {
        try {
            MapLocation cells[] = rc.getAllLocationsWithinRadiusSquared(myLoc, rc.getType().visionRadiusSquared);
            for (MapLocation cell : cells) { // interlinked
                if (!rc.canSenseLocation(cell)) continue; // needed?
                int lead = rc.senseLead(cell);
                if (lead > 0) {
                    // should use comms here probably
                    depositsDetected++;
                }
                /*
                int gold = rc.senseGold(cell);
                if (gold > bestGold) {
                    bestMine = cell;
                    bestGold = gold;
                    // if there is gold, for now we will go for the largest deposit
                }
                */
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void play(){
        //beginning of game: archons should probably get each others locations so the archons closest to a deposits spawns the miner
        if (minersBuilt == 0) {
            for (Direction dir : Direction.allDirections()) {
                try {
                    if (rc.canBuildRobot(RobotType.MINER, dir)) {
                        rc.buildRobot(RobotType.MINER, dir); // each archon should spawn at least 1 miner
                        minersBuilt++;
                        break;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            checkCells();
        }
        while(minersBuilt <= depositsDetected) {
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
        while(rc.getTeamLeadAmount(rc.getTeam()) > 100) { // arbitrary number
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
        // could do some repairs here
        // consider sac'ing miners with comms
        // droids should report deposits back to archon and create miners

    }

}

