package mcts;
import static pacman.game.Constants.DELAY;
import static pacman.game.Constants.EDIBLE_TIME;
import static pacman.game.Constants.EDIBLE_TIME_REDUCTION;
import static pacman.game.Constants.LEVEL_RESET_REDUCTION;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dataRecording.HelperExtendedGame;
import pacman.controllers.Controller;
import pacman.controllers.examples.AggressiveGhosts;
import pacman.controllers.examples.RandomGhosts;
import pacman.controllers.examples.RandomPacMan;
import pacman.controllers.examples.StarterGhosts;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * Monte Carlo Tree Search algorithm implementing UCT method
 * Run main method to test
 * 
 * @author D.Vitonis
 * @modified A. Hartzen
 *
 */
public class UCT {
	
	//TODO: CHECK EVERY JUNCTION IN THE CONTROLLER
	//GIVE HUGE REWARD FOR EATING
	//I think problem of the controller is that it calculates
	//that after eat it won't be as good.
	
	
	private boolean DEBUG 					= false;
	private int 	MAX_LEVEL_TIME 			= 80;
	private int 	MAX_DEPTH				= 55;
	private int		CHILD_VISITED_THRESHOLD = 3;
	private float	GHOST_SCORE_THRESHOLD	= 0.5f;
	protected final int MAX_ITERATIONS 		= 300;
	
	
	/*
	 * Maze used to control the game
	 */
	public Game game;
	private Random random = new Random();
	
	private ArrayList<Integer> juncs = new ArrayList<Integer>();
	public int[] selected_juncs;
	
	/*
	 * rootNode is the starting point of the present state
	 */
	public MCTSNode rootNode;
	
	/*
	 * currentNode refers to the node we work at every step
	 */
	MCTSNode currentNode;
	
	int action_range = 4;
	
	/*
	 * Exploration coefficient
	 */
	private float C = (float) Math.sqrt(2);

	//Random ghosts
	//Random PACMAN
	public Controller<MOVE> randomPacman;
	public Controller<EnumMap<GHOST,MOVE>> randomGhost;
	
	//Internal values to calculate
	//terminal state and rewards
	protected int pacman_lives;
	protected int previous_score = 0;
	protected int previous_pills = 0;
	protected int previous_pp = 0;
	protected int previous_ghost_eaten = 0;
	protected float ghost_eaten = 0.0f;
	protected int starting_time = 0;
	protected float ghost_time_multiplier = 1.0f;
	protected long timeDue;
	protected long time_left;
	protected boolean died = false;
	protected boolean powerpill_eaten  = false;
	protected float ghost_divisor;
	protected float pills_eaten;
	protected int closest_ghost_dist;
	private MOVE[] allMoves=MOVE.values();
	private int current_depth  =0;
	private int child_depth = 0;

	public HelperExtendedGame helper;
	
	//FOR PACMAN REAL MOVEMENT
	public int target = 0;
	public int previous_target = 0;
	
	//FOR PACMAN MOVEMENT in playout
	private int past_selection = 0;
	private int current_selection = 0;
	private boolean reverse = false;
	
	
	//need to be set
	public int[] current_pacman_path = new int[0];
	private MOVE current_pacman_action= null;

	//Tactics
	enum Tactics
	{
		GHOST,
		PILL,
		SURVIVAL
	};
	
	protected Tactics tactic;
	
	/**
	 * Constructor
	 * Initialize the maze game
	 */
	public UCT()
	{
		tactic = Tactics.PILL;
		helper = new HelperExtendedGame();
		
//		randomPacman = new RandomPacMan();
//		randomGhost = new RandomGhosts();
		
//		randomPacman.update(game.copy(),System.currentTimeMillis()+DELAY);
//		randomGhost.update(game.copy(),System.currentTimeMillis()+DELAY);
//		game.advanceGame(randomPacman.getMove(),randomGhost.getMove());	
	}
	
	public void SetGame(Game game)
	{
		this.game = game;
		helper.SetState(game);
	}
	
