package mcts;

import java.util.List;

import mcts.UCT.Tactics;

public class MCTSReward 
{
	public float pill_reward 			= 0.0f;
	public float ghost_reward 			= 0.0f;
	public float survival_reward 		= 0.0f;
	
	public float norm_pill_reward 		= 0.0f;
	public float norm_ghost_reward 		= 0.0f;
	public float norm_survival_reward 	= 0.0f;
	
	public float AVG_pill_reward 		= 0.0f;
	public float AVG_ghost_reward 		= 0.0f;
	public float AVG_survival_reward 	= 0.0f;
	
	public float MAX_pill_reward 		= 0.0f;
	public float MAX_ghost_reward 		= 0.0f;
	public float MAX_survival_reward 	= 0.0f;
	
	public MCTSReward()
	{
	}
	
	public MCTSReward(float _pill_reward, float _ghost_reward, float _survival_reward)
	{
		this.pill_reward 		= _pill_reward;
		this.ghost_reward 		= _ghost_reward;
		this.survival_reward 	= _survival_reward;
		AVG_pill_reward 		= pill_reward;
		AVG_ghost_reward 		= ghost_reward;
		AVG_survival_reward 	= survival_reward;
		MAX_pill_reward 		= pill_reward;
		MAX_ghost_reward 		= ghost_reward;
		MAX_survival_reward 	= survival_reward;
	}
	
	public void AddValues(MCTSReward other, List<MCTSNode> children, int times_visited)
	{
		this.pill_reward += other.pill_reward;
		this.ghost_reward += other.ghost_reward;
		this.survival_reward += other.survival_reward;
		
		this.norm_pill_reward = this.pill_reward/times_visited;
		this.norm_ghost_reward = this.ghost_reward/times_visited;
		this.norm_survival_reward = this.survival_reward/times_visited;
		
		if(children.isEmpty())
		{
			AVG_pill_reward 		= pill_reward;
			AVG_ghost_reward 		= ghost_reward;
			AVG_survival_reward 	= survival_reward;
			MAX_pill_reward 		= norm_pill_reward;
			MAX_ghost_reward 		= norm_ghost_reward;
			MAX_survival_reward 	= norm_survival_reward;
		}
		else
		{
			CalculateAvgFromChildren(children, times_visited);
			CalculateMaxFromChildren(children);
		}
	}
	
	public void CalculateAvgFromChildren(List<MCTSNode> children, int times_visited)
	{
		if(!children.isEmpty())
		{
			AVG_pill_reward = 0;
			AVG_ghost_reward = 0;
			AVG_survival_reward = 0;
			
			for(MCTSNode child : children)
			{
				AVG_pill_reward 	+= child.my_reward.norm_pill_reward;
				AVG_ghost_reward 	+= child.my_reward.norm_ghost_reward;
				AVG_survival_reward += child.my_reward.norm_survival_reward;
				
//				System.out.println("CHILD AVG PILL REWARD: " + child.my_reward.AVG_pill_reward);
//				System.out.println("CHILD AVG GHOST REWARD: " + child.my_reward.AVG_ghost_reward);
//				System.out.println("CHILD AVG SURVIVAL REWARD: " + child.my_reward.AVG_survival_reward);
			}
			
			AVG_pill_reward 	/= children.size();
			AVG_ghost_reward 	/= children.size();
			AVG_survival_reward /= children.size();
		}
	}
	
	public void CalculateMaxFromChildren(List<MCTSNode> children)
	{
		float pr = -1000.0f;
		float gr = -1000.0f;
		float sr = -1000.0f;
		if(!children.isEmpty())
		{
			for(MCTSNode child : children)
			{
				if(child.my_reward.MAX_pill_reward * child.my_reward.MAX_survival_reward >= pr)
					pr = child.my_reward.MAX_pill_reward * child.my_reward.MAX_survival_reward;
				
				if(child.my_reward.MAX_ghost_reward * child.my_reward.MAX_survival_reward>= gr)
					gr = child.my_reward.MAX_ghost_reward* child.my_reward.MAX_survival_reward;
				
				if(child.my_reward.MAX_survival_reward >= sr)
					sr = child.my_reward.MAX_survival_reward;
			}
			
			MAX_pill_reward = pr;
			MAX_ghost_reward = gr;
			MAX_survival_reward = sr;
			
			// I THINK THIS IS GOOD ALREADY
		}
		else
		{
			MAX_pill_reward = norm_pill_reward;
			MAX_ghost_reward = norm_ghost_reward;
			MAX_survival_reward = norm_survival_reward;
		}

	}
}
