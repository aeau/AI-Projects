package mcts;

public class MCTSConstants 
{
	//ALL CONSTANTS USED IN MCTS
	public static final boolean DEBUG 					= false;
	public static final int 	MAX_ITERATIONS 			= 2000;
	public static final int 	MAX_LEVEL_TIME 			= 70;
	public static final int 	MAX_DEPTH				= 55;
	public static final int		CHILD_VISITED_THRESHOLD = 3; //--> check this one
	public static final int		TIME_THRESHOLD			= 2;
	public static final int		PACMAN_RANGE			= 40;
	public static final int		MIN_DIST_TO_PACMAN		= 10;
	public static final int		MAX_MAZE_TIME			= 1800;
	public static final float	GHOST_SCORE_THRESHOLD	= 0.4f;
	public static final float	SURVIVAL_THRESHOLD		= 0.6f;
	public static final float	GHOST_EPSILON			= 0.2f;
	public static int			TIMES					= 0;
	
}