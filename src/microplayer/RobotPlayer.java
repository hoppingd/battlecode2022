package microplayer;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public strictfp class RobotPlayer {


    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        MyRobot r;
        switch(rc.getType()){
            case ARCHON:
                r = new Archon(rc);
                break;
            case MINER:
                r = new Miner(rc);
                break;
            case SOLDIER:
                r = new Soldier(rc);
                break;
            case LABORATORY:
                r = new Laboratory(rc);
                break;
            case WATCHTOWER:
                r = new Watchtower(rc);
                break;
            case BUILDER:
                r = new Builder(rc);
                break;
            case SAGE:
            default:
                r = new Sage(rc);
                break;
        }

        while (true) {
            r.initTurn();
            r.play();
            r.endTurn();
            Clock.yield();
        }
    }
}

