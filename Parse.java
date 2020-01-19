package CPS842;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Scanner;

public class Parse {
	//Term list
	
	//Doc list
	//Link list
	//metadata?
	
	//make temp string of entry
	//if status = 1, delete string
	//if status = 2, add string to new file
	
	public static void run() throws IOException {
		File file = new File("crawl_results.txt");
		FileWriter fw = new FileWriter("db_crawl.txt");
		PrintWriter pw = new PrintWriter(fw);
		String line = null;
		Scanner scan = new Scanner(file);
		String entry = ""; 
		Boolean stat = false; 
		Boolean macrumor_archive = false;
		Boolean wiki_article = false;
		Boolean duplicate = false;	
		int count = 0;

		
		
		while (scan.hasNextLine())
			{
				line = scan.nextLine();	
				if (line.isEmpty()) {
					count++;
					if (stat == true && wiki_article == false && macrumor_archive == false && duplicate == false && count%12==0) { pw.print(entry + "\n"); }
					
					macrumor_archive = false;
					wiki_article = false;
					duplicate = false;					
					entry = " "; 
					//entry += "\n" + line; 
				}
				else if (line.startsWith("status:"))
				{
						if (line.substring(8,9).equals("2")) {
								entry += "\n"+ line;
								stat = true;
						}
						else {
							stat = false;
						}
				}	
				else if (line.contains("wikipedia") || line.contains("wikimedia") || line.contains("creativecommons"))
				{
					wiki_article = true;
				}
				else if (line.startsWith("Title: MacRumors Archives"))
				{
					macrumor_archive = true;
				}
				else if (!line.startsWith("outlink") && !line.startsWith("inlink") && line.endsWith("/amp"))
				{
					duplicate = true;
				}
				else if (line.startsWith("metadata"))
				{
					
				}
				else {
					entry += "\n" + line;
				}
				
				
	
			}
		
		scan.close();
		pw.close();
		fw.close();
	}
	
	public static void main (String[] args) throws IOException
	{
		run(); 
	}

}
