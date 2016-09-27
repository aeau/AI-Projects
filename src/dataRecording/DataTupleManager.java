package dataRecording;

import java.util.ArrayList;

import pacman.game.util.*;

/**
 * This class uses the IO class in the PacMan framework to do the actual saving/loading of
 * training data.
 * @author andershh
 *
 */
public class DataTupleManager {
	
	private static String FileName = "trainingData.txt";
	
	public static void SavePacManData(DataTuple data)
	{
		IO.saveFile(FileName, data.getSaveString(), true);
	}
	
	public static DataTuple[] LoadPacManData()
	{
		String data = IO.loadFile(FileName);
		String[] dataLine = data.split("\n");
		DataTuple[] dataTuples = new DataTuple[dataLine.length];
		
		for(int i = 0; i < dataLine.length; i++)
		{
			dataTuples[i] = new DataTuple(dataLine[i]);
		}
		
		return dataTuples;
	}
	
	public static ArrayList<DataTuple> LoadPacManDataArrayList()
	{
		String data = IO.loadFile(FileName);
		String[] dataLine = data.split("\n");
		ArrayList<DataTuple> dataTuples = new ArrayList<DataTuple>();
		
		for(int i = 0; i < dataLine.length; i++)
		{
			dataTuples.add(new DataTuple(dataLine[i]));
		}
		
		return dataTuples;
	}
}