	/**
	 * run the UCT search and find the optimal action for the root node state
	 * @return
	 * @throws InterruptedException
	 */
	public MOVE runUCT(int action, long timeDue) throws InterruptedException{
		
			juncs.clear();
		
            /*
             * Create root node with the present state
             */
            rootNode = new MCTSNode(game.copy(), action_range, action);
            rootNode.maxChild = CalculateChildrenAndActions(rootNode);

//            System.out.println("ROOT NODE MAX CHILDREN: " + rootNode.maxChild);
            
            this.timeDue = timeDue;
            this.time_left = timeDue - System.currentTimeMillis();
//            System.out.println("TIME LEFT AT BEGGINING: " + this.time_left);
            /*
             * Apply UCT search inside computational budget limit (default=100 iterations) 
             */
            int iterations = 0;
            while(!Terminate(iterations))
            {
            	iterations++;
            	
            	//Implement UCT algorithm here
            	TreePolicy();
            	Backpropagate(DefaultPolicy());
            }
            System.out.println(iterations);
            /*
             * Get the action that directs to the best node
             */
            currentNode = rootNode;
            //rootNode is the one we are working with 
            //and we apply the exploitation of it to find the child with the highest average reward
//            time_left = timeDue - System.currentTimeMillis();
//            System.out.println("TIME LEFT BEFORE ACTION SELECTED: " + time_left);
            
            MOVE bestAction = BestChild(0.0f).pacman_move;
            previous_target = this.game.getPacmanCurrentNodeIndex();
            target = currentNode.target_junction;
            current_pacman_path = game.getShortestPath(previous_target, target);
//            time_left = timeDue - System.currentTimeMillis();
//            System.out.println("TIME LEFT AFTER ACTION SELECTED: " + time_left);
            return bestAction;
	}
	
	/**
	 * Expand the nonterminal nodes with one available child. 
	 * Chose a node to expand with BestChild(C) method
	 */
	private void TreePolicy() {
		currentNode = rootNode;
		current_depth = currentNode.path_cost;
		
		Game st = currentNode.state.copy();
		pacman_lives = st.getPacmanNumberOfLivesRemaining();
		while(!TerminalState(st) && !Terminate(0) && !DepthReached(0))
		{
			if(!currentNode.IsFullyExpanded())
			{
				currentNode = Expand();
				return;
			}
			else
			{
				currentNode = BestChild(C);
				current_depth += currentNode.path_cost;
			}
			
			st = currentNode.state.copy();
			
		}
	}
	
	//Simulate game from our position to target junction/intersection
	//If we can traverse the path without any problem (i.e. die)
	//We return the position of the junction for the DefaultPolicy to move us
	//else the movement will start from the root position.
	//so we calculate the children of the node as the directions to the possible junctions
	//and the state we store in them is the state returned from this function.
	private Game ExecutePathToTarget(int junction_node, Game st)
	{
		//TODO: CHECK THIS CODE! PROBLEMS IN THE PATH OR MAYBE DEFAULT POLICY
		Game returning_state = st.copy();
		int[] path = returning_state.getShortestPath(st.getPacmanCurrentNodeIndex(), junction_node);
//		System.out.println("WE START THE PLAY");
		child_depth = path.length;
		target = junction_node;
		current_pacman_path = path;
		for(int p : path)
		{
			int pacman_position = returning_state.getPacmanCurrentNodeIndex();
			
			if(returning_state.wasPacManEaten())
			{
//				System.out.println("PACMAN DEAD in node: " + pacman_position);
//				System.out.println("OLD PACMAN POS: " + st.getPacmanCurrentNodeIndex());
//				this.juncs.remove(this.juncs.size() - 1);
//				this.juncs.add(junction_node);
				return st;
			}
			
			if(returning_state.wasPowerPillEaten())
			{
				return returning_state;
			}
			
			for(GHOST ghost : GHOST.values())
			{
				if(returning_state.wasGhostEaten(ghost))
				{
					return returning_state;
				}
			}
			
				
				
//			System.out.println("next position: " + p);
//			System.out.println("current_position: " + pacman_position);
			returning_state.advanceGame(returning_state.getNextMoveTowardsTarget(pacman_position, p,DM.PATH),GhostPlayout(returning_state));
		}
		
		return returning_state;
	}
	
	private int GetJunction(MOVE move, Game st)
	{
		
		int index = helper.NextJunctionORIntersectionTowardMovement(st.getPacmanCurrentNodeIndex(), move);
//		int index = helper.NextJunctionORIntersectionTowardMovement(st.getPacmanCurrentNodeIndex(), move);
		this.juncs.add(index);
		return index;
//		int next_index = st.getNeighbour(st.getPacmanCurrentNodeIndex(), move);
//		return ClosestJunction(st, st.getPacmanCurrentNodeIndex(), next_index);
		
	}
	
	public int[] GetSelectedJuncs()
	{
		selected_juncs = new int[juncs.size()];
		
		for(int i=0;i<selected_juncs.length;i++)
			selected_juncs[i]=juncs.get(i);
		
		return selected_juncs;
	}
	
