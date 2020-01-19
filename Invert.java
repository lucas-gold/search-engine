package CPS842;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;


public class Invert {

	public static String[] stopWords = {"i", "a", "about", "an", "and", "", "are", "as", "at", "be", "by", "for", "from", "how", "in", "is", "it", "of", "on", "or", "that", "the", "this", "to", "was", "what", "when", "where", "who", "will", "with", "which", "m", "ve", "ll", "s", "d", "t", "b", "w", "q"};
	public List<String> termList;
	public List<List<String>> docList;
	public List<String> urlList = new ArrayList<String>();
	public List<Crawl> crawldb = new ArrayList<Crawl>(); 
	public String[] queryTerms;
	Crawl pages;

	public String build(String query) {

		query = query.toLowerCase();
		queryTerms = query.toLowerCase().split("[^A-Za-z0-9]+");
		Arrays.sort(queryTerms);

		Map<Integer, Double> docsim =  termFreq();
		return output(docsim);


	}
	public String output (Map<Integer, Double> docsim)
	{
		String s = "";
		int count = 0;

		for (Integer x : docsim.keySet())
		{
			count++;

			s += count + ": " + crawldb.get(x).getTitle() + "\n" + crawldb.get(x).getUrl() + "\n";

			if (crawldb.get(x).getContent() != null && crawldb.get(x).getContent().length() > 1600 && !crawldb.get(x).getContent().startsWith(" baseUrl"))
			{
				s+= crawldb.get(x).getContent().substring(1400,1600) + "...\n";
			}

			s += "\n";

			//if (count >= 50) {
			//	break;
			//}
		}
		return s;
	}

	public Map<Integer, Double> termFreq() {

		Map<String, Double> queryIDF = new HashMap<String,Double>(); 

		Map<String, Double> termIDF = new HashMap<String,Double>(); 

		Map<String, Double> termTF = new HashMap<>(); 

		Map<String, Double> queryTF = new HashMap<String,Double>(); 

		final double docs_amount = docList.size();

		ArrayList<Map<String,Double>> docsTFs = new ArrayList<>(); 

		//list to map
		Map<String, Double> termListMap = new HashMap<>(); 
		for (String s : termList)
		{
			termListMap.put(s, 0.0);
		}

		for (int i = 0; i < docList.size(); i++)
		{
			List<String> doc = docList.get(i);

			Map<String, Double> docTerms = countMatches(doc);

			termTF = new TreeMap<>(termListMap);

			for (String key : docTerms.keySet())
			{
				Double val = docTerms.get(key);
				if (val > 0.0) {val = 1 + Math.log10(val);}
				else {val = 0.0; }

				termTF.replace(key, val);

				Double doc_count = termIDF.get(key); 
				if (doc_count != null) {
					termIDF.replace(key, doc_count+1);
				}
				else {
					termIDF.put(key, 1.0);
				}

			}
			docsTFs.add(termTF);

			//times that terms appears in document (for tf)
			for (String term : queryTerms) { 
				if (docTerms.get(term) != null) {
					Double result = docTerms.get(term);
					if (result > 0.0) {result = 1 + Math.log10(result);}
					else {result = 0.0; }
					queryTF.put(term, result);

					//count number of docs with term in it (idf) 
					Double doc_count = queryIDF.get(term);
					if (doc_count != null) {
						queryIDF.replace(term, doc_count+1);
					}
					else {
						queryIDF.put(term, 1.0);
					}
				}

			}	

		}


		for (String key : termIDF.keySet())
		{
			Double val = termIDF.get(key);
			termIDF.replace(key, Math.log10(docs_amount/val));
		}

		for (String key : queryIDF.keySet())
		{
			Double val = queryIDF.get(key);
			queryIDF.replace(key, Math.log10(docs_amount/val));
		}

		for (int x = 0; x < docsTFs.size(); x++)
		{
			for (String key : docsTFs.get(x).keySet())
			{
				Double idf = termIDF.get(key); 
				Double tf = docsTFs.get(x).get(key);
				if (idf != null)
				{
					docsTFs.get(x).replace(key, idf * tf); 
				}

			}
		}

		Map<String, Double> queryWeightVec = new TreeMap<>(queryTF); 

		for (String s : queryIDF.keySet())
		{
			Double tf = queryTF.get(s);
			Double idf = queryIDF.get(s);
			queryWeightVec.replace(s, tf * idf);
		}
		for (String s : termList)
		{
			queryWeightVec.putIfAbsent(s, 0.0);
		}
		
		Double queryNorm = normalizeQuery(queryWeightVec);
		Map<Integer, Double> docsim = new HashMap<>(); 

		//calculate weight and add to map showing similarity to query per document
		for (int i = 0; i < docsTFs.size(); i++)
		{
			Double x = dotProduct(queryWeightVec, docsTFs.get(i));
			Double norm = normalizeDoc(docsTFs.get(i));

			Double result = 0.0;
			
			//Calculate vector weight
			if (x != 0.0) {
				result = x / (norm*queryNorm);
			}
			
			//Calculate vector weight when considering PageRank.
			if (crawldb.get(i).outlinks.size() > 0) {
				result = result/crawldb.get(i).outlinks.size();
			}

			docsim.put(i, result);

		}

		docsim = sortByValue(docsim);

		return docsim;

	}

