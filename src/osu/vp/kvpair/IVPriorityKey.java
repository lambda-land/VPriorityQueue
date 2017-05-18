package osu.vp.kvpair;

import java.util.*;
import de.fosd.typechef.featureexpr.FeatureExpr;
import cmu.conditional.Function;
import osu.util.Triple;

public interface IVPriorityKey<T> {
	public void updateKey(FeatureExpr ctx, final T k, final Integer p);

	public Iterator<Triple<FeatureExpr, Integer, T>> popMin();
	
	public boolean popMinCallback(Function<Iterator<Triple<FeatureExpr, Integer, T>>, Boolean> callback);

}
