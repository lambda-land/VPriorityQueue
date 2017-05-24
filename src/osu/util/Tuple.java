package osu.util;

import java.util.Map;

public class Tuple<T1, T2> implements Map.Entry<T1, T2> {
	public T1 t1;
	public T2 t2;
	
	public Tuple(T1 t1, T2 t2) {
		this.t1 = t1;
		this.t2 = t2;
	}
	
	@Override
	public T1 getKey() {
		return t1;
	}
	
	@Override
	public T2 getValue() {
		return t2;
	}
	
	@Override
	public T2 setValue(T2 value) {
		T2 old = t2;
		t2 = value;
		return old;
	}
}
