package worker;

import javax.swing.SwingWorker;

import clientLogic.Client;

public class LogoutWorker extends SwingWorker<Void,Void> {
	//VARIABILE
	Client client;
	
	public LogoutWorker(Client client) {
		this.client = client;
	}

	/*
	 * REGISTRA IL TEMPO DEL CLIENT NELLA SALA IN CUI ERA
	 * EFFETTUA IL LOGOUT DEL CLIENT DAL SISTEMA
	 * AGGIORNA LA LISTA DEGLI UTENTI SUL SERVER 
	 */
	@Override
	protected Void doInBackground() throws Exception {
		client.getServer().registerTime(client.getRoom().getName(), client.getTimeStay());
		client.getServer().logoutClient(client, client.getRoom().getName());
		client.getServer().updateUsersPanel();
		return null;
	}
	
	/*
	 * RILASCIA LE RISORSE DEL FRAME E CHIUDE ANCHE LA FINESTRA DEL LOGIN
	 */
	@Override
	protected void done() {
		client.getClientFrame().dispose();
		client.getClientFrame().getClientFrameLogin().shutDown();
	}
}