// [0] Pathing to HQ with flag: HQ_DECIDED yyyy yyxx xxxx
// [1] Type flags: IS_SCOUT
// [2] Enemy HQ location and symmetry: MIR HOR VERT HQ_FOUND yyyy yyxx xxxx

package newplayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Communication {

    RobotController rc;
    MapLocation HQloc = null;
    MapLocation enemyHQloc = null; // for now, we only keep track of the enemy archon that matches our HQ

    Communication(RobotController rc) {
        this.rc = rc;
    }

    void init() {

    }

    void readMessages() {

    }

    // updates comm.HQloc. returns false if HQ has not been decided
    boolean readHQloc() {
        try {
            if (HQloc != null) return true;
            int HQ = rc.readSharedArray(0); //HQ_DECIDED yyyy yyxx xxxx
            boolean HQ_DECIDED = (rc.readSharedArray(0) >> 12) == 1; // HQ_DECIDED yyyy yyxx xxxx
            if (!HQ_DECIDED) return false;
            int x = HQ & 0x3F;
            int y = (HQ >> 6) & 0x3F;
            HQloc = new MapLocation(x,y);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return true;
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

    void setEnemyHQloc(MapLocation loc) { //MIR HOR VERT HQ_FOUND yyyy yyxx xxxx
        try {
            int oldCode = rc.readSharedArray(2);
            System.err.println("wrote enemy hq at " + loc);
            int code = ((1 << 12) + (loc.y << 6) + loc.x) | oldCode; // or with old code in case symmetry was detected prior
            rc.writeSharedArray(2, code);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    boolean readEnemyHQloc() {
        try {
            if (enemyHQloc != null) return true;
            int code = rc.readSharedArray(2); //MIR HOR VERT HQ_FOUND yyyy yyxx xxxx
            boolean HQ_FOUND = (rc.readSharedArray(2) >> 12) == 1; //MIR HOR VERT HQ_FOUND yyyy yyxx xxxx
            if (!HQ_FOUND) return false;
            int x = code & 0x3F;
            int y = (code >> 6) & 0x3F;
            enemyHQloc = new MapLocation(x,y);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return true;

    }
    void setSymmetry(boolean isHorizontal) { //MIR HOR VERT HQ_FOUND yyyy yyxx xxxx
        try {
            int oldCode = rc.readSharedArray(2);
            System.err.println("set symmetry. isHorizontal: " + isHorizontal);
            int code = 0;
            if (isHorizontal) {
                code = 1 << 14;
            }
            else {
                code = 1 << 13;
            }
            code |= oldCode;
            rc.writeSharedArray(2, code);
        } catch (Throwable t) {
            t.printStackTrace();
        }
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
