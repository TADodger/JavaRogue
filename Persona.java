class Persona extends Item implements java.io.Serializable{
	Rogue self;
	Montype mt;

	// These all count moves until the condition stops
	int	ring_exp= 0;
	boolean wizard= false;

	Toy armor;
	Toy weapon;
	Toy left_ring;
	Toy right_ring;

	int blind= 0;
	int confused= 0;
	int halluc= 0;
	int levitate= 0;
	int bear_trap= 0;
	int haste_self= 0;
	int hp_current;
	int hp_max;
	int extra_hp= 0;
	String hit_message= "";

	Persona ihate;
	int stealthy= 0;
	boolean con_mon= false;	// confuse monsters
	int r_rings= 0;
	int e_rings= 0;
	int str_current;		// Current strength
	int str_max;			// Max strength
	int	add_strength= 0;
	int gold= 0;
	int exp;				// Experience level
	boolean being_held= false;

	int m_flags;			/* monster flags */

	Persona(Rogue self){
		super();
		this.self= self;
	}
	Persona(Level level){
		super(level,0,0);
		if(level!=null)
			this.self= level.self;
	}
	void tell(String s, boolean b){
		/* Describe my internal state (only if this is me) */
//		if(this instanceof Man)
//			((Man)this).view.msg.message(s, b);
		self.tell(this, s, b);
	}
	void tell(String s){
		tell(s, false);
	}
	boolean describe(String s, boolean b){
		/* Describe a visible event about this guy */
		return self.describe(this, s, b);
	}
	String name(){
		return mt.m_name;
	}
	boolean reg_move(){
		if(haste_self>0 && 0==(--haste_self))
			tell(who("feel") + " yourself slowing down");

		if(confused>0 && 0==(--confused))
			unconfuse();

		if(halluc>0 && 0==(--halluc))
			unhallucinate();

		if(bear_trap>0)
			bear_trap--;

		if(levitate>0 && 0>=(--levitate))
			describe(who("float")+"gently to the ground", true);
		if(blind>0 && 0==(--blind))
			unblind();
		return false;	/* not fainted */
	}
	void heal_potional(boolean extra){
		if(confused>0 && extra)
				unconfuse();
		else if(confused>0)
			confused= (confused / 2) + 1;

		if(halluc>0 && extra)
			unhallucinate();
		else if(halluc>0)
			halluc= (halluc / 2) + 1;

		if(blind>0)
			unblind();
	}

	void go_blind(){
		if(blind==0)
			tell("a cloak of darkness falls around " + who());
		blind += self.rand.get(500, 800);
		if(this instanceof Man)
			((Man)this).view.markall();
	}
	void unblind(){
		blind= 0;
		tell("the veil of darkness lifts", true);
		if(this instanceof Man)
			((Man)this).view.markall();	// relight
	}
	int mov_confused(){
		String s= "jklhyubn";
		return s.charAt(self.rand.get(7));
	}
	void cnfs(int amt){
		confused += amt;
	}
	String who(){
		return "@<"+name()+">";
	}
	String who(String verb, String itverb){
		return "@>"+name()+ "+" + verb + "+" + itverb + "< ";
	}
	String who(String verb){
		return who(verb, verb+'s');
	}
	void unconfuse(){
			if(confused>0)
				tell(who("feel") + " less " + (halluc>0 ? "trippy" : "confused") + " now");
			confused= 0;
			m_flags &= ~Monster.CONFUSED;
	}
	void unhallucinate(){
		halluc= 0;
		if(this instanceof Man){
			((Man)this).view.markall();
			tell("everything looks SO boring now", true);
		}
	}
	void take_a_nap(){
		m_flags |= Monster.ASLEEP;
	}
	Trap trap_player(){
		/* Traps a monster (man traps overrides this) */
		Trap t= (Trap)level.level_traps.item_at(row, col);
		if(t!=null)	switch(t.kind){
		case Trap.BEAR_TRAP:
			if(describe(t.trap_message(this), true))
				level.map[row][col] &= ~HIDDEN;
			bear_trap= self.rand.get(4, 7);
			t= null;
			break;
		case Trap.TRAP_DOOR:
			die();	/* Just kill it! */
			if(describe("the "+name()+" disappears!", false))
				level.map[row][col] &= ~HIDDEN;
			break;
		case Trap.TELE_TRAP:
			if(describe("the "+name()+" disappears!", false))
				level.map[row][col] &= ~HIDDEN;
			tele();
			break;
		case Trap.SLEEPING_GAS_TRAP:
			if(describe(t.trap_message(this), true))
				level.map[row][col] &= ~HIDDEN;
			take_a_nap();
			break;
		case Trap.DART_TRAP:
			String s= t.trap_message(this);
			if(damage(null, Id.get_damage("1d6", self.rand), 0))
				s += ", and killed it.";
			if(describe(s, true))
				level.map[row][col] &= ~HIDDEN;
			break;
		}
		return t;
	}
	boolean m_confuse(Persona monster){
		if(!can_see(monster.row, monster.col))
			return false;
		if(self.rand.percent(45)){
			monster.m_flags &= ~Monster.CONFUSES;	/* will not confuse the rogue */
			return false;
		}
		if(self.rand.percent(55)){
			monster.m_flags &= ~Monster.CONFUSES;
			tell("the gaze of the "+monster.name()+" has confused " + who());
			cnfs(self.rand.get(12, 22));
			return true;
		}
		return false;
	}
	void do_wear(Toy obj){
		armor= obj;
		obj.in_use_flags |= Id.BEING_WORN;
		obj.identified= true;
	}
	void unwear(){
		if(armor != null)
			armor.in_use_flags &= ~Id.BEING_WORN;
		armor= null;
	}
	void do_wield(Toy obj){
		weapon= obj;
		obj.in_use_flags |= Id.BEING_WIELDED;
	}
	void unwield(){
		if(weapon!=null)
			weapon.in_use_flags &= ~Id.BEING_WIELDED;
		weapon= null;
	}
	void print_stat(){
		/* override for real man */
	}
	void ring_stats(boolean huh){
		/* monsters immune to rings */
	}
	int get_hit_chance(Toy t){
		int hit_chance= 40;
		hit_chance += 3 * (t==null? 1 : t.to_hit());
		hit_chance += 2*(exp + ring_exp) - r_rings;
		return hit_chance;
	}
	int get_weapon_damage(Toy t){
		int damage= t==null? -1 : t.get_w_damage();

		damage += damage_for_strength();
		damage += (exp + ring_exp - r_rings + 1)/2;
		return damage;
	}
	int damage_for_strength(){
		int strength= str_current + add_strength;

		if(strength <= 6)	return strength-5;
		if(strength <= 14)	return 1;
		if(strength <= 17)	return 3;
		if(strength <= 18) 	return 4;
		if(strength <= 20) 	return 5;
		if(strength <= 21) 	return 6;
		if(strength <= 30) 	return 7;
		return 8;
	}
	boolean can_see(int r, int c){
		return true;
	}
	String get_ench_color(){
		if(halluc>0)
			return Id.id_potions[self.rand.get(Id.id_potions.length-1)].title;
		if(con_mon)
			return "red ";
		return "blue ";
	}
	void tele(){
		/* overridden for men and monsters */
	}
	boolean damage(Persona hurter, int dmg, int other){	
		/* Force an error (must be overridden) */
		hurter= null; hurter.col= 0;
		return false;
	}
	void die(){
		if(ihate != null){
			if(0!= (m_flags & Monster.HOLDS))
				ihate.being_held= false;
			if(ihate.ihate==this)
				ihate.ihate= null;
		}
		level.map[row][col] &= ~(MONSTER|MAN);
		level.level_monsters.removeElement(this);
		self.mark(row, col);
	}
	void gloat(Persona victim){
		String s= "Oof!";
		switch(self.rand.get(4)){
		case 0:	s= "\"You don't look so good, "+victim.name() + "\", " + who("say")+'.'; break;
		case 1:	s= who("say") + "\"Your future is all in the past, " + victim.name() + ".\""; break;
		case 2: s= "\"I foresee your swift demise, "+victim.name()+"!\" , " + who("opine")+'.'; break;
		case 3:	s= who("command") + "\"Go now to your eternal rest, " + victim.name() + ".\""; break;
		case 4: s= "\"Death, where is thy sting? Right here, "+victim.name()+"!\", "+who("exclaim")+'.'; break;
		}
		victim.tell(s);
	}
}
