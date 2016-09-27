package behaviortree.ga;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import behaviortree.*;
import behaviortree.Selector;
import behaviortree.Sequence;
import behaviortree.Tree;
import behaviortree.actions.*;
import pacman.game.internal.Ghost;

public class Genome 
{
	//public String genotype; 
	//public char[] gen;
	public ArrayList<Character> genotype = new ArrayList<Character>();
	public double fitness = 0.0;
	public int max_layer_bredth = 3;
	public int max_leaf_nodes = 4;
	public int min_gen_size = 20;
	public int max_gen_size = 40;
	
	public Genome()
	{
	}
	
	public Genome(int min_size, int max_size, int max_layer, int max_leaf)
	{
		min_gen_size = min_size;
		max_gen_size = max_size;
		max_layer_bredth = max_layer;
		max_leaf_nodes = max_leaf;
		
		//gen = new char[]{'2','[','5','4',']','2','[','6','7','3',']','2','[','8','3',']','2','[','9','3',']'};
		
		/*
		switch(Character.getNumericValue(g))
		{
			case 1: //Selector
				my_nodes.push(new Selector(my_nodes.peek()));
				break;
			case 2://Sequence
				my_nodes.push(new Sequence(my_nodes.peek()));
				break;
			case 3: //GoTo
				my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new GoTo()));
				break;
			case 4: //RunAway
				my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new RunAway()));
				break;
			case 5: //GhostNearby
				my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new GhostNearby()));
				break;
			case 6: //HasPowerPill
				my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new HasPowerPill()));
				break;
			case 7: //GetNearbyScaredGhost
				my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new GetNearbyScaredGhost()));
				break;
			case 8: //PowerPillsAround
				my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new PowerPillsAround()));
				break;
			case 9: //NearestPill
				my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new NearestPill()));
				break;
		}	
		*/
	}
	
	public void InitGenome()
	{
		Random r = new Random();
		int open_brackets = 0;
		int length = r.nextInt(min_gen_size) + max_gen_size;
		Stack<Integer> lc = new Stack<Integer>();
		
		int leaf_probability = 50;
		int composite_probability = 50;
		
		//Open brackets + layer counter need to be fixed
		
		for(int i = 0; i < length; i++)
		{
			if(!lc.empty() && (lc.peek() >= max_leaf_nodes || lc.size() == max_layer_bredth))
			{
				genotype.add(']');
				open_brackets--;
				lc.pop();
				if(lc.size() != max_layer_bredth)
					composite_probability = 50;
				
				continue;
			}
			
			if(r.nextInt(100) <= composite_probability)
			{
				composite_probability = 30;
				int rand = r.nextInt(2) + 1;
				genotype.add((char)(rand + '0'));
				genotype.add('[');
				open_brackets++;
				lc.push(0);
			}
			else
			{
				int rand = r.nextInt(9-2) + 3;
				genotype.add((char)(rand + '0'));
				if(!lc.empty())
				{
					int c = lc.pop();
					c++;
					lc.push(c);
				}
				composite_probability += 5;
			}
			
			/*
			int rand = r.nextInt(9) + 1;
			
			if(rand == 1 || rand == 2)
			{
				gen.add((char)(rand + '0'));
				gen.add('[');
				open_brackets++;
				lc.push(0);
			}
			else
			{
				gen.add((char)(rand + '0'));
				if(!lc.empty())
				{
					int c = lc.pop();
					c++;
					lc.push(c);
				}
			}*/
		}
		
		for(int i = 0; i < open_brackets; i++)
		{
			genotype.add(']');
		}
	}
	
	public void GenerateGenome(List<Character> p1, List<Character> p2)
	{
		Stack<Object> brackets = new Stack<Object>();
		ArrayList<Integer> error_pos = new ArrayList<Integer>();
		
		for(char c : p1)
		{
			genotype.add(c);
		}
		
		//Some checks
		if(p2.get(0) == '[' && genotype.get(genotype.size() - 1) == '[')
		{
			genotype.remove(genotype.size() - 1);
		}
		else if(p2.get(0) == ']' && genotype.get(genotype.size() - 1) == ']')
		{
			genotype.remove(genotype.size() - 1);
		}

		for(char c : p2)
		{
			genotype.add(c);
		}
		
		for(int i = 0; i < genotype.size(); i++)
		{
			if(genotype.get(i) == '[')
			{
				brackets.push(new Object());
			}
			else if(genotype.get(i) == ']')
			{
				if(!brackets.isEmpty())
				{
					brackets.pop();
				}
				else
				{
					error_pos.add(i);
				}
					
			}
		}
		
		error_pos.sort((o1, o2) -> Integer.compare(o1, o2));
		
		try
		{
			
			//Finally add ending brackets if stack have items
			for(int i = 0; i < brackets.size(); i++)
			{
				genotype.add(']');
			}
			
			//Check for bracket error
			for(int error_index : error_pos)
			{
				genotype.remove(error_index);
			}
			
			
		}
		catch(Exception e)
		{
			System.out.println(PrintGenome());
		}
	}
	
	public void AddGene()
	{
		
	}
	
	public void RemoveGene()
	{
		
	}
	
	public void Serialize()
	{
		
	}
	
	public String PrintGenome()
	{
		String result = "";
		for(char c : genotype)
		{
			result += c;
		}
		
		return result;
		
	}
	
	public Tree Deserialize()
	{
		Stack<Node> my_nodes = new Stack<Node>();
		Node root = new Selector();
		my_nodes.push(root);
		
		Tree t = new Tree();
		
		for(char g : genotype)
		{
			if(g == '[')
			{
				
			}
			else if(g == ']')
			{
				Node n = my_nodes.pop();
				if(!my_nodes.isEmpty())
					my_nodes.peek().AddChild(n);
				else
					my_nodes.push(n);
			}
			int a = g-48;
			switch(a)
			{
				case 1: //Selector
					my_nodes.push(new Selector(my_nodes.peek()));
					break;
				case 2://Sequence
					my_nodes.push(new Sequence(my_nodes.peek()));
					break;
				case 3: //GoTo
					my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new GoTo()));
					break;
				case 4: //RunAway
					my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new RunAway()));
					break;
				case 5: //GhostNearby
					my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new GhostNearby()));
					break;
				case 6: //HasPowerPill
					my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new HasPowerPill()));
					break;
				case 7: //GetNearbyScaredGhost
					my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new GetNearbyScaredGhost()));
					break;
				case 8: //PowerPillsAround
					my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new PowerPillsAround()));
					break;
				case 9: //NearestPill
					my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new NearestPill()));
					break;
			}			
		}
		
		t.root = my_nodes.pop();
		return t;
	}
	
	public int ConvertCharToInt(char c)
	{
		return 0;
	}

	public double getFitness() {
		// TODO Auto-generated method stub
		return fitness;
	}
}
