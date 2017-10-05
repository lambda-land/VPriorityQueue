package osu.vp;

/**
 * @author Meng Meng 
 */


import java.util.ArrayList;
import java.util.List;

import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

public class heapSort {
	public static int FeatureID = 0;
	public static boolean flag = false;

	private static NonStaticFeature[] getOptions(int nrOptions) {
		NonStaticFeature[] options = new NonStaticFeature[nrOptions];
		for (int i = 0; i < options.length; i++) {
			options[i] = new NonStaticFeature();
		}
		return options;
	}

	public static FeatureExpr randomFEGen(NonStaticFeature[] options) {
		int i = (int) (Math.random() * options.length);
		FeatureExpr f = options[i].a;
		if (Math.random() < 0.2) {
			f = f.not();
		}
		if (Math.random() < 0.5) {
			return f;
		} else {
			if (Math.random() < 0.5)
				return f.and(randomFEGen(options));
			else
				return f.or(randomFEGen(options));
		}
	}

	public static FeatureExpr randomFEComlexity(NonStaticFeature[] options, int size) {
		if (size == 0)
			return FeatureExprFactory.True();
		if (options.length == size)
			return randomFEGen(options);
		else {
			int sz = (int) (Math.random() * (size + 1));
			// System.out.println(sz);
			FeatureExpr f = options[(int) (Math.random() * options.length)].a;
			for (int i = 0; i < sz - 1; i++) {
				if (Math.random() < 0.5) {
					f = f.and(options[(int) (Math.random() * options.length)].a);
				} else {
					f = f.or(options[(int) (Math.random() * options.length)].a);
				}
			}
			if (Math.random() < 0.8) {
				return f;
			} else {
				return f.not();
			}
		}
	}

	// generate next feature with possibility
	public static FeatureExpr nextFeatureExpr(FeatureExpr ctx, double possibility, int feComplexity,
			NonStaticFeature[] options) {

		if (Math.random() < possibility)
			return ctx;
		else
			return randomFEComlexity(options, feComplexity);
	}

	public static Conditional<Integer> randomCIGen(NonStaticFeature[] options, int sz) {
		if (sz == 0)
			return One.valueOf((int) (Math.random() * 100000));
		else {
			return ChoiceFactory.create(randomFEComlexity(options, 1), randomCIGen(options, sz - 1),
					randomCIGen(options, sz - 1));
		}
	}

	public static Conditional<Integer> ratioGen(NonStaticFeature[] options, double ratio, int sz) {
		if (Math.random() < ratio) {
			return randomCIGen(options, sz);
		} else {
			return randomCIGen(options, 0);
		}
	}
	
	
	
	static VSimplePQ vpq;
    
	private static long testPlainPQ(Conditional<Integer>[] conditionalValues, FeatureExpr[] fes) {
		//System.out.println("Conditional PQ");
		long start = System.nanoTime();
		ConditionalPQ cpq = new ConditionalPQ();
		for(int i = 0; i < conditionalValues.length; i++) {
			final int j = i;
			conditionalValues[i].mapf(fes[i], new VoidBiFunction<FeatureExpr, Integer>() {

				@Override
				public void apply(final FeatureExpr ctx, final Integer value) {
					cpq.add(value, ctx.and(fes[j]));
				}

			});
		} 
		List<Conditional> retCtx = new ArrayList<>();

		FeatureExpr curCtx = FeatureExprFactory.True(); 
		while(true) {
			Conditional<Integer> x = cpq.poll(curCtx);
			//System.out.println(x);
			Conditional<Boolean> tmp = cpq.isEmpty(FeatureExprFactory.True());
			curCtx = tmp.getFeatureExpr(false);
			if(curCtx.equivalentTo(FeatureExprFactory.False())) {
				break;
			}
			retCtx.add(x);
		}
		long end = System.nanoTime();
		long duration = (end - start);
		
		//System.out.println("pollplain(ctx) time : " + duration);
		return duration;
		
	}
	
