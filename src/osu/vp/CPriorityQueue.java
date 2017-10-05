package osu.vp;

/**
 * @author Meng Meng 
 */


import java.util.Arrays;
import java.util.PriorityQueue;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import osu.vp.*;
import osu.vp.kvpair.IVPriorityQueue;

public class CPriorityQueue implements IVPriorityQueue {
	Conditional<PriorityQueue<Integer>> cpq = new One<>(new PriorityQueue<Integer>());
	
	public CPriorityQueue() {
		
	}
	
	public void add(final int val, final FeatureExpr e){
		 cpq = cpq.mapfr(e,new BiFunction<FeatureExpr, PriorityQueue<Integer>, Conditional<PriorityQueue<Integer>>>(){
			@Override 
			public Conditional<PriorityQueue<Integer>> apply(FeatureExpr ctx, PriorityQueue<Integer> pq) {
				PriorityQueue<Integer> npq = new PriorityQueue(Arrays.asList((pq.toArray(new Integer[pq.size()]))));
				if(ctx.and(e).isContradiction()) {
					return new One<>(pq);
				}
				npq.add(val);
				return ChoiceFactory.create(ctx, new One<>(npq), new One<>(pq));
			}
		});
	}
	
	public Conditional<Integer> pollMin() {
		return poll(FeatureExprFactory.True());
		//throw new UnsupportedOperationException();
	}
	
	public Conditional<Integer> peekMin() {
		throw new UnsupportedOperationException();
	}
	
	
	public Conditional<Integer> poll(final FeatureExpr e){
		Conditional<Integer> ret = cpq.mapfr(e, new BiFunction<FeatureExpr, PriorityQueue<Integer>, Conditional<Integer>>(){
			@Override 
			public Conditional<Integer> apply(FeatureExpr ctx, PriorityQueue<Integer> pq) {
				if(ctx.and(e).isContradiction()) {
					return null;
				}
				return new One<>(pq.poll());
			}
		});
		return ret.simplify(e);
	}
	public void remove(final int val, final FeatureExpr e) {
		cpq.mapfr(e, new VoidBiFunction<FeatureExpr, PriorityQueue<Integer>>(){
			@Override 
			public void apply(FeatureExpr ctx, PriorityQueue<Integer> pq) {
				if(ctx.and(e).isContradiction()) {
					return;
				}
				pq.remove(val);
				return;
			}
		});		
	}
	public Conditional<Integer> peek(final FeatureExpr e) {
		Conditional<Integer> ret = cpq.mapfr(e, new BiFunction<FeatureExpr, PriorityQueue<Integer>, Conditional<Integer>>(){
			@Override 
			public Conditional<Integer> apply(FeatureExpr ctx, PriorityQueue<Integer> pq) {
				if(ctx.and(e).isContradiction()) {
					return null;
				}
				return new One<>(pq.peek());
			}
		});
		return ret.simplify(e);
	}
	
	public Conditional<Boolean> isEmpty(FeatureExpr e) {
		if(peek(FeatureExprFactory.True()).equals(One.NULL)) return new One<>(true);
		return new One<>(false);
	}
	public static void main(String[] args) {
		CPriorityQueue vpq = new CPriorityQueue();
		//ChoiceFactory.activateMapChoice();
		Conditional<Integer> x;
		vpq.add(2, VPriorityQueueTest.getFe("a"));
		System.out.println(vpq.peek(FeatureExprFactory.True()));
		vpq.add(3, VPriorityQueueTest.getFe("b"));
		System.out.println(vpq.peek(FeatureExprFactory.True()));
		vpq.add(2, VPriorityQueueTest.getFe("b"));
		System.out.println(vpq.peek(FeatureExprFactory.True()));
		vpq.add(1, VPriorityQueueTest.getFe("c"));
		System.out.println(vpq.peek(FeatureExprFactory.True()));
		

		x = vpq.poll(VPriorityQueueTest.getFe("b"));
		System.out.println("test1 " + x);
		System.out.println(vpq.cpq);

		x = vpq.poll(VPriorityQueueTest.getFe("b"));
		System.out.println("test2 " + x);
		System.out.println(vpq.cpq);
		
		x = vpq.poll(VPriorityQueueTest.getFe("a"));
		System.out.println("test3 " + x);
		System.out.println(vpq.cpq);
		
		x = vpq.poll(VPriorityQueueTest.getFe("c"));
		System.out.println("test4 " + x);
		System.out.println(vpq.cpq);
	}
}
