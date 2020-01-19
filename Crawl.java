package CPS842;

import java.util.ArrayList;
import java.util.List;

public class Crawl {
	
	String title;
	String url;
	String content;
	List<String> outlinks = new ArrayList<String>();
	List<String> inlinks = new ArrayList<String>();
	
	public String toString() {
		String s = ""; 
		s = "[Title: " + title + " | url: " + url + " | outlinks_count: " + outlinks.size() + " | content: " + content + "],\n";
		return s;
	}

	
	public String getTitle() {
		return title;
	}
	
	public String getOutlinks() {
		return outlinks.toString();
	}
	
	public String getInlinks() {
		return inlinks.toString();
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getContent() {
		return content;
	}
	
	
	public void setTitle(String x) {
		title = x;
	}
	
	public void setOutlinks(List<String> x) {
		outlinks = new ArrayList<String>(x);
	}
	
	public void setInlinks(List<String> x) {
		inlinks = new ArrayList<String>(x);
	}
	
	public void setUrl(String x) {
		url = x;
	}
	
	public void setContent(String x) {
		content = x;
	}
	
	
}


