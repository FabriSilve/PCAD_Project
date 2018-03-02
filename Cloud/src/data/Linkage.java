package data;

import java.io.Serializable;
import java.rmi.Remote;

public class Linkage implements Serializable, Remote {
	private static final long serialVersionUID = 3L;
	
	//VARIABILI
	private Room start;
	private Room end;
	
	//COSTRUTTORE CON DUE SALE
	public Linkage(Room start, Room end) {
		this.start = start;
		this.end = end;
	}
	
	//COSTRUTTORE CON COLLEGAMENTO
	public Linkage(Linkage link) {
		this.start = link.start;
		this.end = link.end;
	}

	public Room getStart() {
		return start;
	}
	
	public Room getEnd() {
		return end;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Linkage))
			return false;
		Linkage temp = (Linkage) obj;
		return (start.equals(temp.start) && end.equals(temp.end));
	}
	
	@Override
	public String toString() {
		return "";		//start + " - " + end;
	}
}
