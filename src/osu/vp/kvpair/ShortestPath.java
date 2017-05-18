package osu.vp.kvpair;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;

import cmu.conditional.Conditional;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import osu.util.Triple;
import osu.vp.Util;



public class ShortestPath {
	public Graph graph;
	public IVAirlineTrans trans;
	public IVPriorityKey<Vertex> vpk;
	public int s, t;
	public FeatureExpr running;
	
	public Conditional<Integer> arrTime;
	public ShortestPath(Graph graph) {
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
		vpk.updateKey(FeatureExprFactory.True(), vertex, hour * 60);
		arrTime = (Conditional<Integer>)One.NULL;
	}
	
	
	public void run(FeatureExpr _running) {
		this.running = _running;
		if(running == null) {
			running = FeatureExprFactory.True();
		}
		while(!running.isContradiction()) {
			Iterator<Triple<FeatureExpr, Integer, Vertex>> e = vpk.popMin();
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
					if(!ctx.and(running).isContradiction()) {
						System.out.println(ctx.and(running) + " arr time:" + 
								(day > 0 ? ("day" + (day + 1)) + " " : "") + 
								min / 60 + ":" + min % 60 + " " + currTime);
	
						arrTime = Util.vmin(arrTime, ctx, currTime);
						running = running.andNot(ctx);
					}
					continue;
				}
				
				Map<Integer, Vertex> vetice = graph.a2v.get(vertex.id);
				
				for(Edge al : vertex.edge) {
					FeatureExpr alCtx = al.fe;
					alCtx = ctx.and(alCtx).and(running);
					if(!alCtx.isContradiction()) {
						vpk.updateKey(alCtx, al.u, currTime + al.weight);
					}
				
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
//		String[] carrier = new String[]{"UA", "AA"};
//		CarrierTrans ct = new CarrierTrans(carrier);
//		FeatureExpr running = ct.other.not();
//		for(String s : carrier) {
//			running = running.andNot(ct.getFeatureExpr(s));
//		}
//		running = running.not();
//		running = running.and(ct.getFeatureExpr("UA").not());
		
		ShortestPath vasp = new ShortestPath(graph);

		//vasp.set(1, 4, 5, new VPriorityKey<Vertex>());
		vasp.set(11298, 14100, 23, new VPriorityKey<Vertex>());
		vasp.run(FeatureExprFactory.True());
	
	}

	public static void main(String[] args) throws FileNotFoundException{
		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
		//System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("console.out")), true));
		//compare2Impl();
		simpleTest();
	}
}
