/* Rogue.java -- Rogue game for java */
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.net.*;
//import java.applet.*;
import java.util.*;
import java.io.*;

interface Header {
	static final int MIN_ROW	= 1;
	static final int AMULET_LEVEL= 26;
	static final int LAST_map = 99;

	// Values for the level map:
	static final int TOY		=    01;
	static final int MONSTER	=    02;
	static final int STAIRS		=    04;
	static final int HORWALL	=   010;
	static final int VERTWALL	=   020;
	static final int DOOR		=   040;
	static final int FLOOR		=  0100;
	static final int TUNNEL		=  0200;
	static final int TRAP		=  0400;
	static final int HIDDEN		= 01000;
	static final int MAN		= 02000;	// The rogue is here
	static final int HOLDER     = 04000;	// May contain trap/toy/monster
	static final int DARK		=010000;	// Dark place

	static final int DROPHERE	= (DOOR|FLOOR|TUNNEL|MAN|HOLDER|MONSTER);
	static final int SOMETHING  =  0777;

	static final int uNormal	= 0x000;	// Light gray
	static final int uWeak		= 0x100;	// Dark gray
	static final int uBlack		= 0x200;	// Black is invisible
	static final int uBrite		= 0x300;	// White
	static final int uRed		= 0x400;
	static final int uRogue		= 0x500;
	static final int uDarkRed	= 0x600;
	static final int uGreen		= 0x700;
}
class View extends Canvas implements java.io.Serializable{
	Rogue self;
	Message msg;
	Item dummy;
	Stack marked= new Stack();
	int pointsize;
	Level level;
	char terminal[][];
	char buffer[][];		// Low byte=ascii character, High byte=color index
	boolean line_dirty[];
	int nrow;
	int ncol;
	int cw;	/* Character width */
	int ch; /* Character height */
	int ca; /* Ascent */
	int lead;	/* Font leading */
	Font ffixed;
	FontMetrics fm;
	Man man= null;	/* From whose point of view this is */

	public View(Rogue self, int pointsize, int nrow, int ncol){
		this.self= self;
		this.pointsize= pointsize;
		this.level= level;
		this.nrow= nrow;
		this.ncol= ncol;
		this.msg= new Message(this);
		terminal= new char[nrow][ncol];
		buffer= new char[nrow][ncol];
		line_dirty= new boolean[nrow];

		for(int k= 0; k<nrow; k++){
			line_dirty[k]= false;
			for(int c= 0; c<ncol; c++)
				terminal[k][c]= ' ';
		}
		// Set up the view canvas
		Dimension d= preferredSize();
//System.out.println("d="+d);
		resize(d);
		requestFocus();
	}
	public Dimension preferredSize(){
		Dimension d= self.size();
		++pointsize;
		if(d.height>0)
			pointsize+= pointsize/2;
		do{
			--pointsize;
			ffixed= new Font("Courier", Font.PLAIN, pointsize);
			FontMetrics fm= getFontMetrics(ffixed);
			cw= fm.charWidth('X');
			ch= fm.getHeight();
			ca= fm.getAscent();
		}while(d.height>0 && pointsize>8 && ((nrow+1)*ch>d.height || (ncol+1)*cw>d.width));
///System.out.println("SIZE" + cw + " " + ch + " ascent="+ ca);
///System.out.println("--");
		return new Dimension(ncol * cw, nrow * ch);
	}
	boolean in_sight(int row, int col){
		return man.can_see(row, col);
	}
	static Color cmap[]= new Color[8];
	static {
		cmap[0]= Color.lightGray;
		cmap[1]= Color.gray;
		cmap[2]= Color.black;
		cmap[3]= Color.white;
		cmap[4]= Color.red;
		cmap[5]= Color.yellow;
		cmap[6]= new Color(128,0,0);	// Dark red
		cmap[7]= new Color(0,160,0);	// Green
	}
	private void inupdate(Graphics g){
		if(g != null){
			Font ft= g.getFont();
			g.setFont(ffixed);
			byte ba[]= new byte[ncol];
			for(int y= 0; y<nrow; y++)if(line_dirty[y]){
				line_dirty[y]= false;
				char ter[]= terminal[y];
				char buf[]= buffer[y];
				for(int x= 0; x<ncol; x++){
					ter[x]= buf[x];
					ba[0]= (byte)(buf[x]&127);
					int st= buf[x]>>8;
					if(ba[0]=='_' || ba[0]==0 || st==2)
						ba[0]= ' ';
					g.setColor(Color.black);
					g.fillRect(x*cw, y*ch, cw, ch);
					g.setColor(cmap[st]);
					g.drawBytes(ba, 0, 1, x*cw, y*ch+ca);
				}
			}
			g.setFont(ft);
		}
	}
	void mark(int r, int c){
		marked.push(new Rowcol(r, c));
	}
	void markall(){
		marked= new Stack();
		marked.push(new Rowcol(0, 0));
	}
	Rowcol getmark(){
		Rowcol pt;
		try{
			pt= (Rowcol)marked.pop();
		}catch (EmptyStackException e){
			pt= null;
		}
		return pt;
	}
	public void update(Graphics g){
		synchronized (self.gamer){
			for(int r= 0; r<nrow; r++){
				line_dirty[r]= true;
				char ter[]= terminal[r];
				for(int c= 0; c<ncol; c++)
					ter[c]= 0;
				inupdate(g);
			}
		}
	}
	public void paint(Graphics g) {
		update(g);
	}
	void refresh(){
		Graphics g= getGraphics();
		//assert (g != null);
		inupdate(g);
		//g.dispose();
	}
	void addch(int row, int col, char ch){
		if(row>=0 && row<nrow && col>=0 && col<ncol){
			if(ch!=buffer[row][col]){
				buffer[row][col]= ch;
				line_dirty[row]= true;
			}
		}
	}
	void addch(int row, int col, String s){
		if(row>=0 && row<nrow){
			int n= s.length();
			col += n;
			while(--n>=0){
				--col;
				if(col>=0 && col<ncol)
					buffer[row][col]= s.charAt(n);
			}
			line_dirty[row]= true;
		}
	}
	char charat(int row, int col){
		return buffer[row][col];
	}
	void empty(){
		for(int r= 0; r<nrow; r++){
			line_dirty[r]= true;
			for(int c= 0; c<ncol; c++){
				terminal[r][c]= 0;
				buffer[r][c]= ' ';
			}
		}
		refresh();
	}
}
public class Rogue extends Panel implements Runnable, Header, java.io.Serializable {
	Frame f;
	transient Thread gamer;
	Level level;
	Room endroom;
	int cur_level;
	int max_level;
	Vector view_list= new Vector();
	Vector flashers= new Vector();
	Randomx rand;
	String keybuf= "";
	long starttime;
	
