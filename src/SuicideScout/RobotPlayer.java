package SuicideScout;

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer {


	/*
	 * Put code in these run methods
	 */

	private static void runArchon() throws GameActionException{
		if(rc.isCoreReady()){
			Direction randDir = directions[rand.nextInt(4)];
			if(rc.canBuild(randDir, RobotType.SCOUT)){
				rc.build(randDir, RobotType.SCOUT);
			}
			if(rc.canMove(randDir.opposite())&&rc.isCoreReady()){
				rc.move(randDir.opposite());
			}
		}
	}
	private static void runViper() throws GameActionException{
		// TODO Auto-generated method stub
	}
	private static void runTurret() throws GameActionException{
		// TODO Auto-generated method stub
	}
	private static void runGuard() throws GameActionException{
		// TODO Auto-generated method stub
	}
	private static void runSoldier() throws GameActionException{
		// TODO Auto-generated method stub
	}
	private static void runScout() throws GameActionException{
		if(rc.isCoreReady()){
			MapLocation myLoc = rc.getLocation();
			Direction goalDir = myLoc.directionTo(creatorLoc).opposite();
			if(rc.canMove(goalDir)){
				rc.move(goalDir);
			}
		}
	}
	private static void runTTM() throws GameActionException{
		// TODO Auto-generated method stub
	}

	/*
	 * =====
	 * TEMPLATE BELOW
	 * ===
	 */

	static Direction[] directions = { Direction.NORTH_EAST,Direction.SOUTH_EAST,
			Direction.SOUTH_WEST, Direction.NORTH_WEST};
	static RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
			RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET};
	static Random rand;
	static int myAttackRange = 0;
	static Team myTeam;
	static Team enemyTeam;
	static RobotController rc;

	static enum STATES {INIT,SEARCHING,LURING}; //Add States if Needed
	static STATES curState;
	static MapLocation[] usefulLocs;
	static RobotInfo creator;
	static MapLocation creatorLoc;
	static int suicideThresh = 10;

	/**
	 * run() is the method that is called when a robot is instantiated in the Battlecode world.
	 * If this method returns, the robot dies!
	 **/

	public static void run(RobotController rc) {
		RobotPlayer.rc = rc;
		rand = new Random(rc.getID());
		myTeam = rc.getTeam();
		enemyTeam= myTeam.opponent();
		myAttackRange = rc.getType().attackRadiusSquared;

		curState = STATES.INIT;
		usefulLocs = new MapLocation[5];
		
		RobotInfo[] initFriends = rc.senseNearbyRobots(1);
		
		for(int i = initFriends.length-1;i>=0;--i){ //Finds a touching Archon on startup. Archons may or may not have creators.
			RobotInfo curFriend = initFriends[i];
			if(curFriend.type == RobotType.ARCHON){
				creator = curFriend;
				creatorLoc = curFriend.location;
			}
		}
		if(creatorLoc == null){
			creatorLoc = rc.getLocation();
		}
		//Insert Any Initialization Methods Here If Needed
		try{
			switch(rc.getType()){
			
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		while(true){
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
				if(rc.getHealth()<=suicideThresh && rc.isInfected()){ //Rather than turn into Zombie, die
					rc.disintegrate();
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
