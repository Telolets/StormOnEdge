package external;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.util.MultiMap;
import core.Cloud;

public class ZoneGroupingConnector {
	
	final String CONF_ZoneGroupingInput = "geoScheduler.out-ZoneGrouping";
	
	MultiMap tasksByCloudName = new MultiMap();
	@SuppressWarnings("rawtypes")
	Map storm_conf;
	
	public ZoneGroupingConnector(Map conf){ 
		storm_conf = conf;
		
		String resultPath = storm_conf.get(CONF_ZoneGroupingInput).toString();
		if(resultPath == null || resultPath == "")
		{
			System.out.println("WARNING: file Path is invalid");
			System.out.println("Path: " + resultPath);
		}
	}
	
	public MultiMap readInfoFromStormConf()
	{
		try {
			String resultPath = storm_conf.get(CONF_ZoneGroupingInput).toString();
			
			FileReader supervisorTaskFile = new FileReader(resultPath);
		    BufferedReader textReader = new BufferedReader(supervisorTaskFile);
		    
		    String line = textReader.readLine();
		    while(line != null && !line.equals(""))
		    {
		    	//Format:
		    	//cloudA;1,2,3,4,5
		    	String[] pairString = line.split(";");
		    	String key = pairString[0];
		    	String[] taskString = pairString[1].split(",");
		    	
		    	for(int ii = 0; ii < taskString.length; ii++)
		    	{
		    		tasksByCloudName.add(key, new Integer(taskString[ii]));
		    	}
		    	
		    	line = textReader.readLine();
		    }
		    
		    textReader.close();
		    
		}catch(Exception e) { 
			System.out.println(e.getMessage());
			System.out.println("ERROR: No file provided!");
			return null;
		}
		
		return tasksByCloudName;
	}
	
	
	public void writeInfo(HashMap<String,Cloud> clouds) {
		
		for(Cloud c : clouds.values())
		{
			String cloudName = (String) c.getName() + ";";
			
			if(!c.getTasks().isEmpty())
			{
				for(Integer t : c.getTasks())
					tasksByCloudName.add(cloudName, t);	
			}
		}
		
		writeInfoToFile();
	}
	
	private void writeInfoToFile() {
		
		StringBuilder taskStringBuilder = new StringBuilder();   
		
		for(Object cloudName : tasksByCloudName.keySet())
		{

			@SuppressWarnings("unchecked")
			ArrayList<Integer> taskIDs = (ArrayList<Integer>)tasksByCloudName.get(cloudName);
			String cloudString = cloudName.toString();
			
			for(Integer t : taskIDs)
				cloudString = cloudString + t.toString() + ",";
			
			taskStringBuilder.append(cloudString.substring(0, cloudString.length()-1));
			taskStringBuilder.append("\n");
			
		}
		System.out.println(taskStringBuilder.toString());
		
		try {
			
			String resultPath = storm_conf.get(CONF_ZoneGroupingInput).toString();
			FileWriter writer = new FileWriter(resultPath, true);
			writer.write(taskStringBuilder.toString());
			writer.close();

		} catch(Exception e) { 
			System.out.println(e.getMessage());
			System.out.println("WARNING: No file provided for the ZoneGrouping!");
		}
	}
}