class Message implements java.io.Serializable {
	View view;
	static final int NMESSAGES= 5;
	static final int MIN_ROW= 1;
	static private String msgs[]= new String[NMESSAGES];
	int msg_col= 0, imsg= -1;
	boolean msg_cleared= true;
	boolean rmsg= false;
	static private String more= " -more-";
	boolean cant_int, did_int, flush;

	Message(View view){
		this.view= view;
		msg_col= 0;
		imsg= -1;
		for(int k= 0; k<NMESSAGES; k++)msgs[k]= null;
		msg_cleared= true;
		rmsg= false;
	}	
	void message(String msg, boolean intrpt){
		if(view==null)
			return;
		cant_int= true;
	
		if(intrpt){
			view.self.interrupted= true;
			if(flush)
				view.self.md_slurp();
		}
		if(!msg_cleared){
			view.addch(MIN_ROW-1, msg_col, more);
			view.refresh();
			view.self.wait_for_ack();
			check_message();
		}
		if(!rmsg){
			imsg= (imsg + 1) % NMESSAGES;
			msgs[imsg]= new String(msg);
		}
		view.addch(MIN_ROW-1, 0, msg+' ');
		view.refresh();
		msg_cleared= false;
		msg_col= msg.length();
	
		cant_int= false;
	
		if(did_int){
			did_int= false;
//			onintr();
		}
	}
	void message(String msg){
		message(msg, false);
	}
	void remessage(int c){
		if(imsg != -1 && view!=null){
			check_message();
			rmsg= true;
			while(c > imsg){
				c -= NMESSAGES;
			}
			message(msgs[((imsg - c) % NMESSAGES)], false);
			rmsg= false;
			view.refresh();
		}
	}
	void check_message(){
		/* Erase the message line */
		if(msg_cleared)
			return;
		String erase= new String();
		for(int i= 0; i<view.ncol; i++)erase += ' ';
		view.addch(MIN_ROW-1, 0, erase);
		view.refresh();
		msg_cleared= true;
	}
	String get_input_line(String prompt, String insert, String if_cancelled, boolean add_blank, boolean do_echo){
		int ch;
		int i= 0, n= 0;
		String buf= new String(insert);
	
		if(prompt != null){
			message(prompt, false);
			n= prompt.length();
		}
		if(insert!=null){
			view.addch(0, n + 1, insert);
			i= insert.length();
		}
		view.refresh();
		while(((ch= view.self.rgetchar()) != '\r') && ch!='\n' && ch!='\033'){
			if(ch >=' ' && ch<= '~'){
				if(ch!=' ' || i>0){
					buf= buf + ((char)ch);
					++i;
					if(do_echo)
						view.addch(MIN_ROW-1, n+i, (char)ch);
				}
			}
			if(ch=='\b' && i>0){
				if(do_echo){
					view.addch(0, i+n, ' ');
				}
				i--;
				buf= buf.substring(0, i);
			}
			view.refresh();
		}
		check_message();
		buf= add_blank? buf + ' ' : buf.trim();

		if(ch=='\033' || i==0 || (i==1 && add_blank)){
			if(if_cancelled!=null)
				message(if_cancelled, false);
			return null;
		}
		return buf;
	}
	int left_or_right(){
		int ch;
		do{
			message("left or right hand?");
			ch= view.self.rgetchar();
		}while(ch!='\033' && ch!='l' && ch!='r' && ch!='\n' && ch!='\r');
		if(ch != 'l' && ch != 'r')
			ch= 0;
		return ch;
	}
	boolean yes_or_no(String s){
		int ch;
		do{
			message(s + " [y or n] ");
			ch= view.self.rgetchar();
		}while(ch!='\033' && ch!='y' && ch!='n' && ch!='\n' && ch!='\r');
		return ch=='y';
	}
	int rightlist(String list[], boolean asking){
		int i, n;
		int ch= '\033';
		String lastline= " --press space to continue-- ";
		int nmax= lastline.length();
		String keep[]= new String[view.nrow-1];
		char chmin= 'z';
		char chmax= 'a';

		for(i= 0; i<list.length; i++)if(list[i]!=null){
			n= list[i].length();
			if(nmax<n)
				nmax= n;
		}
		int col= view.ncol - nmax - 2;
		if(col<0)
			col= 0;
	
		int row= 0;
		--i;
		for(int k= 0; k<view.nrow-1; k++)	// Save
			keep[k]= new String(view.terminal[k],col, view.ncol-col);

		for(int k= 0; k<=i; k++)if(list[k]!=null){
			if(list[k].length()>1){
				char c= list[k].charAt(1);
				if(chmin>c)chmin= c;
				if(chmax<c)chmax= c;
			}
			view.addch(row, col, list[k]);
			for(n= list[k].length(); n+col<view.ncol; n++)
				view.addch(row, n+col, ' ');
			++row;
			if(k==i || row>=view.nrow-2){
				if(asking & k==i)
					lastline= " --Choose " + chmin + "-" + chmax+ ", or ESC--";
				view.addch(row, col, lastline);
				view.refresh();
				do ch= view.self.rgetchar();
				while(ch!=' ' && ch!='\033' && (!asking || !('a'<=ch && ch<='z')));
				for(int j= 0; j<=row; j++)	// Restore
					view.addch(j, col, keep[j]);
				row= 0;
				if(ch>='a')
					break;
			}
		}
		for(int j= 0; j<view.nrow-1; j++)	// Restore
			view.addch(j, col, keep[j]);
		view.refresh();
		return ch;
	}
	int kbd_direction(){
		int ch= 0;
		int dir= -1;
		do{
			message(ch==0? "direction?": "direction (one of hjklyubn or ESC)?");
			ch= view.self.rgetchar();
			check_message();			
		}while(-2==(dir= Id.is_direction(ch)));
		return dir;
	}
/*
	static void pad(String s, int n){
		int i;
	
		for(i= strlen(s); i < n; i++){
			addch(' ');
		}
	}
	static void save_screen(){
		FILE *fp;
		int i, j;
		char buf[DCOLS+2];
		boolean found_non_blank;
	
		if((fp= fopen("rogue.screen", "w")) != NULL){
			for(i= 0; i < DROWS; i++){
				found_non_blank= 0;
				for(j= (DCOLS - 1); j >= 0; j--){
					buf[j]= charat(i, j);
					if(!found_non_blank){
						if((buf[j] != ' ') || (j == 0)){
							buf[j + ((j == 0) ? 0 : 1)]= 0;
							found_non_blank= 1;
						}
					}
				}
				fputs(buf, fp);
				putc('\n', fp);
			}
			fclose(fp);
		} else {
			sound_bell();
		}
	}

*/
	static String bnr[]= new String[5];
	static {
		bnr[0]= " @@@ @@@@  @@@ @@@@ @@@@@@@@@@ @@@ @   @  @  @@@  @   @@    @   @@   @ @@@ @@@@  @@@ @@@@  @@@ @@@@@@   @@   @@   @@   @@   @@@@@@";
		bnr[1]= "@   @@   @@   @@   @@    @    @    @   @  @    @  @  @ @    @@ @@@@  @@   @@   @@   @@   @@      @  @   @@   @@   @ @ @  @ @    @ "; 
		bnr[2]= "@@@@@@@@@ @    @   @@@@  @@@  @ @@@@@@@@  @    @  @@@  @    @ @ @@ @ @@   @@@@@ @   @@@@@  @@@   @  @   @ @ @ @ @ @  @    @    @  "; 
		bnr[3]= "@   @@   @@   @@   @@    @    @   @@   @  @  @ @  @  @ @    @   @@  @@@   @@    @  @@@  @     @  @  @   @ @ @ @@ @@ @ @   @   @   ";
		bnr[4]= "@   @@@@@  @@@ @@@@ @@@@@@     @@@ @   @  @   @   @   @@@@@@@   @@   @ @@@ @     @@ @@   @ @@@   @   @@@   @  @   @@   @  @  @@@@@";
	}
	void banner(int ro, int co, String s){
		for(int k= 0; k<5; k++){
			for(int i= 0; i<s.length(); i++){
				int c= (int)s.charAt(i);
				if(c>='a' && c<='z')
					c -= 'a';
				else if(c>='A' && c<='Z')
					c -= 'A';
				else continue;
				try{
					c *= 5;
					view.addch(k+ro, co+6*i, bnr[k].substring(c, c+5));
				} catch(Exception e){
				}
			}
		}
	}
}
