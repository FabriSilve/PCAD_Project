package serverLogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.graph.ListenableDirectedGraph;

import client.ClientInterface;
import data.Linkage;
import data.Room;
import server.ServerInterface;
import server.clientState;
import user.User;
import user.UserInterface;

class AndroidClient implements Runnable, Remote, ClientInterface {
		private static final long serialVersionUID = 1L;
		
		//RIFERIMENTI
		private Socket socket;
		private ServerInterface server;
		private ClientInterface clientStub;
		private User user;
		
		//VARIABILI
		private Room currentRoom;
		private String text, username, password, room;
		private String oldRoom = "";
		private int sinceTime;
		boolean logged = false;
		boolean justVisit = true;
		
		//STRUTTURE DATI
		private ArrayList<Room> roomVisited = new ArrayList<Room>();
		private ListenableDirectedGraph<Room, Linkage> graph;
		private DirectedNeighborIndex<Room, Linkage> neighborIndex;
		
		/*
		 * COSTRUTORE CHE DEFINISCE I DATI DELLA CONNESSIONE
		 */
		public AndroidClient(String IP_ADDR, int PORT_REGISTRY, Socket socket) throws RemoteException, NotBoundException {
			this.socket = socket;
			Registry registry = LocateRegistry.getRegistry(IP_ADDR, PORT_REGISTRY);
			server = (ServerInterface) registry.lookup("SERVER");
			clientStub = (ClientInterface) UnicastRemoteObject.exportObject(this, 0);
			System.out.println("Connessione Eseguita");
		}
		
		public void run(){
			try {
				/*
				 * SCARICA IL GRAFO DAL SERVER E
				 * INIZIALIZZO I SOCKET
				 */
				downloadGraph(server);
				BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter outputStream = new PrintWriter(socket.getOutputStream(), true);
				
				/*
				 * RESTA IN ASCOLTO DEGLI INPUT CHE ARRIVANO DAL SOCKET
				 */
				while (true) {
					text = inputStream.readLine();
					/*
					 * SE L'INPUT è NULLO VI è STATA UNA CHIUSURA DELL'APPLICAZIONE SUL
					 * CLIENT E SE SI ERA LOGGATO IN PRECEDENZA EFFETTUO IL LOGOUT
					 * PER NON LASCIARE UTENTI FANTASMA NEL SISTEMA
					 */
					if(text == null){
						if(logged)
							System.out.println("Errore client, ricevuto: null\n Effettuo logout Client");
							server.registerTime(currentRoom.getName(),getTimeStay());
							server.logoutClient(clientStub, currentRoom.getName());
							server.updateUsersPanel();
							logged = false;
						return;
					}
					/*
					 * GESTISCO I VARI INPUT RICEVUTI
					 */
					System.out.println("Ricevuto: " + text);
					switch (text) {
						/*
						 * RICHIESTA DI REGISTRAIONE
						 */
						case "signin":
							username = inputStream.readLine();
							password = inputStream.readLine();
							/*
							 * CONTROLLO CHE I DATI NON SIANO NULLI O VUOTI
							 */
							if ((username == null || username == "") || (password == "" || password == null)) {
								outputStream.println("CampoVuoto");
								outputStream.close();
								inputStream.close();
								socket.close();
								System.out.println("errore registrazione, rispondo: CampoVuoto");
								return;
							}
							/*
							 * CREO L'USER E CHIAMO LA SIGNIN() DEL SERVER E SE HA SUCCESSO HO REGISTRATO L'UTENTE 
							 * AL SISTEMA, ALTRIMENTI RESTITUISCO ERRORE
							 */
							user = new User(username, password);
							if (server.signIn(user).equals(clientState.SUCCESS)) {
								outputStream.println("Ok");
								server.updateUsersPanel();
								System.out.println("registrazione avvenuta, rispondo: Ok");
							}
							else {
								outputStream.println("Errore");
								System.out.println("Errore sconosciuto, rispondo: Errore");
							}
							break;
						
						/*
						 * RICHIESTA DI LOGIN
						 */
						case "login":
							username = inputStream.readLine();
							password = inputStream.readLine();
							currentRoom = server.lookupRoom(Room.getEntry());
							sinceTime = getTimeInMinute();
							/*
							 * CONTROLLO CHE I DATI NON SIANO NULLI O VUOTI
							 */
							if ((username == null || username == "") || (password == "" || password == null)) {
								outputStream.println("Error");
								outputStream.close();
								inputStream.close();
								socket.close();
								System.out.println("Errore credenziali, rispondo: Error");
								return;
							}
							/*
							 * CREO L'USER E CHIAMO LA LOGIN() DEL SERVER E SE HA SUCCESSO HO REGISTRATO L'UTENTE 
							 * AL SISTEMA, ALTRIMENTI RESTITUISCO ERRORE
							 */
							user = new User(username, password);
							try {
								if (server.logIn(clientStub).equals(clientState.SUCCESS)) {
									outputStream.println("Ok");
									System.out.println("utente loggato, rispondo: Ok");
									logged = true;
								}
								else {
									outputStream.println("Error");
									System.out.println("Errore login, rispondo: Error");
								}
								break;
							}
							catch( IOException e){
								System.out.println("Errore login: "+e.getMessage());
								outputStream.println("Error");
							}
						/*
						 * RICHIESTA DI VISITA
						 */
						case "visit":
							room = inputStream.readLine();
							/*
							 *CONVERTO FORMATO ROOM IN MODO CHE LA PRIMA LETTERA SIA MAIUSCOLA
							*/
							room = room.substring(0,1).toUpperCase() + room.substring(1,room.length()).toLowerCase();
							/*
							 * GESTISCO L'ACQUISIZIONE DELLA NUOVA SALA E I DATI CHE DEVO COMUNICARE
							 */
							if (!room.equals(Room.getEntry()))
									oldRoom = currentRoom.getName();
							currentRoom = server.lookupRoom(room);
							/*
							 * AGGIUNGO LA SALA A QUELLE VISITATE DAL CLIENT E RICHIAMO IL SERVER CHE AGGIORNA
							 * LA SUA INTERFACCIA E REGISTRO IL TEMPO TRASCORSO NELLA SALA PRECEDENTE
							 */
							if (room.equals(Room.getEntry())) {
									roomVisited.add(currentRoom);
									server.increaseVisitors(currentRoom.getName());
									sinceTime = getTimeInMinute();
									oldRoom = currentRoom.getName();
							}else {
									if (!roomVisited.contains(currentRoom)) {
											roomVisited.add(currentRoom);
												justVisit = false;
											server.registerTime(oldRoom, getTimeInMinute()-sinceTime);	
										}
									server.increaseVisitors(currentRoom.getName());
									server.decreaseVisitors(oldRoom);
									sinceTime = getTimeInMinute();
							}
							/*
							 * INVIO AL CLIENT ANDROID I NOMI DELLE SALE E SE è GIA STATA VISITATA O MENO
							 * QUESTO PERMETTE AL CLIENT DI VOTARE UNA SALA AL MASSIMO UNA VOLTA
							 */
							for (Room room : neighborIndex.successorListOf(currentRoom)){
								outputStream.println(room.getName().toLowerCase());
								if(roomVisited.contains(room))
									outputStream.println("true");
								else
									outputStream.println("false");
							}
							outputStream.println("EndListLinkages");
							/*
							 * AGGIORNO IL FRAME DEL SERVER
							 */
							server.updateRoomPanel();
							break;
						/*
						 * RICHIESTA DI VOTO
						 */
						case "vote":
							int vote = Float.valueOf(inputStream.readLine()).intValue();
							/*
							 * VOTO LA SALA DEL SERVER CON IL VOTO RICEVUTO
							 */
							server.voteRoom(currentRoom.getName(), vote);
							server.updateRoomPanel();
							break;
						/*
						 * RICHIESTA EXIT
						 */
						case "exit":
							oldRoom = currentRoom.getName();
							//room = currentRoom.getName();
							currentRoom = server.lookupRoom(Room.getExit());
							roomVisited.add(currentRoom);
							server.increaseVisitors(currentRoom.getName());
							server.decreaseVisitors(oldRoom);
							/*
							 * ACQUISISCO I DATI DELLA SALA FINALE E INVIO I L'ELENCO DELLE SALE VISITATE
							 */
							for (Room room : roomVisited) {
								System.out.println("invio:"+room.getName());
								outputStream.println(
										room.getName().substring(0,1).toUpperCase() 
										+ room.getName().substring(1,room.getName().length()).toLowerCase());
								outputStream.println("true");
							}
							outputStream.println("EndListPassages");
							server.updateRoomPanel();
							break;
						/*
						 * RICHIESTA LOGOUTCLIENT
						 */
						case "logoutClient":
							/*
							 * LA ESEGUO SOLO SE IL CLIENT RISULTA REGISTRATO
							 */
							if(logged) {
								/*
								 * REGISTRO IL TEMPO DEL CLIENT SUL SERVER E AGGIORNO L'ELENCO DEGLI USER
								 */
								room = inputStream.readLine();
								room = room.substring(0,1).toUpperCase() 
										+ room.substring(1,room.length()).toLowerCase();
								server.registerTime(room,getTimeStay());
								server.logoutClient(clientStub, room);
								server.updateUsersPanel();
								logged = false;
							}		
							return;
						/*
						 * CASO DI DEFAULT
						 */
						default:
							System.out.println("Caso default");
							break;
					}
				}
			} catch (IOException e) {
				System.out.println("IOException: " + e.getMessage());
				return;
			}
		}
		
