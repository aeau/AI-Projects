package mcts;

public class MCTSConstants 
{
	//ALL CONSTANTS USED IN MCTS
	public static final boolean DEBUG 					= false; 	//Debug option for console printing
	public static final int 	MAX_ITERATIONS 			= 2000;		//Max numbers sof iterations before stopping, ensure the tree to stop eventually
	public static final int 	MAX_LEVEL_TIME 			= 70;		//maximum time the agent can expend in the default policy
	public static final int 	MAX_DEPTH				= 55;		//maxmium tree distance depth between root node and leaf node path.
	public static final int		CHILD_VISITED_THRESHOLD = 3; 		//minimum visits aa child need to have for UCT to be used (ensure exploration). 
	public static final int		TIME_THRESHOLD			= 5;		//Maximun time we can spend calculating the next movement for the agent
	public static final int		PACMAN_RANGE			= 40;		//How close is being paacman range
	public static final int		MIN_DIST_TO_PACMAN		= 10;		//minimum distance to pacman to be in range to kill by the ghosts
	public static final int		MAX_MAZE_TIME			= 1800;		//Maximum time before start asking for target by end game tactic
	public static final float	GHOST_SCORE_THRESHOLD	= 0.4f;		//Minimum ghost score the agent have to reach in order to validate power pill
	public static final float	SURVIVAL_THRESHOLD		= 0.6f;		//Minimum survival score needed to select another tactic except survive.
	public static final float	GHOST_EPSILON			= 0.2f;		//If random is less than epsilon the ghost will make a random movement in playout
	
}
