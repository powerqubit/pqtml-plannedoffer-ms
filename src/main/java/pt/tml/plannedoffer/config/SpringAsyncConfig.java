package pt.tml.plannedoffer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync()
@ComponentScan("pt.tml.plannedoffer")
public class SpringAsyncConfig implements AsyncConfigurer
{

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor()
    {
        return new ThreadPoolTaskExecutor();
    }

    @Override
    public Executor getAsyncExecutor()
    {
        return new SimpleAsyncTaskExecutor();
    }
}