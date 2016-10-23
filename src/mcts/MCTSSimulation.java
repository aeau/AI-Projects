package mcts;

import static pacman.game.Constants.EDIBLE_TIME;
import static pacman.game.Constants.EDIBLE_TIME_REDUCTION;
import static pacman.game.Constants.LEVEL_RESET_REDUCTION;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Random;
import java.util.Stack;

import dataRecording.HelperExtendedGame;
import mcts.UCT.Tactics;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Tree phase & default phase
 * Also default policy
 * @author A. Alvarez
 *
 */
public class MCTSSimulation {
	
	//
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
	
	//Values for debug and ongoing enhacenment
	private Stack<MCTSNode> selected_nodes;
	private int		max_tree_depth		= -1;
	
	/**
	 * Values that will eventualyl stop the simulation
	 * in any of its stepss.
	 */
	private boolean dead 				= false;
	private boolean ate_pp 				= false;
	private boolean ate_ghost			= false;
	private boolean ilegal_power_pill 	= false;
	
	/**
	 * State of the game & state of last save state/junction
	 */
	private Game 	current_state 		= null;
	private Game 	last_visited_state 	= null;
	private int 	last_visited_junc 	= -1;
	
	
	/**
	 * Default policy valuess
	 *
	 */
	private Game 	playout_state 		= null; //we need special state for that simulation
	private float 	starting_time 		= 0.0f; //starting time of the simulation so we calculated wwhen we should teerminate
	private boolean reverse 			= false; //if we have done a reverse movement in the playout
	private boolean unsafe_move			= false; //if we have select an unsafe movement in a junction
	private MOVE 	current_pacman_act	= MOVE.NEUTRAL; //current pacman movement
	private int 	previous_pacman_pos = -1; //position of pacman at the moment of deciding where to go
	private int 	next_dest 			= -1; //next destination that should be reached by pacman
	
	//For end game tactic only
	/**
	 * For end game tactic only
	 */
	int 	target 						= -1; //target which we should go
	float 	previous_distance_to_target = -1f; //starting distance to the aforementioned target 
	float 	actual_distance_to_target 	= -1f; //ending distance to target
	boolean target_reached 				= false; //if in any point of the playout we reach the goal before finishing
	Tactics	current_tactic				= Tactics.PILL; //current tactic of the game
	
