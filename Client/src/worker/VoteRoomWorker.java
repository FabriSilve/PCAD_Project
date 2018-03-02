package worker;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import clientLogic.Client;
import server.ServerInterface;

public class VoteRoomWorker extends SwingWorker<Boolean,Void> {
	//VARIABILI
	int vote;
	Client client;
	ServerInterface server;
	
	public VoteRoomWorker(int vote, Client client, ServerInterface server) {
		this.vote = vote;
		this.client = client;
		this.server = server;
	}
	
	/*
	 * GESTISCE LA VOTAZIONE DI UNA SALA
	 */
	@Override
	protected Boolean doInBackground(){
		try {
			/*
			 * CHIAMA LA VOTEROOM DEL SERVER E AGGIORNA IL FREM DEL SERVER
			 */
			server.voteRoom(client.getRoom().getName(),vote);
			server.updateRoomPanel();
		} catch (RemoteException e) {
			System.out.println("Server offline");
			return false;
		}
		return true;
	}
	
	/*
	 * NOTIFICA IL RISULTATO DELLA VOTAZIONE DEL CLIENT
	 */
	@Override
	protected void done() {
		try {
			if(get())
				client.getClientFrame().setNotification("Voto registrato!");
			else
				client.getClientFrame().setNotification("Errore votazione");
		} catch (InterruptedException | ExecutionException e) {
			client.getClientFrame().setNotification("Errore votazione");
			e.printStackTrace();
		}
	}

}
