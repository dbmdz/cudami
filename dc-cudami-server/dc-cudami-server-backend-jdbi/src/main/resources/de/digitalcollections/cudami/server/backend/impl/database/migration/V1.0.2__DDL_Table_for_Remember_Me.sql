-- see http://docs.spring.io/spring-security/site/docs/3.2.7.RELEASE/reference/htmlsingle/#remember-me-persistent-token
CREATE TABLE persistent_logins (username  VARCHAR NOT NULL,
                                series    VARCHAR PRIMARY KEY,
                                token     VARCHAR NOT NULL,
                                last_used TIMESTAMP   NOT NULL);