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


class IDTrans implements IVAirlineTrans {

	@Override
	public FeatureExpr VAirline(Airline al) {
		return FeatureExprFactory.True();
	}

	@Override
	public FeatureExpr VAirport(Airport ap) {
		return FeatureExprFactory.True();
	}
}

class CarrierTrans implements IVAirlineTrans {
	public HashMap<String, FeatureExpr> options = new HashMap<>();
	public FeatureExpr other = null;
	public CarrierTrans(String[] carrier) {
		for(int i = 0; i < carrier.length; ++i) {
			options.put(carrier[i], FeatureExprFactory.createDefinedExternal(carrier[i]));
		}
		other = FeatureExprFactory.createDefinedExternal("OTHER");
	}
	
	public FeatureExpr getFeatureExpr(String carrier) {
		return options.getOrDefault(carrier, other);
	}
	
	@Override
	public FeatureExpr VAirline(Airline al) {
		FeatureExpr fe = options.getOrDefault(al.unique_carrier, other);
		return fe;
	}

	@Override
	public FeatureExpr VAirport(Airport ap) {
		return FeatureExprFactory.True();
	}
}

public class VAirlineShortestPath {
	public AirlineGraph graph;
	public IVAirlineTrans trans;
	public IVPriorityKey<Integer> vpk;
	public int s, t;
	public FeatureExpr running;
	
	public Conditional<Integer> arrTime;
	public VAirlineShortestPath(AirlineGraph graph, IVAirlineTrans trans) {
		this.graph = graph;
		this.trans = trans;
	}
	
	
	public void set(int s, int t, int hour, IVPriorityKey<Integer> _vpk) {
		this.s = s;
		this.t = t;
		System.out.println("source:" + s + ", target:" + t + ", dep time:" + hour + ":00");
		if(_vpk == null) {
			vpk = new VPriorityKey<Integer>();
		} else {
			vpk = _vpk;
		}
		//vpk = new VNaivePriorityKey<Integer>();
		vpk.updateKey(FeatureExprFactory.True(), s, hour * 60);
		arrTime = (Conditional<Integer>)One.NULL;
	}
	
//	public void run() {
//		FeatureExpr running = FeatureExprFactory.True();
//		while(!running.isContradiction()) {
//			Map.Entry<Integer, VHashTable<Integer>> e = vpk.popMin();
//			if(e == null) {
//				System.out.println(running + " NO path found");
//				break;
//			}
//			int currTime = e.getKey();
//			int day = currTime / 1440;
//			int min = currTime % 1440;
//			//if(day > 7) break;
//			//System.out.println("currTime: " + currTime);
//			
//			VHashTable<Integer> airports = e.getValue();
//			for(Map.Entry<Integer, FeatureExpr> ac : airports.getMap().entrySet()) {
//				int id = ac.getKey();
//				FeatureExpr ctx = ac.getValue();
//				if(id == t) {
//					int hour = min / 60;
//					System.out.println(ctx + " arr time:" + 
//							(day > 0 ? ("day" + (day + 1)) + " " : "") + 
//							min / 60 + ":" + min % 60);
//					arrTime = Util.vmin(arrTime, ctx, currTime);
//					running = running.andNot(ctx);
//				}
//				
//				Airport ap = graph.getAirport(id);
//				for(Airline al : ap.airlines) {
//					/*
//					if(al.crs_dep_time <= min) {
//						continue;
//					}
//					*/
//					int arr_time = day * 1440;
//					if(al.crs_dep_time <= min) {
//						arr_time += 1440;
//					}
//					
//					FeatureExpr alCtx = trans.VAirline(al);
//					alCtx = ctx.and(alCtx).and(running);
//					if(!alCtx.isContradiction()) {
//						//System.out.println("update " + alCtx + " " + al.dest_airport.id + " " + al.crs_dep_time + " " + al.crs_arr_time);
//						if(al.crs_dep_time > al.crs_arr_time) {
//							arr_time += 1440;
//						}
//						vpk.updateKey(alCtx, al.dest_airport.id, arr_time + al.crs_arr_time);
//						/*
//						if(al.crs_dep_time < al.crs_arr_time) {
//							vpk.updateKey(alCtx, al.dest_airport.id, day * 1440 + al.crs_arr_time);
//						} else {
//							vpk.updateKey(alCtx, al.dest_airport.id, (day + 1) * 1440 + al.crs_arr_time);
//						}
//						*/
//					}
//				}
//			}
//			
//		}
//	}
	
	
	public void run(FeatureExpr _running) {
		this.running = _running;
		if(running == null) {
			running = FeatureExprFactory.True();
		}
		while(!running.isContradiction()) {
			Iterator<Triple<FeatureExpr, Integer, Integer>> e = vpk.popMin();
			if(e == null) {
				//System.out.println(running + " NO path found");
				break;
			}
			
			while(e.hasNext()) {
				// FeatureExpr, Priority, Key
				// FeatureExpr, ArrTime, ID
				Triple<FeatureExpr, Integer, Integer> triple = e.next();
				FeatureExpr ctx = triple.t1;
				int currTime = triple.t2;
				int id = triple.t3;
				
				int day = currTime / 1440;
				int min = currTime % 1440;
				
				if(id == t) {
					System.out.println(ctx + " arr time:" + 
							min / 60 + ":" + min % 60);
					arrTime = Util.vmin(arrTime, ctx, currTime);
					running = running.andNot(ctx);
				}
				
				Airport ap = graph.getAirport(id);
				for(Airline al : ap.airlines) {
					int arr_time = day * 1440;
					if(al.crs_dep_time <= min) {
						arr_time += 1440;
					}
					
					FeatureExpr alCtx = trans.VAirline(al);
					alCtx = ctx.and(alCtx).and(running);
					if(!alCtx.isContradiction()) {
						if(al.crs_dep_time > al.crs_arr_time) {
							arr_time += 1440;
						}
						vpk.updateKey(alCtx, al.dest_airport.id, arr_time + al.crs_arr_time);
					}
				
				}
			}
			
		}
	}
	
