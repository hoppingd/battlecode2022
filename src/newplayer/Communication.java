// [0] Pathing to HQ with flag: HQ_DECIDED yyyy yyxx xxxx
// [1] Type flags: IS_SCOUT

package newplayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Communication {

    RobotController rc;
    MapLocation HQloc = null;

    Communication(RobotController rc) {
        this.rc = rc;
    }

    void init() {

    }

    void readMessages() {

    }

    // updates comm.HQloc. assumes HQ has been decided
    void readHQloc() {
        try {
            int HQ = rc.readSharedArray(0); //HQ_DECIDED yyyy yyxx xxxx
            int x = HQ & 0x3F;
            int y = (HQ >> 6) & 0x3F;
            HQloc = new MapLocation(x,y);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // sets HQLoc. if HQ has already been decided, returns false
    boolean setHQloc(MapLocation myLoc) {
        try {
            boolean HQ_DECIDED = (rc.readSharedArray(0) >> 12) == 1; // HQ_DECIDED yyyy yyxx xxxx
            if (HQ_DECIDED) return false;
            //System.err.println("wrote hq at " + rc.getLocation());
            int code = (1 << 12) + (myLoc.y << 6) + myLoc.x;
            rc.writeSharedArray(0, code);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return true;
    }

    // gets type, 0 == scout, 1 == normal. the scout will update the array. if we need to, we can condense info instead
    // of using a new array index for each
    int getTask() {
        int task = 0;
        try {
            task = rc.readSharedArray(1);
            if (task == 0) {
                rc.writeSharedArray(1, 1);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return task;
    }
}
