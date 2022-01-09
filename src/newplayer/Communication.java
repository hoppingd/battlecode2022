// [0] Pathing to HQ with flag: HQ_DECIDED yyyy yyxx xxxx
// [1] Task: 0 = scout, 1 = lattice, 2 = emergency
// [2] Symmetry: MIR HOR VERT INITIAL_SYMMETRY
// [3] Archon 1: ARCHON_SET yyyy yyxx xxxx
// [4] Archon 2: ARCHON_SET yyyy yyxx xxxx
// [5] Archon 3: ARCHON_SET yyyy yyxx xxxx
// [6] Archon 4: ARCHON_SET yyyy yyxx xxxx
// [7] EnemyArchon 1: ARCHON_SET yyyy yyxx xxxx
// [8] EnemyArchon 2: ARCHON_SET yyyy yyxx xxxx
// [9] EnemyArchon 3: ARCHON_SET yyyy yyxx xxxx
// [10] EnemyArchon 4: ARCHON_SET yyyy yyxx xxxx
// [11] Lab is built : IS_BUILT
// [12] Build code P1: CODE
// [13] Build code P2: CODE
// [14] Build code P3: CODE
// [15] Build code P4: CODE
// [16] Emergency location: yyyy yyxx xxxx

package newplayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Communication {

    final static int ALLY_ARCHON_ARRAY_START = 3;
    final static int ENEMY_ARCHON_ARRAY_START = 7;
    final static int BUILD_CODE_ARRAY_START = 12;
    final static int NUM_PHASES = 4;

    RobotController rc;
    MapLocation HQloc = null;
    MapLocation enemyHQloc = null; // for now, we only keep track of the enemy archon that matches our HQ
    int numArchons;
    int H, W;
    MapLocation[] allyArchons;
    MapLocation[] enemyArchons;

    Communication(RobotController rc) {
        this.rc = rc;
        numArchons = rc.getArchonCount();
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        allyArchons = new MapLocation[numArchons];
        enemyArchons = new MapLocation[numArchons];

    }

    void init() {

    }

    // writes to first available archon location
    void writeAllyArchonLocation() {
        try {
            for (int i = ALLY_ARCHON_ARRAY_START; i < ALLY_ARCHON_ARRAY_START + numArchons; i++) {
                boolean ARCHON_SET = (rc.readSharedArray(i) >> 12) == 1; // ARCHON_SET yyyy yyxx xxxx
                if (!ARCHON_SET) {
                    int code = (1 << 12) + (rc.getLocation().y << 6) + rc.getLocation().x;
                    rc.writeSharedArray(i, code);
                    System.err.println("wrote ally archon at index " + i + " location " + rc.getLocation());
                    return;
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
        initialSymmetry();
        return;
    }

    // reads ally archon locations. returns true if all have been read.
    boolean readAllyArchonLocations() {
        if (allyArchons[numArchons - 1] != null) return true;
        try {
            for (int i = ALLY_ARCHON_ARRAY_START; i < ALLY_ARCHON_ARRAY_START + numArchons; i++) {
                int code = rc.readSharedArray(i);
                boolean ARCHON_SET = (rc.readSharedArray(i) >> 12) == 1; // ARCHON_SET yyyy yyxx xxxx
                if (ARCHON_SET) {
                    int x = code & 0x3F;
                    int y = (code >> 6) & 0x3F;
                    allyArchons[i - ALLY_ARCHON_ARRAY_START] = new MapLocation(x,y);
                    System.err.println("read ally archon at index " + i + " location " + allyArchons[i - ALLY_ARCHON_ARRAY_START]);

                }
                else {
                    return false;
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
        initialSymmetry();
        return true;
    }

    //write enemy archon location. should check for ids and update if changed location. can also update if one is destroyed;
    void writeEnemyArchonLocation(MapLocation loc) {
        try {
            for (int i = ENEMY_ARCHON_ARRAY_START; i < ENEMY_ARCHON_ARRAY_START + numArchons; i++) {
                boolean ARCHON_SET = (rc.readSharedArray(i) >> 12) == 1; // ARCHON_SET yyyy yyxx xxxx
                if (!ARCHON_SET) {
                    int code = (1 << 12) + (loc.y << 6) + loc.x;
                    System.err.println("wrote enemy archon at index " + i + " location " + loc);
                    rc.writeSharedArray(i, code);
                    return;
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return;
    }

    //reads enemy archon locations
    void readEnemyArchonLocations() {
        try {
            for (int i = ENEMY_ARCHON_ARRAY_START; i < ENEMY_ARCHON_ARRAY_START + numArchons; i++) {
                int code = rc.readSharedArray(i);
                boolean ARCHON_SET = (rc.readSharedArray(i) >> 12) == 1; // ARCHON_SET yyyy yyxx xxxx
                if (ARCHON_SET) {
                    int x = code & 0x3F;
                    int y = (code >> 6) & 0x3F;
                    enemyArchons[i - ENEMY_ARCHON_ARRAY_START] = new MapLocation(x,y);
                }
                else {
                    return;
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return;
    }

    void initialSymmetry() {
        try {
            int code = rc.readSharedArray(2);
            boolean INITIAL_SYMMETRY = (code & 1) == 1; // [2] Symmetry: MIR HOR VERT INITIAL_SYMMETRY
            if (INITIAL_SYMMETRY) return;
            int VERT_SYMMETRY = 0;
            int HORIZ_SYMMETRY = 0;
            int MIRROR_SYMMETRY = 0;
            //System.err.println("wrote hq at " + rc.getLocation());
            //TODO: calculate symmetry
            code = (MIRROR_SYMMETRY << 3) + (HORIZ_SYMMETRY << 2) + (VERT_SYMMETRY << 1) + 1;
            rc.writeSharedArray(2, code);

        } catch (Throwable t) {
            t.printStackTrace();
        }
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
            HQloc = myLoc;
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
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return task;
    }

    void setTask(int n) {
        try {
            rc.writeSharedArray(1, n);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    int readBuildCode(int phase) {
        int build = 0;
        try {
            build = rc.readSharedArray(BUILD_CODE_ARRAY_START - 1 + phase);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return build;
    }

    void writeBuildCode(int phase, int buildCode) {
        try {
            rc.writeSharedArray(BUILD_CODE_ARRAY_START - 1 + phase, buildCode);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return;
    }

    boolean labIsBuilt() {
        boolean isBuilt = false;
        try {
            isBuilt = rc.readSharedArray(11) == 1;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return isBuilt;
    }

    void setLabBuilt() {
        try {
            rc.writeSharedArray(11, 1);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    MapLocation getHQOpposite() {
        if (HQloc == null) return null;
        return new MapLocation(W - HQloc.x - 1,H - HQloc.y - 1);
    }

    // sets emergency location
    void setEmergencyLoc(MapLocation loc) {
        try {
            int code = (1 << 12) + (loc.y << 6) + loc.x;
            rc.writeSharedArray(16, code);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    MapLocation getEmergencyLoc() {
        MapLocation loc = null;
        try {
            int code = rc.readSharedArray(0); //HQ_DECIDED yyyy yyxx xxxx
            int x = code & 0x3F;
            int y = (code >> 6) & 0x3F;
            loc = new MapLocation(x, y);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return loc;
    }
}