	public int ClosestJunction(Game game, int previous_index, int actual_pos)
	{
		int[] juncs = game.getJunctionIndices();
		int selected_junc = -1;
		
		//System.out.println("PACMAN POS: " + pacm + "; PREVIOUS PACMAN POS: " + previous_index);
		for(int j = 0; j < 1; j++)
		{
			int min_dist = Integer.MAX_VALUE;
			int selected_index = -1;
			for(int index : juncs)
			{
				int dist = game.getShortestPathDistance(actual_pos, index);
				if(dist < min_dist && 
					index != actual_pos &&
					previous_index != index)
				{
					min_dist = dist;
					selected_index = index;
				}
			}
			
			selected_junc = selected_index;
		}
		
		this.juncs.add(selected_junc);
		return selected_junc;
	}
	
	/**
	 * Simulation of the game. Choose random actions up until the game is over (goal reached or dead)
	 * @return reward (1 for win, 0 for loss)
	 */
	private float DefaultPolicy() {
		Game st = currentNode.state.copy();
		Game previous_state = st;
		Game root_state = rootNode.state.copy();
		previous_score = root_state.getScore();
		previous_pills = root_state.getNumberOfActivePills();
		previous_pp = st.getNumberOfActivePowerPills();
		previous_ghost_eaten = st.getNumGhostsEaten();
		ghost_eaten = 0.0f;
		starting_time = root_state.getCurrentLevelTime();
		ghost_time_multiplier = 1.0f;
		pills_eaten = 0.0f;
		powerpill_eaten = false;
		HashMap<GHOST, Integer> edible_table = new HashMap<GHOST, Integer>();
		
		int gen = 0;
		
		//FOR CONTROLLER
		past_selection = 0;
		current_selection = 0;
		boolean first_time = true;
		

		while(!TerminalState(st) && !Terminate(0))
		{
//			randomPacman.update(st,this.time_left);
//			randomGhost.update(st,this.time_left);
			
			//Check ghost edible and time
			for(GHOST ghostType : GHOST.values())
			{
				if(st.wasGhostEaten(ghostType))
				{
					ghost_time_multiplier += previous_state.getGhostEdibleTime(ghostType);
					ghost_eaten += 1.0f;
//					ghost_time_multiplier += edible_table.get(ghostType);
//					previous_ghost_eaten++;
				}
			}
			previous_state = st.copy();
			gen++;
			
			//LETS HAVE SOME FUN PACMAN &&& GHOSTS :D;
			if(first_time)
			{
				first_time = false;
				past_selection = st.getPacmanCurrentNodeIndex();
				current_selection = PacmanPlayoutJunction(st);
			}
			else
			{
				//AT JUNCTION & INTERS
				if(helper.IsJunction(st.getPacmanCurrentNodeIndex()) ||
						helper.IsIntersection(st.getPacmanCurrentNodeIndex()))
				{
					past_selection = st.getPacmanCurrentNodeIndex();
					current_selection = PacmanPlayoutJunction(st);
					current_pacman_path = st.getShortestPath(past_selection, current_selection);
					reverse = false;
				}
				else //IN THE PATH
				{
					current_pacman_path = game.getShortestPath(game.getPacmanCurrentNodeIndex(), current_selection);
					int index = PacmanPlayoutPath(st, current_pacman_path);
					
					if(index != -1)
					{
						past_selection = st.getPacmanCurrentNodeIndex();
						current_selection = index;
						current_pacman_path = st.getShortestPath(past_selection, current_selection);
					}
				}
			}
			
			
			current_pacman_action = st.getNextMoveTowardsTarget(st.getPacmanCurrentNodeIndex(), current_selection, DM.PATH);
			
			
			st.advanceGame(current_pacman_action,GhostPlayout(st));	
			
			if(st.wasPillEaten()) pills_eaten+=1.0f;
			if(st.wasPowerPillEaten()) powerpill_eaten = true;
			
//			for(GHOST ghostType : GHOST.values())
//			{
//				if(st.isGhostEdible(ghostType))
//				{
//					edible_table.put(ghostType, st.getGhostEdibleTime(ghostType));
//					//System.out.println("EDIBLE SCORE OF " + ghostType + " IS: " + st.getGhostEdibleTime(ghostType));
//				}
//			}

//			if(TerminalState(st))
//			{
//				break;
//			}
//			System.out.println("starting time: " + starting_time);
//			System.out.println("current time: " + st.getCurrentLevelTime());
//			System.out.println("difference: " + (st.getCurrentLevelTime() - starting_time));
			
//			int action = RandomAction(st);
//			st = maze.getNextState(action, st);
//			int ghostAction = RandomGhostAction(st);
//			st = maze.getNextGhostState(ghostAction, st);
		}
//		if(maze.getReward(st) != 0.0f)
//		System.out.println("GOT REWARD: " + maze.getReward(st));	
		
		int min_dist = Integer.MAX_VALUE;
		for(GHOST ghost : GHOST.values())
		{
			int dist = previous_state.getShortestPathDistance(previous_state.getPacmanCurrentNodeIndex(),st.getGhostCurrentNodeIndex(ghost));
			if(dist < min_dist)
			{
				min_dist = dist;
				closest_ghost_dist = dist;
//				ghost_time_multiplier += edible_table.get(ghostType);
//				previous_ghost_eaten++;
			}
		}
		
//		System.out.println("HOW LONG: " + gen);
		died = st.wasPacManEaten();
		return GetReward(st);
	}
	
