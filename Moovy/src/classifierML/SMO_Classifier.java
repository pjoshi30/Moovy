package classifierML;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;

import java.awt.BorderLayout;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


/**
 * author: preetam
 */
public class SMO_Classifier {
  /** the classifier used internally */
  protected Classifier m_Classifier = null;
  
  /** the filter to use */
  protected Filter m_Filter = null;

  /** the training file */
  protected String m_TrainingFile = null;

  /** the training instances */
  protected Instances m_Training = null;

  /** for evaluating the classifier */
  protected Evaluation m_Evaluation = null;
  
  HashMap<String, String> actor_fullname;
  
  HashMap<String, Double> _positivity;

  public SMO_Classifier(){
	  actor_fullname = new HashMap<String, String>();
	  _positivity =  new HashMap<String, Double>();
  }

  /**
   * sets the classifier to use
   * @param name        the classname of the classifier
   * @param options     the options for the classifier
   */
  public void setClassifier(String name, String[] options) throws Exception {
    m_Classifier = Classifier.forName(name, options);
  }

  /**
   * sets the file to use for training
   */
  public void setTraining(String name) throws Exception {
    m_TrainingFile = name;
    m_Training     = new Instances(
                        new BufferedReader(new FileReader(m_TrainingFile)));
    m_Training.setClassIndex(m_Training.numAttributes() - 1);
  }

  
  public ArrayList<HashMap<String, Integer>> loadActorLineNumbersFromFile(){
	  	
	  ArrayList<HashMap<String, Integer>> arr = null;
	  	try{
		    
		    FileInputStream fstream = new FileInputStream("/home/preetam/MalwareAnalyzer/Moovy/charAppearingOnLines.txt");
		    // Get the object of DataInputStream
		    DataInputStream in = new DataInputStream(fstream);
		        BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    
		    
		    arr= new ArrayList<HashMap<String, Integer>>();
		    String strLine;
		    //Read File Line By Line
		    while ((strLine = br.readLine()) != null)   {
		    	HashMap<String, Integer> hm = new HashMap<String, Integer>();
		        String[] splitt = strLine.trim().split(" ");
		        hm.put(splitt[0], Integer.parseInt(splitt[1]));
		        arr.add(hm);
		    }
		    //Close the input stream
		    in.close();
		    }catch (Exception e){//Catch exception if any
		      System.err.println("Error: " + e.getMessage());
		    }
	  
		    return arr;
  }
  
  public ArrayList<String> loadActorList(){
	  
	  ArrayList<String> actors = null;
	  try{
		  FileInputStream fstream = new FileInputStream("/home/preetam/MalwareAnalyzer/Moovy/castFreq.txt");
		    // Get the object of DataInputStream
		    DataInputStream in = new DataInputStream(fstream);
		        BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    
		    
		    actors = new ArrayList<String>();
		    String strLine;
		    //Read File Line By Line
		    while ((strLine = br.readLine()) != null)   {
		        String[] splitt = strLine.trim().split("::");
		        String[] splitt_again = splitt[0].trim().split(" ");
		        actors.add(splitt_again[1]);
		        actor_fullname.put(splitt_again[1], splitt[0]);
		    }
		    //Close the input stream
		    in.close();
		    }catch (Exception e){//Catch exception if any
		      System.err.println("Error: " + e.getMessage());
		    }
		  
		  return actors;
	  
  }
  
  /**
   * Generate the individual actor positive or negative sentiment
   * @param arr
   * @param labelmap
   */
  public void generateActorSentiment(ArrayList<HashMap<String, Integer>> arr, HashMap<Integer,Double> labelmap){
	  
	  ArrayList<String> actors = loadActorList();
	  
	  HashMap<String, HashMap<String,Integer>> actorSentiment = new HashMap<String, HashMap<String, Integer>>();
	  
	  //Initialize HashMap
	  for(String actor: actors){
		  HashMap<String,Integer> posnegcount = new HashMap<String,Integer>();
		  posnegcount.put("Positive", 0);
		  posnegcount.put("Negative", 0);
		  actorSentiment.put(actor, posnegcount);
	  }
	  
	  
	  double sentiment = -1;
	  System.out.println("LABELMAP: "+labelmap);
	  for(String actor: actors){
		  for (HashMap<String, Integer> hm: arr){
			  if(!hm.containsKey(actor)){
				  continue;
			  }
			  System.out.println("Actor: "+actor+" LINE: "+hm.get(actor));
			  sentiment = labelmap.get(hm.get(actor));
			  //System.out.println("Actor: "+actor+" Sentiment: "+sentiment+" LINE: "+hm.get(actor));
			  if(sentiment == 0.0 ){
				  HashMap<String,Integer> posneg_count = new HashMap<String,Integer>();
				  posneg_count = actorSentiment.get(actor);
				  posneg_count.put("Negative",posneg_count.get("Negative")+1);
				  actorSentiment.put(actor, posneg_count);
			  } else if(sentiment == 1.0){
				  HashMap<String,Integer> posneg_count = new HashMap<String,Integer>();
				  posneg_count = actorSentiment.get(actor);
				  posneg_count.put("Positive",posneg_count.get("Positive")+1);
				  actorSentiment.put(actor, posneg_count);
			  }
			  //System.out.println("Actor List: "+ actorSentiment);
		  }
		  
	  }
	  
	  //Output the HashMap
	  System.out.println( "FINAL Actor List: "+ actorSentiment );
	  double actorpospercent;
	  
	  for(String actor: actors){
		  int pos = actorSentiment.get(actor).get("Positive");
		  int neg = actorSentiment.get(actor).get("Negative");
		  if(pos+neg == 0){
			  actorpospercent = 0;
		  }else{
			  actorpospercent = pos*100/(pos+neg);  
		  }
		  _positivity.put(actor_fullname.get(actor),actorpospercent );
	  }
	  
	  
  }
  
