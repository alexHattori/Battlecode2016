package GuardParty;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

public class RobotPlayer {

	private static void runArchon() throws GameActionException{
		if(rc.isCoreReady()){
			MapLocation myLoc = rc.getLocation();
			switch(curState){
			case INIT:
				curState = STATES.SEARCHING;
				break;
			case SEARCHING:
				Direction goalDir;
				if(initArchon.length>0){
					goalDir = myLoc.directionTo(initArchon[0]).opposite();
				}
				else{
					goalDir = directions[rand.nextInt(8)];
				}
				if(!rc.canMove(goalDir)){
					Direction left = goalDir.rotateLeft().rotateLeft();
					Direction right = goalDir.rotateRight().rotateRight();
					if(rc.isLocationOccupied(myLoc.add(left))||rc.isLocationOccupied(myLoc.add(right))||rc.isLocationOccupied(myLoc.add(goalDir))){
						break;
					}
					if(!rc.canMove(left)&&!rc.canMove(right)){
						inCorner = true;
					}
				}
				else{
					rc.move(goalDir);
				}
				if(inCorner){
					curState = STATES.SIGNALING;
				}
				break;
			case SIGNALING:
				rc.broadcastSignal(4);
				if(signalPeriod>0){
					signalPeriod--;
				}
				else{
					curState = STATES.WAITING;
				}
				break;
			case WAITING:
				//				if(rand.nextInt(100)<10){
				//					curState = STATES.SEARCHING;
				//					inCorner = false;
				//				}
				break;
			}
			Direction dir = directions[rand.nextInt(8)];
			RobotType typeToBuild = types[rand.nextInt(3)];
			if(rc.senseHostileRobots(myLoc, -1).length>3){
				typeToBuild = RobotType.GUARD;
			}
			if(rc.hasBuildRequirements(typeToBuild)&&rc.isCoreReady()){
				while(!rc.canBuild(dir, typeToBuild)){
					dir = dir.rotateRight();
				}
				rc.build(dir, typeToBuild);
			}

		}
	}
	private static void runViper() throws GameActionException{
	}
	private static void runTurret() throws GameActionException{
	}
	private static void runGuard() throws GameActionException{
		MapLocation myLoc = rc.getLocation();
		if(rc.isCoreReady()){
			Direction goalDir = myLoc.directionTo(creatorLoc);

			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(),rc.getType().attackRadiusSquared);
			if(enemies.length>0){
				if(rc.isWeaponReady()){
					RobotInfo weakestEnemy = enemies[0];
					boolean foundTurret = false;
					boolean foundArchon = false;
					for(RobotInfo a:enemies){
						if(foundTurret){
							if(a.type==RobotType.TURRET && a.health<weakestEnemy.health){
								weakestEnemy = a;
							}
						}
						else if(foundArchon){
							if(a.type==RobotType.TURRET){
								foundTurret = true;
								weakestEnemy = a;
							}
							else if(a.type == RobotType.ARCHON && a.health<weakestEnemy.health){
								weakestEnemy = a;
							}
						}
						else{
							if(a.type == RobotType.TURRET){
								foundTurret = true;
								weakestEnemy = a;
							}
							else if(a.type == RobotType.ARCHON){
								foundArchon = true;
								weakestEnemy = a;
							}
							else if(a.health<weakestEnemy.health){
								weakestEnemy = a;
							}
						}
					}
					rc.attackLocation(weakestEnemy.location);
				}
			}
			else{
				Signal curSignal = rc.readSignal();
				if(curSignal!=null){
					if(curSignal.getID()==creatorID){
						creatorLoc = curSignal.getLocation();
					}
				}
				RobotInfo[] localFriends = rc.senseNearbyRobots(1, rc.getTeam());
				for(RobotInfo lf:localFriends){
					if(lf.type==RobotType.GUARD)
						if(lf.location.isAdjacentTo(myLoc)){
							goalDir = myLoc.directionTo(lf.location).opposite();
						}
				}
				if(goalDir == null){
					goalDir = directions[rand.nextInt(8)];
				}
				double rb = rc.senseRubble(myLoc.add(goalDir));
				if(rb>0 && rb<200){
					rc.clearRubble(goalDir);
				}
				if(rc.isCoreReady()){
					while(!rc.canMove(goalDir)){
						goalDir = goalDir.rotateLeft();
					}
					rc.move(goalDir);
				}
			}
		}

	}
	private static void runSoldier() throws GameActionException{
		
	}
	private static void runScout() throws GameActionException{
		


	}
	private static void runTTM() throws GameActionException{
	}

	/*
	 * =====
	 * TEMPLATE BELOW
	 * ===
	 */

	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
			Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET};
	static Random rand;
	static int myAttackRange = 0;
	static Team myTeam;
	static Team enemyTeam;
	static RobotController rc;

	static enum STATES {INIT,SEARCHING,SIGNALING,LURING,WAITING};
	static RobotType[] types = {RobotType.GUARD, RobotType.GUARD, RobotType.GUARD};
	static STATES curState;
	static MapLocation[] usefulLocs;
	static RobotInfo creator;
	static MapLocation creatorLoc;
	static int suicideThresh = 10;
	static ArrayList<MapLocation> visitedLocs;
	static int eArchonID;
	static MapLocation[] initArchon;
	static boolean inCorner;
	static int signalPeriod;
	static int creatorID;

	public static void run(RobotController rc) {
		RobotPlayer.rc = rc;
		rand = new Random(rc.getID());
		myTeam = rc.getTeam();
		enemyTeam= myTeam.opponent();
		myAttackRange = rc.getType().attackRadiusSquared;

		curState = STATES.INIT;
		usefulLocs = new MapLocation[5];
		RobotInfo[] initFriends = rc.senseNearbyRobots(3,rc.getTeam());
		visitedLocs = new ArrayList<MapLocation>();
		initArchon = rc.getInitialArchonLocations(enemyTeam);
		inCorner = false;
		signalPeriod = 5;

		for(int i = initFriends.length-1;i>=0;--i){
			RobotInfo curFriend = initFriends[i];
			if(curFriend.type == RobotType.ARCHON){
				creator = curFriend;
				creatorLoc = creator.location;
				creatorID = creator.ID;
			}
		}
		//Insert Any Initialization Methods Here If Needed
		switch(rc.getType()){

		}
		while(true){
			if(rc.getHealth()<=suicideThresh && rc.isInfected()){ //Rather than turn into Zombie, die
				rc.disintegrate();
				continue;
			}
			try{
				switch(rc.getType()){
				case ARCHON:
					runArchon();
					break;
				case SCOUT: 
					runScout();
					break; 
				case SOLDIER: 
					runSoldier(); 
					break; 
				case GUARD:
					runGuard(); 
					break;
				case VIPER:
					runViper();
					break;
				case TURRET:
					runTurret();
					break;
				case TTM:
					runTTM();
					break;
				}

			}catch (Exception e) {
				// Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
				// Caught exceptions will result in a bytecode penalty.
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			Clock.yield();
		}

	}

}