	public float GetReward(Game st)
	{
		float reward = 0.0f;
		float ghost_reward = 0.0f;
		float pill_reward = 0.0f;
//		previous_ghost_eaten = st.getNumGhostsEaten();
//		System.out.println("previous_ghost_eaten: " + previous_ghost_eaten);
		
		if(died)
		{
			return 0.0f;
		}
		
		if (previous_pills > 0) {
			//
			pill_reward = pills_eaten / (float)previous_pills;
		}
		
		if(ghost_eaten > 0)
		{
//			System.out.println("WTF IS THIS VALUE AT THE END ?: " + EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,helper.maze_index%LEVEL_RESET_REDUCTION)));
//			System.out.println("GHOST MULTIPLIER BEFORE NORMALIZATION: " + ghost_time_multiplier + ", GHOST EATEN BEFORE NORM: " + ghost_eaten);
			ghost_eaten /= 4.0f;
			ghost_time_multiplier /= (EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,helper.maze_index%LEVEL_RESET_REDUCTION)) * 4.0f);
//			System.out.println("GHOST MULTIPLIER AFTER NORMALIZATION: " + ghost_time_multiplier + ", GHOST EATEN AFTER NORM: " + ghost_eaten);
			ghost_reward = (ghost_eaten * ghost_time_multiplier);
		}
		System.out.println("GHOST SCORE: " + ghost_reward);
//		if (previous_pp > st.getNumberOfActivePowerPills()) 
		if(powerpill_eaten)
		{
			
			if(ghost_eaten >= GHOST_SCORE_THRESHOLD)
			{
				pill_reward += ghost_reward;
				System.out.println("THE REWARD IS TASTY");
			}
			else
			{
				pill_reward = 0.0f;
				ghost_reward = 0.0f;
				System.out.println("THIS IS HAPPENING BABY");
			}
		 	
//		 	System.out.println("GHOST EATEN: " + ghost_eaten + ", PREVIOUS PP: " + previous_pp + ", PILLS EATEN: " + pills_eaten);
//		 	System.out.println("GHOST REWARD: " + ghost_reward + ", PILL REWRD: " + pill_reward);
		}
			
//		if(ghost_eaten > 0)
//		{
//			
////			ghost_reward = ((float)(previous_ghost_eaten) / 4.0f)* ghost_time_multiplier;
//			ghost_reward = (ghost_eaten * ghost_time_multiplier);// - closest_ghost_dist;
////			ghost_reward += previous_ghost_eaten * 100.0f;
////			System.out.println("GHOST REWARD: " + ghost_reward);
//			if (ghost_eaten > 1) {
//				pill_reward += ghost_reward;
//			}
//			else if(previous_pp > st.getNumberOfActivePowerPills())
//			{
//				pill_reward = 0.0f;
//				ghost_reward = 0.0f;
////				ghost_reward = 0.0f;
//			}
//		}
//		 else if (previous_pp > st.getNumberOfActivePowerPills()) {
//			 	pill_reward = 0.0f;
//			}
		
		reward += pill_reward + ghost_reward;
//		System.out.println("FINAL REWARD: " + reward);
//		System.out.println();
		
