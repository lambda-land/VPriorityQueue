package osu.vp.kvpair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fosd.typechef.featureexpr.FeatureExpr;

class Vertex {
	int id;
	int arrive;
	List<Edge> edge = new ArrayList<>();

	public Vertex(Airport ap, int arrive) {
		this.id = ap.id;
		this.arrive = arrive;
	}

	public Vertex(int id, int arrive) {
		this.id = id;
		this.arrive = arrive;
	}

	public void addEdge(Edge e) {
		if (e.v != this)
			System.out.println("error");
		edge.add(e);
	}

	public int getTime() {
		return arrive;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(this.arrive / 60 + ":");
		sb.append(this.arrive % 60 + ", ");
		sb.append(id + ")");

		return sb.toString();
	}
}

class Edge {
	FeatureExpr fe;
	Vertex v;
	Vertex u;
	int weight;

//	public Edge(Airline al, Vertex v, Vertex u, int weight) {
//		this.fe = FeaturePool.getFe(al.unique_carrier);
//		this.v = v;
//		this.u = u;
//		this.weight = weight;
//	}

	public Edge(FeatureExpr fe, Vertex v, Vertex u, int weight) {
		// this.al = al;
		this.fe = fe;
		this.v = v;
		this.u = u;
		this.weight = weight;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(v);
		sb.append("--");
		sb.append(this.fe + " " + this.weight + "--");
		sb.append(u);
		return sb.toString();

	}
}

class Pair {
	int id;
	int arrive;

	public Pair(int id, int arrive) {
		this.id = id;
		this.arrive = arrive;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof Pair))
			return false;
		if (this == o)
			return true;
		Pair tmp = (Pair) o;
		return tmp.id == this.id && this.arrive == tmp.arrive;
	}

	@Override
	public int hashCode() {
		return id + arrive;
	}
}

public class Graph {
	List<Vertex> vertexes;
	Map<Pair, Vertex> id2v = new HashMap<>();
	Map<Integer, Map<Integer, Vertex>> a2v = new HashMap<>();



	public void addVertex(Vertex s) {
		Map<Integer, Vertex> map = a2v.get(s.id);
		for (Map.Entry<Integer, Vertex> entry : map.entrySet()) {
			if (entry.getKey() > s.arrive) {
				for (Edge e : entry.getValue().edge) {
					s.addEdge(new Edge(e.fe, s, e.u, e.weight + e.v.arrive - s.arrive));
				}
			} else {
				for (Edge e : entry.getValue().edge) {
					s.addEdge(new Edge(e.fe, s, e.u, 1440 - (s.arrive - e.v.arrive) + e.weight));
				}
			}
		}
//		for (Edge e : s.edge) {
//			System.out.println(e);
//		}

	}

	public Graph(AirlineGraph airlineGraph,	IVAirlineTrans trans) {
		HashMap<String, Airline> airlines = airlineGraph.airlines;
		for (Airline l : airlines.values()) {
			Airport dest = l.dest_airport;
			Airport origin = l.origin_airport;
			int arrive = l.crs_arr_time;
			if (!a2v.containsKey(dest.id)) {
				a2v.put(dest.id, new HashMap<>());
			}

			if (!a2v.containsKey(origin.id)) {
				a2v.put(origin.id, new HashMap<>());
			}
			Map<Integer, Vertex> vs = a2v.get(dest.id);
			Vertex vertex = new Vertex(dest, arrive);
			vs.put(arrive, vertex);
			id2v.put(new Pair(dest.id, arrive), vertex);

		}

		for (Airline al : airlines.values()) {

			Airport origin = al.origin_airport;
			Airport dest_airport = al.dest_airport;
			int crs_dep_time = al.crs_dep_time;
			int crs_arr_time = al.crs_arr_time;

			Map<Integer, Vertex> vs = a2v.get(origin.id);
			if (vs.size() == 0) {
				int time = Integer.MAX_VALUE;
				for (Airline e : origin.airlines) {
					time = Math.min(time, crs_dep_time);
				}
				time = time - 1;
				vs = new HashMap<>();
				vs.put(time, new Vertex(origin, time));
				a2v.put(origin.id, vs);

			}
			Vertex u = a2v.get(dest_airport.id).get(crs_arr_time);
			if(u == null) System.out.println("null there");
			for (Vertex v : vs.values()) {
				int w = 0;

				if (v.getTime() >= crs_dep_time) {
					w += 24 * 60 + crs_dep_time - v.getTime();
				} else {
					w += crs_dep_time - v.getTime();
				}

				if (crs_dep_time > crs_arr_time) {
					w += 24 * 60 + crs_arr_time - crs_dep_time;
				} else {
					w += crs_arr_time - crs_dep_time;
				}

				Edge e = new Edge(trans.VAirline(al), v, u, w);
				v.addEdge(e);

			}

		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, Map<Integer, Vertex>> entry : a2v.entrySet()) {
			for (Vertex v : entry.getValue().values()) {

				for (Edge e : v.edge) {
					sb.append(e.v.toString() + "--" + e.weight + "--" + e.u.toString());
					sb.append("\n");
				}

			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public Vertex getVertex(Pair pair) {
		return id2v.get(pair);
	}

	public static void main(String[] args) {
		AirlineDataSet dataset = new AirlineDataSet("data/test.csv");
		AirlineGraph airlineGraph = new AirlineGraph(dataset);
		String[] carrier = new String[]{"UA", "AA"};
		CarrierTrans ct = new CarrierTrans(carrier);
		Graph graph = new Graph(airlineGraph, ct);
		System.out.println(graph);
	}
}
