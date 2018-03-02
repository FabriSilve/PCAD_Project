package serverLogic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.Semaphore;
import javax.swing.SwingUtilities;

import server.ServerInterface;
import serverGUI.ServerFrame;


public class ServerMain{
	//DATI DEL SERVER
	final static String URL = "jdbc:postgresql://127.0.0.1:5432/postgres?currentSchema=pcad";
	final static String USER = "postgres";
	final static String PASSWORD = "admin";
	private static int registryPort = 8000;
	private static int socketPort = 8050;

	//VARIABILI
	static Server server;
	static ServerAndroid androidServer;
	
	public static void main(String args[]) throws UnknownHostException, RemoteException, InterruptedException {
		/*
		 * DEFINISCO LE PROPRIETà DELLE CONNESSIONE DEL SISTEMA
		 */
		System.setProperty("java.rmi.server.hostname","localhost");
		setPropriety();
		
		String IP_ADDRESS = (InetAddress.getLocalHost()).toString();
		String serverInfo="<html><div style=\"color:#808080;\">&nbsp;&nbsp;Avvio Server: "+todayDate()+"</div>"
				+ "&nbsp;&nbsp;Server addr: " + IP_ADDRESS + "<br/>"
						+ "<table width=\"200px\" height=\"30px\" align=\"center\">"
						+ "<tr><td>Server port:</td><td> " + registryPort+"</td>";
		/*
		 * INIZIALIZZO IL REGISTRY SUL QUALE AGISCE IL SISTEMA
		 */
		Registry registry = null;
		try {
			registry = LocateRegistry.createRegistry(registryPort);
		} catch (RemoteException e) {
			registry = LocateRegistry.getRegistry(registryPort);
		}
		/*
		 * INIZIALIZZO IL SERVER
		 */
		Server server = new Server();
		/*
		 * ESPORTO SUL REGISTRI IL SERVER REMOTO UTILIZZATO DAI CLIENT
		 */
		ServerInterface stubServer = null;
		try{
			stubServer = (ServerInterface) UnicastRemoteObject.exportObject(server, registryPort);
		}
		catch(ExportException e){
			System.out.println("Errore esportazione server: "+e.getMessage());
		}
		registry.rebind("SERVER", stubServer);
	    /*
	     * LANCIO IL SERVER DEDICATO AD ANDROID
	     */
		lunchAndroidServer();
		/*
		 * INIZIALIZZO IL SEMAFORO
		 */
		Semaphore semaphore = new Semaphore(1);
		semaphore.acquire();

		serverInfo = serverInfo+databaseConnection()+"</table><table border=\"1\" width=\"100%\">"
				+ "<caption>Notifiche</caption>";
		server.setInfo(serverInfo);
		/*
		 * AVVIO SU SWINGUTILITIES IL TASK CONTENENTE L'INIZIALIZZAZIONE DI SERVERFRAME
		 */
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					new ServerFrame(server, semaphore);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		};
		SwingUtilities.invokeLater(task);
		
		/*
		 * ATTENDE LA TERMINAZIONE DEL SERVER CHE RILASCERA IL SEMAFORO E IN SEGUITO TOGLIERà 
		 * IL SERVER REMOTO DAL REGISTRY
		 */
		semaphore.acquire();		
		try {
			registry.unbind("SERVER");
		} catch (NotBoundException e) {
			System.out.println("Errore registry");
			e.printStackTrace();
		}
		UnicastRemoteObject.unexportObject(server, true);
		/*
		 * AVVIA LA CHIUSURA DEL SERVER DEDICATO AD ANDROID
		 */
		androidServer.interrupt();
		semaphore.release();
		System.exit(0);
	 }
	
	/*
	 * TESTA LA CONNESSIONE CON IL DATABASE E SALVA IL RISLULTATO IN UNA STRINGA 
	 */
	private static String databaseConnection(){
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return "<td>Database:</td><td><div style=\"color:red;\">Driver Error</div></td></tr>";
		}

		try {
			connection = DriverManager.getConnection(URL,USER, PASSWORD);
			if(connection == null)
				throw new SQLException();
		} catch (SQLException e) {
			e.printStackTrace();
			return "<td>Database:</td><td><div style=\"color:red;\">Connection Error</div></td></tr>";
		}
		
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "<td>Database:</td><td><div style=\"color:green;\">online</div></td></tr>";
	}
	
	/*
	 * IMPOSTA LE PROPRIETà DEL SISTEMA RICHIAMANDO IL FILE SECURITY.POLICY
	 */
	private static void setPropriety() {
		System.setProperty("java.security.policy","file:./security.policy");
		System.setProperty("java.rmi.server.codebase","file:${workspace_loc}/Server/");
		if (System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
	}
	
	/*
	 * RESTITUISCE UNA STRINGA CONTENENTE LA DATA E L'ORA UTILIZZATA PER
	 * SALVARE QUANDO IL SERVER è STATO AVVIATO
	 */
	private static String todayDate(){
		GregorianCalendar date = new GregorianCalendar();
		int anno = date.get(Calendar.YEAR);
		int mese = date.get(Calendar.MONTH) + 1;
		int giorno = date.get(Calendar.DATE);
		int ore = date.get(Calendar.HOUR_OF_DAY);
		int min = date.get(Calendar.MINUTE);
		return new String(giorno+"/"+mese+"/"+anno+" - "+ore+":"+min);
	}
	
	/*
	 * LANCIA IL SERVER DEDICATO AD ANDROID DOPO AVERLO INIZIALIZZATO CON I DATI DELLE PORTE
	 */
	private static void lunchAndroidServer(){
		try {
			androidServer = new ServerAndroid(registryPort,socketPort);
			androidServer.start();
		} catch ( NotBoundException e) {
			System.out.println("Errore avvio server android");
			e.printStackTrace();
		}
	}
}

