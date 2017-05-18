package osu.vp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import osu.vp.kvpair.VHashTable;

/**
 * @author Meng Meng
 *
 */
class Graph {

    private Set<Node> nodes = new HashSet<>();

    public void addNode(Node nodeA) {
        nodes.add(nodeA);
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }
}

class Node {
    FeatureExpr fe;
    String name;
    Conditional<Integer> distance = One.valueOf(Integer.MAX_VALUE);
    public Map<Node, Integer> adjacentNodes = new HashMap<>();
    
    public Node(FeatureExpr e, String name) {
    	this.fe = e;
    	this.name = name;
    }

    public Map<Node, Integer> getAdjacentNodes() {
        return adjacentNodes;
    }
    public String toString() {
    	return name + ":" + distance;
    	
    	//return distance.toString() + " with " + fe;
    }
}


public class ShortestPath {
	Map<Integer, VHashTable<Node>> distTable = new HashMap();
	//RBPriorityQueue rbpq = new RBPriorityQueue();
	//VPriorityQueue rbpq = new VPriorityQueue();
	CPriorityQueue rbpq = new CPriorityQueue();
	public void init() {
		distTable = new HashMap();
		//rbpq = new RBPriorityQueue();
		rbpq = new CPriorityQueue();
	}
	
	private void updateNode(final FeatureExpr f, final Node t, final Integer dist) {
		//System.out.println("updateNode");
		
		t.distance = t.distance.mapfr(f, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
			@Override
			public Conditional<Integer> apply(FeatureExpr ctx, Integer y) {
				if(ctx.isContradiction() || dist.compareTo(y) >= 0) return One.valueOf(y);
				if(distTable.get(y) != null) {
					distTable.get(y).remove(ctx, t);
				}
				if(distTable.get(dist) == null) {
					distTable.put(dist, new VHashTable());

				}
				distTable.get(dist).put(ctx, t);
				rbpq.add(dist, ctx);
				return ChoiceFactory.create(ctx, One.valueOf(dist), One.valueOf(y));
			}
			
		}).simplify();
		System.out.println(t);
	}
	
	private void updateNodeTable(final FeatureExpr ctx, VHashTable<Node> table, final Integer dist) {
		Map<Node, FeatureExpr> map = table.getMap();

		//System.out.println("updateNT");

		for(Map.Entry<Node, FeatureExpr> e : map.entrySet()) {
			final Node node = e.getKey();
			FeatureExpr f = e.getValue().and(ctx);
			node.distance.mapfr(f, new VoidBiFunction<FeatureExpr, Integer>() {
				@Override
				public void apply(FeatureExpr f, final Integer a) {
					if(f.isContradiction()) return;
					if(a != dist) return;
					for(Map.Entry<Node, Integer> e : node.adjacentNodes.entrySet()) {
						FeatureExpr ctx_e = f.and(e.getKey().fe);
						if(ctx_e.isContradiction()) continue;
						updateNode(ctx_e, e.getKey(), e.getValue() + a);
					}	
				}
			});
		}
	}
	
	private void update() {
		System.out.println("before");

		//rbpq.preorder(rbpq.root.right);
		Conditional<Integer> cur = rbpq.poll(FeatureExprFactory.True());
		System.out.println("after");
		//rbpq.preorder(rbpq.root.right);

		System.out.println("cur " + cur);

		cur.mapfr(FeatureExprFactory.True(), new VoidBiFunction<FeatureExpr, Integer>() {
			@Override
			public void apply(FeatureExpr f, final Integer a) {
				VHashTable<Node> m = distTable.get(a);
				if(m == null) {
					m = new VHashTable();
					distTable.put(a, m);
				}
				updateNodeTable(f, m, a);
			}
		});
	}
	
	
	public void calculateShortestPathFromSource(Graph graph, Node source) {
		init();
		updateNode(FeatureExprFactory.True(), source, 0);
    	
    	while(rbpq.isEmpty(FeatureExprFactory.True()).getValue() == false) {
    		update();
    		for(Node node : graph.getNodes()) {
    			System.out.println(node);
    		}
    	}
    }
	
	
   
}