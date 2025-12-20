package com.reactive.service.macro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reactive.service.app.api.InMemoryWorkspace;
import com.reactive.service.parser.ENV_Manager;

public class MergeIncremental {
	public static final String baseMerge="Merge_Main_S";
	
	public static String findAndExpandMacro(String input) {
		String result="";
		String[] splited = input.split("\n");
		String remaining = "";
		for(String line:splited) {
			ArrayList output = expandMacro(line);
			result+=output.get(0);
			if(!output.get(1).equals("")) {
				remaining+=output.get(1)+"\n";
			};
			//result+=handleForLoop(line);
		}
		//add the remaining
		result+="\n"+remaining;
		return result;
	}
	public static ArrayList<String> expandMacro(String input) {
		String regex = " - (.*)\\s*=\\s*(__call)?\\s+Merge\\s+\\|\\s*(\\d+)\\s*\\((\\w+),\\s*(\\w+)\\s*\\)";
		Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        ArrayList<String> result = new ArrayList<String>();
        if (matcher.find()) {
        	for(int i =1;i<=matcher.groupCount();i++) {
        		System.out.println("group "+i+" : "+matcher.group(i));
        	}
        	String left = matcher.group(1);
        	Boolean isCall = (matcher.group(2)!=null)?true:false;
        	Integer lenght = Integer.parseInt(matcher.group(3).trim());
        	String lenghtName = matcher.group(4);
        	String arrayName = matcher.group(5);
        	result.add(" - "+left+"="+((isCall)?" __call ":"")+baseMerge+"_"+lenght+"("+lenghtName+","+arrayName+")"+"\n");
        	//result.add("");
        	
        	//init size 
        	
        	Integer currentSize = lenght;
        	String rootFolder = "macro";
        	try {
				String baseMacro = ENV_Manager.readFileToString(rootFolder + "/merge-base-incremental-template.yml");
				String continousMacro = ENV_Manager.readFileToString(rootFolder + "/merge-incremental-template.yml");
        	
        	
        	String output="";

    		Map<String, String> EnvVariables = new HashMap<String, String>();
    		for (String key : InMemoryWorkspace.environmentVariables.keySet()) {
				EnvVariables.put(key, InMemoryWorkspace.environmentVariables.get(key));
			}
        	while(currentSize>=1) {
        		Integer array_size = currentSize;
        		Integer double_array_size = array_size *2;
        		Integer divide_array_size = currentSize/2 + (currentSize%2);
        		currentSize =divide_array_size;
        		EnvVariables.put("array_size",array_size.toString());
        		EnvVariables.put("divide_array_size",divide_array_size.toString());
        		EnvVariables.put("double_array_size",double_array_size.toString());
        		
        		if (array_size==1) {
        			output+=ENV_Manager.replaceEnvVariablesInYaml(baseMacro,EnvVariables)+"\n";
        			break;
        		}else {
        			output+=ENV_Manager.replaceEnvVariablesInYaml(continousMacro,EnvVariables)+"\n";
        		}
        	}
        	result.add(output);
        	
        	} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	
        	//Map envVariables=
        	return result;
        }
        
        result.add(input+"\n");
        result.add("");
		return result;
	}
	
	public static void main(String args[]) {
		
		String input = " - abc = __call Merge | 7 (lenght, array )";
		System.out.println(findAndExpandMacro(input));
		
		
	}
	

}
