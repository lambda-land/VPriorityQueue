package osu.vp.kvpair;

import java.util.*;
import java.util.Map.Entry;

import cmu.conditional.*;
import de.fosd.typechef.featureexpr.FeatureExpr;
import osu.util.Triple;

public class VNaivePriorityKey<T> implements IVPriorityKey<T> {
	Map<T, Conditional<Integer>> keyTable = new HashMap();
	Conditional<PriorityQueue<Map.Entry<Integer, T>>> cpq = new One<PriorityQueue<Map.Entry<Integer, T>>>(new PriorityQueue());
	public VNaivePriorityKey() {
		
	}
	
	private void add2CPQ(final FeatureExpr fe, final T k, final Integer p) {
		cpq = cpq.mapfr(fe, new BiFunction<FeatureExpr, PriorityQueue<Map.Entry<Integer, T>>, Conditional<PriorityQueue<Map.Entry<Integer, T>>>>() {
			@Override
			public Conditional<PriorityQueue<Map.Entry<Integer, T>>> apply(FeatureExpr ctx,
					PriorityQueue<Map.Entry<Integer, T>> pq) {
				if(ctx.isContradiction()) {
					return new One<PriorityQueue<Map.Entry<Integer,T>>>(pq);
				}
				
				PriorityQueue<Map.Entry<Integer, T>> newPQ = new PriorityQueue<Map.Entry<Integer,T>>(pq);
				newPQ.add(new PriorityEntry<T>(p, k));
				
				return ChoiceFactory.create(ctx, new One<PriorityQueue<Map.Entry<Integer,T>>>(newPQ), new One<PriorityQueue<Map.Entry<Integer,T>>>(pq)).simplify();
			}
			
		}).simplify();
	}
	
	@Override
	public void updateKey(FeatureExpr ctx, final T k, final Integer p) {
		// TODO Auto-generated method stub
		Conditional<Integer> cp = keyTable.getOrDefault(k, (Conditional<Integer>)One.NULL);
		
		cp = cp.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
			@Override
			public Conditional<Integer> apply(FeatureExpr ctx, Integer y) {
				//System.out.println("updateKey " + ctx + " " + y);
				if(ctx.isContradiction() || y != null && p.compareTo(y) >= 0) {
					if(y == null) return (Conditional<Integer>)One.NULL;
					return One.valueOf(y);
				}

				add2CPQ(ctx, k, p);
				
				if(y == null) {
					return ChoiceFactory.create(ctx, One.valueOf(p), (Conditional<Integer>)One.NULL).simplify();		
				}
				return ChoiceFactory.create(ctx, One.valueOf(p), One.valueOf(y)).simplify();
			}
		}).simplify();
		
		keyTable.put(k, cp);
	}

	@Override
	public Iterator<Triple<FeatureExpr, Integer, T>> popMin() {
		Conditional<Map.Entry<Integer, T>> e = cpq.mapfr(null, new BiFunction<FeatureExpr, PriorityQueue<Map.Entry<Integer, T>>, Conditional<Map.Entry<Integer, T>>>() {
			@Override
			public Conditional<Entry<Integer, T>> apply(FeatureExpr ctx, PriorityQueue<Entry<Integer, T>> pq) {
				// TODO Auto-generated method stub
				return new One<Entry<Integer, T>>(pq.poll());
			}
		}).simplify();
		return new CPQEntryIterator<T>(e.toMap().entrySet().iterator());
	}

	@Override
	public boolean popMinCallback(Function<Iterator<Triple<FeatureExpr, Integer, T>>, Boolean> callback) {
		Iterator<Triple<FeatureExpr, Integer, T>> iter = popMin();
		boolean b = callback.apply(iter);
		return b;
	}

	@Override
	public Iterator<Triple<FeatureExpr, Integer, T>> popMin(FeatureExpr f) {
		// TODO Auto-generated method stub
		return null;
	}

}

class PriorityEntry<V> implements Map.Entry<Integer, V>, Comparable {
	Integer key;
	V value;
	
	public PriorityEntry(Integer k, V v) {
		key = k;
		value = v;
	}
	
	@Override
	public int compareTo(Object e) {
		if(e instanceof PriorityEntry) {
			return key.compareTo(((Entry<Integer, V>) e).getKey());
		}
		return 0;
	}

	@Override
	public Integer getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		V old = this.value;
		this.value = value;
		return old;
	}
	
}


class CPQEntryIterator<T> implements Iterator<Triple<FeatureExpr, Integer, T>>{
	Iterator<Entry<Entry<Integer, T>, FeatureExpr>> iter;
	Triple<FeatureExpr, Integer, T> nextItem = null;
	public CPQEntryIterator(Iterator<Entry<Entry<Integer, T>, FeatureExpr>> iter) {
		this.iter = iter;
		nextItem = nextHelper();
	}
	@Override
	public boolean hasNext() {
		return nextItem != null;
	}
	@Override
	public Triple<FeatureExpr, Integer, T> next() {
		Triple<FeatureExpr, Integer, T> e = nextItem;
		if(e != null) {
			nextItem = nextHelper();
		}
		return e;
	}
	
	private Triple<FeatureExpr, Integer, T> nextHelper() {
		Entry<Entry<Integer, T>, FeatureExpr> e = null;
		while(iter.hasNext()) {
			e = iter.next();
			if(e == null) return null;
			if(e.getKey() == null) continue;
			if(e.getKey().getKey() == null) continue;
			if(e.getKey().getValue() == null) continue;
			if(e.getValue() == null) continue;
			//System.out.println(e);
			return new Triple<FeatureExpr, Integer, T>(e.getValue(), e.getKey().getKey(), e.getKey().getValue());
			
		}
		
		return null;
	}
}