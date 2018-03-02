package worker;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import data.Room;
import serverLogic.Server;


public class AddLinkageWorker extends SwingWorker<Boolean,Void>{
	//VARIABILI
	private Room roomStart;
	private Room roomEnd;
	private Server server;

	/*
	 * COSTRUTTORE CHE RICEVE I DATI SUI QUALI LAVORARE
	 */
	public AddLinkageWorker(Room roomStart, Room roomEnd, Server server) {
		this.roomStart = roomStart;
		this.roomEnd = roomEnd;
		this.server = server;
	}

	/*
	 * RICHIAMA LA FUNZIONE DEL SERVER CHE RIMUOVE IL COLLEGAMENTP
	 */
	@Override
	protected Boolean doInBackground() {
		if (server.addLinkage(roomStart,roomEnd)) {
			server.updateAllClients();
			return true;
		}
		return false;
	}
	
	/*
	 * SE LA RIMOZIONE è STATA ESEGUITA CON SUCCESSO MI AGGIORNA IL GRAFO
	 * ALTRIMENTI MI SEGNALA L'ESITO NEGATIVO
	 */
	@Override
	protected void done() {
		try {
			if (get()) {
				server.getFrame().setInfoText("<font color=\"green\">Linkage add!</font>");
				server.getFrame().getGraph().updateUI();
			}
			else 
				server.getFrame().setInfoText("<font color=\"red\">Impossible add linkage</font>");
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}		
	}
}

