//hardcoded pathfinding for 20 vision units (droids)
//  consider trying these to reduce bytecode
//- Some "distance1 + weight < distance2" conditions are always true because their distance1 is always 0 and their distance2 is always 1,000,000
//- All rc.onTheMap calls can be replaced with cheaper switch statements
//- Math.sqrt isn't needed in the score calculations
package microplayer;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Arrays;
import java.util.Collections;

public class BFSDroid extends BFS {

    BFSDroid(RobotController rc, Exploration explore){
        super(rc, explore);
    }

    static MapLocation l30;
    static double v30;
    static Direction d30;
    static double p30;

    static MapLocation l31;
    static double v31;
    static Direction d31;
    static double p31;

    static MapLocation l32;
    static double v32;
    static Direction d32;
    static double p32;

    static MapLocation l33;
    static double v33;
    static Direction d33;
    static double p33;

    static MapLocation l34;
    static double v34;
    static Direction d34;
    static double p34;

    static MapLocation l42;
    static double v42;
    static Direction d42;
    static double p42;

    static MapLocation l43;
    static double v43;
    static Direction d43;
    static double p43;

    static MapLocation l44;
    static double v44;
    static Direction d44;
    static double p44;

    static MapLocation l45;
    static double v45;
    static Direction d45;
    static double p45;

    static MapLocation l46;
    static double v46;
    static Direction d46;
    static double p46;

    static MapLocation l47;
    static double v47;
    static Direction d47;
    static double p47;

    static MapLocation l48;
    static double v48;
    static Direction d48;
    static double p48;

    static MapLocation l54;
    static double v54;
    static Direction d54;
    static double p54;

    static MapLocation l55;
    static double v55;
    static Direction d55;
    static double p55;

    static MapLocation l56;
    static double v56;
    static Direction d56;
    static double p56;

    static MapLocation l57;
    static double v57;
    static Direction d57;
    static double p57;

    static MapLocation l58;
    static double v58;
    static Direction d58;
    static double p58;

    static MapLocation l59;
    static double v59;
    static Direction d59;
    static double p59;

    static MapLocation l60;
    static double v60;
    static Direction d60;
    static double p60;

    static MapLocation l61;
    static double v61;
    static Direction d61;
    static double p61;

    static MapLocation l62;
    static double v62;
    static Direction d62;
    static double p62;

    static MapLocation l67;
    static double v67;
    static Direction d67;
    static double p67;

    static MapLocation l68;
    static double v68;
    static Direction d68;
    static double p68;

    static MapLocation l69;
    static double v69;
    static Direction d69;
    static double p69;

    static MapLocation l70;
    static double v70;
    static Direction d70;
    static double p70;

    static MapLocation l71;
    static double v71;
    static Direction d71;
    static double p71;

    static MapLocation l72;
    static double v72;
    static Direction d72;
    static double p72;

    static MapLocation l73;
    static double v73;
    static Direction d73;
    static double p73;

    static MapLocation l74;
    static double v74;
    static Direction d74;
    static double p74;

    static MapLocation l75;
    static double v75;
    static Direction d75;
    static double p75;

    static MapLocation l80;
    static double v80;
    static Direction d80;
    static double p80;

    static MapLocation l81;
    static double v81;
    static Direction d81;
    static double p81;

    static MapLocation l82;
    static double v82;
    static Direction d82;
    static double p82;

    static MapLocation l83;
    static double v83;
    static Direction d83;
    static double p83;

    static MapLocation l84;
    static double v84;
    static Direction d84;
    static double p84;

    static MapLocation l85;
    static double v85;
    static Direction d85;
    static double p85;

    static MapLocation l86;
    static double v86;
    static Direction d86;
    static double p86;

    static MapLocation l87;
    static double v87;
    static Direction d87;
    static double p87;

    static MapLocation l88;
    static double v88;
    static Direction d88;
    static double p88;

    static MapLocation l93;
    static double v93;
    static Direction d93;
    static double p93;

    static MapLocation l94;
    static double v94;
    static Direction d94;
    static double p94;

    static MapLocation l95;
    static double v95;
    static Direction d95;
    static double p95;

    static MapLocation l96;
    static double v96;
    static Direction d96;
    static double p96;

    static MapLocation l97;
    static double v97;
    static Direction d97;
    static double p97;

    static MapLocation l98;
    static double v98;
    static Direction d98;
    static double p98;

    static MapLocation l99;
    static double v99;
    static Direction d99;
    static double p99;

    static MapLocation l100;
    static double v100;
    static Direction d100;
    static double p100;

    static MapLocation l101;
    static double v101;
    static Direction d101;
    static double p101;

    static MapLocation l106;
    static double v106;
    static Direction d106;
    static double p106;

    static MapLocation l107;
    static double v107;
    static Direction d107;
    static double p107;

    static MapLocation l108;
    static double v108;
    static Direction d108;
    static double p108;

    static MapLocation l109;
    static double v109;
    static Direction d109;
    static double p109;

    static MapLocation l110;
    static double v110;
    static Direction d110;
    static double p110;

    static MapLocation l111;
    static double v111;
    static Direction d111;
    static double p111;

    static MapLocation l112;
    static double v112;
    static Direction d112;
    static double p112;

    static MapLocation l113;
    static double v113;
    static Direction d113;
    static double p113;

    static MapLocation l114;
    static double v114;
    static Direction d114;
    static double p114;

    static MapLocation l120;
    static double v120;
    static Direction d120;
    static double p120;

    static MapLocation l121;
    static double v121;
    static Direction d121;
    static double p121;

    static MapLocation l122;
    static double v122;
    static Direction d122;
    static double p122;

    static MapLocation l123;
    static double v123;
    static Direction d123;
    static double p123;

    static MapLocation l124;
    static double v124;
    static Direction d124;
    static double p124;

    static MapLocation l125;
    static double v125;
    static Direction d125;
    static double p125;

    static MapLocation l126;
    static double v126;
    static Direction d126;
    static double p126;

    static MapLocation l134;
    static double v134;
    static Direction d134;
    static double p134;

    static MapLocation l135;
    static double v135;
    static Direction d135;
    static double p135;

    static MapLocation l136;
    static double v136;
    static Direction d136;
    static double p136;

    static MapLocation l137;
    static double v137;
    static Direction d137;
    static double p137;

    static MapLocation l138;
    static double v138;
    static Direction d138;
    static double p138;


