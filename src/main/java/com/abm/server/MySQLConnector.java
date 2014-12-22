package com.abm.server;


import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import com.ocs.bean.abm.Balance;
import com.ocs.bean.abm.Counter;
import com.ocs.bean.abm.DeductBalance;
import com.ocs.bean.abm.DeductCounter;
import com.ocs.bean.abm.ReserveBalance;
import com.ocs.bean.abm.Subscriber;
import com.ocs.utils.PropertiesUtils;

public class MySQLConnector { 
	
	/**
	 * 连接数据库
	 */
	public Connection getConnection(){
		//驱动程序名
		final String driverName="com.mysql.jdbc.Driver";
		
		//数据库用户名
		final String userName=PropertiesUtils.getDBUsername();
		//数据库名
		final String dbName="abm";
		//密码
		final String userPasswd=PropertiesUtils.getDBPassword();
		//数据库服务器地址
		final String ipAddr=PropertiesUtils.getDBIP();
		//联结字符串
		String url="jdbc:mysql://"+ipAddr+"/"+dbName+"?user="+userName+"&password="+userPasswd;	
		
		try {
			Class.forName(driverName).newInstance();
			Connection connection= (Connection) DriverManager.getConnection(url);
			return connection;
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取用户信息
	 * @param id 用户id - 电话号码
	 * @return Subscriber
	 * */
	public Subscriber getSubsInfo(long id){
		Subscriber subs = null;
		// 表名
		String tableName = "SUBS_INFO";
		Connection connection = getConnection();
		Statement statement;
		
		try {
			statement = (Statement) connection.createStatement();
			String sql = "SELECT * FROM "+tableName+" WHERE SUBS_ID = '"+id+"'";
			System.out.println(sql);
			ResultSet rs;
			
			rs = (ResultSet)statement.executeQuery(sql);
			
			if(rs.next()){
				subs = new Subscriber(0, Long.toString(rs.getLong("SUBS_ID")), rs.getString("BELONG_AREA"),Long.toString(rs.getLong("ACCT_ID")));
				System.out.println(subs.toString());
			}
			
			//关闭
			rs.close(); 
			statement.close(); 
			connection.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return subs; 
	}
	
	/**
	 * 查询一个用户的所有账本余额
	 * @param id - 账户id，即用户电话号码
	 */
	public ArrayList<Balance> getAccountBalance(long id){
		ArrayList<Balance> balancesList = new ArrayList<Balance>();
		
		//表名
		String tableName="ACCT_BALANCE";
		Connection connection = getConnection();
		Statement statement;
		
		try {
			statement = (Statement) connection.createStatement();
			String sql="SELECT * FROM "+tableName + " WHERE ACCT_ID = '"+ id +"'";
			System.out.println(sql);
			ResultSet rs;
			
			rs = (ResultSet) statement.executeQuery(sql);
	
			while(rs.next()){
				Balance blc = new Balance(rs.getLong("ACCT_BALANCE_ITEM_ID"), 0, rs.getTimestamp("EXP_TIME"), rs.getDouble("AMT"));
				balancesList.add(blc);
				
				System.out.println(blc.toString());
			}
			
			//关闭
			rs.close(); 
			statement.close(); 
			connection.close();
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return balancesList;
		
	}
	
	public ArrayList<Balance> getAccountBalance(long acntId,long balanceId){
		ArrayList<Balance> balancesList = new ArrayList<Balance>();
		
		//表名
		String tableName="ACCT_BALANCE";
		Connection connection = getConnection();
		Statement statement;
		
		try {
			statement = (Statement) connection.createStatement();
			String sql="SELECT * FROM "+tableName + " WHERE ACCT_ID = '"+ acntId +"' AND ACCT_BALANCE_ITEM_ID = '"+balanceId+"'";
			System.out.println(sql);
			ResultSet rs;
			
			rs = (ResultSet) statement.executeQuery(sql);
	
			while(rs.next()){
				Balance blc = new Balance(rs.getLong("ACCT_BALANCE_ITEM_ID"),0,rs.getTimestamp("EXP_TIME"),rs.getDouble("AMT"));
				balancesList.add(blc);
				
				System.out.println(blc.toString());
			}
			
			//关闭
			rs.close(); 
			statement.close(); 
			connection.close();
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return balancesList;
		
	}
	
	public ArrayList<Counter> getCounterByObjectID(long id){
		ArrayList<Counter> countersList = new ArrayList<Counter>();
		
		//表名
		String tableName="COUNTER";
		Connection connection = getConnection();
		Statement statement;
		
		try {
			statement = (Statement) connection.createStatement();
			String sql="SELECT * FROM "+tableName + " WHERE OBJECT_ID = '"+ id +"'";
			System.out.println(sql);
			ResultSet rs;
			
			rs = (ResultSet) statement.executeQuery(sql);
	
			while(rs.next()){
				Counter cnt = new Counter(rs.getLong("COUNTER_ID"), 
										rs.getString("COUNTER_TYPE"), 
										rs.getTimestamp("EXP_DATE"), 
										rs.getDouble("COUNTER_VALUE"),
										rs.getDouble("COUNTER_THRESHOLD"));
				countersList.add(cnt);
				
				System.out.println(cnt.toString());
			}
			
			//关闭
			rs.close(); 
			statement.close(); 
			connection.close();
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return countersList;
		
	}
	
	public ArrayList<Counter> getCounterByCounterID(long id){
		ArrayList<Counter> countersList = new ArrayList<Counter>();
		
		//表名
		String tableName="COUNTER";
		Connection connection = getConnection();
		Statement statement;
		
		try {
			statement = (Statement) connection.createStatement();
			String sql="SELECT * FROM "+tableName + " WHERE COUNTER_ID = '"+ id +"'";
			System.out.println(sql);
			ResultSet rs;
			
			rs = (ResultSet) statement.executeQuery(sql);
	
			while(rs.next()){
				Counter cnt = new Counter(rs.getLong("COUNTER_ID"), 
							rs.getString("COUNTER_TYPE"), 
							rs.getTimestamp("EXP_DATE"), 
							rs.getDouble("COUNTER_VALUE"),
							rs.getDouble("COUNTER_THRESHOLD"));
				countersList.add(cnt);

				System.out.println(cnt.toString());
			}
			
			//关闭
			rs.close(); 
			statement.close(); 
			connection.close();
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return countersList;
		
	}
	
	public boolean deductBalance(ArrayList<DeductBalance> dBalances){
		// Deduct Balance print infos
		System.out.println("账本余额扣减- "+ dBalances.size() + "条记录");
		
		// 表名
		String tableName = "ACCT_BALANCE";
		Connection connection = getConnection();
		Statement statement;
		
		for( DeductBalance dBalance : dBalances){
			ArrayList<Balance> rsltBalances = getAccountBalance(dBalance.getAccountID(), dBalance.getBalanceID());
			
			if(!rsltBalances.isEmpty()){
				// log
				System.out.println("查找到匹配数据，进行扣减");
				
				Balance rsltBalance = rsltBalances.get(0);
				rsltBalance.setBalanceValue(rsltBalance.getBalanceValue()-dBalance.getChgValue());
				
				System.out.println("账户余额扣减：id - " + rsltBalance.getBalanceID() + "  value - "+rsltBalance.getBalanceValue());
				
				try {
					statement = (Statement) connection.createStatement();
					statement.executeQuery("SET NAMES UTF8");
							
					String str = "UPDATE "+tableName
							+ " SET AMT = ?,UPDATE_TIME = ? " 
							+ "WHERE ACCT_ID = '" + dBalance.getAccountID()
							+ "' AND ACCT_BALANCE_ITEM_ID = '" +dBalance.getBalanceID() + "'";
					PreparedStatement stm = (PreparedStatement) connection.prepareStatement(str);
							
					stm.setDouble(1, rsltBalance.getBalanceValue());
					stm.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
							
					stm.executeUpdate();
							
					connection.close();
					
					System.out.println("账户余额扣减成功");
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("账户余额扣减失败");
					return false;
				}
				
			}
		}
		
		return true;
	}
	
	public boolean deductCounter(ArrayList<DeductCounter> dCounters, String phoneNum){
		// Deduct Counter print infos
		System.out.println("累积量扣减- " + dCounters.size() + "条记录");
		
		//表名
		String tableName="COUNTER";
		Connection connection = getConnection();
		Statement statement;
			
		for(DeductCounter dCounter : dCounters){
			ArrayList<Counter> rsltCounters = getCounterByCounterID(dCounter.getCounterID());
			
			if(!rsltCounters.isEmpty()){
				// log
				System.out.println("查找到匹配数据，进行扣减");
				
				Counter rsltCounter = rsltCounters.get(0);
				rsltCounter.setCounterValue(rsltCounter.getCounterValue()-dCounter.getChgValue());
				
				System.out.println("累积量扣减：id - " + rsltCounter.getCounterID() + "  value - "+rsltCounter.getCounterValue());
				
				try {
					statement = (Statement) connection.createStatement();
					statement.executeQuery("SET NAMES UTF8");
							
					String str = "UPDATE " + tableName 
							+ " SET COUNTER_VALUE = ?,UPDATE_TIME = ? " 
							+ "WHERE COUNTER_ID = '" + dCounter.getCounterID() 
							+ "' AND OBJECT_ID = '" + phoneNum + "'";
					PreparedStatement stm = (PreparedStatement) connection.prepareStatement(str);
							
					stm.setDouble(1, rsltCounter.getCounterValue());
					stm.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
							
					stm.executeUpdate();
							
					connection.close();
					
					System.out.println("累积量扣减成功");
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("累积量扣减失败");
					return false;
				}
			}
		}
		
		return true;
	}

	
	public boolean addReserveBalances(ArrayList<ReserveBalance> rBalances){
		// Reserve balance print infos
		System.out.println("预留- " + rBalances.size() + "条记录");
			
		boolean isSuccess = true;
		
		for(ReserveBalance rb : rBalances){			
			ReserveBalance srb = getReserveBalances(rb.getSessionID());
			if(srb == null){
				// 出入一条新的
				isSuccess &= creatReserveBalances(rb);
			}
			else{
				// 更新
				isSuccess &= updateReserveBalance(rb);
			}
					
		}
		return isSuccess;
	}
	
	public boolean creatReserveBalances(ReserveBalance rb){
		//表名
		String tableName="BALANCE_RESERVE";
		Connection connection = getConnection();
		Statement statement;
		
		try {
			statement = (Statement) connection.createStatement();
			statement.executeQuery("SET NAMES UTF8");
			
			String str = "INSERT INTO " + tableName +" (SESSIONID, SERVICE_ID, SERVICE_TYPE, ACCT_ID, ACCT_ITEM_ID, AMT, RESERVE_DATE, EXPIRY_DATE, UPDATE_TIME) values(?,?,?,?,?,?,?,?,?)";
			PreparedStatement stm = (PreparedStatement) connection.prepareStatement(str);
								
			stm.setString(1, rb.getSessionID());
			stm.setLong(2, rb.getServiceID());
			stm.setLong(3, rb.getServiceType());
			stm.setLong(4, rb.getAccountID());
			stm.setLong(5, rb.getAccountID());
			stm.setDouble(6, rb.getReserveAmount());
			stm.setTimestamp(7, rb.getReserveDate());
			stm.setTimestamp(8, rb.getExpDate());
			stm.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
			
			stm.executeUpdate();
								
			connection.close();
						
			System.out.println(rb.toString());
			System.out.println("预留成功");
			return true;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("预留失败");
			return false;
		}
		
	}
	
	public ReserveBalance getReserveBalances(String sessionID){
		ReserveBalance rb = null;
		//表名
		String tableName="BALANCE_RESERVE";
		Connection connection = getConnection();
		Statement statement;
		
		try {
			statement = (Statement) connection.createStatement();
			String sql = "SELECT * FROM "+tableName+" WHERE SESSIONID = '"+sessionID+"'";
			System.out.println(sql);
			ResultSet rs;
			
			rs = (ResultSet)statement.executeQuery(sql);
			
			if(rs.next()){
				rb = new ReserveBalance(sessionID, 
						rs.getLong("SERVICE_ID"),
						rs.getLong("SERVICE_TYPE"),
						rs.getLong("ACCT_ID"), 
						rs.getLong("ACCT_ITEM_ID"), 
						rs.getDouble("AMT"), 
						rs.getTimestamp("RESERVE_DATE"), 
						rs.getTimestamp("EXPIRY_DATE"),
						rs.getTimestamp("UPDATE_TIME"));
			}
			
			//关闭
			rs.close(); 
			statement.close(); 
			connection.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return rb; 
	}
	
	public boolean updateReserveBalance(ReserveBalance rb){
		// Deduct Balance print infos
		System.out.println("更新预留余额");
		
		//表名
		String tableName="BALANCE_RESERVE";
		Connection connection = getConnection();
		Statement statement;
		
		try {
			statement = (Statement) connection.createStatement();
			statement.executeQuery("SET NAMES UTF8");
							
			String str = "UPDATE "+tableName
						+ " SET SERVICE_ID = ?,SERVICE_TYPE = ?,ACCT_ID = ?,ACCT_ITEM_ID = ?,AMT = ?,RESERVE_DATE = ?,EXPIRY_DATE = ?,UPDATE_TIME = ?" 
						+ "WHERE SESSIONID = '" + rb.getServiceID()+ "'";
			
			PreparedStatement stm = (PreparedStatement) connection.prepareStatement(str);
							
			stm.setLong(1, rb.getServiceID());
			stm.setLong(2, rb.getServiceType());
			stm.setLong(3,rb.getAccountID());
			stm.setLong(4,rb.getAccountItemID());
			stm.setDouble(5,rb.getReserveAmount());
			stm.setTimestamp(6,rb.getReserveDate());
			stm.setTimestamp(7,rb.getExpDate());
			stm.setTimestamp(8,new Timestamp(System.currentTimeMillis()));
			
			stm.executeUpdate();
							
			connection.close();
					
			System.out.println("预留余额更新成功");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("预留余额更新失败");
			return false;
		}
		return false;		
	}
	
	public boolean deleteReserveBalances(ArrayList<ReserveBalance> rBalances){
		//表名
		String tableName="BALANCE_RESERVE";
		Connection connection = getConnection();
		Statement statement;
		
		for(ReserveBalance rb : rBalances){	
			try {
				statement = (Statement) connection.createStatement();
				String sql="DELETE FROM "+tableName + " WHERE " 
						+ "SESSIONID = '"+ rb.getSessionID() +"'" 
						+ " AND ACCT_ID = '" + rb.getAccountID() +"'"
						+ " AND SERVICE_ID = '" + rb.getServiceID() + "'";
				System.out.println(sql);
			
				PreparedStatement stm = (PreparedStatement) connection.prepareStatement(sql);
				stm.execute();
		
				//关闭
				statement.close(); 
				connection.close();
			
				return true;
			} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
				return false;
			}
		}
		return true;
	}
}
