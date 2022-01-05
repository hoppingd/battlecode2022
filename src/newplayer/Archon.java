package newplayer;

import battlecode.common.RobotController;
import battlecode.common.Direction;
import battlecode.common.RobotType;

public class Archon extends MyRobot {

    RobotController rc;

    Archon(RobotController rc){
        this.rc = rc;
    }

    void play(){
        for (Direction dir : Direction.allDirections()) {
            try {
                if (rc.canBuildRobot(RobotType.MINER, dir)) rc.buildRobot(RobotType.MINER, dir); // we simply spam miners
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

}

