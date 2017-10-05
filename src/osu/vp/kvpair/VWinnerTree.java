package osu.vp.kvpair;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import java.util.*;

/**
 * @author Meng Meng 
 */

public class VWinnerTree {
	private Map.Entry<Integer, Integer> makePair(int k, int v) {
		return new AbstractMap.SimpleEntry<Integer,Integer>(k, v);
	}
	
	private Conditional<Map.Entry<Integer, Integer>> mergeWinner(Conditional<Map.Entry<Integer, Integer>> l, Conditional<Map.Entry<Integer, Integer>> r) {
		
	}

	public class VWTNode {
		int l, r;
		VWTNode lt, rt;
		Conditional<Map.Entry<Integer, Integer>> winner;
		public VWTNode(int l, int r, Function<Integer, Conditional<Map.Entry<Integer, Integer>>> f) {
			this.l = l;
			this.r = r;
			lt = rt = null;
			if(f != null) {
				winner = f.apply(l);
			} else {
				winner = new One(makePair(l, 0));
			}
		}
		
		public VWTNode(VWTNode lt, VWTNode rt) {
			this.lt = lt;
			this.rt = rt;
			this.l = lt.l;
			this.r = rt.r;
			
			
		}
		
		
	}
	
	private int n;
	private VWTNode root;
	
	public VWinnerTree(int n) {
		this.n = n;
		this.root = buildtree(0, n - 1);
	}
	
	VWTNode buildtree(int l, int r) {
		if(l == r) {
			return new VWTNode(l, r);
		}
		int mid = (l+r)/2;
		VWTNode lt = buildtree(l, mid);
		VWTNode rt = buildtree(mid+1, r);
		return new VWTNode(lt, rt);
	}
	
}
