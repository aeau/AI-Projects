package pacman.controllers.examples;

import java.util.EnumMap;
import java.util.Random;

import dataRecording.HelperExtendedGame;
import mcts.MCTSConstants;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class PlayoutGhosts extends Controller<EnumMap<GHOST,MOVE>> {

	Random random=new Random();
	EnumMap<GHOST,MOVE> moves=new EnumMap<GHOST,MOVE>(GHOST.class);
	HelperExtendedGame helper = new HelperExtendedGame();
	
	@Override
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
		
		helper.SetState(game);
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
							m = game.getNextMoveTowardsTarget(currentIndex,current_pacman_pos,DM.PATH);
//							continue;
						}
						
						if(m == null)
						{
							if(random.nextInt(100) > 50)//front
							{
								m = game.getNextMoveTowardsTarget(
										currentIndex, 
										helper.NextJunctionTowardMovement(current_pacman_pos, game.getPacmanLastMoveMade()),
										DM.PATH);
//								moves.put(ghost,
//										game.getNextMoveTowardsTarget(
//																				currentIndex, 
//																				helper.NextJunctionTowardMovement(current_pacman_pos, game.getPacmanLastMoveMade()),
//																				DM.PATH));
							}
							else //back
							{
								m = game.getNextMoveTowardsTarget(
										currentIndex, 
										helper.NextJunctionTowardMovement(current_pacman_pos, game.getPacmanLastMoveMade().opposite()),
										DM.PATH);
//								moves.put(ghost,
//										game.getNextMoveTowardsTarget(
//																				currentIndex, 
//																				helper.NextJunctionTowardMovement(current_pacman_pos, game.getPacmanLastMoveMade().opposite()),
//																				DM.PATH));
							}
						}
					}
					else //escape
					{
						m = game.getNextMoveAwayFromTarget(currentIndex,current_pacman_pos,DM.PATH);
//						moves.put(ghost,game.getNextMoveAwayFromTarget(currentIndex,current_pacman_pos,DM.PATH));   
					} 
					
					//Case 3, no followers
					int d = helper.NextJunctionTowardMovement(currentIndex, m);
					if(helper.GhostInThePathOfGhost(game, ghost, game.getShortestPath(currentIndex, d)))
					{
						m = poss[random.nextInt(poss.length)];
					}

					moves.put(ghost, m);
				}
			}
				
		}
		
		return moves;
	}

}
