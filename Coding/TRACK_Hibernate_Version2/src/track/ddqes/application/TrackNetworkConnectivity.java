/**
 * 
 * @author Ng Yi Ying
 * Created on: 10 May, 2013
 * Last modified: 10 May, 2013
 * Description: This class serves to handle the socket communication to support multiple networked computers
 *
 */

package track.ddqes.application;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class TrackNetworkConnectivity extends Thread {
	private int connectingFromSite;
	private int port;
	private String ip;
	private String connectingFromIP, connectingToIP;
	private Socket socket;
	private ServerSocket serverSocket;
	private BufferedReader inputStream;
	private PrintWriter outputStream;
	private boolean alive;
	private int connectStatus;
	private DDQESLauncher launcher;
	
	public TrackNetworkConnectivity(){
		
	}
	
	/**
	 * The preferred constructor for client
	 * @param port The port which intended to listen on
	 * @throws IOException
	 */
	public TrackNetworkConnectivity(String ip, int port) throws IOException{
		serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress(ip, port));
		this.ip = ((InetSocketAddress)serverSocket.getLocalSocketAddress()).getAddress().toString().replace("/", "");
		this.port = (int)(((InetSocketAddress)serverSocket.getLocalSocketAddress()).getPort());
	}
	
	public TrackNetworkConnectivity(String ip, int port, String connectingFrom){
		this.ip = ip;
		this.port = port;
		this.connectingFromIP = connectingFrom;
	}
	
	public void setConnectingFromSite(int site){
		this.connectingFromSite = site;
	}
	
	/**
	 * Setter for the connect status(to connect/ to be connected)
	 * @param client The value
	 */
	public void setConnectStatus(int connectStatus){
		this.connectStatus = connectStatus;
	}
	
	/**
	 * To set the flag on to keep the loop running after the thread is ran
	 * @param alive The value
	 */
	public void setAlive(boolean alive){
		this.alive = alive;
	}
	
	public void setLauncher(DDQESLauncher launcher){
		this.launcher = launcher;
	}
	
	/**
	 * Set the network IP
	 * @param IP The value
	 */
	public void setIP(String ip){
		this.ip = ip;
	}
	
	/**
	 * Set the network port
	 * @param port The value
	 */
	public void setPort(int port){
		this.port = port;
	}
	
	/**
	 * Get the network IP
	 * @return The IP
	 */
	public String getIP(){
		return this.ip;
	}
	
	/**
	 * Get the network port
	 * @return The port
	 */
	public int getPort(){
		return this.port;
	}
	
	/**
	 * Check if single player or dual-player
	 * @return True/ False
	 */
	public boolean isConnected(){
		if(socket != null && socket.isConnected())
			return true;
		else
			return false;
	}
	
	private void addConnectivityLog(String log){
		DefaultTableModel model = (DefaultTableModel)launcher.connectivityTable.getModel();
		model.addRow(new Object[]{ log });
	}

	/**
	 * Default method to be executed when the thread is ran
	 */
	public void run(){
		String line ="";
		if(connectStatus == 0){
			try {
				socket = serverSocket.accept();
				inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				outputStream = new PrintWriter(socket.getOutputStream());

				passToConnectedSite("Connected to: " + this.ip + ":" + this.port + " [ site " + launcher.getSiteID() + "]");

				while(true){
					if(alive){
						if(socket == null)
							JOptionPane.showMessageDialog(launcher.mainFrame, "closed");
						line = inputStream.readLine();
						if(line != null){
							if(line.length() >= 6 && line.trim().substring(0, 6).equalsIgnoreCase("IPADDR")){
								connectingFromIP = line.substring(6, line.length());
								addConnectivityLog("Connecting from: " + connectingFromIP);
							}else if(line.length() >= 3 && line.trim().substring(0, 3).equalsIgnoreCase("SQL")){
								String SQL = line.substring(3, line.length());
								addConnectivityLog("SQL-from: " + SQL + " [ " + connectingFromIP + " ] ");
								passToConnectedSite(line); // pass to connecting site to update activitiy log
							}else if(line.equalsIgnoreCase("TER")){
								addConnectivityLog("Termination " + connectingFromIP);
								connectingFromIP = "";
								passToConnectedSite("TER");
								terminateNetwork();
							}else{
								JOptionPane.showMessageDialog(launcher.mainFrame, line);
							}
						}
					}else
						break;
				}
			} catch (SocketException e1){
				addConnectivityLog("Termination: " + connectingFromIP);
				connectingFromIP = "";
				terminateNetwork();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(launcher.mainFrame, "An error has occurred: " + e1.getMessage());
			}
		}else{
			try {
				socket= new Socket(ip, port);
				inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				outputStream = new PrintWriter(socket.getOutputStream());
				connectingToIP = ip + ":" + port;
				// pass to the site which it connected to to update connectivity log
				passToConnectedSite("IPADDR" + connectingFromIP);

				while(true){
					if(alive){
						line = inputStream.readLine();
						if(line != null){
							if(line.equalsIgnoreCase("TER")){
								addConnectivityLog("Terminate connection: " + connectingToIP);
								connectingToIP = "";
							}else if(line.length() >= 3 && line.trim().substring(0, 3).equalsIgnoreCase("SQL")){
								String SQL = line.substring(3, line.length());
								addConnectivityLog("SQL-to: " + SQL + " [ " + connectingToIP + " ] ");
							}else
								addConnectivityLog(line);
							//JOptionPane.showMessageDialog(launcher.mainFrame, line);
						}
					}else
						break;
				}
			} catch (UnknownHostException e) {
				JOptionPane.showMessageDialog(launcher.mainFrame, "Unknown host. " + e.getMessage());
				launcher.submitButton.setEnabled(false);
			} catch (ConnectException e1){
				JOptionPane.showMessageDialog(launcher.mainFrame, "Sorry, there's no connection available for given IP.");
				launcher.submitButton.setEnabled(false);
				switch(connectingFromSite){
					case 0:
						launcher.connectButton1.setText("Connect to Site " + launcher.connectingToSiteIDs[0]);
						break;
					case 1:
						launcher.connectButton2.setText("Connect to Site " + launcher.connectingToSiteIDs[1]);
						break;
					case 2:
						launcher.connectButton3.setText("Connect to Site " + launcher.connectingToSiteIDs[2]);
						break;
				}
			} catch (SocketException e1){
				//JOptionPane.showMessageDialog(launcher.mainFrame, e1.getMessage() + ": The server may have terminated the connection.");
				addConnectivityLog("Connection lost: " + connectingToIP);
				connectingToIP = "";
				terminateNetwork();
			} catch (IOException e) {
				launcher.mainFrame.dispose();
				JOptionPane.showMessageDialog(launcher.mainFrame, "An error occurred: "+e);
			} 
		}
	}
	
	public String passToConnectedSite(String line){
		outputStream.println(line);
		outputStream.flush();
		return line;
	}

	/**
	 * Terminate and close all the necessary elements
	 */
	public void terminateNetwork(){
		outputStream.close();
		try {
			inputStream.close();
			if(socket != null){
				socket.close();
				socket = null;
			}
			run();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(launcher.mainFrame, "An error has occurred: " + e.getMessage());
		}
	}


}
