package shmooveplayer;

import battlecode.common.*;

public class Miner extends MyRobot {

    static final Direction[] fleeDirections = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static final int MIN_LEAD_TO_MINE = 6;
    static final int DISINTEGRATE_HEALTH = 7;

    Direction[] dirs = Direction.allDirections();
    int H, W;
    Team myTeam, enemyTeam;
    int birthday;

    RobotInfo[] nearbyEnemies;
    double mapLeadScore;
    int minerCode = 0;

    public Miner(RobotController rc){
        super(rc);
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        birthday = rc.getRoundNum();
        comm.readHQloc();
        mapLeadScore = (comm.leadScore / (double)comm.numArchons) * (400.0/(H*W));
    }

    public void play(){
        comm.readHQloc();
        // alternate explore targets
        if (rc.getRoundNum() == birthday) {
            minerCode = comm.getMinerCode();
            if (minerCode == 0) {
                comm.setMinerCode(1);
            }
            else {
                comm.setMinerCode(0);
            }
        }
        comm.getLoggedEnemies();
        nearbyEnemies = rc.senseNearbyRobots(RobotType.MINER.visionRadiusSquared, enemyTeam);
        //tryDisintegrate();
        tryMine();
        if (rc.isMovementReady()) {
            tryMove();
            tryMine();
        }
    }

    MapLocation getMineProspect() {
        MapLocation myLoc = rc.getLocation();
        // consider giving up if too far away
        MapLocation target = null;
        int bestDist = 10000;
        try {
            MapLocation[] cells = rc.getAllLocationsWithinRadiusSquared(myLoc, RobotType.MINER.visionRadiusSquared);
            for (MapLocation cell : cells){
                if (rc.senseLead(cell) > 0 || (cell.distanceSquaredTo(comm.HQloc) >  RobotType.ARCHON.visionRadiusSquared)) continue;
                int dist = myLoc.distanceSquaredTo(cell);
                if (dist < bestDist) { // closest spot with no lead within turtle radius
                    bestDist = dist;
                    target = cell;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (target == null) return comm.HQloc;
        return target;
    }

    void tryDisintegrate() {
        if (rc.getHealth() >= DISINTEGRATE_HEALTH) return;
        if (!rc.isActionReady()) return;
        MapLocation myLoc = rc.getLocation();
        try {
            if ((myLoc.distanceSquaredTo(comm.HQloc) <= RobotType.ARCHON.visionRadiusSquared) && rc.senseLead(myLoc) == 0) {
                rc.disintegrate();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // flees to HQ and resets explore if danger
    MapLocation flee() {
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, myTeam);
        int numAllies = 0;
        int numEnemies = 0;
        for (RobotInfo r : nearbyAllies) {
            if (r.getType().canAttack()) numAllies++;
        }
        for (RobotInfo r : nearbyEnemies) {
            if (r.getType().canAttack()) {
                comm.writeEnemyToLog(r.location);
                numEnemies++;
            }
        }
        if (numEnemies > numAllies) {
            explore.reset();
            return comm.HQloc;
        }

        return null;
    }

    // TODO: improve
    void tryMine(){
        MapLocation myLoc = rc.getLocation();
        int leadToLeave = 1;
        if (comm.HQloc != null && myLoc.distanceSquaredTo(comm.HQloc) > myLoc.distanceSquaredTo(comm.getHQOpposite())) {
            leadToLeave = 0;
        }
        try {
            for (Direction dir : dirs) {
                MapLocation prospect = myLoc.add(dir);
                if (!(rc.onTheMap(prospect))) continue; // reduce bytecode?
                int lead = rc.senseLead(prospect);
                int gold = rc.senseGold(prospect); //adds max of 45 bytecode

                while (lead > leadToLeave) {
                    if (rc.isActionReady()) {
                        rc.mineLead(prospect);
                        lead--;
                    }
                    else {
                        return;
                    }
                }
                while (gold > 0) {
                    if (rc.isActionReady()) {
                        rc.mineGold(prospect);
                        gold--;
                    }
                    else {
                        return;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // miners ignore soldiers TODO: mine on low rubble locations
    void tryMove() {
        MapLocation loc = flee();
        //if (rc.getHealth() < DISINTEGRATE_HEALTH && rc.isActionReady()) loc = getMineProspect(); // if too far from HQ, don't bother
        if (loc == null) loc = getClosestMine();
        if (loc != null){
            bfs.move(loc);
            return;
        }
        if (minerCode == 0) { // switch on miner code?
            loc = explore.getExplore2Target(); // use alternate function to find points of interest
        }
        else {
            loc = explore.getExploreTarget();
        }
        bfs.move(loc);
    }

    // TODO: miners should fan out more, especially if early rounds of turtle
    MapLocation getClosestMine(){
        MapLocation myLoc = rc.getLocation();
        MapLocation bestMine = null;
        int bestDist = 10000;
        try {
            MapLocation[] leadMines = rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared, MIN_LEAD_TO_MINE);
            for (MapLocation mine : leadMines) { // interlinked
                if ((comm.HQloc != null && mine.isAdjacentTo(comm.HQloc)) || rc.senseNearbyRobots(mine, 2, myTeam).length > 2) continue;
                int dist = myLoc.distanceSquaredTo(mine);
                if (bestMine == null) {
                    bestMine = mine;
                    bestDist = dist;
                }
                if (dist < bestDist) {
                    bestMine = mine;
                    bestDist = dist;
                }
            }
            // get lowest rubble adjacent location, break ties with proximity TODO: consider if location is occupied
            if (bestMine != null) {
                MapLocation bestLoc = bestMine;
                int bestRubble = rc.senseRubble(bestMine);
                for (Direction dir : dirs) {
                    MapLocation prospect = bestMine.add(dir);
                    if (!rc.canSenseLocation(prospect)) continue;
                    int rubble = rc.senseRubble(prospect);
                    if (rubble < bestRubble) {
                        bestRubble = rubble;
                        bestLoc = prospect;
                    }
                    else if (rubble == bestRubble && myLoc.distanceSquaredTo(prospect) < myLoc.distanceSquaredTo(bestLoc)) {
                        bestLoc = prospect;
                    }
                }
                bestMine = bestLoc;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return bestMine;
    }

}