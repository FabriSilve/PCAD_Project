package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;

import org.jgrapht.graph.DefaultDirectedGraph;
//import org.jgrapht.graph.ListenableDirectedGraph;

import client.ClientInterface;
import data.Linkage;
import data.Room;
import user.UserInterface;

import java.io.Serializable;

public interface ServerInterface extends Remote,Serializable {
	public boolean isPresent(UserInterface user) throws RemoteException;
	public clientState logIn(ClientInterface client) throws RemoteException;
	public clientState signIn(UserInterface user) throws RemoteException;
	public void logoutClient(ClientInterface client, String roomName) throws RemoteException;
	
	public void increaseVisitors(String room) throws RemoteException;
	public void decreaseVisitors(String room) throws RemoteException;
	public DefaultDirectedGraph<Room, Linkage> downloadGraph() throws RemoteException;
	
	public Room lookupRoom(String room) throws RemoteException;
	public List<Room> getRooms() throws RemoteException;
	public List<Linkage> getLinkages() throws RemoteException;
	public void voteRoom(String roomName, int vote) throws RemoteException;
	public void registerTime(String Room, int visitTime) throws RemoteException;
	
	public void updateRoomPanel() throws RemoteException;
	public void updateUsersPanel() throws RemoteException;
}
