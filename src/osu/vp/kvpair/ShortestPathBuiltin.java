package osu.vp.kvpair;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import cmu.conditional.Conditional;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import osu.vp.*;

/**
 * 
 * @author Meng Meng
 * Built-in implementation 
 *
 */

class Entry implements Comparable<Entry>{
    Vertex v;
    int distance;
    public FeatureExpr fe;
    public Entry(Vertex v, int distance) {
        this.v = v;
        this.distance = distance;
    }
    public int compareTo(Entry o) {
        return this.distance - o.distance;
    }
}

public class ShortestPathBuiltin {
	public int max = 0;
	public Graph graph;
	public IVAirlineTrans trans;
	public PriorityQueue<Entry> vpk;
	public int s, t;
	public FeatureExpr running;
	
	public Conditional<Integer> arrTime;
	public Map<Vertex, Conditional<Integer>> dist;

	public ShortestPathBuiltin(Graph graph) {
		this.graph = graph;

	}

	
	public void set(int s, int t, int hour, IVPriorityKey<Vertex> _vpk) {
		this.s = s;
		this.t = t;
		System.out.println("source:" + s + ", target:" + t + ", dep time:" + hour + ":00");
		vpk = new PriorityQueue<>();
		Vertex vertex = new Vertex(s, hour *60);
		graph.addVertex(vertex);
		Entry entry = new Entry(vertex, hour*60);
		entry.fe = FeatureExprFactory.True();
		vpk.add(entry);
		dist = new HashMap<>();
		dist.put(vertex, new One<>(0));
		arrTime = (Conditional<Integer>)One.NULL;
	}
	
	
	public void run(FeatureExpr _running) {
		
		max = Math.max(max, vpk.size());
		Map<Vertex, Integer> map = new HashMap<>();
	 
		this.running = _running;
		if(running == null) {
			running = FeatureExprFactory.True();
		}
		while(!running.isContradiction()) {
			max = Math.max(max, vpk.size());
			Entry entry = vpk.poll();
			if(entry == null) {
				System.out.println(running + " NO path found");
				break;
			}
			
			Vertex vertex = entry.v;
			FeatureExpr ctx = entry.fe;
		
			int currTime = entry.distance;
			//System.out.println(vertex + " " + ctx + " " + currTime);
			
			int day = currTime / 1440;
			int min = currTime % 1440;
			//System.out.println(vertex.id + " " + currTime +  " visited");
			
			if(vertex.id == t) {
				if(!ctx.and(running).isContradiction()) {
					System.out.println(ctx.and(running) + " arr time:" + 
							(day > 0 ? ("day" + (day + 1)) + " " : "") + 
							min / 60 + ":" + min % 60);

					arrTime = Util.vmin(arrTime, ctx, currTime);
					running = running.andNot(ctx);
				}
				continue;
			}
			
			
			for(Edge al : vertex.edge) {
				FeatureExpr alCtx = al.fe;
				alCtx = ctx.and(alCtx).and(running);
				//System.out.println(" hi " + alCtx);
				if(!alCtx.isContradiction()) {
					if(!dist.containsKey(al.u)) dist.put(al.u, new One<>(Integer.MAX_VALUE));
					VDijkstra.UpdateFunction update = VDijkstra.UpdateFunction.getInstance(currTime + al.weight);
					Conditional<Integer> tmp = dist.get(al.u).mapfr(alCtx, update).simplify(); 
					if(update.ctx != null) {
						//vpk.updateKey(update.ctx.and(alCtx), al.u, currTime + al.weight);
						Entry newEntry = new Entry(al.u,  currTime + al.weight);
						newEntry.fe = alCtx;
						vpk.add(newEntry);
					}

					dist.put(al.u, tmp);
				}
				
			}
		}
	}
	
	
	public static void simpleTest() {
		//AirlineDataSet dataset = new AirlineDataSet("data/test.csv");
		AirlineDataSet dataset = new AirlineDataSet("data/T_ONTIME.csv");
    	System.out.println("dataset is loaded.");
		AirlineGraph airGraph = new AirlineGraph(dataset);
		String[] carrier = new String[]{"UA", "AA"};
		CarrierTrans ct = new CarrierTrans(carrier);
		Graph graph = new Graph(airGraph, ct);
		System.out.println("graph is created.");
		ShortestPathBuiltin vasp = new ShortestPathBuiltin(graph);
		//vasp.set(1, 4, 5, new VPriorityKey<Vertex>());
		vasp.set(11298, 14100, 23, new VPriorityKey<Vertex>());
		vasp.run(FeatureExprFactory.True());
	
	}

	public static void main(String[] args) throws FileNotFoundException{
		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
		simpleTest();
	}
}