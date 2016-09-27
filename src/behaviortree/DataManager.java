package behaviortree;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Stack;

import behaviortree.actions.*;
import neuralnetwork.PacmanInfo;
import reinforcementlearning.QTable;

public class DataManager {

	public static Tree LoadTree(String data_to_load)
	{
		//TODO: Check if file exists.
		Tree loaded_tree = new Tree();
		Stack<Node> my_nodes = new Stack<Node>();
		Node root = null;
		Node previous = null;
		
		try
		{
			String[][] data = DataSaverLoader.readDataFile(data_to_load,";", "-", false);
			
			
			//Class<?> clazz = Class.forName("behaviortree.actions.GoTo");
			//Constructor<?> constructor = clazz.getConstructor(String.class, Integer.class);
			//Object instance = constructor.newInstance("stringparam", 42);
			
			//Object action = clazz.newInstance();
			
			for(String[] branches : data)
			{
				
				if(branches[0].equals("}"))
				{
					if(my_nodes.peek() != root)
					{
						Node n = my_nodes.pop();
						my_nodes.peek().AddChild(n);
					}
				}
				else if(branches[0].equals("{"))
				{
					continue;
				}
				else if(branches[0].equals("Leaf"))
				{
					Class<?> action_class = Class.forName("behaviortree.actions." + branches[1]);
					Object action = action_class.newInstance();
					my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), action));
				}
				else
				{
					if(root == null)
					{
						Class<?> composite_class = Class.forName("behaviortree." + branches[0]);
						Object composite = composite_class.newInstance();
						root = (Node)composite;
						my_nodes.push(root);
					}
					else
					{
						Class<?> composite_class = Class.forName("behaviortree." + branches[0]);
						Constructor<?> constructor = composite_class.getConstructor(Object.class);
						Object composite = constructor.newInstance(my_nodes.peek());
						my_nodes.push((Node)composite);
					}
					
				}
				

				
				/*
				switch(branches[0])
				{
				case "Selector":
					if(root == null)
					{
						root = new Selector(null);
						my_nodes.push(root);
					}
					else
					{
						my_nodes.push(new Sequence(my_nodes.peek()));
					}
					
					break;
				case "Sequence":
					if(root == null)
					{
						root = new Sequence(null);
						my_nodes.push(root);
					}
					else
					{
						my_nodes.push(new Sequence(my_nodes.peek()));
					}
					
					break;
				case "Leaf":
					
					//Reflection for creating actions from text. (Have to match)
					Class<?> action_class = Class.forName("behaviortree.actions." + branches[1]);
					Object action = action_class.newInstance();
					my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), action));
					
					//Uncomment if you prefer switch cases
					/*
					switch(branches[1])
					{
					case "GhostNearby":
						my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new GhostNearby()));
						break;
					case "RunAway":
						my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new RunAway()));
						break;
					case "GoTo":
						my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new GoTo()));
						break;
					case "HasPowerPill":
						my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new HasPowerPill()));
						break;
					case "NearestPill":
						my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new NearestPill()));
						break;
					case "PowerPillsAround":
						my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new PowerPillsAround()));
						break;
					case "GetNearbyScaredGhost":
						my_nodes.peek().AddChild(new Leaf(my_nodes.peek(), new GetNearbyScaredGhost()));
						break;
					}
					
					break;
				case "{":
					break;
				case "}":
					if(my_nodes.peek() != root)
						root.AddChild(my_nodes.pop()); //Check this
					break;
				default:
					break;
				}*/
				
			}
		}
		catch(Exception e)
		{
			
		}
		
		
		loaded_tree.root = root;
		return loaded_tree;
		
	}
	
	public static ArrayList<PacmanInfo> LoadNeuralNetworkDataset(String data_to_load)
	{
		//TODO: Check if file exists.
		ArrayList<PacmanInfo> dataset = new ArrayList<PacmanInfo>();
		Tree loaded_tree = new Tree();
		Stack<Node> my_nodes = new Stack<Node>();
		Node root = null;
		Node previous = null;
				
		try
		{
			String[][] data = DataSaverLoader.readDataFile(data_to_load,";", "-", false);
			
			for(String[] tuple : data)
			{
				
				ArrayList<Double> inf = new ArrayList<Double>();
				inf.add(Double.parseDouble(tuple[0]));
				inf.add(Double.parseDouble(tuple[1]));
				inf.add(Double.parseDouble(tuple[2]));
				inf.add(Double.parseDouble(tuple[3]));
				inf.add(Double.parseDouble(tuple[4]));
				inf.add(Double.parseDouble(tuple[5]));
				
				dataset.add(new PacmanInfo(inf, tuple[6]));
				
			}
		}
		catch(Exception e)
		{
			
		}
		
		return dataset;
	}
	
	public static boolean SaveTree(Tree tree, String filename)
	{
		String to_save = tree.root.SaveFormat();
		
		return DataSaverLoader.saveFile(filename, to_save, false);
		
	}
	
	public static boolean SaveTree(Tree tree, String extra, String filename)
	{
		String to_save = tree.root.SaveFormat();
		to_save += extra;
		DataSaverLoader.saveFile(filename, to_save, false);
		return true;
	}
	
	public static boolean SaveQTable(QTable q, String filename)
	{
		String to_save = q.SaveFormat();
		return DataSaverLoader.saveFile(filename, to_save, false);
	}
	
	public static QTable LoadQTable (String data_to_load)
	{
		//TODO: Check if file exists.
		ArrayList<PacmanInfo> dataset = new ArrayList<PacmanInfo>();
		Tree loaded_tree = new Tree();
		Stack<Node> my_nodes = new Stack<Node>();
		Node root = null;
		Node previous = null;
		QTable q = new QTable(4);
		
		try
		{
			String[][] data = DataSaverLoader.readDataFile(data_to_load,";", "-", false);
			
			for(String[] line : data)
			{
				float[] values = new float[]{Float.parseFloat(line[1]),Float.parseFloat(line[2]),Float.parseFloat(line[3]),Float.parseFloat(line[4])};
				q.AddStateAndValues(line[0], values);
			}
			
		}
		catch(Exception e)
		{
			
		}
		return q;

	}
}
