
package osu.vp;

/**
 * @author Meng Meng 
 */

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import osu.vp.kvpair.IVPriorityQueue;

import java.util.*;

public class VSimplePQ implements IVPriorityQueue {
	private Map<Integer, Conditional<Integer>> map = new TreeMap();
	private Conditional<Integer> min = null;
	private Conditional<Integer> vadd(Conditional<Integer> num, final FeatureExpr ctx, final int v) {
		if(num == null) return num;
		Conditional<Integer> ret = num.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
			@Override
			public Conditional<Integer> apply(FeatureExpr f, final Integer a) {
				Integer av = a + v;
				if(av < 0) av = 0;
				if(f.isContradiction() || av == a) return One.valueOf(a);
				if(f.isTautology()) return One.valueOf(av);
				return ChoiceFactory.create(f, One.valueOf(av), One.valueOf(a)).simplify();
			}
		}).simplify();
		return ret;
	}
	
	public VSimplePQ() {
	}
	
	
	public void add(int v, FeatureExpr ctx) {
		Conditional<Integer> c = map.get(v);
		if(c == null) {
			c = One.ZERO;
		}
		c = vadd(c, ctx, 1);
		map.put(v, c);
		min = null;
	}
	
	public Conditional<Integer> pollMin() {
		for(Map.Entry<Integer, Conditional<Integer>> e : map.entrySet()) {
			Integer t = e.getKey();
			FeatureExpr fe = e.getValue().getFeatureExpr(0).not();
			
			if(fe.isContradiction()) {
				map.remove(t);
				return pollMin();
			}
			
			Conditional<Integer> nv = vadd(e.getValue(), fe, -1);
			map.put(t, nv);
			return ChoiceFactory.create(fe, One.valueOf(e.getKey()), (One<Integer>)One.NULL);
		}
		return (One<Integer>)One.NULL;
	}
	
	public Conditional<Integer> peekMin() {
		for(Map.Entry<Integer, Conditional<Integer>> e : map.entrySet()) {
			Integer t = e.getKey();
			FeatureExpr fe = e.getValue().getFeatureExpr(0).not();
			if(fe.isContradiction()) {
				map.remove(t);
				return peekMin();
			}
			return ChoiceFactory.create(fe, One.valueOf(e.getKey()), (One<Integer>)One.NULL);
		}
		return (One<Integer>)One.NULL;
	}
	
	
	
	public Conditional<Integer> poll(final FeatureExpr ctx) {
		Conditional<Integer> ret = peek(ctx);
		ret.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
			@Override
			public Conditional<Integer> apply(FeatureExpr f, final Integer v) {
				if(v != null && !f.isContradiction()) {
					Conditional<Integer> c = map.get(v);
					c = vadd(c, f, -1);
					if(c.equals(One.ZERO)) {
						map.remove(v);
					} else {
						map.put(v, c);
					}
				}
				return (One<Integer>)One.NULL;
			}
		});
		min = null;
		return ret;
	}
	
	
	public Conditional<Integer> peek(FeatureExpr ctx) {
		if(min != null) return min;
		List<Integer> lc = new ArrayList();
		List<FeatureExpr> lf = new ArrayList();
		
		for(Map.Entry<Integer, Conditional<Integer>> e : map.entrySet()) {
			Conditional<Integer>[] s = e.getValue().split(ctx);
			if(s[0].equals(One.ZERO)) continue;
			FeatureExpr fe = s[0].getFeatureExpr(0);
			lc.add(e.getKey());
			lf.add(fe.not());
			ctx = ctx.and(s[0].getFeatureExpr(0));
			if(ctx.isContradiction()) {
				break;
			}
		}
		Conditional<Integer> ret = (One<Integer>)One.NULL;
		for(int i = lc.size() - 1; i >= 0; --i) {
			if(lf.get(i).isTautology()) {
				ret = One.valueOf(lc.get(i));
			} else if(!lf.get(i).isContradiction()) {
				ret = ChoiceFactory.create(lf.get(i), One.valueOf(lc.get(i)), ret);
			}
		}
		min = ret;
		return min;
	}
	public void print() {
		System.out.println(map);
	}

	@Override
	public Conditional<Boolean> isEmpty(FeatureExpr e) {
		if(peek(e).equals(One.NULL)) return One.TRUE;
		return One.FALSE;
	}
	/*
	public Conditional<Boolean> isEmpty() {
		if(peekMin().equals(One.NULL)) return One.TRUE;
		return One.FALSE;
	}
	*/
	public boolean isEmpty() {
		if(peekMin().equals(One.NULL)) return true;
		return false;
	}
}
