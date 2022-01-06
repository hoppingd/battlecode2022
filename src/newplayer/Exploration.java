package newplayer;

import battlecode.common.Direction;
import battlecode.common.RobotController;

public class Exploration {

    RobotController rc;
    Direction lastDirMoved;

    Exploration() { this.rc = rc; }
    void move(Direction dir){
        /*try{
            if (!rc.canMove(dir)) return;
            rc.move(dir);
            lastDirMoved = dir;
        } catch (Exception e){
            e.printStackTrace();
        }*/
    }

    void initTurn() {

    }

    void initialize() {

    }
}
