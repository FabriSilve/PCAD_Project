package clientLogic;

import java.lang.reflect.InvocationTargetException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.AccessControlException;
import javax.swing.SwingUtilities;
import clientGUI.ClientFrameLogin;
import server.ServerInterface;

public class ClientMain{
	static ServerInterface server = null;
	//COSTANTI
	private static int port = 8000;
	private static String serverAddress = "127.0.0.1";
	
	
	public static void main(String[] args) throws RemoteException, NotBoundException, InvocationTargetException, InterruptedException {
		/*
		 * DEFINISCO PROPRIETà DELLE CONNESSIONI DEL CLIENT
		 */
		setPropriety();
		/*
		 * MI COLLEGO AL REGISTRY DOVE VI è IL SERVER REMOTO
		 */
		Registry registry = LocateRegistry.getRegistry(serverAddress, port); 
		try{
			server = (ServerInterface) registry.lookup("SERVER");
		}
		catch(AccessControlException e){
			System.out.println("errore importazione server: "+e.getMessage());
		}
		/*
		 * CREO UN NUOVO TASK CHE GESTISCE IL CLIENTFRAMELOGIN E 
		 * POI LO ESEGUO CON SWINGUTILITIES
		 */
		Runnable task = new Runnable() {
			@Override
			public void run() {
				new ClientFrameLogin(server);
			}
		};
		SwingUtilities.invokeLater(task);
	}	
	
	/*
	 * DEFINISCO LE PROPRIETà DELLE CONNESSIONE
	 */
	private static void setPropriety() {
		System.setProperty("java.security.policy","file:./security.policy");
		System.setProperty("java.rmi.server.codebase","file:${workspace_loc}/Client/");
		if (System.getSecurityManager() == null) 
			System.setSecurityManager(new SecurityManager());
	}
}

