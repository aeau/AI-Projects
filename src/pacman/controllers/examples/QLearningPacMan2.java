package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import reinforcementlearning.QPacmanState;
import reinforcementlearning.QTable;

public class QLearningPacMan2 extends Controller<MOVE>{

	QTable q_table = null;
	int pacman_lives = 3;
	MOVE next_move;
	
	public QLearningPacMan2(QTable q)
	{
		q_table = q;
	}
	
	public boolean GoalReached(int current_pos)
	{
		return current_pos == q_table.target;
	}
	
	@Override
	public MOVE getMove(Game game, long timeDue) {
		// TODO Auto-generated method stub
		
		int pacman_pos = game.getPacmanCurrentNodeIndex();

		if(GoalReached(pacman_pos) || game.wasPacManEaten())
		{
			q_table.target = -1;
		}
				
		if(q_table.target == -1)
		{
			QPacmanState ps = new QPacmanState(game, game.getPacmanLastMoveMade());
			//String info = game.getGameState();
			int[] juncs = ps.ClosestKJunctions(game, q_table.actionRange, q_table.previous_target);
			q_table.previous_target = game.getPacmanCurrentNodeIndex();
			q_table.target = juncs[q_table.getNextAction(ps.toString())];
		}
		
		//QPacmanState ps = new QPacmanState(game, game.getPacmanLastMoveMade());
		//String ps = game.getGameState();
		
		//Current state --- we compute value in table for the next move
		//next_move = MOVE.values()[q_table.getNextAction(ps.toString())];
		next_move = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), q_table.target, DM.PATH);
//		
//		 for(GHOST ghost : GHOST.values())
//		{
//        	if(game.getGhostLairTime(ghost) == 0 && game.getGhostEdibleTime(ghost)==0)
//        	{
//	        	int dist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),
//						game.getGhostCurrentNodeIndex(ghost));
//	        	
//				if(dist<=25)
//				{
//					q_table.target = -1;
//				}
//        	}
//		}
		
		return next_move;
	}

}
