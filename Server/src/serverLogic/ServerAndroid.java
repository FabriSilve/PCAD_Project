package serverLogic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;

public class ServerAndroid extends Thread{
	//DATI RELATIVI AL SERVER
	private ServerSocket serverSocket;
	String IP_ADDR = "127.0.0.1";
	int PORT_REGISTRY;
	int PORT_SOCKET;
	int NUM_OF_THREAD = 10;
	
	/*
	 * COSTRUTTORE DEL SERVERANDROID CHE IMPOSTA LE PORTE UTILIZZATE PER
	 * IL REGISTRY E IL SOCKET
	 */
	public ServerAndroid( int registry, int socket) throws NotBoundException {
		PORT_REGISTRY = registry;
		PORT_SOCKET = socket;	
	}
	
	public void run(){
		/*
		 * APRO LA CONNESSIONE SOCKET SULLA PORTA PORT_SOCKET
		 */
		try {
			serverSocket = new ServerSocket(PORT_SOCKET);
		} catch (IOException e) {
			System.out.println("Errore connessione socket");
			e.printStackTrace();
		}
		/*
		 * CREO UN POOL THREAD PER AVERE UN NUMERO LIMITATO DI 
		 * THREAD IN ESECUZIONE
		 */
		ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
		/*
		 * IL SERVER SI METTE IN ATTESA DEI CLIENT
		 */
		System.out.println("Server in attesa...");
		while (true) {
			Socket socket = null;
			/*
			 * RICEVE UNA NUOVA RICHIESTA DA UN CLIENT
			 */
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				System.out.println("Errore accettazione socket");
				e.printStackTrace();
			}
			System.out.println("Chiamata ricevuta da " + socket);
			/*
			 * DEFINISCO IL NUOVO ANDROIDCLIENT CHE GESTIRà LA RICHIESTA
			 */
			AndroidClient androidClient = null;
			try {
				androidClient = new AndroidClient(IP_ADDR, PORT_REGISTRY, socket);
			} catch (RemoteException | NotBoundException e) {
				System.out.println("Errore creazione thread");
				break;
			}
			/*
			 * AVVIO L'ESECUZIONE DELL'ANDROID CLIENT
			 */
			executor.execute(androidClient);
		}
		/*
		 * CHIUDO I THREAD DEL POOL THRED
		 */
		/*try {
			while(!executor.awaitTermination(1000L, TimeUnit.MILLISECONDS)) {}
		} catch (InterruptedException e) {
			System.out.println("Errore chiusura executor");
		}
		System.out.println("Server Android Chiuso");*/
	}
}

