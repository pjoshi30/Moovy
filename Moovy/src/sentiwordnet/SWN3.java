package sentiwordnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * @author: khyati
 */
public class SWN3 {
	/**
	 * @param args
	 */
	private String pathToSWN = "/media/2C7457705DD85BA3/NetBeans/NLP_5/SentiWordNet_3.0.0_20100908_1.txt";
	public static HashMap<String, Double> _dict;
	public static HashMap<String, String> _stop;
	public static HashMap<String, Integer> _actor;
	public static HashMap<String, Double> _positivity;
	public static HashMap<String, HashMap<String,Integer>> _actor_popular;

	public SWN3() {
		_actor = new HashMap<String, Integer>();
		_dict = new HashMap<String, Double>();
		_positivity = new HashMap<String, Double>();
		_actor_popular = new HashMap<String, HashMap<String,Integer>>();
		HashMap<String, Vector<Double>> _temp = new HashMap<String, Vector<Double>>();
		try {
			BufferedReader csv = new BufferedReader(new FileReader(pathToSWN));
			String line = "";

			while ((line = csv.readLine()) != null) {

				String[] data = line.split("\t");
				// System.out.println("data 0 "+data[0]+" data 1 "+data[1]+" data 2 "+data[2]+"data 3 "+
				// data[3]+" data 4 "+data[4]);
				Double score = Double.parseDouble(data[2])
						- Double.parseDouble(data[3]);
				String[] words = data[4].split(" ");
				for (String w : words) {
					String[] w_n = w.split("#");
					w_n[0] += "#" + data[0];
					int index = Integer.parseInt(w_n[1]) - 1;
					if (_temp.containsKey(w_n[0])) {
						Vector<Double> v = _temp.get(w_n[0]);
						if (index > v.size())
							for (int i = v.size(); i < index; i++)
								v.add(0.0);
						v.add(index, score);
						_temp.put(w_n[0], v);
					} else {
						Vector<Double> v = new Vector<Double>();
						for (int i = 0; i < index; i++)
							v.add(0.0);
						v.add(index, score);
						_temp.put(w_n[0], v);
					}
				}
			}
			Set<String> temp = _temp.keySet();
			for (Iterator<String> iterator = temp.iterator(); iterator
					.hasNext();) {
				String word = (String) iterator.next();
				Vector<Double> v = _temp.get(word);
				double score = 0.0;
				double sum = 0.0;
				for (int i = 0; i < v.size(); i++)
					score += ((double) 1 / (double) (i + 1)) * v.get(i);
				for (int i = 1; i <= v.size(); i++)
					sum += (double) 1 / (double) i;
				score /= sum;
				String sent = "";
				if (score >= 0.75)
					sent = "strong_positive";
				else if (score > 0.25 && score <= 0.5)
					sent = "positive";
				else if (score > 0 && score >= 0.25)
					sent = "weak_positive";
				else if (score == 0)
					sent = "neutral";
				else if (score < 0 && score >= -0.25)
					sent = "weak_negative";
				else if (score < -0.25 && score >= -0.5)
					sent = "negative";
				else if (score <= -0.75)
					sent = "strong_negative";
				_dict.put(word, score);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * public String extract(String word, String pos) { return
	 * _dict.get(temp[0]+"#"+pos); }
	 */
	public static void stopword() {
		int num = 1;
		String path = "/home/preetam/MalwareAnalyzer/FaceMonkey/stopList.txt";
		_stop = new HashMap<String, String>();
		try {
			BufferedReader csv = new BufferedReader(new FileReader(path));
			String word = "";
			while ((word = csv.readLine()) != null) {
				_stop.put(word, word);
				// System.out.println(num);
				num++;
			}
			//System.out.println("Total stop words :" + num);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * public static int evaluate_word(String word) { String eval =
	 * _dict.get(word); if(eval.equalsIgnoreCase("weak_negative")) return -1;
	 * if(eval.equalsIgnoreCase("negative")) return -2;
	 * if(eval.equalsIgnoreCase("strong_negative")) return -3;
	 * if(eval.equalsIgnoreCase("strong_postive")) return 3;
	 * if(eval.equalsIgnoreCase("positive")) return 2;
	 * if(eval.equalsIgnoreCase("wek_positive")) return 1;
	 * if(eval.equalsIgnoreCase("neutral")) return 0; return 0; }
	 */
	public static void word_analysis() {

		String path = "/home/preetam/MalwareAnalyzer/Moovy/generatedReviews.txt";
		BufferedReader csv;
		MaxentTagger tagger;
		try {
			csv = new BufferedReader(new FileReader(path));
			tagger = new MaxentTagger("/home/preetam/MalwareAnalyzer/Moovy/models/left3words-wsj-0-18.tagger");
			String line = "";
			int matches = 0, total = 0;
			int pos_cast[] = new int[3];
			int neg_cast[] = new int[3];
			while ((line = csv.readLine()) != null) {
				double sum = 0;
				// System.out.print(line);
				// String[] sent = line.trim().split("::");
				// System.out.println("**"+sent[0]);
				String sent = line.trim();
				//System.out.println("Line :" + line);
				String[] data = sent.split(" ");
				for (String w : data) {
					if (w.trim().contains("("))
						w = w.replace("(", "");

					if (w.trim().contains(")"))
						w = w.replace(")", "");

					if (_stop.containsValue(w.toLowerCase()))
						;// System.out.println("stop word "+w);

					else if (_stop.containsValue(w.toUpperCase()))
						;// System.out.println("stop word "+w);

					else if (w.equalsIgnoreCase(""))
						;
					else if (w.contains("&"))
						;
					else {
						//System.out.println("w1 word "+ w);
						Set temp = _actor.entrySet();
						Iterator it = temp.iterator();
						while (it.hasNext()) {
							Map.Entry m = (Map.Entry) it.next();
							String key = m.getKey().toString();
							int val = (Integer) m.getValue();
							String name[] = m.getKey().toString().split(" ");
							for (String w1 : name) {
								
								if (w.toString().contains(w1)) {
									_actor.put(key, val + 1);
									
									//System.out.println("Line is "+line);
								}
							}
						}

						String taggedstring = tagger.tagString(w);
						// System.out.println("word = "+w+"length ="+w.length());
						String tail = taggedstring.substring(w.length() + 1);
						// System.out.println("tagged string ="+taggedstring+" tail ="+tail);
						if (tail.contains("NN") || tail.contains("NNS")
								|| tail.contains("NNP")
								|| tail.contains("NNPS")) {
							String new_word = w + "#n";
							if (_dict.containsKey(new_word.toLowerCase())
									|| _dict
											.containsKey(new_word.toUpperCase())) {
								if (_dict.get(new_word.toLowerCase()) != null) {
									// System.out.println(w+"="+_dict.get(new_word.toLowerCase()));
									sum += _dict.get(new_word.toLowerCase());
								} else {
									// System.out.println(w+"="+_dict.get(new_word.toUpperCase()));
									sum += _dict.get(new_word.toUpperCase());
								}

								// System.out.println("word is "+w+" score is "+sum);
							}
							// System.out.println(w+"= noun");

						} else if (tail.contains("VB") || tail.contains("VBD")
								|| tail.contains("VBG") || tail.contains("VBN")
								|| tail.contains("VBP") || tail.contains("VBZ")) {
							String new_word = w + "#v";
							if (_dict.containsKey(new_word.toLowerCase())
									|| _dict
											.containsKey(new_word.toUpperCase())) {
								if (_dict.get(new_word.toLowerCase()) != null) {
									// System.out.println(w+"="+_dict.get(new_word.toLowerCase()));
									sum += _dict.get(new_word.toLowerCase());
								} else {
									// System.out.println(w+"="+_dict.get(new_word.toUpperCase()));
									sum += _dict.get(new_word.toUpperCase());
								}

								// System.out.println("word is "+w+" score is "+sum);
								// System.out.println(w+"="+_dict.get(new_word));
							}
							// System.out.println(w+"= verb");

						} else if (tail.contains("JJ") || tail.contains("JJR")
								|| tail.contains("JJS")) {
							String new_word = w + "#a";
							if (_dict.containsKey(new_word.toLowerCase())
									|| _dict
											.containsKey(new_word.toUpperCase())) {
								if (_dict.get(new_word.toLowerCase()) != null) {
									// System.out.println(w+"="+_dict.get(new_word.toLowerCase()));
									sum += _dict.get(new_word.toLowerCase());
								} else {
									// System.out.println(w+"="+_dict.get(new_word.toUpperCase()));
									sum += _dict.get(new_word.toUpperCase());
								}

								// System.out.println("word is "+w+" score is "+sum);
								// System.out.println(w+"="+_dict.get(new_word));
							}
							// System.out.println(w+"= adjective");

						} else if (tail.contains("RB") || tail.contains("RBR")
								|| tail.contains("RBS")) {
							String new_word = w + "#r";
							if (_dict.containsKey(new_word.toLowerCase())
									|| _dict
											.containsKey(new_word.toUpperCase())) {
								if (_dict.get(new_word.toLowerCase()) != null) {
									// System.out.println(w+"="+_dict.get(new_word.toLowerCase()));
									sum += _dict.get(new_word.toLowerCase());

								} else {
									// System.out.println(w+"="+_dict.get(new_word.toUpperCase()));
									sum += _dict.get(new_word.toUpperCase());
								}

								// System.out.println("word is "+w+" score is "+sum);
								// System.out.println(w+"="+_dict.get(new_word));
							}
							// System.out.println(w+"= adverb");

						} else
							;// System.out.println("word is "+w+" taggedword is "+taggedstring);

					}
				}
				//System.out.println("Line score = " + sum);
				Set temp = _actor.entrySet();
				Iterator it = temp.iterator();
				Set temp1 = _actor_popular.entrySet();
				Iterator it1 = temp1.iterator();
				int ct = 0;
				while ((it.hasNext()&&it1.hasNext())|| ct < 3) {
					Map.Entry m = (Map.Entry) it.next();
					Map.Entry m1 = (Map.Entry)it1.next();
					if ((Integer) m.getValue() > 0) {
						if (sum > 0.1)
						{
							String name = m.getKey().toString();
							HashMap <String,Integer> act = new HashMap<String,Integer>();
							act = _actor_popular.get(name);
							int val = act.get("Positive");
							act.put("Positive", val+1);
							//System.out.println(name + "in positive line");
						}
						else if (sum < -0.1)
						{
							String name = m.getKey().toString();
							HashMap <String,Integer> act = new HashMap<String,Integer>();
							act = _actor_popular.get(name);
							int val = act.get("Negative");
							act.put("Negative", val+1);	
							//System.out.println(name + "in negative line");
						}
						else
						{
							String nm = m.getKey().toString();
							//System.out.println(nm + "in neutral line");
						}
					}
					ct++;
				}
				// System.out.println("score orig = "+sent[1]);
				if (sum > 0.1) {
					// int comp = Integer.parseInt(sent[1]);
					// if(comp==1)
					matches++;
					total++;
				} else if (sum < -0.1) {
					// int comp = Integer.parseInt(sent[1]);
					// if(comp==0)
					total++;
				}
				/*
				 * else { int comp = Integer.parseInt(sent[1]); if(comp==2)
				 * matches++;total++; }
				 */
				
	
				reset_cast_list();
			}
			double res = matches * 100 / total;
			//System.out.println("Matches = " + matches + " Total = " + total);
			//System.out.println("Positivity = " + res);
			_positivity.put("movie", res);

			// Positive popularity of actor
			Set temp = _actor_popular.entrySet();
			Iterator it = temp.iterator();
			
			while(it.hasNext())
			{
				Map.Entry e = (Map.Entry) it.next();
				String actor_name = e.getKey().toString();
				HashMap<String,Integer> hm = new HashMap<String,Integer>();
				hm = _actor_popular.get(actor_name);
				float poscnt = hm.get("Positive");
				float negcnt = hm.get("Negative");
				//System.out.println("Positivity of Actor "+actor_name+" = "+(poscnt*100/(negcnt+poscnt)));
				//System.out.println("Negativity of Actor "+actor_name+" = "+(negcnt*100/(poscnt+negcnt)));
				double pos_actor = poscnt*100/(negcnt+poscnt);
				_positivity.put(actor_name, pos_actor);
				//System.out.println("Positive count = "+poscnt+" Negative count = "+negcnt);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void reset_cast_list() {
		Set temp = _actor.entrySet();
		Iterator it = temp.iterator();
		int ct = 0;
		while (it.hasNext()) {
			Map.Entry m = (Map.Entry) it.next();
			String key = m.getKey().toString();
			_actor.put(key, 0);
		}

	}

	public static void cast_list() {

		try {
			String path = "/home/preetam/MalwareAnalyzer/Moovy/castFreq.txt";

			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = "";
			int count = 0;
			while ((line = br.readLine()) != null && count < 3) {
				String data[] = line.split("::");
				int val = Integer.parseInt(data[1].toString().trim());
				if (val > 0)
					_actor.put(data[0], 0);
				HashMap<String,Integer> temp = new HashMap<String,Integer>();
				temp.put("Positive", 0);
				temp.put("Negative", 0);
				_actor_popular.put(data[0], temp);
				count++;
			}

			Set temp = _actor.entrySet();
			Iterator it = temp.iterator();
			int ct = 0;
			while (it.hasNext() || ct < 3) {
				Map.Entry m = (Map.Entry) it.next();
				//System.out.println(m.getKey());
				ct++;
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static HashMap<String,Double> getPopularMap(){
		return _positivity;
	}

	public static ArrayList<String> getActorArrayList(){
		ArrayList<String> keysAsList = new ArrayList<String>(_actor.keySet());
		return keysAsList;
		
	}
	/*public static void main(String[] args) {
		// TODO Auto-generated method stub
		SWN3 temp = new SWN3();
		temp.stopword();
		temp.cast_list();
		temp.word_analysis();*/

		/*
		 * System.out.println("Size of the hashmap is "+_dict.size() ); Iterator
		 * itr = _dict.entrySet().iterator(); while(itr.hasNext())
		 * System.out.println(itr.next()); System.out.println("done");
		 */

		/*
		 * String word = "turtles#n"; if(_dict.containsKey(word))
		 * System.out.println(_dict.get(word));
		 *///}

}