	Id id_potions[]= null;
	Id id_scrolls[]= null;
	Id id_weapons[]= null;
	Id id_armors[]= null;
	Id id_wands[]= null;
	Id id_rings[]= null;

	int pointsize= 12;
	String scorepagename;
	boolean interrupted;

	public static void main(String[] args) {
		// Rogue r = new Rogue ();
		Rogue r = loadGame();
		if (r.f == null) {
			r.f = new Frame();
			// r.f.setSize(800,520);
			r.setPreferredSize(new Dimension(800, 500));

			r.f.setLayout(new FlowLayout());

			r.f.add(r);
			r.f.pack();
		}
		r.f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		r.f.invalidate();
		r.f.validate();
		r.f.repaint();
		r.f.pack();
		r.f.setVisible(true);
		r.f.validate();

	}
	/*
	public void windowClosing(WindowEvent e) {
		dispose();
		System.exit(0);
	}
	public void windowOpened(WindowEvent e)
	{ }
	public void windowIconified(WindowEvent e)
	{ }
	public void windowClosed(WindowEvent e) {
		dispose();
		System.exit(0);
	}
	public void windowDeiconified(WindowEvent e)
	{ }
	public void windowActivated(WindowEvent e)
	{ }
	public void windowDeactivated(WindowEvent e)
	{ }
	*/
	public Rogue(){
		try {
			//pointsize= Integer.parseInt(getParameter("pointsize"));
			pointsize = 16;
		}catch (NumberFormatException e){
			pointsize= 12;
		}
		rand= new Randomx((int)System.currentTimeMillis());
		try {
			//int i= Integer.parseInt(getParameter("srand"));
			//rand= new Randomx(i);
			rand= new Randomx();
		}catch (NumberFormatException e){
		}
		//scorepagename= getParameter("score");
		setBackground(Color.black);
		Monster m= new Monster();	// Force static definitions
		start();
	}
	public void start(){
		gamer= new Thread(this);
		gamer.start();
		repaint(30);
	}
	public void stop(){
		if(gamer!=null)
			gamer.stop();
		gamer= null;
	}
	boolean newlevel= true;
	
