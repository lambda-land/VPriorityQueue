package cmu.conditional;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import gov.nasa.jpf.vm.MJIEnv;

/**
 * Non conditional value.
 * 
 * @author Jens Meinicke
 *
 * @param <T>
 */
public class One<T> extends Conditional<T> implements Cloneable {

	public static final One<?> NULL = new One<>(null);
	public static final One<Boolean> FALSE = new One<>(Boolean.FALSE);
	public static final One<Boolean> TRUE = new One<>(Boolean.TRUE);
	public static final One<Integer> MJIEnvNULL = new One<>(MJIEnv.NULL);
	
	public static final One<Integer> ONE = new One<>(1);
	public static final One<Integer> ZERO = new One<>(0);
	public static final One<Integer> NEG_ONE = new One<>(-1);
	
	private T value;

	public One(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public T getValue(final boolean ignore) {
		return getValue();
	}

	@Override
	public <U> Conditional<U> mapfr(FeatureExpr inFeature, BiFunction<FeatureExpr, T, Conditional<U>> f) {
		return f.apply(inFeature, value);
	}

	@Override
	public void mapfr(FeatureExpr inFeature, VoidBiFunction<FeatureExpr, T> f) {
		f.apply(inFeature, value);
	}

	@Override
	public String toString() {
		String s = "One(";
		if (value instanceof int[]) {
			s += Arrays.toString((int[]) value);
		} else if (value instanceof long[]) {
			s += Arrays.toString((long[]) value);
		} else if (value instanceof double[]) {
			s += Arrays.toString((double[]) value);
		} else if (value instanceof float[]) {
			s += Arrays.toString((float[]) value);
		} else if (value instanceof char[]) {
			s += Arrays.toString((char[]) value);
		} else {
			s += value;
		}
		return s + ')';
	}

	@Override
	public Conditional<T> simplify(FeatureExpr ctx) {
		if (ctx == null) {
			throw new RuntimeException("ctx == null");
		}
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof One) {
			if (value == ((One<?>) obj).value) {
				return true;
			}
			if (value instanceof char[]) {
				Arrays.equals((char[]) value, (char[]) ((One<?>) obj).value);
			}

			if (((One<?>) obj).value != null && value != null && ((One<?>) obj).value.equals(value))
				return true;
		}
		return false;
	}

	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public List<T> toList() {
		List<T> list = new LinkedList<>();
		list.add(value);
		return list;
	}

	@Override
	protected void toMap(FeatureExpr f, Map<T, FeatureExpr> map) {
		FeatureExpr ctx = map.get(value);
		if (ctx == null) {
			map.put(value, f);
		} else {
			map.put(value, f.or(ctx));
		}
	}

	@Override
	public Conditional<T> clone() {
		return new One<>(value);
	}

	@Override
	public Conditional<T> simplifyValues() {
		return this;
	}

	@Override
	public int size() {
		return 1;
	}
	
	/*################################################
	 * cache methods for int, byte, char
	 */
	
	public static One<Boolean> valueOf(boolean value) {
		return value ? TRUE : FALSE;
	}
	
	/**
	 * Only use this for values: -128 <= x <= 128 
	 * @return Returns a cached One<Integer>
	 */
	public static One<Integer> valueOf(int value) {
		if (-128 <= value && value <= 128) {
			return IntegerCache.cache[value + 128];
		}
		return new One<>(value);
	}
	
	@SuppressWarnings("unchecked")
	private static class IntegerCache {
		static final One<Integer>[] cache = new One[128- (-128) + 1];

		static {
			int j = -128;
			for (int k = 0; k < cache.length; ++k) {
				cache[k] = new One<>(Integer.valueOf(j++));
			}
		}
	}
		
	public static One<Byte> valueOf(byte value) {
		return ByteCache.cache[value - Byte.MIN_VALUE];
	}
	
	@SuppressWarnings("unchecked")
	private static class ByteCache {
		static final One<Byte>[] cache = new One[-Byte.MIN_VALUE + Byte.MAX_VALUE + 1];

		static {
			byte j = Byte.MIN_VALUE;
			for (int k = 0; k < cache.length; ++k) {
				cache[k] = new One<>(Byte.valueOf(j++));
			}
		}
	}
	
	public static One<Character> valueOf(char c) {
		if (c <= 127) {
			return CharacterCache.cache[(int)c];
		}
		return new One<>(c);
	}
	
	@SuppressWarnings("unchecked")
	private static class CharacterCache {
		static final One<Character>[] cache = new One[127 + 1];

		static {
			for (int k = 0; k < cache.length; k++) {
				cache[k] = new One<>(Character.valueOf((char)k));
			}
		}
	}

	@Override
	public boolean isOne() {
		return true;
	}
	
	@Override
    public FeatureExpr getFeatureExpr(T t) {
		if(value == t) return FeatureExprFactory.True();
		if(value != null && t != null && value.equals(t)) return FeatureExprFactory.True();
		return FeatureExprFactory.False();
	}
    
	@Override
    public Conditional<T>[] split(FeatureExpr ctx) {
    	return new Conditional[]{this, this};
    }
	
	@Override
    public <U,Y> Conditional<U> fastApply(final Conditional<Y> rhs, final BiFunction<T, Y, Conditional<U>> f) {
		return rhs.mapfr(FeatureExprFactory.True(), new BiFunction<FeatureExpr, Y, Conditional<U>>() {
			public Conditional<U> apply(final FeatureExpr cc, final Y y) {
				return f.apply(value, y);
			}
		});
    }
	
	@Override
	public int depth() {
	    return 1;
	}
}