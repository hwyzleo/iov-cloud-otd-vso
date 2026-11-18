package net.hwyz.iov.cloud.otd.vso.service.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration;
import org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 配置刷新行为验证（OTD-VSO-DSN-CR-013）
 * <p>
 * 复现事故链路：Nacos 推送 → {@code ConfigurationPropertiesRebinder#rebind()} 对数据源 Bean
 * 执行 {@code initializeBean(...)}（再次 {@code afterPropertiesSet} + 重新绑定属性），
 * Druid 连接池被"就地重绑定"：属性 setter 在池已初始化后重新执行并抛错（Nacos 记录 notify-error
 * 后每 5s 重推），绑定过程中还会调用 {@code DruidDataSource#getConnection()} 取走连接且不归还，
 * 三次推送即把 max-active 个连接全部泄漏，此后所有 DB 请求挂死。
 * <p>
 * 断言：列入 {@code spring.cloud.refresh.never-refreshable} 的数据源不会被刷新流程重新初始化/绑定。
 *
 * @author VSO Team
 */
@DisplayName("配置刷新行为")
class DataSourceRebindProtectionTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class,
                    ConfigurationPropertiesRebinderAutoConfiguration.class))
            .withUserConfiguration(FakeDataSourceConfiguration.class);

    @Test
    @DisplayName("数据源列入 never-refreshable 后，配置刷新不会重新绑定连接池")
    void shouldNotRebindDataSourceWhenNeverRefreshable() {
        runner.withPropertyValues("spring.cloud.refresh.never-refreshable=" + FakeDataSource.class.getName())
                .run(context -> {
                    FakeDataSource dataSource = context.getBean(FakeDataSource.class);
                    ConfigurationPropertiesRebinder rebinder =
                            context.getBean(ConfigurationPropertiesRebinder.class);

                    assertThat(rebinder.getNeverRefreshable()).contains(FakeDataSource.class.getName());
                    assertThat(dataSource.initializeCount).isEqualTo(1);

                    rebinder.rebind();

                    assertThat(dataSource.initializeCount)
                            .as("列入 never-refreshable 的数据源不能被刷新流程重新初始化/绑定")
                            .isEqualTo(1);
                    assertThat(context).hasNotFailed();
                });
    }

    @Test
    @DisplayName("未列入 never-refreshable 时，配置刷新会就地重新绑定连接池（事故复现）")
    void shouldRebindDataSourceWhenNotProtected() {
        runner.withPropertyValues("spring.cloud.refresh.never-refreshable=com.zaxxer.hikari.HikariDataSource")
                .run(context -> {
                    FakeDataSource dataSource = context.getBean(FakeDataSource.class);
                    ConfigurationPropertiesRebinder rebinder =
                            context.getBean(ConfigurationPropertiesRebinder.class);

                    assertThat(rebinder.getBeanNames()).contains("fakeDataSource");
                    assertThat(dataSource.initializeCount).isEqualTo(1);

                    rebinder.rebind();

                    assertThat(dataSource.initializeCount)
                            .as("未保护的数据源会被刷新流程就地重新初始化/绑定（Druid 场景即连接泄漏）")
                            .isEqualTo(2);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class FakeDataSourceConfiguration {

        @Bean
        @ConfigurationProperties(prefix = "app.fake-datasource")
        FakeDataSource fakeDataSource() {
            return new FakeDataSource();
        }
    }

    /**
     * 用 afterPropertiesSet 计数模拟 DruidDataSourceWrapper 在刷新时被重新初始化/绑定
     */
    static class FakeDataSource implements InitializingBean {

        private int initializeCount;

        @Override
        public void afterPropertiesSet() {
            this.initializeCount++;
        }
    }
}
