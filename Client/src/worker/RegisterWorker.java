package worker;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import clientGUI.ClientFrameLogin;
import server.ServerInterface;
import server.clientState;
import user.User;

public class RegisterWorker extends SwingWorker<clientState,Void> {
	//VARIABILI
	private User user;
	private ServerInterface server;
	private ClientFrameLogin clientFrameLogin;
	
	public RegisterWorker(String username, String password, ServerInterface server, ClientFrameLogin clientFrameLogin) {
		user = new User(username,password);
		this.server = server;
		this.clientFrameLogin = clientFrameLogin;
	}
	
	/*
	 * REGISTRA UN UTENTE NEL SISTEMA SE RISPETTA LE CONDIZIONI
	 */
	@Override
	public clientState doInBackground() {
		clientState state = null;
		/*
		 * ESEGUO LA SIGNIN() DEL SERVER PER REGISTRARE L'USER
		 */
		try {
			state = server.signIn(user);
		} catch (RemoteException e) {
			clientFrameLogin.setLoginInfo("<html><p style=\"color:red;\">"
					+ "Impossibile contattare il server."
					+ "</p></html>");
			System.err.println("Impossibile contattare il server: " + e.getMessage());
		}
		return state;
	}
	
	/*
	 * ANALIZZO IL RISULTATO DELLA SIGNIN() DEL SERVER E NOTIFICO LA REGISTRAZIONE AVVENUTA
	 * CON SUCCESSO O GLI EVENTUALI ERRORI
	 */
	@Override
	protected void done() {
		try {
			switch(get()) {
			case ALREADY_REGISTERED:
				clientFrameLogin.setLoginInfo("<html><p style=\"color:red;\">"
						+ "Utente già registrato."
						+ "</p></html>");
				break;
			case USERNAME_UNAVAILABLE:
				clientFrameLogin.setLoginInfo("<html><p style=\"color:red;\">"
						+ "Username non disponibile"
						+ "</p></html>");
				break;
			case SERVER_DOWN:
				clientFrameLogin.setLoginInfo("<html><p style=\"color:red;\">"
						+ "Il server è offline."
						+ "</p></html>");
				break;
			case SUCCESS:
				try {
					server.updateUsersPanel();
				} catch (RemoteException e) {
					System.out.println("impossibile aggiornare pannello server: "+e.getMessage());
				}
				clientFrameLogin.getLoginInfo().setText("<html><p style=\"color:green;\">"
						+ "Registrazione completata!"
						+ "</p></html>");
				break;
			default:
				clientFrameLogin.getLoginInfo().setText("<html><p style=\"color:red;\">"
						+ "Errore sconosciuto."
						+ "</p></html>");
				break;
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}