////		 = (float)(previous_ghost_eaten)/ 4.0f;
//		
//		if(previous_pp != 0)
//		{
//			pill_reward = (float)(previous_pp - st.getNumberOfActivePowerPills()) / (float)previous_pp;
//		}
//		
//		
//		System.out.println("pill_reward: " + pill_reward);
//		System.out.println("ghost_reward: " + ghost_reward);
//		if(pill_reward > 0.0f)
//		{
//			if(ghost_reward < 0.2f)
//			{
//				pill_reward = 0.0f;
//			}
//			else
//			{
//				pill_reward += ((float)(previous_pills - st.getNumberOfActivePills()) / (float)previous_pills) + ghost_reward;
//			}
//			
//		}
//		else
//		{
//			pill_reward += (float)(previous_pills - st.getNumberOfActivePills()) / (float)previous_pills;
//		}
//		
//		
//		reward += pill_reward;
//		reward += ghost_reward * ghost_time_multiplier; //ghosts
		
//		System.out.println("FINAL REWARD: " + reward);
		
//		reward = st.getScore() - previous_score;
//		reward += (st.getNumGhostsEaten() - previous_ghost_eaten) * 200;
//		reward += (previous_pp - st.getNumberOfActivePowerPills()) * 50;
//		reward += (previous_pills - st.getNumberOfActivePills()) * 10;
		
		if(DEBUG)
		{
			System.out.println("NEW SCORE: " + st.getScore() + "; PREVIOUS SCORE: " + previous_score);
			System.out.println("NEW GHOST EATEN: " + st.getNumGhostsEaten() + "; PREVIOUS GHOST EATEN: " + previous_ghost_eaten);
			System.out.println("NEW PP: " + st.getNumberOfActivePowerPills() + "; PREVIOUS PP: " + previous_pp);
			System.out.println("NEW PILLS: " + st.getNumberOfActivePills() + "; PREVIOUS PILLS: " + previous_pills);
			System.out.println("REWARD : " + reward);
		}
		
		return reward;
	}

	/**
	 * Assign the received reward to every parent of the parent up to the rootNode
	 * Increase the visited count of every node included in backpropagation
	 * @param reward
	 */
	private void Backpropagate(float reward) 
	{
//		System.out.println("BEFORE BACKPROPAGATE: " + reward);
		while(currentNode != null)
		{
			currentNode.reward += reward;
//			reward = currentNode.reward;
			currentNode.times_visited++;
			currentNode = currentNode.parent;
		}
		
//		System.out.println("AFTER BACKPROPAGATE: " + reward);
//		System.out.println();
	}
	
	/**
	 * Check if the node is fully expanded
	 * @param nt
	 * @return
	 */
	private boolean FullyExpanded(MCTSNode nt) 
	{
		
		return false;
	}
	
	private int CalculateChildrenAndActions(MCTSNode n)
	{
		int children = 0;
		if(n.parent != null)
		{
			n.opposite_parent = n.parent.pacman_move.opposite().ordinal();
		}
		if(n.parent == null)
		{
			n.opposite_parent = n.pacman_move.opposite().ordinal();
		}
//		
		for (int i=0;i<n.maxChild;i++)
		{

			if (isValidMove(i, n))
			{
				children++;
				n.untried_actions.add(i);
			}
		}
		return children;
	}

	/**
	 * Check if the state is the end of the game
	 * @param state
	 * @return
	 */
	private boolean TerminalState(Game st)
	{
		return ((st.getCurrentLevelTime() - starting_time) > MAX_LEVEL_TIME ||st.wasPacManEaten() || st.gameOver());
		//return maze.isGoalReached(state) || maze.isAvatarDead(state);
	}

	/**
	 * Choose the best child according to the UCT value
	 * Assign it as a currentNode
	 * @param c Exploration coefficient
	 */
	private MCTSNode BestChild(float c) {
		MCTSNode nt = currentNode;
		MCTSNode bestChild = null;

		float best_one = -10000000.0f;
		
		if(nt.children.isEmpty() && nt.equals(rootNode))
		{
			System.out.println("HOW CAN I GO FOR CHILDS IF EMPTY!!!");
		}
		
		if(nt.times_visited > CHILD_VISITED_THRESHOLD)
		{
			for(MCTSNode n : nt.children)
			{
				float uct = UCTvalue(n, nt, c);
	//			float uct = n.reward;
				if(uct > best_one)
				{
					bestChild = n;
					best_one = uct;
				}
			}
		}
		else
		{
			bestChild = nt.GetRandomChild(random);
		}
		
		bestChild.parent = currentNode;
		currentNode = bestChild;
		return bestChild;
	}
	
	private MCTSNode FinalBestChild(float c) {
		MCTSNode nt = currentNode;
		MCTSNode bestChild = null;

		float best_one = -10000000.0f;
		
		if(nt.children.isEmpty() && nt.equals(rootNode))
		{
			System.out.println("HOW CAN I GO FOR CHILDS IF EMPTY!!!");
		}
		int counter = 0;
		for(MCTSNode n : nt.children)
		{
			float uct = UCTvalue(n, nt, c);
			System.out.println("CHILD NUMBER " + counter++ + " UCT VALUE: " + uct);
//			float uct = n.reward;
			if(uct > best_one)
			{
				bestChild = n;
				best_one = uct;
			}
		}
		
		bestChild.parent = currentNode;
		currentNode = bestChild;
		return bestChild;
	}

	/**
	 * Calculate UCT value for the best child choosing
	 * @param n child node of currentNode
	 * @param c Exploration coefficient
	 * @return
	 */
	private float UCTvalue(MCTSNode n, MCTSNode parent, float c) 
	{
		float value = n.reward;
		if(c > 0.0f)
		{
			if(parent.times_visited != 0 && n.times_visited != 0)
			{
				value += c * (Math.sqrt((2.0f * Math.log((float)parent.times_visited)) / (float)n.times_visited));
			}
			else
			{
				value = Float.MAX_VALUE;
			}
		}
		return value;
	}

	/**
	 * Expand the current node by adding new child to the currentNode
	 */
	private MCTSNode Expand() {
		/*
		 * Choose untried action
		 */
		int action = UntriedAction(currentNode);
		current_pacman_action = allMoves[action];
		Game current_state = currentNode.state.copy();
		current_state = ExecutePathToTarget(GetJunction(allMoves[action], current_state), current_state);
//		randomPacman.update(current_state.copy(),System.currentTimeMillis()+DELAY);
//		randomGhost.update(current_state.copy(),System.currentTimeMillis()+DELAY);
//		current_state.advanceGame(allMoves[action],randomGhostMovement(current_state));	
		
		/*
		 * Create a child, set its fields and add it to currentNode.children
		 */
		MCTSNode child = new MCTSNode(current_state, action_range, action);

		currentNode.children.add(child);
		child.parent = currentNode;
		child.maxChild = CalculateChildrenAndActions(child);
		child.SetPathCost(child_depth);
		child.SetPath(current_pacman_path);
		child.target_junction = target;
		return child;
		
	}
	
	//TODO: CHECK THE VALUES AFTER ISPATHSAFE
	public int PacmanPlayoutPath(Game st, int... path)
	{
		boolean just_ate = false;
		
		if(!reverse)
		{
			if(!helper.IsPathSafe(st, path) || helper.WillGhostsArriveFirst(st, current_selection))
			{
				reverse = true;
				return past_selection;
			}
			
			int go = 0;
			int min_dist = Integer.MAX_VALUE;
			for(GHOST ghost : GHOST.values())
			{
				if(st.wasGhostEaten(ghost))
				{
					just_ate = true;
				}
				
				if(st.isGhostEdible(ghost) && st.getGhostLairTime(ghost)==0)
				{
					int dist = st.getShortestPathDistance(st.getPacmanCurrentNodeIndex(),st.getGhostCurrentNodeIndex(ghost));
					if(dist < min_dist)
					{
						go = st.getGhostCurrentNodeIndex(ghost);
						min_dist = dist;
					}
				}
			}
			
			if(st.wasPowerPillEaten() || just_ate)
			{
				return go;
			}
			
			//reverse = true;
			
		}
		
		return -1;
	}
	
	
	//Return junction to move to
	public int PacmanPlayoutJunction(Game st)
	{
		//First we set the possible safe moves
		//i.e. • Has no non-edible ghost on it moving in Pac-Man’s direction.
//		• Next junction is safe, i.e. in any case Pac-Man will reach
//		the next junction before a non-edible ghost.
		//CHECK THIS FOR NOT CONSIDER REVERSE
		MOVE[] possibleMoves=st.getPossibleMoves(st.getPacmanCurrentNodeIndex());
		ArrayList<Integer> safe_moves = new ArrayList<Integer>();
		HashMap<Integer, Integer> move_junc = new HashMap<Integer,Integer>();
		int selected_move = -1;
		int path_pills = 0;
		int movements = 0;
		
		moves:
		for(MOVE move : possibleMoves)
		{
			int junc = helper.NextJunctionORIntersectionTowardMovement(st.getPacmanCurrentNodeIndex(), move);
			if(junc == -1)
				continue moves;
			
			int[] path = st.getShortestPath(st.getPacmanCurrentNodeIndex(), junc);
			move_junc.put(move.ordinal(), junc);
			movements++;
			
			if(helper.IsPathSafe(st, path) && !helper.WillGhostsArriveFirst(st, junc))
			{
				safe_moves.add(junc);
				
				int pi = helper.PillsInPath(st, path);
				
				if(pi >= path_pills)
				{
					selected_move = junc;
					path_pills = pi;
				}
			}
		}
		
		if(selected_move != -1)
			return selected_move;
		
		if(safe_moves.isEmpty())
		{
			int index = random.nextInt(movements);
			if(move_junc.get(index) != null)
				return move_junc.get(index);
			else
			{
				return helper.NextJunctionORIntersectionTowardMovement(st.getPacmanCurrentNodeIndex(), possibleMoves[0]);
			}
		}
		
		return safe_moves.get(random.nextInt(safe_moves.size()));
		
	}
	
	public MOVE RandomPacmanMove(Game st)
	{
		MOVE[] possibleMoves=st.getPossibleMoves(st.getPacmanCurrentNodeIndex(),st.getPacmanLastMoveMade());
		return possibleMoves[random.nextInt(possibleMoves.length)];
	}
	
	public MOVE StarterPacmanMove(Game st)
	{
		int current=st.getPacmanCurrentNodeIndex();

		//Strategy 1: if any non-edible ghost is too close (less than MIN_DISTANCE), run away
		for(GHOST ghost : GHOST.values())
			if(st.getGhostEdibleTime(ghost)==0 && st.getGhostLairTime(ghost)==0)
				if(st.getShortestPathDistance(current,st.getGhostCurrentNodeIndex(ghost))< 25)
					return st.getNextMoveAwayFromTarget(st.getPacmanCurrentNodeIndex(),st.getGhostCurrentNodeIndex(ghost),DM.PATH);
		
		//Strategy 2: find the nearest edible ghost and go after them 
		int minDistance=Integer.MAX_VALUE;
		GHOST minGhost=null;		
		
		for(GHOST ghost : GHOST.values())
			if(st.getGhostEdibleTime(ghost)>0)
			{
				int distance=st.getShortestPathDistance(current,st.getGhostCurrentNodeIndex(ghost));
				
				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=ghost;
				}
			}
		
		if(minGhost!=null)	//we found an edible ghost
			return st.getNextMoveTowardsTarget(st.getPacmanCurrentNodeIndex(),st.getGhostCurrentNodeIndex(minGhost),DM.PATH);
		
		//Strategy 3: go after the pills and power pills
		int[] pills=st.getPillIndices();
		int[] powerPills=st.getPowerPillIndices();		
		
		ArrayList<Integer> targets=new ArrayList<Integer>();
		
		for(int i=0;i<pills.length;i++)					//check which pills are available			
			if(st.isPillStillAvailable(i))
				targets.add(pills[i]);
		
		for(int i=0;i<powerPills.length;i++)			//check with power pills are available
			if(st.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);				
		
		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		
		//return the next direction once the closest target has been identified
		return st.getNextMoveTowardsTarget(current,st.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);
		
		
