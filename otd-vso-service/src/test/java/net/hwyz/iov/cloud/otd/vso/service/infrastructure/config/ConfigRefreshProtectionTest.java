package net.hwyz.iov.cloud.otd.vso.service.infrastructure.config;

import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 配置刷新保护测试（OTD-VSO-DSN-CR-013）
 * <p>
 * Spring Cloud 的 {@code spring.cloud.refresh.never-refreshable} 默认只包含 HikariDataSource。
 * VSO 使用 Druid，若不显式补齐名单，任何一次 Nacos 配置推送都会通过
 * {@code ConfigurationPropertiesRebinder} 对数据源执行 {@code initializeBean}（就地重新绑定）：
 * Druid 属性 setter 在池已初始化后重复执行并抛错（Nacos 记 notify-error 后每 5s 重推），
 * 绑定过程还会调用 {@code DruidDataSource#getConnection()} 取走连接且不归还 →
 * 连接泄漏 / 池枯竭 / 全部 DB 请求挂死。
 *
 * @author VSO Team
 */
@DisplayName("配置刷新保护")
class ConfigRefreshProtectionTest {

    @Test
    @DisplayName("数据源必须列入 never-refreshable，配置刷新不得销毁连接池")
    void shouldProtectDataSourceFromConfigurationPropertiesRebind() throws Exception {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader()
                .load("application", new ClassPathResource("application.yml"));
        assertThat(sources).isNotEmpty();

        Object configured = sources.get(0).getProperty("spring.cloud.refresh.never-refreshable");
        assertThat(configured)
                .as("application.yml 必须声明 spring.cloud.refresh.never-refreshable")
                .isNotNull();

        Set<String> protectedTypes = StringUtils.commaDelimitedListToSet(String.valueOf(configured));
        assertThat(protectedTypes)
                .as("Druid 数据源必须排除在 ConfigurationPropertiesRebinder 之外")
                .contains(DruidDataSourceWrapper.class.getName());
        assertThat(protectedTypes)
                .as("必须保留 Spring Cloud 默认保护的 HikariDataSource")
                .contains("com.zaxxer.hikari.HikariDataSource");
    }
}