	//normalize query element
	public double normalizeQuery (Map<String, Double> query)
	{
		double normal = 0.0;
		for (String s : query.keySet())
		{
			normal += Math.pow(query.get(s),2); 
		}
		normal = Math.sqrt(normal);
		return normal;
	}

	//normalize all document elements
	public double normalizeDoc (Map<String, Double> doc)
	{
		double normal = 0.0;
		for (String s : doc.keySet())
		{
			normal += Math.pow(doc.get(s),2); 
		}
		normal = Math.sqrt(normal);
		return normal;
	}

	//calculate dot product for weight
	public double dotProduct (Map<String, Double> query, Map<String, Double> doc)
	{
		Double product = 0.0;
		for (String s : doc.keySet())
		{
			Double queryVal = query.get(s);
			Double docVal = doc.get(s);

			product += queryVal * docVal; 
		}

		return product;
	}

	//Counts repeat terms
	public Map<String, Double> countMatches(List<String> doc)
	{

		Map<String, Double> map = new HashMap<String, Double>();

		for (int i = 0; i < doc.size(); i++)
		{
			if (map.containsKey(doc.get(i)))
			{
				Double count = map.get(doc.get(i));
				map.replace(doc.get(i), count+1);
			}
			else {
				map.put(doc.get(i), 1.0);
			}


		}
		return map;

	}
	
	//parse file into list of Crawl objects containing each title, URL, content, and in/out links
	public void parse() throws FileNotFoundException {

		File f = new File("db_crawl.txt");
		Scanner scan = new Scanner(f);
		String line = null;
		String url;
		String content = "";

		Crawl entry = new Crawl(); 
		List<String> outlinks = new ArrayList<String>(); 
		List<String> inlinks = new ArrayList<String>(); 
		int entry_count = 0;
		Boolean content_start = false;

		while(scan.hasNextLine())
		{
			line = scan.nextLine();
			if (line.startsWith("http")) { 
				entry.setOutlinks(outlinks);
				entry.setInlinks(inlinks);

				crawldb.add(entry);

				entry = new Crawl(); 
				outlinks = new ArrayList<String>(); 
				inlinks = new ArrayList<String>(); 
				entry.setUrl(line.substring(0, line.indexOf("key:")-1)); 
				entry_count++;
			}
			else if (line.startsWith("title:")) { 
				entry.setTitle(line.substring(7)); 
			}

			else if (content_start == true && !line.startsWith("text:end:"))
			{
				content += line; 
			}
			else if (line.startsWith("text:start:"))
			{
				content_start = true;
			}
			else if (line.startsWith("text:end:"))
			{
				entry.setContent(content);
				content = ""; 
				content_start = false;
			}
			else { //add outlinks and inlinks
				String[] split = line.split("\\s+");
				String link = "";
				String type = ""; 
				for (String s : split)
				{
					if (s.startsWith("outlink"))
					{
						type = "out";
					}
					else if (s.startsWith("inlink"))
					{
						type = "in";
					}
					else if (s.startsWith("http"))
					{
						if (type.equals("in")) { inlinks.add(s); }
						else if (type.equals("out")) { outlinks.add(s); }

					}
				}
			}
		}

		termList = new ArrayList<String>();
		docList = new ArrayList<List<String>>();
		crawldb.remove(0); //null entry
		for (int i = 0; i < crawldb.size(); i++)
		{
			List<String> terms = new ArrayList<String>();

			if (crawldb.get(i).getUrl() != null) {
				int occurences = Collections.frequency(urlList, crawldb.get(i).getUrl());
				urlList.add(crawldb.get(i).getUrl());
				if (occurences >= 1) { //remove url entry if it already exists in database
					crawldb.remove(i);
					i--;
				}
				else {
					if (crawldb.get(i).getTitle() != null) {
						Collections.addAll(terms, crawldb.get(i).getTitle().replaceAll("[^a-zA-Z0-9 ]","").toLowerCase().split("\\s+"));
					}
					if (crawldb.get(i).getContent() != null) {
						Collections.addAll(terms, crawldb.get(i).getContent().replaceAll("[^a-zA-Z0-9 ]","").toLowerCase().split("\\s+"));
					}
					termList.addAll(terms);
					docList.add(terms);
				}

			}

		}
		
		//Replace outlinks from crawl with only those in the database
		for (Crawl crawl : crawldb)
		{
			crawl.setOutlinks(countMatch(crawl.outlinks));  
		}


	}	
	//Creates list of outlinks that match urls in database for PageRank algorithm
	public List<String> countMatch(List<String> outlinks) {
		List<String> list1 = outlinks;
		List<String> list2 = urlList;
		list1.retainAll(list2);
		return list1;
	}

