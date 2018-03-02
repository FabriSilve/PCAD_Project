package worker;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import data.Room;
import serverLogic.Server;


public class RemoveLinkageWorker extends SwingWorker<Boolean,Void>{
	private Room roomStart;
	private Room roomEnd;
	private Server server;

	/*
	 * ACQUISICE I DATI SUL QUALE SVOLGERà LE OPERAZIONI
	 */
	public RemoveLinkageWorker(Room roomStart, Room roomEnd, Server server) {
		this.roomStart = roomStart;
		this.roomEnd = roomEnd;
		this.server = server;
	}

	/*
	 * RICHIAMA LA FUNZIONE DEL SERVER REMOVELINKAGE E SE HA ESITO POSITIVO NOTIFICA
	 * LA MODIFICA A TUTTI I CLIENT
	 */
	@Override
	protected Boolean doInBackground() {
		if (server.removeLinkage(roomStart,roomEnd)) {
			server.updateAllClients();
			return true;
		}
		return false;
	}
	
	/*
	 * SE LA MODIFICA HA SUCCESSO AGGIORNO IL GRAFO ALTRIMENTI NOTIFICO L'ERRORE
	 */
	@Override
	protected void done() {
		try {
			if (get()) {
				server.getFrame().setInfoText("<font color=\"green\">Linkage removed!</font>");
				server.getFrame().getGraph().updateUI();
			}
			else server.getFrame().setInfoText("<font color=\"red\">Impossible remove linkage</font>");
		} catch (InterruptedException | ExecutionException e) {
			server.getFrame().setInfoText("<font color=\"red\">Impossible remove linkage</font>");
			e.printStackTrace();
		}
		
	}
}