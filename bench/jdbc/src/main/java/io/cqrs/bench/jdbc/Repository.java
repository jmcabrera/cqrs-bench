package io.cqrs.bench.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

public abstract class Repository {

	static BasicDataSource	DS;

	static {
		try {
			Repository.class.getClassLoader().loadClass("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		Properties props = new Properties();
		props.put("url", "jdbc:h2:mem:");
		props.put("username", "root");
		props.put("password", "root");

		try {
			DS = BasicDataSourceFactory.createDataSource(props);
			DS.setDefaultAutoCommit(false);
			DS.setPoolPreparedStatements(false);
			DS.setMaxTotal(10);
			DS.setTestOnBorrow(true);
			DS.setValidationQuery("SELECT 1");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection() throws SQLException {
		return DS.getConnection();
	}

}
