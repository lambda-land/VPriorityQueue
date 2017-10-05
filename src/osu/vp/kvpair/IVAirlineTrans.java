package osu.vp.kvpair;

import de.fosd.typechef.featureexpr.FeatureExpr;

/**
 * @author Meng Meng 
 */

public interface IVAirlineTrans {
	public FeatureExpr VAirline(Airline al);
	public FeatureExpr VAirport(Airport ap);
}
