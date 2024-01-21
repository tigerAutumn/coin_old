package com.qkwl.service.score.config;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


@Component("flexibleTransMgr")
public class DrdsFlexibleTransaction extends DataSourceTransactionManager {
	
	public DrdsFlexibleTransaction(DataSource dataSource) {
        super(dataSource);
    }
	
    @Override
    protected void prepareTransactionalConnection(Connection con, TransactionDefinition definition) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate("SET drds_transaction_policy = 'FLEXIBLE'");
        }
    }
}
