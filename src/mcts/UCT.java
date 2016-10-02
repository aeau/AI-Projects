package mcts;
import static pacman.game.Constants.DELAY;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import pacman.controllers.Controller;
import pacman.controllers.examples.AggressiveGhosts;
import pacman.controllers.examples.RandomGhosts;
import pacman.controllers.examples.RandomPacMan;
import pacman.controllers.examples.StarterGhosts;
import pacman.game.Game;
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

	private boolean DEBUG 			= false;
	private int 	MAX_LEVEL_TIME 	= 200;
	
	/*
	 * Maze used to control the game
	 */
	public Game game;
	private Random random = new Random();
	
	/*
	 * rootNode is the starting point of the present state
	 */
	MCTSNode rootNode;
	
	/*
	 * currentNode refers to the node we work at every step
	 */
	MCTSNode currentNode;
	
	int action_range = 4;
	
	/*
	 * Exploration coefficient
	 */
	private float C = (float) Math.sqrt(2);
	
	/*
	 * Computational limit
	 */
	protected final int maxIterations = 14;
	
	
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
	protected int starting_time = 0;
	protected int ghost_time_multiplier = 1;
	protected long timeDue;
	protected long time_left;
	private MOVE[] allMoves=MOVE.values();
	
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
		
//		randomPacman = new RandomPacMan();
//		randomGhost = new RandomGhosts();
		
