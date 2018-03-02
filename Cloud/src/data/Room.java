package data;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class Room implements Serializable, Remote{
	private static final long serialVersionUID = 2L;
	
	//COSTANTI
	private static final String entry = "Ingresso";
	private static final String exit = "Uscita";
	
	//STRUTTURE DATI DELLA SALE
	private static CopyOnWriteArrayList<Room> roomsList = new CopyOnWriteArrayList<Room>();	//TUTTE LE SALE
	private CopyOnWriteArrayList<Integer> votesList = new CopyOnWriteArrayList<Integer>();	//VOTI DI UNA SALA
	private CopyOnWriteArrayList<Integer> timesList = new CopyOnWriteArrayList<Integer>();	//TEMPI DI UNA SALA
	
	//VARIABILI
	private String name;
	private String description;
	private AtomicInteger currentClients =  new AtomicInteger(0);
	
	/*
	 * COSTRUTTORE DI UNA NUOVA SALA E L'AGGIUNGE ALL'ELENCO DI TUTTE LE SALE
	 */
	public Room(String name, String description, int vote, int time) {
		this.name = name;
		this.description = description;
		this.votesList.add(vote);
		this.timesList.add(time);
		synchronized (roomsList) {
			roomsList.add(this);
		}
	}
	
	/*
	 * CONTROLLA SE UNA SALA INDICATA DA UNA STRINGA è PRESENTE
	 * NELL'ELENCO DELLE SALE
	 */
	public static Room lookup(String s) {
		for (Room room : roomsList)
			if (room.name.equals(s))
				return room;
		return null;
	}
	
	/*
	 * RIMUOVE UNA SALA INDICATA DALLA STRINGA DALL'ELENCO DI TUTTE LE SALE
	 */
	public static boolean removeFromRoomList(String s) {
		synchronized (roomsList) {
			Room room = lookup(s);
			if(!roomsList.remove(room))
				return false;
		}
		return true;
	}
	
	/*
	 * INCREMENTA IL NUMERO DI VISITATORI IN UNA SALA
	 */
	public static void increaseClients(String roomName) {
		Room room = lookup(roomName);
		room.currentClients.incrementAndGet();
	}
	
	/*
	 * DECREMENTA IL NUMERO DEI VISITATORI IN UNA SALA
	 */
	public static void decreaseClients(String s) {
		Room room = lookup(s);
		room.currentClients.decrementAndGet();
	}
	
	/*
	 * AGGIUNGE UN VOTO ALL'ELENCO DEI VOTO DELLA SALA
	 */
	public int voteRoom(int i) {
		votesList.add(i);
		return mediumValue(votesList);
	}
	
	/*
	 * AGGIUNGE IL TEMPO ALL'ELENCO DEI TEMPI DELLA SALA
	 */
	public void registerTime(int time) {
		timesList.add(time);
	}
	
	/*
	 * RESTITUISCE IL VALORE MEDIO DEI VOTI
	 */
	public int mediumVote(){
		return mediumValue(getVoteList());
	}
	
	/*
	 * RESTITUISCE IL VALORE MEDIO DEI TEMPI
	 */
	public int mediumTime(){
		return mediumValue(getTimeList());
	}
	
	/*
	 * ALGORITMO PER CALCOLARE IL VALORE MEDIO 
	 * DI UNA LISTA DA INTERI
	 */
	public static int mediumValue(List<Integer> list) {
		int sum = 0;
		if (list.isEmpty())
			return 0;
		for (int i : list)
			sum += i;
		return sum/list.size();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Room))
			return false;
		Room temp = (Room) obj;
		return temp.name.equals(name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

	//GETTER
	public static String getEntry() {
		return entry;
	}
	
	public static String getExit() {
		return exit;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription(){
		return description;
	}
	
	public static List<Room> getRoomList(){
			return roomsList;
	}
	
	public List<Integer> getTimeList(){
			return timesList;
	}
	
	public List<Integer> getVoteList(){
			return votesList;
	}
	
	public int getCurrentClients() {
		return currentClients.get();
	}
}

