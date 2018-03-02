package serverGUI;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.Semaphore;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.jgraph.JGraph;
import org.jgrapht.ext.JGraphModelAdapter;

import data.Linkage;
import data.Room;
import serverLogic.Server;
import systemGUI.ButtonFont;
import worker.AddLinkageWorker;
import worker.AddRoomWorker;
import worker.LogoutWorker;
import worker.RemoveLinkageWorker;
import worker.RemoveRoomWorker;

public class ServerFrame extends JFrame{
	private static final long serialVersionUID = 1L;
	
	//INFOBOX
	JLabel infoText;
	JScrollPane scrollInfo;
	JScrollBar verticalInfo;
	JPanel infoPanel;
	
	//ROOM
	JTextField roomText;
	JTextArea roomDescription;
	JButton addRoom;
	JButton removeRoom;
	JPanel roomPanel;
	
	//LINKAGE
	JButton addLinkage;
	JButton removeLinkage;
	JLabel between;
	JComboBox<Room> roomStart;
	JComboBox<Room> roomEnd;
	JPanel linkagePanel;
	
	//GRAPH
	JGraph graph;
	JPanel graphPanel;
	
	//USERS
	JButton userButton;
	JLabel userList;
	JScrollPane scrollUser;
	JPanel userPanel;
	
	//ROOM
	JButton roomButton;
	JLabel roomList;
	JScrollPane scrollRoom;
	JPanel roomListPanel;
	
	//TAB ROOM-USERS
	JTabbedPane tabPanel;
	
	//LOGOUT
	JButton logout;
	JPanel logoutPanel;
	
	//VARIABILI UTILIZZATE
	Server server;
	Semaphore semaphore;
	boolean logoutStatus = false;
	