    Direction getBestDir(MapLocation target){
        l84 = rc.getLocation();
        v84 = 0;
        l85 = rc.adjacentLocation(Direction.NORTH);
        v85 = 1000000;
        d85 = null;
        l72 = rc.adjacentLocation(Direction.NORTHWEST);
        v72 = 1000000;
        d72 = null;
        l71 = rc.adjacentLocation(Direction.WEST);
        v71 = 1000000;
        d71 = null;
        l70 = rc.adjacentLocation(Direction.SOUTHWEST);
        v70 = 1000000;
        d70 = null;
        l83 = rc.adjacentLocation(Direction.SOUTH);
        v83 = 1000000;
        d83 = null;
        l96 = rc.adjacentLocation(Direction.SOUTHEAST);
        v96 = 1000000;
        d96 = null;
        l97 = rc.adjacentLocation(Direction.EAST);
        v97 = 1000000;
        d97 = null;
        l98 = rc.adjacentLocation(Direction.NORTHEAST);
        v98 = 1000000;
        d98 = null;
        l99 = l98.add(Direction.NORTH);
        v99 = 1000000;
        d99 = null;
        l86 = l99.add(Direction.WEST);
        v86 = 1000000;
        d86 = null;
        l73 = l86.add(Direction.WEST);
        v73 = 1000000;
        d73 = null;
        l60 = l73.add(Direction.WEST);
        v60 = 1000000;
        d60 = null;
        l59 = l60.add(Direction.SOUTH);
        v59 = 1000000;
        d59 = null;
        l58 = l59.add(Direction.SOUTH);
        v58 = 1000000;
        d58 = null;
        l57 = l58.add(Direction.SOUTH);
        v57 = 1000000;
        d57 = null;
        l56 = l57.add(Direction.SOUTH);
        v56 = 1000000;
        d56 = null;
        l69 = l56.add(Direction.EAST);
        v69 = 1000000;
        d69 = null;
        l82 = l69.add(Direction.EAST);
        v82 = 1000000;
        d82 = null;
        l95 = l82.add(Direction.EAST);
        v95 = 1000000;
        d95 = null;
        l108 = l95.add(Direction.EAST);
        v108 = 1000000;
        d108 = null;
        l109 = l108.add(Direction.NORTH);
        v109 = 1000000;
        d109 = null;
        l110 = l109.add(Direction.NORTH);
        v110 = 1000000;
        d110 = null;
        l111 = l110.add(Direction.NORTH);
        v111 = 1000000;
        d111 = null;
        l112 = l111.add(Direction.NORTH);
        v112 = 1000000;
        d112 = null;
        l100 = l112.add(Direction.NORTHWEST);
        v100 = 1000000;
        d100 = null;
        l87 = l100.add(Direction.WEST);
        v87 = 1000000;
        d87 = null;
        l74 = l87.add(Direction.WEST);
        v74 = 1000000;
        d74 = null;
        l61 = l74.add(Direction.WEST);
        v61 = 1000000;
        d61 = null;
        l47 = l61.add(Direction.SOUTHWEST);
        v47 = 1000000;
        d47 = null;
        l46 = l47.add(Direction.SOUTH);
        v46 = 1000000;
        d46 = null;
        l45 = l46.add(Direction.SOUTH);
        v45 = 1000000;
        d45 = null;
        l44 = l45.add(Direction.SOUTH);
        v44 = 1000000;
        d44 = null;
        l43 = l44.add(Direction.SOUTH);
        v43 = 1000000;
        d43 = null;
        l55 = l43.add(Direction.SOUTHEAST);
        v55 = 1000000;
        d55 = null;
        l68 = l55.add(Direction.EAST);
        v68 = 1000000;
        d68 = null;
        l81 = l68.add(Direction.EAST);
        v81 = 1000000;
        d81 = null;
        l94 = l81.add(Direction.EAST);
        v94 = 1000000;
        d94 = null;
        l107 = l94.add(Direction.EAST);
        v107 = 1000000;
        d107 = null;
        l121 = l107.add(Direction.NORTHEAST);
        v121 = 1000000;
        d121 = null;
        l122 = l121.add(Direction.NORTH);
        v122 = 1000000;
        d122 = null;
        l123 = l122.add(Direction.NORTH);
        v123 = 1000000;
        d123 = null;
        l124 = l123.add(Direction.NORTH);
        v124 = 1000000;
        d124 = null;
        l125 = l124.add(Direction.NORTH);
        v125 = 1000000;
        d125 = null;
        l113 = l125.add(Direction.NORTHWEST);
        v113 = 1000000;
        d113 = null;
        l101 = l113.add(Direction.NORTHWEST);
        v101 = 1000000;
        d101 = null;
        l88 = l101.add(Direction.WEST);
        v88 = 1000000;
        d88 = null;
        l75 = l88.add(Direction.WEST);
        v75 = 1000000;
        d75 = null;
        l62 = l75.add(Direction.WEST);
        v62 = 1000000;
        d62 = null;
        l48 = l62.add(Direction.SOUTHWEST);
        v48 = 1000000;
        d48 = null;
        l34 = l48.add(Direction.SOUTHWEST);
        v34 = 1000000;
        d34 = null;
        l33 = l34.add(Direction.SOUTH);
        v33 = 1000000;
        d33 = null;
        l32 = l33.add(Direction.SOUTH);
        v32 = 1000000;
        d32 = null;
        l31 = l32.add(Direction.SOUTH);
        v31 = 1000000;
        d31 = null;
        l30 = l31.add(Direction.SOUTH);
        v30 = 1000000;
        d30 = null;
        l42 = l30.add(Direction.SOUTHEAST);
        v42 = 1000000;
        d42 = null;
        l54 = l42.add(Direction.SOUTHEAST);
        v54 = 1000000;
        d54 = null;
        l67 = l54.add(Direction.EAST);
        v67 = 1000000;
        d67 = null;
        l80 = l67.add(Direction.EAST);
        v80 = 1000000;
        d80 = null;
        l93 = l80.add(Direction.EAST);
        v93 = 1000000;
        d93 = null;
        l106 = l93.add(Direction.EAST);
        v106 = 1000000;
        d106 = null;
        l120 = l106.add(Direction.NORTHEAST);
        v120 = 1000000;
        d120 = null;
        l134 = l120.add(Direction.NORTHEAST);
        v134 = 1000000;
        d134 = null;
        l135 = l134.add(Direction.NORTH);
        v135 = 1000000;
        d135 = null;
        l136 = l135.add(Direction.NORTH);
        v136 = 1000000;
        d136 = null;
        l137 = l136.add(Direction.NORTH);
        v137 = 1000000;
        d137 = null;
        l138 = l137.add(Direction.NORTH);
        v138 = 1000000;
        d138 = null;
        l126 = l138.add(Direction.NORTHWEST);
        v126 = 1000000;
        d126 = null;
        l114 = l126.add(Direction.NORTHWEST); // droid vision
        v114 = 1000000;
        d114 = null;


        try {
            if (rc.onTheMap(l71)) {
                if (rc.canMove(Direction.WEST)) {
                    p71 = 10 + rc.senseRubble(l71);
                    if (v71 > v84 + p71) {
                        v71 = v84 + p71;
                        d71 = Direction.WEST;
                    }
                }
            }
            if (rc.onTheMap(l83)) {
                if (rc.canMove(Direction.SOUTH)) {
                    p83 = 10 + rc.senseRubble(l83);
                    if (v83 > v84 + p83) {
                        v83 = v84 + p83;
                        d83 = Direction.SOUTH;
                    }
                    if (v83 > v71 + p83) {
                        v83 = v71 + p83;
                        d83 = d71;
                    }
                }
            }
            if (rc.onTheMap(l85)) {
                if (rc.canMove(Direction.NORTH)) {
                    p85 = 10 + rc.senseRubble(l85);
                    if (v85 > v84 + p85) {
                        v85 = v84 + p85;
                        d85 = Direction.NORTH;
                    }
                    if (v85 > v71 + p85) {
                        v85 = v71 + p85;
                        d85 = d71;
                    }
                }
            }
            if (rc.onTheMap(l97)) {
                if (rc.canMove(Direction.WEST)) {
                    p97 = 10 + rc.senseRubble(l97);
                    if (v97 > v84 + p97) {
                        v97 = v84 + p97;
                        d97 = Direction.EAST;
                    }
                    if (v97 > v85 + p97) {
                        v97 = v85 + p97;
                        d97 = d85;
                    }
                    if (v97 > v83 + p97) {
                        v97 = v83 + p97;
                        d97 = d83;
                    }
                }
            }
            if (rc.onTheMap(l70)) {
                if (rc.canMove(Direction.SOUTHWEST)) {
                    p70 = 10 + rc.senseRubble(l70);
                    if (v70 > v84 + p70) {
                        v70 = v84 + p70;
                        d70 = Direction.SOUTHWEST;
                    }
                    if (v70 > v71 + p70) {
                        v70 = v71 + p70;
                        d70 = d71;
                    }
                    if (v70 > v83 + p70) {
                        v70 = v83 + p70;
                        d70 = d83;
                    }
                }
            }
            if (rc.onTheMap(l72)) {
                if (rc.canMove(Direction.NORTHWEST)) {
                    p72 = 10 + rc.senseRubble(l72);
                    if (v72 > v84 + p72) {
                        v72 = v84 + p72;
                        d72 = Direction.NORTHWEST;
                    }
                    if (v72 > v71 + p72) {
                        v72 = v71 + p72;
                        d72 = d71;
                    }
                    if (v72 > v85 + p72) {
                        v72 = v85 + p72;
                        d72 = d85;
                    }
                }
            }
            if (rc.onTheMap(l96)) {
                if (rc.canMove(Direction.SOUTHEAST)) {
                    p96 = 10 + rc.senseRubble(l96);
                    if (v96 > v84 + p96) {
                        v96 = v84 + p96;
                        d96 = Direction.SOUTHEAST;
                    }
                    if (v96 > v97 + p96) {
                        v96 = v97 + p96;
                        d96 = d97;
                    }
                    if (v96 > v83 + p96) {
                        v96 = v83 + p96;
                        d96 = d83;
                    }
                }
            }
            if (rc.onTheMap(l98)) {
                if (rc.canMove(Direction.NORTHEAST)) {
                    p98 = 10 + rc.senseRubble(l98);
                    if (v98 > v84 + p98) {
                        v98 = v84 + p98;
                        d98 = Direction.NORTHEAST;
                    }
                    if (v98 > v85 + p98) {
                        v98 = v85 + p98;
                        d98 = d85;
                    }
                    if (v98 > v97 + p98) {
                        v98 = v97 + p98;
                        d98 = d97;
                    }
                }
            }
            if (rc.onTheMap(l58)) {
                p58 = 10 + rc.senseRubble(l58);
                if (v58 > v71 + p58) {
                    v58 = v71 + p58;
                    d58 = d71;
                }
                if (v58 > v70 + p58) {
                    v58 = v70 + p58;
                    d58 = d70;
                }
                if (v58 > v72 + p58) {
                    v58 = v72 + p58;
                    d58 = d72;
                }
            }
            if (rc.onTheMap(l82)) {
                p82 = 10 + rc.senseRubble(l82);
                if (v82 > v83 + p82) {
                    v82 = v83 + p82;
                    d82 = d83;
                }
                if (v82 > v70 + p82) {
                    v82 = v70 + p82;
                    d82 = d70;
                }
                if (v82 > v96 + p82) {
                    v82 = v96 + p82;
                    d82 = d96;
                }
            }
            if (rc.onTheMap(l86)) {
                p86 = 10 + rc.senseRubble(l86);
                if (v86 > v85 + p86) {
                    v86 = v85 + p86;
                    d86 = d85;
                }
                if (v86 > v72 + p86) {
                    v86 = v72 + p86;
                    d86 = d72;
                }
                if (v86 > v98 + p86) {
                    v86 = v98 + p86;
                    d86 = d98;
                }
            }
            if (rc.onTheMap(l110)) {
                p110 = 10 + rc.senseRubble(l110);
                if (v110 > v97 + p110) {
                    v110 = v97 + p110;
                    d110 = d97;
                }
                if (v110 > v98 + p110) {
                    v110 = v98 + p110;
                    d110 = d98;
                }
                if (v110 > v96 + p110) {
                    v110 = v96 + p110;
                    d110 = d96;
                }
            }
            if (rc.onTheMap(l57)) {
                p57 = 10 + rc.senseRubble(l57);
                if (v57 > v71 + p57) {
                    v57 = v71 + p57;
                    d57 = d71;
                }
                if (v57 > v70 + p57) {
                    v57 = v70 + p57;
                    d57 = d70;
                }
                if (v57 > v58 + p57) {
                    v57 = v58 + p57;
                    d57 = d58;
                }
            }
            if (rc.onTheMap(l59)) {
                p59 = 10 + rc.senseRubble(l59);
                if (v59 > v71 + p59) {
                    v59 = v71 + p59;
                    d59 = d71;
                }
                if (v59 > v72 + p59) {
                    v59 = v72 + p59;
                    d59 = d72;
                }
                if (v59 > v58 + p59) {
                    v59 = v58 + p59;
                    d59 = d58;
                }
            }
            if (rc.onTheMap(l69)) {
                p69 = 10 + rc.senseRubble(l69);
                if (v69 > v83 + p69) {
                    v69 = v83 + p69;
                    d69 = d83;
                }
                if (v69 > v70 + p69) {
                    v69 = v70 + p69;
                    d69 = d70;
                }
                if (v69 > v82 + p69) {
                    v69 = v82 + p69;
                    d69 = d82;
                }
                if (v69 > v57 + p69) {
                    v69 = v57 + p69;
                    d69 = d57;
                }
            }
            if (rc.onTheMap(l73)) {
                p73 = 10 + rc.senseRubble(l73);
                if (v73 > v85 + p73) {
                    v73 = v85 + p73;
                    d73 = d85;
                }
                if (v73 > v72 + p73) {
                    v73 = v72 + p73;
                    d73 = d72;
                }
                if (v73 > v86 + p73) {
                    v73 = v86 + p73;
                    d73 = d86;
                }
                if (v73 > v59 + p73) {
                    v73 = v59 + p73;
                    d73 = d59;
                }
            }
            if (rc.onTheMap(l95)) {
                p95 = 10 + rc.senseRubble(l95);
                if (v95 > v83 + p95) {
                    v95 = v83 + p95;
                    d95 = d83;
                }
                if (v95 > v96 + p95) {
                    v95 = v96 + p95;
                    d95 = d96;
                }
                if (v95 > v82 + p95) {
                    v95 = v82 + p95;
                    d95 = d82;
                }
            }
            if (rc.onTheMap(l99)) {
                p99 = 10 + rc.senseRubble(l99);
                if (v99 > v85 + p99) {
                    v99 = v85 + p99;
                    d99 = d85;
                }
                if (v99 > v98 + p99) {
                    v99 = v98 + p99;
                    d99 = d98;
                }
                if (v99 > v86 + p99) {
                    v99 = v86 + p99;
                    d99 = d86;
                }
            }
            if (rc.onTheMap(l109)) {
                p109 = 10 + rc.senseRubble(l109);
                if (v109 > v97 + p109) {
                    v109 = v97 + p109;
                    d109 = d97;
                }
                if (v109 > v96 + p109) {
                    v109 = v96 + p109;
                    d109 = d96;
                }
                if (v109 > v110 + p109) {
                    v109 = v110 + p109;
                    d109 = d110;
                }
                if (v109 > v95 + p109) {
                    v109 = v95 + p109;
                    d109 = d95;
                }
            }
            if (rc.onTheMap(l111)) {
                p111 = 10 + rc.senseRubble(l111);
                if (v111 > v97 + p111) {
                    v111 = v97 + p111;
                    d111 = d97;
                }
                if (v111 > v98 + p111) {
                    v111 = v98 + p111;
                    d111 = d98;
                }
                if (v111 > v110 + p111) {
                    v111 = v110 + p111;
                    d111 = d110;
                }
                if (v111 > v99 + p111) {
                    v111 = v99 + p111;
                    d111 = d99;
                }
            }
            if (rc.onTheMap(l56)) {
                p56 = 10 + rc.senseRubble(l56);
                if (v56 > v70 + p56) {
                    v56 = v70 + p56;
                    d56 = d70;
                }
                if (v56 > v57 + p56) {
                    v56 = v57 + p56;
                    d56 = d57;
                }
                if (v56 > v69 + p56) {
                    v56 = v69 + p56;
                    d56 = d69;
                }
            }
            if (rc.onTheMap(l60)) {
                p60 = 10 + rc.senseRubble(l60);
                if (v60 > v72 + p60) {
                    v60 = v72 + p60;
                    d60 = d72;
                }
                if (v60 > v59 + p60) {
                    v60 = v59 + p60;
                    d60 = d59;
                }
                if (v60 > v73 + p60) {
                    v60 = v73 + p60;
                    d60 = d73;
                }
            }
            if (rc.onTheMap(l108)) {
                p108 = 10 + rc.senseRubble(l108);
                if (v108 > v96 + p108) {
                    v108 = v96 + p108;
                    d108 = d96;
                }
                if (v108 > v109 + p108) {
                    v108 = v109 + p108;
                    d108 = d109;
                }
                if (v108 > v95 + p108) {
                    v108 = v95 + p108;
                    d108 = d95;
                }
            }
            if (rc.onTheMap(l112)) {
                p112 = 10 + rc.senseRubble(l112);
                if (v112 > v98 + p112) {
                    v112 = v98 + p112;
                    d112 = d98;
                }
                if (v112 > v99 + p112) {
                    v112 = v99 + p112;
                    d112 = d99;
                }
                if (v112 > v111 + p112) {
                    v112 = v111 + p112;
                    d112 = d111;
                }
            }
            if (rc.onTheMap(l45)) {
                p45 = 10 + rc.senseRubble(l45);
                if (v45 > v58 + p45) {
                    v45 = v58 + p45;
                    d45 = d58;
                }
                if (v45 > v57 + p45) {
                    v45 = v57 + p45;
                    d45 = d57;
                }
                if (v45 > v59 + p45) {
                    v45 = v59 + p45;
                    d45 = d59;
                }
            }
            if (rc.onTheMap(l81)) {
                p81 = 10 + rc.senseRubble(l81);
                if (v81 > v82 + p81) {
                    v81 = v82 + p81;
                    d81 = d82;
                }
                if (v81 > v69 + p81) {
                    v81 = v69 + p81;
                    d81 = d69;
                }
                if (v81 > v95 + p81) {
                    v81 = v95 + p81;
                    d81 = d95;
                }
            }
            if (rc.onTheMap(l87)) {
                p87 = 10 + rc.senseRubble(l87);
                if (v87 > v86 + p87) {
                    v87 = v86 + p87;
                    d87 = d86;
                }
                if (v87 > v73 + p87) {
                    v87 = v73 + p87;
                    d87 = d73;
                }
                if (v87 > v99 + p87) {
                    v87 = v99 + p87;
                    d87 = d99;
                }
            }
            if (rc.onTheMap(l123)) {
                p123 = 10 + rc.senseRubble(l123);
                if (v123 > v110 + p123) {
                    v123 = v110 + p123;
                    d123 = d110;
                }
                if (v123 > v111 + p123) {
                    v123 = v111 + p123;
                    d123 = d111;
                }
                if (v123 > v109 + p123) {
                    v123 = v109 + p123;
                    d123 = d109;
                }
            }
            if (rc.onTheMap(l44)) {
                p44 = 10 + rc.senseRubble(l44);
                if (v44 > v58 + p44) {
                    v44 = v58 + p44;
                    d44 = d58;
                }
                if (v44 > v57 + p44) {
                    v44 = v57 + p44;
                    d44 = d57;
                }
                if (v44 > v56 + p44) {
                    v44 = v56 + p44;
                    d44 = d56;
                }
                if (v44 > v45 + p44) {
                    v44 = v45 + p44;
                    d44 = d45;
                }
            }
            if (rc.onTheMap(l46)) {
                p46 = 10 + rc.senseRubble(l46);
                if (v46 > v58 + p46) {
                    v46 = v58 + p46;
                    d46 = d58;
                }
                if (v46 > v59 + p46) {
                    v46 = v59 + p46;
                    d46 = d59;
                }
                if (v46 > v60 + p46) {
                    v46 = v60 + p46;
                    d46 = d60;
                }
                if (v46 > v45 + p46) {
                    v46 = v45 + p46;
                    d46 = d45;
                }
            }
            if (rc.onTheMap(l68)) {
                p68 = 10 + rc.senseRubble(l68);
                if (v68 > v82 + p68) {
                    v68 = v82 + p68;
                    d68 = d82;
                }
                if (v68 > v69 + p68) {
                    v68 = v69 + p68;
                    d68 = d69;
                }
                if (v68 > v56 + p68) {
                    v68 = v56 + p68;
                    d68 = d56;
                }
                if (v68 > v81 + p68) {
                    v68 = v81 + p68;
                    d68 = d81;
                }
            }
            if (rc.onTheMap(l74)) {
                p74 = 10 + rc.senseRubble(l74);
                if (v74 > v86 + p74) {
                    v74 = v86 + p74;
                    d74 = d86;
                }
                if (v74 > v73 + p74) {
                    v74 = v73 + p74;
                    d74 = d73;
                }
                if (v74 > v60 + p74) {
                    v74 = v60 + p74;
                    d74 = d60;
                }
                if (v74 > v87 + p74) {
                    v74 = v87 + p74;
                    d74 = d87;
                }
            }
            if (rc.onTheMap(l94)) {
                p94 = 10 + rc.senseRubble(l94);
                if (v94 > v82 + p94) {
                    v94 = v82 + p94;
                    d94 = d82;
                }
                if (v94 > v95 + p94) {
                    v94 = v95 + p94;
                    d94 = d95;
                }
                if (v94 > v108 + p94) {
                    v94 = v108 + p94;
                    d94 = d108;
                }
                if (v94 > v81 + p94) {
                    v94 = v81 + p94;
                    d94 = d81;
                }
            }
            if (rc.onTheMap(l100)) {
                p100 = 10 + rc.senseRubble(l100);
                if (v100 > v86 + p100) {
                    v100 = v86 + p100;
                    d100 = d86;
                }
                if (v100 > v99 + p100) {
                    v100 = v99 + p100;
                    d100 = d99;
                }
                if (v100 > v112 + p100) {
                    v100 = v112 + p100;
                    d100 = d112;
                }
                if (v100 > v87 + p100) {
                    v100 = v87 + p100;
                    d100 = d87;
                }
            }
            if (rc.onTheMap(l122)) {
                p122 = 10 + rc.senseRubble(l122);
                if (v122 > v110 + p122) {
                    v122 = v110 + p122;
                    d122 = d110;
                }
                if (v122 > v109 + p122) {
                    v122 = v109 + p122;
                    d122 = d109;
                }
                if (v122 > v108 + p122) {
                    v122 = v108 + p122;
                    d122 = d108;
                }
                if (v122 > v123 + p122) {
                    v122 = v123 + p122;
                    d122 = d123;
                }
            }
            if (rc.onTheMap(l124)) {
                p124 = 10 + rc.senseRubble(l124);
                if (v124 > v110 + p124) {
                    v124 = v110 + p124;
                    d124 = d110;
                }
                if (v124 > v111 + p124) {
                    v124 = v111 + p124;
                    d124 = d111;
                }
                if (v124 > v112 + p124) {
                    v124 = v112 + p124;
                    d124 = d112;
                }
                if (v124 > v123 + p124) {
                    v124 = v123 + p124;
                    d124 = d123;
                }
            }
            if (rc.onTheMap(l43)) {
                p43 = 10 + rc.senseRubble(l43);
                if (v43 > v57 + p43) {
                    v43 = v57 + p43;
                    d43 = d57;
                }
                if (v43 > v56 + p43) {
                    v43 = v56 + p43;
                    d43 = d56;
                }
                if (v43 > v44 + p43) {
                    v43 = v44 + p43;
                    d43 = d44;
                }
            }
            if (rc.onTheMap(l47)) {
                p47 = 10 + rc.senseRubble(l47);
                if (v47 > v59 + p47) {
                    v47 = v59 + p47;
                    d47 = d59;
                }
                if (v47 > v60 + p47) {
                    v47 = v60 + p47;
                    d47 = d60;
                }
                if (v47 > v46 + p47) {
                    v47 = v46 + p47;
                    d47 = d46;
                }
            }
            if (rc.onTheMap(l55)) {
                p55 = 10 + rc.senseRubble(l55);
                if (v55 > v69 + p55) {
                    v55 = v69 + p55;
                    d55 = d69;
                }
                if (v55 > v56 + p55) {
                    v55 = v56 + p55;
                    d55 = d56;
                }
                if (v55 > v68 + p55) {
                    v55 = v68 + p55;
                    d55 = d68;
                }
                if (v55 > v43 + p55) {
                    v55 = v43 + p55;
                    d55 = d43;
                }
            }
            if (rc.onTheMap(l61)) {
                p61 = 10 + rc.senseRubble(l61);
                if (v61 > v73 + p61) {
                    v61 = v73 + p61;
                    d61 = d73;
                }
                if (v61 > v60 + p61) {
                    v61 = v60 + p61;
                    d61 = d60;
                }
                if (v61 > v74 + p61) {
                    v61 = v74 + p61;
                    d61 = d74;
                }
                if (v61 > v47 + p61) {
                    v61 = v47 + p61;
                    d61 = d47;
                }
            }
            if (rc.onTheMap(l107)) {
                p107 = 10 + rc.senseRubble(l107);
                if (v107 > v95 + p107) {
                    v107 = v95 + p107;
                    d107 = d95;
                }
                if (v107 > v108 + p107) {
                    v107 = v108 + p107;
                    d107 = d108;
                }
                if (v107 > v94 + p107) {
                    v107 = v94 + p107;
                    d107 = d94;
                }
            }
            if (rc.onTheMap(l113)) {
                p113 = 10 + rc.senseRubble(l113);
                if (v113 > v99 + p113) {
                    v113 = v99 + p113;
                    d113 = d99;
                }
                if (v113 > v112 + p113) {
                    v113 = v112 + p113;
                    d113 = d112;
                }
                if (v113 > v100 + p113) {
                    v113 = v100 + p113;
                    d113 = d100;
                }
            }
            if (rc.onTheMap(l121)) {
                p121 = 10 + rc.senseRubble(l121);
                if (v121 > v109 + p121) {
                    v121 = v109 + p121;
                    d121 = d109;
                }
                if (v121 > v108 + p121) {
                    v121 = v108 + p121;
                    d121 = d108;
                }
                if (v121 > v122 + p121) {
                    v121 = v122 + p121;
                    d121 = d122;
                }
                if (v121 > v107 + p121) {
                    v121 = v107 + p121;
                    d121 = d107;
                }
            }
            if (rc.onTheMap(l125)) {
                p125 = 10 + rc.senseRubble(l125);
                if (v125 > v111 + p125) {
                    v125 = v111 + p125;
                    d125 = d111;
                }
                if (v125 > v112 + p125) {
                    v125 = v112 + p125;
                    d125 = d112;
                }
                if (v125 > v124 + p125) {
                    v125 = v124 + p125;
                    d125 = d124;
                }
                if (v125 > v113 + p125) {
                    v125 = v113 + p125;
                    d125 = d113;
                }
            }
            if (rc.onTheMap(l32)) {
                p32 = 10 + rc.senseRubble(l32);
                if (v32 > v45 + p32) {
                    v32 = v45 + p32;
                    d32 = d45;
                }
                if (v32 > v44 + p32) {
                    v32 = v44 + p32;
                    d32 = d44;
                }
                if (v32 > v46 + p32) {
                    v32 = v46 + p32;
                    d32 = d46;
                }
            }
            if (rc.onTheMap(l80)) {
                p80 = 10 + rc.senseRubble(l80);
                if (v80 > v81 + p80) {
                    v80 = v81 + p80;
                    d80 = d81;
                }
                if (v80 > v68 + p80) {
                    v80 = v68 + p80;
                    d80 = d68;
                }
                if (v80 > v94 + p80) {
                    v80 = v94 + p80;
                    d80 = d94;
                }
            }
            if (rc.onTheMap(l88)) {
                p88 = 10 + rc.senseRubble(l88);
                if (v88 > v87 + p88) {
                    v88 = v87 + p88;
                    d88 = d87;
                }
                if (v88 > v74 + p88) {
                    v88 = v74 + p88;
                    d88 = d74;
                }
                if (v88 > v100 + p88) {
                    v88 = v100 + p88;
                    d88 = d100;
                }
            }
            if (rc.onTheMap(l136)) {
                p136 = 10 + rc.senseRubble(l136);
                if (v136 > v123 + p136) {
                    v136 = v123 + p136;
                    d136 = d123;
                }
                if (v136 > v124 + p136) {
                    v136 = v124 + p136;
                    d136 = d124;
                }
                if (v136 > v122 + p136) {
                    v136 = v122 + p136;
                    d136 = d122;
                }
            }
            if (rc.onTheMap(l31)) {
                p31 = 10 + rc.senseRubble(l31);
                if (v31 > v45 + p31) {
                    v31 = v45 + p31;
                    d31 = d45;
                }
                if (v31 > v44 + p31) {
                    v31 = v44 + p31;
                    d31 = d44;
                }
                if (v31 > v43 + p31) {
                    v31 = v43 + p31;
                    d31 = d43;
                }
                if (v31 > v32 + p31) {
                    v31 = v32 + p31;
                    d31 = d32;
                }
            }
            if (rc.onTheMap(l33)) {
                p33 = 10 + rc.senseRubble(l33);
                if (v33 > v45 + p33) {
                    v33 = v45 + p33;
                    d33 = d45;
                }
                if (v33 > v46 + p33) {
                    v33 = v46 + p33;
                    d33 = d46;
                }
                if (v33 > v47 + p33) {
                    v33 = v47 + p33;
                    d33 = d47;
                }
                if (v33 > v32 + p33) {
                    v33 = v32 + p33;
                    d33 = d32;
                }
            }
            if (rc.onTheMap(l67)) {
                p67 = 10 + rc.senseRubble(l67);
                if (v67 > v81 + p67) {
                    v67 = v81 + p67;
                    d67 = d81;
                }
                if (v67 > v68 + p67) {
                    v67 = v68 + p67;
                    d67 = d68;
                }
                if (v67 > v55 + p67) {
                    v67 = v55 + p67;
                    d67 = d55;
                }
                if (v67 > v80 + p67) {
                    v67 = v80 + p67;
                    d67 = d80;
                }
            }
            if (rc.onTheMap(l75)) {
                p75 = 10 + rc.senseRubble(l75);
                if (v75 > v87 + p75) {
                    v75 = v87 + p75;
                    d75 = d87;
                }
                if (v75 > v74 + p75) {
                    v75 = v74 + p75;
                    d75 = d74;
                }
                if (v75 > v61 + p75) {
                    v75 = v61 + p75;
                    d75 = d61;
                }
                if (v75 > v88 + p75) {
                    v75 = v88 + p75;
                    d75 = d88;
                }
            }
            if (rc.onTheMap(l93)) {
                p93 = 10 + rc.senseRubble(l93);
                if (v93 > v81 + p93) {
                    v93 = v81 + p93;
                    d93 = d81;
                }
                if (v93 > v94 + p93) {
                    v93 = v94 + p93;
                    d93 = d94;
                }
                if (v93 > v107 + p93) {
                    v93 = v107 + p93;
                    d93 = d107;
                }
                if (v93 > v80 + p93) {
                    v93 = v80 + p93;
                    d93 = d80;
                }
            }
            if (rc.onTheMap(l101)) {
                p101 = 10 + rc.senseRubble(l101);
                if (v101 > v87 + p101) {
                    v101 = v87 + p101;
                    d101 = d87;
                }
                if (v101 > v100 + p101) {
                    v101 = v100 + p101;
                    d101 = d100;
                }
                if (v101 > v113 + p101) {
                    v101 = v113 + p101;
                    d101 = d113;
                }
                if (v101 > v88 + p101) {
                    v101 = v88 + p101;
                    d101 = d88;
                }
            }
            if (rc.onTheMap(l135)) {
                p135 = 10 + rc.senseRubble(l135);
                if (v135 > v123 + p135) {
                    v135 = v123 + p135;
                    d135 = d123;
                }
                if (v135 > v122 + p135) {
                    v135 = v122 + p135;
                    d135 = d122;
                }
                if (v135 > v121 + p135) {
                    v135 = v121 + p135;
                    d135 = d121;
                }
                if (v135 > v136 + p135) {
                    v135 = v136 + p135;
                    d135 = d136;
                }
            }
            if (rc.onTheMap(l137)) {
                p137 = 10 + rc.senseRubble(l137);
                if (v137 > v123 + p137) {
                    v137 = v123 + p137;
                    d137 = d123;
                }
                if (v137 > v124 + p137) {
                    v137 = v124 + p137;
                    d137 = d124;
                }
                if (v137 > v125 + p137) {
                    v137 = v125 + p137;
                    d137 = d125;
                }
                if (v137 > v136 + p137) {
                    v137 = v136 + p137;
                    d137 = d136;
                }
            }
            if (rc.onTheMap(l42)) {
                p42 = 10 + rc.senseRubble(l42);
                if (v42 > v56 + p42) {
                    v42 = v56 + p42;
                    d42 = d56;
                }
                if (v42 > v43 + p42) {
                    v42 = v43 + p42;
                    d42 = d43;
                }
                if (v42 > v55 + p42) {
                    v42 = v55 + p42;
                    d42 = d55;
                }
            }
            if (rc.onTheMap(l48)) {
                p48 = 10 + rc.senseRubble(l48);
                if (v48 > v60 + p48) {
                    v48 = v60 + p48;
                    d48 = d60;
                }
                if (v48 > v47 + p48) {
                    v48 = v47 + p48;
                    d48 = d47;
                }
                if (v48 > v61 + p48) {
                    v48 = v61 + p48;
                    d48 = d61;
                }
            }
            if (rc.onTheMap(l120)) {
                p120 = 10 + rc.senseRubble(l120);
                if (v120 > v108 + p120) {
                    v120 = v108 + p120;
                    d120 = d108;
                }
                if (v120 > v121 + p120) {
                    v120 = v121 + p120;
                    d120 = d121;
                }
                if (v120 > v107 + p120) {
                    v120 = v107 + p120;
                    d120 = d107;
                }
            }
            if (rc.onTheMap(l126)) {
                p126 = 10 + rc.senseRubble(l126);
                if (v126 > v112 + p126) {
                    v126 = v112 + p126;
                    d126 = d112;
                }
                if (v126 > v113 + p126) {
                    v126 = v113 + p126;
                    d126 = d113;
                }
                if (v126 > v125 + p126) {
                    v126 = v125 + p126;
                    d126 = d125;
                }
            }
            if (rc.onTheMap(l30)) {
                p30 = 10 + rc.senseRubble(l30);
                if (v30 > v44 + p30) {
                    v30 = v44 + p30;
                    d30 = d44;
                }
                if (v30 > v43 + p30) {
                    v30 = v43 + p30;
                    d30 = d43;
                }
                if (v30 > v31 + p30) {
                    v30 = v31 + p30;
                    d30 = d31;
                }
                if (v30 > v42 + p30) {
                    v30 = v42 + p30;
                    d30 = d42;
                }
            }
            if (rc.onTheMap(l34)) {
                p34 = 10 + rc.senseRubble(l34);
                if (v34 > v46 + p34) {
                    v34 = v46 + p34;
                    d34 = d46;
                }
                if (v34 > v47 + p34) {
                    v34 = v47 + p34;
                    d34 = d47;
                }
                if (v34 > v33 + p34) {
                    v34 = v33 + p34;
                    d34 = d33;
                }
                if (v34 > v48 + p34) {
                    v34 = v48 + p34;
                    d34 = d48;
                }
            }
            if (rc.onTheMap(l54)) {
                p54 = 10 + rc.senseRubble(l54);
                if (v54 > v68 + p54) {
                    v54 = v68 + p54;
                    d54 = d68;
                }
                if (v54 > v55 + p54) {
                    v54 = v55 + p54;
                    d54 = d55;
                }
                if (v54 > v67 + p54) {
                    v54 = v67 + p54;
                    d54 = d67;
                }
                if (v54 > v42 + p54) {
                    v54 = v42 + p54;
                    d54 = d42;
                }
            }
            if (rc.onTheMap(l62)) {
                p62 = 10 + rc.senseRubble(l62);
                if (v62 > v74 + p62) {
                    v62 = v74 + p62;
                    d62 = d74;
                }
                if (v62 > v61 + p62) {
                    v62 = v61 + p62;
                    d62 = d61;
                }
                if (v62 > v75 + p62) {
                    v62 = v75 + p62;
                    d62 = d75;
                }
                if (v62 > v48 + p62) {
                    v62 = v48 + p62;
                    d62 = d48;
                }
            }
            if (rc.onTheMap(l106)) {
                p106 = 10 + rc.senseRubble(l106);
                if (v106 > v94 + p106) {
                    v106 = v94 + p106;
                    d106 = d94;
                }
                if (v106 > v107 + p106) {
                    v106 = v107 + p106;
                    d106 = d107;
                }
                if (v106 > v93 + p106) {
                    v106 = v93 + p106;
                    d106 = d93;
                }
                if (v106 > v120 + p106) {
                    v106 = v120 + p106;
                    d106 = d120;
                }
            }
            if (rc.onTheMap(l114)) {
                p114 = 10 + rc.senseRubble(l114);
                if (v114 > v100 + p114) {
                    v114 = v100 + p114;
                    d114 = d100;
                }
                if (v114 > v113 + p114) {
                    v114 = v113 + p114;
                    d114 = d113;
                }
                if (v114 > v101 + p114) {
                    v114 = v101 + p114;
                    d114 = d101;
                }
                if (v114 > v126 + p114) {
                    v114 = v126 + p114;
                    d114 = d126;
                }
            }
            if (rc.onTheMap(l134)) {
                p134 = 10 + rc.senseRubble(l134);
                if (v134 > v122 + p134) {
                    v134 = v122 + p134;
                    d134 = d122;
                }
                if (v134 > v121 + p134) {
                    v134 = v121 + p134;
                    d134 = d121;
                }
                if (v134 > v135 + p134) {
                    v134 = v135 + p134;
                    d134 = d135;
                }
                if (v134 > v120 + p134) {
                    v134 = v120 + p134;
                    d134 = d120;
                }
            }
            if (rc.onTheMap(l138)) {
                p138 = 10 + rc.senseRubble(l138);
                if (v138 > v124 + p138) {
                    v138 = v124 + p138;
                    d138 = d124;
                }
                if (v138 > v125 + p138) {
                    v138 = v125 + p138;
                    d138 = d125;
                }
                if (v138 > v137 + p138) {
                    v138 = v137 + p138;
                    d138 = d137;
                }
                if (v138 > v126 + p138) {
                    v138 = v126 + p138;
                    d138 = d126;
                }
            }

            int dx = target.x - l84.x;
            int dy = target.y - l84.y;
            switch (dx) {
                case -4:
                    switch (dy) {
                        case -2:
                            return d30;
                        case -1:
                            return d31;
                        case 0:
                            return d32;
                        case 1:
                            return d33;
                        case 2:
                            return d34;
                    }
                    break;
                case -3:
                    switch (dy) {
                        case -3:
                            return d42;
                        case -2:
                            return d43;
                        case -1:
                            return d44;
                        case 0:
                            return d45;
                        case 1:
                            return d46;
                        case 2:
                            return d47;
                        case 3:
                            return d48;
                    }
                    break;
                case -2:
                    switch (dy) {
                        case -4:
                            return d54;
                        case -3:
                            return d55;
                        case -2:
                            return d56;
                        case -1:
                            return d57;
                        case 0:
                            return d58;
                        case 1:
                            return d59;
                        case 2:
                            return d60;
                        case 3:
                            return d61;
                        case 4:
                            return d62;
                    }
                    break;
                case -1:
                    switch (dy) {
                        case -4:
                            return d67;
                        case -3:
                            return d68;
                        case -2:
                            return d69;
                        case -1:
                            return d70;
                        case 0:
                            return d71;
                        case 1:
                            return d72;
                        case 2:
                            return d73;
                        case 3:
                            return d74;
                        case 4:
                            return d75;
                    }
                    break;
                case 0:
                    switch (dy) {
                        case -4:
                            return d80;
                        case -3:
                            return d81;
                        case -2:
                            return d82;
                        case -1:
                            return d83;
                        case 0:
                            return d84;
                        case 1:
                            return d85;
                        case 2:
                            return d86;
                        case 3:
                            return d87;
                        case 4:
                            return d88;
                    }
                    break;
                case 1:
                    switch (dy) {
                        case -4:
                            return d93;
                        case -3:
                            return d94;
                        case -2:
                            return d95;
                        case -1:
                            return d96;
                        case 0:
                            return d97;
                        case 1:
                            return d98;
                        case 2:
                            return d99;
                        case 3:
                            return d100;
                        case 4:
                            return d101;
                    }
                    break;
                case 2:
                    switch (dy) {
                        case -4:
                            return d106;
                        case -3:
                            return d107;
                        case -2:
                            return d108;
                        case -1:
                            return d109;
                        case 0:
                            return d110;
                        case 1:
                            return d111;
                        case 2:
                            return d112;
                        case 3:
                            return d113;
                        case 4:
                            return d114;
                    }
                    break;
                case 3:
                    switch (dy) {
                        case -3:
                            return d120;
                        case -2:
                            return d121;
                        case -1:
                            return d122;
                        case 0:
                            return d123;
                        case 1:
                            return d124;
                        case 2:
                            return d125;
                        case 3:
                            return d126;
                    }
                    break;
                case 4:
                    switch (dy) {
                        case -2:
                            return d134;
                        case -1:
                            return d135;
                        case 0:
                            return d136;
                        case 1:
                            return d137;
                        case 2:
                            return d138;
                    }
                    break;
            }

            Direction ans = null;
            double initialDist = Math.sqrt(l84.distanceSquaredTo(target));

            double dist30 = (initialDist - Math.sqrt(l30.distanceSquaredTo(target))) / v30;
            double dist31 = (initialDist - Math.sqrt(l31.distanceSquaredTo(target))) / v31;
            double dist32 = (initialDist - Math.sqrt(l32.distanceSquaredTo(target))) / v32;
            double dist33 = (initialDist - Math.sqrt(l33.distanceSquaredTo(target))) / v33;
            double dist34 = (initialDist - Math.sqrt(l34.distanceSquaredTo(target))) / v34;
            double dist42 = (initialDist - Math.sqrt(l42.distanceSquaredTo(target))) / v42;
            double dist43 = (initialDist - Math.sqrt(l43.distanceSquaredTo(target))) / v43;
            double dist47 = (initialDist - Math.sqrt(l47.distanceSquaredTo(target))) / v47;
            double dist48 = (initialDist - Math.sqrt(l48.distanceSquaredTo(target))) / v48;
            double dist54 = (initialDist - Math.sqrt(l54.distanceSquaredTo(target))) / v54;
            double dist55 = (initialDist - Math.sqrt(l55.distanceSquaredTo(target))) / v55;
            double dist61 = (initialDist - Math.sqrt(l61.distanceSquaredTo(target))) / v61;
            double dist62 = (initialDist - Math.sqrt(l62.distanceSquaredTo(target))) / v62;
            double dist67 = (initialDist - Math.sqrt(l67.distanceSquaredTo(target))) / v67;
            double dist75 = (initialDist - Math.sqrt(l75.distanceSquaredTo(target))) / v75;
            double dist80 = (initialDist - Math.sqrt(l80.distanceSquaredTo(target))) / v80;
            double dist88 = (initialDist - Math.sqrt(l88.distanceSquaredTo(target))) / v88;
            double dist93 = (initialDist - Math.sqrt(l93.distanceSquaredTo(target))) / v93;
            double dist101 = (initialDist - Math.sqrt(l101.distanceSquaredTo(target))) / v101;
            double dist106 = (initialDist - Math.sqrt(l106.distanceSquaredTo(target))) / v106;
            double dist107 = (initialDist - Math.sqrt(l107.distanceSquaredTo(target))) / v107;
            double dist113 = (initialDist - Math.sqrt(l113.distanceSquaredTo(target))) / v113;
            double dist114 = (initialDist - Math.sqrt(l114.distanceSquaredTo(target))) / v114;
            double dist120 = (initialDist - Math.sqrt(l120.distanceSquaredTo(target))) / v120;
            double dist121 = (initialDist - Math.sqrt(l121.distanceSquaredTo(target))) / v121;
            double dist125 = (initialDist - Math.sqrt(l125.distanceSquaredTo(target))) / v125;
            double dist126 = (initialDist - Math.sqrt(l126.distanceSquaredTo(target))) / v126;
            double dist134 = (initialDist - Math.sqrt(l134.distanceSquaredTo(target))) / v134;
            double dist135 = (initialDist - Math.sqrt(l135.distanceSquaredTo(target))) / v135;
            double dist136 = (initialDist - Math.sqrt(l136.distanceSquaredTo(target))) / v136;
            double dist137 = (initialDist - Math.sqrt(l137.distanceSquaredTo(target))) / v137;
            double dist138 = (initialDist - Math.sqrt(l138.distanceSquaredTo(target))) / v138;

            double bestEstimation = Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(dist30, dist31), dist32), dist33), dist34), dist42), dist43), dist47), dist48), dist54), dist55), dist61), dist62), dist75), dist80), dist88), dist93), dist101), dist106), dist107), dist113), dist114), dist120), dist121), dist125), dist126), dist134), dist135), dist136), dist137), dist138);

            if (bestEstimation == dist30) {
                return d30;
            }

            if (bestEstimation == dist31) {
                return d31;
            }

            if (bestEstimation == dist32) {
                return d32;
            }

            if (bestEstimation == dist33) {
                return d33;
            }

            if (bestEstimation == dist34) {
                return d34;
            }

            if (bestEstimation == dist42) {
                return d42;
            }

            if (bestEstimation == dist43) {
                return d43;
            }

            if (bestEstimation == dist47) {
                return d47;
            }

            if (bestEstimation == dist48) {
                return d48;
            }

            if (bestEstimation == dist54) {
                return d54;
            }

            if (bestEstimation == dist55) {
                return d55;
            }

            if (bestEstimation == dist61) {
                return d61;
            }

            if (bestEstimation == dist62) {
                return d62;
            }

            if (bestEstimation == dist67) {
                return d67;
            }

            if (bestEstimation == dist75) {
                return d75;
            }

            if (bestEstimation == dist80) {
                return d80;
            }

            if (bestEstimation == dist88) {
                return d88;
            }

            if (bestEstimation == dist93) {
                return d93;
            }

            if (bestEstimation == dist101) {
                return d101;
            }

            if (bestEstimation == dist106) {
                return d106;
            }

            if (bestEstimation == dist107) {
                return d107;
            }

            if (bestEstimation == dist113) {
                return d113;
            }

            if (bestEstimation == dist114) {
                return d114;
            }

            if (bestEstimation == dist120) {
                return d120;
            }

            if (bestEstimation == dist121) {
                return d121;
            }

            if (bestEstimation == dist125) {
                return d125;
            }

            if (bestEstimation == dist126) {
                return d126;
            }

            if (bestEstimation == dist134) {
                return d134;
            }

            if (bestEstimation == dist135) {
                return d135;
            }

            if (bestEstimation == dist136) {
                return d136;
            }

            if (bestEstimation == dist137) {
                return d137;
            }

            return d138;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}