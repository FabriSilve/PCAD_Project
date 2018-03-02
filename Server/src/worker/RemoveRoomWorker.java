package worker;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import data.Room;
import serverLogic.Server;


public class RemoveRoomWorker extends SwingWorker<Boolean,Void> {
	private String roomName;
	private Room room;
	private Server server;

	/*
	 * ACQUISISCE I DATI CON I QUALI LAVORARE
	 */
	public RemoveRoomWorker(String roomName, Server server) {
		this.roomName = roomName;
		this.server = server;
	}
	
	/*
	 * VERIFICA LA PRESENZA DELLA SALA NEL SERVER E IN CASO AFFERMATIVO
	 * LANCIA LA FUNZIONE REMOVEROOM() DEL SERVER
	 */
	@Override
	protected Boolean doInBackground() {
		try {
			room = server.lookupRoom(roomName);
			if ( room == null)
				return false;
		} catch (RemoteException e) {
			System.err.println("Impossible contect server: " + e.getMessage());
		}
		return server.removeRoom(room);
	}
	
	/*
	 * SE LA RIMOZIONE HA AVUTO ESITO POSITOVO RIMUOVO LA SALA DALLE COMBOBOX DEL SERVER,
	 * IN CASO DI ERRORE LO NOTIFICA
	 */
	@Override
	protected void done() {
		try {
			if(get()){
				server.getFrame().roomStartRemoveItem(room.getName());
				server.getFrame().roomEndRemoveItem(room.getName());
				server.getFrame().setInfoText("<font color=\"green\">Room removed!</font>");
			}
			else {
				server.getFrame().setInfoText("<font color=\"red\">Room Not Removed!</font>");
			}
		} catch (InterruptedException | ExecutionException e) {
			server.getFrame().setInfoText("<font color=\"red\">Room Not Removed!</font>");
			e.printStackTrace();
		}
	}

}
