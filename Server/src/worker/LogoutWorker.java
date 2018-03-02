package worker;

import java.rmi.RemoteException;
import java.sql.SQLException;

import javax.swing.SwingWorker;

import client.ClientInterface;
import serverLogic.Server;

public class LogoutWorker extends SwingWorker<Boolean,Void> {
	Server server;

	public LogoutWorker(Server server) {
		this.server = server;
	}

	/*
	 * PONE A FALSO LO STATUS DEL SERVER E AVVIA IL LOGOUT  
	 */
	@Override
	protected Boolean doInBackground() throws InterruptedException{
		server.setStatus(false);
		synchronized (server.getLoggedClients()) {
			server.getFrame().setInfoText("Disconnessione Clients");
			boolean error = false;
			/*
			 * ATTENDE CHE NON VI SIANO CLIENTI LOGGATI O CHE SI VERIFICHI UN ERRORE
			 * DI COMUNICAZIONE CON UN CLIENT
			 */
			while(!server.getLoggedClients().isEmpty())
				if(!error)
					/*
					 * COMUNICA A TUTTI I CLIENT LOGGATI DI CHIUDERE L'APPLICAZIONE
					 */
					for (ClientInterface client: server.getLoggedClients())
						try {
							client.setNotification("Server down, close app");
						} catch (RemoteException e) {
							System.out.println("client non contattato: "+e.getMessage());
							error = true;
							break;
						}
				else
					break;
			/*
			 * AGGIORNO IL DATABASE CON I DATI DELLE SALE AQUISITI NELLA SESSIONE DI UTILIZZO
			 */
			try{
				server.updateDB();
			}
			catch(SQLException e){
				server.setInfo("<font color=\"red\">Database not updated correctly</font>");
			}
			server.setInfo("<font color=\"green\">Database updated correctly</font>");
		}
		/*
		 * SCRIVO IL FILE DI LOG DELLA SESSIONE
		 */
		server.writeLog();
		return true;
	}
	
	/*
	 * RILASCIA L ERISORSE DEL SERVER FRAME E RILASCI IL SEMAFORO RESTITUENDOLO AL SERVER MAIN
	 */
	@Override
	protected void done() {
		server.getFrame().dispose();
		server.getFrame().getSemaphore().release();
	}

}

