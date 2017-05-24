package osu.vp.kvpair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import osu.util.Triple;
import osu.util.Tuple;

public class VPriorityKey<T> implements IVPriorityKey<T> {
	
	Map<T, Conditional<Integer>> keyTable = new HashMap();
	TreeMap<Integer, VHashTable<T>> proiTable = new TreeMap();
	//IVPriorityQueue pq;
	
	//public VPriorityKey(IVPriorityQueue pq) {
	//	this.pq = pq;
	//}
	
	public VPriorityKey() {
		keyTable = new HashMap();
		proiTable = new TreeMap();
	}
	
	public void updateKey(FeatureExpr ctx, final T k, final Integer p) {
		if(ctx.isContradiction()) return;
		/*
		System.out.println("updateKey " + ctx + " " + k + " " + p);
		for(Map.Entry<Integer, VHashTable<T>> x : proiTable.entrySet()) {
			System.out.println(x);
		}
		*/
		Conditional<Integer> cp = keyTable.getOrDefault(k, (Conditional<Integer>)One.NULL);
		
		cp = cp.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
			@Override
			public Conditional<Integer> apply(FeatureExpr ctx, Integer y) {
				//System.out.println("updateKey " + ctx + " " + y);
				if(ctx.isContradiction() || y != null && p.compareTo(y) >= 0) {
					if(y == null) return (Conditional<Integer>)One.NULL;
					return One.valueOf(y);
				}
				
				if(y != null && proiTable.get(y) != null) {
					proiTable.get(y).remove(ctx, k);
				}
				
				if(proiTable.get(p) == null) {
					proiTable.put(p, new VHashTable());

				}
				proiTable.get(p).put(ctx, k);
				//pq.add(p, ctx);
				if(y == null) {
					return ChoiceFactory.create(ctx, One.valueOf(p), (Conditional<Integer>)One.NULL).simplify();		
				}
				return ChoiceFactory.create(ctx, One.valueOf(p), One.valueOf(y)).simplify();
			}
		}).simplify();
		
		keyTable.put(k, cp);
		
	}
	
	public Map.Entry<Integer, VHashTable<T>> peekMin() {
		Map.Entry<Integer, VHashTable<T>> e = proiTable.firstEntry();
		return e;
	}
	
	public Map.Entry<Integer, VHashTable<T>> peekMin(FeatureExpr fe) {
		return null;
	}
	
	private void removeKeyTable(VHashTable<T> t) {
		Map<T, FeatureExpr> map = t.getMap();
		
		for(Map.Entry<T, FeatureExpr> e : map.entrySet()) {
			T k = e.getKey();
			FeatureExpr ctx = e.getValue();
			
			Conditional<Integer> cp = keyTable.get(k);
			cp = cp.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
				@Override
				public Conditional<Integer> apply(FeatureExpr ctx, Integer y) {
					if(y == null) {
						return (Conditional<Integer>)One.NULL; 
					}
					if(ctx.isContradiction()) {
						return One.valueOf(y);			
					}
					return ChoiceFactory.create(ctx, (Conditional<Integer>)One.NULL, One.valueOf(y)).simplify();
				}
			}).simplify();
			
			keyTable.put(k, cp);
		}
	}
	
	
	private Map.Entry<Integer, VHashTable<T>> popMinEntry() {
		Map.Entry<Integer, VHashTable<T>> e = proiTable.firstEntry();
		/*
		for(Map.Entry<Integer, VHashTable<T>> x : proiTable.entrySet()) {
			System.out.println(x);
		}
		*/
		if(e == null) return null;
		removeKeyTable(e.getValue());
		proiTable.remove(e.getKey());
		return e;

	}

	@Override
	public Iterator<Triple<FeatureExpr, Integer, T>> popMin() {
		Map.Entry<Integer, VHashTable<T>> e = popMinEntry();
		if(e == null) return null;
		return new VHashTableIterator(e.getKey(), e.getValue());
	}
	
	
	public Iterator<Triple<FeatureExpr, Integer, T>> popMin(FeatureExpr f) {
		FeatureExpr ctx = f;
		List<Triple<FeatureExpr, Integer, T>> list = new LinkedList();
		
		for(Map.Entry<Integer, VHashTable<T>> e : proiTable.entrySet()) {
			if(e == null) return null;
			Integer proi = e.getKey();
			VHashTable<T> table = e.getValue();
			
			Tuple<List<Tuple<FeatureExpr, T>>, FeatureExpr> lc = table.pop(ctx);
			List<Tuple<FeatureExpr, T>> l = lc.getKey();
			ctx = lc.getValue();
			//System.out.println("ctx is " + ctx);
			for(Tuple<FeatureExpr, T> t : l) {
				list.add(new Triple(t.t1, proi, t.t2));
			}
			
			if(ctx.isContradiction()) break;
			
		}
		
		for(Triple<FeatureExpr, Integer, T> e : list) {
			final FeatureExpr fe = e.t1;
			T key = e.t3;
			
			Conditional<Integer> val = keyTable.get(key);
			val = val.mapfr(fe, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
				@Override
				public Conditional<Integer> apply(FeatureExpr ctx, Integer y) {
					if(y == null) {
						return (Conditional<Integer>)One.NULL; 
					}
					if(ctx.isContradiction()) {
						return One.valueOf(y);			
					}
					return ChoiceFactory.create(ctx, (Conditional<Integer>)One.NULL, One.valueOf(y)).simplify();
				}
			}).simplify();
			keyTable.put(key, val);
		}
		if(list.isEmpty()) return null;
		return list.iterator();
	}
	
	
	
	@Override
	public boolean popMinCallback(Function<Iterator<Triple<FeatureExpr, Integer, T>>, Boolean> callback) {
		Iterator<Triple<FeatureExpr, Integer, T>>  iter = popMin();
		Boolean b = callback.apply(iter);
		return b;
	}	
	
}


class VHashTableIterator<T> implements Iterator<Triple<FeatureExpr, Integer, T>> {
	private Integer priority;
	private VHashTable<T> table;
	private Iterator<Map.Entry<T, FeatureExpr>> iter;
	public VHashTableIterator(Integer priority, VHashTable<T> table) {
		this.priority = priority;
		this.table = table;
		this.iter = table.getMap().entrySet().iterator();
	}

	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public Triple<FeatureExpr, Integer, T> next() {
		Map.Entry<T, FeatureExpr> e = iter.next();
		if(e == null) return null;
		return new Triple(e.getValue(), this.priority, e.getKey());
	}
	
	
}