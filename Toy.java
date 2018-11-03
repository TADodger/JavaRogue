import java.util.Vector;

class Toy extends Item implements java.io.Serializable{
	// The stuff you can pick up
	String damage= "1d1";
	int quantity= 1;
	int kill_exp;
	boolean is_protected;
	boolean is_cursed= false;
	int klass;
	boolean identified= false;
	int d_enchant;
	int quiver;
	int hit_enchant;
	int kind;
	boolean picked_up= false;			/* sleep from wand of sleep */
	int in_use_flags= 0;
	Persona owner;				/* Who is carrying this item */

	static String curse_message= "you can't, it appears to be cursed";

	Toy(Level level, int kind){
		super(level, 0, 0);

		int percent;
		int blessing;
		int increment;

		this.kind= kind;
		this.ichar= Id.get_mask_char(kind);
		switch(kind & Id.ALL_TOYS){
		case Id.WEAPON:
			if(kind == Id.ARROW || kind == Id.DAGGER
			|| kind == Id.SHURIKEN || kind == Id.DART){
				this.quantity= level.self.rand.get(3, 15);
				this.quiver= level.self.rand.get(126);
			} else
				this.quantity= 1;
			this.hit_enchant= this.d_enchant= 0;

			percent= level.self.rand.get(1, 96);
			blessing= level.self.rand.get(1, 3);
			increment= 1;

			if(percent>16 && percent <= 32){
				increment= -1;
				this.is_cursed= true;
			}
			if(percent <= 32){
				for(int i= 0; i < blessing; i++){
					if(level.self.rand.coin())
						this.hit_enchant += increment;
					else
						this.d_enchant += increment;
				}
			}
			switch(kind){
			case Id.BOW:
			case Id.DART:				this.damage= "1d1"; break;
			case Id.ARROW:				this.damage= "1d2"; break;
			case Id.DAGGER:				this.damage= "1d3"; break;
			case Id.SHURIKEN:			this.damage= "1d4"; break;
			case Id.MACE:				this.damage= "2d3"; break;
			case Id.LONG_SWORD:			this.damage= "3d4"; break;
			case Id.TWO_HANDED_SWORD:	this.damage= "4d5"; break;
			} break;
		case Id.ARMOR:
			klass= (255&kind) + 2;
			if(kind == Id.PLATE || kind == Id.SPLINT)
				klass--;
			is_protected= false;
			d_enchant= 0;

			percent= level.self.rand.get(1, 100);
			blessing= level.self.rand.get(1, 3);

			if(percent <= 16){
				is_cursed= true;
				d_enchant -= blessing;
			} else if(percent <= 33){
				d_enchant += blessing;
			} break;
		case Id.WAND:
			klass= level.self.rand.get(3, 7);
			break;
		case Id.RING:
			klass= 0;
			switch(kind){
			case Id.STEALTH: 	break;
			case Id.SLOW_DIGEST: break;
			case Id.REGENERATION: 	break;
			case Id.R_SEE_INVISIBLE: break;
			case Id.SUSTAIN_STRENGTH: break;
			case Id.MAINTAIN_ARMOR: break;
			case Id.SEARCHING: break;
			case Id.R_TELEPORT: is_cursed= true; break;
			case Id.ADD_STRENGTH:
			case Id.DEXTERITY:
				while((klass= level.self.rand.get(4) - 2) == 0) ;
				is_cursed= klass < 0;
				break;
			case Id.ADORNMENT:
				is_cursed= level.self.rand.coin();
				break;
			} break;
		default:
			break;
		}
	}
	Toy(Toy t){	// New copy
		super(t.level, t.row, t.col);
		damage= new String(t.damage);
		kill_exp= t.kill_exp;
		is_protected= t.is_protected;
		is_cursed= t.is_cursed;
		klass= t.klass;
		identified= t.identified;
		d_enchant= t.d_enchant;
		quiver= t.quiver;
		hit_enchant= t.hit_enchant;
		kind= t.kind;
		in_use_flags= Id.NOT_USED;
		owner= t.owner;
		this.ichar= Id.get_mask_char(kind);
	}
	Toy check_duplicate(Vector pack){
		if(0==(kind & (Id.WEAPON | Id.FOOD | Id.SCROLL | Id.POTION)))
			return null;
		if(kind==Id.FRUIT)
			return null;
		int i= pack.size();
		while(--i>=0){
			Toy op= (Toy)pack.elementAt(i);
			if(op.kind == kind){
				if(0==(kind & Id.WEAPON)
				||(( kind == Id.ARROW
				  || kind == Id.DAGGER
				  || kind == Id.DART
				  || kind == Id.SHURIKEN)
				 && quiver == op.quiver)){
					op.quantity += quantity;
					return op;
				}
			}
		}
		return null;
	}
	Toy add_to_pack(Man man){
		// Return the pack and the added toy
		Toy pdup= check_duplicate(man.pack);
		if(null != pdup)
			return pdup;
		ichar= man.pack.next_avail_ichar();
		place_at(-1,-1,TOY);
		int k= ichar-'a';
		if(k<man.pack.size())
			man.pack.insertElementAt(this, k);
		else
			man.pack.addElement(this);
		owner= man;
		return this;
	}
	int get_armor_class(){
		return klass + d_enchant;
	}
	String get_desc(){
		return Id.get_desc(this);
	}
	String name(){
		String retstring= "unknown ";
		switch(kind & Id.ALL_TOYS){
		case Id.SCROLL:
			retstring= quantity > 1 ? "scrolls " : "scroll ";
			break;
		case Id.POTION:
			retstring= quantity > 1 ? "potions " : "potion ";
			break;
		case Id.FOOD:
			if(kind == Id.RATION || owner==null || !(owner instanceof Man))
				retstring= "food ";
			else
				retstring= ((Man)owner).option.fruit;
			break;
		case Id.WAND:
			retstring= Id.is_wood[kind&255] ? "staff " : "wand ";
			break;
		case Id.WEAPON:
			switch(kind){
			case Id.DART:
				retstring=quantity > 1 ? "darts " : "dart ";
				break;
			case Id.ARROW:
				retstring=quantity > 1 ? "arrows " : "arrow ";
				break;
			case Id.DAGGER:
				retstring=quantity > 1 ? "daggers " : "dagger ";
				break;
			case Id.SHURIKEN:
				retstring=quantity > 1?"shurikens ":"shuriken ";
				break;
			default:
				retstring= Id.id_weapons[kind&255].title;
			}
			break;
		case Id.ARMOR:
			retstring= "armor ";
			break;
		case Id.RING:
			retstring= "ring ";
			break;
		case Id.AMULET:
			retstring= "amulet ";
			break;
		}
		return retstring;
	}
	void drop(){
		if(0 != (in_use_flags & Id.BEING_WIELDED)){
			if(is_cursed){
				owner.tell(curse_message);
				return;
			}
			owner.unwield();
		} else if(0 != (in_use_flags & Id.BEING_WORN)){
			if(is_cursed){
				owner.tell(curse_message);
				return;
			}
			level.mv_aquatars(owner);
			owner.unwear();
			owner.print_stat();
		} else if(0 != (in_use_flags & Id.ON_EITHER_HAND)){
			if(is_cursed){
				owner.tell(curse_message);
				return;
			}
			un_put_on();
		}
		Toy obj= this;
		if(quantity>1 && 0==(kind&Id.WEAPON)){
			quantity--;
			obj= new Toy(obj);
		} else if(owner instanceof Man){
			ichar= Id.get_mask_char(kind);
			((Man)owner).pack.removeElement(this);
		}
		obj.place_at(owner.row, owner.col, TOY);
		owner.self.check_message(owner);
		owner.self.describe(owner, "dropped " + obj.get_desc(), false);
		owner.reg_move();
		obj.owner= null;
	}
	void identify(){
		Id.identify(kind);
	}
	void un_put_on(){
		if(this==owner.left_ring){
			in_use_flags &= ~Id.ON_LEFT_HAND;
			owner.left_ring= null;
		}else if(this==owner.right_ring){
			owner.right_ring= null;
			in_use_flags &= ~Id.ON_RIGHT_HAND;
		}else
			return;
		owner.ring_stats(true);
	}
	void vanish(){
		place_at(-1,-1,TOY);
		if(quantity>1)
			--quantity;
		else if(owner != null){
			if(owner instanceof Man)
				((Man)owner).pack.removeElement(this);
			if(0 != (in_use_flags & Id.BEING_WIELDED))
				owner.unwield();
			else if(0!=(in_use_flags & Id.BEING_WORN))
				owner.unwear();
			else if(0!=(in_use_flags & Id.ON_EITHER_HAND))
				un_put_on();
			owner= null;
		}
	}
	void eatenby(){
		if(!(owner instanceof Man))
			return;
		Man owner= (Man)this.owner;
		int moves= 0;
		if((kind == Id.FRUIT) || level.self.rand.percent(60)){
			moves= level.self.rand.get(950, 1150);
			if(kind == Id.RATION)
				owner.tell("yum, that tasted good");
			else
				owner.tell("my, that was a yummy " + owner.option.fruit);
		} else {
			moves= level.self.rand.get(750, 950);
			owner.tell("yuk, that food tasted awful");
			owner.add_exp(2, true);
		}
		owner.moves_left /= 3;
		owner.moves_left += moves;
		owner.hunger_str= "      ";
		owner.print_stat();
		vanish();
	}
	void thrownby(int dir){
		if(0!=(in_use_flags & Id.BEING_WIELDED) && quantity<=1)
			owner.unwield();
		else if(0!=(in_use_flags & Id.BEING_WORN)){
			level.mv_aquatars(owner);
			owner.unwear();
			owner.print_stat();
		} else if(0!=(in_use_flags & Id.ON_EITHER_HAND))
			un_put_on();
		row= owner.row;
		col= owner.col;

		get_thrown_at_monster(dir);
		Monster monster= (Monster)level.level_monsters.item_at(row, col);
	/* Only if the point is visible? */
		owner.self.mark(owner.row, owner.col);
		owner.self.refresh();
	
		owner.self.mark(row, col);
		if(monster!=null){
			monster.wake_up();
			monster.check_gold_seeker();
	
			if(!monster.throw_at_monster(this, owner))
				flop_weapon();
		} else
			flop_weapon();
		vanish();
	}
	void get_thrown_at_monster(int dir){
		int orow= row, ocol= col;
		int ch= Id.get_mask_char(kind);
	
		for(int i= 0; i < 24; i++){
			Rowcol pt= level.get_dir_rc(dir, row, col, false);
			row= pt.row; col= pt.col;
			if(col<=0 || col>=level.ncol-1 || 0==(level.map[row][col]&SOMETHING)
			||(0!=(level.map[row][col] & (HORWALL | VERTWALL | HIDDEN))
			 && 0==(level.map[row][col] & TRAP)))
				break;
			if(i != 0)
				level.self.mark(orow, ocol);

			if(0==(level.map[row][col]&MONSTER)){
				level.self.vflash(row, col, (char)ch);
			}else{
				level.self.refresh();
				level.self.md_sleep(50);
			}
			orow= row; ocol= col;
			if(0!=(level.map[row][col] & MONSTER))
				if(!level.imitating(row, col))
					break;
			if(0!=(level.map[row][col] & TUNNEL))
				i += 2;
		}
		place_at(orow, ocol, TOY);
	}
	void flop_weapon(){
		int r= row, c= col;
		Rowcol pt= new Rowcol(r,c);
		int i;

		// Find a point near the destination where something can be dropped
		int perm[]= level.self.rand.permute(9);
		for(i= 0; i<9; i++){
			pt.col= Id.xtab[perm[i]] + c;
			pt.row= Id.ytab[perm[i]] + r;
			if(pt.row<=level.nrow-2 && pt.row>MIN_ROW
			&& pt.col<level.ncol && pt.col>=0 && 0!=level.map[pt.row][pt.col]
			&& 0==(level.map[pt.row][pt.col] & (~DROPHERE))){
				r= pt.row; c= pt.col;
				Toy new_missile= new Toy(this);
				new_missile.place_at(r, c, TOY);
				return;
			}
		}
		int t= quantity;
		quantity= 1;
		owner.tell("the " + name() + "vanishes as it hits the ground");
		quantity= t;
	}
	int get_w_damage(){	//Check for null (-1)
		if(0==(kind & Id.WEAPON))
			return -1;
		int dd[]= Id.parse_damage(damage);
		dd[0] += hit_enchant;
		dd[1] += d_enchant;
		String dnew= "" + dd[0] + 'd' + dd[1];
		return Id.get_damage(dnew, level.self.rand);
	}
	int to_hit(){//Check for null (1)
		return Id.parse_damage(damage)[0] + hit_enchant;
	}
	public String toString(){
		return super.toString() + Integer.toString(kind, 16);
	}
}
