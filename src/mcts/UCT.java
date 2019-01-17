package mcts;

import java.util.Random;
import java.util.Stack;

import dataRecording.HelperExtendedGame;
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
 * @modified A. Alvarez
 */
public class UCT 
{
	
	//Object which will simulate the playouts and return a reward to backpropagate
	MCTSSimulation simulator;
	
	//Selected nodes to perform playout (tree phase & playout phase)
	Stack<MCTSNode> selected_nodes = new Stack<MCTSNode>();
	
	//General instance of the current game
	public Game game;
	private Random random = new Random();
	
	/*
	 * rootNode is the starting point of the present state
	 */
	public MCTSNode rootNode = null;
	
	/*
	 * currentNode refers to the node we work at every step
	 */
	MCTSNode currentNode;
	
	//max possible actions performed by a node.
	int action_range = 4;
	
	/*
	 * Exploration coefficient ~1.4
	 */
	private float C = (float) Math.sqrt(2);
	
	//Max time passed by the controller (i.e. currentMillis + 40).
	protected long timeDue;
	
	//Time left until we have to return a movement.
	protected long time_left;
	private MOVE[] allMoves=MOVE.values();
	
	//Current depth we are in by measuring distances of paths through nodess
	private int current_depth  =0;

	//Extended version of the game which calculate practical stuffs for the controller.
	public HelperExtendedGame helper;
	
	//FOR PACMAN REAL MOVEMENT
	public int target = 0;
	public int previous_target = 0;
	
	//FOR END GAME TACTIC
	public int end_target; //target to be reached by pacman
	
	//need to be set
	public int[] current_pacman_path = new int[0];
	private MOVE current_pacman_action= null;

	//Tactics which will drive the goal of pacman in the game
	public enum Tactics
	{
		GHOST,
		PILL,
		SURVIVE,
		ENDGAME
	};
	public Tactics tactic;
	
	/**
	 * Constructor
	 * Initialize the maze game
	 */
	public UCT()
	{
		tactic = Tactics.PILL;
		helper = new HelperExtendedGame();
		simulator = new MCTSSimulation();
	}
	
	/**
	 * Set current game state
	 * @param game
	 */
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
		
			//Reset variables
			end_target = -1;
			target = -1;
            current_pacman_path = new int[0];

            //if its not the first time we are running the algorithm this
            //will allow us to select initial tactic
			if(rootNode != null)
			{
				SelectTactic(game.copy());
			}

			 /*
             * Create root node with the present state
             */
            rootNode = new MCTSNode(game.copy(), action_range, action, game.getPacmanCurrentNodeIndex(), "ROOT NODE", allMoves[action]);
            rootNode.maxChild = CalculateChildrenAndActions(rootNode);
//            rootNode.maxChild = CalculateChildrenAndActionsWithReverse(rootNode);
            
            this.timeDue = timeDue;
            
            if(this.timeDue == -1)
            {
            	this.timeDue = System.currentTimeMillis() + 40;
            	timeDue = System.currentTimeMillis() + 40;
            }
            
            this.time_left = timeDue - System.currentTimeMillis();
            
            System.out.println(timeDue);
            System.out.println(time_left);

            /*
             * Apply UCT search inside computational budget limit (default=100 iterations) 
             * (and 40 ms from framework)
             */
            int iterations = 0;

            while(!Terminate(iterations))
            {
            	iterations++;
            	selected_nodes.clear();
            	TreePolicy();
            	FillList(); //values for simulation
            	//initialize default values of the simulator with calculated in this end.
            	simulator.init(selected_nodes, rootNode, helper, this.timeDue, end_target, tactic);
            	//simulation wwill return reward which will propagate through the tree
            	Backpropagate(simulator.Simulate());
            }

           
            currentNode = rootNode;
            
            //Iterate through the nodes to update their max values & then reselect tactic with new information
            IterateAllScores(rootNode);
            SelectTactic(rootNode.state); 
            
