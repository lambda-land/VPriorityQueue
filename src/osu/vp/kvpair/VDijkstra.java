package osu.vp.kvpair;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import osu.util.Triple;
import osu.vp.Util;
import osu.vp.kvpair.*;


/**
 * 
 * @author Meng Meng
 * VDijkstra + popMin(ctx)/popMin() + Graph
 *
 */
public class VDijkstra {
	public Graph graph;
	public IVAirlineTrans trans;
	public IVPriorityKey<Vertex> vpk;
	public int s, t;
	public FeatureExpr running;
	
	public Conditional<Integer> arrTime;
	public Map<Vertex, Conditional<Integer>> dist;
	
	public VDijkstra(Graph graph) {
		this.graph = graph;
	//	this.trans = trans;
	}
	
	
	public void set(int s, int t, int hour, IVPriorityKey<Vertex> _vpk) {
		this.s = s;
		this.t = t;
		System.out.println("source:" + s + ", target:" + t + ", dep time:" + hour + ":00");
		if(_vpk == null) {
			vpk = new VPriorityKey<Vertex>();
		} else {
			vpk = _vpk;
		}
		Vertex vertex = new Vertex(s, hour *60);
		graph.addVertex(vertex);
		
		//vpk = new VNaivePriorityKey<Integer>();
		dist = new HashMap<>();
		dist.put(vertex,new One<>(0));
		vpk.updateKey(FeatureExprFactory.True(), vertex, hour * 60);
		arrTime = (Conditional<Integer>)One.NULL;
	}
	
	
	public void run(FeatureExpr _running, boolean withCtx) {
		this.running = _running;
		if(running == null) {
			running = FeatureExprFactory.True();
		}
		while(!running.isContradiction()) {
			Iterator<Triple<FeatureExpr, Integer, Vertex>> e ;
			if(withCtx) {
				e = vpk.popMin(running);
			} else {
				e = vpk.popMin();
			} 
			
			if(e == null) {
				System.out.println(running + " NO path found");
				break;
			}
			
			while(e.hasNext()) {
				// FeatureExpr, Priority, Key
				// FeatureExpr, ArrTime, ID
				Triple<FeatureExpr, Integer, Vertex> triple = e.next();
				FeatureExpr ctx = triple.t1;
				int currTime = triple.t2;
				Vertex vertex = triple.t3;
				
				int day = currTime / 1440;
				int min = currTime % 1440;
				//System.out.println(vertex.id + " " + currTime +  " visited");
				
				if(vertex.id == t) {
					if(running.and(ctx).isContradiction()) {
						continue;
					}
					System.out.println(running.and(ctx) + " arr time:" + 
							(day > 0 ? ("day" + (day + 1)) + " " : "") + 
							min / 60 + ":" + min % 60);

					arrTime = Util.vmin(arrTime, ctx, currTime);
					running = running.andNot(ctx);
				//	System.out.println("running is " + running);
					continue;
				}
				Map<Integer, Vertex> vetice = graph.a2v.get(vertex.id);
				
				for(Edge al : vertex.edge) {
					FeatureExpr alCtx = al.fe;
					alCtx = ctx.and(alCtx).and(running);
					if(!alCtx.isContradiction()) {
						if(!dist.containsKey(al.u)) dist.put(al.u, new One<>(Integer.MAX_VALUE));
						UpdateFunction update = UpdateFunction.getInstance(currTime + al.weight);
						Conditional<Integer> tmp = dist.get(al.u).mapfr(alCtx, update).simplify(); 
						if(update.ctx != null) {
							vpk.updateKey(update.ctx.and(alCtx), al.u, currTime + al.weight);
						}

						dist.put(al.u, tmp);
					}
				
				}
			}
			
		}
	}

	public static class UpdateFunction implements BiFunction<FeatureExpr, Integer,  Conditional<Integer>> {
		public FeatureExpr ctx = null;
		public int prio = 0;
		
		private UpdateFunction(int p) {
			this.prio = p;
			this.ctx = null;
		}
		
		@Override
		public Conditional<Integer> apply(FeatureExpr fe, final Integer b) {
			if(fe.isContradiction() || b <= prio) return One.valueOf(b);
			if(ctx == null) {
				ctx = fe;
			} else {
				ctx = ctx.or(fe);
			}
			return ChoiceFactory.create(fe, One.valueOf(prio), One.valueOf(b));
		}
		
		private static final UpdateFunction instance = new UpdateFunction(0);
		public static UpdateFunction getInstance(int p) {
			instance.prio = p;
			instance.ctx = null;
			return instance;
		}
		
	}
	
	public static void simpleTest() {
		//AirlineDataSet dataset = new AirlineDataSet("data/test.csv");
		AirlineDataSet dataset = new AirlineDataSet("data/T_ONTIME.csv");
    	System.out.println("dataset is loaded.");
		AirlineGraph airGraph = new AirlineGraph(dataset);
		//String[] carrier = new String[]{"UA", "AA"};
		String[] carrier = new String[]{"UA", "AA", "DL", "OO", "HA"};
		CarrierTrans ct = new CarrierTrans(carrier);
		Graph graph = new Graph(airGraph, ct);
		System.out.println("graph is created.");
		VDijkstra vasp = new VDijkstra(graph);

		//vasp.set(1, 4, 5, new VPriorityKey<Vertex>());
		vasp.set(11298, 14100, 23, new VPriorityKey<Vertex>());
		
		long start, end;
		start = System.nanoTime();
		vasp.run(FeatureExprFactory.True(), false);
		end = System.nanoTime();
		System.out.println((end - start)/1e9);
		
		vasp.set(11298, 14100, 23, new VPriorityKey<Vertex>());
		start = System.nanoTime();
		vasp.run(FeatureExprFactory.True(), true);
		end = System.nanoTime();
		System.out.println((end - start)/1e9);
		
		
		
		ShortestPathBuiltin vsp = new ShortestPathBuiltin(graph);

		vsp.set(11298, 14100, 23, new VPriorityKey<Vertex>());
		
		start = System.nanoTime();
		vsp.run(FeatureExprFactory.True());
		end = System.nanoTime();
		System.out.println((end - start)/1e9);
	}

	public static void main(String[] args) throws FileNotFoundException{
		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
		//System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("console.out")), true));
		//compare2Impl();
		simpleTest();
	}
}
