package worker;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import data.Room;
import serverLogic.Server;


public class AddRoomWorker extends SwingWorker<Boolean,Void> {
	private String roomName;
	private String roomDescription;
	private Server server;
	private Room room;
	
	/*
	 * INIZIALIZZA IL WORKER CON I DATI CHE UTILIZZERà PER LE OPERAZIONI
	 */
	public AddRoomWorker(String roomName, String roomDescription, Server server) {
		this.roomName = roomName;
		this.roomDescription = roomDescription;
		this.server = server;
	}
	
	/*
	 * VERIFICO CHE LA SALA NON SIA PRESENTE NEL SERVER E IN SEGUITO LA AGGIUNGO AL SERVER
	 */
	@Override
	protected Boolean doInBackground() {
		if (Room.lookup(roomName) != null)
			return false;
		room = server.addRoom(roomName, roomDescription);
		return true;
	}
	
	/*
	 * SE LA SALA ERA GIA PRESENTE NOTIFICO L'ERRORE, ALTRIMENTI 
	 * AGGIORNO LE COMBOBOX DEL SERVER E AGGIORNO SIA L'ELENCO DELLE SALE
	 * CHE IL GRAFO
	 */
	@Override
	protected void done() {
		try {
			if (!get()) {
				server.getFrame().setInfoText("<font color=\"red\">Room already present</font>");
				return;
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
			
		//AGGIORNO SERVER FRAME
		server.getFrame().roomStartAddItem(room);
		server.getFrame().roomEndAddItem(room);
		server.getFrame().setInfoText("<font color=\"green\">Room added!</font>");
		
		try {
			server.updateRoomPanel();
		} catch (RemoteException e){
			e.printStackTrace();
		}
		server.getFrame().getGraph().updateUI();
	}
}
