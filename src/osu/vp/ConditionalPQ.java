package osu.vp;

/**
 * @author Meng Meng 
 */


import java.util.PriorityQueue;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.va.Stack;
import osu.vp.kvpair.IVPriorityQueue;

public class ConditionalPQ implements IVPriorityQueue{
	Conditional<PriorityQueue<Integer>> vpq;
	
	public ConditionalPQ(){
		vpq = new One(new PriorityQueue<Integer>());
	}
	@Override
	public void add(final int val, final FeatureExpr e) {
		vpq = vpq.mapf(FeatureExprFactory.True(), new BiFunction<FeatureExpr, PriorityQueue<Integer>, Conditional<PriorityQueue<Integer>>>(){
			@Override
			public Conditional<PriorityQueue<Integer>> apply(FeatureExpr ctx, PriorityQueue<Integer> pq) {
				FeatureExpr ctxande = ctx.and(e);
				if(ctxande.isContradiction()) return new One(pq);
				
				if(ctxande.equivalentTo(ctx)) {
					pq.add(val);
					return new One(pq);
				}
				
				PriorityQueue<Integer> tmp = new PriorityQueue<Integer>(pq);
				tmp.add(val);
				return ChoiceFactory.create(ctxande, new One<>(tmp), new One<>(pq));
			}
		}); 
	}

	@Override
	public Conditional<Integer> poll(final FeatureExpr ctx) {
		//System.out.println(ctx);
		Conditional<Integer> result = vpq.simplify(ctx).mapf(ctx, new BiFunction<FeatureExpr, PriorityQueue<Integer>, Conditional<Integer>>() {

			@SuppressWarnings("unchecked")
			@Override
			public Conditional<Integer> apply(final FeatureExpr f, final PriorityQueue<Integer> pq) {
				if(f.isContradiction() || pq == null || pq.isEmpty()) return null;
				
				if(f.and(ctx).equivalentTo(f)) {
					final int res = pq.poll();
					return (Conditional<Integer>) new One<>(Integer.valueOf(res));
				}
				
				PriorityQueue<Integer> clone = new PriorityQueue(pq);
				Integer res;
				final int lo = clone.poll();
				res = Integer.valueOf(lo);
				if(f.isTautology()) vpq = new One<>(clone);
				else vpq = ChoiceFactory.create(f, new One<>(clone), vpq);
				return (Conditional<Integer>) new One<>(res);
			}
		});
		vpq = vpq.simplify();
		return result;
	}

	@Override
	public Conditional<Integer> peek(FeatureExpr ctx) {
		return vpq.simplify(ctx).map(new Function<PriorityQueue<Integer>, Integer>() {

			@SuppressWarnings("unchecked")
			@Override
			public Integer apply(PriorityQueue<Integer> pq) {
				return pq.peek();
			}
		}).simplifyValues();
	}

	@Override
	public Conditional<Integer> pollMin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Conditional<Integer> peekMin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Conditional<Boolean> isEmpty(FeatureExpr e) {
	    return vpq.mapf(e,  new BiFunction<FeatureExpr, PriorityQueue<Integer>, Conditional<Boolean>>(){

			@Override
			public Conditional<Boolean> apply(FeatureExpr f, PriorityQueue<Integer> y) {
				return new One<>(y.isEmpty());
			}
	    	
	    }).simplify(e);
	}

}
