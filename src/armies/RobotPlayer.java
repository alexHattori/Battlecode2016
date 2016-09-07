package armies;

import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
	private static Random random = null;
	
	/* STRATEGY:
	 * - Bring archons together
	 * - Build units, some defensive, some offensive
	 * - Offensive units go to rally point
	 * - After enough offensive units, clear map
	 * - Split army if required
	 * 
	 * DEFENSIVE UNITS:
	 * - Turrets + guards
	 * 
	 * OFFENSIVE UNITS:
	 * - 
	 */
	
	public static void run(RobotController rc){
		random = new Random(rc.getID());
		
		try {
			switch (rc.getType()) {
			case ARCHON:
				runArchon(rc);
				break;
			case GUARD:
				runGuard(rc);
				break;
			case SCOUT:
				runScout(rc);
				break;
			case SOLDIER:
				runSoldier(rc);
				break;
			case TTM:
			case TURRET:
				runTTMTurret(rc);
				break;
			case VIPER:
				runViper(rc);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void tryAttackMoveDirection(RobotController rc, Direction direction) throws GameActionException {
		if (rc.isWeaponReady()) {
			RobotInfo[] robotInfos = rc.senseHostileRobots(rc.getLocation(), -1);
			for (RobotInfo robotInfo : robotInfos) {
				if (rc.canAttackLocation(robotInfo.location)) {
					rc.attackLocation(robotInfo.location);
					break;
				}
			}
		}
		
		tryMoveDirection(rc, direction);
	}
	private static void tryMoveDirection(RobotController rc, Direction direction) throws GameActionException {
		if (rc.isCoreReady()) {
			if (rc.senseRubble(rc.getLocation().add(direction)) > GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
				rc.clearRubble(direction);
			} else if (rc.canMove(direction)) {
				rc.move(direction);
			}
		}
	}
	
	private enum ArchonState {
		BUILDING_ESCORTS,
		COMBINING
	}
	
	private static ArchonState archonState = ArchonState.BUILDING_ESCORTS;
	private static final int ESCORT_SOLDIERS = 5;
	
	private static void runArchon(RobotController rc) throws GameActionException {
		while (true) {
			// Heal weakest ally
			RobotInfo weakest = null;
			for (RobotInfo ri : rc.senseNearbyRobots(-1, rc.getTeam())) {
				if (ri.type != RobotType.ARCHON &&
						(weakest == null || ri.health / ri.maxHealth < weakest.health / weakest.maxHealth)) {
					weakest = ri;
				}
			}
			rc.repair(weakest.location);
			
			// Activate nearby neutrals
			for (Direction dir : Direction.values()) {
//				Location loc = rc.getLocation().add(dir)
//				if (rc.senseRobotAtLocation(loc))
			}
			
			switch (archonState) {
			case BUILDING_ESCORTS:
				int numSoldiers = 0;
				for (RobotInfo ri : rc.senseNearbyRobots(-1, rc.getTeam())) {
					if (ri.type == RobotType.SOLDIER) {
						numSoldiers++;
					}
				}
				
				if (numSoldiers < ESCORT_SOLDIERS) {
					if (rc.isCoreReady()) {
						for (Direction dir : Direction.values()) {
							if (rc.canBuild(dir, RobotType.SOLDIER)) {
								rc.build(dir, RobotType.SOLDIER);
								break;
							}
						}
					}
				} else {
					archonState = ArchonState.COMBINING;
				}
				break;
			case COMBINING:
				
				break;
			}
			
			Clock.yield();
		}
	}
	
	private static void runGuard(RobotController rc) throws GameActionException {
		while (true) {
			Clock.yield();
		}
	}

	private static void runScout(RobotController rc) throws GameActionException {
		while (true) {
			Clock.yield();
		}
	}
	
	private static void runSoldier(RobotController rc) throws GameActionException {
		while (true) {
			Clock.yield();
		}
	}
	
	private static void runTTMTurret(RobotController rc) throws GameActionException {
		while (true) {
			Clock.yield();
		}
	}
	
	private static void runViper(RobotController rc) throws GameActionException {
		while (true) {
			Clock.yield();
		}
	}
}
