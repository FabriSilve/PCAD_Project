package clientLogic;

import java.lang.reflect.InvocationTargetException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.AccessControlException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import data.Room;
import server.ServerInterface;
import server.clientState;
import user.User;

public class ClientSimulator{
	static ServerInterface server = null;
	//COSTANTI
	private static int port = 8000;
	private static String serverAddress = "127.0.0.1";
	
	
	public static void main(String[] args) throws RemoteException, NotBoundException, InvocationTargetException, InterruptedException {
		/*
		 * DEFINISCO PROPRIETà DELLE CONNESSIONI DEL CLIENT
		 */
		setPropriety();
		/*
		 * MI COLLEGO AL REGISTRY DOVE VI è IL SERVER REMOTO
		 */
		Registry registry = LocateRegistry.getRegistry(serverAddress, port); 
		try{
			server = (ServerInterface) registry.lookup("SERVER");
		}
		catch(AccessControlException e){
			System.out.println("errore importazione server: "+e.getMessage());
		}
		/*
		 * CREO DEI CLIENT CHE SIMULINO DEI PERCORSI
		 */
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for(int i=0; i<10; i++){
			int num = i;
			Runnable task = new Runnable() {
				@Override
				public void run() {
					Client client = null;
					/*
					 * SIGNIN
					 */
					try {
						Thread.sleep((long)(Math.random()*10)*2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					User user = new User("u"+num,"p"+num);
					clientState state = null;
					try {
						state = server.signIn(user);
					} catch (RemoteException e) {
						System.err.println("Impossibile contattare il server: " + e.getMessage());
					}
					try {
						server.updateUsersPanel();
					} catch (RemoteException e) {
						System.out.println("impossibile aggiornare pannello server: "+e.getMessage());
					}
					/*
					 * LOGIN
					 */
					try {
						Thread.sleep((long)(Math.random()*10)*2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try {
						if (server.isPresent(user)) {
							return;
						}
						client = new Client(user,server);
						state = server.logIn(client);
					} catch (RemoteException e) {
						System.err.println("Impossibile contattare il server: " + e.getMessage());
					}
					try {
						server.increaseVisitors(client.getRoom().getName());
						server.updateUsersPanel();
					} catch (RemoteException e) {
						System.err.println("Impossibile contattare il server : " + e.getMessage());
					}
					/*
					 * MOVE
					 */
					String sala;
					for(int j=0; j<8; j++) {
						sala = "";
						switch (j){
						case 0:
							sala = "Delfini";
							break;
						case 1:
							sala = "Squali";
							break;
						case 2:
							sala = "Foche";
							break;
						case 3:
							sala = "Granchi";
							break;
						case 4:
							sala = "Orche";
							break;
						case 5:
							sala = "Pinguini";
							break;
						case 6:
							sala = "Piranha";
							break;
						case 7:
							sala = "Uscita";
							break;
						}
						try {
							Thread.sleep((long)(Math.random()*10)*2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Room clientNextRoom = null;
						try{
							clientNextRoom = server.lookupRoom(sala);
							/*
							 * REGISTRO TEMPO E DECREMENTO VISITATORI SALA
							*/
							server.registerTime(client.getRoom().toString(), client.getTimeStay());
							server.decreaseVisitors(client.getRoom().getName());
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						/*
						 * CAMBIO SALA DEL CLIENT E RESETTO IL TEMPO
						*/
						Boolean justVisit;
						client.setRoom(clientNextRoom);
						if(!client.getRoomVisited().contains(clientNextRoom)){
							client.getRoomVisited().add(clientNextRoom);
							justVisit = false;
						}
						else
							justVisit=true;
						List<Room> nearRooms = client.getNeighborIndex().successorListOf(clientNextRoom);
						client.resetTime();
						/*
						 * AUMENTO I VISITATORI DELLA NUOVA SALA DEL CLIENT
						 */
						try {
							server.increaseVisitors(clientNextRoom.getName());
							server.updateRoomPanel();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						/*
						 * AGGIORNO IL FRAME DEL SERVER
						 */
						try {
							server.updateUsersPanel();
						} catch (RemoteException e) {
							System.out.println("Aggiornamento server non riuscito");
						}
						try {
							/*
							 * CHIAMA LA VOTEROOM DEL SERVER E AGGIORNA IL FREM DEL SERVER
							 */
							server.voteRoom(client.getRoom().getName(),num+1);
							server.updateRoomPanel();
						} catch (RemoteException e) {
							System.out.println("Server offline");
							return;
						}
					
					}
					/*
					 * EFFETTUA LOGOUT
					 */
					try {
						Thread.sleep((long)(Math.random()*10)*2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try {
						client.getServer().registerTime(client.getRoom().getName(), client.getTimeStay());
						client.getServer().logoutClient(client, client.getRoom().getName());
						client.getServer().updateUsersPanel();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					return;
					
				}
			};
			executor.execute(task);
		}
	}	
	
	/*
	 * DEFINISCO LE PROPRIETà DELLE CONNESSIONE
	 */
	private static void setPropriety() {
		System.setProperty("java.security.policy","file:./security.policy");
		System.setProperty("java.rmi.server.codebase","file:${workspace_loc}/Client/");
		if (System.getSecurityManager() == null) 
			System.setSecurityManager(new SecurityManager());
	}
}
	

