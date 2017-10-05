package osu.vp;

/**
 * @author Meng Meng 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import osu.vp.kvpair.IVPriorityQueue;



class VNode {
	FeatureExpr ctx;
	int id;
	List<VEdge> edges = new ArrayList<VEdge>();
	public VNode(FeatureExpr c, int id) {
		this.ctx = c;
		this.id = id;
	}
	public void addEdge(VEdge edge) {
		edges.add(edge);
	}
	
}

class VEdge {
	FeatureExpr ctx;
	VNode v, u;
	public VEdge(FeatureExpr c, VNode v, VNode u) {
		this.ctx = c;
		this.v = v;
		this.u = u;
	}
	
	public FeatureExpr isValid(FeatureExpr c) {
		return c.and(ctx);
	}
}

public class VGraph {
	private HashMap<Integer, VNode> nodes = new HashMap<Integer, VNode>();
	
	public VGraph() {
		
	}
	
	public void addNode(FeatureExpr c, int id) {
		VNode node = new VNode(c, id);
		nodes.put(id, node);
	}
	
	public void addEdge(FeatureExpr c, int v, int u) {
		if(!nodes.containsKey(v)) {
			nodes.put(v, new VNode(FeatureExprFactory.True(), v));
		}
		if(!nodes.containsKey(u)) {
			nodes.put(u, new VNode(FeatureExprFactory.True(), u));
		}
		VNode x = nodes.get(v);
		VNode y = nodes.get(u);
		VEdge edge = new VEdge(c, x, y);
		x.addEdge(edge);
	}
	
}

class ShorestPathAlgo {
	VGraph graph;
	IVPriorityQueue pq = new VSimplePQ();
	int s;
	int t;
	FeatureExpr ctx; 
	public ShorestPathAlgo(VGraph graph) {
		this.graph = graph;
	}
	
	public void config(int s, int t, FeatureExpr ctx) {
		
	}
}
