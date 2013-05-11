
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.hibernate.Session;
import org.hibernate.Transaction;

import track.hibernate.entity.Customer;


public class Launcher implements TrackConstants, ActionListener, KeyListener {
	public JFrame loginFrame, mainFrame;
	// login GUI
	private JPanel panel1, panel2;
	private JTextField siteText, siteIPText;
	private JLabel portText;
	private JButton launchButton;
	public JButton connectButton;	
	// main GUI
	private JPanel topPanel, sqlPanel, connectPanel, connectivityPanel;
	public JButton submitButton;
	private JTextField sqlText, connectIPText;
	public JTable connectivityTable;
	
	private TrackNetworkConnectivity siteNetworkHandler, connectNetworkHandler;
	private int siteID = 1;
	private int connectStatus = 0;
	
	public static void main(String[] args){
		Launcher launcher = new Launcher();
		launcher.loginGame();
	}
	
	/**
	 * Display login frame to allow the user to start/join a new game
	 */
	public void loginGame(){
		loginFrame = new JFrame();
		loginFrame.setTitle("Login");
		loginFrame.setSize(LOGIN_DISPLAY_WIDTH, LOGIN_DISPLAY_HEIGHT);
		loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loginFrame.setLayout(new GridLayout(3, 2));

		loginFrame.add(new JLabel(" IP Address"));
		
		siteIPText = new JTextField(9);
		panel1 = new JPanel();
		panel1.add(siteIPText);
		try {
			siteIPText.setText(String.valueOf(InetAddress.getLocalHost().getHostAddress()));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		loginFrame.add(panel1);
		
		loginFrame.add(new JLabel(" Enter Site ID"));
		panel2 = new JPanel();
		siteText = new JTextField(5);
		siteText.setText(String.valueOf(DEFAULT_SITE_ID));
		siteText.setName("siteText");
		siteText.addKeyListener(this);
		portText = new JLabel();
		portText.setText(getPort(Integer.parseInt(siteText.getText())));
		panel2.add(siteText);
		panel2.add(portText);
		loginFrame.add(panel2);
		
		loginFrame.add(new JLabel());
		
		launchButton = new JButton("Launch!");
		launchButton.addActionListener(this);
		loginFrame.add(launchButton);
		
		loginFrame.setVisible(true);
	}
	/*
	public static void main(String[] args){
		Customer customer = new Customer();
		customer.setId(3);
		customer.setName("Ng Yi Ying");
		customer.setRank(3);

		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();

		try{
			session.save(customer);
			tx.commit();
		}catch (RuntimeException e) {
			System.out.println("Rolling back...");
			 tx.rollback();
		}
		
		Object o = null;
		o = session.get("track.hibernate.entity.Customer", 2);
		System.out.println(o.toString());
		 
		session.close();
		HibernateUtil.shutdown();
	}
	*/

	private String getPort(int parseInt) {
		String port = String.valueOf(DEFAULT_PORT_1);
		switch(Integer.parseInt(siteText.getText())){
		case 1:
			port = String.valueOf(DEFAULT_PORT_1);
			break;
		case 2:
			port = String.valueOf(DEFAULT_PORT_2);
			break;
		case 3:
			port = String.valueOf(DEFAULT_PORT_3);
			break;
		case 4:
			port = String.valueOf(DEFAULT_PORT_4);
			break;
		default:
			break;
		}
		return port;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equalsIgnoreCase("Launch!")){
			try {
				connectStatus = 0;
				siteNetworkHandler = new TrackNetworkConnectivity(siteIPText.getText(), Integer.valueOf(portText.getText()));
				siteNetworkHandler.setLauncher(this);
				siteNetworkHandler.setConnectStatus(0);
				siteNetworkHandler.setAlive(true);
				siteNetworkHandler.start();
				setupMainGUI();
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(mainFrame, "An error has occurred: " + e1.getMessage());
			} catch (IllegalArgumentException e1){
				JOptionPane.showMessageDialog(mainFrame, "Illegal port value: " + e1.getMessage() + ". Please enter port value within 1 - 66535");
				portText.setText(String.valueOf(DEFAULT_PORT_1));
			} catch (BindException e1){
				JOptionPane.showMessageDialog(mainFrame, "Launch server failed: " + e1.getMessage());
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(mainFrame, "An error has occurred: " + e1.getMessage());
			}
		}else if(e.getActionCommand().equalsIgnoreCase("Connect")){
			String[] ip = connectIPText.getText().split("\\:");
			if(ip.length == 1){
				JOptionPane.showMessageDialog(mainFrame, "Please enter the correct IP, i.e. 127.0.0.1:31416", "Invalid IP", 
						JOptionPane.OK_OPTION + JOptionPane.ERROR_MESSAGE);
			}else{
				if(Integer.parseInt(ip[1]) == siteNetworkHandler.getPort() && ip[0].equalsIgnoreCase(siteNetworkHandler.getIP())){
					JOptionPane.showMessageDialog(mainFrame, "You cannot connect to your own site.", "Invalid IP", 
							JOptionPane.OK_OPTION + JOptionPane.ERROR_MESSAGE);
				}
				else{
					connectNetworkHandler = new TrackNetworkConnectivity(ip[0], Integer.parseInt(ip[1]), siteIPText.getText() + ":" + String.valueOf(portText.getText()));
					connectNetworkHandler.setLauncher(this);
					connectNetworkHandler.setConnectStatus(1);
					connectNetworkHandler.setAlive(true);
					connectNetworkHandler.start();
					submitButton.setEnabled(true);
					connectButton.setText("Terminate");
				}
			}
		}else if(e.getActionCommand().equals("Submit SQL")){
			if(connectNetworkHandler.isConnected())
				connectNetworkHandler.passToConnectedSite("SQL" + sqlText.getText());
		}else if(e.getActionCommand().equals("Terminate")){
			connectButton.setText("Connect");
			connectNetworkHandler.setConnectStatus(1);
			connectNetworkHandler.passToConnectedSite("TER");
			connectNetworkHandler = null;
			submitButton.setEnabled(false);
		}
	}

