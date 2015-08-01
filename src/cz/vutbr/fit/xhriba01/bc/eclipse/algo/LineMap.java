package cz.vutbr.fit.xhriba01.bc.eclipse.algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.xhriba01.bc.lib.Utils;


// N .. 1 mapping
public class LineMap {

	private Map<Integer, Integer> fFromToMap = new HashMap<>();
	
	private Map<Integer, ArrayList<Integer>> fToFromMap = new HashMap<Integer, ArrayList<Integer>>();
	
	private int fMaxFrom = Utils.INVALID_LINE;
	
	public void add(int from, int to) {
		
		fFromToMap.put(from, to);
		
		ArrayList<Integer> froms = fToFromMap.get(to);
		
		if (froms == null) {
			froms = new ArrayList<Integer>();
			fToFromMap.put(to, froms);
		}
		
		froms.add(from);
		
		if (fMaxFrom == Utils.INVALID_LINE || from > fMaxFrom) fMaxFrom = from;
		
	}
	
	public int getMaxFrom() {
		return fMaxFrom;
	}
	
	public int getTo(int from) {
		
		Integer to = fFromToMap.get(from);
		
		if (to == null) {
			return Utils.INVALID_LINE;
		}
		else {
			return to;
		}
		
	}
	
	public List<Integer> getFrom(int to) {
		
		List<Integer> froms = fToFromMap.get(to);
		
		if (froms == null) {
			froms = Collections.emptyList();
		}
		
		return froms;
		
	}
	
}
