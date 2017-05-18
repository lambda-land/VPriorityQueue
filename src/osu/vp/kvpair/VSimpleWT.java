package osu.vp.kvpair;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import java.util.*;

public class VSimpleWT {
	private Map<Integer, VHashTable<Integer>> pqMap = new TreeMap();
	private Map<Integer, Conditional<Integer>> priority = new HashMap();
	public void update(final FeatureExpr ctx, final int k, final Integer p) {
		Conditional<Integer> old_p = priority.get(k);
		Conditional<Integer> new_p;
		if(old_p != null) {
			new_p = old_p.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
				@Override
				public Conditional<Integer> apply(FeatureExpr x, Integer y) {
					if(x.isContradiction()) return One.valueOf(y);
					if(y != null) {
						pqMap.get(y).remove(ctx, k);
					}
					return ChoiceFactory.create(ctx, One.valueOf(p), One.valueOf(y));
				}
			});
		} else {
			new_p = ChoiceFactory.create(ctx, One.valueOf(p), (One<Integer>)One.NULL);
		}
		priority.put(k, new_p);
		
		if(p == null) return;
		
		VHashTable<Integer> f = pqMap.get(p);
		if(f == null) {
			f = new VHashTable<Integer>();
		}
		f.put(ctx, k);
		pqMap.put(p, f);
		
	}
	
	public Conditional<Integer> poll(final FeatureExpr ctx) {
		for(Map.Entry<Integer, VHashTable<Integer>> e : pqMap) {
			
		}
	}
	
	public Conditional<Integer> peek(final FeatureExpr ctx) {
		for(Map.Entry<Integer, VHashTable<Integer>> e : pqMap) {
			e.getValue().get();
		}
	}
}