	/*
	 * values used by the Legacy2TheReckoning controller
	 */
	public static final int CROWDED_DISTANCE=30;
	public static final int PACMAN_DISTANCE=10;
    public static final int PILL_PROXIMITY=15;
    private final EnumMap<GHOST,MOVE> myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);
    private final EnumMap<GHOST,Integer> cornerAllocation=new EnumMap<GHOST,Integer>(GHOST.class);
	
	
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
	
	/**
	 * on going enhancement to only reward nodes which we actually reached insteaad of the whole list
	 * @return
	 */
	public int GetMaxTreeDepth()
	{
		return max_tree_depth;
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
		
		//we are using the end game tactic, special meaasures for it
		if(target != -1)
		{
			this.target = target;
			this.previous_distance_to_target = current_state.getShortestPathDistance(
												current_state.getPacmanCurrentNodeIndex(), 
												this.target);
		}
	}
	
	/**
	 * Reset all the values so we now we are in a fresh state
	 */
	private void ResetEverything()
	{
		max_tree_depth 		= -1;
		
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
		unsafe_move			= false;
		current_pacman_act	= MOVE.NEUTRAL; 
		previous_pacman_pos = -1;           
		helper 				 = null;
		
		target 						= -1;   
		previous_distance_to_target = -1f;  
		actual_distance_to_target 	= -1f;  
		target_reached 				= false;
		current_tactic				= null;
		
	}
	
	/**
	 * Update each value of the game as we do each step in the default policy
	 * values updaated here will be use for calculating reward or terminate state.
	 * @param st, current state
	 * @param previous_state, helper for certain calculations aas the previous edible time of the ghosts when we ate them
	 */
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
		
		if(this.current_tactic == Tactics.ENDGAME &&
				!target_reached &&
			st.getShortestPathDistance(st.getPacmanCurrentNodeIndex(), this.target) < 3)
		{
			target_reached = true;
		}
		
		previous_state = st.copy();
	}
	
	/**
	 * Update each value of the game as we do each step in the tree phase because here
	 * we change the last visited junction, only relevant for us.
	 * values updaated here will be use for calculating reward or terminate state.
	 * @param st, current state
	 * @param previous_state, helper for certain calculations aas the previous edible time of the ghosts when we ate them
	 */
	private void UpdateInfoTreePhase(Game st, Game previous_state)
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
		
		if(previous_state.isJunction(previous_state.getPacmanCurrentNodeIndex()) && !dead)
		{
			last_visited_junc = previous_state.getPacmanCurrentNodeIndex();
			last_visited_state = previous_state.copy();
		}
		
		if(this.current_tactic == Tactics.ENDGAME &&
				!target_reached &&
			st.getShortestPathDistance(st.getPacmanCurrentNodeIndex(), this.target) < 3)
		{
			target_reached = true;
		}
		
		previous_state = st.copy();
	}
	
	/**
	 * Main method which execute all steps in the simulation until finally return a reward to backpropagate
	 * @return reward to backpropagate
	 */
	public MCTSReward Simulate()
	{
		SetupLegacy2TheReckoning(); //We use the Legacy2TheReckoning ghost playout
		PerformTreePhase();
		return PerformPlayoutPhase();
	}
	
	/**
	 * The tree phase does a simultion from the root node through all
	 * the selected nodes done in the selection phase. We traverse each movement
	 * until reaching to the destination in which we will select move and destination
	 * of the next node, evaluating the path meanwhile is being traverse
	 */
	private void PerformTreePhase()
	{
		//Go through all the selected nodes.
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
			max_tree_depth += current.path_cost;
			next_dest = current.destination;
			previous_pacman_pos = pacman_pos;
			
			//we take the movement of the current node.
			current_state.advanceGame(current.pacman_move, getGhostMove(current_state));
			UpdateInfoTreePhase(current_state, previous_state);
			
			//if we reached the next index, we simulate all the movements until reaching target junction (i.e. destination)
			if(TreePhaseOver(current_state))
			{
				break outer;
			}
			
			int dest = current.destination;
			
			follow_path:
			while(pacman_pos != dest)
			{
				pacman_pos = current_state.getPacmanCurrentNodeIndex();
				MOVE next_pacman_move = current_state.getPossibleMoves(pacman_pos, current_state.getPacmanLastMoveMade())[0];
				
				//different types of setup
				//1. custom ghost playout
				//2. legacy2thereckoning playout
				//3. random ghost playout
//				current_state.advanceGame(next_pacman_move, GhostsPlayout(current_state));
				current_state.advanceGame(next_pacman_move, getGhostMove(current_state));
//				current_state.advanceGame(next_pacman_move, randomGhostMovement(current_state));
				
				UpdateInfoTreePhase(current_state, previous_state);
				if(TreePhaseOver(current_state))
				{
					break outer;
				}
			}
			
			if(!selected_nodes.empty())
				current = selected_nodes.pop();
			else
				current = null;

		}
		
		//if we died, we use the last visited "safe" junction to see if we can overcome
		//the problem in the playout phase.
		if((dead || ilegal_power_pill) && last_visited_state != null)
		{
			playout_state = last_visited_state.copy();
//			return;
		}

		//ongoing enhancement
		if((dead || ilegal_power_pill))
		{
			max_tree_depth -= current.path_cost;
		}

		//set the playout state to the state we reached.
		playout_state = current_state.copy();
	}

	/**
	 * Default policy of MCTS, it simulates from the last state reached in tree phase
	 * until one of the terminate conditions are met
	 * @return the reward use to backpropagate
	 */
	private MCTSReward PerformPlayoutPhase()
	{
		//reset variables used in the tree phase
		Game previous_state = playout_state.copy();
		next_dest 			= playout_state.getPacmanCurrentNodeIndex();
		starting_time 		= playout_state.getCurrentLevelTime();
		int pacman_pos 		= playout_state.getPacmanCurrentNodeIndex();
		boolean action_done = false;
		boolean first_time 	= true;
		dead 				= false;

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
			//First time executing the default policy, just being precocious 
			if(playout_state.wasPacManEaten() || playout_state.getCurrentLevelTime() - starting_time == 0 || first_time)
			{
				previous_pacman_pos = pacman_pos;
				current_pacman_act = PacmanPlayoutJunction(playout_state);
				next_dest = helper.NextJunctionTowardMovement(pacman_pos, current_pacman_act);
				reverse = false;
				action_done = true;
				first_time = false;
			}

			//when we're at a junction a different strategy is followed than in the path
			if(helper.IsJunction(pacman_pos))
			{
				previous_pacman_pos = pacman_pos; //we set our previous position
				current_pacman_act = PacmanPlayoutJunction(playout_state); //get next movement
				next_dest = helper.NextJunctionTowardMovement(pacman_pos, current_pacman_act); //set our new target 
				action_done = true;
				reverse = false; //reset the reverse used in the path strategy
			}
			
			//we're in the path, 1297 and more is an invalid index sometimes reached by the algorithm still researchign why
			else if(!reverse && next_dest <= 1296 && pacman_pos <= 1296 && next_dest != -1 )
			{
				//the path from the current position of the agent to the destination is updated for more precise calculaations
				int[] updated_path = playout_state.getShortestPath(pacman_pos, next_dest);
				int index = PacmanPlayoutPath(playout_state, updated_path); //we select a new destination for the agent to redirect
				
				//if we selected a destination we update all the values 
				if(index != -1)
				{
					previous_pacman_pos = pacman_pos;
					current_pacman_act = playout_state.getNextMoveTowardsTarget(pacman_pos, index, DM.PATH);
					next_dest = index;
					action_done = true;
				}
			}
			
			//if we didn't calculate any new action in the path we just continue traversing it.
			if(!action_done)
			{
				current_pacman_act = playout_state.getPossibleMoves(pacman_pos, playout_state.getPacmanLastMoveMade())[0];	
			}
			
			playout_state.advanceGame(current_pacman_act,getGhostMove(playout_state));	
//			playout_state.advanceGame(current_pacman_act, GhostsPlayout(playout_state));
//			playout_state.advanceGame(RandomPacmanMove(playout_state), randomGhostMovement(playout_state));
			UpdateInfo(playout_state, previous_state);
		}
		
		//calculate actual distance to end game target if necessary
		if(current_tactic == Tactics.ENDGAME && !target_reached && !dead)
		{
			actual_distance_to_target = playout_state.getShortestPathDistance(pacman_pos, target);
		}

		return CalculateReward();
	}
	
	/**
	 * Calculates the different scores used for selection in the algorithm based on preset rules.
	 * @return a reward object fill with the current reward to be propagated
	 */
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
		
		//ghost rewrd gets updated and normalize after eating any ghost
		if(ghost_multiplier > 0)
		{
			ghost_multiplier /= (EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,helper.maze_index%LEVEL_RESET_REDUCTION)) * 4.0f);
			ghost_score = ghost_multiplier;
		}
		
		if(ate_pp && current_tactic != Tactics.ENDGAME)
		{
			//if any power pill is consumed we ensure that we reach a certain ghost score because of it
			//huge reward if we do it good but way penalizing if not.
			if(ghost_score >= MCTSConstants.GHOST_SCORE_THRESHOLD) //MAYBE GHOST EATEN INSTEAD OF MULTIPLER //TODO:
			{
				pill_score += ghost_score;
			}
			else
			{
				pill_score = 0.0f;
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
	
	/**
	 * Possible destiny the agent must go if path gets dangerous (i.e. ghost in the way)
	 * or a more interesting reward have appear (edible ghost).
	 * @param st, current state of the game
	 * @param path, updated path of pacman
	 * @return
	 */
	public int PacmanPlayoutPath(Game st, int... path)
	{
		if(!reverse)
		{
			//if path is dangeours or a ghost will arrive first than us to destiny, we just go back to where we start
			if(unsafe_move && !helper.IsPathSafePowerPill(st, path) && helper.WillGhostsArriveFirst(st, path[path.length - 1]))
			{
				reverse = true;
				return previous_pacman_pos;
			}
		
			int go = -1;
			int min_dist = Integer.MAX_VALUE;
			GHOST selected_ghost = null;
			
			//we ate powe pill so check for edible ghostss in the way :D :D :D
			//only set reverse if we indeed went reverse. if not we are just setting a new destination for pacman
			if(st.wasPowerPillEaten()) 
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
							if(st.isGhostEdible(ghost_2) && st.getGhostLairTime(ghost_2)==0)
							{
								int dist = st.getShortestPathDistance(st.getPacmanCurrentNodeIndex(),st.getGhostCurrentNodeIndex(ghost_2));
								if(dist < min_dist)
								{
									selected_ghost = ghost_2;
									go = st.getGhostCurrentNodeIndex(ghost_2);
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
	
	/**
	 * Movement performed by pacman at junctions
	 * @param st, current game state
	 * @return next move to perform by pacman to reach desstiny
	 */
	public MOVE PacmanPlayoutJunction(Game st)
	{
		
		MOVE[] possibleMoves=st.getPossibleMoves(st.getPacmanCurrentNodeIndex(), st.getPacmanLastMoveMade());
		ArrayList<MOVE> safe_moves = new ArrayList<MOVE>();
		ArrayList<MOVE> moves = new ArrayList<MOVE>();
		MOVE selected_move = null;
		int path_pills = 0;
		unsafe_move = false;
		
		moves:
		for(MOVE move : possibleMoves)
		{
			//First we calculate if the movement will make the agent reach a junction
			int junc = helper.NextJunctionTowardMovement(st.getPacmanCurrentNodeIndex(), move);
			if(junc == -1)
				continue moves;
			
			int[] path = helper.GetPathFromMove(move);
			moves.add(move);
			
			//path is safe ? and no gost will arrive there ? we have a safe move
			if(helper.IsPathSafePowerPill(st, path) && !helper.WillGhostsArriveFirst(st, path[path.length - 1]))
			{
				//if there is an edible ghost in this safe path, take directly that move
				if(helper.EdibleGhostInPath(st, path))
				{
					return move;
				}
				
				safe_moves.add(move);
				
				int pi = helper.PillsInPath(st, path);
				
				//calculate possible pillss in the new safe path
				if(pi >= path_pills)
				{
					selected_move = move;
					path_pills = pi;
				}
			}
		}
		
		//if there was a path with pills we go there
		if(selected_move != null)
		{
			return selected_move;
		}
			
		//if there was not safe paths we take a random mvoement
		if(safe_moves.isEmpty())
		{
			unsafe_move = true;
			if(!moves.isEmpty())
				return moves.get(random.nextInt(moves.size()));
			else
			{
				return possibleMoves[0];
			}
		}
		
		//we return a random safe movement
		return safe_moves.get(random.nextInt(safe_moves.size()));
	}
	
	/**
	 * Ghost playout followwing a custom strategy
	 * @param game, current game state
	 * @return
	 */
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
						//if we are in range to pacman we take the next move to him
						if(game.getShortestPathDistance(currentIndex,current_pacman_pos)<= MCTSConstants.MIN_DIST_TO_PACMAN)
						{
							m = game.getApproximateNextMoveTowardsTarget(currentIndex,
																		current_pacman_pos,
																		game.getGhostLastMoveMade(ghost),
																		DM.PATH);
						}
						
						if(m == null)
						{
							//if the ghost is situated in a junction connected directly to the path pacman is currently traversing
							//we sselect that move
							m = helper.JunctionConnectedToPath(game, currentIndex, pacman_path);
							
							if(m == null)
							{
								//our movement is to the junction infront of pacman or the junction behind pacman, TRAP HIM!
								if(random.nextInt(100) > 50)//front
								{
									m = game.getApproximateNextMoveTowardsTarget(
																				currentIndex, 
																				helper.NextJunctionTowardMovement(current_pacman_pos,
																											game.getPacmanLastMoveMade()),
																				game.getGhostLastMoveMade(ghost),
																				DM.PATH);
								}
								else //back
								{
									m = game.getApproximateNextMoveTowardsTarget(
																				currentIndex, 
																				helper.NextJunctionTowardMovement(current_pacman_pos, 
																									game.getPacmanLastMoveMade().opposite()),
																				game.getGhostLastMoveMade(ghost),
																				DM.PATH);
								}
							}
						}
					}
					else //if we are edible, escape
					{
						m = game.getApproximateNextMoveAwayFromTarget(currentIndex,current_pacman_pos, game.getGhostLastMoveMade(ghost),DM.PATH);
					} 
					
					//Case 3, no followers
					int d = helper.NextJunctionTowardMovement(currentIndex, m);
					
					//if theres more ghosts in the path i'm about to take, get another one randomly
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
	
	/**
	 * Helper to just get a posssible random movement of pacman
	 * @param st, current state
	 * @return random pacman movement
	 */
	public MOVE RandomPacmanMove(Game st)
	{
		MOVE[] possibleMoves=st.getPossibleMoves(st.getPacmanCurrentNodeIndex(),st.getPacmanLastMoveMade());
		return possibleMoves[random.nextInt(possibleMoves.length)];
	}
	
	/**
	 * Helper to just get possible random movements of the ghosts
	 * @param st, current state
	 * @return random ghost movements
	 */
	public EnumMap<GHOST,MOVE> randomGhostMovement(Game st)
	{
		EnumMap<GHOST,MOVE> moves=new EnumMap<GHOST,MOVE>(GHOST.class);
		
		for(GHOST ghostType : GHOST.values())
		{
			MOVE[] poss = st.getPossibleMoves(st.getGhostCurrentNodeIndex(ghostType), st.getGhostLastMoveMade(ghostType));
			if(st.doesGhostRequireAction(ghostType))
				moves.put(ghostType,poss[random.nextInt(poss.length)]);
		}
		
		return moves;
	}

    
    /**
     * Setup the ghost
     */
    public void SetupLegacy2TheReckoning()
    {
    	cornerAllocation.put(GHOST.BLINKY,0);
    	cornerAllocation.put(GHOST.INKY,1);
    	cornerAllocation.put(GHOST.PINKY,2);
    	cornerAllocation.put(GHOST.SUE,3);
    }
    
   /**
    * We request a set of movements for the ghosts calculated by the controller provided by the framework Legacy2TheReckoning
    * @param game, actual game state
    * @return
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

    /**
     * The tree phase is over if we die, eat a power pill, eat a ghost, reach the end game target or pass to another maze.
     * also if we take a power pill when we were already under the effects of one of them. (overdoze)
     * @param st, current game state
     * @return
     */
	private boolean TreePhaseOver(Game st)
	{
		return (dead || ate_pp || ate_ghost ||  target_reached || st.getMazeIndex() != initial_maze);
	}
	
	/**
	 * Terminate conditionss for the default policy, pacman is eaten, it overpases the max playout time or the game is over
	 * @param st, actual game state
	 * @return
	 */
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
	
	/**
	 * if we reach the millisecond limit of the framework to return a movement we stop doing everything and return until
	 * the simulation reached.
	 * @return
	 */
	private boolean Terminate() 
	{
		return (time_due - System.currentTimeMillis()) < MCTSConstants.TIME_THRESHOLD;
	}
	
}
