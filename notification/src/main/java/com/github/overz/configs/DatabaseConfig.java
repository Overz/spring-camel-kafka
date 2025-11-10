package com.github.overz.configs;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.camel.BindToRegistry;
import org.apache.camel.Configuration;
import org.apache.camel.PropertyInject;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

	@BindToRegistry("dataSource")
	public DataSource dataSource(
		@PropertyInject("app.datasource.jdbc-url") final String url,
		@PropertyInject("app.datasource.username") final String username,
		@PropertyInject("app.datasource.password") final String password,
		@PropertyInject("app.datasource.driver") final String driver
	) {
		final var ds = new HikariDataSource();
		ds.setJdbcUrl(url);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setDriverClassName(driver);
		return ds;
	}
}
