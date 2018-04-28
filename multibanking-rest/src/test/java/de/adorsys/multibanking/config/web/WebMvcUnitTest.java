package de.adorsys.multibanking.config.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.annotation.AliasFor;

/**
 * Extension of  @WebMvcTest for excluding security filtering.
 * 
 * @author bwa
 * @author fpo 2018-03-31 12:43
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@WebMvcTest // Web application mock
@AutoConfigureMockMvc(secure = false) 
// Disable autoconfiguration of inmemory database.
public @interface WebMvcUnitTest {

    /**
     * Specifies the controllers to test. May be left blank if all
     * {@code @Controller} beans should be added to the application context.
     * 
     * @see WebMvcTest#value()
     * @return the controllers to test
     */
    @AliasFor(annotation = WebMvcTest.class, value = "controllers")
    Class<?>[] controllers() default {};
}
