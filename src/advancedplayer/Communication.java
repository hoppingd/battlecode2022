// [0] Pathing to HQ with flag: HQ_DECIDED yyyy yyxx xxxx
// [1] Task: 0 = scout, 1 = lattice, 2 = emergency 3 = explore, 4 = crunch
// [2] Symmetry: MIR HOR VERT INITIAL_SYMMETRY
// [3] Archon 1: ARCHON_SET yyyy yyxx xxxx
// [4] Archon 2: ARCHON_SET yyyy yyxx xxxx
// [5] Archon 3: ARCHON_SET yyyy yyxx xxxx
// [6] Archon 4: ARCHON_SET yyyy yyxx xxxx
// [7] EnemyArchon 1: ARCHON_SET ID(6bits) yyyy yyxx xxxx
// [8] EnemyArchon 2: ARCHON_SET ID(6bits) yyyy yyxx xxxx
// [9] EnemyArchon 3: ARCHON_SET ID(6bits) yyyy yyxx xxxx
// [10] EnemyArchon 4: ARCHON_SET ID(6bits) yyyy yyxx xxxx
// [11] Lab is built : IS_BUILT
// [12] Build code P1: CODE
// [13] Build code P2: CODE
// [14] Build code P3: CODE
// [15] Build code P4: CODE
// [16] Emergency location: yyyy yyxx xxxx
// [17] EnemyArchonID: ID
// [18] EnemyArchonID: ID
// [19] EnemyArchonID: ID
// [20] EnemyArchonID: ID
// [21] HQ score counter: COUNT
// [22] HQ score 1: score
// [23] HQ score 2: score
// [24] HQ score 3: score
// [25] HQ score 4: score
// [26] Map Lead Score: score
// [27] Spawn counter = count;