	private static long testPollCtx(Conditional<Integer>[] conditionalValues, FeatureExpr[] fes) {
		//System.out.println("testPollCtx");
		long start = System.nanoTime();
		vpq = new VSimplePQ();
		for(int i = 0; i < conditionalValues.length; i++) {
			final int j = i;
			conditionalValues[i].mapf(fes[i], new VoidBiFunction<FeatureExpr, Integer>() {

				@Override
				public void apply(final FeatureExpr ctx, final Integer value) {
					vpq.add(value, ctx.and(fes[j]));
				}

			});
		} 
		List<Conditional> retCtx = new ArrayList<>();

		while(!vpq.isEmpty()) {
			Conditional<Integer> x = vpq.poll(FeatureExprFactory.True());
			//System.out.println(x);
			retCtx.add(x);
		}
		long end = System.nanoTime();
		long duration = (end - start);
		//System.out.println("poll(ctx) time : " + duration);
		return duration;
	}

	private static long testPoll(Conditional<Integer>[] conditionalValues, FeatureExpr[] fes) {
		//System.out.println("testPoll");
		long start = System.nanoTime();
		vpq = new VSimplePQ();
		for(int i = 0; i < conditionalValues.length; i++) {
			final int j = i;
			conditionalValues[i].mapf(fes[i], new VoidBiFunction<FeatureExpr, Integer>() {

				@Override
				public void apply(final FeatureExpr ctx, final Integer value) {
					vpq.add(value, ctx.and(fes[j]));
				}

			});
		} 
		List<Conditional> retList = new ArrayList<>();

		while(!vpq.isEmpty()) {
			Conditional<Integer> x = vpq.pollMin();
			//System.out.println(x);
			retList.add(x);
		}
		long end = System.nanoTime();
		long duration = (end - start);
		//System.out.println("poll() time    : " + duration);
		return duration;
	}
	
	private static long[] test(int num, int feComplexity, NonStaticFeature[] options) {
		//System.out.println("test");
		Conditional<Integer>[] conditionalValues = new Conditional[num];
		FeatureExpr[] fes = new FeatureExpr[num];
		FeatureExpr fe = randomFEComlexity(options, feComplexity);
		
		for (int i = 0; i < num; i++) {
			fes[i] = randomFEComlexity(options, feComplexity);	
		}
		
		for (int i = 0; i < conditionalValues.length; i++) {
			conditionalValues[i] = randomCIGen(options, 1);		
		}
		
//		for(int i = 0; i < conditionalValues.length; i++) {
//			System.out.println(fes[i] + " " + conditionalValues[i] + " ");
//		}
		long[] ret = new long[3];
		ret[0] = testPollCtx(conditionalValues, fes);
		ret[1] = testPoll(conditionalValues, fes);
		ret[2] = testPlainPQ(conditionalValues, fes);
		return ret;
	}
	
	public static void testCPQ() {
		ConditionalPQ cpq = new ConditionalPQ();
		FeatureExpr a = FeatureExprFactory.createDefinedExternal("a");
		cpq.add(5, a);
		cpq.add(2, a.not());
		
		System.out.println(cpq.poll(a));
		System.out.println(cpq.poll(a.not()));
	}
	
	
	public static void main(String[] args) {
		
		System.out.println("main");
		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
		int num = 50, feComplexity = 2;
		
		for(int i = 1; i <= 20; i++) {
			NonStaticFeature[] options = getOptions(i);
			
			int n = 10; 
			long pollCtxTime = -1, pollTime = -1, pollPlain = -1;
			long[] ret;
			for(int j = 0; j < n; j++) {
				ret = test(num, feComplexity, options);
				if(pollCtxTime == -1 || pollCtxTime > ret[0]) pollCtxTime = ret[0];
				if(pollTime == -1 || pollTime > ret[1]) pollTime = ret[1];
				if(pollPlain == -1 || pollPlain > ret[2]) pollPlain = ret[2];
//				pollCtxTime += ret[0];
//				pollTime += ret[1];
//				pollPlain += ret[2];
			}
			System.out.println("Feature is " + options.length + ", " + pollCtxTime/n  + " " + pollTime/n  + " " + pollPlain/n);
		
		}
	}
}
