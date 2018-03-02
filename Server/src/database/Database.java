package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jgrapht.graph.ListenableDirectedGraph;

import data.Linkage;
import data.Room;
import user.User;
import user.UserInterface;

public class Database {
	/*
	 * DATI DEL DATABASE
	 */
	final static String URL = "jdbc:postgresql://127.0.0.1:5432/postgres?currentSchema=pcad";
	final static String USER = "postgres";
	final static String PASSWORD = "admin";
	
	/*
	 * STRUTTURE DATI UTILIZZATE DAL DATABASE
	 */
	private CopyOnWriteArrayList<UserInterface> allUsers;
	private CopyOnWriteArrayList<Room> allRooms;
	
	/*
	 * COSTRUTTORE CHE INIZIALIZZA L'ELENCO DELLE SALE E DEGLI USER
	 */
	public Database() throws SQLException {
		allUsers  =  allUsersInitalize();
		allRooms = allRoomInitalize();
	}
	
	public CopyOnWriteArrayList<UserInterface> getAllUsers() {
		return allUsers;
	}
	
	public CopyOnWriteArrayList<Room> getAllRooms(){
		return allRooms;
	}
	
	/*
	 * INIZIALIZZO L'ELENCO DEGLI USERS CONNETTENDOMI AL DATABASE E LANCIANDO UNA QUERY SULLA
	 * TABELLA CLIENT
	 */
	private CopyOnWriteArrayList<UserInterface> allUsersInitalize() throws SQLException{
		CopyOnWriteArrayList<UserInterface> usersList = new CopyOnWriteArrayList<UserInterface>();
		
		Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT ClientName, Password FROM CLIENT" );
		
		while (resultSet.next()){
			usersList.add(new User(resultSet.getString("ClientName"), resultSet.getString("Password")));
		}
		connection.close();
		return usersList;		
	}
	
	/*
	 * INIZIALIZZO L'ELENCO DELLE SALE CONNETTENDMI AL DATABASE E LANCIANDO UNA QUERY SULLA
	 * TABELLA ROOM
	 */
	private CopyOnWriteArrayList<Room> allRoomInitalize() throws SQLException{
		CopyOnWriteArrayList<Room> roomsList = new CopyOnWriteArrayList<Room>();
		
		Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT RoomName,RoomDescription,RoomVote,RoomTime FROM ROOM" );
		
		while (resultSet.next()){
			roomsList.add(new Room(
					resultSet.getString("RoomName"),
					resultSet.getString("RoomDescription"),
					resultSet.getInt("RoomVote"),
					resultSet.getInt("RoomTime"))
					);
		}
		connection.close();
		return roomsList;
	}
	
	/*
	 * EFFETTUO IL DOWNLOAD DEL GRAFO CREANDO PRIMA LE SALE PRELEVANDOLE DAL DATABASE CHE SARANNO I VERTICI
	 * E IN SEGUITO CREANDO I LINKAGE CHE SARANNO I COLLEGAMENTI 
	 */
	public void downloadGraph(ListenableDirectedGraph<Room, Linkage> graph) throws SQLException{
		Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		Statement statement = connection.createStatement();
		//CREO LE SALE
		ResultSet resultSet = statement.executeQuery("SELECT RoomName, RoomDescription, RoomVote, RoomTime FROM ROOM" );
		while (resultSet.next()) {
			Room room = new Room (
					resultSet.getString("RoomName"),
					resultSet.getString("RoomDescription"),
					resultSet.getInt("RoomVote"),
					resultSet.getInt("RoomTime"));
			graph.addVertex(room);
		}
		//CREO I COLLEGAMENTI TRA LE SALE
		resultSet = statement.executeQuery("SELECT RoomStart, RoomEnd FROM LINKAGE" );
		while (resultSet.next()){
			Room start = Room.lookup(resultSet.getString("RoomStart"));
			Room end = Room.lookup(resultSet.getString("RoomEnd"));
			graph.addEdge(start, end);
		}
		connection.close();
	}
	
	/*
	 * LANCIO UNA QUERY SUL DATABASE CHE AGGIUNGE IL NUOVO USER ALLA TABELLA CLIENT
	 */
	public void addUser (UserInterface user) throws SQLException{
		Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		Statement statement = connection.createStatement();
		statement.execute("INSERT into CLIENT (ClientName, Password) values ('"+ user.getUsername() +"', '"+ user.getPassword() +"') " );
		connection.close();
	}
	
	/*
	 * LANCIO UNA QUERY SUL DATABASE CHE AGGIUNGE LA NUOVA SALA ALLA TABELLA ROOM
	 */
	public void addRoom(Room room) throws SQLException{
		Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		Statement statement = connection.createStatement();
		statement.execute("INSERT into ROOM (roomName,roomDescription) values ('" + room.getName()+"','"+room.getDescription() + "')");
		connection.close();
	}
	
	/*
	 * LANCIO UNA QUERY SUL DATABASE CHE RIMUOVE LA SALA DALLA TABELLA ROOM
	 */
	public void removeRoom(String roomName) throws SQLException {
		Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		Statement statement = connection.createStatement();
		statement.execute("DELETE FROM ROOM WHERE RoomName='" + roomName + "';");
		connection.close();
	}
	
	/*
	 * LANCIO UNA QUERY SUL DATABASE CHE AGGIUNGE IL NUOVO COLLEGAMENTO DALLA TABELLA LINKAGE
	 */
	public void addLinkage(String roomStart, String roomEnd) throws SQLException {
		Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		Statement statement = connection.createStatement();
		statement.execute("INSERT into LINKAGE (roomStart,roomEnd) values ('" + roomStart + "', '" + roomEnd + "')");
		connection.close();
	}
	
	/*
	 * LANCIO UNA QUERY SUL DATABASE CHE RIMUOVE IL COLLEGAMENTO DALLA TABELLA LINKAGE
	 */
	public void removeLinkage(String roomStart, String roomEnd) throws SQLException {
		Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		Statement statement = connection.createStatement();
		statement.execute("DELETE FROM LINKAGE where RoomStart='"+roomStart+"' AND RoomEnd='"+roomEnd+"';");
		connection.close();
	}
	
	/*
	 * LANCIO UNA QUERY CHE AGGIORNA I DATI DI TEMPO E VALUTAZIONE DELLA SALA 
	 */
	public void updateRoomValue(Room room) throws SQLException{
		Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		Statement statement = connection.createStatement();
		statement.execute("UPDATE ROOM SET "
					+ "RoomVote = '"+room.mediumVote()+"',"
					+ "RoomTime = '"+room.mediumTime()+"'"
					+ "WHERE RoomName = '"+room.getName()+"';"
					);
	}	
}
