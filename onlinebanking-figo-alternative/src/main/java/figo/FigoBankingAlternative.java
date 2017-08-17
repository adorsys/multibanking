package figo;

import org.adorsys.envutils.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import domain.BankApi;
import me.figo.FigoConnection;

public class FigoBankingAlternative extends FigoBanking {

    private static final Logger LOG = LoggerFactory.getLogger(FigoBankingAlternative.class);

    public FigoBankingAlternative() {
        super();
        String figoClientId = EnvProperties.getEnvOrSysProp("FIGO_ALTERNATIVE_CLIENT_ID", clientId);
        String figoSecret = EnvProperties.getEnvOrSysProp("FIGO_ALTERNATIVE_SECRET", secret);
        int figoTimeout = Integer.parseInt(EnvProperties.getEnvOrSysProp("FIGO_ALTERNATIVE_TIMEOUT", String.valueOf(timeout)));
        String figoConnectionUrl = EnvProperties.getEnvOrSysProp("FIGO_ALTERNATIVE_CONNECTION_URL", "https://api.figo.me");

        if (figoClientId == null || figoSecret == null) {
            LOG.warn("missing env properties FIGO_ALTERNATIVE_CLIENT_ID and/or FIGO_ALTERNATIVE_SECRET");
        } else {
            figoConnection = new FigoConnection(figoClientId, figoSecret, "http://nowhere.here", figoTimeout, figoConnectionUrl);
        }
    }

    @Override
    Logger getLogger() {
        return LOG;
    }

    @Override
    public BankApi bankApi() {
        return BankApi.FIGO_ALTERNATIVE;
    }
}
