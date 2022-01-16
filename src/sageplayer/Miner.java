package sageplayer;

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
    static final int DISINTEGRATE_HEALTH = 21;

    Direction[] dirs = Direction.allDirections();
    int H, W;
    Team myTeam, enemyTeam;

    RobotInfo[] nearbyEnemies;
    double mapLeadScore;

    public Miner(RobotController rc){
        super(rc);
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        comm.readHQloc();
        mapLeadScore = (comm.leadScore / (double)comm.numArchons) * (400.0/(H*W));
    }

    public void play(){
        if (comm.HQloc == null) comm.readHQloc();
        nearbyEnemies = rc.senseNearbyRobots(RobotType.MINER.visionRadiusSquared, enemyTeam);
        tryDisintegrate();
        tryMine();
        if (rc.isMovementReady()) {
            tryMove();
            tryMine();
        }
    }

    //TODO: improve
    MapLocation moveInCombat() {
        for (RobotInfo enemy : nearbyEnemies) {
            //sense enemyArchons
            if (enemy.type == RobotType.ARCHON) {
                comm.writeEnemyArchonLocation(enemy);
                try {
                    if (mapLeadScore < Communication.HIGH_LEAD_THRESHOLD && rc.senseNearbyLocationsWithLead(RobotType.MINER.visionRadiusSquared).length > 9) { // sense not rush
                        comm.setTask(4); // RUSH!
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            // only consider offensive units
            if (!enemy.type.canAttack()) continue;
            //TODO: only consider combat units, with more weight given to watchtowers
            int myForcesCount = 0;
            RobotInfo[] myForces = rc.senseNearbyRobots(enemy.location, RobotType.SOLDIER.visionRadiusSquared, myTeam);
            for (RobotInfo r : myForces) {
                if (r.type.canAttack()) {
                    myForcesCount += r.health;
                }
            }
            int enemyForcesCount = 0;
            RobotInfo[] enemyForces = rc.senseNearbyRobots(enemy.location, RobotType.SOLDIER.visionRadiusSquared, enemyTeam);
            for (RobotInfo r : enemyForces) {
                if (r.type.canAttack()) {
                    enemyForcesCount += r.health;
                }
            }

            if (myForcesCount < enemyForcesCount * 2) { // arbitrary modifier to be a bit safer TODO: flee from highest enemyForcesCount
                explore.reset();
                //return flee(enemy.location);
                return comm.HQloc;
            }
        }
        return null;
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

    // flees to the lowest rubble tile away from enemy
    MapLocation flee(MapLocation enemy) {
        MapLocation myLoc = rc.getLocation();
        int bestRubble = GameConstants.MAX_RUBBLE;
        MapLocation bestLoc = null;
        int d1 = myLoc.distanceSquaredTo(enemy);
        try {
            for (Direction dir : fleeDirections) {
                MapLocation prospect = myLoc.add(dir);
                if (!(rc.onTheMap(prospect))) continue; // reduce bytecode?
                if (prospect.distanceSquaredTo(enemy) > d1) {
                    int r = rc.senseRubble(prospect);
                    if (r < bestRubble) {
                        bestLoc = prospect;
                        bestRubble = r;
                    }
                    //TODO: tiebreak with distance
                }

            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        //System.err.println("miner fleeing at " + myLoc + " fleeing " + enemy + " to " + bestLoc);
        return bestLoc;
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

    void tryMove(){
        MapLocation loc = moveInCombat();
        if (rc.getHealth() < DISINTEGRATE_HEALTH) loc = getMineProspect(); // if too far from HQ, don't bother
        if (loc == null) loc = getClosestMine();
        if (loc != null){
            bfs.move(loc);
            return;
        }
        loc = explore.getExplore2Target(); // use alternate function to find points of interest
        bfs.move(loc);
    }

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
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return bestMine;
    }

}