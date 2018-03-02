package clientGUI;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.JPasswordField;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

import javax.swing.border.LineBorder;

import server.ServerInterface;
import worker.LoginWorker;
import worker.RegisterWorker;

import java.awt.Color;
import javax.swing.JLabel;


public class ClientFrameLogin extends JFrame{

	private static final long serialVersionUID = -4231080042449601151L;
	
	//VARIABILI
	private ClientFrameLogin thisClientInterfaceLogin;
	
	//COMPONENTI DEL FRAME
	private JLabel welcome;
	private JPanel title;
	private JLabel usernameText;
	private JTextArea username;
	private JLabel passwordText;
	private JPasswordField password;
	private JLabel loginInfo;
	private JButton loginButton;
	private JPanel body;
	private JLabel image;
	private JButton registerButton;
	private JPanel bottom;
	
	//INIZIALIZZO CLIENT FRAME LOGIN
	public ClientFrameLogin (ServerInterface server){
		super("Login");
		thisClientInterfaceLogin = this;
		setResizable(false);
		setBounds(100, 100, 200, 300);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		
		//DEFINIZIONE CHIUSURA FINESTRA
		addWindowListener(new WindowAdapter()
		{
		    public void windowClosing(WindowEvent e)
		    {
		    	 System.exit(0);
		    }
		});
		
		//DEFINIZIONE PANNELLO TITOLO
		title = new JPanel();
		getContentPane().add(title, BorderLayout.NORTH);
		welcome = new JLabel(""
				+ "<html><p style=\"color:#000080;\">Benvenuto</p></html>"
				);
		welcome.setHorizontalAlignment(SwingConstants.CENTER);
		title.add(welcome);
		
		//DEFINIZIONE PANNELLO BODY
		body = new JPanel();
		body.setBorder(new LineBorder(new Color(0, 0, 5)));
		body.setLayout(null);
		getContentPane().add(body, BorderLayout.CENTER);;
				
		//DEFINIZIONE USERNAMETEXT
		usernameText = new JLabel("Username: ");
		usernameText.setBounds(10,15, 70, 25);
		
		//DEFINIZIONE USERNAME
		username = new JTextArea("username");
		username.setBounds(90,15,100,20);
		
		//DEFINIZIONE PASSWORDTEXT
		passwordText = new JLabel("Password: ");
		passwordText.setBounds(10, 40, 70, 25);
		
		//DEFINIZIONE PASSWORD
		password = new JPasswordField("password");
		password.setBounds(90,40,100,20);
		
		//DEFINIZIONE LOGININFO
		loginInfo = new JLabel("");
		loginInfo.setBounds(10,65,175,25);
		loginInfo.setHorizontalAlignment(SwingConstants.CENTER);
		
		//DEFINIZIONE LOGINBUTTON
		loginButton = new JButton("LOGIN");
		loginButton.setBounds(40, 95, 120, 25);
		//DEFINIZIONE LISTENER LOGIN 
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = username.getText();
				String pass = String.valueOf(password.getPassword());
				if (username.equals("") ){
					setLoginInfo("<html><p style=\"color:red;\">Error! Empty username</p></html>");
					return;
				}
				if(password.equals("")){
					setLoginInfo("<html><p style=\"color:red;\">Error! Empty password</p></html>");
					return;
				}
				new LoginWorker(user, pass, server, thisClientInterfaceLogin ).execute();
			}
		});
		
		//DEFINIZIONE BOTTONE REGISTRAZIONE
		registerButton = new JButton("REGISTRATI");
		registerButton.setBounds(40,120,120,25);
		//DEFINIZIONE LISTENER REGISTRAZIONE
		registerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = username.getText();
				String pass = String.valueOf(password.getPassword());
				if (user.equals("")){
					setLoginInfo("<html><p style=\"color:red;\">Errore! Username vuoto</p></html>");
					return;
				}
				if (pass.equals("")){
					setLoginInfo("<html><p style=\"color:red;\">Errore! Password vuota</p></html>");
				}
				new RegisterWorker(user,pass,server,thisClientInterfaceLogin).execute();
			}
		});
		
		//AGGIUNGO 
		body.add(usernameText);
		body.add(username);
		body.add(passwordText);
		body.add(password);
		body.add(loginInfo);
		body.add(loginButton);
		body.add(registerButton);
		
		//DEFINIZIONE BUTTON PANEL
		bottom = new JPanel();
		getContentPane().add(bottom, BorderLayout.SOUTH);
		ImageIcon imageLogin = new ImageIcon("image/login.jpg");   	
		image = new JLabel(imageLogin);
		bottom.add(image);
			
		//RENDO VISIVILE IL FRAME
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/*
	 * SPEGNE IL FRAME
	 */
	public void shutDown() {
		System.exit(0);
	}
	
	//SETTER AND GETTER
	public JLabel getLoginInfo(){
		return loginInfo;
	}

	public void setLoginInfo(String info){
		loginInfo.setText(info);
	}
	
}