package advancedplayer;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Communication {

    final static int ALLY_ARCHON_ARRAY_START = 3;
    final static int ENEMY_ARCHON_ARRAY_START = 7;
    final static int BUILD_CODE_ARRAY_START = 12;
    final static int ENEMY_ARCHON_TO_ID = 10; // id array start - enemy archon array start
    final static int HQ_SCORE_ARRAY_START = 22;

    // TASK CODES
    final static int SCOUT = 0;
    final static int LATTICE = 1;
    final static int EMERGENCY = 2;
    final static int EXPLORE = 3;
    final static int CRUNCH = 4;

    // GLOBALS
    //P1: build scout, miners, and voyage
    //P2: spam soldiers to stop rush
    //P3: disintegrate builders to start lead engine
    //P4: start stockpiling lead for watchtowers and laboratory
    static final int P2_START = 80;
    static final int P3_START = 400; //survived rush, hopefully
    static final int P4_START = 500;
    static final int P4_SAVINGS = 1000;

    static final int HIGH_LEAD_THRESHOLD = 2000;
    static final int LOW_LEAD_THRESHOLD = 25;

    RobotController rc;
    MapLocation HQloc = null;
    MapLocation HQopposite = null;
    int numArchons; // inital archons
    int archonsAlive;
    int H, W;
    MapLocation[] allyArchons;
    MapLocation[] enemyArchons;
    int spawnID = 0;
    double leadScore = 0;

    Communication(RobotController rc) {
        this.rc = rc;
        archonsAlive = numArchons = rc.getArchonCount();
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        allyArchons = new MapLocation[numArchons];
        enemyArchons = new MapLocation[numArchons];
    }

    void init() {

    }

    int getSpawnCount() {
        int spawnCounter = 0;
        try {
            spawnCounter = rc.readSharedArray(27);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return spawnCounter;
    }

    void incSpawnCounter() {
        try {
            int spawnCounter = 0;
            spawnCounter = rc.readSharedArray(27);
            rc.writeSharedArray(27, spawnCounter + 1);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void fixSpawnID() {
        spawnID -= numArchons - rc.getArchonCount();
        archonsAlive = rc.getArchonCount();
    }

    void readLeadScore() {
        try {
            leadScore = rc.readSharedArray(26);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    // writes to first available archon location. also writes lead score and sets spawnid.
    void writeAllyArchonLocation(int leadScore) {
        try {
            for (int i = ALLY_ARCHON_ARRAY_START; i < ALLY_ARCHON_ARRAY_START + numArchons; i++) {
                boolean ARCHON_SET = (rc.readSharedArray(i) >> 12) == 1; // ARCHON_SET yyyy yyxx xxxx
                if (!ARCHON_SET) {
                    int code = (1 << 12) + (rc.getLocation().y << 6) + rc.getLocation().x;
                    rc.writeSharedArray(i, code);
                    System.err.println("wrote ally archon at index " + i + " location " + rc.getLocation());
                    spawnID = i - ALLY_ARCHON_ARRAY_START;
                    int score = rc.readSharedArray(26);
                    score += leadScore/numArchons;
                    rc.writeSharedArray(26, score);
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

    //TODO: clean up
    //write enemy archon location. should check for ids and update if changed location. can also update if one is destroyed;
    void writeEnemyArchonLocation(RobotInfo r) {
        try {
            for (int i = ENEMY_ARCHON_ARRAY_START; i < ENEMY_ARCHON_ARRAY_START + numArchons; i++) {
                boolean ARCHON_SET = (rc.readSharedArray(i) >> 12) == 1; // ARCHON_SET yyyy yyxx xxxx
                boolean SAME_ID = r.ID == rc.readSharedArray(rc.readSharedArray(i + ENEMY_ARCHON_TO_ID));
                // annoying possible bug here where id is 0 so it seems like the robot was already discovered. prob need an id set bit
                if (r.ID == 0) SAME_ID = 900 == rc.readSharedArray(rc.readSharedArray(i + ENEMY_ARCHON_TO_ID));
                if (!ARCHON_SET || SAME_ID) {
                    int code = (1 << 12) + (r.location.y << 6) + r.location.x;
                    System.err.println("wrote enemy archon at index " + i + " location " + new MapLocation(r.location.x, r.location.y));
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
            HQopposite = getHQOpposite();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return true;
    }

    // sets HQLoc. if HQ has already been decided, returns false
    boolean decideHQ() {
        try {
            boolean HQ_DECIDED = (rc.readSharedArray(0) >> 12) == 1; // HQ_DECIDED yyyy yyxx xxxx
            if (HQ_DECIDED) return false;
            //System.err.println("wrote hq at " + rc.getLocation());
            int bestScore = 40000;
            MapLocation bestLoc = allyArchons[0];
            for (int i = HQ_SCORE_ARRAY_START; i < HQ_SCORE_ARRAY_START + numArchons; i++) {
                int score = rc.readSharedArray(i);
                if (score <=  bestScore) { //if the scores are equal, we'll take the higher id, just to be unpredictable
                    bestScore = score;
                    bestLoc = allyArchons[i - HQ_SCORE_ARRAY_START];
                }
            }
            int code = (1 << 12) + (bestLoc.y << 6) + bestLoc.x;
            rc.writeSharedArray(0, code);
            HQloc = bestLoc;
            HQopposite = getHQOpposite();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return true;
    }

    // lower score = better hq
    void writeScore(int avgRubble, int myRubble) {
        try {
            readAllyArchonLocations();
            int index = rc.readSharedArray(21);
            int leadScore = rc.readSharedArray(26);
            int score = 1800; // max from corner weight
            System.err.println("lead score = " + leadScore / (double)numArchons);
            System.err.println("lead score = " + ((leadScore / (double)numArchons) * (400.0/(H*W))));
            score -= allyArchons[index].distanceSquaredTo(new MapLocation((W-1)/2, (H-1)/2)); // distance from center
            //System.err.println("score after dist from center" + score);
            for (int i = 0; i < numArchons; i++) {
                score += allyArchons[index].distanceSquaredTo(allyArchons[i]) / numArchons; //scale with map size?
            }
            //System.err.println("score after dist from archons " + score);
             // distance from other archons
             // my rubble score
            score += Math.pow(avgRubble, 2); // avg weighted rubble score
            //System.err.println("score after avg rubble " + score);
            score += (int)Math.pow(myRubble, 1.5);
            //System.err.println("score after my rubble " + score);
            if (score < 0) score = 0;
            rc.writeSharedArray(HQ_SCORE_ARRAY_START + index, score);
            rc.writeSharedArray(21, index+1);

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



