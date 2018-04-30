package com.lmax.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

import com.mysql.jdbc.PreparedStatement;

public class Connect_Mysql {
	
	public void declaracao(long instrumentId, String instrumentName, Date lastUpdate, FixedPointNumber bid, FixedPointNumber ask) throws SQLException {
		System.out.println(Long.toString(instrumentId) + " " + instrumentName + " " + lastUpdate + " " + bid + " " + ask);
		
	try {
	
	//1.Get a Connection to database
        Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/alveo_kinesis?useSSL=false&createDatabaseIfNotExist=true", "root", "root");
     //2. Create a statement
        PreparedStatement myStmt = (PreparedStatement) myConn.prepareStatement("INSERT INTO MarketData (InstrumentID,Name,date,bid,ask) VALUES ('" + Long.toString(instrumentId) + "','" + instrumentName + "','" + lastUpdate + "',"
        		+ "'" + bid + "','" +ask+ "')");
     //3. Execute a SQL query
        myStmt.executeUpdate();
        myStmt.closeOnCompletion();
	}catch(Exception e) {System.out.println(e);}
    finally {
    	System.out.println("Insert Completed.");
    	
    }
        

        
	}
	

}