	private void setupMainGUI() {
		mainFrame = new JFrame();
		mainFrame.getContentPane().setLayout(new BorderLayout());
		topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1, 2));
		topPanel.add(new JLabel(" Current Site IP: " + siteIPText.getText() + ":" + portText.getText()));
		
		connectPanel = new JPanel();
		connectIPText = new JTextField(12);
		connectIPText.setText(String.valueOf(DEFAULT_IP) + ":" + String.valueOf(DEFAULT_PORT_1));
		connectButton = new JButton("Connect");
		connectButton.addActionListener(this);
		connectPanel.add(new JLabel("Connect IP: "));
		connectPanel.add(connectIPText);
		connectPanel.add(connectButton);
		topPanel.add(connectPanel);
		mainFrame.getContentPane().add("North",topPanel);
		
		sqlPanel = new JPanel();
		sqlPanel.add(new JLabel("Enter SQL:"));
		sqlText = new JTextField(50);
		sqlPanel.add(sqlText);
		submitButton = new JButton("Submit SQL");
		submitButton.addActionListener(this);
		submitButton.setEnabled(false);
		sqlPanel.add(submitButton);	
		mainFrame.getContentPane().add("Center",sqlPanel);
		
		connectivityPanel = new JPanel();
		connectivityPanel.setBorder(BorderFactory.createTitledBorder("Connectivity log"));
		connectivityPanel.setLayout(new BorderLayout());
		connectivityTable = new JTable(new DefaultTableModel(new Object[][]{}, new String[]{"URL"}) {
	        public boolean isCellEditable(int row, int column) {
	            return false;
	        }
	    });
		connectivityPanel.setSize(connectivityPanel.getWidth(), 150);
		connectivityPanel.add(new JScrollPane(connectivityTable), BorderLayout.CENTER);
		mainFrame.add("South", connectivityPanel);
		
		mainFrame.setTitle("TRACK Distributed Database Query Engine System [ Site " + siteID + " ]");
		mainFrame.setSize(FRAME_DISPLAY_SIZE, FRAME_DISPLAY_SIZE);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}
	
	public int getSiteID(){
		return this.siteID;
	}

	@Override
	public void keyPressed(KeyEvent key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent key) {
		if(key.getComponent().getName().equalsIgnoreCase("siteText")){
			JTextField temp = (JTextField)key.getComponent();
			if(temp.getText().length() > 0){
				siteID = Integer.parseInt(temp.getText());
				portText.setText(getPort(Integer.parseInt(siteText.getText())));
			}
		}
		
	}

	@Override
	public void keyTyped(KeyEvent key) {
		// TODO Auto-generated method stub
		
	}
	
	public int getConnectStatus(){
		return this.connectStatus;
	}

}
