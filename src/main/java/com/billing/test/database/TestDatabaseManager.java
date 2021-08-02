package com.billing.test.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestDatabaseManager {
	/** * 
	 * 데이터베이스 프로퍼티 
	 * */ 
	private Properties databaseInfo = new Properties();
	
	/** 
	 * * 데이터베이스 드라이버 초기화 여부 
	 * */ 
	private boolean initDriver = false; 
	
	/** 
	 * * 데이터베이스 드라이버 초기화 여부를 가져옵니다. * 
	 * @return 데이터베이스 드라이버 초기화 여부 
	 * */ 
	public boolean isInitDriver() {
		return initDriver; 
		}
	
	/** 
	 * * 데이터베이스 매니저 프로퍼티를 로드하고 검증합니다. * 
	 * @param databaseManagerPath 데이터베이스 매니저 프로퍼티 경로 * 
	 * @return 프로퍼티 로드 여부 
	 * */ 
	private boolean loadProperties(String databaseManagerPath) { 
		boolean result = true; 
		InputStream inputStream = getClass().getResourceAsStream(databaseManagerPath); 
		if (inputStream != null) { 
			try { databaseInfo.load(inputStream); 
			System.out.println("데이터베이스 프로퍼티를 읽음"); 
			String id = databaseInfo.getProperty("id"); 
			if (id == null || id.trim().isEmpty()) { 
				System.out.println("데이터베이스 프로퍼티에 id가 없음");
				result = false; 
				} 
			String driverClassName = databaseInfo.getProperty("driverClassName"); 
			if (driverClassName == null || driverClassName.trim().isEmpty()) { 
				System.out.println("데이터베이스 프로퍼티에 driverClassName이 없음"); 
				result = false; 
				} 
			String url = databaseInfo.getProperty("url"); 
			if (url == null || url.trim().isEmpty()) { 
				System.out.println("데이터베이스 프로퍼티에 url이 없음");
				result = false; 
				} 
			String username = databaseInfo.getProperty("username"); 
			if (username == null || username.trim().isEmpty()) { 
				System.out.println("데이터베이스 프로퍼티에 username이 없음"); 
				result = false; 
				} 
			String password = databaseInfo.getProperty("password"); 
			if (password == null || password.trim().isEmpty()) { 
				System.out.println("데이터베이스 프로퍼티에 password가 없음"); 
				result = false; 
				} 
			if (!result) { 
				System.out.println("데이터베이스 프로퍼티 정보가 정확하지 않음"); 
				} 
			} catch (IOException e) { 
				System.out.println("데이터베이스 프로퍼티를 읽지 못함"); result = false; 
				e.printStackTrace(); } 
			finally { try { inputStream.close(); } catch (IOException e) { e.printStackTrace(); } } } 
		else { 
			System.out.println("데이터베이스 프로퍼티를 읽지 못함"); 
			result = false; 
			} 
		return result; 
		}
	
	/** * 
	 * 데이터베이스 드라이버를 로드합니다. 
	 * */ 
	private void initDriver() { 
		String driverClassName = databaseInfo.getProperty("driverClassName"); 
		try { Class<?> dbDriver = Class.forName(driverClassName); 
		System.out.println("데이터베이스 드라이버(" + dbDriver.toString() + ")가 로딩됨"); 
		initDriver = true; 
		} catch (ClassNotFoundException e) { 
			System.out.println("데이터베이스 드라이버가 로딩되지 않음"); 
			e.printStackTrace(); } 
		}
	/** 
	 * * 생성자 * 
	 * @param databaseManagerPath 
	 * 데이터베이스 매니저 프로퍼티 경로 
	 * */ 
	public TestDatabaseManager(String databaseManagerPath) { 
		if (loadProperties(databaseManagerPath)) { 
			initDriver(); 
			} 
		}
	/** 
	 * * 데이터베이스에 연결합니다. * 
	 * @return 데이터베이스 연결 객체 */ 
	public Connection getConnection() { 
		if (!initDriver) { 
			System.out.println("데이터베이스 드라이버가 로딩되지 않음"); 
			return null; 
			} 
		Connection connection = null; 
		String url = databaseInfo.getProperty("url"); 
		String username = databaseInfo.getProperty("username"); 
		String password = databaseInfo.getProperty("password"); 
		try { 
			connection = DriverManager.getConnection(url, username, password); 
			System.out.println("데이터베이스에 연결됨"); } 
		catch (SQLException e) { 
			System.out.println("데이터베이스에 연결하지 못함"); 
			e.printStackTrace(); } 
		return connection; 
		}
	
	/** * 
	 * 데이터베이스에 연결을 종려합니다. * 
	 * @param connection 데이터베이스 연결 객체 
	 * */ 
	public void closeConnection(Connection connection) { 
		if (connection != null) {
			try { 
				connection.close(); 
				System.out.println("데이터베이스에서 연결을 종료함"); 
				} catch (SQLException e) { 
					System.out.println("데이터베이스에서 연결을 종료하지 못함"); 
					e.printStackTrace(); 
					} 
			} 
		}
	/** * 쿼리문(SELECT)을 실행한다. * 
	 * @param query 쿼리 문자열 * 
	 * @return ResultSet 객체 */ 
	public ResultSet executeQuery(String query) { 
		ResultSet resultSet = null; 
		Connection connection = getConnection(); 
		if (connection != null) { 
			resultSet = executeQuery(connection, query); 
			closeConnection(connection); 
			} return resultSet; 
			} 
	/** * 쿼리문(SELECT)을 실행한다. * 
	 * @param connection 데이터베이스 연결 객체 *
	 *  @param query 쿼리 문자열 * 
	 *  @return ResultSet 객체 */ 
	public ResultSet executeQuery(Connection connection, String query) { 
		if (!initDriver) { 
			System.out.println("데이터베이스 드라이버가 로딩되지 않음"); 
			return null; 
			} 
		if (connection == null) { 
			System.out.println("데이터베이스가 연결되지 않음"); 
			return null; 
			} 
		if (query == null || query.trim().isEmpty()) { 
			System.out.println("쿼리가 빈 문자열임"); 
			return null; 
			} 
		ResultSet resultSet = null;
		Statement statement = null; 
		try { 
			statement = connection.createStatement(); 
			System.out.println("Statement를 생성함"); 
		} catch (SQLException e) { 
			System.out.println("Statement를 생성하지 못함"); 
			e.printStackTrace(); 
			} 
		if (statement != null) { 
			try { 
				resultSet = statement.executeQuery(query); 
				System.out.println("Query[" + query + "]를 실행함"); 
				} catch (SQLException e) { 
					System.out.println("Query[" + query + "]를 실행하지 못함"); 
					e.printStackTrace(); } 
			finally { 
				try { 
					statement.close(); 
					System.out.println("Statement를 종료함"); 
					} catch (SQLException e) { 
						System.out.println("Statement를 종료하지 못함"); 
						e.printStackTrace(); 
						} 
				} 
			} 
		return resultSet; 
		}
	/** * 쿼리문(UPDATE, INSERT, DELETE)을 실행한다. * 
	 * @param query 쿼리 문자열 * 
	 * @return 처리된 행 수(-1: 데이터베이스 연결 오류, 0: 처리된 행 없음, 1이상: 처리된 행 수) 
	 * */ 
	public int executeUpdate(String query) { 
		int result = -1;
		Connection connection = getConnection(); 
		if (connection != null) { 
			result = executeUpdate(connection, query); 
			closeConnection(connection); 
			} 
		return result; 
		} 
	/** * 쿼리문(UPDATE, INSERT, DELETE)을 실행한다. * 
	 * @param connection 데이터베이스 연결 객체 * 
	 * @param query 쿼리 문자열 * 
	 * @return 처리된 행 수(-1: 데이터베이스 연결 오류, 0: 처리된 행 없음, 1이상: 처리된 행 수) */ 
	public int executeUpdate(Connection connection, String query) { 
		if (!initDriver) { 
			System.out.println("데이터베이스 드라이버가 로딩되지 않음"); 
			return -1; 
			} 
		if (connection == null) { 
			System.out.println("데이터베이스가 연결되지 않음"); 
			return -1; 
			} 
		if (query == null || query.trim().isEmpty()) { 
			System.out.println("쿼리가 빈 문자열임"); 
			return -1; 
			} int result = -1; 
			Statement statement = null;
			try { 
				statement = connection.createStatement(); 
				System.out.println("Statement를 생성함"); 
				} catch (SQLException e) { 
					System.out.println("Statement를 생성하지 못함");
					e.printStackTrace(); 
					} 
			if (statement != null) { 
				try { 
					result = statement.executeUpdate(query); 
					System.out.println("Query[" + query + "]를 실행함"); 
					} catch (SQLException e) { 
						System.out.println("Query[" + query + "]를 실행하지 못함"); 
						e.printStackTrace(); 
						} finally { 
							try { 
								statement.close(); 
								System.out.println("Statement를 종료함"); 
								} catch (SQLException e) { 
									System.out.println("Statement를 종료하지 못함"); 
									e.printStackTrace(); 
								} 
					} 
			} 
			return result; 
	}
	/** * 데이터베이스 프로퍼티에 id를 가져옵니다. * 
	 * @return id 
	 * */ 
	public String getId() { 
		return databaseInfo.getProperty("id") == null ? "" : 
			databaseInfo.getProperty("id").trim(); 
		}

	public TestDatabaseManager() {
		// TODO Auto-generated constructor stub
	}

}
