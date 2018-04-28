package de.adorsys.mbs.service.example;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Security;

import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
import org.adorsys.cryptoutils.utils.ShowProperties;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;


@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
@ComponentScan
public class MultibankingApplication {
    public static void main(String[] origargs) throws UnknownHostException {
        String[] args = ExtendedStoreConnectionFactory.readArguments(origargs);
		Security.addProvider(new BouncyCastleProvider());
		SpringApplication app = new SpringApplication(MultibankingApplication.class);
        Environment env = app.run(args).getEnvironment();
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        ShowProperties.log();
        LoggerFactory.getLogger(MultibankingApplication.class).info("\n----------------------------------------------------------\n\t" +
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
}
