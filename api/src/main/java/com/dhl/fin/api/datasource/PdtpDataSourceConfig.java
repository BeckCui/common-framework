package com.dhl.fin.api.datasource;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;


@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = {"com.dhl.fin.api.dao.fin"}, sqlSessionFactoryRef = "pdtpSqlSessionFactory")
public class PdtpDataSourceConfig {


    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.pdtp")
    @Primary
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public SqlSessionFactory pdtpSqlSessionFactory(DataSource pdtpDataSource, org.apache.ibatis.session.Configuration configuration) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(pdtpDataSource);
        factoryBean.setConfiguration(configuration);
        factoryBean.setDatabaseIdProvider(databaseIdProvider());
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/*.xml"));
        return factoryBean.getObject();
    }

    @Bean
    @Primary
    public DataSourceTransactionManager pdtpDataSourceTransactionManager(DataSource pdtpDataSource) {
        return new DataSourceTransactionManager(pdtpDataSource);
    }

    @Bean
    @Primary
    public DatabaseIdProvider databaseIdProvider() {
        DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        Properties p = new Properties();
        p.setProperty("MySQL", "mysql");
        p.setProperty("SQL Server", "sqlserver");
        databaseIdProvider.setProperties(p);
        return databaseIdProvider;
    }

    @Bean
    @Primary
    public SqlSessionTemplate pdtpSqlSessionTemplate(SqlSessionFactory pdtpSqlSessionFactory) {
        return new SqlSessionTemplate(pdtpSqlSessionFactory);
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "mybatis.configuration")
    public org.apache.ibatis.session.Configuration mybatisConfig() {
        return new org.apache.ibatis.session.Configuration();
    }


}