	public void runCallback(FeatureExpr _running) {
		this.running = _running;
		if(running == null) {
			running = FeatureExprFactory.True();
		}
		boolean b;
		do {
			b = vpk.popMinCallback(new Function<Iterator<Triple<FeatureExpr, Integer, Integer>>, Boolean>() {
				@Override
				public Boolean apply(Iterator<Triple<FeatureExpr, Integer, Integer>> e) {
					if(e == null || !e.hasNext()) {
						System.out.println(running + " NO path found");
						return false;
					}

					while(e.hasNext()) {
						// FeatureExpr, Priority, Key
						// FeatureExpr, ArrTime, ID
						Triple<FeatureExpr, Integer, Integer> triple = e.next();
						FeatureExpr ctx = triple.t1;
						int currTime = triple.t2;
						int id = triple.t3;
						

						if(running.and(ctx).isContradiction()) {
							continue;
						}
						
						System.out.print("visit " + ctx + " " + id + " ");
						
						int day = currTime / 1440;
						int min = currTime % 1440;
					System.out.println(" " +(day > 0 ? ("day" + (day + 1)) + " " : "") + 
				        min / 60 + ":" + min % 60);
						if(id == t) {
							System.out.println(ctx + " arr time:" + 
									(day > 0 ? ("day" + (day + 1)) + " " : "") + 
									min / 60 + ":" + min % 60);
							arrTime = Util.vmin(arrTime, ctx, currTime);
							running = running.andNot(ctx);
						}
						
						Airport ap = graph.getAirport(id);
						for(Airline al : ap.airlines) {
							int arr_time = day * 1440;
							if(al.crs_dep_time <= min) {
								arr_time += 1440;
							}
							
							FeatureExpr alCtx = trans.VAirline(al);
							alCtx = ctx.and(alCtx).and(running);
							if(!alCtx.isContradiction()) {
								if(al.crs_dep_time > al.crs_arr_time) {
									arr_time += 1440;
								}
								vpk.updateKey(alCtx, al.dest_airport.id, arr_time + al.crs_arr_time);
							}
						
						}
					}
					
					return true;
				}
			});
			if(running.isContradiction()) {
				//System.out.println("Break");
				break;
			}
			System.out.println("current running feature is " + this.running);
		} while(b);
	}
	public static void simpleTest() {
		AirlineDataSet dataset = new AirlineDataSet("data/test.csv");
    	System.out.println("dataset is loaded.");
		AirlineGraph graph = new AirlineGraph(dataset);
		System.out.println("graph is created.");
		String[] carrier = new String[]{"UA", "AA"};
		CarrierTrans ct = new CarrierTrans(carrier);
		FeatureExpr running = ct.other.not();
		for(String s : carrier) {
			running = running.andNot(ct.getFeatureExpr(s));
		}
		running = running.not();
		running = running.and(ct.getFeatureExpr("UA").not());
		VAirlineShortestPath vasp = new VAirlineShortestPath(graph, ct);

		vasp.set(1, 4, 0, new VPriorityKey<Integer>());
		//vasp.set(1, 4, 1, new VNaivePriorityKey());
		vasp.runCallback(running);
	
	}
	public static void compare2Impl() {
		AirlineDataSet dataset = new AirlineDataSet("data/T_ONTIME.csv");
    	System.out.println("dataset is loaded.");
		AirlineGraph graph = new AirlineGraph(dataset);
		System.out.println("graph is created.");
		String[] carrier = new String[]{"UA", "AA"};
		CarrierTrans ct = new CarrierTrans(carrier);
		FeatureExpr running = ct.other.not();
		for(String s : carrier) {
			running = running.andNot(ct.getFeatureExpr(s));
		}
		running = running.not();
		long start = 0;
		VAirlineShortestPath vasp = new VAirlineShortestPath(graph, ct);
		System.out.println("VPriority Queue");
		for(int i = 0; i < 3; ++i) {
			start = System.nanoTime();
			vasp.set(11298, 14100, 23, new VPriorityKey<Integer>());
			vasp.runCallback(running);
			System.out.println((System.nanoTime() - start)/1e9);
		}
		System.out.println("\nCPriority Queue");
		for(int i = 0; i < 3; ++i) {
			start = System.nanoTime();
			vasp.set(11298, 14100, 23, new VNaivePriorityKey());
			vasp.runCallback(running);
			System.out.println((System.nanoTime() - start)/1e9);
		}
	}
	public static void main(String[] args) throws FileNotFoundException{
		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
		//System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("console.out")), true));
		//compare2Impl();
		simpleTest();
	}
}
