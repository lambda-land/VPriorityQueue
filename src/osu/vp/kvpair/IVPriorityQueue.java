package osu.vp.kvpair;

import cmu.conditional.Conditional;
import de.fosd.typechef.featureexpr.FeatureExpr;

/**
 * @author Meng Meng 
 */

public interface IVPriorityQueue {
	public void add(final int val, final FeatureExpr e);
	public Conditional<Integer> poll(final FeatureExpr e);
	public Conditional<Integer> peek(final FeatureExpr e);
	public Conditional<Integer> pollMin();
	public Conditional<Integer> peekMin();
	public Conditional<Boolean> isEmpty(FeatureExpr e);
}
