package osu.vp.kvpair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cmu.conditional.BiFunction;
import cmu.conditional.Conditional;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

public class VHashTable<T> {
	private Map<T, FeatureExpr> map = new HashMap();
	public VHashTable() {
		
	}
	
	public Map<T, FeatureExpr> getMap() {
		return map;
	}
	
	public void remove(FeatureExpr f, T t) {
		FeatureExpr now = map.get(t);
		if(now == null) return;
		now = now.andNot(f);
		if(now.isContradiction()) {
			map.remove(t);
		} else {
			map.put(t, now);
		}
	}
	
	public void put(FeatureExpr f, T t) {
		FeatureExpr now = map.get(t);
		if(now == null) now = f;
		else now = now.or(f);
		map.put(t, now);
	}
	
	public FeatureExpr get(T t) {
		FeatureExpr now = map.get(t);
		if(now == null) return FeatureExprFactory.False();
		return now;
	}
	
	public FeatureExpr get(FeatureExpr f, T t) {
		FeatureExpr now = get(t);
		return now.and(f).simplify(f);
	}
	
//	public Conditional<T> get(FeatureExpr f) {
//		
//		for(Map.Entry<T, FeatureExpr> e : map.entrySet()) {
//			
//		}
//	}
}
