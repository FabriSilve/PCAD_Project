package worker;

import java.rmi.RemoteException;
import java.util.List;

import javax.swing.SwingWorker;

import clientGUI.ClientFrame;
import clientLogic.Client;
import data.Room;
import server.ServerInterface;

public class MoveWorker extends SwingWorker<Boolean,Void>{
	//VARIABILI
	String roomSelected;
	Room clientNextRoom;
	Client client;
	ClientFrame clientFrame;
	ServerInterface server;
	List<Room> nearRooms;
	Boolean justVisit;
	
	public MoveWorker(String roomSelected, Client client, ServerInterface server) {
		this.roomSelected = roomSelected;
		this.client = client;
		clientFrame = client.getClientFrame();
		this.server = server; 
	}
	
	/*
	 * GESTISCE IL CAMBIO DI SALA DEL CLIENTE
	 */
	@Override
	protected Boolean doInBackground() throws Exception {
		clientNextRoom = server.lookupRoom(roomSelected);
		/*
		 * DISABILITA I COMPONENTI GRAFICI CHE GESTISCONO LA SCELTA DELLO SPOSTAMENTO
		 */
		clientFrame.moveComponentEnable(false);
		clientFrame.voteComponentEnable(false);
		/*
		 * REGISTRO TEMPO E DECREMENTO VISITATORI SALA
		*/
		server.registerTime(client.getRoom().toString(), client.getTimeStay());
		server.decreaseVisitors(client.getRoom().getName());
		/*
		 * CAMBIO SALA DEL CLIENT E RESETTO IL TEMPO
		*/
		client.setRoom(clientNextRoom);
		if(!client.getRoomVisited().contains(clientNextRoom)){
			client.getRoomVisited().add(clientNextRoom);
			justVisit = false;
		}
		else
			justVisit=true;
		nearRooms = client.getNeighborIndex().successorListOf(clientNextRoom);
		client.resetTime();
		
		/*
		 * AUMENTO I VISITATORI DELLA NUOVA SALA DEL CLIENT
		 */
		server.increaseVisitors(clientNextRoom.getName());
		server.updateRoomPanel();
		
		return true;
	}
	
	/*
	 * AGGIORNO L'INTERFACCIA DEL CLIENT
	 */
	@Override
	protected void done() {
		/*
		 * AGGIORNO INTESTAZIONE FRAME CLIENT
		 */
		clientFrame.setNotification("Stanza cambiata");
		clientFrame.setTitle(clientNextRoom.getName());
		clientFrame.setRoomText(clientNextRoom.getDescription());
		/*
		 * CONTROLLO SE IL CLIENT è ARRIVATO ALL'USCITA
		 */
		try {
			if (client.getRoom().getName().equals(Room.getExit())) {
				clientFrame.moveComponentEnable(false);
				clientFrame.setNotification("Fine");
				return;
			}
		} catch (RemoteException e) {
			System.out.println("Client non raggiungibile: "+e.getMessage());
		}
		/*
		 * AGGIORNO IL FRAME DEL SERVER
		 */
		try {
			server.updateUsersPanel();
		} catch (RemoteException e) {
			System.out.println("Aggiornamento server non riuscito");
		}
		/*
		 * AGGIORNO FRAME CLIENT
		 */
		clientFrame.nextRoom_removeAllItems();
		for (Room room : nearRooms)
			clientFrame.nextRooms_addItem(room.getName());
		clientFrame.moveComponentEnable(true);
		/*
		 * ABILITO LA VOTAZIONE SE LA  SALA NON è ANCORA STATA VISITATA
		 */
		if(justVisit)
			clientFrame.voteComponentEnable(false);
		else
			clientFrame.voteComponentEnable(true);
	}
}