		/*
		 * UPDATE DEL GRAFO CHIAMATA DAL SERVER QUANDO AGGIORNA IL GRAFO
		 */
		@Override
		public void updateGraph() throws RemoteException {
			downloadGraph(server);
			try {
				PrintWriter outputStream = new PrintWriter(socket.getOutputStream(), true);
				outputStream.println("update");
			} catch (IOException e) {
				System.out.println("Errore update");
				e.printStackTrace();
			}
		}

		/*
		 * SCARICO IL GRAFO DELL'ACQUARIO DAL SERVER
		 */
		@Override
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
				System.err.println("Impossibile contattare il server per aggiungere linkage: " + e.getMessage());
			}
			neighborIndex = new DirectedNeighborIndex<Room,Linkage>(graph);
		return true;
		}

		//SETTER AND GETTER
		@Override
		public int getTimeStay() throws RemoteException {
			return getTimeInMinute()-sinceTime;
		}

		@Override
		public String getTimeInToString() throws RemoteException {
			return new String(sinceTime/60+":"+sinceTime%60);
		}

		/*
		 * UTILIZZATA DAL CLIENT JAVA
		 */
		@Override
		public void setNotification(String text) throws RemoteException{
			return;
		}

		@Override
		public UserInterface getUser() throws RemoteException {
			return user;
		}

		@Override
		public Room getRoom() throws RemoteException {
			return currentRoom;
		}
		
		/*
		 * RESTITUISCE L'ORA ATTUALE IN MINUTI
		 */
		public int getTimeInMinute(){
			GregorianCalendar date = new GregorianCalendar();
			return (date.get(Calendar.HOUR_OF_DAY)*60)+date.get(Calendar.MINUTE);
	}
}