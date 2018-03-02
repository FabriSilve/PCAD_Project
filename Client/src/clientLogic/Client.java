package clientLogic;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.graph.ListenableDirectedGraph;

import client.ClientInterface;
import clientGUI.ClientFrame;
import data.Linkage;
import data.Room;
import server.ServerInterface;
import user.User;
import user.UserInterface;

public class Client implements ClientInterface{

	private static final long serialVersionUID = 1L;
	
	//STRUTTURE DATI
	private ListenableDirectedGraph<Room, Linkage> graph;
	private ArrayList<Room> roomVisited = new ArrayList<Room>();
	private DirectedNeighborIndex<Room, Linkage> neighborIndex;

	//VARIABILI
	private ClientFrame frame;
	private ServerInterface server;
	private Room room;			
	public User user;			
	private int timeIn;			
	
	/*
	 * COSTRUTTORE CHE SCARICA IL GRAFO DAL SERVER
	 * INIZIALIZZA IL TEMPO DEL CLIENT NELLA PRIMA SALA
	 * ESPORTA L'OGGETTO CLIENT PER RENDERLO VISIBILE AL SERVER
	 */
	public Client(User user, ServerInterface server) throws RemoteException{
		this.user = user;
		this.server = server;
		downloadGraph(server);
		timeIn = getMinuteTime();
		room = server.lookupRoom(Room.getEntry());
		roomVisited.add(room);
		UnicastRemoteObject.exportObject(this, 0);	
	}
	
	/*
	 * SCARICA DAL SERVER TUTTE LE SALE E I COLLEGAMENTI AGGIUNGENDOLI
	 * IN UN GRAFO LOCALE
	 */
	public boolean downloadGraph(ServerInterface server) throws RemoteException {
		 graph = new ListenableDirectedGraph<Room,Linkage>(server.downloadGraph());
			try {
				for(Room room: server.getRooms())
					graph.addVertex(room);
			} catch (RemoteException e){
				System.err.println("Impossibile contattare il server per aggiungere stanze: " + e.getMessage());
			}
			try {
				for(Linkage linkage: server.getLinkages())
					graph.addEdge(linkage.getStart(), linkage.getEnd());
			} catch (RemoteException e) {
				System.err.println("Impossibile contattare il server per aggiungere collegamento: " + e.getMessage());
			}
			neighborIndex = new DirectedNeighborIndex<Room,Linkage>(graph);
		return true;
	}

	/*
	 * AGGIORNAMENTO DEL GRAFO CHIAMATA DAL SERVER
	 */
	@Override
	public void updateGraph() throws RemoteException {
		downloadGraph(server);
		
	}
	
	/*
	 * INIZIALIZZA IL TEMPO DI INGRESSO DI UN CLIENT IN UNA SALA
	 */
	public void resetTime() {
		timeIn = getMinuteTime();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Client))
			return false;
		Client aux = (Client) obj;
		return user.equals(aux.user);
	}
	
	@Override
	public String toString() {
		return "Client " + user;
	}
	
	//SETTER AND GETTER
	public void setRoom(Room room){
		this.room = room;
	}
	
	public ArrayList<Room> getRoomVisited() {
		return roomVisited;
	}
	
	public void setClientFrame(ClientFrame frame) {
		this.frame = frame;
	}
	
	public DirectedNeighborIndex<Room, Linkage> getNeighborIndex() {
		return neighborIndex;
	}

	public ServerInterface getServer() {
		return server;
	}
	
	public ClientFrame getClientFrame() {
		return frame;
	}
	
	@Override
	public UserInterface getUser() throws RemoteException {
		return user;
	}

	@Override
	public Room getRoom() throws RemoteException {
		return room;
	}

	@Override
	public int getTimeStay() throws RemoteException {
		return getMinuteTime()-timeIn;
	}
	
	@Override
	public String getTimeInToString() throws RemoteException{
		String temp = (timeIn/60)+":"+timeIn%60;
		return temp;
	}
	
	@Override
	public void setNotification(String text) throws RemoteException{
		frame.setNotification(text);
	}
	
	/*
	 * RECUPERA L'ORA IN MINUTI DEL MOMENTO IN CUI è CHIAMATA
	 */
	private int getMinuteTime(){
		GregorianCalendar date = new GregorianCalendar();
		int minuti = date.get(Calendar.MINUTE);
		int ore = date.get(Calendar.HOUR_OF_DAY)*60;
		return ore+minuti;	
	}
}

