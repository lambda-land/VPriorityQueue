package osu.vp.kvpair;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import osu.util.Triple;

/**
 * @author Meng Meng 
 */

public class VPriorityKeyTest {

    FeatureExpr a = FeatureExprFactory.createDefinedExternal("a");
	FeatureExpr b = FeatureExprFactory.createDefinedExternal("b");
	FeatureExpr c = FeatureExprFactory.createDefinedExternal("c");

	@Before
	public void setUp() throws Exception {
		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
	}

//	@Test
//	public void testVPriorityKey() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testUpdateKey() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testPeekMin() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testPeekMinFeatureExpr() {
		VPriorityKey<Integer> vpq = new VPriorityKey();
	    FeatureExpr a = FeatureExprFactory.createDefinedExternal("a");
		FeatureExpr b = FeatureExprFactory.createDefinedExternal("b");
		FeatureExpr c = FeatureExprFactory.createDefinedExternal("c");
		vpq.updateKey(a, 100, 1);
		vpq.updateKey(b, 10, 1);
		vpq.updateKey(a, 200, 1);
		Iterator<Triple<FeatureExpr, Integer, Integer>> e = vpq.popMin(b);
		while(e.hasNext()) {
			Triple<FeatureExpr, Integer, Integer> triple = e.next();
			System.out.println(triple.t1 + " " + triple.t2 + " " +triple.t3);
		}
		System.out.println("----");
	    e = vpq.popMin(a);
		while(e.hasNext()) {
			Triple<FeatureExpr, Integer, Integer> triple = e.next();
			System.out.println(triple.t1 + " " + triple.t2 + " " +triple.t3);
		}
		System.out.println("----");
		e = vpq.popMin(b);
		while(e.hasNext()) {
			Triple<FeatureExpr, Integer, Integer> triple = e.next();
			System.out.println(triple.t1 + " " + triple.t2 + " " +triple.t3);
		}
		
	}

//	@Test
//	public void testPopMin() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testPopMinFeatureExpr() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testPopMinCallback() {
//		fail("Not yet implemented");
//	}

}
