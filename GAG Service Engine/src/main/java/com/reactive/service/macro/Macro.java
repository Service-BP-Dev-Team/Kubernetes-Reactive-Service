package com.reactive.service.macro;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reactive.service.util.FileWriting;

public class Macro {

	public static String handleForLoop(String input) {
		String regex =""+
	"\\s-\\s([a-zA-Z_][a-zA-Z_0-9]*)\\[([a-zA-Z_][a-zA-Z_0-9]*)/([a-zA-Z_][a-zA-Z_0-9]*)\\]"
	// first part above will match expression such as 'tab[i/sync]'
	+"\\s?=\\s?\\[\\s?(__call)?\\s?([a-zA-Z_][a-zA-Z_0-9]*)\\((([a-zA-Z_][a-zA-Z_0-9]*,)*([a-zA-Z_][a-zA-Z_0-9]*))\\)"+ 
	// second part above will match expression such as ' = [service (a,b1,c2) ' 
	" for ([a-zA-Z_][a-zA-Z_0-9]*),([a-zA-Z_][a-zA-Z_0-9]*),(\\d+)\\s?\\]";
	// this last part above match expression such as 'for i,inc,5]'
	// and example of expression that we want to match is 
	// tab[i/sync] = [service(a,b,c) for i,inc,10]
		
		String result=input+"\n";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
        	for(int i =1;i<=matcher.groupCount();i++) {
        		System.out.println("group "+i+" : "+matcher.group(i));
        	}
            String matchedString = matcher.group();
            String array = matcher.group(1);
            String index = matcher.group(2);
            String sync = matcher.group(3);
            String call = matcher.group(4);
            String service = matcher.group(5);
            String parameters=matcher.group(6);
            String parametersWithoutSync="";
            String[] parametersSplit = parameters.split(",");
            if(parametersSplit.length>1) {
            	for(int i=1;i<parametersSplit.length;i++) {
            		parametersWithoutSync+=","+parametersSplit[i];
            	}
            }
            String incFunction= matcher.group(10);
            String incRepeat= matcher.group(11);
            int incRepeatNumber = Integer.parseInt(incRepeat);
            //first line
            String translation=" - ("+array+"["+index+"],"+sync+"1)="+
            ((call!=null)?" __call ":"")+service+"("+sync+parametersWithoutSync+")"+"\n";
            String lastIndex=index;
            for(int i=2; i<=incRepeatNumber; i++) {
            	translation+=" - "+index+(i-1)+"="+incFunction+"("+lastIndex+")"+"\n";
            	translation+=" - ("+array+"["+index+(i-1)+"],"+sync+i+")="+
            			((call!=null)?" __call ":"") +service+"("+sync+(i-1)+parametersWithoutSync+")"+"\n";
            	lastIndex=index+(i-1);
            	
            }
            System.out.println(translation);
            return translation;
            // System.out.println("Matched string: " + matchedString);
        } else {
           // System.out.println("No match found.");
        }
		return result;
	}
	
	public static String expandAllMacro(String input) {
		String result="";
		String inputWithMerge = MergeIncremental.findAndExpandMacro(input);
		String[] splited = inputWithMerge.split("\n");
		for(String line:splited) {
			result+=handleForLoop(line);
		}
		//FileWriting.writeStringToFile(result,"./macro/log.yml");
		return result;
	}
	
	public static void main(String args[]) {
		String part1 = " - tab[i/sync]";
		String regex1 ="\\s-\\s([a-zA-Z_][a-zA-Z_0-9]*)\\[([a-zA-Z_][a-zA-Z_0-9]*)/([a-zA-Z_][a-zA-Z_0-9]*)\\]";
		String part2 = " = [ __call service(a,b,c)";
		String regex2 = "\\s?=\\s?\\[([a-zA-Z_][a-zA-Z_0-9]*)\\(([a-zA-Z_][a-zA-Z_0-9]*,)*([a-zA-Z_][a-zA-Z_0-9]*)\\)";
		String part3 = " for i,inc,10]";
		String regex3 =" for ([a-zA-Z_][a-zA-Z_0-9]*),([a-zA-Z_][a-zA-Z_0-9]*),(\\d+)\\s?\\]";
		//String regex3 = " for [a-zA-Z_][a-zA-Z_0-9]*,[a-zA-Z_][a-zA-Z_0-9]*,\\d+\\]";
		String input =part1+part2+part3;
		String completeInput =part1+part2+part3+"\n";
		System.out.println(input);
		handleForLoop(input);
		
		Pattern pattern1 = Pattern.compile(regex1);
        Matcher matcher1 = pattern1.matcher(part1);
        if (matcher1.find()) {
            String matchedString = matcher1.group();
            System.out.println("Matched string: " + matchedString);
        } else {
            System.out.println("No match found.");
        }
        
        Pattern pattern2 = Pattern.compile(regex2);
        Matcher matcher2 = pattern2.matcher(part2);
        if (matcher2.find()) {
            String matchedString = matcher2.group();
            System.out.println("Matched string: " + matchedString);
        } else {
            System.out.println("No match found.");
        }
        
        Pattern pattern3 = Pattern.compile(regex3);
        Matcher matcher3 = pattern3.matcher(part3);
        if (matcher3.find()) {
            String matchedString = matcher3.group();
            System.out.println("Matched string: " + matchedString);
        } else {
            System.out.println("No match found.");
        }
        String text = expandAllMacro(completeInput);
        System.out.println(text);
	}
}
