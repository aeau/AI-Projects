package mcts;

import static pacman.game.Constants.EDIBLE_TIME;
import static pacman.game.Constants.EDIBLE_TIME_REDUCTION;
import static pacman.game.Constants.LEVEL_RESET_REDUCTION;

import java.awt.Color;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

import dataRecording.HelperExtendedGame;
import mcts.UCT.Tactics;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public class MCTSSimulation {
	
	//Tree phase & default phase
	//Also default policy
	//
	private HelperExtendedGame helper 	= null;
	private Random				random 	= new Random();
	
	//Variables shared by phases and reward
	private int 	initial_maze 		= 0;
	private float 	initial_ghosts 		= 0.0f;
	private float 	initial_pills 		= 0.0f;
	private float 	initial_pp 			= 0.0f;
	private long	time_due;
	
	private float 	ghost_eaten 		= 0.0f;
	private float	ghost_multiplier 	= 0.0f;
	private float 	pills_eaten 		= 0.0f;
	private float 	power_pills_eaten 	= 0.0f;
	
	
	private Stack<MCTSNode> selected_nodes;
	private boolean dead 				= false;
	private boolean ate_pp 				= false;
	private boolean ate_ghost			= false;
	private boolean ilegal_power_pill 	= false;
	
	//State of the game.
	private Game 	current_state 		= null;
	private Game 	last_visited_state 	= null;
	private int 	last_visited_junc 	= -1;
	
	//deafault phase values
	private Game 	playout_state 		= null;
	private float 	starting_time 		= 0.0f;
	private boolean reverse 			= false;
	private MOVE 	current_pacman_act	= MOVE.NEUTRAL;
	private int 	previous_pacman_pos = -1;
	private int 	next_dest 			= -1;
	
	//For end game tactic only
	int 	target 						= -1;
	float 	previous_distance_to_target = -1f;
	float 	actual_distance_to_target 	= -1f;
	boolean target_reached 				= false;
	Tactics	current_tactic				= Tactics.PILL;
	
	
	public MCTSSimulation(Stack<MCTSNode> nodes, MCTSNode root_node, HelperExtendedGame current_helper)
	{
		helper = current_helper;
		current_state = root_node.state.copy();
		initial_maze = current_state.getMazeIndex();
		initial_ghosts = current_state.getNumGhostsEaten();
		initial_pills = current_state.getNumberOfActivePills();
		initial_pp = current_state.getNumberOfActivePowerPills();
		selected_nodes = nodes;
	}
	
	public MCTSSimulation()
	{
		
	}
	
	public void init(Stack<MCTSNode> nodes, MCTSNode root_node, HelperExtendedGame current_helper, long time_due, int target, Tactics tactic)
	{
		ResetEverything();
		helper 					= current_helper;
		current_state 			= root_node.state.copy();
		initial_maze 			= current_state.getMazeIndex();
		initial_ghosts 			= current_state.getNumGhostsEaten();
		initial_pills 			= current_state.getNumberOfActivePills();
		initial_pp 				= current_state.getNumberOfActivePowerPills();
		selected_nodes 			= nodes;
		this.time_due 			= time_due;
		reverse					= false;
		
		this.current_tactic = tactic;
		if(target != -1)
		{
			this.target = target;
			this.previous_distance_to_target = current_state.getShortestPathDistance(
												current_state.getPacmanCurrentNodeIndex(), 
												this.target);
//			System.out.println("TARGET IS: " + this.target + "; AND TACTIC IS: " + this.current_tactic );
			
		}
	}
	
	private void ResetEverything()
	{
		initial_maze 		= 0;     
		initial_ghosts 		= 0.0f;  
		initial_pills 		= 0.0f;  
		initial_pp 			= 0.0f;  
		time_due			= 0;                    
		                             
		ghost_eaten 		= 0.0f;  
		ghost_multiplier 	= 0.0f;  
		pills_eaten 		= 0.0f;  
		power_pills_eaten 	= 0.0f;  
		                               
		dead 				= false; 
		ate_pp 				= false; 
		ate_ghost			= false; 
		ilegal_power_pill 	= false; 
		                             
		current_state 		= null;  
		last_visited_state 	= null;  
		last_visited_junc 	= -1;    
		                             
		playout_state 		= null;  
		starting_time 		= 0.0f;        
		reverse 			= false;        
		current_pacman_act	= MOVE.NEUTRAL; 
		previous_pacman_pos = -1;           
		helper 				 = null;
		
		target 						= -1;   
		previous_distance_to_target = -1f;  
		actual_distance_to_target 	= -1f;  
		target_reached 				= false;
		current_tactic				= null;
		
	}
	
	private void UpdateInfo(Game st, Game previous_state)
	{
		if(st.wasPacManEaten()) { dead = true; }
		if(st.wasPillEaten()) pills_eaten += 1.0f;
		if(st.wasPowerPillEaten())
		{
			power_pills_eaten += 1.0f;
			if(ate_pp)
			{
				ilegal_power_pill = true;
			}
			else
			{
				ate_pp = true;
			}
		}
		
		for(GHOST ghost : GHOST.values())
		{
			if(st.wasGhostEaten(ghost))
			{
				ghost_eaten += 1.0f;
				ghost_multiplier += previous_state.getGhostEdibleTime(ghost);
				ate_ghost = true;
			}
		}
		
		if(st.isJunction(st.getPacmanCurrentNodeIndex()) && !dead)
		{
			last_visited_junc = st.getPacmanCurrentNodeIndex();
			last_visited_state = st.copy();
		}
		
		if(this.current_tactic == Tactics.ENDGAME &&
				!target_reached &&
			st.getShortestPathDistance(st.getPacmanCurrentNodeIndex(), this.target) < 3)
		{
			target_reached = true;
		}
		
		previous_state = st.copy();
	}
	
	public MCTSReward Simulate()
	{
		SetupLegacy2TheReckoning();
		PerformTreePhase();
		return PerformPlayoutPhase();
	}
	
	private void PerformTreePhase()
	{
		//Go through all the selected nodes.
//		System.out.println("THIS MANY TIMES THIS SHOULD BE EEXECUTED UNLESS I DIE: " + selected_nodes.size());
		playout_state = current_state;
		if(selected_nodes.isEmpty())
			return;
		//Next position after root node selected here
		MCTSNode current = selected_nodes.pop();
		int pacman_pos = current_state.getPacmanCurrentNodeIndex();
		Game previous_state = current_state.copy();
		
		
		//Don't follow path just get to destination in next node.
		
		outer:
		while(current != null && !TreePhaseOver(current_state))
		{
			next_dest = current.destination;
			previous_pacman_pos = pacman_pos;
			//we take the movement of the current node.
			current_state.advanceGame(current.pacman_move, GhostsPlayout(current_state));
			UpdateInfo(current_state, previous_state);
			if(TreePhaseOver(current_state))
			{
				break outer;
			}
			
//			actual_path = current.my_path;
//			current_state = current.state.copy();
			int dest = current.destination;
			
			follow_path:
			while(pacman_pos != dest)
			{
				pacman_pos = current_state.getPacmanCurrentNodeIndex();
				MOVE next_pacman_move = current_state.getPossibleMoves(pacman_pos, current_state.getPacmanLastMoveMade())[0];
//				current_state.advanceGame(next_pacman_move, GhostsPlayout(current_state));
				current_state.advanceGame(next_pacman_move, getGhostMove(current_state));
				UpdateInfo(current_state, previous_state);
				if(TreePhaseOver(current_state))
				{
					break follow_path;
				}
			}
			
			MCTSConstants.TIMES++;
			
			if(!selected_nodes.empty())
				current = selected_nodes.pop();
			else
				current = null;
		}
		
//		System.out.println("We go out of the loop, a.k.a we aarrived to destination; TIME: " + MCTSConstants.TIMES);
		
		if((dead || ilegal_power_pill) && last_visited_state != null)
		{
			playout_state = last_visited_state.copy();
		}
		
			playout_state = current_state.copy();
	}

	private MCTSReward PerformPlayoutPhase()
	{
		Game previous_state = playout_state.copy();
		boolean action_done = false;
		next_dest = playout_state.getPacmanCurrentNodeIndex();
		boolean first_time = true;
		dead = false;
		starting_time = playout_state.getCurrentLevelTime();
		int pacman_pos = playout_state.getPacmanCurrentNodeIndex();
//		SetupLegacy2TheReckoning();
		
		if(MCTSConstants.DEBUG)
		{
			System.out.println("initial_ghosts	: "	+ initial_ghosts 		);
			System.out.println("initial_pills	: "	+ initial_pills 		);
			System.out.println("initial_pp	: "	+ initial_pp 			);
			System.out.println("ghost_eaten	: "	+ ghost_eaten 		);
			System.out.println("ghost_multiplier: " + ghost_multiplier );
			System.out.println("pills_eaten	: "	+ pills_eaten 		);
			System.out.println("power_pills_eaten: "+ power_pills_eaten);
			System.out.println();
		}
		
		while(!Terminate() && !DefaultPhaseOver(playout_state))
		{
			pacman_pos = playout_state.getPacmanCurrentNodeIndex();
			action_done = false;
			
			//Improved playout version
			if(playout_state.wasPacManEaten() || playout_state.getCurrentLevelTime() - starting_time == 0 || first_time)
			{
				previous_pacman_pos = pacman_pos;
				current_pacman_act = PacmanPlayoutJunction(playout_state);
				next_dest = helper.NextJunctionTowardMovement(pacman_pos, current_pacman_act);
				reverse = false;
				action_done = true;
				first_time = false;
			}

			//AT JUNCTION
			if(helper.IsJunction(pacman_pos))
			{
				previous_pacman_pos = pacman_pos;
				current_pacman_act = PacmanPlayoutJunction(playout_state);
				next_dest = helper.NextJunctionTowardMovement(pacman_pos, current_pacman_act);
				action_done = true;
				reverse = false;
			}
			else if(!reverse)//IN THE PATH
			{
				int[] updated_path = playout_state.getShortestPath(pacman_pos, next_dest); //--> TODO: check next_dest from time to time gives -1 ... FU
				int index = PacmanPlayoutPath(playout_state, updated_path);
				
				if(index != -1)
				{
					previous_pacman_pos = pacman_pos;
					current_pacman_act = playout_state.getNextMoveTowardsTarget(pacman_pos, index, DM.PATH);
					next_dest = index;
					action_done = true;
				}
			}
			
			if(!action_done)
			{
				current_pacman_act = playout_state.getPossibleMoves(pacman_pos, playout_state.getPacmanLastMoveMade())[0];	
			}
					
			playout_state.advanceGame(current_pacman_act,getGhostMove(playout_state));	
//			current_state.advanceGame(current_pacman_act, GhostsPlayout(playout_state));
			UpdateInfo(playout_state, previous_state);
		}
		
		if(current_tactic == Tactics.ENDGAME && !target_reached && !dead)
		{
			actual_distance_to_target = playout_state.getShortestPathDistance(pacman_pos, target);
		}

		return CalculateReward();
	}
	
	private MCTSReward CalculateReward()
	{
		float pill_score = 0.0f;
		float ghost_score = 0.0f;
		float survival_score = 0.0f;
		
		if(!dead)
		{
			survival_score = 1.0f;
		}
		
		if(current_tactic == Tactics.ENDGAME)
		{
			if(dead)
			{
				pill_score = 0.0f;
			}
			else if(target_reached)
			{
				pill_score = 1.0f;
			}
			else
			{
				if(actual_distance_to_target > previous_distance_to_target)
				{
					pill_score = 0.0f;
				}
				else
				{
					pill_score = Math.abs((actual_distance_to_target/previous_distance_to_target) - 1.0f);
				}
//				System.out.println("DISTANCE TO TARGET: " + actual_distance_to_target + "; STARTING DIST: " + previous_distance_to_target);
//				System.out.println("PILL REWARD: "+ pill_score);
			}
		}
		else if (initial_pills > 0) 
		{
			//
			pill_score = pills_eaten / (float)initial_pills;
		}
		
		if(ghost_eaten > 0)
		{
//				System.out.println("WTF IS THIS VALUE AT THE END ?: " + EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,helper.maze_index%LEVEL_RESET_REDUCTION)));
//				System.out.println("GHOST MULTIPLIER BEFORE NORMALIZATION: " + ghost_time_multiplier + ", GHOST EATEN BEFORE NORM: " + ghost_eaten);
			ghost_eaten /= 4.0f;
			ghost_multiplier /= (EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,helper.maze_index%LEVEL_RESET_REDUCTION)) * 4.0f);
//				System.out.println("GHOST MULTIPLIER AFTER NORMALIZATION: " + ghost_time_multiplier + ", GHOST EATEN AFTER NORM: " + ghost_eaten);
			ghost_score = ghost_multiplier; //MAYBE WE NEED THE GHOST EATEN
		}
