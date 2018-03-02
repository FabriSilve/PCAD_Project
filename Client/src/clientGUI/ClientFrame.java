package clientGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.border.LineBorder;
import clientLogic.Client;
import data.Room;
import server.ServerInterface;
import systemGUI.ButtonFont;
import worker.LogoutWorker;
import worker.MoveWorker;
import worker.VoteRoomWorker;

public class ClientFrame extends JFrame{
	private static final long serialVersionUID = 1L;
	//VARIABILI
	private Client client;
	private ServerInterface server;
	private ClientFrameLogin clientFrameLogin;
	
	//COMPONENTI DELL'INTERFACCIA
	private JPanel logoutPanel;
		private JLabel notification;
		private JButton logoutButton;
	private JPanel bodyPanel;
		
		private JLabel title;
		private JSlider voteSlider;
		private JButton voteButton;
		private JScrollPane roomScrollPanel;
			private JPanel roomDescriptionPanel;
				private JLabel roomText;
				private ImageIcon roomImage;
				private JLabel roomImageLabel;
		private JComboBox<String> nextRoom;
		private JButton moveButton;

	/*
	 * COSTRUTTORE CHE GENERA LA FINESTRA DEL CLIENT
	 */
	public ClientFrame(Client c, ServerInterface s, ClientFrameLogin clientFrameLogin) throws RemoteException {
		super("Client");
		this.client = c;
		this.server = s;
		this.clientFrameLogin = clientFrameLogin;
		client.setClientFrame(this);		
		Room clientRoom = server.lookupRoom(client.getRoom().getName());
		server.updateRoomPanel();
		
		//DEFINISCO IL FRAME
		setResizable(false);
		setSize(220, 300);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(null);
		
		//LISTENER CHIUSURA PAGINA
		this.addWindowListener(new WindowAdapter()
		{
		    public void windowClosing(WindowEvent e)
		    {
		    	 int result = JOptionPane.showConfirmDialog(null, "<html><center>Verrà effettuato il logout. Continuare?</center></html>", 
		    			 "Conferma Log Out", JOptionPane.YES_NO_OPTION,
		                  JOptionPane.QUESTION_MESSAGE);

		    	 if (result == JOptionPane.YES_OPTION) {
		    		logoutButton.doClick();
		    	 }
		    }
		});
		
		//DEFINIZIONE PANNELLO LOGOUT
		logoutPanel = new JPanel();
		logoutPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		logoutPanel.setBounds(5, 5, 205, 30);
		logoutPanel.setLayout(null);
		getContentPane().add(logoutPanel, BorderLayout.NORTH);
		
		//DEFINIZIONE LABEL NOTIFICHE
		notification = new JLabel("Benvenuto");
		notification.setBounds(5,5,150,20);
		logoutPanel.add(notification);
		
		//DEFINIZIONE BOTTONE LOGOUT
		logoutButton = new JButton("X");
		logoutButton.setFont(new ButtonFont());
		logoutButton.setBounds(160, 0, 45, 30);
		logoutPanel.add(logoutButton);
		//DEFINISCO IL LISTENER DEL BOTTONE LOGOUT
		logoutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new LogoutWorker(client).execute();
			}
		});
		
		//DEFINISCO IL PANNELLO BODY
		bodyPanel = new JPanel();
		bodyPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		bodyPanel.setBounds(5, 40, 205, 225);
		bodyPanel.setLayout(null);
		getContentPane().add(bodyPanel);
		
		//DEFINIZIONE TITOLO
		title = new JLabel(clientRoom.getName());
		title.setBounds(10,10, 200, 20);
		bodyPanel.add(title);
		
		//DEFINIZIONE VOTE SLIDER
		voteSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);
		voteSlider.setBounds(5, 35, 110, 50);
		voteSlider.setMajorTickSpacing(10);
		voteSlider.setMinorTickSpacing(1);
		voteSlider.setPaintTicks(true);
		voteSlider.setPaintLabels(true);
		voteSlider.setFont(new ButtonFont());
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put( new Integer( 0 ), new JLabel("0") );
		labelTable.put( new Integer( 5 ), new JLabel("VOTO") );
		labelTable.put( new Integer( 10 ), new JLabel("10") );
		voteSlider.setLabelTable( labelTable );
		bodyPanel.add(voteSlider);

		//BOTTONE VOTA
		voteButton = new JButton("VOTA");
		voteButton.setBounds(120,40,70,20);
		bodyPanel.add(voteButton);
		//DEFINIZIONE LISTENER VOTA
		voteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				voteComponentEnable(false);
				int vote = voteSlider.getValue();
				new VoteRoomWorker(vote,client,server).execute();
			}
		});
		
		//DEFINIZIONE ROOMTEXT
		roomText = new JLabel(clientRoom.getDescription());
		roomText.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		//DEFINIZIONE ROOMIMAGE
		roomImage = new ImageIcon("image/"+client.getRoom().getName()+".jpg");   
		roomImageLabel = new JLabel(roomImage);
		
		//DEFINIZIONE 
		roomDescriptionPanel = new JPanel();
		roomDescriptionPanel.setLayout(new BorderLayout());
		roomDescriptionPanel.add(roomText, BorderLayout.CENTER);
		roomDescriptionPanel.add(roomImageLabel, BorderLayout.SOUTH);
		
		//DEFINIZIONE PANNELLO ROOMSCROL
		roomScrollPanel = new JScrollPane(roomDescriptionPanel);
		roomScrollPanel.createVerticalScrollBar();
		roomScrollPanel.setBounds(5,85,195,110);
		bodyPanel.add(roomScrollPanel);
		
		//DEFINIZIONE COMBOBOX NEXTROOM 
		nextRoom = new JComboBox<String>();
		for (Room room : client.getNeighborIndex().successorListOf(clientRoom))
			nextRoom.addItem(room.getName());
		nextRoom.setBounds(5, 200, 100, 20);
		bodyPanel.add(nextRoom);
		
		//DEFINIZIONE BOTTONE MUOVI
		moveButton = new JButton("MUOVI");
		moveButton.setBounds(120, 200, 80, 20);
		bodyPanel.add(moveButton);
		//DEFINIZIONE LISTENER SUL BOTTONE MUOVI
		moveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String RoomSelected = nextRoom.getSelectedItem().toString();
				moveComponentEnable(false);
				new MoveWorker(RoomSelected,client,server).execute();		
			}
		});
		
		//CONCLUDO DEFINIZIONE FRAME
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/*
	 * GESTISCE I BOTTONI DEL VOTO
	 */
	public void voteComponentEnable(Boolean bool){
		voteButton.setEnabled(bool);
		voteSlider.setEnabled(bool);
	}
	
	/*
	 * GESTISCE IL BOTTONE DEL MOVIMENTO
	 */
	public void moveComponentEnable(Boolean bool){
		moveButton.setEnabled(bool);
		nextRoom.setEnabled(bool);
	}
	
	/*
	 * RICHIAMO LA FUNZIONE REMOVEALLITEMS() SU NEXTROOM
	 */
	public void nextRoom_removeAllItems() {
		nextRoom.removeAllItems();
	}
	
	/*
	 * RICHIAMO LA FUNZIONE ADDITEM89 SU NEXTROOM
	 */
	public void nextRooms_addItem(String nome) {
		nextRoom.addItem(nome);
	}
	
	/*
	 * DESCRIZIONE ACQUARIO
	 */
	public String acquarioDescription(){
		return "<html>"
				+ "<h3 style=\"color:blue;\">"
				+ "Acquario di genova"
				+ "</h3>"
				+ "<p>"
				+ "Informazioni sull'acquario"
				+ "</p>"
				+ "<image src=\"info.jpg\" width=\"100%\">"
				+ "</html>";
	}
	
	//SETTER AND GETTER
	public void setRoomText(String text){
		roomText.setText(text);
	}
	
	public void setTitle(String string){
		title.setText(string);
		roomDescriptionPanel.remove(roomImageLabel);
		roomImage = new ImageIcon("image/"+string+".jpg");
		roomImageLabel = new JLabel(roomImage);
		roomDescriptionPanel.add(roomImageLabel, BorderLayout.SOUTH);
	}
	
	public ClientFrameLogin getClientFrameLogin(){
		return clientFrameLogin;
	}
	
	public void setNotification(String info){
		notification.setText(info);
	}
	
	
}

