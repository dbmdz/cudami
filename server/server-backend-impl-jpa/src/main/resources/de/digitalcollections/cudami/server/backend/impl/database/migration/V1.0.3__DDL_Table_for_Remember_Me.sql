-- see http://docs.spring.io/spring-security/site/docs/3.2.7.RELEASE/reference/htmlsingle/#remember-me-persistent-token
CREATE TABLE persistent_logins (username  VARCHAR(64) NOT NULL,
                                series    VARCHAR(64) PRIMARY KEY,
                                token     VARCHAR(64) NOT NULL,
                                last_used TIMESTAMP   NOT NULL);