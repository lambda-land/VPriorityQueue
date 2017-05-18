package osu.vp;

import java.util.HashMap;
import java.util.PriorityQueue;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

public class VPriorityQueueTest {
	private static HashMap<String, FeatureExpr> sf = new HashMap<String, FeatureExpr>(); 

	
	public static void randomTest() {
		CPriorityQueue cpq = new CPriorityQueue();
		//RBPriorityQueue rbpq = new RBPriorityQueue();
		VSimplePQ rbpq = new VSimplePQ();
		int randomFEComlexity = 2;
		int n = 30;
		NonStaticFeature[] options = Main.getOptions(20);
		FeatureExpr[] fe = new FeatureExpr[n];
		int[] val = new int[n];
		for(int i = 0; i < fe.length; i++) {
			fe[i] = Main.randomFEComlexity(options, randomFEComlexity);
			val[i] = (int)(Math.random() * 3);
			//System.out.println(fe[i] + ", " + val[i]);
		}
		long start = System.nanoTime();
		for(int i = 0; i < fe.length; i++) {
			cpq.add(val[i], fe[i]);
			
		}
		System.out.println("duration: " + (System.nanoTime() - start));
		for(int i = 0; i < fe.length; i++) {
			Conditional<Integer> x = cpq.poll(FeatureExprFactory.True());
			System.out.println(x);
		}
		long end = System.nanoTime();
		long duration = (end - start);
		System.out.println("duration: " + duration );
		 
		
		//rbpq
		System.out.println("new Impl");
		rbpq.print();
		start = System.nanoTime();

		for(int i = 0; i < fe.length; i++) {
			rbpq.add(val[i], fe[i]);
		}
		System.out.println("duration: " + (System.nanoTime() - start));
		rbpq.print();

		for(int i = 0; i < fe.length; i++) {
			Conditional<Integer> x = rbpq.poll(FeatureExprFactory.True());
			System.out.println(x);
		}
		end = System.nanoTime();
		duration = (end - start);
		System.out.println("duration: " + duration);
		rbpq.print();

	      
	}
	
	
	public static FeatureExpr getFe(String e) {
		if(!sf.containsKey(e)) {
			sf.put(e, FeatureExprFactory.createDefinedExternal(e));
		}
		return sf.get(e);
	}
	private static boolean isEqual(FeatureExpr fe, Conditional<Integer> o1, final Conditional<Integer> o2) {
		Conditional<Boolean> ret = o1.mapfr(fe, new BiFunction<FeatureExpr, Integer, Conditional<Boolean>>() {
			public Conditional<Boolean> apply(FeatureExpr ctx, Integer i) {
				if(ctx.isContradiction()) return new One(true);
				Conditional<Integer> t = o2.simplify(ctx);
				//System.out.println(t);
				if(t instanceof One) {
					if(t.getValue() == i) return new One(true);
					else return new One(false);
				} else {
					return new One(false);
				}
			}
		}).simplify();
		if(ret instanceof One) return ret.getValue();
		return false;
	}
	public static void testAdd() {
		VPriorityQueue vpq = new VPriorityQueue();
		CPriorityQueue cpq = new CPriorityQueue();
		RBPriorityQueue rbpq = new RBPriorityQueue();
		

		Conditional<Integer> x;
		vpq.add(2, getFe("a"));
		vpq.add(3, getFe("b"));
		vpq.add(2, getFe("b"));
		vpq.add(1, getFe("c"));
		
		Conditional<Integer> y;
		cpq.add(2, getFe("a"));
		cpq.add(3, getFe("b"));
		cpq.add(2, getFe("b"));
		cpq.add(1, getFe("c"));
		
		
		Conditional<Integer> z;
		rbpq.add(2, getFe("a"));
		rbpq.add(3, getFe("b"));
		rbpq.add(2, getFe("b"));
		rbpq.add(1, getFe("c"));
		vpq.root.print();
        
		rbpq.print();
		x = vpq.poll(getFe("b"));
		y = cpq.poll(getFe("b"));
		z = rbpq.poll(getFe("b"));
		
		System.out.println("test1 y " + y);
		System.out.println("test1 z " + z);
		if(isEqual(getFe("b"), x, z)) System.out.println("true");
		else System.out.println("false");
		
		rbpq.print();

		
		x = vpq.poll(getFe("b"));
		y = cpq.poll(getFe("b"));
		z = rbpq.poll(getFe("b"));
		
		System.out.println("test1 y " + y);
		System.out.println("test1 z " + z);
		if(isEqual(getFe("b"), z, y)) System.out.println("true");
		else System.out.println("false");
		
		
		x = vpq.poll(getFe("a"));
		y = cpq.poll(getFe("a"));
		z = rbpq.poll(getFe("a"));

		if(isEqual(getFe("a"), z, y)) System.out.println("true");
		else System.out.println("false");

		x = vpq.poll(getFe("c"));
		y = cpq.poll(getFe("c"));
		z = rbpq.poll(getFe("c"));
		if(isEqual(getFe("c"), z, y)) System.out.println("true");
		else System.out.println("false");
		
		if(rbpq.isEmpty(FeatureExprFactory.True()).getValue() == true) {
			System.out.println("102true");
		} else {
			System.out.println(rbpq.root.right.mins);
			System.out.println("102false");
		}
	}
	
	public static void testBalanced() {
		RBPriorityQueue rbpq = new RBPriorityQueue();
		int n = 20;
		for(int i = 0; i < n; i++) {
			rbpq.add(i, getFe("a"));
		}
		rbpq.print();
	}
	public static void main(String[] args) {
		System.setProperty("FEATUREEXPR", "BDD");
		ChoiceFactory.activateMapChoice();
		//testAdd();
		//testBalanced();
		randomTest();
	}
}