//		//Strategy 3: go after the pills and power pills
//		int[] pills=st.getPillIndices();
//		
//		ArrayList<Integer> targets=new ArrayList<Integer>();
//		
//		for(int i=0;i<pills.length;i++)					//check which pills are available			
//			if(st.isPillStillAvailable(i))
//				targets.add(pills[i]);	
//		
//		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
//		
//		for(int i=0;i<targetsArray.length;i++)
//			targetsArray[i]=targets.get(i);
//		
//		//return the next direction once the closest target has been identified
//		return st.getNextMoveTowardsTarget(current,st.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);
		
//		MOVE[] possibleMoves=game.getPossibleMoves(game.getPacmanCurrentNodeIndex(),game.getPacmanLastMoveMade());
		
//		int action = random.nextInt(action_range);
//		while (!isValidMove(action,st)){
//        	action = random.nextInt(action_range);
//        }
//        return allMoves[action];
	}
	
	public EnumMap<GHOST,MOVE> AggressiveGhostMovement(Game st)
	{
		EnumMap<GHOST,MOVE> moves=new EnumMap<GHOST,MOVE>(GHOST.class);
		
		for(GHOST ghost : GHOST.values())				//for each ghost
			if(st.doesGhostRequireAction(ghost))		//if it requires an action
			{
				if(random.nextFloat()<1.0f)	//approach/retreat from the current node that Ms Pac-Man is at
					moves.put(ghost,st.getApproximateNextMoveTowardsTarget(st.getGhostCurrentNodeIndex(ghost),
							st.getPacmanCurrentNodeIndex(),st.getGhostLastMoveMade(ghost),DM.PATH));
				else									//else take a random action
					moves.put(ghost,allMoves[random.nextInt(allMoves.length)]);
			}

		return moves;
	}
	
	public EnumMap<GHOST,MOVE> randomGhostMovement(Game st)
	{
		EnumMap<GHOST,MOVE> moves=new EnumMap<GHOST,MOVE>(GHOST.class);
		for(GHOST ghostType : GHOST.values())
		{
			if(st.doesGhostRequireAction(ghostType))
				moves.put(ghostType,allMoves[random.nextInt(allMoves.length)]);
		}
		
		return moves;
	}
	
	public EnumMap<GHOST, MOVE> GhostPlayout(Game st)
	{
		EnumMap<GHOST,MOVE> moves=new EnumMap<GHOST,MOVE>(GHOST.class);
		for(GHOST ghost : GHOST.values())
		{
			if(st.doesGhostRequireAction(ghost))
			{
				int currentIndex=st.getGhostCurrentNodeIndex(ghost);
				int current_pacman_pos = st.getPacmanCurrentNodeIndex();
				if(!st.isGhostEdible(ghost)) //case 1
				{
					if(st.getShortestPathDistance(current_pacman_pos,currentIndex)< 10)
					{
						moves.put(ghost,st.getApproximateNextMoveTowardsTarget(currentIndex,current_pacman_pos,st.getGhostLastMoveMade(ghost),DM.PATH));   
						continue;
					}
					
					MOVE m = helper.JunctionConnectedToPath(st, currentIndex, current_pacman_path);
					
					if(m != null)
					{
						moves.put(ghost, m);
					}
					else
					{
						if(random.nextInt(100) > 50)//front
						{
							moves.put(ghost,
									st.getApproximateNextMoveTowardsTarget(
																			currentIndex, 
																			helper.NextJunctionTowardMovement(current_pacman_pos, current_pacman_action),
																			st.getGhostLastMoveMade(ghost),DM.PATH));
						}
						else //back
						{
							moves.put(ghost,
									st.getApproximateNextMoveTowardsTarget(
																			currentIndex, 
																			helper.NextJunctionTowardMovement(current_pacman_pos, current_pacman_action.opposite()),
																			st.getGhostLastMoveMade(ghost),DM.PATH));
						}
					}

				}
				else //escape
				{
					moves.put(ghost,st.getApproximateNextMoveTowardsTarget(currentIndex,current_pacman_pos,st.getGhostLastMoveMade(ghost),DM.PATH));   
				} //missing case to don't have followers
			}
				
		}
		
		return moves;
	}

	/**
	 * Returns the first untried action of the node
	 * @param n
	 * @return
	 */
	private int UntriedAction(MCTSNode n) 
	{
		int selected_index = random.nextInt(n.untried_actions.size());
		int selected_action = 0;
		
		selected_action = n.untried_actions.get(selected_index);
		n.untried_actions.remove(selected_index);
		return selected_action;
	}

	/**
	 * Check if the algorithm is to be terminated, e.g. reached number of iterations limit
	 * @param i
	 * @return
	 */
	private boolean Terminate(int i) 
	{
//		int p = (int)this.time_left;
		this.time_left = this.timeDue - System.currentTimeMillis();
//		if(time_left < 5)
//		{
//			System.out.println("I WAS CALLED FROM: " + function + "; TIME LEFT: " + this.time_left + "; BEFORE CALCULATING: " + p);
//		}
		return (i>MAX_ITERATIONS) || this.time_left < 5;
	}
	
	private boolean DepthReached(int extra_value)
	{
		return (current_depth + extra_value) > MAX_DEPTH;
	}
	
	public boolean isValidMove(int action, MCTSNode n)
	{
		Game st = n.state;
		//Just checking if possible no for ghost
		return ((n.opposite_parent != action) && st.getNeighbour(st.getPacmanCurrentNodeIndex(), MOVE.values()[action]) != -1);
	}
	
	public boolean isValidMove(int action, Game st)
	{
		//Just checking if possible no for ghost
		return (st.getNeighbour(st.getPacmanCurrentNodeIndex(), MOVE.values()[action]) != -1);
	}
}

