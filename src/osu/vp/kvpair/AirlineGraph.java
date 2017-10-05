package osu.vp.kvpair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author Meng Meng 
 */

class Airport {
	public int id;
	public List<Airline> airlines = new ArrayList<>();
	
	public Airport(int id) {
		this.id = id;
	}
	
	public void addAirline(Airline al) {
		this.airlines.add(al);
	}
	
	public void initDone() {
		airlines.sort(null);
	}
	
	@Override
    public int hashCode() {
		return id;
	}
	
}

class Airline implements Comparable<Airline> {
	public String unique_carrier;
	public String fl_num;
	public Airport origin_airport;
	public Airport dest_airport;
	public int crs_dep_time;
	public int crs_arr_time;
	public int crs_elapsed_time;	
	
	
	public Airline(String carrier, String fl_num, Airport origin, Airport dest, int dep_time, int arr_time, int elapsed_time) {
		this.unique_carrier = carrier;
		this.fl_num = fl_num;
		this.origin_airport = origin;
		this.dest_airport = dest;
		this.crs_dep_time = dep_time;
		this.crs_arr_time = arr_time;
		this.crs_elapsed_time = elapsed_time;
	}
	
	@Override
	public String toString() {
		return this.unique_carrier + this.fl_num + " " + this.origin_airport.id + " " + this.dest_airport.id + " " + this.crs_dep_time + " " + this.crs_arr_time;
	}
	
	public String toStringWithoutArrTime() {
		return this.unique_carrier + this.fl_num + " " + this.origin_airport.id + " " + this.dest_airport.id + " " + this.crs_dep_time;
	}
	
	@Override
    public int hashCode() {
		int x = unique_carrier.hashCode() + fl_num.hashCode() + 
				Integer.hashCode(this.origin_airport.hashCode()) + 
				Integer.hashCode(this.dest_airport.hashCode()) + 
				Integer.hashCode(this.crs_dep_time) + 
				Integer.hashCode(this.crs_arr_time);
		return x;
	}
	
	@Override
    public boolean equals(Object obj) {
		if(obj == null) return false;
		if(!(obj instanceof Airline)) return false;
		if(obj.hashCode() != this.hashCode()) return false;
		Airline al = (Airline)obj;
		if(this.unique_carrier != al.unique_carrier) return false;
		if(this.fl_num != al.fl_num) return false;
		if(this.origin_airport != al.origin_airport) return false;
		if(this.dest_airport != al.dest_airport) return false;
		if(this.crs_dep_time != al.crs_dep_time) return false;
		if(this.crs_arr_time != al.crs_arr_time) return false;
		return true;
	}

	@Override
	public int compareTo(Airline al) {
		if(this.origin_airport.id != al.origin_airport.id) {
			return this.origin_airport.id - al.origin_airport.id;
		}
		
		if(this.dest_airport.id != al.dest_airport.id) {
			return this.dest_airport.id - al.dest_airport.id;
		}
		
		if(this.crs_dep_time != al.crs_dep_time) {
			return this.crs_dep_time - al.crs_dep_time;
		}
		
		
		return this.crs_arr_time - al.crs_arr_time;
	}
}

public class AirlineGraph {
	public HashMap<Integer, Airport> airports = new HashMap<>();
	public HashMap<String, Airline> airlines = new HashMap<>();
	
	public AirlineGraph(AirlineDataSet ds) {
		for(int i = 0; i < ds.size(); ++i) {
			String carrier = ds.unique_carrier.get(i);
			String fl_num = ds.fl_num.get(i);
			int origin_id = ds.origin_airport_id.get(i);
			int dest_id = ds.dest_airport_id.get(i);
			int dep_time = ds.crs_dep_time.get(i);
			int arr_time = ds.crs_arr_time.get(i);
			int elapsed_time = ds.crs_elapsed_time.get(i);
			addAirline(carrier, fl_num, origin_id, dest_id, dep_time, arr_time, elapsed_time);
		}
		initDone();
	}
	
	public Airport getAirport(int id) {
		Airport ret = airports.get(id);
		if(ret == null) {
			ret = new Airport(id);
			airports.put(id, ret);
		}
		return ret;
	}
	
	public void addAirline(String carrier, String fl_num, int origin_id, int dest_id, int dep_time, int arr_time, int elapsed_time) {
		Airport origin = getAirport(origin_id), dest = getAirport(dest_id);
		Airline al = new Airline(carrier, fl_num, origin, dest, dep_time, arr_time, elapsed_time);
		String alstr = al.toStringWithoutArrTime();
		if(!airlines.containsKey(alstr)) {
			airlines.put(alstr, al);
			origin.addAirline(al);
		}
		return;
	}
	
	public void initDone() {
		for(Airport a : airports.values()) {
			a.initDone();
		}
	}
	
	public static void main(String[] args) {
    	AirlineDataSet dataset = new AirlineDataSet("data/T_ONTIME.csv");
		AirlineGraph graph = new AirlineGraph(dataset);
		Airport x = graph.getAirport(11298);
		for(Airline al : x.airlines) {
			System.out.println(al);
		}
	}
	
}

