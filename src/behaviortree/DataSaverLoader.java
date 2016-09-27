package behaviortree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DataSaverLoader {
public static final String DIRECTORY="my-data/";
	
	public DataSaverLoader() {
		// TODO Auto-generated constructor stub

	}
	
	public static boolean DoesFileExist(String csvFile)
	{
		File f = new File(DIRECTORY + csvFile);
		return f.exists();
	}
	
	public static String[][] readDataFile(String csvFile, String seperationChar, String nullValue, boolean skipHeaderRow) throws IOException
	{
		
		List<String[]> lines = new ArrayList<String[]>();
		BufferedReader bufRdr = new BufferedReader(new FileReader(new File(DIRECTORY + csvFile)));
		
		String line;
		
		while ((line = bufRdr.readLine()) != null) {
			
			if (skipHeaderRow) {
				skipHeaderRow = false;
				continue;
			}
			
			String[] arr = line.split(seperationChar); 
			
			for(int i = 0; i < arr.length; i++)
			{
				if(arr[i].equals(""))
				{
					arr[i] = nullValue;
				}				
			}
			
			lines.add(arr);			
		}
		
		String[][] ret = new String[lines.size()][];
		bufRdr.close();
		return lines.toArray(ret);
	}

	public static boolean saveFile(String fileName,String data,boolean append)
	{
        try 
        {
            FileOutputStream outS=new FileOutputStream(DIRECTORY+fileName,append);
            PrintWriter pw=new PrintWriter(outS);

            pw.println(data);
            pw.flush();
            outS.close();

        } 
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        
        return true;
	}
}
