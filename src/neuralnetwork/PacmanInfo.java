package neuralnetwork;

import java.util.ArrayList;

import pacman.game.Constants.MOVE;

public class PacmanInfo {

	public MOVE move;
	public ArrayList<Double> info = new ArrayList<Double>();
	public int move_position= 0;
	
	public PacmanInfo()
	{
		
	}

	public PacmanInfo(ArrayList<Double> inf, MOVE move)
	{
		for(double i : inf)
		{
			info.add(i);
		}
		
		this.move = move;
		move_position = this.move.ordinal();
		
		//0 - 3 = distance to ghosts
		//4 = distance to closest powerpill
	}
	
	public PacmanInfo(ArrayList<Double> inf, String move)
	{
		for(double i : inf)
		{
			info.add(i);
		}
		
		this.move = ConvertMovement(move);
		move_position = this.move.ordinal();
		
		//0 - 3 = distance to ghosts
		//4 = distance to closest powerpill
	}
	
	public PacmanInfo(String[] pis)
	{
		info.add(Double.parseDouble(pis[0]));
		info.add(Double.parseDouble(pis[1]));
		info.add(Double.parseDouble(pis[2]));
		info.add(Double.parseDouble(pis[3]));
		info.add(Double.parseDouble(pis[4]));
		info.add(Double.parseDouble(pis[5]));

		this.move = MOVE.NEUTRAL;
		move_position = this.move.ordinal();
		//0 - 3 = distance to ghosts
		//4 = distance to closest powerpill
		//5 = distance to closest normal pill
	}
	
	public MOVE ConvertMovement(String move)
	{
		if(move.equals("UP"))
		{
			return MOVE.UP;
		}
		else if(move.equals("DOWN"))
		{
			return MOVE.DOWN;
		}
		if(move.equals("RIGHT"))
		{
			return MOVE.RIGHT;
		}
		if(move.equals("LEFT"))
		{
			return MOVE.LEFT;
		}
		else
		{
			return MOVE.NEUTRAL;
		}
	}
}
