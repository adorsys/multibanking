package de.adorsys.multibanking;

import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.cryptoutils.utils.ShowProperties;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Security;


@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
@EnableAsync
@ComponentScan
// @PropertySource("classpath:application.properties")
public class Application {

    public static void main(String[] origargs) throws UnknownHostException {
        String[] args = ExtendedStoreConnectionFactory.readArguments(origargs);

		// turnOffEncPolicy();
		Security.addProvider(new BouncyCastleProvider());

		SpringApplication app = new SpringApplication(Application.class);
        Environment env = app.run(args).getEnvironment();
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        ShowProperties.log();
        LoggerFactory.getLogger(Application.class).info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\t{}://localhost:{}\n\t" +
                        "External: \t{}://{}:{}\n\t" +
                        "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                env.getProperty("server.port"),
                protocol,
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getActiveProfiles());
    }

	public static void turnOffEncPolicy() {
		// Warning: do not do this for productive code. Download and install the
		// jce unlimited strength policy file
		// see
		// http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
		try {
			Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
			field.setAccessible(true);
			field.set(null, java.lang.Boolean.FALSE);
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException
				| IllegalAccessException ex) {
			ex.printStackTrace(System.err);
		}
	}
}
