package sageplayer;

import battlecode.common.*;

import java.awt.*;
import java.util.HashSet;

public class Pathfinding {

    static final int MIN_HEALTH_TO_REINFORCE = 11; // soldiers and sages

    RobotController rc;
    Communication comm;
    MapLocation target = null;
    double avgRubble = 100;

    BugNav bugNav = new BugNav();
    Exploration explore;

    Team myTeam, enemyTeam;

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.CENTER
    };

    boolean[] impassable = null;

    void setImpassable(boolean[] impassable){
        this.impassable = impassable;
    }

    void initTurn(){
        impassable = new boolean[directions.length];
    }

    boolean canMove(Direction dir){
        if (!rc.canMove(dir)) return false;
        if (impassable[dir.ordinal()]) return false;
        return true;
    }

    boolean doMicro() {
        return bugNav.doMicro();
    }

    Pathfinding(RobotController rc, Exploration explore, Communication comm){
        this.rc = rc;
        this.explore = explore;
        this.comm = comm;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
    }

    double getEstimation (MapLocation loc){
        try {
            if (loc.distanceSquaredTo(target) == 0) return 0;
            int d = Util.distance(target, loc);
            double r = 10 + rc.senseRubble(loc);
            return r + (d - 1)*avgRubble;
        } catch (Throwable e){
            e.printStackTrace();
        }
        return 1e9;
    }

    public void move(MapLocation loc){
        if (rc.getMovementCooldownTurns() >= 1) return;
        target = loc;

        //rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 0);

        if (!bugNav.move()) greedyPath();
        bugNav.move();
    }

    final double eps = 1e-5;

    void greedyPath(){
        try {
            MapLocation myLoc = rc.getLocation();
            Direction bestDir = null;
            double bestEstimation = 0;
            double firstStep = rc.senseRubble(myLoc);
            int contRubble = 0;
            int bestEstimationDist = 0;
            double avgR = 0;
            for (Direction dir : directions) {
                MapLocation newLoc = myLoc.add(dir);
                if (!rc.onTheMap(newLoc)) continue;

                //pass
                avgR += rc.senseRubble(newLoc);
                ++contRubble;


                if (!canMove(dir)) continue;
                if (!strictlyCloser(newLoc, myLoc, target)) continue;

                int newDist = newLoc.distanceSquaredTo(target);

                double estimation = firstStep + getEstimation(newLoc);
                if (bestDir == null || estimation < bestEstimation - eps || (Math.abs(estimation - bestEstimation) <= 2*eps && newDist < bestEstimationDist)) {
                    bestEstimation = estimation;
                    bestDir = dir;
                    bestEstimationDist = newDist;
                }
            }
            if (contRubble != 0) {
                avgRubble = avgR / contRubble;
            }
            if (bestDir != null) rc.move(bestDir);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    boolean strictlyCloser(MapLocation newLoc, MapLocation oldLoc, MapLocation target){
        int dOld = Util.distance(target, oldLoc), dNew = Util.distance(target, newLoc);
        if (dOld < dNew) return false;
        if (dNew < dOld) return true;
        return target.distanceSquaredTo(newLoc) < target.distanceSquaredTo(oldLoc);

    }

    class BugNav{

        BugNav(){}

        final int INF = 1000000;

        boolean rotateRight = true; //if I should rotate right or left
        MapLocation lastObstacleFound = null; //latest obstacle I've found in my way
        int minDistToEnemy = INF; //minimum distance I've been to the enemy while going around an obstacle
        MapLocation prevTarget = null; //previous target
        HashSet<Integer> visited = new HashSet<>();
        int braveryModifier = 1;

        boolean move() {
            try{

                //different target? ==> previous data does not help!
                if (prevTarget == null || target.distanceSquaredTo(prevTarget) > 0) resetPathfinding();

                //If I'm at a minimum distance to the target, I'm free!
                MapLocation myLoc = rc.getLocation();
                int d = myLoc.distanceSquaredTo(target);
                if (d <= minDistToEnemy) resetPathfinding();

                int code = getCode();

                if (visited.contains(code)) resetPathfinding();
                visited.add(code);

                //Update data
                prevTarget = target;
                minDistToEnemy = Math.min(d, minDistToEnemy);

                //If there's an obstacle I try to go around it [until I'm free] instead of going to the target directly
                Direction dir = myLoc.directionTo(target);
                if (lastObstacleFound != null) dir = myLoc.directionTo(lastObstacleFound);
                if (canMove(dir)){
                    resetPathfinding();
                }

                //I rotate clockwise or counterclockwise (depends on 'rotateRight'). If I try to go out of the map I change the orientation
                //Note that we have to try at most 16 times since we can switch orientation in the middle of the loop. (It can be done more efficiently)
                for (int i = 8; i-- > 0;) {
                    if (canMove(dir)) {
                        rc.move(dir);
                        return true;
                    }
                    MapLocation newLoc = myLoc.add(dir);
                    if (!rc.onTheMap(newLoc)) rotateRight = !rotateRight;
                        //If I could not go in that direction, and it was not outside the map, then this is the latest obstacle found
                    else lastObstacleFound = myLoc.add(dir);
                    if (rotateRight) dir = dir.rotateRight();
                    else dir = dir.rotateLeft();
                }

                if (canMove(dir)) rc.move(dir);
            } catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }

        //clear some of previous data
        void resetPathfinding(){
            lastObstacleFound = null;
            minDistToEnemy = INF;
            visited.clear();
        }

        int getCode(){
            int x = rc.getLocation().x;
            int y = rc.getLocation().y;
            Direction obstacleDir = rc.getLocation().directionTo(target);
            if (lastObstacleFound != null) obstacleDir = rc.getLocation().directionTo(lastObstacleFound);
            int bit = rotateRight ? 1 : 0;
            return (((((x << 6) | y) << 4) | obstacleDir.ordinal()) << 1) | bit;
        }

        boolean doMicro() {
            // if tons of allies, we need to use our numbers advantage. returning false will cause units to push to archon TODO: improve, try making this code change a modifier. soldier should still be smart
            braveryModifier = 1;
            if (comm.getTask() == Communication.CRUNCH) {
                RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared);
                int numAllies = 0;
                for (RobotInfo r : allies) {
                    if (r.getType().canAttack()) numAllies++;
                }
                if (numAllies > 12) braveryModifier = 2;
            }
            // doMicro
            MicroInfo[] microInfo = new MicroInfo[9];
            int minRubble = INF;
            for (int i = 0; i < 9; i++) {
                microInfo[i] = new MicroInfo(rc.getLocation().add(directions[i]));
                if (microInfo[i].myRubble < minRubble) minRubble = microInfo[i].myRubble;
            }

            RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, enemyTeam);

            for (RobotInfo enemy : enemies) {
                for (int i = 0; i < 9; i++) {
                    microInfo[i].update(enemy);
                }
            }

            int bestIndex = 8;
            for (int i = 7; i >= 0; i--) {
                if (!rc.canMove(directions[i])) continue;
                //if (microInfo[i].myRubble - 20 > minRubble) continue; // avoid significantly higher rubble
                if (!microInfo[bestIndex].isBetter(microInfo[i])) {
                    bestIndex = i;
                    //System.err.println("thought " + microInfo[i].loc + " was better");
                }
            }

            if (enemies.length > 0) {
                try {
                    //try attacking if fleeing all combat
                    if(rc.isActionReady() && microInfo[bestIndex].enemyDPS == 0) { // TODO: improve?
                        if (rc.getType() == RobotType.SOLDIER) {
                            RobotInfo[] enemiesToAttack = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, enemyTeam);
                            MapLocation bestLoc = null;
                            boolean attackerInRange = false;
                            // don't attack miners if soldiers in view
                            for (RobotInfo r : enemies) {
                                if (r.type.canAttack()) {
                                    attackerInRange = true;
                                }
                            }
                            int bestHealth = 10000;
                            int bestRubble = GameConstants.MAX_RUBBLE;
                            for (RobotInfo r : enemiesToAttack) {
                                MapLocation enemyLoc = r.getLocation();
                                boolean isAttacker = r.type.canAttack();
                                // if there are attackers, ignore all non-attackers and reset variables
                                if (!isAttacker && attackerInRange) continue;
                                int rubble = GameConstants.MAX_RUBBLE;
                                try {
                                    rubble = rc.senseRubble(r.location);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                                if (isAttacker && !attackerInRange) {
                                    bestHealth = 10000;
                                    bestRubble = rubble;
                                    attackerInRange = true;
                                }
                                // shoot lowest health with rubble as tiebreaker
                                if (r.health < bestHealth) {
                                    bestHealth = r.health;
                                    bestRubble = rubble;
                                    bestLoc = enemyLoc;
                                }
                                else if (r.health == bestHealth && rubble < bestRubble) {
                                    bestRubble = rubble;
                                    bestLoc = enemyLoc;
                                }
                            }
                            try {
                                if (bestLoc != null) {
                                    rc.attack(bestLoc);
                                }
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                        else if (rc.getType() == RobotType.SAGE) {
                            RobotInfo[] enemiesToAttack = rc.senseNearbyRobots(RobotType.SAGE.actionRadiusSquared, enemyTeam);
                            MapLocation bestLoc = null;
                            boolean attackerInRange = false;
                            // don't attack miners if soldiers in view
                            for (RobotInfo r : enemies) {
                                if (r.type.canAttack()) {
                                    comm.writeEnemyToLog(r.location);
                                    attackerInRange = true;
                                    break;
                                }
                            }
                            int bestHealth = 10000;
                            int bestRubble = GameConstants.MAX_RUBBLE;
                            boolean canKill = false;
                            int chargeKills = 0;
                            for (RobotInfo r : enemiesToAttack) {
                                MapLocation enemyLoc = r.getLocation();
                                boolean isAttacker = r.type.canAttack();
                                // if there are attackers, ignore all non-attackers and reset variables
                                if (!isAttacker && attackerInRange) continue;
                                int rubble = GameConstants.MAX_RUBBLE;
                                try {
                                    rubble = rc.senseRubble(r.location);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                                // pick target
                                if (!canKill && r.getHealth() <= rc.getType().damage) canKill = true;
                                if (isAttacker) {
                                    if (!attackerInRange) {
                                        bestHealth = 0;
                                        bestRubble = rubble;
                                        attackerInRange = true;
                                        if (!canKill && r.getHealth() <= rc.getType().damage) {
                                            canKill = true;
                                        }
                                        else {
                                            canKill = false;
                                        }
                                    }
                                    if (r.getHealth() <= r.getType().getMaxHealth(r.getLevel()) * AnomalyType.CHARGE.sagePercentage) chargeKills++;
                                }
                                if (canKill) {
                                    // shoot lowest health with rubble as tiebreaker
                                    if (r.getHealth() <= rc.getType().damage && r.getHealth() > bestHealth) {
                                        bestHealth = r.getHealth();
                                        bestRubble = rubble;
                                        bestLoc = enemyLoc;
                                    }
                                    else if (r.getHealth() == bestHealth && rubble < bestRubble) {
                                        bestRubble = rubble;
                                        bestLoc = enemyLoc;
                                    }
                                }
                                else {
                                    if (r.getHealth() < bestHealth) {
                                        bestHealth = r.getHealth();
                                        bestRubble = rubble;
                                        bestLoc = enemyLoc;
                                    }
                                    else if (r.getHealth() == bestHealth && rubble < bestRubble) {
                                        bestRubble = rubble;
                                        bestLoc = enemyLoc;
                                    }
                                }
                            }
                            try {
                                if (bestLoc != null) {
                                    if (chargeKills > 3) {
                                        rc.envision(AnomalyType.CHARGE);
                                    }
                                    else if (rc.senseRobotAtLocation(bestLoc).getType() == RobotType.ARCHON) {
                                        rc.envision(AnomalyType.FURY);
                                    }
                                    else {
                                        rc.attack(bestLoc);
                                    }
                                }
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    }
                    //System.err.println("microing to " + rc.getLocation().add(directions[bestIndex]));
                    rc.setIndicatorString("doMicro");
                    if (directions[bestIndex] == Direction.CENTER) return true;
                    rc.move(directions[bestIndex]);
                    return true;
                } catch (Throwable e){
                    e.printStackTrace();
                }
            }
            return false;
        }

        class MicroInfo {
            long enemyDPS = INF;
            int minDistToEnemy = INF;
            long myDPS = 0;
            int myRubble = INF;
            MapLocation loc;

            public MicroInfo(MapLocation loc) {
                this.loc = loc;
                try {
                    if (rc.canSenseLocation(loc)) {
                        myRubble = rc.senseRubble(loc);
                        myDPS = Math.round(1000*rc.getType().damage / (rc.getType().actionCooldown * (1+myRubble/10.0)));
                        enemyDPS = 0;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            void update(RobotInfo robot) {
                int d = robot.location.distanceSquaredTo(loc);
                if (d <= robot.type.actionRadiusSquared && robot.getType().canAttack()) {
                    int r = 0;
                    try {
                        if (rc.canSenseLocation(loc)) r = rc.senseRubble(robot.getLocation());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    int damage = robot.getType().damage;
                    if (robot.getMode() == RobotMode.PROTOTYPE) damage = 0; // watchtower edge case
                    enemyDPS += Math.round(1000*damage / (robot.getType().actionCooldown * (1+r/10.0)));
                }
                if (d < minDistToEnemy) minDistToEnemy = d; // TODO: improve?
            }

            boolean canAttack() {
                return rc.getType().actionRadiusSquared >= minDistToEnemy;
            }

            //TODO: improve
            boolean isBetter(MicroInfo m) {
                // never move to significantly higher rubble
                long dpsDiff = myDPS - enemyDPS;
                long mdpsDiff = m.myDPS - m.enemyDPS;
                if (rc.getHealth() >= MIN_HEALTH_TO_REINFORCE) {
                    //winning fight or safe location
                    if (dpsDiff > 0) {
                        if (dpsDiff > mdpsDiff) { // more winning / lower rubble
                            if (enemyDPS != 0) return true; // is a fight
                            if (m.enemyDPS != 0) { // not a fight, and m is a fight
                                if (mdpsDiff >= 0) return false; // m is a winning fight
                                return true; // is not a fight and m is a losing fight
                            }
                            // both locations have no enemies,
                            if (canAttack()) {
                                if (!m.canAttack()) return true; // cant attack non offensive unit from m
                                if (myDPS > m.myDPS) return true; // m has higher rubble
                                if (myDPS < m.myDPS) return false; // m has lower rubble
                                return minDistToEnemy >= m.minDistToEnemy; // max mindist
                            }
                            if (m.canAttack()) return false; // can attack non offensive unit from m
                            if (myDPS > m.myDPS) return true; // m has higher rubble
                            if (myDPS < m.myDPS) return false; // m has lower rubble
                            return minDistToEnemy <= m.minDistToEnemy; // minimize mindist
                        }
                        else if (dpsDiff == mdpsDiff) {
                            if (enemyDPS != 0) { // is a fight
                                if (m.enemyDPS == 0) return true; // m is not a fight
                                //both locations have enemies
                                if (myDPS > m.myDPS) return true; // m has higher rubble
                                if (myDPS < m.myDPS) return false; // m has lower rubble
                                return minDistToEnemy >= m.minDistToEnemy; // max mindist
                            }
                            if (m.enemyDPS != 0) return false; // m is a fight
                            // both locations have no enemies
                            if (canAttack()) {
                                if (!m.canAttack()) return true; // cant attack non offensive unit from m
                                return minDistToEnemy >= m.minDistToEnemy; // max mindist
                            }
                            if (m.canAttack()) return false; // can attack non offensive unit from m
                            return minDistToEnemy <= m.minDistToEnemy; // minimize mindist
                        }
                        else {
                            if (enemyDPS != 0) { // is a fight
                                if (m.enemyDPS == 0) return true; // m is not a fight
                                //both locations have enemies, but m has a better combat score
                                return false;
                            }
                            // m is either a fight, or both locations have no enemies, but m has lower rubble
                            return false;
                        }
                    }
                    // even fight
                    else if (dpsDiff == 0) {
                        if (dpsDiff > mdpsDiff) { // better combat score
                            return true;
                        }
                        else if (dpsDiff == mdpsDiff) { // both are even fights
                            if (myDPS > m.myDPS) return true; // m has higher rubble
                            if (myDPS < m.myDPS) return false; // m has lower rubble
                            return minDistToEnemy >= m.minDistToEnemy; // max mindist
                        }
                        else { // worse combat score
                            if (m.enemyDPS == 0) return true; // m is not a fight
                            // m is a fight, and has a better combat score
                            return false;
                        }
                    }
                    // losing fight
                    else {
                        if (dpsDiff > mdpsDiff) {
                            return true; // less losing than m
                        }
                        else if (dpsDiff == mdpsDiff) {
                            if (myDPS > m.myDPS) return true; // m has higher rubble
                            if (myDPS < m.myDPS) return false; // m has lower rubble
                            return minDistToEnemy >= m.minDistToEnemy; // max mindist
                        }
                        else {
                            return false;
                        }
                    }
                }
                // if health is low, will try to get out of combat
                if (enemyDPS != 0) {
                    if (m.enemyDPS == 0) return false;
                    if (myDPS > m.myDPS) return true;
                    if (myDPS < m.myDPS) return false;
                    return minDistToEnemy >= m.minDistToEnemy;
                }
                if (m.enemyDPS != 0) return true;
                if (myDPS > m.myDPS) return true;
                if (myDPS < m.myDPS) return false;
                return minDistToEnemy >= m.minDistToEnemy;
            }
        }
    }
}