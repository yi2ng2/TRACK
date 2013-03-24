import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLAccess {
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	public MySQLAccess(){
		try {
			connectDatabase();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void connectDatabase() throws ClassNotFoundException{
		 // This will load the MySQL driver, each DB has its own driver
	      Class.forName("com.mysql.jdbc.Driver");
	      // Setup the connection with the DB
	      try {
			//connect = DriverManager.getConnection("jdbc:mysql://localhost/yyildcarddb?" + "user=root&password=");
	    	  connect = DriverManager.getConnection("jdbc:mysql://localhost/mysql","root","");
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readDatabase(String queryStatement) throws Exception{
		try{
		    // Statements allow to issue SQL queries to the database
		    statement = connect.createStatement();
		    // Result set get the result of the SQL query
		    resultSet = statement.executeQuery(queryStatement);
		    writeResultSet(resultSet);
		 }catch (Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
	}
	
	public void updateDatabase(String queryStatement){
	    // PreparedStatements can use variables and are more efficient
	    try {
			preparedStatement = connect.prepareStatement(queryStatement);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    /*
	    // Alternatively
	    preparedStatement = connect.prepareStatement("insert into DBNAME.TABLENAME values (default, ?, ?, ?, ? , ?, ?)");
	    // Parameters start with 1
	    preparedStatement.setString(1, "Test");
	    preparedStatement.setString(2, "TestEmail");
	    preparedStatement.setString(3, "TestWebpage");
	    preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
	    preparedStatement.setString(5, "TestSummary");
	    preparedStatement.setString(6, "TestComment");
	    preparedStatement.executeUpdate();

	    preparedStatement = connect.prepareStatement("SELECT myuser, webpage, datum, summery, COMMENTS from FEEDBACK.COMMENTS");
	    resultSet = preparedStatement.executeQuery();
	    writeResultSet(resultSet);
	    */
	}

	private void writeResultSet(ResultSet resultSet) throws SQLException {
	    // ResultSet is initially before the first data set
	    while(resultSet.next()){
	        // It is possible to get the columns via name
	        // also possible to get the columns via the column number
	        // which starts at 1
	        // e.g. resultSet.getString(2);
	    	for(int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
	    	    // System.out.println("Column " + i  + " "+ resultSet.getMetaData().getColumnName(i));
	    	    System.out.print(resultSet.getString(resultSet.getMetaData().getColumnName(i)) + " || ");
	    	}
	    	System.out.println("");
	    }
	  }

	  public void closeDatabase(){
		  close();
	  }
	  
	  private void close(){
		  try{
			  if(resultSet != null){
				  resultSet.close();
			  }
			  
			  if(statement != null) {
			      statement.close();
			  }
			  
			  if(connect != null){
				  connect.close();
			  }
		  }catch (Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
	  }


}
