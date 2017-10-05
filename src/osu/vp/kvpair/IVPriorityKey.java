package osu.vp.kvpair;

import java.util.*;
import de.fosd.typechef.featureexpr.FeatureExpr;
import cmu.conditional.Function;
import osu.util.Triple;

/**
 * @author Meng Meng 
 */

public interface IVPriorityKey<T> {
	public void updateKey(FeatureExpr ctx, final T k, final Integer p);

	public Iterator<Triple<FeatureExpr, Integer, T>> popMin();
	
	public Iterator<Triple<FeatureExpr, Integer, T>> popMin(FeatureExpr f);
	
	public boolean popMinCallback(Function<Iterator<Triple<FeatureExpr, Integer, T>>, Boolean> callback);
	
	public int totalNode();
	
	public void setCtx(FeatureExpr fe);
	
	public FeatureExpr getCtx();
	

}