//			System.out.println("GHOST SCORE: " + ghost_reward);
//			if (previous_pp > st.getNumberOfActivePowerPills()) 
		if(ate_pp && current_tactic != Tactics.ENDGAME)
		{
			
			if(ghost_multiplier >= MCTSConstants.GHOST_SCORE_THRESHOLD) //MAYBE GHOST EATEN INSTEAD OF MULTIPLER //TODO:
			{
				pill_score += ghost_score;
//				System.out.println("THE REWARD IS TASTY");
			}
			else
			{
				pill_score = 0.0f;
//					System.out.println("THIS IS HAPPENING BABY");
			}

		}
		
		if(MCTSConstants.DEBUG)
		{
			System.out.println("PILL REWARD: " + pill_score);
			System.out.println("GHOST REWARD: " + ghost_score);
			System.out.println("SURVIVAL REWARD: " + survival_score);
		}
		
		return new MCTSReward(pill_score, ghost_score, survival_score);
	}
	
	//TODO: CHECK THE VALUES AFTER ISPATHSAFE
	public int PacmanPlayoutPath(Game st, int... path)
	{
		boolean just_ate = false;
		
		if(!reverse)
		{
			if(!helper.IsPathSafePowerPill(st, path) && helper.WillGhostsArriveFirst(st, path[path.length - 1]))
			{
//					System.out.println("I'M SCARED PLEAE DONT KILL ME GHOST");
				reverse = true;
				return previous_pacman_pos;
			}
		
			int go = -1;
			int min_dist = Integer.MAX_VALUE;
			GHOST selected_ghost = null;
			
			if(st.wasPowerPillEaten()) //we ate powe pill so check for edible ghostss in the way :D :D :D
			{
				for(GHOST ghost : GHOST.values())
				{
					if(st.isGhostEdible(ghost) && st.getGhostLairTime(ghost)==0)
					{
						int dist = st.getShortestPathDistance(st.getPacmanCurrentNodeIndex(),st.getGhostCurrentNodeIndex(ghost));
						if(dist < min_dist)
						{
							selected_ghost = ghost;
							go = st.getGhostCurrentNodeIndex(ghost);
							min_dist = dist;
						}
					}
				}
				
				if(selected_ghost == null)
					return -1;
				
				MOVE m = st.getNextMoveTowardsTarget(st.getPacmanCurrentNodeIndex(), st.getGhostCurrentNodeIndex(selected_ghost), DM.PATH);
				
				if(m == st.getPacmanLastMoveMade().opposite() || m.opposite() == st.getPacmanLastMoveMade())
				{
					reverse = true;
				}
				
				return go;
			}
			else
			{
				for(GHOST ghost : GHOST.values())
				{
					if(st.wasGhostEaten(ghost))
					{
						for(GHOST ghost_2 : GHOST.values())
						{
							if(st.isGhostEdible(ghost) && st.getGhostLairTime(ghost)==0)
							{
								int dist = st.getShortestPathDistance(st.getPacmanCurrentNodeIndex(),st.getGhostCurrentNodeIndex(ghost));
								if(dist < min_dist)
								{
									selected_ghost = ghost;
									go = st.getGhostCurrentNodeIndex(ghost);
									min_dist = dist;
								}
							}
						}
						
						if(selected_ghost == null)
							return -1;
						
						MOVE m = st.getNextMoveTowardsTarget(st.getPacmanCurrentNodeIndex(), st.getGhostCurrentNodeIndex(selected_ghost), DM.PATH);
						
						if(m == st.getPacmanLastMoveMade().opposite() || m.opposite() == st.getPacmanLastMoveMade())
						{
							reverse = true;
						}
						
						return go;
					}
				}
			}

		}
		
		return -1;
	}
	
		
	//Return junction to move to
	public MOVE PacmanPlayoutJunction(Game st)
	{
		//First we set the possible safe moves
		//i.e. • Has no non-edible ghost on it moving in Pac-Man’s direction.
//			• Next junction is safe, i.e. in any case Pac-Man will reach
//			the next junction before a non-edible ghost.
		MOVE[] possibleMoves=st.getPossibleMoves(st.getPacmanCurrentNodeIndex(), st.getPacmanLastMoveMade());
		ArrayList<MOVE> safe_moves = new ArrayList<MOVE>();
		ArrayList<MOVE> moves = new ArrayList<MOVE>();
		MOVE selected_move = null;
		int path_pills = 0;
		
		moves:
		for(MOVE move : possibleMoves)
		{
			int junc = helper.NextJunctionTowardMovement(st.getPacmanCurrentNodeIndex(), move);
			if(junc == -1)
				continue moves;
			
			int[] path = helper.GetPathFromMove(move);
			moves.add(move);
			
			if(helper.IsPathSafePowerPill(st, path) && !helper.WillGhostsArriveFirst(st, path[path.length - 1]))
			{
				if(helper.EdibleGhostInPath(st, path))
				{
					return move;
				}
				
				safe_moves.add(move);
				
				int pi = helper.PillsInPath(st, path);
				
				if(pi >= path_pills)
				{
					selected_move = move;
					path_pills = pi;
				}
			}
		}
		
		if(selected_move != null)
		{
			return selected_move;
		}
			
		
		if(safe_moves.isEmpty())
		{
			if(!moves.isEmpty())
				return moves.get(random.nextInt(moves.size()));
			else
			{
				return possibleMoves[0];
			}
		}
		
		return safe_moves.get(random.nextInt(safe_moves.size()));
	}

	public static final int CROWDED_DISTANCE=30;
	public static final int PACMAN_DISTANCE=10;
    public static final int PILL_PROXIMITY=15;
    private final EnumMap<GHOST,MOVE> myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);
    private final EnumMap<GHOST,Integer> cornerAllocation=new EnumMap<GHOST,Integer>(GHOST.class);
    
    /**
     * Instantiates a new legacy2 the reckoning.
     */
    public void SetupLegacy2TheReckoning()
    {
    	cornerAllocation.put(GHOST.BLINKY,0);
    	cornerAllocation.put(GHOST.INKY,1);
    	cornerAllocation.put(GHOST.PINKY,2);
    	cornerAllocation.put(GHOST.SUE,3);
    }
    
    /* (non-Javadoc)
     * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
     */
    public EnumMap<GHOST,MOVE> getGhostMove(Game game)
    {
		int pacmanIndex=game.getPacmanCurrentNodeIndex();
    	
        for(GHOST ghost : GHOST.values())      
        {
        	if(game.doesGhostRequireAction(ghost))
        	{
        		int currentIndex=game.getGhostCurrentNodeIndex(ghost);
        		
        		//if ghosts are all in close proximity and not near Ms Pac-Man, disperse
        		if(isCrowded(game) && !closeToMsPacMan(game,currentIndex))
        			myMoves.put(ghost,getRetreatActions(game,ghost));                          				//go towards the power pill locations
        		//if edible or Ms Pac-Man is close to power pill, move away from Ms Pac-Man
        		else if(game.getGhostEdibleTime(ghost)>0 || closeToPower(game))
        			myMoves.put(ghost,game.getApproximateNextMoveAwayFromTarget(currentIndex,pacmanIndex,game.getGhostLastMoveMade(ghost),DM.PATH));      			//move away from ms pacman
        		//else go towards Ms Pac-Man
        		else        		
        			myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(currentIndex,pacmanIndex,game.getGhostLastMoveMade(ghost),DM.PATH));       			//go towards ms pacman
        	}
        }
        
        return myMoves;
    }

    /**
     * Close to power.
     *
     * @param game the game
     * @return true, if successful
     */
    private boolean closeToPower(Game game)
    {
    	int pacmanIndex=game.getPacmanCurrentNodeIndex();
    	int[] powerPillIndices=game.getActivePowerPillsIndices();
    	
    	for(int i=0;i<powerPillIndices.length;i++)
    		if(game.getShortestPathDistance(powerPillIndices[i],pacmanIndex)<PILL_PROXIMITY)
    			return true;

        return false;
    }

    /**
     * Close to ms pac man.
     *
     * @param game the game
     * @param location the location
     * @return true, if successful
     */
    private boolean closeToMsPacMan(Game game,int location)
    {
    	if(game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),location)<PACMAN_DISTANCE)
    		return true;

    	return false;
    }

    /**
     * Checks if is crowded.
     *
     * @param game the game
     * @return true, if is crowded
     */
    private boolean isCrowded(Game game)
    {
    	GHOST[] ghosts=GHOST.values();
        float distance=0;
        
        for (int i=0;i<ghosts.length-1;i++)
            for(int j=i+1;j<ghosts.length;j++)
                distance+=game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghosts[i]),game.getGhostCurrentNodeIndex(ghosts[j]));
        
        return (distance/6)<CROWDED_DISTANCE ? true : false;
    }

    /**
     * Gets the retreat actions.
     *
     * @param game the game
     * @param ghost the ghost
     * @return the retreat actions
     */
    private MOVE getRetreatActions(Game game,GHOST ghost)
    {
    	int currentIndex=game.getGhostCurrentNodeIndex(ghost);
    	int pacManIndex=game.getPacmanCurrentNodeIndex();
    	
        if(game.getGhostEdibleTime(ghost)==0 && game.getShortestPathDistance(currentIndex,pacManIndex)<PACMAN_DISTANCE)
            return game.getApproximateNextMoveTowardsTarget(currentIndex,pacManIndex,game.getGhostLastMoveMade(ghost),DM.PATH);
        else
            return game.getApproximateNextMoveTowardsTarget(currentIndex,game.getPowerPillIndices()[cornerAllocation.get(ghost)],game.getGhostLastMoveMade(ghost),DM.PATH);
    }
	
	private EnumMap<GHOST,MOVE> GhostsPlayout(Game game)
	{
		EnumMap<GHOST,MOVE> moves=new EnumMap<GHOST,MOVE>(GHOST.class);
		int[] pacman_path = game.getShortestPath(previous_pacman_pos, next_dest);
		for(GHOST ghost : GHOST.values())
		{
			if(game.doesGhostRequireAction(ghost))
			{
				MOVE m = null;
				int currentIndex=game.getGhostCurrentNodeIndex(ghost);
				int current_pacman_pos = game.getPacmanCurrentNodeIndex();
				MOVE[] poss = game.getPossibleMoves(currentIndex);
				
				// case 0, if random hits epsilon, random movement.
				if(random.nextFloat() < MCTSConstants.GHOST_EPSILON)
				{
					moves.put(ghost,poss[random.nextInt(poss.length)]);
				}
				else
				{
					if(!game.isGhostEdible(ghost)) //case 1 not-edible
					{
						if(game.getShortestPathDistance(currentIndex,current_pacman_pos)<= MCTSConstants.MIN_DIST_TO_PACMAN)
						{
							m = game.getApproximateNextMoveTowardsTarget(currentIndex,
									current_pacman_pos,
									game.getGhostLastMoveMade(ghost),
									DM.PATH);
//							continue;
						}
						
						if(m == null)
						{
							m = helper.JunctionConnectedToPath(game, currentIndex, pacman_path);
							
							if(m == null)
							{
								if(random.nextInt(100) > 50)//front
								{
									m = game.getApproximateNextMoveTowardsTarget(
											currentIndex, 
											helper.NextJunctionTowardMovement(current_pacman_pos,
																	game.getPacmanLastMoveMade()),
											game.getGhostLastMoveMade(ghost),
											DM.PATH);
	//								moves.put(ghost,
	//										game.getNextMoveTowardsTarget(
	//																				currentIndex, 
	//																				helper.NextJunctionTowardMovement(current_pacman_pos, game.getPacmanLastMoveMade()),
	//																				DM.PATH));
								}
								else //back
								{
									m = game.getApproximateNextMoveTowardsTarget(
											currentIndex, 
											helper.NextJunctionTowardMovement(current_pacman_pos, 
																	game.getPacmanLastMoveMade().opposite()),
											game.getGhostLastMoveMade(ghost),
											DM.PATH);
	//								moves.put(ghost,
	//										game.getNextMoveTowardsTarget(
	//																				currentIndex, 
	//																				helper.NextJunctionTowardMovement(current_pacman_pos, game.getPacmanLastMoveMade().opposite()),
	//																				DM.PATH));
								}
							}
						}
					}
					else //escape
					{
						m = game.getApproximateNextMoveAwayFromTarget(currentIndex,current_pacman_pos, game.getGhostLastMoveMade(ghost),DM.PATH);
//						moves.put(ghost,game.getNextMoveAwayFromTarget(currentIndex,current_pacman_pos,DM.PATH));   
					} 
					
					//Case 3, no followers
					int d = helper.NextJunctionTowardMovement(currentIndex, m);
					
					if(m!=null && d != -1)
					{
						if(helper.GhostInThePathOfGhost(game, ghost, game.getShortestPath(currentIndex, d)))
						{
							m = poss[random.nextInt(poss.length)];
						}
					}
					else if(m == null)
					{
						m = poss[random.nextInt(poss.length)];
					}
					moves.put(ghost, m);
				}
			}
				
		}
		
		return moves;
	}
	
	private boolean ShouldFinish()
	{
		return false;
	}
	
	private boolean TreePhaseOver(Game st)
	{
		return (dead || ate_pp || ate_ghost ||  target_reached || st.getMazeIndex() != initial_maze);
	}
	
	private boolean DefaultPhaseOver(Game st)
	{
		if(st.wasPacManEaten())
		{
			dead = true;
		}
		
		return ((st.getCurrentLevelTime() - starting_time) > MCTSConstants.MAX_LEVEL_TIME ||
				st.wasPacManEaten() || 
				st.gameOver() ||
				target_reached ||
				st.getMazeIndex() != initial_maze);
	}
	
	private boolean Terminate() 
	{
		return (time_due - System.currentTimeMillis()) < MCTSConstants.TIME_THRESHOLD;
	}
	
}
