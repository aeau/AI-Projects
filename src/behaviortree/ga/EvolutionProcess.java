package behaviortree.ga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import behaviortree.DataSaverLoader;
import pacman.Executor;
import pacman.controllers.examples.BehaviorTreePacMan;
import pacman.controllers.examples.StarterGhosts;

public class EvolutionProcess 
{
	public ArrayList<Genome> population = new ArrayList<Genome>();
	public Genome champion;
	private int generation;
	private int max_generation = 500;
	private int population_size;
	public ArrayList<String> results = new ArrayList<String>();
	
	public static boolean SAVE_DATA = true;
	static String SEPARATION_CHARACTER=";";
    static String FILENAME = "result.csv";
	
	public EvolutionProcess(int population_size)
	{
		this.population_size = population_size;
		for(int i = 0; i < population_size; ++i)
		{
			Genome g = new Genome();
			g.InitGenome();
			population.add(g);
		}
	}
	
	public void InitPopulation()
	{
		
	}
	
	public void EvaluateIndividuals()
	{
		//First we evaluate for score: weighted sum between score and small and power pills
		//Second we evaluate for surviving longer.
		Executor exec=new Executor();
		
		for(Genome g : population)
		{
			ArrayList<Double> res = exec.runExperimentEvolution(new BehaviorTreePacMan(g.Deserialize()),new StarterGhosts(),10);
			double fitness = res.get(0) + (0.5 * res.get(1)) + res.get(2) + (0.25 * res.get(3));
			//res[0] = avg_score
			//res[1] = avg_small_pills
			//res[2] = avg_power_pills
			//res[3] = avg_time
			
			g.fitness = fitness;
		}

		
	}
	
	public void SaveChampion(Genome champ)
	{
		champion = champ;
	}
	
	public void SelectIndividuals()
	{
		//Select by tournament
		Random rand = new Random();
    	Genome[] parents = new Genome[2];
    	ArrayList<Genome> new_generation = new ArrayList<Genome>();;
    	
    	while(new_generation.size() < population_size)
    	{
    		for(int i = 0; i < 2; i++)
    		{
        		Genome first_fighter = population.get(rand.nextInt(population_size));
        		Genome second_fighter = population.get(rand.nextInt(population_size));
    			
        		parents[i] = first_fighter.getFitness() <= second_fighter.getFitness() ? first_fighter : second_fighter;
    		}
    		
    		Genome[] offspring = Reproduction(parents);
    		
    		for(int j = 0; j < offspring.length; j++)
    		{
    			new_generation.add(offspring[j]);
    		}
    	}
    	
    	//Generational replacement just for the lulz
    	for(int i = 0; i < population_size; i++)
    	{
    		population.set(i, new_generation.get(i));
    	}
	}
	
	public Genome[] Reproduction(Genome[] parents)
	{
		//Linear crossover for starters
		Genome[] offsprings = new Genome[2];
		
		Random rnd = new Random();
		int perc = rnd.nextInt(90);
		int parent_1 = (parents[0].genotype.size() * perc)/100;
		int parent_2 = (parents[1].genotype.size() * perc)/100;
		
		Genome o1 = new Genome();
		o1.GenerateGenome(parents[0].genotype.subList(0, parent_1), parents[1].genotype.subList(parent_2, parents[1].genotype.size()));
		offsprings[0] = o1;
		
		Genome o2 = new Genome();
		o2.GenerateGenome(parents[1].genotype.subList(0, parent_2), parents[0].genotype.subList(parent_1, parents[0].genotype.size()));
		offsprings[1] = o2;
		
		return offsprings;
	}
	
	public void Replacement(ArrayList<Genome> offsprings)
	{
		//Elite + worst fit replacement
	}
	
	public void ExecuteEvolution()
	{
		InitPopulation();
		
		double avgFitness=0.f;
        double minFitness=Float.POSITIVE_INFINITY;
        double maxFitness=Float.NEGATIVE_INFINITY;
        String bestIndividual="";
        String worstIndividual="";
        Genome best = null;
		
        try
        {
			while(generation != max_generation)
			{
				EvaluateIndividuals();
				//Sort population by fitness -- lambda
				//population.sort((o1, o2) -> Double.compare(o1.fitness, o2.fitness));
				
				
				
				for(int i = 0; i < population.size(); i++){
	                double currFitness = population.get(i).getFitness();
	                avgFitness += currFitness;
	                if(currFitness < minFitness){
	                    minFitness = currFitness;
	                    worstIndividual = population.get(i).PrintGenome();
	                }
	                if(currFitness > maxFitness){
	                    maxFitness = currFitness;
	                    bestIndividual = population.get(i).PrintGenome();
	                    best = population.get(i);
	                }
	            }
				SaveChampion(best);
	            if(population.size()>0){ avgFitness = avgFitness/population.size(); }
	            String output = "Generation: " + generation;
	            output += "\t AvgFitness: " + avgFitness;
	            output += "\t MinFitness: " + minFitness + " (" + worstIndividual +")";
	            output += "\t MaxFitness: " + maxFitness + " (" + bestIndividual +")";
	            System.out.println(output);
	            
	            if(SAVE_DATA)
	            {
	            	String tuple = generation + SEPARATION_CHARACTER;
	            	tuple += avgFitness + SEPARATION_CHARACTER;
	            	tuple += minFitness + SEPARATION_CHARACTER;
	            	tuple += maxFitness + SEPARATION_CHARACTER;
	    			DataSaverLoader.saveFile(FILENAME, tuple, true);
	            }
	            
	            results.add(output);
				
	            SelectIndividuals();
				generation++;
			}
        }
        catch(Exception e)
        {
        	System.out.println("I DONT HAVE ENOUGH MINERALS");
        }
		
		
	}
}
