package gui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import processing.core.*;

/**
 * @author khyati
 */
public class TagCloud extends PApplet {

//PApplet parent;
    private String filepath = new String("/home/preetam/MalwareAnalyzer/Moovy/");
    PFont font;
    String fontFile = "PTF-Nordic-48.vlw";
    float baseline_ratio = (float) 0.4;
    float angle = 0;
    float outwards = 1;
//String[] tags  = {  "The Office", "The Colbert Report", "Battlestar Galactica", "weeds", "californication", "scrubs", "CSI", "Smallville", "House", "Family Guy", "Daily Show", "US", "Boondocks", "Venture Brothers", "Flight of the Conchords", "Firefly", "Angel", "Grey's Anatomy", "Venture Bros", "King of The Hill", "America's Next Top Model", "Project Runway", "My Name is Earl", "Dirty Jobs", "Eureka", "Friends", "Frasier", "Sex and the City", "Food Network", "South Park", "Seinfeld", "The Daily Show", "Colbert Report", "The Riches", "LOST", "Aqua Teen Hunger Force", "Arrested Development", "Freaks and Geeks", "MythBusters", "Dexter", "Six Feet Under", "Home Movies", "Pete & Pete", "Extras", "tears", "Law and Order", "Pete and Pete", "Invader Zim", "Jericho", "The Boondocks", "The Simpsons", "Futurama", "jeopardy", "King Of Queens", "American Dad", "Dr. Who", "That 70's Show", "Avatar", "Fraggle Rock", "History Channel", "24", "Kids in the Hall", "Strangers With Candy"};
//int[] tagtally = {  16, 4, 3, 8, 2, 15, 5, 3, 8, 16, 3, 2, 2, 2, 5, 5, 2, 9, 2, 2, 4, 4, 3, 2, 2, 5, 2, 4, 2, 2, 3, 6, 7, 2, 9, 4, 7, 2, 2, 6, 3, 5, 2, 2, 2, 2, 3, 3, 2, 3, 5, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 };
//String[] tags  = {  "The Office", "The Colbert Report", "Battlestar Galactica", "weeds" };
//int[] tagtally = {  16, 4, 3, 8 };
    String[] tags;
    int[] tagtally;
    int most;
    int least;
    int large_font;
    int small_font;
    Box[] boxes;

    public void setup() {
//  size(420,210);
        ArrayList<String> tgs = new ArrayList<String>();
        ArrayList<Integer> tls = new ArrayList<Integer>();


        try {
            FileReader fr = new FileReader(this.filepath + "castFreq.txt");
            BufferedReader br = new BufferedReader(fr);
            String str;

            try {
                while (!((str = br.readLine()) == null)) {
                    String val = str.substring(0, str.indexOf("::"));
                    String num = str.substring(str.indexOf("::") + 2);
                    tgs.add(new String(val));
                    tls.add(new Integer(num));
                    System.out.println(num);
                }

                br.close();
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        tags = new String[tgs.size()];
        tagtally = new int[tgs.size()];


        for(int i=0;i<tgs.size();i++){
            tags[i] = tgs.get(i);
            System.out.print(tags[i]);
            tagtally[i] = tls.get(i).intValue();
            System.out.println("--------" + tagtally[i]);
        }
        

        most = max(tagtally);
        least = min(tagtally);
        large_font = 28;
        small_font = 8;

        boxes = new Box[tags.length];

        smooth();
        rectMode(CENTER);
        font = createFont("FFScala", 32);

        for (int i = 0; i < tags.length; i++) {
            int h_test = (int) (map(tagtally[i], least, most, small_font, large_font));
            textFont(font, h_test);
            int w_test = (int) (textWidth(tags[i]));

            boxes[i] = new Box(tags[i], w_test, h_test);
        }

    }

    public void draw() {
        background(0);
        pushMatrix();
//  parent.scale((float)0.82,(float)0.82);
        scale((float) 1.0, (float) 1.0);
        translate((float) 0.12 * width, (float) 0.15 * height);
        for (int i = 0; i < boxes.length; i++) {
            boxes[i].collide(i);
            fill(0);
            boxes[i].render(i);
        }
        popMatrix();

        //check for stopping
        int completed = 0;
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i].frozen) {
                completed++;
            }
        }
        if (completed == boxes.length) {
            noLoop();
        }

    }

    public void mouseReleased() {
        boxes[0].x = random(20, 380);
        boxes[0].y = random(20, 180);
    }

    class Box {

        public float x, y, w, h, volume, attempts;
        String word;
        boolean frozen = true;
        int c;

        Box(String word, int w, int h) {
            this.word = word;
            this.x = cos(angle) * log(outwards) * 42 + 210;
            this.y = sin(angle) * log(outwards) * 21 + 105;
            angle -= 61;
            outwards += 0.4;
            this.w = w;
            this.h = h;
            this.volume = w * h;
            this.c = color(random(100, 255), random(100, 255), random(100, 255));
        }

        void render(int id) {
            /*    noFill();
            stroke(0,20);
            rect(x,y,w,h); */
            fill(c);
            textFont(font, h);
            text(tags[id], (int) (x - w / 2), (int) (y + h * baseline_ratio));
        }

        void collide(int i) {
            frozen = true;
            for (i += 1; i < boxes.length; i++) {
                float dx = boxes[i].x - this.x;
                float dy = boxes[i].y - this.y;
                float tx = boxes[i].w / 2 + this.w / 2;
                float ty = boxes[i].h / 2 + this.h / 2;
                if ((abs(dx) < tx) && (abs(dy) < ty)) {
                    float me = (float) 0.5 * boxes[i].volume / this.volume;
                    float you = (float) 0.5 * this.volume / boxes[i].volume;
                    attempts++;

                    frozen = false;
                    if (dx > 0) {
                        this.x -= me * 1.5;
                        boxes[i].x += you * 1.5;
                    } else {
                        this.x += me * 1.5;
                        boxes[i].x -= you * 1.5;
                    }

                    if (dy > 0) {
                        this.y -= me;
                        boxes[i].y += you;
                    } else {
                        this.y += me;
                        boxes[i].y -= you;
                    }

                    if (this.attempts > 500) {
                        this.x = random(0, width);
                        this.y = random(0, height);
                        this.attempts = 0;
                    }
                }
            }
        }
    }
}
