// [0] Pathing to HQ with flag: HQ_DECIDED yyyy yyxx xxxx
// [1] Task and build codes: p4 (3 bits) p3 (3 bits) p2 (3 bits) p1 (3 bits) task (3 bits)
// [2] Symmetry: MIR HOR VERT INITIAL_SYMMETRY (not currently used
// [3] Archon 1: ARCHON_SET yyyy yyxx xxxx
// [4] Archon 2: ARCHON_SET yyyy yyxx xxxx
// [5] Archon 3: ARCHON_SET yyyy yyxx xxxx
// [6] Archon 4: ARCHON_SET yyyy yyxx xxxx
// [7] EnemyArchon 1: ARCHON_SET yyyy yyxx xxxx
// [8] EnemyArchon 2: ARCHON_SET yyyy yyxx xxxx
// [9] EnemyArchon 3: ARCHON_SET yyyy yyxx xxxx
// [10] EnemyArchon 4: ARCHON_SET yyyy yyxx xxxx
// [11] MinerCode: 0|1
// [12] builderIsBuilt: 0|1
// [13] labIsBuilt: 0|1
// [14] shouldBuildLab: 0|1
// [15] FREE
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
// [27] Spawn counter = count
// [28] Crunch index = idx
// [29-63] Enemy logs and ids = yyyy yyxx xxxx / id (6 bits)

package hopeplayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Communication {

    final static int ALLY_ARCHON_ARRAY_START = 3;
    final static int ENEMY_ARCHON_ARRAY_START = 7;
    final static int ENEMY_ARCHON_TO_ID = 10; // id array start - enemy archon array start
    final static int HQ_SCORE_ARRAY_START = 22;
    final static int ENEMY_LOGS_ARRAY_START = 29;

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
    static final int P3_START = 200; //survived rush, hopefully
    static final int P4_START = 1500;
    static final int P4_SAVINGS = 325;

    static final int HIGH_LEAD_THRESHOLD = 2000;
    static final int LOW_LEAD_THRESHOLD = 25;
    static final int MAX_LOGGED_ENEMIES = 12;

    RobotController rc;
    MapLocation HQloc = null;
    MapLocation HQopposite = null;

    int numArchons; // inital archons
    int archonsAlive;
    int H, W;

    MapLocation[] loggedEnemies;

    MapLocation[] allyArchons;
    MapLocation[] enemyArchons;

    int spawnID = 0;
    double leadScore = 0;
    int localIndex = 0;

    Communication(RobotController rc) {
        this.rc = rc;
        archonsAlive = numArchons = rc.getArchonCount();
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        loggedEnemies = new MapLocation[MAX_LOGGED_ENEMIES];
        allyArchons = new MapLocation[numArchons];
        enemyArchons = new MapLocation[numArchons];
        try {
            localIndex = rc.readSharedArray(ENEMY_LOGS_ARRAY_START + MAX_LOGGED_ENEMIES);
        } catch (Throwable t) {
            t.printStackTrace();
        }
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
            int spawnCounter = rc.readSharedArray(27);
            rc.writeSharedArray(27, spawnCounter + 1);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void fixSpawnID() {
        spawnID -= archonsAlive - rc.getArchonCount();
        archonsAlive = rc.getArchonCount();
    }

    int getCrunchIdx() {
        int idx = 0;
        try {
            idx = rc.readSharedArray(28);
            idx %= numArchons;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return idx;
    }

    void incCrunchIdx() {
        try {
            int idx = rc.readSharedArray(28);
            if (idx == 59999) idx = -1;
            rc.writeSharedArray(28, idx + 1);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void readLeadScore() {
        try {
            leadScore = rc.readSharedArray(26);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    int getMinerCode() {
        int minerCode = 0;
        try {
            minerCode = rc.readSharedArray(11);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return minerCode;
    }

    void setMinerCode(int minerCode) {
        try {
            rc.writeSharedArray(11, minerCode);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    //returns nearest newly logged enemy
    MapLocation getLoggedEnemies() {
        MapLocation bestLoc = null;
        MapLocation myLoc = rc.getLocation();
        try {
            int sharedIndex = rc.readSharedArray(ENEMY_LOGS_ARRAY_START + MAX_LOGGED_ENEMIES);
            while (localIndex != sharedIndex) {
                int code = rc.readSharedArray(ENEMY_LOGS_ARRAY_START + localIndex);
                int x = code & 0x3F;
                int y = (code >> 6) & 0x3F;
                loggedEnemies[localIndex] = new MapLocation(x, y);
                if (bestLoc == null) bestLoc = loggedEnemies[localIndex];
                else if (myLoc.distanceSquaredTo(loggedEnemies[localIndex]) < myLoc.distanceSquaredTo(bestLoc)) {
                    bestLoc = loggedEnemies[localIndex];
                }
                localIndex = (localIndex + 1) % MAX_LOGGED_ENEMIES;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return bestLoc;
    }

    void writeEnemyToLog(MapLocation loc) {
        try {
            // avoid repeats
            /*for (int i = 0; i < MAX_LOGGED_ENEMIES; i++) {
                if (loggedEnemies[i] == null) continue;
                if (loggedEnemies[i].equals(loc)) return;
            }*/
            int sharedIndex = rc.readSharedArray(ENEMY_LOGS_ARRAY_START + MAX_LOGGED_ENEMIES);
            int code = (loc.y << 6) + loc.x;
            rc.writeSharedArray(ENEMY_LOGS_ARRAY_START + sharedIndex, code);
            sharedIndex = (sharedIndex + 1) % MAX_LOGGED_ENEMIES;
            rc.writeSharedArray(ENEMY_LOGS_ARRAY_START + MAX_LOGGED_ENEMIES, sharedIndex);
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
                    //System.err.println("wrote ally archon at index " + i + " location " + rc.getLocation());
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
                    //System.err.println("read ally archon at index " + i + " location " + allyArchons[i - ALLY_ARCHON_ARRAY_START]);

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
                int readID = rc.readSharedArray(i + ENEMY_ARCHON_TO_ID);
                boolean SAME_ID = (r.ID == readID);
                // only write to unused space or same id space
                if ((!ARCHON_SET && readID == 0) || SAME_ID) {
                    int code = (1 << 12) + (r.location.y << 6) + r.location.x;
                    //System.err.println("wrote enemy archon at index " + i + " location " + new MapLocation(r.location.x, r.location.y));
                    rc.writeSharedArray(i, code);
                    rc.writeSharedArray(i + ENEMY_ARCHON_TO_ID, r.ID);
                    return;
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void wipeEnemyArchonLocation(int i) {
        try {
            rc.writeSharedArray(ENEMY_ARCHON_ARRAY_START + i, 0);
            enemyArchons[i] = null;
        } catch (Throwable t) {
            t.printStackTrace();
        }
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
                    enemyArchons[i - ENEMY_ARCHON_ARRAY_START] = null;
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
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

    // updates the HQ location in case it moves
    void updateHQ() {
        int code = (1 << 12) + (rc.getLocation().y << 6) + rc.getLocation().x;
        try {
            rc.writeSharedArray(0, code);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        HQloc = rc.getLocation();
        HQopposite = getHQOpposite();
    }

    // lower score = better hq
    void writeScore(int avgRubble, int myRubble) {
        try {
            readAllyArchonLocations();
            int index = rc.readSharedArray(21);
            int leadScore = rc.readSharedArray(26);
            int score = 1800; // max from corner weight
            //System.err.println("lead score = " + leadScore / (double)numArchons);
            //System.err.println("lead score = " + ((leadScore / (double)numArchons) * (400.0/(H*W))));
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
        return task & 0x7;
    }

    void setTask(int n) {
        try {
            int code = rc.readSharedArray(1);
            code &= 0xFFF8;
            rc.writeSharedArray(1, code | n);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    int readBuildCode(int phase) {
        int build = 0;
        try {
            int code = rc.readSharedArray(1);
            build = (code >> 3*phase) & 0x7;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return build;
    }

    void writeBuildCode(int phase, int buildCode) {
        try {
            int code = rc.readSharedArray(1);
            int mask = ~(0x7 << (3*phase));
            code &= mask;
            code |= (buildCode << (3*phase));
            rc.writeSharedArray(1, code);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    boolean labIsBuilt() {
        boolean isBuilt = false;
        try {
            isBuilt = rc.readSharedArray(13) == 1;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return isBuilt;
    }

    void setLabBuilt() {
        try {
            rc.writeSharedArray(13, 1);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    boolean builderIsBuilt() {
        boolean isBuilt = false;
        try {
            isBuilt = rc.readSharedArray(12) == 1;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return isBuilt;
    }

    void setBuilderBuilt() {
        try {
            rc.writeSharedArray(12, 1);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    boolean getShouldBuildLab() {
        boolean isBuilt = false;
        try {
            isBuilt = rc.readSharedArray(14) == 1;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return isBuilt;
    }

    void setShouldBuildLab() {
        try {
            rc.writeSharedArray(14, 1);
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
            int code = rc.readSharedArray(16); // yyyy yyxx xxxx
            int x = code & 0x3F;
            int y = (code >> 6) & 0x3F;
            loc = new MapLocation(x, y);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return loc;
    }

    class LogEntry {
        MapLocation location;
        int id;

        LogEntry(MapLocation location, int id) {
            this.location = location;
            this.id = id;
        }
    }
}




