package edu.mit.genome.gp.ui.hclviewer;
import java.util.*;

public class Annotation {
	List list;
	String id;
	static Iterator EMPTY_ITERATOR = new ArrayList().iterator();

	public Annotation(String id) {
		this.id = id;
	}

	public void add(String key, String annot) {
		if(list == null) {
			list = new ArrayList();
		}
		list.add(new Entry(key, annot));
	}

	public String getId() {
		return id;
	}

	/**
	 * @return    Description of the Return Value
	 */
	public Iterator iterator() {
		if(list == null) {
			return EMPTY_ITERATOR;
		}
		return list.iterator();
	}

	public String toString() {
		return id;	
	}
	/*public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(id);
		sb.append("\t");
		if(list != null) {
			for(int i = 0, size = list.size(); i < size; i++) {
				Entry entry = (Entry) list.get(i);
				sb.append(entry.value);
				sb.append("\t");
			}
		}
		return sb.toString();
	}*/

	static class Entry {
		public String key;
		public String value;

		public Entry(String key, String value) {
			this.key = key;
			this.value = value;
		}

	}

}

