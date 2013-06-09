/*
 * Title: Distributed Database Query Engine Service (DDQES)
 * Description: This interface captures all the constants value for the project
 * Author: Ng Yi Ying
 * Data Created: 6 June, 2013
 * Data Modified: 6 June, 2013
 */
package track.ddqes.application;

public interface TrackConstants {
	// networking	
	public static final int DEFAULT_SITE_ID = 1;
	public static final String DEFAULT_IP = "127.0.0.1";
	public static final int[] DEFAULT_PORTS = { 31416, 31417, 31418, 31419 };
	
	// networking display configurations
	public static final int LOGIN_DISPLAY_WIDTH = 225;
	public static final int LOGIN_DISPLAY_HEIGHT = 140;
	public static final int FRAME_DISPLAY_WIDTH = 725;
	public static final int FRAME_DISPLAY_HEIGHT = 425;
	
	// SQL query processing
	public static final int PROCESS_COLUMN = 1;
	public static final int PROCESS_TABLE = 2;
	public static final int PROCESS_CONDITION = 3;
	public static final int PROCESS_GROUP = 4;
	public static final int PROCESS_HAVING = 5;
	public static final int PROCESS_ORDER = 6;
	
	// table information
	public static final String[] PUBLISHER_TABLE = { "pid", "pid,pname,pnation" }; 
	public static final String[] CUSTOMER_TABLE = { "cid", "cid,cname,crank" };
	public static final String[] BOOK_TABLE = { "bid", "bid,btitle,bauthors,bpid,bcopies" };
	public static final String[] ORDERS_TABLE = { "ocid,obid", "ocid,obid,oquantity" };
}