	/*
	 * COSTRUTTORE CHE IMPOSTA L'INTERFACCIA SWING
	 */
	public ServerFrame(Server server, Semaphore semaphore) throws RemoteException {
		super("Server");
		this.server = server;
		this.semaphore = semaphore;
		server.setFrame(this);
		setResizable(false);
		setSize(900, 600);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(null);
		
		/*
		 * DEFINISCO LISTENER PER LA CHIUSURA FINESTRA CHE LANCIA IL LOGOUT SE CONFERMATO
		 */
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
		    	if(logoutStatus == false){
		    		int result = JOptionPane.showConfirmDialog(null,
		    				"<html> "
		    				+ "<center>"
		    				+ "Il Server effettuerà il logout.<br/> Continuare?"
		    				+ "</center>"
		    				+ "</html>", 
		    				"Conferma Logout", 
		    				JOptionPane.YES_NO_OPTION,
		    				JOptionPane.QUESTION_MESSAGE);
		            if (result == JOptionPane.YES_OPTION) {
		            	logout.doClick();
		            	logoutStatus = true;
		            }
		    	}
		    }
		});
				
		//DFINISCO PANNELLO INFO BOX 
		infoPanel = new JPanel();
		infoPanel.setBorder((Border)new LineBorder(new Color(0, 0, 0)));
		infoPanel.setBounds(10, 10, 315, 135);
		infoPanel.setLayout(null);
		getContentPane().add(infoPanel);
		
		infoText = new JLabel(server.getInfo());

		scrollInfo = new JScrollPane(infoText);
		scrollInfo.setBounds(5, 5, 305, 125);
		scrollInfo.createVerticalScrollBar();
				
		infoPanel.add(scrollInfo);
		
		//DEFINISCO PANNELLO SALE 
		roomPanel = new JPanel();
		roomPanel.setBorder((Border)new LineBorder(new Color(0, 0, 0)));
		roomPanel.setBounds(330, 10, 150, 135);
		roomPanel.setLayout(null);
		getContentPane().add(roomPanel);
		
		roomText = new JTextField("Room name");
		roomText.setColumns(25);
		roomText.setBounds(5, 10, 140, 25);
		
		roomDescription = new JTextArea("Descrizione Sala");
		
		JScrollPane scrollDesc = new JScrollPane(roomDescription);
		scrollDesc.createVerticalScrollBar();
		scrollDesc.setBounds(5, 40, 140, 50);
				
		addRoom = new JButton("Aggiungi");
		addRoom.setBounds(10, 95,130,15);
		addRoom.setFont(new ButtonFont());
		
		/*
		 * DEFINISCO LISTENER DEL PULSANTE AGGIUNGI SALE CHE LANCIA ADDROOMWORKER SE SONO RISPETTATE
		 * LE CONDIZIONI
		 */
		addRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String roomName = roomText.getText();
				String roomDesc = roomDescription.getText();
				if (roomName.equals("")) {
					setInfoText("<font color=\"red\">Inserire nome Sala</font> ");
					return;
				}
				if (roomDesc.equals("")) {
					setInfoText("<font color=\"red\">Inserire descrizione Sala</font> ");
					return;
				}
				new AddRoomWorker(roomName, roomDesc, server).execute();		
			} 
		});
		
		removeRoom = new JButton("Rimuovi");
		removeRoom.setFont(new ButtonFont());
		removeRoom.setBounds(10, 115,130,15);
		
		/*
		 * AGGIUNGO LISTENER AL BOTTONE RIMUOVI CHE LANCIA REMOVEROOMWORKER SE LE CONSIZIONI 
		 * SONO RISPETTATE
		 */
		removeRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String roomName = roomText.getText();
				if (roomText.equals("")) {
					setInfoText("<font color=\"red\">Empty input Insert room's name</font>");
					return;
				}
				new RemoveRoomWorker(roomName,server).execute();
			}
		});
		
		roomPanel.add(roomText);
		roomPanel.add(scrollDesc);
		roomPanel.add(addRoom);
		roomPanel.add(removeRoom);
		
		//DEFINIZIONE PANNELLO COLLEGAMENTI
		linkagePanel = new JPanel();
		linkagePanel.setBorder((Border)new LineBorder(new Color(0, 0, 0)));
		linkagePanel.setBounds(485, 10, 210, 135);
		linkagePanel.setLayout(null);
		getContentPane().add(linkagePanel);
		
		addLinkage = new JButton("Aggiungi");
		addLinkage.setFont(new ButtonFont());
		addLinkage.setBounds(10, 10, 90, 25);
		/*
		 * DEFINISCO LISTENER SUL PULSANTE AGGIUNGI CHE LANCIA ADDLINKAGEWORKER SE SONO 
		 * RISPETTATE LE CONDIZIONI
		 */
		addLinkage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Room start = (Room) roomStart.getSelectedItem();
				Room end = (Room) roomEnd.getSelectedItem();
				if (start.equals(end)) {
					setInfoText("<font color=\"red\">Sale uguali! Collegamento non aggiungibile!</font>");
					return;
				}
				new AddLinkageWorker(start,end,server).execute();
			}
		});
		
		removeLinkage = new JButton("Rimuovi");
		removeLinkage.setFont(new ButtonFont());
		removeLinkage.setBounds(110, 10, 90, 25);
		/*
		 * DEFINISCO IL LISTENER SUL PULSANTE RIMUOVI CHE LANCIA IL REMOVELINKAGEWORKER
		 */
		removeLinkage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Room start = (Room) roomStart.getSelectedItem();
				Room end = (Room) roomEnd.getSelectedItem();
				if (start.equals(end)) {
					setInfoText("<font color=\"red\">Stanze uguali Linkage non rimovibile!</font>");
					return;
				}
				new RemoveLinkageWorker(start,end,server).execute();;
			}
		});
		between = new JLabel("collegamento tra:");
		between.setBounds(60, 40, 140, 25);
		
		roomStart = new JComboBox<Room>();
		roomStart.setBounds(10, 70, 190, 25);
		roomStart.setToolTipText("Iniziale");
		
		roomEnd = new JComboBox<Room>();
		roomEnd.setToolTipText("Finale");
		roomEnd.setBounds(10, 100, 190, 25);
		for(Room room: Room.getRoomList()){
			roomStart.addItem(room);
			roomEnd.addItem(room);
		}
		
		linkagePanel.add(addLinkage);
		linkagePanel.add(removeLinkage);
		linkagePanel.add(between);
		linkagePanel.add(new JLabel(""));
		linkagePanel.add(roomStart);
		linkagePanel.add(roomEnd);
		
		//DEFINISCO PANNELLO ELENCO UTENTI
		userPanel = new JPanel();
		userPanel.setBorder((Border)new LineBorder(new Color(0, 0, 0)));
		userPanel.setLayout(new BorderLayout(5,5));
		
		userButton = new JButton("UTENTI");
		userButton.setFont(new ButtonFont());
		/*
		 * DEFINISCO LISTENER SUL PULSANTE UTENTI CHE LANCIA LA FUNZIONE DEL SERVER UPDATEUSERPANEL()
		 */
		userButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	//Uguale a SALE
				try {
					server.updateUsersPanel();
					setInfoText("<font color=\"green\">Utenti aggiornati!</font>");
				} catch (RemoteException exe) {
					setInfoText("<font color=\"red\">Utenti non aggiornati correttamente!</font>");
					exe.printStackTrace();
				}
			}
		});
		
		userList = new JLabel("");
		server.updateUsersPanel();
		
		scrollUser = new JScrollPane(userList);
		scrollUser.createVerticalScrollBar();
		
		userPanel.add(userButton, BorderLayout.NORTH);
		userPanel.add(scrollUser, BorderLayout.CENTER);
		
		//DEFINIZIONE PANNELLO ELENCO SALE
		roomPanel = new JPanel();
		roomPanel.setBorder((Border)new LineBorder(new Color(0, 0, 0)));
		roomPanel.setLayout(new BorderLayout(5,5));
		
		roomButton = new JButton("SALE");
		roomButton.setFont(new ButtonFont());
		/*
		 * DEFINISCO LISTENER SUL PULSANTE SALE CHE LANCIA LA FUNZIONE DEL SERVER UPDATEROOMPANEL() 
		 */
		roomButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					server.updateRoomPanel();
					setInfoText("<font color=\"green\">Sale aggiornate!"
							+ "</font>");
				} catch (RemoteException exe) {
					server.getFrame().setInfoText("<font color=\"red\">Sale non aggionrate correttamente!"
							+ "</font>");
					exe.printStackTrace();
				}
			}
		});
		roomList = new JLabel("");
		server.updateRoomPanel();
		
		scrollRoom = new JScrollPane(roomList);
		scrollRoom.createVerticalScrollBar();
		
		roomPanel.add(roomButton, BorderLayout.NORTH);
		roomPanel.add(scrollRoom, BorderLayout.CENTER);
		
		//DEFINISCO PANNELLO TAB CHE CONTIENE ELENCO USER E SALE
		tabPanel = new JTabbedPane();
		tabPanel.setBorder((Border)new LineBorder(new Color(0, 0, 0)));
		tabPanel.setBounds(700, 10, 185, 475);
		getContentPane().add(tabPanel);
		
		tabPanel.addTab("USER", userPanel);
		tabPanel.addTab("ROOM", roomPanel);
		
		//DEFINISCO PANNELLO LOGOUT
		logoutPanel = new JPanel();
		logoutPanel.setBorder((Border)new LineBorder(new Color(0, 0, 0)));
		logoutPanel.setBounds(700, 490, 185, 70);
		logoutPanel.setLayout(new BorderLayout(20,20));
		getContentPane().add(logoutPanel);
		
		logout = new JButton("LOGOUT");
		logout.setFont(new ButtonFont());
		/*
		 * DEFINISCO LISTENER SUL PULSANTE LOGOUT CHE LANCIA IL LOGOUTWORKER
		 */
		logout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setInfoText("Disconnessione...");
				disableButtons();
				new LogoutWorker(server).execute();
			}
		});
		
		logoutPanel.add(logout, BorderLayout.CENTER);
		
		//DEFINISCO PANNELLO DEL GRAFO
		graphPanel = new JPanel();
		graphPanel.setBorder((Border)new LineBorder(new Color(0, 0, 0)));
		graphPanel.setBounds(10, 150, 685, 410);
		graphPanel.setLayout(new BorderLayout(0, 0));
		getContentPane().add(graphPanel);
		
		graph = new JGraph(new JGraphModelAdapter<Room, Linkage>(server.getGraph()));
		graph.setSizeable(false);
		graph.setDisconnectable(false);
		graph.setAutoResizeGraph(false);
		graph.setBendable(false);
		graph.setEdgeLabelsMovable(false);
		graph.setEditable(false);
		graph.setMoveBeyondGraphBounds(false);
		graphPanel.add(graph);
		
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/*
	 * DISABILITA TUTTI I BOTTONI E I CAMPI DELL'INTERFACCIA
	 */
	protected void disableButtons() {
		addRoom.setEnabled(false);
		roomDescription.setEnabled(false);
		removeRoom.setEnabled(false);
		addLinkage.setEnabled(false);
		removeLinkage.setEnabled(false);
		roomText.setEnabled(false);
		roomStart.setEnabled(false);
		roomEnd.setEnabled(false);
		logout.setEnabled(false);
	}
	
	/*
	 * RESTITUISCE L'ORA DEL MOMENTO IN CUI è CHIAMATA COME STRINGA
	 */
	private static String todayHour(){
		GregorianCalendar date = new GregorianCalendar();
		int ore = date.get(Calendar.HOUR_OF_DAY);
		int min = date.get(Calendar.MINUTE);
		String temp = ore+":"+min;
		return temp;
	}
	
	/*
	 * RIMUOVE UNA SALA DALLA COMBOBOX STARTROOM
	 */
	public void roomStartRemoveItem(String name) {
		roomStart.removeItem(name);
	}
	
	/*
	 * RIMUOVE UNA SALA DALLA COMBOBOX ENDROOM
	 */
	public void roomEndRemoveItem(String name) {
		roomEnd.removeItem(name);
	}
	
	/*
	 * AGGIUNGE UNA SALA ALLA COMBOBOX STARTROOM
	 */
	public void roomStartAddItem(Room room){
		roomStart.addItem(room);
	}

	/*
	 * AGGIUNGE UNA SALA DALLA COMBOBOX ENDROOM
	 */
	public void roomEndAddItem(Room room){
		roomEnd.addItem(room);
	}

	//SETTER AND GETTER
	public JGraph getGraph() {
		return graph;
	}
	
	public String getLog(){
		return infoText.getText();
	}
	
	public Semaphore getSemaphore() {
		return semaphore;
	}

	/*
	 * DEFINISCO LA CASELLA DI TESTO ROOMLIST SE SONO NEL EDT, ALTRIMENTI LANCIO UN TASK SU SWINGUTILITIES
	 * CHE LO IMPOSTI
	 */
	public void setRoomList(String roomsString) {
		if(SwingUtilities.isEventDispatchThread())
			roomList.setText(roomsString);
		else {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					roomList.setText(roomsString);
				}
			};
			SwingUtilities.invokeLater(task);
		}
	}
	
	/*
	 * DEFINISCO LA CASELLA DI TESTO USERLIST SE SONO NEL EDT, ALTRIMENTI LANCIO UN TASK SU SWINGUTILITIES
	 * CHE LO IMPOSTI
	 */
	public void setUsersList(String usersString){
		if(SwingUtilities.isEventDispatchThread())
			userList.setText(usersString);
		else {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					userList.setText(usersString);
				}
			};
			SwingUtilities.invokeLater(task);
		}
	}
	
	/*
	 * DEFINISCO LA CASELLA DI TESTO INFOTEXT SE SONO NEL EDT, ALTRIMENTI LANCIO UN TASK SU SWINGUTILITIES
	 * CHE LO IMPOSTI
	 */
	public void setInfoText(String info){
		if(SwingUtilities.isEventDispatchThread()) {
			String temp = infoText.getText();
			infoText.setText(temp+
					"<tr><td>"+todayHour()+"</td><td>"+info+"</td></tr>");
		}
		else
		{
			Runnable task = new Runnable() {
				@Override
				public void run() {
					String temp = infoText.getText();
					infoText.setText(temp+
							"<tr><td>"+todayHour()+"</td><td>"+info+"</td></tr>");
				}
			};
			SwingUtilities.invokeLater(task);
		}
	}
}