	void outputScores() {
		View view = (View) view_list.elementAt(0);
		view.empty();
			view.addch(4, 32, "__--HIGH SCORES---__");
			

		
		File scoreFile = new File("jrogue.scr");
		String[] topTen = new String[10];
		try {
			Scanner inStream = new Scanner(scoreFile);
			for (int i = 0; inStream.hasNextLine() && i < 10; i++) {
				topTen[i] = inStream.nextLine();
			}
			inStream.close();
		} catch (FileNotFoundException e) {
			System.out.println("No score file found.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			PrintStream outStream = new PrintStream(scoreFile);
			
			Man man = view.man;
			boolean currentScoreOutputted = false;
			for (int i = 0; i < topTen.length; i++) {
				if (!currentScoreOutputted && topTen[i] == null) {
					outStream.println(i+1 + " " + man.gold + " " + man.name() + " " + man.game_over_String);
					view.addch(5+i, 30, i+1 + " " + man.gold + " " + man.name() + " " + man.game_over_String);
					currentScoreOutputted = true;
				} else  if (!currentScoreOutputted && topTen[i] != null) {
					System.out.println(topTen[i]);
					String[] score = topTen[i].split("\\s+");
					for (int j = 4; j < score.length; j++) { // terrible hack, fix
						score[3] += " " + score[j];
					}
					if (!currentScoreOutputted && man.gold > Integer.parseInt(score[1])) {
						outStream.println(i+1 + " " + man.gold + " " + man.name() + " " + man.game_over_String);
						view.addch(5+i, 30, i+1 + " " + man.gold + " " + man.name() + " " + man.game_over_String);
						outStream.println(i+2 + " " + score[1] + " " +score[2]+ " "+score[3]);
						view.addch(6+i, 30, i+2 + " " + score[1] + " " +score[2]+ " "+score[3]);
						currentScoreOutputted = true;
					} else if (score.length >= 3){
						outStream.println(i+1 + " " + score[1] + " " +score[2]+ " "+score[3]);
						view.addch(5+i, 30, i+1 + " " + score[1] + " " +score[2]+ " "+score[3]);
					}
				} else if (topTen[i] != null) {
					String[] score = topTen[i].split("\\s+");
					for (int j = 4; j < score.length; j++) { // terrible hack, fix
						score[3] += " " + score[j];
					}
					outStream.println(i+2 + " " + score[1] + " " +score[2]+ " "+score[3]);
					view.addch(6+i, 30, i+2 + " " + score[1] + " " +score[2]+ " "+score[3]);
				}
			}
			outStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		view.addch(19, 33, "~---------~");
		view.addch(23, 8, "Press SPACE to exit");
		view.refresh();
		wait_for_ack();
	}
	void end_game() {
		if (!Man.saved_game) 
			outputScores();
		System.exit(0);
	}
	void begin_game(){
		System.out.println("Beginning new game");
		View view= null;
		//Id.list_items();
		if(view_list.size()==0){
			add(view= new View(this, pointsize, 25, 80));
			view_list.addElement(view);
			Man man= new Man(this, view);
			view.man= man;
		}else{
			view= (View)view_list.elementAt(0);
			Man man= view.man;
			Option oldop= man!=null? man.option : null;
			view.empty();
			view.man= man= new Man(this, view);
			man.option= oldop;
		}
		System.gc();
		view.requestFocus();
		Id.mix_colors(rand);
		Id.make_scroll_titles(rand);
		// Level.cur_level= 0;
		cur_level= 0;
		// Level.max_level= 0;
		max_level= 0;
		Date d= new Date();
		starttime= d.getTime();
	}
	public void run(){
		System.out.println("running");
		Man man;
		Enumeration e;
		gamer.setPriority(Thread.MIN_PRIORITY);
		for(;;){
			System.out.println("in main game loop");
			if(newlevel){
				System.out.println("creating new level");
				if(view_list.size()==0)
					begin_game();
				interrupted= false;
				level= new NineRoom(25, 80, this);
				level.put_monsters();
				e= view_list.elements();
				while(e.hasMoreElements()){
					View v= (View)e.nextElement();
					man= v.man;
					man.level= level;
					if(man.pack==null)
						man.player_init();
					if(!man.has_amulet() && (level.cur_level >= Level.AMULET_LEVEL)){
						Rowcol pt= level.gr_row_col(Level.FLOOR|Level.TUNNEL, null);
						if(pt!=null){
							Toy amulet= new Toy(level, Id.AMULET);
							amulet.place_at(pt.row, pt.col, TOY);
						}
					}
					v.level= level;
					v.empty();
					v.man.pack.relevel(level);
					level.put_player(v.man);
				}
				newlevel= false;
				f.revalidate();
			}
			e= view_list.elements();
			while(e.hasMoreElements()){
				View v= (View)e.nextElement();
				v.man.init_seen();
				v.man.print_stat();
				v.refresh();
			}
			repaint();
////			view.refresh();
			man= (Man)((View)view_list.elementAt(0)).man;
			man.play_level();
			if(man.game_over){
				md_slurp();
				end_game();
			}
			newlevel= true;
		}

	}
	public void paint(Graphics g){
		Enumeration e;
		int y= 0;
		e= view_list.elements();
		while(e.hasMoreElements()){
			View v= (View)e.nextElement();
			Dimension s= size();
			Dimension d= v.size();
			v.move((s.width-d.width)/2, y+v.ch);
			v.repaint();
			y += d.height+2*v.ch;
		}
	}
	public String getAppletInfo() {
		return "My Rogue";
	}
	public boolean mouseDown(Event evt, int x, int y) {
		return true;
	}
	public boolean mouseUp(Event evt, int x, int y) {
		return true;
	}
	synchronized public boolean keyDown(Event evt, int key) {
		if(key=='\033')
			interrupted= true;
		if(!gamer.isAlive()){
			if(key==' ')
					start();
		}else
			keybuf= keybuf+((char)key);
		notify();
		return true;
	}
	synchronized void md_sleep(int mseconds){
		if(mseconds>0){
			try {
				Thread.sleep(mseconds);
			} catch (InterruptedException e){};
		}
		keybuf= "";
	}
	synchronized void md_slurp(){
		keybuf= "";
	}
	synchronized int md_getchar(){
		while(keybuf==null || keybuf.length()==0){
			try{
				 wait();
			}catch(InterruptedException e){
				interrupted= true;
				return '\033';
			}
		}
		int key= (int)keybuf.charAt(0);
		keybuf= keybuf.substring(1);
		return key;
	}
	int rgetchar(){
		return md_getchar();
	}
	void wait_for_ack(){
		int c;
		do c= rgetchar();
		while(c!=' ' && c!='\033');
	}
	void flashadd(int row, int col, int color){
		int ia[]= new int[3];
		ia[0]= row; ia[1]= col; ia[2]= color;
		flashers.addElement(ia);
	}
	void xflash(){
		if(flashers.size()>0){
			boolean bseen= false;
			Enumeration e= view_list.elements();
			Vector chsave= new Vector(flashers.size());

			while(e.hasMoreElements()){
				View v= (View)e.nextElement();
				Enumeration f= flashers.elements();
				boolean vseen= false;
				while(f.hasMoreElements()){
					int ia[]= (int [])f.nextElement();
					if(v.in_sight(ia[0], ia[1])){
						int ch= v.terminal[ia[0]][ia[1]];
						chsave.addElement(new Character((char)ch));
						ch &= 255;
						if(ch=='.')
							ch= '*';
						ch= (ch&255)|ia[2];
						v.addch(ia[0], ia[1], (char)ch);
						vseen= true;
					}
				}
				if(vseen){
					v.refresh();
					bseen= true;
					md_sleep(120);
				}
			}
			e= view_list.elements();
			if(bseen)while(e.hasMoreElements()){
				View v= (View)e.nextElement();
				Enumeration f= flashers.elements();
				Enumeration c= chsave.elements();
				while(f.hasMoreElements()){
					int ia[]= (int [])f.nextElement();
					if(v.in_sight(ia[0], ia[1]))
						v.addch(ia[0], ia[1], ((Character)c.nextElement()).charValue());
					v.mark(ia[0], ia[1]);
				}
			}
			flashers= new Vector();
		}
	}
	void vflash(int r, int c, char ch){
		boolean bseen= false;

		Enumeration e= view_list.elements();
		while(e.hasMoreElements()){
			View v= (View)e.nextElement();
			if(v.in_sight(r, c)){
				bseen= true;
				v.addch(r, c, ch);
			}
		}
		if(bseen){
			refresh();
			md_sleep(50);
			ch= level.get_char(r, c);
			e= view_list.elements();
			while(e.hasMoreElements()){
				View v= (View)e.nextElement();
				v.addch(r, c, ch);
			}
		}
	}
	void tell(Persona p, String s, boolean bintr){
		Enumeration e= view_list.elements();
		while(e.hasMoreElements()){
			View v= (View)e.nextElement();
			if(v.man==p){
				String ss= whoify(p, s);
				v.msg.message(ss, bintr);
			}
		}
		xflash();
	}
	boolean describe(Rowcol rc, String s, boolean bintr){
		Enumeration e= view_list.elements();
		while(e.hasMoreElements()){
			View v= (View)e.nextElement();
			if(v.in_sight(rc.row, rc.col)){
				String ss= whoify(v.man, s);
				v.msg.message(ss, bintr);
				return true;
			}
		}
		xflash();
		return false;
	}
	void check_message(Persona p){
		Enumeration e= view_list.elements();
		while(e.hasMoreElements()){
			View v= (View)e.nextElement();
			if(v.man==p)
				v.msg.check_message();
		}
	}
	void refresh(){
		Enumeration e= view_list.elements();
		while(e.hasMoreElements()){
			View v= (View)e.nextElement();
			v.refresh();
		}
	}
	void vset(int r, int c){
		Enumeration e= view_list.elements();
		while(e.hasMoreElements()){
			View v= (View)e.nextElement();
			char ch= v.charat(r, c);
			v.addch(r,c,ch);
		}
	}
	void mark(int r, int c){
		Enumeration e= view_list.elements();
		while(e.hasMoreElements()){
			View v= (View)e.nextElement();
			v.mark(r,c);
		}
	}
	void markall(){
		Enumeration e= view_list.elements();
		while(e.hasMoreElements()){
			View v= (View)e.nextElement();
			v.markall();
		}
	}
	String whoify(Persona p, String src){
		String dst= "";
		int i= 0;
		int j;
		try{
		while((j= src.indexOf('@', i)) >= 0){
			dst += src.substring(i, j);
			boolean hasverb= src.charAt(++j) == '>';
			i= j+1;
			j= src.indexOf(hasverb? '+' : '>', i);
			boolean byou= false;
			String name= src.substring(i, j);
			if(name.equals(p.name())){
				dst += "you";
				byou= true;
			}else
				dst += "the " + name;
			if(hasverb){
				i= j+1;
				dst += " ";
				j= src.indexOf('+', i);
				if(byou){
					dst += src.substring(i, j);
					i= src.indexOf('<', j);
				}else{
					i= src.indexOf('<', j);
					dst += src.substring(j+1, i);
				}
			}else
				i= j;
			++i;
		}} catch(Exception e){
			System.out.println("whoify error on " + p.name());
			System.out.println(src+"\n"+dst);
		}
		dst += src.substring(i);
		return dst;
	}
	void pullIds() {
		id_potions= Id.id_potions;
		id_scrolls= Id.id_scrolls;
		id_weapons= Id.id_weapons;
		id_armors= Id.id_armors;
		id_wands= Id.id_wands;
		id_rings= Id.id_rings;
	}
	void pushIds() {
		Id.id_potions = id_potions;
		Id.id_scrolls = id_scrolls;
		Id.id_weapons = id_weapons;
		Id.id_armors = id_armors;
		Id.id_wands = id_wands;
		Id.id_rings = id_rings;
	}
	static Rogue loadGame() {
		Rogue r = null;
		try
        {
          
            // Reading the object from a file 
			File file = new File("rogue.ser");
            FileInputStream fileStream = new FileInputStream(file); 
            ObjectInputStream in = new ObjectInputStream(fileStream); 
              
            // Method for deserialization of object 
            try {
            	r = (Rogue)in.readObject();
        		//Id.mix_colors(new Randomx(r.potionSeed));
        		//Id.make_scroll_titles(new Randomx(r.scrollSeed));
            	r.pushIds();
            } catch (InvalidClassException e) {
            	// This is thrown when the code is updated and invalidates 
            	// saved games from previous versions.
            	System.err.println("Saved game is from a previous versions.  Starting new game.");
            	r = new Rogue ();
            }
            in.close(); 
            fileStream.close(); 
            file.delete();
              
            System.out.println("Object has been deserialized "); 
            Man.justLoaded = 2;
            r.start();
        }
        catch (Exception e)
        {
        	System.err.println("Couldn't load saved game.  Starting new game.");
            r = new Rogue ();
        }
        
        return r;
	}
}
