package net.hwyz.iov.cloud.otd.vso.service.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 定时任务配置
 *
 * @author VSO Team
 */
@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    /**
     * 配置定时任务线程池
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }

    /**
     * 创建定时任务线程池
     *
     * @return 线程池
     */
    public Executor taskScheduler() {
        return Executors.newScheduledThreadPool(5, r -> {
            Thread thread = new Thread(r);
            thread.setName("vso-scheduler-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        });
    }

}