  public HashMap<String,Double> getPopularMap(){
		return _positivity;
	}
  
  /**
   * Generates the overall sentiment of the movie
   * @param labelmap
   */
  public void generateOverallSentiment(HashMap<Integer,Double> labelmap){
	  
	  Iterator<Integer> itr = labelmap.keySet().iterator();
	  int pos = 0;
	  int neg = 0;
	  
	  int line = itr.next();
	  while(itr.hasNext()){
		if(labelmap.get(line) == 0.0){
			neg++;
			line = itr.next();
		}else if (labelmap.get(line) == 1.0){
			pos++;
			line = itr.next();
		}else{
			line= itr.next();
		}
	  }
	  
	  System.out.println("Overall Movie Positive: "+pos);
	  System.out.println("Overall Movie Negative: "+neg);
	  
	  double pospercent = pos*100/(pos+neg);
	  _positivity.put("movie", pospercent);
	  
	  
  }
  
  /**
   * runs 10fold CV over the training file
   */
  public void execute() throws Exception {
    
	//load the values from the file
	 ArrayList<HashMap<String, Integer>> arr = loadActorLineNumbersFromFile();
	
	  ObjectInputStream ois = new ObjectInputStream(
              new FileInputStream("/preetam/NLP/FinalProj/Models/all_700_stemmed.model"));
	  m_Classifier = (Classifier) ois.readObject();
	  ois.close();
      
    //Classify Unlabelled data set
    Instances unlabeled = new Instances(
    		new BufferedReader(
    				new FileReader("/home/preetam/MalwareAnalyzer/Moovy/testset.arff")));

    // set class attribute
   unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

    // create copy
//    Instances labeled = new Instances(unlabeled);
    
    //Hashmap tp store the generated values
    HashMap<Integer,Double> labelmap = new HashMap<Integer, Double>();
    

    // label instances
    for (int i = 0; i < unlabeled.numInstances(); i++) {
    	double clsLabel = m_Classifier.classifyInstance(unlabeled.instance(i));
    	labelmap.put(i, clsLabel);
    	instance(i).setClassValue(clsLabel);
    }
    
    //generate overall movie sentiment
    generateOverallSentiment(labelmap);
    
    //generate actor specific sentiment output
    generateActorSentiment(arr, labelmap);
    
  }

  /**
   * outputs some data about the classifier
   */
  public String toString() {
    StringBuffer        result;

    result = new StringBuffer();
    result.append("Weka -TwitterSentiment Analysis SMO\n===========\n\n");

    result.append("Classifier...: " 
        + m_Classifier.getClass().getName() + " " 
        + Utils.joinOptions(m_Classifier.getOptions()) + "\n");
    
    result.append("Training file: " 
        + m_TrainingFile + "\n");
    result.append("\n");

    result.append(m_Classifier.toString() + "\n");
    result.append(m_Evaluation.toSummaryString() + "\n");
    try {
      result.append(m_Evaluation.toMatrixString() + "\n");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    try {
      result.append(m_Evaluation.toClassDetailsString() + "\n");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
   
    return result.toString();
  }

  
  
  /**
   * runs the program, the command line looks like this:<br/>
   * WekaDemo CLASSIFIER classname [options] 
   *          FILTER classname [options] 
   *          DATASET filename 
   * <br/>
   * e.g., <br/>
   *   java -classpath ".:weka.jar" WekaDemo \<br/>
   *     CLASSIFIER weka.classifiers.trees.J48 -U \<br/>
   *     FILTER weka.filters.unsupervised.instance.Randomize \<br/>
   *     DATASET iris.arff<br/>
   */
  
	     
 public static void main(String[] args) throws Exception {
   
    
    SMO_Classifier   smo_class;

    String classifier = "weka.classifiers.functions.SMO";
    //String dataset = args[0];
    Vector<String> classifierOptions = new Vector<String>();

    //SMO classifier options
    String options = "-C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K";
    String[] options_arr = options.split(" ");
    for(String var : options_arr){
    	classifierOptions.add(var);
    }
    classifierOptions.add("weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0");
    
    // run
    smo_class = new SMO_Classifier();
    smo_class.setClassifier(
        classifier, 
        (String[]) classifierOptions.toArray(new String[classifierOptions.size()]));

    smo_class.execute();

  }
}
