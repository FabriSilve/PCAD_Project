package client;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
/*import java.util.ArrayList;

import org.jgrapht.alg.DirectedNeighborIndex;*/

import data.Room;
import server.ServerInterface;
import user.UserInterface;


public interface ClientInterface extends Remote, Serializable {
	
	public void updateGraph() throws RemoteException;
	public boolean downloadGraph(ServerInterface server) throws RemoteException;
	
	//GETTER AND SETTER
	public String getTimeInToString() throws RemoteException;
	public void setNotification(String text) throws RemoteException;
	public UserInterface getUser() throws RemoteException;
	public Room getRoom() throws RemoteException;
	public int getTimeStay() throws RemoteException;
}
