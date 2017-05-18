package osu.vp;

import cmu.conditional.ChoiceFactory;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

public class ShortestPathTest {
	

	public static void main(String[] args) {
		ChoiceFactory.activateMapChoice();
		Graph test = new Graph();
		Node s = new Node(FeatureExprFactory.True(), "s");
		Node a = new Node(VPriorityQueueTest.getFe("a"), "a");
		Node b = new Node(VPriorityQueueTest.getFe("b"), "b");
		Node t = new Node(FeatureExprFactory.True(), "t");
		
		test.addNode(s);
		test.addNode(a);
		test.addNode(b);
		test.addNode(t);
		s.adjacentNodes.put(a, 1);
		s.adjacentNodes.put(b, 10);
		a.adjacentNodes.put(t, 100);
		a.adjacentNodes.put(b, 1);
		b.adjacentNodes.put(t, 1);
		ShortestPath sp = new ShortestPath();
		sp.calculateShortestPathFromSource(test, s);
		System.out.println("t:" + t.distance);
	}
}
