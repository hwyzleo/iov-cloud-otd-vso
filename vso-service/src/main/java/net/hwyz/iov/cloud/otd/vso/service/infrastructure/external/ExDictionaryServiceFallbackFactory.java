package net.hwyz.iov.cloud.otd.vso.service.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.external.service.ExDictionaryService;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 外部数据字典服务回退处理
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
public class ExDictionaryServiceFallbackFactory implements FallbackFactory<ExDictionaryService> {

    @Override
    public ExDictionaryService create(Throwable cause) {
        return new ExDictionaryService() {

            @Override
            public List<Map<String, Object>> getDictionaryMap(String code) {
                if (logger.isDebugEnabled()) {
                    logger.warn("根据字典代码获取数据字典异常", cause);
                } else {
                    logger.warn("根据字典代码获取数据字典异常:[{}]", cause.getMessage());
                }
                return null;
            }

        };
    }

}
