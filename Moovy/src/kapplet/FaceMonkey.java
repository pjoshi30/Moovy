package kapplet;


import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import classifierML.SMO_Classifier;

import controlP5.ControlFont;
import controlP5.ControlP5;
import controlP5.RadioButton;

import processing.core.PApplet;
import processing.core.PFont;
import sentiwordnet.SWN3;


/**
 * @author: preetam
 */

public class FaceMonkey extends PApplet{

	/**
	 * @param args
	 */
	
	PFont myfont;
	PFont font3;
	ControlP5 controlP5;
	ControlFont font;

	//Get the top 3 actor names and store them in a string
	String actor1_firstname = "ACTOR1";
	String actor1_lastname = "ACTOR1";
	String actor2_firstname = "ACTOR2";
	String actor2_lastname = "ACTOR2";
	String actor3_firstname = "ACTOR3";
	String actor3_lastname = "ACTOR3";
	Integer[] percen= new Integer[]{
			new Integer(0),	
			new Integer(0),
			new Integer(0),
			new Integer(0),
			new Integer(0),
			new Integer(0),
			new Integer(0),
			new Integer(0),
	};
	//Hashmap to ensure the order of actor population
	HashMap<Integer, String> map = new HashMap<Integer, String>();
	int count = 0;
	//Contains the actor list
	ArrayList<String> actors = new ArrayList<String>();
	
	
	
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		PApplet.main(new String[] {"--present","FaceMonkey"});
		
	}

	public void sentiPopulatePercentArray(HashMap<String,Double> pos){
		percen[1] = pos.get("movie").intValue();
		percen[3] = pos.get(actors.get(0)).intValue();
		map.put(1, actors.get(0));
		percen[5] = pos.get(actors.get(1)).intValue();
		map.put(2, actors.get(1));
		percen[7] = pos.get(actors.get(2)).intValue();
		map.put(3, actors.get(2));
		actor1_firstname = actors.get(0).split(" ")[0];
		actor1_lastname = actors.get(0).split(" ")[1];
		actor2_firstname = actors.get(1).split(" ")[0];
		actor2_lastname = actors.get(1).split(" ")[1];
		actor3_firstname = actors.get(2).split(" ")[0];
		actor3_lastname = actors.get(2).split(" ")[1];
	}
	
	public void runSentiWordnet(){
		//Get Map from Sentiwordnet
		SWN3 temp = new SWN3();
		temp.stopword();
		temp.cast_list();
		temp.word_analysis();
		//Contains movie and top 3 actors with their postivity values
		HashMap<String,Double> positivity_senti = temp.getPopularMap();
		//List of actors
		actors = temp.getActorArrayList();
		
		//Populate the array wiht values from sentiWordnet
		sentiPopulatePercentArray(positivity_senti);
		
	}
	
	public void classifierPopulatePercentArray(HashMap<String,Double> pos){
		System.out.println(" POS!"+pos);
		if(pos.isEmpty()){
			System.out.println("Problem POS Empty!");
		}else if(map.get(1) == null){
			System.out.println("Problem MAP empty"+map.get(1));
		}else{
			percen[0] = pos.get("movie").intValue();
			percen[2] = pos.get(map.get(1)).intValue();
			percen[4] = pos.get(map.get(2)).intValue();
			percen[6] = pos.get(map.get(3)).intValue();
		}
	}
	
	public void runMLClassifier(){
		
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
		SMO_Classifier smo = new SMO_Classifier();
		try {
			smo.setClassifier(
			        classifier, 
			        (String[]) classifierOptions.toArray(new String[classifierOptions.size()]));
			
			smo.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	       
		HashMap<String,Double> positivity_senti = new HashMap<String, Double>();
			positivity_senti = smo.getPopularMap();
		
		//Populate the array wiht values from sentiWordnet
		classifierPopulatePercentArray(positivity_senti);
	
		
	}
	
	public void setup()
	{
		//Perform SentiWordnet operations
		runSentiWordnet();
		
		//Perform ML Classifier operations
		runMLClassifier();
		
		size(860,309);
		background(255);
		font = new ControlFont(createFont("Viner Hand ITC",20),20);
		font.setSmooth(true);
		controlP5 = new ControlP5(this);		
	}
	
	public void draw()
	{
		int u=0;
		myfont=createFont("Viner Hand ITC",20);
		textFont(myfont); 
	    stroke(0);
	    strokeWeight(1);
	    fill(0);
		stroke(0);
	    line(100,250,100,50);
	    text("0",85,248);
	    line(100,150,95,150);
	    text("50",75,148);
	    line(100,50,95,50);
	    text("100",70,48);
	    strokeCap(ROUND);
	    line(100,250,700,250);
	    myfont=createFont("Viner Hand ITC",20);
		textFont(myfont); 
	    text("MOVIE",125,270);
	    text("REVIEWS",125,295);
	    //print actor1 name
	    if(actor1_firstname.length()>10)
	    	actor1_firstname=actor1_firstname.substring(0, 11);
	    if(actor1_lastname.length()>10)
	    	actor1_lastname=actor1_lastname.substring(0, 11);
	    text(actor1_firstname,275,270);
	    text(actor1_lastname,275,295);
	
	    
	    //print actor2 name
	    if(actor2_firstname.length()>10)
	    	actor2_firstname=actor2_firstname.substring(0, 11);
	    if(actor2_lastname.length()>10)
	    	actor2_lastname=actor2_lastname.substring(0, 11);
	    text(actor2_firstname,425,270);
	    text(actor2_lastname,425,295);
	    
	    //print actor3 name
	    if(actor3_firstname.length()>10)
	    	actor3_firstname=actor3_firstname.substring(0, 11);
	    if(actor3_lastname.length()>10)
	    	actor3_lastname=actor3_lastname.substring(0, 11);
	    text(actor3_firstname,575,270);
	    text(actor3_lastname,575,295);
	    
	    fill(236,0,0);
		stroke(236,0,0);
		rect(725,25,15,15);
		text("Negativity",745,40);
		
		fill(0,128,0);
		stroke(0,128,0);
		rect(725,45,15,15);
		text("Positivity",745,60);
		
		
		
	/*	line(100,20,100,15);
		line(150,20,150,15);
		line(200,20,200,15);
	    */
		fill(236,0,0);
		stroke(236,0,0);
		

		u=0;
		for(int i=1;i<=8;i++)
		{
		fill(236,0,0);
		stroke(236,0,0);
		rect(125+u,50,42,200);
		
		//text("Rating "+(i+1),0,50+u);
		if(i%2==0)
		{
		fill(0);
		stroke(0);
		text("SW",125+u+10,30);
		u=u+90;
		}
		else 
		{
			fill(0);
			stroke(0);
			text("CL",125+u+10,30);
			u=u+60;
			
		}
		}
		u=0;
		
		//this function draws the green rectangle i.e. for positivity
		for(int i=0;i<8;i++)
		{
		int percentage=percen[i];
		int total = (100-percentage)*2 +50;
		fill(0,128,0);
		stroke(0,128,0);
		rect(125+u,total,42,250-total);
		if((i+1)%2==0)
			u=u+90;
		else
			u=u+60;
		}
				
	}
	
}
