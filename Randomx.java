import java.util.Random;
import java.util.Vector;

class Randomx extends Random implements java.io.Serializable {
	static final int P= 0x7fffffff;	/* Mask for random number generator */
	Randomx(long l){
		super(l);
	}
	Randomx(){
		super();
	}
	boolean percent(int n){
		return (this.nextInt()&P)%100 <n;
	}
	boolean coin(){
		return 0 != (nextInt() & 1);
	}
	int get(int n0, int n1){
		return n0>=n1? n0 : n0 + (this.nextInt()&P) % (1+n1-n0);
	}
	int get(int n){
		return (this.nextInt()&P) % (1+n);
	}
	Object permute(Object o[])[]{
		int j= o.length;
		while(--j>0){
			int i= get(j);	// was j-1?
			Object t= o[j]; o[j]= o[i]; o[i]= t;
		}
		return o;
	}
	int permute(int b[])[]{
		int n= b.length;
		Integer a[]= new Integer[n];
		int j;
		for(j= 0; j<n; j++)
			a[j]= new Integer(b[j]);
		permute((Object [])a);
		for(j= 0; j<n; j++)
			b[j]= a[j].intValue();
		return b;
	}
	int permute(int n)[]{
		int b[]= new int[n];
		for(int j= 0; j<n; j++)
			b[j]= j;
		return permute(b);
	}
}
