package net.hwyz.iov.cloud.otd.vso.service.domain.external.service;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.external.ExDictionaryServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * 外部数据字典服务
 *
 * @author hwyz_leo
 */
@FeignClient(name = "dictionary-service", path = "/service/dictionary", fallbackFactory = ExDictionaryServiceFallbackFactory.class)
public interface ExDictionaryService {

    /**
     * 根据字典代码获取数据字典
     *
     * @param code 字典代码
     * @return 数据字典
     */
    @GetMapping(value = "/{code}")
    List<Map<String, Object>> getDictionaryMap(@PathVariable("code") String code);

}
