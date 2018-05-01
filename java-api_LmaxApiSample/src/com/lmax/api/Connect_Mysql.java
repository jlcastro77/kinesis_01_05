package com.lmax.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import com.mysql.jdbc.PreparedStatement;

public class Connect_Mysql {
	
	boolean mySqlConnect = true; 
	
	public void declaracao(long instrumentId, String instrumentName, Date lastUpdate, FixedPointNumber bid, FixedPointNumber ask) throws SQLException {
		System.out.println(Long.toString(instrumentId) + " " + instrumentName + " " + lastUpdate + " " + bid + " " + ask);
		
	try {
		Connection myConn = null;
		mySqlConnect = isDbConnected();
		
		if (mySqlConnect == false)
		{
			//1.Get a Connection to database
		   myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/alveo_kinesis?useSSL=false&createDatabaseIfNotExist=true", "root", "root");
		   
		}  	
		   
		   //2. Create a statement
		PreparedStatement myStmt = (PreparedStatement) myConn.prepareStatement("INSERT INTO MarketData (InstrumentID,Name,date,bid,ask) VALUES ('" + Long.toString(instrumentId) + "'"
    		+ ",'" + instrumentName + "','" + lastUpdate + "'," + "'" + bid + "','" +ask+ "')");
		//3. Execute a SQL query
			myStmt.executeUpdate();
			myConn.close();
		}catch(Exception e) {System.out.println(e);}
    	finally {
    		System.out.println("Insert Completed.");
    	
    	}
                
	}
	
	public static boolean isDbConnected() {
	    boolean isConnected = false;

	    try 
	    {
	    	Connection myConn = null;
	    	// 2. Create a statement
	    	Statement myStmt = myConn.createStatement();
	    	// 3. Execute a SQL query
	    	ResultSet myRs = myStmt.executeQuery("SELECT * FROM alveo_kinesis.marketdata");
	    	System.out.println(myRs.getString("InstrumentID") + ", " + myRs.getString("Name") + ", " + myRs.getString("date") );
	    	isConnected = true;
	    }
	    
	    catch(Exception exc)
		{
			
		}
	    
	    return isConnected;
	}

}














