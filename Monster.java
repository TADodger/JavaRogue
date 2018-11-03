import java.util.Vector;
import java.util.Enumeration;
// Hate error happens after zap hits emu

// Check drain life hitting monsters (in level.java)
// Does permute screw up on 0 or 1 elt?

class Montype implements java.io.Serializable {
	String m_name;
	int m_flags;
	String m_damage;

	char ichar;
	int exp_points;
	int first_level;
	int last_level;
	int m_hit_chance;
	int stationary_damage;
	int drop_percent;
	int hp_current;

	Montype(String m_name, int m_flags, String m_damage, int hp_current,
	  char ichar, int exp_points, int first_level, int last_level,
	  int m_hit_chance, int stationary_damage, int drop_percent){
		this.m_name= m_name;
		this.m_flags= m_flags;
		this.m_damage= m_damage;
		this.hp_current= hp_current;
		this.ichar= ichar;
		this.exp_points= exp_points;
		this.first_level= first_level;
		this.last_level= last_level;
		this.m_hit_chance= m_hit_chance;
		this.stationary_damage= stationary_damage;
		this.drop_percent= drop_percent;
	}
}
class Monster extends Persona implements java.io.Serializable{
	int exp_points;
	int m_hit_chance;
	int stationary_damage;
	int drop_percent;
	boolean slowed_toggle;
	int nap_length;
	int disguise;
	int trow, tcol;			/* Target */
	int o_row, o_col, stuck;	/* stuck is how many times stuck at o_row, o_col */
	int dstrow, dstcol;		/* Default target (where the rogue is) */

	/* m_flags values */
	static final int HASTED=			    01;
	static final int SLOWED=			    02;
	static final int INVISIBLE=		        04;
	static final int ASLEEP=		       010;
	static final int WAKENS=		       020;
	static final int WANDERS=		       040;
	static final int FLIES=		          0100;
	static final int FLITS=		          0200;
	static final int CAN_FLIT=	      	  0400;	/* can, but usually doesn't, flit */
	static final int CONFUSED=	     	 01000;
	static final int RUSTS=		     	 02000;
	static final int HOLDS=		      	 04000;
	static final int FREEZES=		    010000;
	static final int STEALS_GOLD=	    020000;
	static final int STEALS_ITEM=	    040000;
	static final int STINGS=		   0100000;
	static final int DRAINS_LIFE=	   0200000;
	static final int DROPS_LEVEL=	   0400000;
	static final int SEEKS_GOLD=	  01000000;
	static final int FREEZING_ROGUE=  02000000;
	static final int RUST_VANISHED=	  04000000;
	static final int CONFUSES=		 010000000;
	static final int IMITATES=		 020000000;
	static final int FLAMES=		 040000000;
	static final int STATIONARY=	0100000000;	/* damage will be 1,2,3,... */
	static final int NAPPING=		0200000000;	/* can't wake up for a while */
	static final int ALREADY_MOVED=	0400000000;
	static final int SPECIAL_HIT=(RUSTS|HOLDS|FREEZES|STEALS_GOLD|STEALS_ITEM|STINGS|DRAINS_LIFE|DROPS_LEVEL);

	static final int WAKE_PERCENT= 45;
	static final int FLIT_PERCENT= 40;
	static final int PARTY_WAKE_PERCENT= 75;
	static final int STEALTH_FACTOR= 3;

	static final int HYPOTHERMIA= 1;
	static final int STARVATION= 2;
	static final int POISON_DART= 3;
	static final int QUIT= 4;
	static final int WIN= 5;
	static final int KFIRE= 6;
	static final int MONSTERS= 26+1;

