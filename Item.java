import java.util.Vector;
import java.util.Enumeration;

class Rowcol implements java.io.Serializable {
	int row;
	int col;
	Rowcol(int r, int c){
		row= r;
		col= c;
	}
	Rowcol(){
		this(0,0);
	}
	public String toString(){
		return '[' + Integer.toString(row) + ' ' + Integer.toString(col) + ']';
	}
}
public class Item extends Rowcol implements Header, java.io.Serializable{
	char ichar;				/* 'A' is for aquatar */
	Level level;

	Item(){
		level= null;
	}
	Item(Level level, int r, int c){
		this.level= level;
		row= r;
		col= c;
		ichar= '?';
	}
	Item(Level level){
		this(level, 0,0);
	}
	void place_at(int r, int c, int what){
		Vector list= null;
		switch(what){
		case TOY:		list= level.level_toys; break;
		case MONSTER:	list= level.level_monsters; break;
		case MAN:		list= level.level_men; break;
		case TRAP:		list= level.level_traps; break;
		case DOOR:		list= level.level_doors; break;
		}
		if(list != null){
			if(list.contains(this)){
				level.mark(row, col);
				level.map[row][col] &= ~what;
			}else if(r>=0)
				list.addElement(this);
		}
		row= r;
		col= c;
		if(r>0){
			level.mark(r, c);
			level.map[r][c] |= what;
		}else if(list != null)
			list.removeElement(this);
	}
	public String toString(){
		return super.toString() + Integer.toString(ichar>>8) + ((char)(ichar&255)) + " ";
	}
}
class ItemVector extends Vector implements java.io.Serializable {
	ItemVector(){
		super();
	}
	ItemVector(int n){
		super(n);
	}
	void relevel(Level level){
		Enumeration e= elements();
		while(e.hasMoreElements())
			((Item)e.nextElement()).level= level;
	}
	Item item_at(int row, int col){
		int i= size();
		while(--i>=0){
			Item p= (Item)elementAt(i);
			if(p.row==row && p.col==col)
				return p;
		}
		return null;
	}
	Item get_letter_toy(int ch){	// Call on the rogue's pack
		int i= size();
		while(--i>=0){
			Item p= (Item)elementAt(i);
				if(p.ichar == ch)
					return p;
		}
		return null;
	}
	int inventory(int mask, Message msg, boolean ask){
		int i= size();
		String descs[];
		descs= new String[1];
		descs[0]= "--";

		if(i==0){
			msg.message("your pack is empty");
			return '\033';
		}
		int n= 0;
		Enumeration e= elements();
		while(e.hasMoreElements()){
			Toy obj= (Toy)e.nextElement();
			if(0 != (obj.kind & mask))
				++n;
		}
		if(n>0){
			descs= new String[n];
			n= 0;
			e= elements();
			while(e.hasMoreElements()){
				Toy obj= (Toy)e.nextElement();
				if(0 != (obj.kind & mask)){
					int k= obj.ichar>='a' && obj.ichar<='z'? obj.ichar:n;
					descs[n++]= single_inv(k);
				}
			}
		}
		if(n==0){
 			descs= new String[1];
			descs[0]= "--nothing appropriate--";
		}
		return msg.rightlist(descs, ask);
	}
	String single_inv(int ch){
		if(ch<'a')
			ch += 'a';
		Enumeration e= elements();
		Toy obj= null;
		while(e.hasMoreElements()){
			obj= (Toy)e.nextElement();
			if(obj.ichar==ch)
				break;
		}
		if(obj==null)
			return "";
		String sep= ") ";
		if(0 !=(obj.kind & Id.ARMOR) && obj.is_protected)
			sep= "} ";
		return " " + (char)obj.ichar + sep + obj.get_desc();
	}
	boolean mask_pack(int mask){
		int i= size();
		while(--i>=0){
			Toy t= (Toy)elementAt(i);
			if(0 != (t.kind & mask))
				return true;
		}
		return false;
	}
	char next_avail_ichar(){
		int i;
		boolean ichars[]= new boolean[26];

		for(i= 0; i < 26; i++)
			ichars[i]= false;
		i= size();
		while(--i>=0){
			Toy obj= (Toy)elementAt(i);
			int k= obj.ichar - 'a';
			if(k>=0 && k<26)
				ichars[k]= true;
		}
		for(i= 0; i < 26; i++)
			if(!ichars[i])
				return (char)(i + 'a');
		return '?';
	}
	void uncurse_all(){
		Enumeration e= elements();
		while(e.hasMoreElements())
			((Toy)e.nextElement()).is_cursed= false;
	}
}
class Door extends Item implements java.io.Serializable {
	// This is actually a directional passage (used by the monster's brain)
	// A monster entering at row,col wants to go to oth
	// passageto is null when it does not connect to another room
	// Monsters prefer passages that do connect to other rooms
	Rowcol oth;
	Door passageto= null;

	Door(Level level, int r, int c, int or, int oc){
		super(level,r,c);
		ichar= '+';
		place_at(r, c, DOOR);
		oth= new Rowcol(or, oc);
	}
	Rowcol porch(){
		return level.porch(row, col);
	}
	Rowcol foyer(){
		return level.foyer(row, col);
	}
	Room other_room(){
		if(0==(level.map[row][col] & level.DOOR))
			return null;
		return level.room_at(row, col);
	}
	void connect(Door dto){
		passageto= dto;
		dto.passageto= this;
	}
	public String toString(){
		return super.toString() + oth + passageto==null? " to void":" to room";
	}
}
class Trap extends Item implements java.io.Serializable {
	int kind;
	static final int TRAP_DOOR=	0;
	static final int BEAR_TRAP=	1;
	static final int TELE_TRAP=	2;
	static final int DART_TRAP=	3;
	static final int SLEEPING_GAS_TRAP=	4;
	static final int RUST_TRAP=	5;
	static final int TRAPS=	6;
	
	static String msg[]= {
		"$ fell down a trap",
		"$are caught in a bear trap",
		"teleport",
		"a small dart just hit $ in the shoulder",
		"a strange white mist envelops $ and $fall asleep",
		"a gush of water hits $ on the head"
	};
	static String name[]= {
		"trap door",
		"bear trap",
		"teleport trap",
		"poison dart trap",
		"sleeping gas trap",
		"rust trap",
	};
	Trap(Level level, int r, int c, int kind){
		super(level, r, c);
		this.kind= kind;
		place_at(r, c, TRAP);
	}
	String trap_message(Persona p){
		String src= msg[kind];
		String dst= "";
		int i= 0;
		int j;
		try {
		while((j= src.indexOf('$', i)) >=0){
			dst += src.substring(i,j);
			i= j+1;
			if(src.charAt(i)!=' '){
				dst += "@>"+p.name() + '+';
				if(src.charAt(i)=='a'){
					dst += "are+is<";
					i += 3;
				}else{
					j= src.indexOf(' ', j);
					dst += src.substring(i,j) + '+' + src.substring(i,j)+'<';
					i= j;
				}
			}else
				dst += "@<"+p.name()+">";
		}} catch(Exception e){
			System.out.println("trap_message("+p.name()+")\n\t"+src+'\n'+dst);
		}
		dst += src.substring(i);
		return dst;
	}
}
