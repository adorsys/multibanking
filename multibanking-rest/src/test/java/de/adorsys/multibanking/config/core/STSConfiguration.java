package de.adorsys.multibanking.config.core;

import org.springframework.context.annotation.Configuration;

import de.adorsys.sts.decryption.EnableDecryption;
import de.adorsys.sts.keyrotation.EnableKeyRotation;
import de.adorsys.sts.pop.EnablePOP;
import de.adorsys.sts.token.authentication.EnableTokenAuthentication;

@Configuration
@EnableTokenAuthentication
@EnablePOP
@EnableDecryption
@EnableKeyRotation
public class STSConfiguration {
}
