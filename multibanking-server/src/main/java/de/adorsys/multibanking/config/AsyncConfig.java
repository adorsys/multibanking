package de.adorsys.multibanking.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig implements AsyncConfigurer {

    @Value("${core.pool.size:2}")
    private int corePoolSize;
    @Value("${max.pool.size:50}")
    private int maxPoolSize;
    @Value("${queue.pool.size:10000}")
    private int queueCapacity;

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        log.debug("Creating Async Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("multibanking-Executor-");
        return new ExceptionHandlingAsyncTaskExecutor(executor);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    private static class ExceptionHandlingAsyncTaskExecutor implements AsyncTaskExecutor,
        InitializingBean, DisposableBean {

        private final Logger log = LoggerFactory.getLogger(ExceptionHandlingAsyncTaskExecutor.class);

        private final AsyncTaskExecutor executor;

        public ExceptionHandlingAsyncTaskExecutor(AsyncTaskExecutor executor) {
            this.executor = executor;
        }

        @Override
        public void execute(Runnable task) {
            executor.execute(createWrappedRunnable(task));
        }

        @Override
        public void execute(Runnable task, long startTimeout) {
            executor.execute(createWrappedRunnable(task), startTimeout);
        }

        private <T> Callable<T> createCallable(final Callable<T> task) {
            return () -> {
                try {
                    return task.call();
                } catch (Exception e) {
                    handle(e);
                    throw e;
                }
            };
        }

        private Runnable createWrappedRunnable(final Runnable task) {
            return () -> {
                try {
                    task.run();
                } catch (Exception e) {
                    handle(e);
                }
            };
        }

        protected void handle(Exception e) {
            log.error("Caught async exception", e);
        }

        @Override
        public Future<?> submit(Runnable task) {
            return executor.submit(createWrappedRunnable(task));
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return executor.submit(createCallable(task));
        }

        @Override
        public void destroy() throws Exception {
            if (executor instanceof DisposableBean) {
                DisposableBean bean = (DisposableBean) executor;
                bean.destroy();
            }
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            if (executor instanceof InitializingBean) {
                InitializingBean bean = (InitializingBean) executor;
                bean.afterPropertiesSet();
            }
        }
    }
}
