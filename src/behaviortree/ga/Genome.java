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
	
	//genotype is represented as characters
	public ArrayList<Character> genotype 	= new ArrayList<Character>();
	public double fitness 					= 0.0;
	
	//adjusted parameters for creating the first population
	public int max_layer_depth 				= 3; //no more layers than this
	public int max_leaf_nodes 				= 4; //a composite node cannot have more than this amount of leaf nodes
	public int min_gen_size 				= 20;//minimum size of the gen based on handmade behavior tree
	public int max_gen_size 				= min_gen_size * 2;
	
	/**
	 * Gene values
	 * 1 = selector
	 * 2 = sequence
	 * 3 = GoTo action
	 * 4 = RunAway action
	 * 5 = GhostNearby action
	 * 6 = HasPowerPill action
	 * 7 = GetNearbyScaredGhost action
	 * 8 = PowerPillsAround action
	 * 9 = NearestPill action
	 */
	
	public Genome()
	{
	}
	
	public Genome(int min_size, int max_size, int max_layer, int max_leaf)
	{
		min_gen_size = min_size;
		max_gen_size = max_size;
		max_layer_depth = max_layer;
		max_leaf_nodes = max_leaf;
		
		//typical gen sequence:
		//gen = new char[]{'1','[','2','[','5','4',']','2','[','6','7','3',']','2','[','8','3',']','2','[','9','3',']',']'};
		//this will result in: 1[2[54]2[673]2[83]2[93]]
		
		
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
	
	/**
	 * Composition of genome in the first population
	 */
	public void InitGenome()
	{
		Random r 					= new Random();
		Stack<Integer> lc 			= new Stack<Integer>();
		int open_brackets 			= 0;
		int length 					= r.nextInt(min_gen_size) + max_gen_size;
		int composite_probability 	= 50;
		
		for(int i = 0; i < length; i++)
		{
			//if the treee have reach maximum depth or a composite node have enough leaf nodes, it is forced to close
			if(!lc.empty() && (lc.peek() >= max_leaf_nodes || lc.size() == max_layer_depth))
			{
				genotype.add(']');
				open_brackets--;
				lc.pop();
				if(lc.size() != max_layer_depth)
					composite_probability = 50;
				
				continue;
			}
			
			//depending on the probability, it will create a composite node or a leaf node
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
		}
		
		//Error checking if there is any open bracket to be close
		for(int i = 0; i < open_brackets; i++)
		{
			genotype.add(']');
		}
	}
	
	/**
	 * Crossover method to be applied to this newly generated genome, one point uniform
	 * @param p1 , a part of the first parent
	 * @param p2 , a part of the second parent
	 */
	public void GenerateGenome(List<Character> p1, List<Character> p2)
	{
		Stack<Object> brackets = new Stack<Object>();
		ArrayList<Integer> error_pos = new ArrayList<Integer>();
		
		for(char c : p1)
		{
			genotype.add(c);
		}
		
		//error checking, if the starting gene of the second parent is "[" or "]"
		//and the current genotype has thaat gene at the end, it just remove last gene in the genotype
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
		
		//Error checking brackets badly open or closed in resulting genotype
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
			System.out.println(PrintGenome()); //print invalid genomes
		}
	}
	
	/**
	 * TODO: add gene in a position
	 */
	public void AddGene()
	{
		
	}
	
	/**
	 * TODO: remove gene in a position
	 */
	public void RemoveGene()
	{
		
	}
	
	/**
	 * Format genotype to be printed in console
	 * @return genotype string
	 */
	public String PrintGenome()
	{
		StringBuilder sb = new StringBuilder();
		for(char c : genotype)
		{
			sb.append(c);
		}
		
		return sb.toString();
		
	}
	
	/**
	 * It creates a behavior tree from the current representation (genotype)
	 * @return the generated tree
	 */
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
			int a = g-48; //Give me the int value of a char 
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

	public double getFitness() {
		// TODO Auto-generated method stub
		return fitness;
	}
}
