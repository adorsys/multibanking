package de.adorsys.multibanking.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import de.adorsys.multibanking.logging.WhitelistMaskEverythingLayout;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Iterator;

/**
 * WhitelistMaskEverythingLayout must be used in logback.xml
 * Otherwise there might be data logged that must not be logged e.g. password / tan
 */
@Configuration
public class MaskEverythingCheckConfig {
    @PostConstruct
    public void init() {
        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
        logCtx.getLoggerList().forEach(
            logger -> {
                for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext(); ) {
                    Appender<ILoggingEvent> appender = index.next();
                    OutputStreamAppender outputStreamAppender = (OutputStreamAppender) appender;
                    LayoutWrappingEncoder layoutWrappingEncoder = layoutWrappingEncoder = (LayoutWrappingEncoder) outputStreamAppender.getEncoder();

                    if (false == layoutWrappingEncoder.getLayout() instanceof WhitelistMaskEverythingLayout) {
                        throw new IllegalStateException("CONFIGURE logback.xml => Layout must be WhitelistMaskEverythingLayout but is "
                            + layoutWrappingEncoder.getLayout()
                            + " in " + outputStreamAppender) ;
                    }
                }
            }
        );
    }
}
