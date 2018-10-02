package de.adorsys.mbs.service.example.config;

import java.util.Arrays;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import de.adorsys.multibanking.auth.BearerTokenFactory;
import org.adorsys.docusafe.business.DocumentSafeService;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.multibanking.auth.SystemContext;
import de.adorsys.multibanking.auth.UserContext;
import de.adorsys.multibanking.service.base.StorageUserService;
import de.adorsys.multibanking.service.base.SystemObjectService;
import de.adorsys.multibanking.service.base.UserObjectService;
import de.adorsys.multibanking.service.crypto.SecretClaimDecryptionService;
import de.adorsys.sts.filter.JWTAuthenticationFilter;
import de.adorsys.sts.token.authentication.TokenAuthenticationService;
import de.adorsys.sts.tokenauth.BearerToken;
import de.adorsys.sts.tokenauth.BearerTokenValidator;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Autowired
    private TokenAuthenticationService tokenAuthenticationService;

    @Autowired
    private SecretClaimDecryptionService secretClaimDecryptionService;
    
    @Autowired
    private StorageUserService storageUserService;
    @Autowired
    private DocumentSafeService documentSafeService;
    
    @Autowired
	private ObjectMapper objectMapper;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .antMatchers(HttpMethod.GET, "/actuator/info").permitAll()
                .antMatchers("/actuator/**").hasAuthority("admin")
                .antMatchers("/").permitAll()
                .antMatchers("/pop").permitAll()
//                .antMatchers("/api-docs/**").permitAll()
                .antMatchers("/swagger-ui.html**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/v2/api-docs/**").permitAll()
//                .antMatchers(HttpMethod.GET, "/api/v1/pop/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/bank/**").permitAll()
                .antMatchers("/api/v1/image/**").permitAll()
                .antMatchers("/api/v1/**").authenticated()
                .anyRequest().denyAll()
                .and().cors();
       http
                .addFilterBefore(new JWTAuthenticationFilter(tokenAuthenticationService), BasicAuthenticationFilter.class);
    }

    @Autowired
    private BearerTokenValidator bearerTokenValidator;

    /**
     * The user context object is used to hold everything associated with the current user request.
     *
     * It is a sort of first level cache.
     *
     * @param request
     * @return
     */
    @Bean
    @Primary
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public UserContext getUserContext(HttpServletRequest request){
        LOGGER.debug("************************************** Enter getUserContext");
    	UserContext userContext = new UserContext();

    	String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String userSecret = secretClaimDecryptionService.decryptSecretClaim();
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(userId), new ReadKeyPassword(userSecret));
        userContext.setAuth(userIDAuth);

        String token = BearerTokenFactory.extractToken(request);
        BearerToken bearerToken = bearerTokenValidator.extract(token);
        userContext.setBearerToken(bearerToken);
        if(StringUtils.isNotBlank(userSecret)){
	        if(!storageUserService.userExists(userContext.getAuth().getUserID())){
	        	storageUserService.createUser(userContext.getAuth());
	        }
        }

        LOGGER.debug("userContext ist " + userContext.getAuth().getUserID().getValue());
        LOGGER.debug("************************************** Exit getUserContext");

        return userContext;
    }
    
    @Bean
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    UserObjectService userObjectService(UserContext userContext){
    	return new UserObjectService(objectMapper, userContext, documentSafeService);
    }

    @Bean
    SystemObjectService systemObjectService(SystemContext systemContext){
    	return new SystemObjectService(objectMapper, systemContext,documentSafeService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
        configuration.setAllowCredentials(true);
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setExposedHeaders(Collections.singletonList("Location"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
