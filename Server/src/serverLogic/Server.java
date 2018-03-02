package serverLogic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.ListenableDirectedGraph;

import client.ClientInterface;
import data.Linkage;
import data.LinkageFactory;
import data.Room;
import database.Database;
import server.ServerInterface;
import server.clientState;
import serverGUI.ServerFrame;
import user.UserInterface;

public class Server implements ServerInterface {

	private static final long serialVersionUID = 2551007136424988871L;
	
	//STRUTTURE DATI
	private CopyOnWriteArrayList<UserInterface> allUsers;
	private CopyOnWriteArrayList<Room> allRooms;
	private CopyOnWriteArrayList<ClientInterface> loggedClients = new CopyOnWriteArrayList<ClientInterface>();
	private ListenableDirectedGraph<Room, Linkage> graph = new ListenableDirectedGraph<Room, Linkage>(
			new DefaultDirectedGraph<Room, Linkage>(new LinkageFactory()));

	//VARIABILI
	private Database database;
	private ServerFrame serverFrame;
	private AtomicBoolean status;
	private String info;

	/*
	 * COSTRUTTORE DEL SERVER IN CUI
	 * - INIZIALIZZO IL DATABASE
	 * - SCARICO IL GRAFO
	 * - INIZIALIZZO ALLUSERS E ALLROOMS
	*/
	public Server() throws RemoteException {
		try {
			database = new Database();
			database.downloadGraph(graph);
		} catch (SQLException e) {
			System.err.println("Impossibile inizializzare database: " + e.getMessage());
		}
		allUsers = database.getAllUsers();
		allRooms = database.getAllRooms();
		status = new AtomicBoolean(true);
	}

	public void setFrame(ServerFrame serverFrame) {
		this.serverFrame = serverFrame;
	}

	public ListenableDirectedGraph<Room, Linkage> getGraph() {
		return graph;
	}

	/*
	 *CHIAMO LA FUNZIONE DI DATABASE PER AGGIUNGERE UNA NUOVA SALA E L'AGGIUNGO AL GRAFO
	*/
	public Room addRoom(String name, String description) {
		Room room = new Room(name, description, 5, 0);
		synchronized (database) {
			try {
				database.addRoom(room);
			} catch (SQLException e) {
				System.err.println("Impossibile contattare database: " + e.getMessage());
			}
		}
		synchronized (graph) {
			graph.addVertex(room);
		}
		return room;
	}

	/*
	 * CHIAMO LA FUNZIONE DI DATABASE PER RIMUOVERE UNA NUOVA SALA E L'AGGIUNGO AL GRAFO
	 * POSSO ESEGUIRE LA RIMOZIONE SOLO SE NON VI SONO CLIENT NEL SISTEMA
	*/
	public boolean removeRoom(Room room) {
		synchronized (loggedClients) { 
			if (!loggedClients.isEmpty()) 
				return false;
			if (!Room.removeFromRoomList(room.getName()))
				return false;
			synchronized (graph) { 
				synchronized (database) {  
					try {
						for (Linkage linkage : graph.edgesOf(room)) {
							graph.removeEdge(linkage);
							database.removeLinkage(linkage.getStart().getName(), linkage.getEnd().getName());
						}
						database.removeRoom(room.getName());
					} catch (SQLException e) {
						System.err.println("Impossibile contattare database: " + e.getMessage());
					}
					graph.removeVertex(room);
				}
			}
		}
		return true;
	}

	/*
	 * CHIAMO LA FUNZIONE DI DATABASE PER AGGIUNGERE UN NUOVO COLLEGAMENTO
	 */
	public boolean addLinkage(Room start, Room end) {
		synchronized (graph) {
			if (graph.addEdge(start, end) != null) {
				synchronized (database) {
					try {
						database.addLinkage(start.getName(), end.getName());
					} catch (SQLException e) {
						System.err.println("impossibile contattare database: " + e.getMessage());
					}
				}
				return true;
			}
		}
		return false;
	}

	/*
	 * CHIAMO LA FUNZIONE DI DATABASE PER RIMUOVERE UN COLLEGAMENTO
	 */
	public boolean removeLinkage(Room start, Room end) {
		synchronized (graph) {
			if (graph.removeEdge(start, end) != null) {
				synchronized (database) {
					try {
						database.removeLinkage(start.getName(), end.getName());
					} catch (SQLException e) {
						System.err.println("Impossibile contattare database: " + e.getMessage());
					}
				}
				return true;
			}
		}
		return false;
	}

	/*
	 * AGGIORNA IL GRAFO DI TUTTI I CLIENT
	 */
	public void updateAllClients() {
		for (ClientInterface client : loggedClients)
			try {
				client.updateGraph();
			} catch (RemoteException e) {
				System.err.println("Impossibile contattare client " + client.toString() + ":\n" + e.getMessage());
			}
	}

