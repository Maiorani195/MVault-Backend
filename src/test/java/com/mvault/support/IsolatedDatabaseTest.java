package com.mvault.support;

import org.springframework.boot.test.context.SpringBootTest;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:mvault-regression;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "api.security.token.secret=mvault-test-only-signing-key-not-for-production"
})
public @interface IsolatedDatabaseTest {}
