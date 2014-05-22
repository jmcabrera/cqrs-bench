package authorization.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

public abstract class Repository {

	static BasicDataSource	DS;

	static {
		Properties props = new Properties();
		props.put("url", "jdbc:mysql://localhost/techforum");
		props.put("username", "root");
		props.put("password", "root");

		try {
			DS = BasicDataSourceFactory.createDataSource(props);
			DS.setDefaultAutoCommit(false);
			DS.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			DS.setPoolPreparedStatements(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws SQLException {
		ResultSet rs = getConnection().createStatement().executeQuery("select 1 from dual");
		while (rs.next()) {
			System.out.println(rs.getInt(1));
		}

	}

	public static Connection getConnection() throws SQLException {
		return DS.getConnection();
	}

}