	/*
	 * AGGIORNA IL ROOMPANEL IN CUI SONO CONTENUTE TUTTE LE SALE
	 * ESEGUE LA MODIFICA SE è L'EDT ALTRIMENTI CHIAMA UN NUOVO TASK SULLO 
	 * SWINGUTILITIES
	 */
	@Override
	public void updateRoomPanel() throws RemoteException {
		StringBuilder roomsString = new StringBuilder("<html>Info Sale:<br><br>");
		int currentClients = 0;
		String color = "";
		for (Room room : allRooms) {
			currentClients = room.getCurrentClients();
			if (currentClients > 0)
				color = "<font color=\"green\">";
			else
				color = "<font color=\"red\">";
			roomsString.append(color + room + " : </font>" + " N°Utenti: " + currentClients + "<br/>" + "Gradimento: "
					+ room.mediumVote() + " - " + "Tempo: " + room.mediumTime() + " m<br>");
		}
		roomsString.append("</html>");
		if (SwingUtilities.isEventDispatchThread())
			serverFrame.setRoomList(roomsString.toString());
		else {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					serverFrame.setRoomList(roomsString.toString());
				}
			};
			SwingUtilities.invokeLater(task);
		}
	}

	/*
	 * AGGIORNA L'USERPANEL IN CUI SONO CONTENUTE TUTTE I CLIENT
	 * ESEGUE LA MODIFICA SE è L'EDT ALTRIMENTI CHIAMA UN NUOVO TASK SULLO 
	 * SWINGUTILITIES
	 */
	@Override
	public void updateUsersPanel() throws RemoteException {
		StringBuilder users = new StringBuilder("<html>Info Utenti:<br>");
		for (ClientInterface client : loggedClients) {
			users.append("<font color=\"green\">" + client.getUser().getUsername() + "</font><br/>" + "Sala: "
					+ client.getRoom() + "<br/>" + "Dalle: " + client.getTimeInToString() + "<br>");
		}
		users.append(" -------------------------------------- " + "<br>Utenti Offline:<br><br><font color=\"#808080\">");
		for (UserInterface user : allUsers) {
			if (!isPresent(user))
				users.append("" + user.getUsername() + "<br>");
		}
		users.append("</font></html>");
		if (SwingUtilities.isEventDispatchThread())
			serverFrame.setUsersList(users.toString());
		else {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					serverFrame.setUsersList(users.toString());
				}
			};
			SwingUtilities.invokeLater(task);
		}
	}

	/*
	 * ASSOCIA AD UNA STRINGA LA SALA CON LO STESSO NOME
	 */
	@Override
	public Room lookupRoom(String roomName) throws RemoteException {
		Room room = Room.lookup(roomName);
		return room;
	}
	
	/*public boolean isIn(UserInterface user) {
		synchronized (allUsers) {
			try {
				for (ClientInterface u : loggedClients)
					if (u.getUser().getUsername() == user.getUsername())
						return true;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}*/

	/*
	 * VERIFICA SE UN USER è HA GIà EFFETTUATO IL LOGIN NEL SISTEMA
	 */
	@Override
	public boolean isPresent(UserInterface user) throws RemoteException {
		synchronized (loggedClients) {
			for (ClientInterface client : loggedClients)
				if (user.equals(client.getUser()))
					return true;
			return false;
		}
	}

	/*
	 * EFFETTUA IL LOGIN DI UN USER NEL SISTEMA SE IL SERVER è ATTIVO
	 * SE L'USER è GIà PRESENTE O LE CREDENZIALI SONO SBAGLIATE 
	 * IL LOGIN NON VA A BUON FINE
	 */
	@Override
	public clientState logIn(ClientInterface client) throws RemoteException {
		if (!status.get())
			return clientState.SERVER_DOWN;
		synchronized (allUsers) {
			for (UserInterface user : allUsers) {
				if (client.getUser().getUsername().equals(user.getUsername())
						&& !(client.getUser().getPassword().equals(user.getPassword()))) {
					return clientState.WRONG_PASSWORD;
				}
			}
			if (!allUsers.contains(client.getUser())) {
				return clientState.NOT_REGISTERED;
			}
		}
		synchronized (loggedClients) {
			if (loggedClients.addIfAbsent(client)) {
				updateUsersPanel();
				return clientState.SUCCESS;
			}
			else
				return clientState.ALREADY_LOGGED;
		}
	}

	/*
	 * INCREMENTO IL NUMERO DI VISITATORI IN UNA SALA
	*/
	@Override
	public void increaseVisitors(String roomName) throws RemoteException {
		Room.increaseClients(roomName);
	}

	/*
	 * RENDE DISPONIBILE IL GRAFO DEL SERVER
	 */
	@Override
	public DefaultDirectedGraph<Room, Linkage> downloadGraph() throws RemoteException {
		return new DefaultDirectedGraph<>(new LinkageFactory());
	}

	/*
	 * AGGIUNGE IL TEMPO DEL CLIENT AI TEMPI DELLA SALE RICHIAMANDO LA FUNZIONE
	 * REGISTERTIME() DELLA SALA
	 */
	@Override
	public void registerTime(String roomName, int visitTime) throws RemoteException {
		Room room = Room.lookup(roomName);
		room.registerTime(visitTime);
	}

	/*
	 * ESEGUE IL LOGOUT DEL CLIENT DIMINUENDO IL NUMERO DI CLIENT PRESENTI NELLA SALA IN CUI ERA,
	 * RIMUOVENDOLO DALLA LISTA DI USER LOGGATI ED INFINE AGGIORNA L'ELENCO DELLE SALE
	 */
	@Override
	public void logoutClient(ClientInterface client, String roomName) throws RemoteException {
		decreaseVisitors(roomName);
		synchronized (loggedClients) {
			loggedClients.remove(client);
			loggedClients.notifyAll();
		}
		updateRoomPanel();
	}

	/*
	 * RICHIAMA LA FUNZIONE DECREASECLIENTS() DELLA SALA PER DECREMENTARE IL NUMERO DI UTENTI PRESENTI
	 */
	@Override
	public void decreaseVisitors(String roomName) throws RemoteException {
		Room.decreaseClients(roomName);
	}

	/*
	 * EFFETTUA LA REGISTRAZIONE DELL'UTENTE AL SISTEMA SE IL SERVER è ATTIVO E ATTIVO
	 * SE L'USERNAME NON è GIà STATO UTILIZZATO 
	 */
	@Override
	public clientState signIn(UserInterface user) throws RemoteException {
		if (!status.get())
			return clientState.SERVER_DOWN;

		synchronized (allUsers) {
			for (UserInterface u : allUsers) {
				if (user.getUsername().equals(u.getUsername()) && !user.getPassword().equals(u.getPassword()))
					return clientState.USERNAME_UNAVAILABLE; 
			}
			if (allUsers.addIfAbsent(user)) {
				synchronized (database) { //
					try {
						database.addUser(user);
					} catch (SQLException e) {
						System.err.println("Impossibile aggiungere l'utente al database: " + e.getMessage());
					}
				}
				return clientState.SUCCESS;
			}
		}
		return clientState.ALREADY_REGISTERED;
	}

	/*
	 * CHIAMA LA VOTEROOM DELLA SALA PER REGISTRARE IL VOTO NELLA SUA LISTA
	 */
	@Override
	public void voteRoom(String roomName, int vote) throws RemoteException {
		Room room = Room.lookup(roomName);
		room.voteRoom(vote);
	}

	/*
	 * AGGIORNA IL DATABASE CHIAMANDO LA SUA FUNZIONE UPDATEROOMVALUE() CHE
	 * PER OGNI SALA AGGIORNA IL TEMPO MEDIO E IL VOTO MEDIO
	 */
	public void updateDB() throws SQLException {
		synchronized (allRooms) {
			for (Room room : allRooms)
				database.updateRoomValue(room);
		}
	}

	/*
	 * SCRIVE UN FILE DI LOG CON TUTTE LE NOTIFICHE CHE SONO STATE ATTIVATE SUL SERVER FRAME
	 */
	public void writeLog() {
		String logName = logName();
		try {
			File logFile = new File(logName);
			FileOutputStream log = new FileOutputStream(logFile);
			PrintStream write = new PrintStream(log);
			write.print("<br/><br/>" + serverFrame.getLog());
			write.close();
		} catch (IOException e) {
			System.out.println("Errore: " + e);
			System.exit(1);
		}
	}

	/*
	 * RESTITUISCE UNA STRINGA UTILIZZATA COME NOME DEL FILE DI LOG CHE CONTIENE 
	 * LA DATA E L'ORARIO IN CUI QUESTO VIENE CREATO
	 */
	private static String logName() {
		GregorianCalendar date = new GregorianCalendar();
		int anno = date.get(Calendar.YEAR);
		int mese = date.get(Calendar.MONTH) + 1;
		int giorno = date.get(Calendar.DATE);
		int ore = date.get(Calendar.HOUR_OF_DAY);
		int min = date.get(Calendar.MINUTE);
		String temp = "log/LOG" + anno + "." + mese + "." + giorno + "_" + ore + "." + min + ".html";
		return temp;
	}

	//SETTER AND GETTER
	@Override
	public List<Room> getRooms() throws RemoteException {
		return Room.getRoomList();
	}

	@Override
	public List<Linkage> getLinkages() throws RemoteException {
		return new ArrayList<>(graph.edgeSet());
	}

	public void setStatus(boolean status) {
		this.status.set(status);
	}

	public CopyOnWriteArrayList<ClientInterface> getLoggedClients() {
		return loggedClients;
	}

	public ServerFrame getFrame() {
		return serverFrame;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getInfo() {
		return info;
	}

}