package sentiwordnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import org.json.JSONArray;
import org.json.JSONObject;



/**
 * @author khyati
 */
public class Twitter {

	/**
	 * @param args
	 */

private String ret_str;


public Twitter(){
    this.ret_str = new String();
}
	
public String Result_finder(String phrase) {
		
		if (!phrase.equalsIgnoreCase("")) {
			StringBuilder resultpage = new StringBuilder();
			resultpage.append("http://search.twitter.com/search.json?q=");
			resultpage.append(phrase);
			resultpage.append("&rpp=30&lang=en");
//			System.out.println("The website connecting is "+resultpage);
			String result1 = HttpGet(resultpage.toString());
//			System.out.println("result :"+result1);
			JSONObject t1;
			SortedMap<Integer,String>tweet_relevancy = new TreeMap<Integer,String>();
			try {
				t1 = new JSONObject(result1);
				
				int len1 = 0;
				JSONArray arr2 = t1.getJSONArray("results");
//				System.out.println("arr length "+arr2.length());
				while (len1 != arr2.length()) {
					StringBuilder tweet = new StringBuilder();

					String name_follow = arr2.getJSONObject(len1).getString(
							"from_user");
//					System.out.println("Top :" + (len1+1));
//					System.out.println("		Twitter account :"
//							+ arr2.getJSONObject(len1).getString("from_user"));
					String twitter_acc = arr2.getJSONObject(len1).getString("from_user");
//					tweet.append(twitter_acc);
//					tweet.append("  : ");
//					System.out.println("		Twitter message :"
//							+ arr2.getJSONObject(len1).getString("text"));
					String message = arr2.getJSONObject(len1).getString("text");
					tweet.append(message);
					StringBuilder s = new StringBuilder("http://twitter.com/");
					s.append(name_follow);
					String followers = Followers(s.toString());
					String f = followers.replaceAll(",", "");
					int follower_count = Integer.parseInt(f.trim());
//					System.out.println("		Followers :" + follower_count);
					tweet_relevancy.put(follower_count, tweet.toString());
					len1++;
				}

					Set s = tweet_relevancy.entrySet();
					Iterator i = s.iterator();
					int temp=0;
					while(i.hasNext())
					{
						Map.Entry m = (Map.Entry)i.next();
						
						int relevant = (Integer)m.getKey();
						String mes = (String)m.getValue();
//						System.out.println("Top "+(++temp)+" : Count : "+relevant+" : "+mes);
					}
				
					
				/*	Set s1 = tweet_relevancy.keySet();
					Integer []arr = new Integer[s1.size()];
					Iterator it2 = s1.iterator();
					int m=0;
					while(it2.hasNext())
					{						
						arr[m++]= (Integer)it2.next();
					}
			
					Arrays.sort(arr,Collections.reverseOrder());
					System.out.println(Arrays.toString(arr));
					*/
					
					
					// This will print the tweets in the descending order 
					NavigableSet<Integer> s2 = (NavigableSet<Integer>) tweet_relevancy.keySet();
					Iterator it2 = s2.descendingIterator();
					while(it2.hasNext())
					{
						String comp = it2.next().toString();

//						System.out.print(comp+" ");
//                                                this.ret_str += comp + " ";
						Set sm = tweet_relevancy.entrySet();
						Iterator itr2 = sm.iterator();
						while(itr2.hasNext())
						{
							Map.Entry mm1 = (Map.Entry)itr2.next();
							if(mm1.getKey().toString().equalsIgnoreCase(comp)){
							System.out.println(mm1.getKey()+" "+mm1.getValue());
//                                                        this.ret_str += mm1.getKey()+" "+mm1.getValue() + "\n";
                                                        this.ret_str += mm1.getValue() + "\n"+"\n";
                                                        }
						}
					}
		
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
                return this.ret_str;
	}

private String Followers(String name) {
	String sourceurl = name;
	Segment followers = null;
	try {
		Source source = new Source(new URL(sourceurl));
		List<Element> elementList = source.getAllElements();
		for (Element element : elementList) {
			StartTag starttag = element.getStartTag();

			Attributes attr = starttag.getAttributes();
			if (attr != null) {
				if (attr.get("id") != null) {
					Attribute attrid = attr.get("id");
					if (attrid.getValue()
							.equalsIgnoreCase("follower_count")) {
						followers = starttag.getElement().getContent();

					}

				}
			}
		}
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return followers.toString();
}


private String HttpGet(String request) {
	URL url;

	StringBuilder sb = new StringBuilder();

	try {
		url = new URL(request);
		
		HttpURLConnection conn;
		conn = (HttpURLConnection) url.openConnection();

		if (conn.getResponseCode() != 200) {
			try {
				throw new IOException(conn.getResponseMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		BufferedReader rd = new BufferedReader(new InputStreamReader(conn
				.getInputStream()));

		String temp = new String();
		try {
			while ((temp = rd.readLine()) != null) {
				sb.append(temp);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		rd.close();
		conn.disconnect();

		return sb.toString();

	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	return sb.toString();
}
	
	
/*
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Twitter tweet = new Twitter();
		String phrase = "matrix+movie";
		tweet.Result_finder(phrase);
	}
 * 
 */

}