            /*
             * Get the action that directs to the best node
             */
            currentNode = BestFinalChild(0.0f);
            MOVE bestAction = currentNode.pacman_move;
            System.out.println(iterations);
            System.out.println("TIME LEFT: " + this.time_left);
            //values used by the controller to debug and perform reverse movements.
            target = currentNode.destination;
            int start = game.getNeighbour(game.getPacmanCurrentNodeIndex(), bestAction);
            current_pacman_path = game.getShortestPath(start, target);
            
            return bestAction;
	}
	
	//
	/**
	 * Debug of tree survival score from indicated node until leaf node.
	 * also used to debug changes in reward showed by colors
	 * 
	 * @param node from which we will debug
	 */
	public void DebugConsideredDestinations(MCTSNode node)
	{
		int counter = 0;
		System.out.println();
		System.out.println(node.name + " SURVIVE FULL SCORE: " + node.my_reward.survival_reward);
		System.out.println(node.name + " SURVIVE NORM SCORE: " + node.my_reward.norm_survival_reward);
		System.out.println(node.name + " SURVIVE MAX SCORE: " + node.my_reward.MAX_survival_reward);
		System.out.println(node.name + " VISIT COUNT: " + node.times_visited);
		
		for(MCTSNode child : node.children)
		{
			System.out.println();
			System.out.println("CHILD " + counter + " SURVIVE FULL SCORE: " + child.my_reward.survival_reward);
			System.out.println("CHILD " + counter + " SURVIVE NORM SCORE: " + child.my_reward.norm_survival_reward);
			System.out.println("CHILD " + counter + " SURVIVE MAX SCORE: " + child.my_reward.MAX_survival_reward);
			System.out.println("CHILD " + counter + " VISIT COUNT: " + child.times_visited);
			
//			float alpha = child.GetMaxValue(tactic);
//			float inversed_alpha = 1 - alpha;
//			Color r = Color.RED;
//			Color g = Color.GREEN;
//			
//			
//			float r_c = (r.getRed() * inversed_alpha + g.getRed() * alpha);
//			float g_c = (r.getGreen() * inversed_alpha + g.getGreen() * alpha);
//			float b_c = (r.getBlue() * inversed_alpha + g.getBlue() * alpha);
//			
//			Color result_color = new Color(r_c/255.0f, g_c/255.0f, b_c/255.0f);
////			Color col = new Color(node.GetMaxValue(tactic),1.0f,1.0f);
//			GameView.addPoints(game,result_color, child.destination);
			DebugConsideredDestinations(child);
			counter++;
		}

	}
	
	/**
	 * Fill the selected nodes to be simulated
	 */
	private void FillList()
	{
		MCTSNode m = currentNode;
		
		while(m != null && m.parent != null)
		{
			selected_nodes.push(m);
			m = m.parent;
		}
	}
	
	
	/**
	 * Recursive Depth-first search from the passed node until end
	 * To update the max values used to select tactics and nodes.
	 * @param node
	 */
	private void IterateAllScores(MCTSNode node)
	{
		int counter = 0;
		while(node.children.size() != counter)
		{
			IterateAllScores(node.children.get(counter));
			counter++;
		}
		
		node.UpdateReward();
	}
	
	/**
	 * Depending on the state of the game and the score in the root node
	 * We select a tactic
	 * @param game
	 */
	private void SelectTactic(Game game)
	{
//		• The Ghost score tactic is selected if edible ghosts is closer
//		to pacman than he's to stop being edible and the maximum survival rate is
//		above the threshold Tsurvival.
//		• The Pill score tactic is applied when Pac-Man is safe and
//		there are no edible ghosts in range, and the maximum
//		survival rate is above the threshold Tsurvival.
//		• The Survival tactic is used when the maximum survival
//		rate of the previous search was below the threshold,
//		Tsurvival.
		
		/**
		 * end tactic if we overpass a max time:
		 * 1. target = nearest edible ghost
		 * 2. target = nearest power pill
		 * 3. target = nearest pill
		 */
		if(game.getCurrentLevelTime() > MCTSConstants.MAX_MAZE_TIME)
		{
			tactic = Tactics.ENDGAME;
			end_target = helper.NearestEdibleGhost(game, MCTSConstants.PACMAN_RANGE);
			if(end_target  == -1)
			{
				end_target = helper.NearestPowerPill(game);
				
				if(end_target  == -1)
				{
					end_target  = helper.NearestPill(game);
//					System.out.println("GOING FOR NEAREST PILL @" + end_target);
				}
				else
				{
//					System.out.println("GOING FOR NEAREST PP @" + end_target);
				}
			}
			else
			{
//				System.out.println("GOING FOR GHOST @" + end_target);
			}
			
			
			return;
		}
		
		float survival_rate = rootNode.GetMaxValue(Tactics.SURVIVE);
		
		if(survival_rate < MCTSConstants.SURVIVAL_THRESHOLD)
		{
			tactic = Tactics.SURVIVE;
			return;
		}
		
		tactic = Tactics.PILL;
		
		for(GHOST ghost : GHOST.values())
		{
			if(game.isGhostEdible(ghost) && game.getGhostLairTime(ghost) == 0)
			{
				int distance=game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost));
				if(distance< game.getGhostEdibleTime(ghost))
				{
					tactic = Tactics.GHOST;
					break;
				}
				
			}
			
		}
		
	}
	
	/**
	 * Expand the nonterminal nodes with one available child. 
	 * Chose a node to expand with BestChild(C) method
	 */
	private void TreePolicy() {
		currentNode = rootNode;
		current_depth = currentNode.path_cost;
		Game st = currentNode.state.copy();
		
		while(!Terminate(0) && !DepthReached(0))
		{
			if(!currentNode.IsFullyExpanded())
			{
				if(Expand())
				{
					return;
				}
				else
				{
					continue;
				}
			}
			else if(!currentNode.children.isEmpty())
			{
				currentNode = BestChild(C);
				current_depth += currentNode.path_cost;
			}
			else
			{
				return;
			}
			
			st = currentNode.state.copy();
		}
	}
	
	/**
	 * Propagate the reward calculated in the simulation from the currentnode to the root.
	 * @param reward
	 */
	private void Backpropagate(MCTSReward reward)
	{
//		int tree_depth = simulator.GetMaxTreeDepth();

		while(currentNode != null)
		{
//			if(current_depth > tree_depth)
//			{
//				current_depth -= currentNode.path_cost;
//				currentNode= currentNode.parent;
//				continue;
//			}
//			
			currentNode.times_visited++;
			currentNode.IncreaseReward(reward);
			currentNode = currentNode.parent;
		}
	}
	
	/**
	 * Calculate possible untried actions a new node could have (not considering reverse movement).
	 * @param new_child 
	 * @return the amount of children we can have
	 */
	private int CalculateChildrenAndActions(MCTSNode new_child)
	{

		MOVE[] possible_moves = new_child.state.getPossibleMoves(new_child.destination, new_child.invalid_child_move);
		
		for(MOVE m : possible_moves)
		{
			new_child.untried_actions.add(m.ordinal());
		}

		return new_child.untried_actions.size();

	}
	
	/**
	 * Calculate possible untried actions a new node could have allowing reverse movement).
	 * @param new_child 
	 * @return the amount of children we can have
	 */
	private int CalculateChildrenAndActionsWithReverse(MCTSNode new_child)
	{
		MOVE[] possible_moves = new_child.state.getPossibleMoves(new_child.destination);
		
		for(MOVE m : possible_moves)
		{
			new_child.untried_actions.add(m.ordinal());
		}

		return new_child.untried_actions.size();

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
		
		//root couldn't expand 
		if(nt.children.isEmpty() && nt.equals(rootNode))
		{
			if(MCTSConstants.DEBUG)
			{
				System.out.println("THIS SHOULDN'T HAPPEN BUT IF IT DOES,");
				System.out.println("IS BECAUSE THE COST TO TRAVERSE THE CHILDRENS WAS BIGGER THAN THE THRESHOLD COST");
				System.out.println("OR BECAUSE ROOT DIDN'T HAD TIME TO CALCULATE");
			}
			
			return currentNode;
		}
		
		for(MCTSNode n : nt.children)
		{
			//if our children haven't been visit enough we select a random of them.
			if(n.times_visited < MCTSConstants.CHILD_VISITED_THRESHOLD)
			{
				bestChild = nt.GetRandomChild(random);
				break;
			}
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
	
	private MCTSNode BestFinalChild(float c) {
		MCTSNode nt = currentNode;
		MCTSNode bestChild = null;

		float best_one = -10000000.0f;
		
		//root couldn't expand 
		if(nt.children.isEmpty() && nt.equals(rootNode))
		{
			if(MCTSConstants.DEBUG)
			{
				System.out.println("THIS SHOULDN'T HAPPEN BUT IF IT DOES,");
				System.out.println("IS BECAUSE THE COST TO TRAVERSE THE CHILDRENS WAS BIGGER THAN THE THRESHOLD COST");
				System.out.println("OR BECAUSE ROOT DIDN'T HAD TIME TO CALCULATE");
			}
			
			return currentNode;
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
		float value = n.GetMaxValue(tactic);
		if(c > 0.0f)
		{
			if(parent.times_visited != 0 && n.times_visited != 0)
			{
				value += c * (Math.sqrt((Math.log((float)parent.times_visited)) / (float)n.times_visited));
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
	private boolean Expand() {
		/*
		 * Choose untried action
		 */
		int action = UntriedAction(currentNode);
		current_pacman_action = allMoves[action];
		Game current_state = currentNode.state.copy();
		
		//we calculate the next position (junction) in which the agent will pe
		int destination = helper.NextJunctionTowardMovement(currentNode.destination, current_pacman_action);
		
		//starting position the agent will be after performing the untried action
		int start = current_state.getNeighbour(currentNode.destination, current_pacman_action);
		
		//distance from node to node
		int path_distance = current_state.getShortestPathDistance(start, destination);
		
		//As we are moving through junctions in the maze, the agent is most likely to hit intersections
		//which will change the ending move until destination; this just returns us, the correct last movement
		//so we can accurately calculate the new node untried actions without reverse.
		MOVE final_actual_move = current_state.getNextMoveTowardsTarget(destination, start,DM.PATH).opposite();	
		
		//if to reach that node overpass our limit we don't add it to the list and continue
		if(DepthReached(path_distance))
		{
			return false;
		}

		/*
		 * Create a child, set its fields and add it to currentNode.children
		 */
		MCTSNode child = new MCTSNode(current_state, action_range, action, destination, currentNode.name, final_actual_move);
		currentNode.children.add(child);
		child.parent = currentNode;
		child.maxChild = CalculateChildrenAndActions(child);
		
		//cost to go from parent to here.
		child.SetPathCost(path_distance);
		current_depth += path_distance;
		currentNode = child;
		
		return true;
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
	 * Check if the algorithm is to be terminated, e.g. reached number of iterations limit or time limit
	 * @param i
	 * @return
	 */
	private boolean Terminate(int i) 
	{
		this.time_left = this.timeDue - System.currentTimeMillis();
		System.out.println("time DUE: " + this.timeDue);
		System.out.println("current: " + System.currentTimeMillis());
		System.out.println(this.time_left);
		return (i>MCTSConstants.MAX_ITERATIONS) || this.time_left < MCTSConstants.TIME_THRESHOLD;
	}
	
	/***
	 * Check if we overpass our depth limit by expanding or selecting a child.
	 * @param extra_value
	 * @return
	 */
	private boolean DepthReached(int extra_value)
	{
		return (current_depth + extra_value) > MCTSConstants.MAX_DEPTH;
	}

}

