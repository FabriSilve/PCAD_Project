package worker;

import java.rmi.RemoteException;

import javax.swing.SwingWorker;

import clientGUI.ClientFrame;
import clientGUI.ClientFrameLogin;
import clientLogic.Client;
import server.ServerInterface;
import server.clientState;
import user.User;

public class LoginWorker extends SwingWorker<Void,Void> {
	//VARIABILI
	User user;
	ServerInterface server;
	Client client;
	ClientFrameLogin clientFrameLogin;
	clientState stateWorker;

	public LoginWorker(String username, String password, ServerInterface server, ClientFrameLogin clientFrameLogin){
		user = new User(username, password);
		this.server = server;
		this.clientFrameLogin = clientFrameLogin;
	}
	
	/*
	 * CONTROLLA SE L'UTENTE è GIA PRESENTE NEL SERVER.
	 * SE è ASSENTE INIZIALIZZO L'USER COME UN NUOVO CLIENT E LO COLLEGO AL SERVER.
	 */
	@Override
	protected Void doInBackground() {	
		 try {
			if (server.isPresent(user)) {
				stateWorker = clientState.ALREADY_LOGGED;
				return null;
			}
			client = new Client(user,server);
			stateWorker = server.logIn(client);
		} catch (RemoteException e) {
			clientFrameLogin.setLoginInfo("<html><p style=\"color:red;\">"
					+ "Impossibile contattare il server."
					+ "</p></html>");
			System.err.println("Impossibile contattare il server: " + e.getMessage());
		}
		return null;
	}
	
	/*
	 * ANALIZZA IL RISULTATO DELLA FUNZIONE LOGIN() DEL SERVER E IN BASE 
	 * A QIESTO RESTITUISCE UNA NOTIFICA CORRISPONDENTE
	 */
	@Override
	protected void done() {
		while(this.getState() != SwingWorker.StateValue.DONE) {}
		switch(stateWorker) {
			case ALREADY_LOGGED:
				clientFrameLogin.setLoginInfo("<html><p style=\"color:red;\">"
						+ "Utente già loggato."
						+ "</p></html>");
				break;
			case NOT_REGISTERED:
				clientFrameLogin.setLoginInfo("<html><p style=\"color:red;\">"
						+ "Utente non ancora registrato."
						+ "</p></html>");
				break;
			case SERVER_DOWN:
				clientFrameLogin.setLoginInfo("<html><p style=\"color:red;\">"
						+ "Il server è offline."
						+ "</p></html>");
				break;
			case WRONG_PASSWORD:
				clientFrameLogin.setLoginInfo("<html><p style=\"color:red;\">"
						+ "Password errata."
						+ "</p></html>");
				break;
			case SUCCESS:
				clientFrameLogin.setLoginInfo("<html><p style=\"color:green;\">"
						+ "LogIn in corso..."
						+ "</p></html>");
				try {
					clientFrameLogin.dispose();
					server.increaseVisitors(client.getRoom().getName());
					server.updateUsersPanel();
					new ClientFrame(client, server, clientFrameLogin);
				} catch (RemoteException e) {
					System.err.println("Impossibile contattare il server : " + e.getMessage());
				}
				clientFrameLogin.setLoginInfo("<html><p style=\"color:green;\">"
						+ "Login completato!"
						+ "</p></html>");
				break;
			default:
				clientFrameLogin.setLoginInfo("<html><p style=\"color:red;\">"
						+ "Errore sconosciuto."
						+ "</p></html>");
				break;
		}
	}
}
