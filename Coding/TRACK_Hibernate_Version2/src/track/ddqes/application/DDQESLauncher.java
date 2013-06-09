/*
 * Title: Distributed Database Query Engine Service (DDQES)
 * Description: This class is used to launch and ready site. This launcher should be installed in each
 * site and launch individual to establish connection among sites.
 * Author: Ng Yi Ying
 * Data Created: 9 June, 2013
 * Data Modified: 9 June, 2013
 */

package track.ddqes.application;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.table.DefaultTableModel;

public class DDQESLauncher implements TrackConstants, ActionListener, KeyListener {
	public JFrame loginFrame, mainFrame;
	// login GUI
	private JPanel panel1, panel2;
	private JTextField siteText, siteIPText;
	private JLabel portText;
	private JButton launchButton;	
	// main GUI
	private JPanel topPanel, sqlPanel, connectivityPanel;
	private JPanel connectPanel, connectPanel1, connectPanel2, connectPanel3;
	public JButton submitButton;
	public JButton connectButton1, connectButton2, connectButton3;
	private JTextField sqlText, connectIPText1, connectIPText2, connectIPText3;
	public JTable connectivityTable;
	
	private TrackNetworkConnectivity siteNetworkHandler;
	private TrackNetworkConnectivity[] connectNetworkHandler;
	public int[] connectingToSiteIDs;
	private int siteID = 1;
	private int connectStatus = 0;
	
	public static void main(String[] args){
		DDQESLauncher launcher = new DDQESLauncher();
		launcher.loginSite();
	}
	
	/**
	 * Display login frame to allow the user to start/join a new game
	 */
	public void loginSite(){
		// initialization
		connectingToSiteIDs = new int[3];
		
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

	private String getPort(int siteID) {
		String port = String.valueOf(DEFAULT_PORTS[connectingToSiteIDs[0]]);
		switch(siteID){
		case 1:
			port = String.valueOf(DEFAULT_PORTS[0]);
			break;
		case 2:
			port = String.valueOf(DEFAULT_PORTS[1]);
			break;
		case 3:
			port = String.valueOf(DEFAULT_PORTS[2]);
			break;
		case 4:
			port = String.valueOf(DEFAULT_PORTS[3]);
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
				readyConnectingSiteInfo();
				setupMainGUI();
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(mainFrame, "An error has occurred: " + e1.getMessage());
			} catch (IllegalArgumentException e1){
				JOptionPane.showMessageDialog(mainFrame, "Illegal port value: " + e1.getMessage() + ". Please enter port value within 1 - 66535");
				portText.setText(String.valueOf(DEFAULT_PORTS[connectingToSiteIDs[0]]));
			} catch (BindException e1){
				JOptionPane.showMessageDialog(mainFrame, "Launch server failed: " + e1.getMessage());
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(mainFrame, "An error has occurred: " + e1.getMessage());
			}
		}else if(e.getActionCommand().equalsIgnoreCase("Connect to Site " + connectingToSiteIDs[0])){
			String[] ip = connectIPText1.getText().split("\\:");
			boolean success = processNetworkConnection(0, ip);
			if(success){
				submitButton.setEnabled(true);
				connectButton1.setText("Terminate Site " + connectingToSiteIDs[0]);
			}
		}else if(e.getActionCommand().equalsIgnoreCase("Connect to Site " + connectingToSiteIDs[1])){
			String[] ip = connectIPText1.getText().split("\\:");
			boolean success = processNetworkConnection(1, ip);
			if(success){
				submitButton.setEnabled(true);
				connectButton2.setText("Terminate Site " + connectingToSiteIDs[1]);
			}
		}else if(e.getActionCommand().equalsIgnoreCase("Connect to Site " + connectingToSiteIDs[2])){
			String[] ip = connectIPText1.getText().split("\\:");
			boolean success = processNetworkConnection(2, ip);
			if(success){
				submitButton.setEnabled(true);
				connectButton3.setText("Terminate Site " + connectingToSiteIDs[2]);
			}
		}else if(e.getActionCommand().equals("Submit SQL")){
			if(connectNetworkHandler[0].isConnected())
				connectNetworkHandler[0].passToConnectedSite("SQL" + sqlText.getText());
		}else if(e.getActionCommand().equals("Terminate Site " + connectingToSiteIDs[0])){
			terminateClientConnection(0);
			connectButton1.setText("Connect to Site " + connectingToSiteIDs[0]);
		}else if(e.getActionCommand().equals("Terminate Site " + connectingToSiteIDs[1])){
			terminateClientConnection(1);
			connectButton2.setText("Connect to Site " + connectingToSiteIDs[1]);
		}else if(e.getActionCommand().equals("Terminate Site " + connectingToSiteIDs[2])){
			terminateClientConnection(2);
			connectButton3.setText("Connect to Site " + connectingToSiteIDs[2]);
		}
	}

