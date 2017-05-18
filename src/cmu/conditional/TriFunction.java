package cmu.conditional;

public interface TriFunction<T1, T2, T3, RETURN> {
	RETURN apply(T1 x, T2 y, T3 z);
}