	//Map Sorting Function taken from www.geeksforgeeks.org/
	public static HashMap<Integer, Double> sortByValue(Map<Integer, Double> sim) 
	{ 
		// Create a list from elements of HashMap 
		List<Map.Entry<Integer, Double> > list = 
				new LinkedList<Map.Entry<Integer, Double> >(sim.entrySet()); 

		// Sort the list 
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double> >() { 
			public int compare(Map.Entry<Integer, Double> o1,  
					Map.Entry<Integer, Double> o2) 
			{ 
				return (o2.getValue()).compareTo(o1.getValue()); 
			} 
		}); 

		// put data from sorted list back into hashmap  
		HashMap<Integer, Double> temp = new LinkedHashMap<Integer, Double>(); 
		for (Map.Entry<Integer, Double> aa : list) { 
			temp.put(aa.getKey(), aa.getValue()); 
		} 
		return temp; 
	} 

	//Removes stop words in query
	public String[] queryStopWords(String[] terms, String[] stop) {

		ArrayList<String> queryList = new ArrayList<String>(Arrays.asList(terms));
		for (int i = 0; i < queryList.size(); i++)
		{
			//for each term, compare with terms in stopWords array 		

			for (int j = 0; j < stop.length; j++)
			{	
				if (queryList.get(i).equals(stop[j]))
				{
					queryList.remove(i); 
					i--;
					break;
				}
			}
		}

		return queryList.toArray(new String[0]);

	}

	//Stems query terms
	public static String[] stemQuery(String[] terms)
	{
		Stemmer stem = new Stemmer(); 
		for (int i = 0; i < terms.length; i++)
		{
			char[] termArr = terms[i].toCharArray();
			for (int j = 0; j < termArr.length; j++)
			{
				stem.add(termArr[j]);
			}
			stem.stem(); 
			terms[i] = stem.toString();
		}

		return terms;

	}
	
	//Removes stop words from term list
	public void removeStopWords(String[] stop) {
		for (int i = 0; i < termList.size(); i++)
		{

			for (int j = 0; j < stop.length; j++)
			{	
				if (termList.get(i).equals(stop[j]))
				{
					termList.remove(i);
					i--;		
				}
			}
		}

	}

	//Stems words using Stemmer class
	public void stemWords() {
		Stemmer stemmer = new Stemmer();
		List<String> termListStemmed = new ArrayList<String>();
		String term = null;
		char[] termChars = null;
		for (int i = 0; i < termList.size(); i++)
		{
			term = termList.get(i);
			termChars = term.toCharArray();
			for (int j = 0; j < termChars.length; j++)
			{
				stemmer.add(termChars[j]);
			}
			stemmer.stem();
			term = stemmer.toString();
			termListStemmed.add(term);
		}
		termList = termListStemmed;

	}




}