	private void terminateClientConnection(int site) {
		connectNetworkHandler[site].setConnectStatus(1);
		connectNetworkHandler[site].passToConnectedSite("TER");
		connectNetworkHandler[site] = null;
		submitButton.setEnabled(false);
	}

	private boolean processNetworkConnection(int site, String[] ip) {
		if(ip.length == 1){
			JOptionPane.showMessageDialog(mainFrame, "Please enter the correct IP, i.e. 127.0.0.1:31416", "Invalid IP", 
					JOptionPane.OK_OPTION + JOptionPane.ERROR_MESSAGE);
			return false;
		}else{
			if(Integer.parseInt(ip[1]) == siteNetworkHandler.getPort() && ip[0].equalsIgnoreCase(siteNetworkHandler.getIP())){
				JOptionPane.showMessageDialog(mainFrame, "You cannot connect to your own site.", "Invalid IP", 
						JOptionPane.OK_OPTION + JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else{
				TrackNetworkConnectivity networkHandler = new TrackNetworkConnectivity(ip[0], Integer.parseInt(ip[1]), siteIPText.getText() + ":" + String.valueOf(portText.getText()));
				networkHandler.setLauncher(this);
				networkHandler.setConnectStatus(1);
				networkHandler.setConnectingFromSite(site);
				networkHandler.setAlive(true);
				networkHandler.start();
				submitButton.setEnabled(true);
				connectNetworkHandler[site] = networkHandler;
				return true;
			}
		}
		
	}

	private void readyConnectingSiteInfo() {
		// setting up site IDs which current site will be connecting to
		int siteID = Integer.parseInt(siteText.getText());
		for(int i = 1; i <= 3; i++){
			if(i == siteID){
				siteID++;
				connectingToSiteIDs[i - 1] = siteID;
			}else{
				connectingToSiteIDs[i - 1] = i;
			}
		}
		
		// setting up the array which holds the network handler to other sites
		connectNetworkHandler = new TrackNetworkConnectivity[3];
	}

	@SuppressWarnings("serial")
	private void setupMainGUI() {
		mainFrame = new JFrame();
		mainFrame.getContentPane().setLayout(new BorderLayout());
		topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1, 2));
		
		connectPanel = new JPanel();
		connectPanel.setLayout(new GridLayout(3, 1));
		connectPanel1 = new JPanel();
		connectPanel2 = new JPanel();
		connectPanel3 = new JPanel();
		
		connectIPText1 = new JTextField(12);
		connectIPText1.setText(String.valueOf(DEFAULT_IP) + ":" + String.valueOf(DEFAULT_PORTS[0]));
		connectButton1 = new JButton("Connect to Site " + connectingToSiteIDs[0]);
		connectButton1.addActionListener(this);
		connectPanel1.add(new JLabel("Connect IP: "));
		connectPanel1.add(connectIPText1);
		connectPanel1.add(connectButton1);
		connectPanel.add(connectPanel1);
		
		connectIPText2 = new JTextField(12);
		connectIPText2.setText(String.valueOf(DEFAULT_IP) + ":" + String.valueOf(DEFAULT_PORTS[1]));
		connectButton2 = new JButton("Connect to Site " + connectingToSiteIDs[1]);
		connectButton2.addActionListener(this);
		connectPanel2.add(new JLabel("Connect IP: "));
		connectPanel2.add(connectIPText2);
		connectPanel2.add(connectButton2);
		connectPanel.add(connectPanel2);
		
		connectIPText3 = new JTextField(12);
		connectIPText3.setText(String.valueOf(DEFAULT_IP) + ":" + String.valueOf(DEFAULT_PORTS[2]));
		connectButton3 = new JButton("Connect to Site " + connectingToSiteIDs[2]);
		connectButton3.addActionListener(this);
		connectPanel3.add(new JLabel("Connect IP: "));
		connectPanel3.add(connectIPText3);
		connectPanel3.add(connectButton3);
		connectPanel.add(connectPanel3);
		
		
		topPanel.add(connectPanel);
		JLabel lblCurrent = new JLabel(" Current Site IP: " + siteIPText.getText() + ":" + portText.getText() + "  ");
		lblCurrent.setHorizontalAlignment(JLabel.RIGHT);
		topPanel.add(lblCurrent);
		
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
		JScrollPane scroll = new JScrollPane(connectivityTable);
		scroll.setPreferredSize(new Dimension(connectivityPanel.getWidth(), 150));
		connectivityPanel.add(scroll, BorderLayout.CENTER);
		connectivityTable.repaint();
		mainFrame.add("South", connectivityPanel);
		
		mainFrame.setTitle("TRACK Distributed Database Query Engine System [ Site " + siteID + " ]");
		mainFrame.setSize(FRAME_DISPLAY_WIDTH, FRAME_DISPLAY_HEIGHT);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.revalidate();
		mainFrame.pack();
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
