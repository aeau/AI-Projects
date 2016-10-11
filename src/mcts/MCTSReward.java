package mcts;

import java.util.List;

public class MCTSReward 
{
	public float pill_reward;
	public float ghost_reward;
	public float survival_reward;
	
	//MISSING AVG AND MAXIMUN OF CHILD
	public float AVG_pill_reward;
	public float AVG_ghost_reward;
	public float AVG_survival_reward;
	
	public float MAX_pill_reward;
	public float MAX_ghost_reward;
	public float MAX_survival_reward;
	
	public MCTSReward(float _pill_reward, float _ghost_reward, float _survival_reward)
	{
		this.pill_reward 		= _pill_reward;
		this.ghost_reward 		= _ghost_reward;
		this.survival_reward 	= _survival_reward;
	}
	
	public void CalculateAvgFromChildren(List<MCTSNode> children)
	{
		if(!children.isEmpty())
		{
			for(MCTSNode child : children)
			{
				AVG_pill_reward 	+= child.my_reward.pill_reward;
				AVG_ghost_reward 	+= child.my_reward.ghost_reward;
				AVG_survival_reward += child.my_reward.survival_reward;
			}
			
			AVG_pill_reward 	/= children.size();
			AVG_ghost_reward 	/= children.size();
			AVG_survival_reward /= children.size();
		}
		else
		{
			AVG_pill_reward 	= pill_reward;
			AVG_ghost_reward 	= ghost_reward;
			AVG_survival_reward = survival_reward;
		}
	}
}
