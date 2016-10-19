package pacman.controllers.examples;

import java.awt.Color;

import mcts.MCTSNode;
import mcts.UCT;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public class MCTSPacMan extends Controller<MOVE>
{
	public UCT 	mcts 			= new UCT();
	public int 	previous_action = 0;
	int 		next_dest 		= 0;
	int 		previous_pos	= 0;
	boolean 	reverse 		= false;
	
	public MCTSPacMan()
	{
		
	}
	
	@Override
	public MOVE getMove(Game game, long timeDue) 
	{
//		GameView.addPoints(game, Color.WHITE, 834);
//		GameView.addPoints(game, Color.WHITE, 348);
//		System.out.println("WTF");
		mcts.SetGame(game);
		MOVE move = MOVE.NEUTRAL;
		int pacman_pos = game.getPacmanCurrentNodeIndex();
		boolean just_ate = false;
//		System.out.println(mcts.tactic);
		//DEBUG
//		int ind = game.getJunctionIndices()[2];
//		int destination = mcts.helper.NextJunctionTowardMovement(ind, MOVE.UP);
//		int start = game.getNeighbour(ind, MOVE.UP);
//		int[] path = game.getShortestPath(start, destination);
////		MOVE final_actual_move = Game.getNextMoveTowardsTarget(destination, start,DM.PATH).opposite();
//		
//		GameView.addPoints(game, Color.ORANGE, path);
//		System.out.println(path.length);
//		System.out.println(game.getShortestPathDistance(start, destination));
//		if(mcts.end_target != -1)
//		{
//			GameView.addPoints(game, Color.WHITE, mcts.end_target);
//			System.out.println(mcts.end_target);
//		}
//		if(mcts.current_pacman_path.length != 0)
//		{
//			GameView.addPoints(game, Color.GREEN, mcts.current_pacman_path);
//		}
//		if(mcts.target != -1)
//		{
//			GameView.addPoints(game, Color.ORANGE, mcts.target);
//		}
//		
		try {
//			System.out.println(mcts.tactic);
			previous_pos = pacman_pos;
			move = mcts.runUCT(game.getPacmanLastMoveMade().ordinal(), timeDue);
			next_dest = mcts.target;
			reverse = false;
			return move;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println("I COULD PASS TO HERE WTF!!!!");
		if(game.wasPacManEaten() || game.getCurrentLevelTime() == 0)
		{
			try
			{
				previous_pos = pacman_pos;
				move = mcts.runUCT(previous_action, timeDue);
				next_dest = mcts.target;
//				GameView.addPoints(game,Color.BLUE, mcts.current_pacman_path);
				reverse = false;
				return move;
			}
			catch(Exception e)
			{
				
			}
		}
		
		
		if(mcts.helper.IsJunction(pacman_pos))
		{
			try {
//				System.out.println(mcts.tactic);
				previous_pos = pacman_pos;
				move = mcts.runUCT(game.getPacmanLastMoveMade().ordinal(), timeDue);
				next_dest = mcts.target;
				reverse = false;
				return move;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
//			mcts.current_pacman_path = game.getShortestPath(game.getPacmanCurrentNodeIndex(), mcts.target);
			
			int[] updated_path = game.getShortestPath(pacman_pos, next_dest); //--> error of 1297 index shit
			//Maybe is when changing level the index already changes
			
//			System.out.println("NEXT DESTINATION = " + next_dest);
//			System.out.println("FUCKING PACMAN POSITION = " + pacman_pos);
			GameView.addPoints(game, Color.GREEN, updated_path);
			//CHECK THIS SHIT
			if(!reverse)
			{
				//TAKE AWAY GHOSTARRIVEFIRST FROM HERE AND PLAYOUT --- not checking for pp
				if(!mcts.helper.IsPathSafePowerPill(game, updated_path)  && mcts.helper.WillGhostsArriveFirst(game, updated_path[updated_path.length - 1]))
				{
					reverse = true;
					next_dest = previous_pos;
					previous_pos = pacman_pos;
					return game.getPacmanLastMoveMade().opposite(); //TODO: maybe here it could improve
//					return game.getNextMoveTowardsTarget(pacman_pos, next_dest, DM.PATH);
				}
//				else
//				{
//					
//					int go = -1;
//					int min_dist = Integer.MAX_VALUE;
//					for(GHOST ghost : GHOST.values())
//					{
//						if(game.wasGhostEaten(ghost))
//						{
//							just_ate = true;
//						}
//						
//						if(game.isGhostEdible(ghost) && game.getGhostLairTime(ghost)==0)
//						{
//							just_ate = true;
//							int dist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost));
//							if(dist < min_dist)
//							{
//								go = game.getGhostCurrentNodeIndex(ghost);
//								min_dist = dist;
//							}
//						}
//					}
//					
//					if(game.wasPowerPillEaten() || just_ate)
//					{
//						if(go != -1)
//						{
//							mcts.target = go;
//							mcts.previous_target = game.getPacmanCurrentNodeIndex();
//							mcts.current_pacman_path = game.getShortestPath(mcts.previous_target, mcts.target);
//	//						reverse = true;
//						}
//					}
//				}
			}
			//move = MOVE.values()[previous_action];
			
		}
		
		move = game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())[0];
		previous_action = move.ordinal();
//		GameView.addPoints(game, Color.ORANGE, mcts.target);
		
		
//		GameView.addPoints(game,Color.CYAN, mcts.GetSelectedJuncs());
//		GameView.addPoints(game,Color.BLUE, mcts.current_pacman_path);
		
//		if(mcts.end_target != -1)
//		{
//			GameView.addPoints(game,Color.ORANGE, mcts.end_target);
//		}
		
		//GameView.addPoints(game, Color.ORANGE, mcts.current_pacman_path);
		
//		if(mcts.rootNode != null)
//		{
//			for(MCTSNode c : mcts.rootNode.children)
//			{
//				float a = c.reward;
//				
//				if(a > 1.0f)
//				{
//					a = 1.0f;
//				}
//				else if(a < 0.0f)
//				{
//					a = 0.0f;
//				}
//				Color col = new Color(a,0.5f,0.8f);
//				if(c.safe_path != null && mcts.helper.IsPathSafe(game, c.safe_path) )
//					GameView.addPoints(game,col, c.safe_path);
//			}
//		}
//		GameView.addPoints(game,Color.CYAN, mcts.helper.GetIntersections());
		// TODO Auto-generated method stub
		return move;
	}

}
