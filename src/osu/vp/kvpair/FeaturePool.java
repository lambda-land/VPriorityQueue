package osu.vp.kvpair;

import java.util.HashMap;
import java.util.Map;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

public class FeaturePool {
	private static Map<String, FeatureExpr> map = new HashMap<>();
	public static FeatureExpr getFe(String fl) {
		if(!map.containsKey(fl)) map.put(fl, FeatureExprFactory.createDefinedExternal(fl));
		return map.get(fl);
	}
}