	static final Montype mon_tab[]= new Montype[MONSTERS];
	static {
		mon_tab[ 0]= new Montype("aquator", (ASLEEP|WAKENS|WANDERS|RUSTS),
			"0d0", 25, 'A', 20, 9, 18, 100,  0, 0);
		mon_tab[ 1]= new Montype("bat", (ASLEEP|WANDERS|FLITS|FLIES),
			"1d3", 10, 'B', 2, 1, 8, 60, 0, 0);
		mon_tab[ 2]= new Montype("centaur", (ASLEEP|WANDERS),
			"3d3/2d5", 32, 'C', 15, 7, 16, 85, 0, 10);
		mon_tab[ 3]= new Montype("dragon", (ASLEEP|WAKENS|FLAMES),
			"4d6/4d9", 145, 'D', 5000, 21, 126, 100, 0, 90);
		mon_tab[ 4]= new Montype("emu", (ASLEEP|WAKENS),
			"1d3", 11, 'E', 2, 1, 7, 65, 0, 0);
		mon_tab[ 5]= new Montype("venus fly-trap", (HOLDS|STATIONARY),
			"5d5", 73, 'F', 91, 12, 126, 80, 0, 0);
		mon_tab[ 6]= new Montype("griffin", (ASLEEP|WAKENS|WANDERS|FLIES),
			"5d5/5d5", 115, 'G', 2000, 20, 126, 85, 0, 10);
		mon_tab[ 7]= new Montype("hobgoblin", (ASLEEP|WAKENS|WANDERS),
			"1d3/1d2", 15, 'H', 3, 1, 10, 67, 0, 0);
		mon_tab[ 8]= new Montype("ice monster", (ASLEEP|FREEZES),
			"0d0", 15, 'I', 5, 2, 11, 68, 0, 0);
		mon_tab[ 9]= new Montype("jabberwock", (ASLEEP|WANDERS),
			"3d10/4d5", 132, 'J', 3000, 21, 126, 100, 0, 0);
		mon_tab[10]= new Montype("kestrel", (ASLEEP|WAKENS|WANDERS|FLIES),
			"1d4", 10, 'K', 2, 1, 6, 60, 0, 0);
		mon_tab[11]= new Montype("leprechaun", (ASLEEP|STEALS_GOLD),
			"0d0", 25, 'L', 21, 6, 16, 75, 0, 0);
		mon_tab[12]= new Montype("medusa", (ASLEEP|WAKENS|WANDERS|CONFUSES),
			"4d4/3d7", 97, 'M', 250, 18, 126, 85, 0, 25);
		mon_tab[13]= new Montype("nymph", (ASLEEP|STEALS_ITEM),
			"0d0", 25, 'N', 39, 10, 19, 75, 0, 100);
		mon_tab[14]= new Montype("orc", (ASLEEP|WANDERS|WAKENS|SEEKS_GOLD),
			"1d6", 25, 'O', 5, 4, 13, 70, 0, 10);
		mon_tab[15]= new Montype("phantom", (ASLEEP|INVISIBLE|WANDERS|FLITS),
			"5d4", 76, 'P', 120, 15, 24, 80, 0, 50);
		mon_tab[16]= new Montype("quagga", (ASLEEP|WAKENS|WANDERS),
			"3d5", 30, 'Q', 20, 8, 17, 78, 0, 20);
		mon_tab[17]= new Montype("rattlesnake", (ASLEEP|WAKENS|WANDERS|STINGS),
			"2d5", 19, 'R', 10, 3, 12, 70, 0, 0);
		mon_tab[18]= new Montype("snake", (ASLEEP|WAKENS|WANDERS),
			"1d3", 8, 'S', 2, 1, 9, 50, 0, 0);
		mon_tab[19]= new Montype("troll", (ASLEEP|WAKENS|WANDERS),
			"4d6/1d4", 75, 'T', 125, 13, 22, 75, 0, 33);
		mon_tab[20]= new Montype("black unicorn", (ASLEEP|WAKENS|WANDERS),
			"4d10", 90, 'U', 200, 17, 26, 85, 0, 33);
		mon_tab[21]= new Montype("vampire", (ASLEEP|WAKENS|WANDERS|DRAINS_LIFE),
			"1d14/1d4", 55, 'V', 350, 19, 126, 85, 0, 18);
		mon_tab[22]= new Montype("wraith", (ASLEEP|WANDERS|DROPS_LEVEL),
			"2d8", 45, 'W', 55, 14, 23, 75, 0, 0);
		mon_tab[23]= new Montype("xeroc", (ASLEEP|IMITATES),
			"4d6", 42, 'X', 110, 16, 25, 75, 0, 0);
		mon_tab[24]= new Montype("yeti", (ASLEEP|WANDERS),
			"3d6", 35, 'Y', 50, 11, 20, 80, 0, 20);
		mon_tab[25]= new Montype("zombie", (ASLEEP|WAKENS|WANDERS),
			"1d7", 21, 'Z', 8, 5, 14, 69, 0, 0);
		mon_tab[26]= new Montype("rogue", 0,
			"1d7", 12, '@', 1, -1, -1, 90, 0, 0);
	}
	Monster(){	// Undefined monster
		super((Level)null);
	}
	Monster(Level level, int k){	// The kth monster
		super(level);
		mt= mon_tab[k];
		m_flags= mt.m_flags;
		hp_max= hp_current= mt.hp_current;
		ichar= mt.ichar;
		exp_points= mt.exp_points;
		m_hit_chance= mt.m_hit_chance;
		stationary_damage= mt.stationary_damage;
		drop_percent= mt.drop_percent;
		set_hated();
	}
	void zap_monster(Man man, int kind){
		switch(kind){
		case Id.SLOW_MONSTER:
			if(0 != (m_flags & HASTED)){
				m_flags &= (~HASTED);
			} else {
				slowed_toggle= false;
				m_flags |= SLOWED;
			}
			break;
		case Id.HASTE_MONSTER:
			if(0 != (m_flags & SLOWED)){
				m_flags &= (~SLOWED);
			} else {
				m_flags |= HASTED;
			}
			break;
		case Id.TELE_AWAY:
			if(0 != (m_flags & HOLDS))
				man.being_held= false;
			tele();
			break;
		case Id.INVISIBILITY:
			m_flags |= INVISIBLE;
			break;
		case Id.POLYMORPH:
			if(0!=(m_flags & HOLDS))
				man.being_held= false;
			Monster mnew= level.gr_monster();
			if(mnew != null){
				die();
				mnew.put_m_at(row, col);
				if(0!=(mnew.m_flags & IMITATES))
					mnew.wake_up();
			} break;
		case Id.MAGIC_MISSILE:
			man.rogue_hit(this, true);
			break;
		case Id.CANCELLATION:
			if(0!=(m_flags & HOLDS))
				man.being_held= false;
			if(0!=(m_flags & STEALS_ITEM))
				drop_percent= 0;
			m_flags &= (~(FLIES | FLITS | SPECIAL_HIT | INVISIBLE |
				FLAMES | IMITATES | CONFUSES | SEEKS_GOLD | HOLDS));
			break;
		case Id.DO_NOTHING:
			man.tell("nothing happens");
			break;
		}
	}
	boolean zapt(Toy obj){
		String s= obj.kind==Id.FIRE? "fire" : "ice";
		boolean doend= true;
		int dmg= 0;
		wake_up();
		if(self.rand.percent(33))
			tell("the "+s+" misses " + who());
		else
			doend= false;
		if(obj.kind==Id.FIRE){
			if(0==(m_flags & RUSTS)){
				if(0!=(m_flags & FREEZES)){
					dmg= hp_current;
				} else if(0!=(m_flags & FLAMES)){
					dmg= (hp_current / 10) + 1;
				} else {
					dmg= self.rand.get((obj.owner.hp_current / 3), obj.owner.hp_max);
				}
			} else {
				dmg= (hp_current / 2) + 1;
			}
			tell("the "+ s + " hits " + who());
			damage(obj.owner, dmg, 0);
		} else {
			dmg= -1;
			if(0==(m_flags & FREEZES)){
				if(self.rand.percent(33)){
					tell("the " + name() + " is frozen");
					m_flags |= (ASLEEP | NAPPING);
					nap_length= self.rand.get(3, 6);
				} else {
					dmg= obj.owner.hp_current / 4;
				}
			} else {
				dmg= -2;
			}
			if(dmg != -1){
				tell("the " + s +" hits " + who());
				damage(obj.owner, dmg, 0);
			}
		}
		return doend;
	}
	boolean throw_at_monster(Toy missile, Persona man){
		int hit_chance= man.get_hit_chance(missile);
		int dmg= man.get_weapon_damage(missile);
		if(missile.kind==Id.ARROW && man.weapon!=null && man.weapon.kind==Id.BOW){
			dmg += man.get_weapon_damage(man.weapon);
			dmg= dmg * 2 / 3;
			hit_chance += hit_chance / 3;
		} else if(0!=(missile.in_use_flags & Id.BEING_WIELDED)
		&& (missile.kind == Id.DAGGER
		 || missile.kind == Id.SHURIKEN
		 || missile.kind == Id.DART)){
			dmg= dmg * 3 / 2;
			hit_chance += hit_chance / 3;
		}
		int t= missile.quantity;
		missile.quantity= 1;
		man.hit_message += "the " + missile.name();
		missile.quantity= t;

		if(!self.rand.percent(hit_chance)){
			man.hit_message += "misses  ";
			return false;
		}
		s_con_mon(man);
		man.hit_message +=  "hit  ";
		damage(man, dmg, 0);
		return true;
	}
	void put_m_at(int r, int c){
		place_at(r, c, MONSTER);
		aim_monster();
	}
	boolean mtry(int row, int col){
		if(!mon_can_go(row, col))
			return false;
		move_mon_to(row, col);
		return true;
	}
	void aim_monster(){
		Vector v= new Vector(4);
		Enumeration e= level.level_doors.elements();
		while(e.hasMoreElements()){
			Door dr= (Door)e.nextElement();
			if(dr.passageto!=null && mon_sees(dr.row, dr.col))
				v.addElement(dr);
		}
		if(v.size()>0){
			Door dr= (Door)v.elementAt(self.rand.get(v.size()-1));
			trow= dr.row;
			tcol= dr.col;
		}
	}
	boolean move_confused(){
		if(0==(m_flags & ASLEEP)){
			if(--confused <= 0)
				m_flags &= ~CONFUSED;
			if(0!=(m_flags & STATIONARY))
				return self.rand.coin();
			if(self.rand.percent(15))
				return true;
			int perm[]= self.rand.permute(9);
			for(int i= 0; i<9; i++){
				int c= Id.xtab[perm[i]]+col;
				int r= Id.ytab[perm[i]]+row;
				if(0!=(level.map[r][c]&MAN))
					return false;
				if(mtry(r, c))
					return true;
			}
		}
		return false;
	}
	boolean flit(){
		if(!self.rand.percent(FLIT_PERCENT + (0!=(m_flags & FLIES) ? 20 : 0)))
			return false;
		if(self.rand.percent(10))
			return true;
		int perm[]= self.rand.permute(9);
		for(int i= 0; i<9; i++){
			int c= Id.xtab[perm[i]]+col;
			int r= Id.ytab[perm[i]]+row;

			if(0==(level.map[r][c] & MAN)){
				if(mtry(r, c))
					return true;
			}
		}
		return true;
	}
	int gmc(Man man){
		if((!(man.detect_monster || man.see_invisible || man.r_see_invisible)
		    && 0!=(m_flags & INVISIBLE))
		|| man.blind>0)
			return level.get_char(row, col);
		if(man.halluc>0)
			return self.rand.get('A','Z');
		if(0!=(m_flags & IMITATES))
			return disguise | color();
		return ichar | color();
	}
	boolean rogue_is_around(){
		for(int r= -1; r<=1; r++)
		for(int c= -1; c<=1; c++)
			if(0!=(level.map[row+r][col+c] & MAN)){
				set_hated();
				if(ihate==null)
					ihate= (Man)level.level_men.item_at(row+r,col+c);
				return true;
			}
		return false;
	}
	void mv_monster(){
		int rwas= row;
		int cwas= col;

  		if(ihate==null)
			set_hated();
		if(ihate!=null)
			mv_to(ihate.row, ihate.col);
		else
			mv_to(trow, tcol);
		reg_move();
		if(row!=rwas || col!=cwas){
			/* It really did move */
			if(0!=(level.map[row][col]&TRAP))
				trap_player();
		}
	}
	void mv_to(int r, int c){
		int i, n;
		if(0!=(m_flags & ASLEEP)){
			if(0!=(m_flags & NAPPING)){
				if(--nap_length <= 0)
					m_flags &= ~(NAPPING | ASLEEP);
				return;
			}
			if(0!=(m_flags & WAKENS)
			&& rogue_is_around()
			&& ihate!=null
			&& self.rand.percent(ihate.stealthy > 0
				 ? WAKE_PERCENT/(STEALTH_FACTOR+ihate.stealthy)
				 : WAKE_PERCENT))
				wake_up();
			return;
		} else if(0!=(m_flags & ALREADY_MOVED)){
			m_flags &= ~ALREADY_MOVED;
			return;
		}
		if(0!=(m_flags & FLITS) && flit())
			return;
		// This is the general stick-close behavior:
		if(0!=(m_flags & STATIONARY) && ihate!=null && !mon_can_go(ihate.row, ihate.col))
			return;
		if(0!=(m_flags & FREEZING_ROGUE))
			return;
		if(0!=(m_flags & CONFUSES) && ihate!=null && ihate.m_confuse(this))
			return;
		if(ihate!=null && mon_can_go(ihate.row, ihate.col)){
			mon_hit(ihate);
			return;
		}
		if(0!=(m_flags & FLAMES) && ihate!=null && flame_broil(ihate))
			return;
		if(0!=(m_flags & SEEKS_GOLD) && level.seek_gold(this))
			return;
		if(trow == row && tcol == col)
			trow= -1;
		else if(trow != -1){
			r= trow;
			c= tcol;
		}
		if(row>r)		r= row - 1;
		else if(row<r)	r= row + 1;
		if(0!=(level.map[r][col]&DOOR) && mtry(r, col))
			return;
		if(col>c)		c= col - 1;
		else if(col<c)	c= col + 1;
		if(0!=(level.map[row][c]&DOOR) && mtry(row, c))
			return;
		if(mtry(r, c))
			return;
		int perm[]= self.rand.permute(6);
		for(i= 0; i<6; i++){
			switch(perm[i]){
			case 0:	if(mtry(r, col-1)) i= 6; break;
			case 1:	if(mtry(r, col)) i= 6; break;
			case 2: if(mtry(r, col+1)) i= 6; break;
			case 3:	if(mtry(row-1, c))i= 6; break;
			case 4: if(mtry(row, c)) i= 6; break;
			case 5:	if(mtry(row+1, c)) i= 6; break;
			}
		}
		if(row==o_row && col==o_col){
			if(++stuck > 4){
				if(trow == -1 && (ihate==null || !mon_sees(ihate.row, ihate.col))){
					set_hated();
					trow= self.rand.get(1, level.nrow - 2);
					tcol= self.rand.get(level.ncol - 1);
				} else {
					trow= -1;
					stuck= 0;
				}
			}
		} else {
			o_row= row;
			o_col= col;
			stuck= 0;
		}
	}
	void set_hated(){
		Persona hated= ihate;
		int dmin= 10000;
		int j= level.level_men.size();
		while(--j>=0){
			Man m= (Man)level.level_men.elementAt(j);
			if(mon_sees(m.row, m.col)){
				int dr= m.row-row;
				int dc= m.col-col;
				int d= dr*dr + dc*dc;
				if(d<dmin){
					dmin= d;
					hated= m;
				}
			}
		}
		if(hated!=ihate){
			if(ihate!=null && 0!=(m_flags & HOLDS))
				ihate.being_held= false;
			ihate= hated;
		}
	}
	void move_mon_to(int r, int c){
		boolean inroom= 0!=(level.map[row][col]&HOLDER);
		place_at(r, c, MONSTER);
		if(0!=(level.map[r][c]&DOOR)){
			trow= -1;
			set_hated();
			if(ihate==null)
				dr_course(!inroom);
		}
	}
	boolean mon_can_go(int r, int c){
		int dr= row - r;	/* check if move distance > 1 */
		if(dr>=2 || dr<=-2)
			return false;
		int dc= c - col;
		if(dc>=2 || dc<=-2)
			return false;
		if(0==level.map[row][c] || 0==level.map[r][col])
			return false;
		if(!level.is_passable(r, c) || 0!=(level.map[r][c] & MONSTER))
			return false;
		if(row!=r && col!=c
		&& 0!=((level.map[r][c]|level.map[row][col])&DOOR))
			return false;
		if(0==(m_flags & (FLITS | CONFUSED | CAN_FLIT)) && trow==-1){
			if(row<dstrow && r<row) return false;
			if(row>dstrow && r>row) return false;
			if(col<dstcol && c<col) return false;
			if(col>dstcol && c>col) return false;
		}
		if(0!=(level.map[r][c] & TOY)){
			Toy obj= (Toy)level.level_toys.item_at(r, c);
			if(obj.kind==Id.SCARE_MONSTER)
				return false;
		}
		return true;
	}
	void wake_up(){
		if(0==(m_flags & NAPPING))
			m_flags &= ~(ASLEEP | IMITATES | WAKENS);
	}
	boolean mon_sees(int r, int c){
		if(level.sees(row,col,r,c))
			return true;
		int rdif= r - row;
		int cdif= c - col;

		return rdif>=-1 && rdif<=1 && cdif>=-1 && cdif<=1;
	}
	void dr_course(boolean entering){
		Door my= (Door)level.level_doors.item_at(row, col);
		if(entering){
			Rowcol foyer= level.foyer(row, col);
			Door dtab[]= new Door[level.level_doors.size()];
			level.level_doors.copyInto(dtab);
			self.rand.permute(dtab);
			for(int i= 0; i<dtab.length; i++){
				Door dr= dtab[i];
				Door d= dr.passageto;
				if(dr==my){
					// My door is a last resort (go back)
					if(trow==-1 && d!=null){
						trow= d.row;
						tcol= d.col;
					}
				}else if(foyer!=null && level.sees(dr.row, dr.col, foyer.row, foyer.col)){
					trow= dr.row;
					tcol= dr.col;
					// Passage to another door is preferred
					if(d!=null)
						break;
				}
			}
		}else if(my!=null && my.passageto!=null){
			trow= my.passageto.row;
			tcol= my.passageto.col;
		}
	}
	void s_con_mon(Persona man){
		if(man.con_mon){
			m_flags |= CONFUSED;
			cnfs(self.rand.get(12, 22));
			tell(who("appear") + " confused");
			man.con_mon= false;
		}
	}
	void mon_hit(Persona man){
		double minus= 0;

		if(man.ihate!=null && this!=man.ihate)
			man.ihate= null;
		ihate= man;
		trow= -1;
		int hit_chance= 100;
		if(level.cur_level < AMULET_LEVEL*2){
			hit_chance= m_hit_chance;
			hit_chance -= 2*man.exp + 2*man.ring_exp - man.r_rings;
		}
		if(man.wizard)
			hit_chance /= 2;
		if(null==man.ihate)
			level.self.interrupted= true;
		if(!self.rand.percent(hit_chance)){
			if(null==man.ihate){
				man.hit_message += who("miss","misses");
				man.tell(man.hit_message, true);
				man.hit_message= "";
			}
			return;
		}
		if(null==man.ihate){
			man.hit_message += who("hit");
			man.tell(man.hit_message, true);
			man.hit_message= "";
		}
		int dmg;
		if(0==(m_flags & STATIONARY)){
			dmg= Id.get_damage(mt.m_damage, self.rand);
			if(level.cur_level >= AMULET_LEVEL*2){
				minus= (double) ((AMULET_LEVEL * 2) - level.cur_level);
			} else {
				if(man.armor!=null)
					minus= (double) man.armor.get_armor_class() * 3.00;
				minus= minus/100.00 * dmg;
			}
			dmg -= (int) minus;
		} else {
			dmg= stationary_damage++;
		}
		if(man.wizard)
			dmg /= 3;
		if(dmg > 0)
			man.damage((Persona)this, dmg, 0);
		if(0!=(m_flags & SPECIAL_HIT))
			special_hit(man);
	}
	boolean damage(Persona man, int dmg, int other){
		if(dmg>0 && man!=this && man!=null)
			ihate= man;
		hp_current -= dmg;
		if(hp_current > 0){
			if(dmg>0)
				self.flashadd(row,col,uRed);
			return true;
		}
		die(man);
		return false;
	}
	void die(Persona man){
		if(man != null){
			if(this==man.ihate)
				man.ihate= null;
			cough_up();
			man.tell(man.who()+" defeated " + who());
			man.hit_message= "";
			if(man instanceof Man)
				((Man)man).add_exp(exp_points, true);
			if(0!=(m_flags & HOLDS))
				man.being_held= false;
		}
		super.die();
	}		
	void steal_gold(Persona man){
		int amount;
		if((man.gold <= 0) || self.rand.percent(10))
			return;
		amount= self.rand.get(level.cur_level*10, level.cur_level*30);

		if(amount > man.gold)
			amount= man.gold;
		man.gold -= amount;
		gold += self.rand.get(amount/4,amount);
		man.tell("your purse feels lighter");
		man.print_stat();
		tele();
	}
	void steal_item(Persona man){
		int i, t= 1;
		int nn= 0;
		Toy obj= null;

		if(self.rand.percent(15) || !(man instanceof Man))
			return;
		Enumeration e= ((Man)man).pack.elements();

		while(e.hasMoreElements()){
			Toy o= (Toy)e.nextElement();
			if(0==(o.in_use_flags & Id.BEING_USED)){
				if(0 == self.rand.get(nn))
					obj= o;
				++nn;
			}
		}
		if(obj!=null){
			if(0==(obj.kind &  Id.WEAPON)){
				t= obj.quantity;
				obj.quantity= 1;
			}
			man.tell("she stole " + obj.get_desc());
			obj.quantity= t;

			obj.vanish();
		}
		die();	/* Kill the monster to disappear it */
	}
	void cough_up(){
		Toy obj;
		if(level.cur_level < level.max_level)
			return;
		if(0!=(m_flags & STEALS_GOLD)){
			obj= new Toy(level, Id.GOLD);
			obj.quantity= self.rand.get(level.cur_level*15, level.cur_level*30);
		} else {
			if(!self.rand.percent(drop_percent))
				return;
			obj= level.gr_toy();
		}
		for(int n= 0; n <= 5; n++){
			for(int i= -n; i <= n; i++){
				if(level.try_to_cough(row+n, col+i, obj))
					return;
				if(level.try_to_cough(row-n, col+i, obj))
					return;
			}
			for(int i= -n; i <= n; i++){
				if(level.try_to_cough(row+i, col-n, obj))
					return;
				if(level.try_to_cough(row+i, col+n, obj))
					return;
			}
		}
	}
	void check_gold_seeker(){
		m_flags &= ~SEEKS_GOLD;
	}
	boolean check_imitator(){
		if(0!=(m_flags & IMITATES)){
			wake_up();
			level.mark(row, col);
			return true;
		}
		return false;
	}
	boolean flame_broil(Persona man){
		if(!mon_sees(man.row, man.col) || self.rand.coin())
			return false;
		int r= man.row - row;
		int c= man.col - col;
		if(r < 0)r= -r;
		if(c < 0)c= -c;
		if((r!=0 && c!=0 && r!=c) || r>7 || c>7)
			return false;

		int dir= Id.get_dir(row, col, man.row, man.col);
		Toy wand= level.gr_wand();
		wand.kind= Id.FIRE;
		wand.owner= this;
		level.bounce(wand, dir, row, col, 0);
		self.markall();	// relight
		return true;
	}
	void special_hit(Persona p){
		if(!(p instanceof Man))
			return;
		Man man= (Man)p;

		if(0!=(m_flags & CONFUSED) && self.rand.percent(66))
			return;
		if(0!=(m_flags & RUSTS))
			man.rust(this);
		if(0!=(m_flags & HOLDS) && man.levitate==0)
			man.being_held= true;
		if(0!=(m_flags & FREEZES))
			man.freeze(this);
		if(0!=(m_flags & STINGS))
			man.sting(this);
		if(0!=(m_flags & DRAINS_LIFE))
			man.drain_life();
		if(0!=(m_flags & DROPS_LEVEL))
			man.drop_level();
		if(0!=(m_flags & STEALS_GOLD))
			steal_gold(man);
		else if(0!=(m_flags & STEALS_ITEM))
			steal_item(man);
	}
	static String mflagname[]= {
		"HASTED", "SLOWED", "INVISIBLE", "ASLEEP", 
		"WAKENS", "WANDERS", "FLIES", "FLITS", 
		"CAN_FLIT", "CONFUSED", "RUSTS", "HOLDS", 
		"FREEZES", "STEALS_GOLD", "STEALS_ITEM", "STINGS", 
		"DRAINS_LIFE", "DROPS_LEVEL", "SEEKS_GOLD", "FREEZING_ROGUE", 
		"RUST_VANISHED", "CONFUSES", "IMITATES", "FLAMES", 
		"STATIONARY", "NAPPING"};
	public String toString(){
		String s= "";
		for(int k= 0; k<mflagname.length; k++)if(0!=(m_flags & (1<<k)))
			s= s + ' ' + mflagname[k];
		return name() + super.toString() + "to " + (new Rowcol(trow, tcol)).toString() + s;
	}
	void tele(){
		Rowcol pt= level.gr_row_col(FLOOR|TUNNEL|STAIRS|TOY, this);
		if(pt!=null)
			put_m_at(pt.row, pt.col);
		being_held= false;
		if(0 != (m_flags & HOLDS) && ihate != null)
			ihate.being_held= false;
		bear_trap= 0;
	}
	int color(){
		//if(0 != (m_flags & (ASLEEP | NAPPING)))
		//	return uGreen;
		//if(2*hp_current <= hp_max)
		//	return uWeak;
		return uNormal;
	}
}
