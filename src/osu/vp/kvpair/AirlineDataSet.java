package osu.vp.kvpair;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.opencsv.CSVReader;

/**
 * @author Meng Meng 
 */

public class AirlineDataSet {
    CSVReader reader;
    List<String> fl_date = new ArrayList<>();
    List<String> unique_carrier = new ArrayList<>();
    List<String> fl_num = new ArrayList<>();
    List<Integer> origin_airport_id = new ArrayList<>();
    List<Integer> dest_airport_id = new ArrayList<>();
    //List<String> crs_dep_time = new ArrayList<>();
    //List<String> crs_arr_time = new ArrayList<>();
    //List<String> crs_elapsed_time = new ArrayList<>();
    List<Integer> crs_dep_time = new ArrayList<>();
    List<Integer> crs_arr_time = new ArrayList<>(); 
    List<Integer> crs_elapsed_time = new ArrayList<>();
    
    public AirlineDataSet(String filename) {
    	try {
			reader = new CSVReader(new FileReader(filename));
			readData();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public int size() {
    	return fl_num.size();
    }
    
    public Object get(int i, String col) {
    	switch(col) {
    	case "FL_DATE":
    		return fl_date.get(i);
    	case "UNIQUE_CARRIER":
    		return unique_carrier.get(i);
    	case "FL_NUM":
    		return fl_num.get(i);
    	case "ORIGIN_AIRPORT_ID":
    		return origin_airport_id.get(i);
    	case "DEST_AIRPORT_ID":
    		return dest_airport_id.get(i);
    	case "CRS_DEP_TIME":
    		return crs_dep_time.get(i);
    	case "CRS_ARR_TIME":
    		return crs_arr_time.get(i);
    	case "CRS_ELAPSED_TIME":
    		return crs_elapsed_time.get(i);
    	}
    	return null;
    }
    private int hhmm2min(String data) {
    	int hour = 0, min = 0;
    	if(data.length() == 4) {
	    	hour = Integer.parseInt(data.substring(0,2));
	    	min = Integer.parseInt(data.substring(2,4));
    	} else if(data.length() == 3) {
    		hour = Integer.parseInt(data.substring(0,1));
	    	min = Integer.parseInt(data.substring(1,3));	
    	}
    	//System.out.println(data + " " + hour + " " + min + " " + (hour * 60 + min));
    	return hour * 60 + min;
    }
    private void addData(String col, String data) {
		//System.out.println(col + " " + data);

    	switch(col) {
    	case "FL_DATE":
    		fl_date.add(data);
    		break;
    	case "UNIQUE_CARRIER":
    		unique_carrier.add(data);
    		break;
    	case "FL_NUM":
    		fl_num.add(data);
    		break;
    	case "ORIGIN_AIRPORT_ID":
    		origin_airport_id.add(Integer.parseInt(data));
    		break;
    	case "DEST_AIRPORT_ID":
    		dest_airport_id.add(Integer.parseInt(data));
    		break;
    	case "CRS_DEP_TIME":
    		crs_dep_time.add(hhmm2min(data));
    		break;
    	case "CRS_ARR_TIME":
    		crs_arr_time.add(hhmm2min(data));
    		break;
    	case "CRS_ELAPSED_TIME":
    		crs_elapsed_time.add((int)Double.parseDouble(data));
    		break;
    	}
    }
    
    private void readData() {
    	try {
			String[] col = reader.readNext();
	    	String[] nextLine;
	    	while ((nextLine = reader.readNext()) != null) {
		    	for(int i = 0; i < nextLine.length; ++i) {
		        	addData(col[i], nextLine[i]);
		        }
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private static void printString(String[] s) {
    	for(int i = 0; i < s.length; ++i) {
    		System.out.print(s[i] + ", ");
    	}
    	System.out.println("");
    }
    private String[] nextLine() {
    	String[] l = null;
		try {
			l = reader.readNext();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return l;
    }

    public static void main(String[] args) {
    	AirlineDataSet dataset = new AirlineDataSet("data/T_ONTIME.csv");
    	/*
    	for(int i = 0; i < dataset.crs_elapsed_time.size(); ++i) {
    		System.out.println(dataset.crs_elapsed_time.get(i));
    	}
    	*/
    }
}
