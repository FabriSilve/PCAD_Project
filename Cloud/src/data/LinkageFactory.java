package data;

import java.io.Serializable;
import java.rmi.Remote;
import org.jgrapht.EdgeFactory;

public class LinkageFactory implements EdgeFactory<Room, Linkage>, Serializable, Remote {

	private static final long serialVersionUID = 3388100752916892080L;
	
	@Override
	public Linkage createEdge(Room roomStart, Room roomEnd) {
		return new Linkage(roomStart,roomEnd);
	}
}
