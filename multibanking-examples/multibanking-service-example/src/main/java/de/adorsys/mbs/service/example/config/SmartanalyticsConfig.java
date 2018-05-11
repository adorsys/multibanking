package de.adorsys.mbs.service.example.config;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.adorsys.mbs.service.example.analytics.SimpleEmbededSmartAnalyticsFacade;
import de.adorsys.smartanalytics.api.GroupConfig;
import de.adorsys.smartanalytics.api.SmartAnalyticsFacade;

@Configuration
public class SmartanalyticsConfig {

//    @Value("${SMARTANALYTICS_URL:http://localhost:8082}")
//    private String smartanalyticsUrl;

    @Value("${SMARTANALYTICS_GROUPCONFIG_URL:classpath:/analytics/group-config.yml}")
    private URL groupConfigUrl;

    @Value("${SMARTANALYTICS_CONTRACT_BLACKLIST_URL:classpath:/analytics/contract-blacklist.yml}")
    private URL contractBlacklistUrl;

    @Bean
    public SmartAnalyticsFacade facade() {
        return new SimpleEmbededSmartAnalyticsFacade();
    }
    
    @Bean
    public GroupConfig groupConfig() throws IOException {
        final YAMLFactory ymlFactory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(ymlFactory);

        return objectMapper.readValue(groupConfigUrl, GroupConfig.class);
    }

    @Bean
    @Qualifier("contractBlacklist")
    public List<String> contractBlacklist() throws IOException {
        final YAMLFactory ymlFactory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(ymlFactory);

        return objectMapper.readValue(contractBlacklistUrl, new TypeReference<List<String>>() {
        });
    }
//
//    @Bean
//    @Qualifier("smartanalytics")
//    public RestTemplate restTemplate() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        mapper.registerModule(new Jackson2HalModule());
//        mapper.registerModule(new JavaTimeModule());
//
//        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
//        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
//        converter.setObjectMapper(mapper);
//
//        final RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
//        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(smartanalyticsUrl));
//        restTemplate.setErrorHandler(new ErrorHandler());
//        restTemplate.setMessageConverters(Collections.singletonList(converter));
//        restTemplate.getInterceptors().add(new LoggingInterceptor("SMARTANALYTICS"));
//        return restTemplate;
//    }
//
//    private class ErrorHandler extends DefaultResponseErrorHandler {
//
//        @Override
//        public void handleError(ClientHttpResponse response) throws IOException {
//            String responseBody = new String(getResponseBody(response), getCharsetOrDefault(response));
//            Message errorMessage = getErrorMessages(responseBody);
//            if (errorMessage != null) {
//                throw new SmartanalyticsException(response.getStatusCode(), errorMessage);
//            }
//
//            super.handleError(response);
//        }
//
//        private Charset getCharsetOrDefault(ClientHttpResponse response) {
//            Charset charset = getCharset(response);
//            return charset != null ? charset : StandardCharsets.UTF_8;
//        }
//
//        private Message getErrorMessages(String responseBody) {
//            try {
//                Collection<Message> messages = new ObjectMapper().readValue(responseBody, Messages.class).getMessages();
//                if (messages.size() > 0) {
//                    return messages.iterator().next();
//                }
//            } catch (IOException e) {
//            }
//            return null;
//        }
//    }

//    private class LoggingInterceptor implements ClientHttpRequestInterceptor {
//
//        private final Logger LOG = LoggerFactory.getLogger(LoggingInterceptor.class);
//        private String backend;
//
//        public LoggingInterceptor(String backend) {
//            this.backend = backend;
//        }
//
//        @Override
//        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
//
//            URI uri = request.getURI();
//
//            String query = "";
//
//            if (uri.getQuery() != null) {
//                query = "?" + uri.getQuery() + " ";
//            }
//
//            Charset charset = Logging.determineCharset(request.getHeaders().getContentType());
//            String requestString = Logging.cleanAndReduce(body, charset);
//
//            LOG.trace("{} > {} {}{} {}", backend, request.getMethod(), request.getURI().getPath(), query, requestString);
//
//            ClientHttpResponse response = execution.execute(request, body);
//
//            String responseString = Logging.cleanAndReduce(ByteStreams.toByteArray(response.getBody()), charset);
//
//            LOG.trace("{} < {} {}", backend, response.getStatusCode(), responseString);
//
//            return response;
//        }
//
//
//    }
}