//		randomPacman.update(game.copy(),System.currentTimeMillis()+DELAY);
//		randomGhost.update(game.copy(),System.currentTimeMillis()+DELAY);
//		game.advanceGame(randomPacman.getMove(),randomGhost.getMove());	
	}
	
	public void SetGame(Game game)
	{
		this.game = game;
	}
	
	/**
	 * run the UCT search and find the optimal action for the root node state
	 * @return
	 * @throws InterruptedException
	 */
	public MOVE runUCT(int action, long timeDue) throws InterruptedException{
		
            /*
             * Create root node with the present state
             */
            rootNode = new MCTSNode(game.copy(), action_range, action);
            rootNode.maxChild = CalculateChildrenAndActions(rootNode);
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
		
		Game st = currentNode.state.copy();
		pacman_lives = st.getPacmanNumberOfLivesRemaining();
		while(!TerminalState(st) && !Terminate(0))
		{
			
			if(!currentNode.IsFullyExpanded())
			{
				currentNode = Expand();
				return;
			}
			else
			{
				currentNode = BestChild(C);
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
		
		//TODO: CHECK THIS CODE! PROBLEMS IN THE PATH OR MAYBE DEFAUL POLICY
		Game returning_state = st.copy();
		int[] path = returning_state.getShortestPath(st.getPacmanCurrentNodeIndex(), junction_node);
		System.out.println("WE START THE PLAY");
		for(int p : path)
		{
			int pacman_position = returning_state.getPacmanCurrentNodeIndex();
			
			if(returning_state.wasPacManEaten())
			{
				return st;
			}
			System.out.println("next position: " + p);
			System.out.println("current_position: " + pacman_position);
			while(pacman_position != p)
			{
				returning_state.advanceGame(returning_state.getNextMoveTowardsTarget(pacman_position, p,DM.PATH),randomGhostMovement(returning_state));
				pacman_position = returning_state.getPacmanCurrentNodeIndex();
			}
			
			System.out.println("DO WE ARRIVE HERE ? ");
		}
		
		return returning_state;
	}
	
	private int GetJunction(MOVE move, Game st)
	{
		int next_index = st.getNeighbour(st.getPacmanCurrentNodeIndex(), move);
		return ClosestJunction(st, st.getPacmanCurrentNodeIndex(), next_index);
		
	}
	
	public int ClosestJunction(Game game, int previous_index, int actual_pos)
	{
		int[] juncs = game.getJunctionIndices();
		int selected_junc = -1;
		ArrayList<Integer> targetjuncs=new ArrayList<Integer>();
		int pacm = actual_pos;
		
		//System.out.println("PACMAN POS: " + pacm + "; PREVIOUS PACMAN POS: " + previous_index);
		for(int j = -1; j < 1; j++)
		{
			int min_dist = Integer.MAX_VALUE;
			int selected_index = -1;
			for(int index : juncs)
			{
				int dist = game.getShortestPathDistance(pacm, index);
				if(dist < min_dist && 
					!targetjuncs.contains(index) && 
					index != pacm &&
					previous_index != index)
				{
					min_dist = dist;
					selected_index = index;
				}
			}
			targetjuncs.add(selected_index);
			if(j >= 0)
				selected_junc = selected_index;
		}
		
		return selected_junc;
	}
	
	/**
	 * Simulation of the game. Choose random actions up until the game is over (goal reached or dead)
	 * @return reward (1 for win, 0 for loss)
	 */
	private float DefaultPolicy() {
		Game st = currentNode.state.copy();
		Game root_state = rootNode.state.copy();
		previous_score = root_state.getScore();
		previous_pills = root_state.getNumberOfActivePills();
		previous_pp = root_state.getNumberOfActivePowerPills();
		previous_ghost_eaten = 0;
		starting_time = st.getCurrentLevelTime();
		ghost_time_multiplier = 1;
		HashMap<GHOST, Integer> edible_table = new HashMap<GHOST, Integer>();
		
		int gen = 0;
		
		while(!TerminalState(st) && !Terminate(0))
		{
//			randomPacman.update(st,this.time_left);
//			randomGhost.update(st,this.time_left);
			
			//Check ghost edible and time
			for(GHOST ghostType : GHOST.values())
			{
				if(st.wasGhostEaten(ghostType) && edible_table.containsKey(ghostType))
				{
					ghost_time_multiplier += edible_table.get(ghostType);
					previous_ghost_eaten++;
				}
			}
			
			gen++;
			st.advanceGame(StarterPacmanMove(st),randomGhostMovement(st));	
			
			for(GHOST ghostType : GHOST.values())
			{
				if(st.isGhostEdible(ghostType))
				{
					edible_table.put(ghostType, st.getGhostEdibleTime(ghostType));
					//System.out.println("EDIBLE SCORE OF " + ghostType + " IS: " + st.getGhostEdibleTime(ghostType));
				}
			}
			
			if(ghost_time_multiplier != 1)
				System.out.println("GHOST EDIBLE TIME MULTIPLIER: " + ghost_time_multiplier);
			
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
		System.out.println("HOW LONG: " + gen);
		return GetReward(st);
	}
	
	public float GetReward(Game st)
	{
		float reward = 0.0f;
		
		float ghost_reward = (float)(previous_ghost_eaten)/ 4.0f;
		float pill_reward = (float)(previous_pp - st.getNumberOfActivePowerPills()) / (float)previous_pp;
		
		System.out.println("pill_reward: " + pill_reward);
		System.out.println("ghost_reward: " + ghost_reward);
		if(pill_reward > 0.0f)
		{
			if(ghost_reward < 0.2f)
			{
				pill_reward = 0.0f;
			}
			else
			{
				pill_reward += ((float)(previous_pills - st.getNumberOfActivePills()) / (float)previous_pills) + ghost_reward;
			}
			
		}
		else
		{
			pill_reward += (float)(previous_pills - st.getNumberOfActivePills()) / (float)previous_pills;
		}
		
		
		reward += pill_reward;
		reward += ghost_reward * ghost_time_multiplier; //ghosts
		
		System.out.println("FINAL REWARD: " + reward);
		
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
		
		for(MCTSNode n : nt.children)
		{
			float uct = UCTvalue(n, nt, c);
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
		float value = n.reward / (float)n.times_visited;
		if(c > 0.0f)
		{
			if(parent.times_visited != 0 && n.times_visited != 0)
			{
				value += Math.sqrt((2.0f * Math.log((float)parent.times_visited)) / (float)n.times_visited);
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
		
		Game current_state = currentNode.state.copy();
		current_state = ExecutePathToTarget(GetJunction(allMoves[action], current_state), current_state);
//		randomPacman.update(current_state.copy(),System.currentTimeMillis()+DELAY);
//		randomGhost.update(current_state.copy(),System.currentTimeMillis()+DELAY);
		//current_state.advanceGame(allMoves[action],randomGhostMovement(current_state));	
		
		/*
		 * Create a child, set its fields and add it to currentNode.children
		 */
		MCTSNode child = new MCTSNode(current_state, action_range, action);

		currentNode.children.add(child);
		child.parent = currentNode;
		child.maxChild = CalculateChildrenAndActions(child);
		return child;
		
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
	
	public EnumMap<GHOST,MOVE> randomGhostMovement(Game st)
	{
		EnumMap<GHOST,MOVE> moves=new EnumMap<GHOST,MOVE>(GHOST.class);
		
		for(GHOST ghostType : GHOST.values())
		{
			if(game.doesGhostRequireAction(ghostType))
				moves.put(ghostType,allMoves[random.nextInt(allMoves.length)]);
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
		return (i>maxIterations) || this.time_left < 5;
	}
	
	public boolean isValidMove(int action, MCTSNode n)
	{
		Game st = n.state;
		//Just checking if possible no for ghost
		return ((n.opposite_parent != action) || st.getNeighbour(st.getPacmanCurrentNodeIndex(), MOVE.values()[action]) != -1);
	}
	
	public boolean isValidMove(int action, Game st)
	{
		//Just checking if possible no for ghost
		return (st.getNeighbour(st.getPacmanCurrentNodeIndex(), MOVE.values()[action]) != -1);
	}
}

