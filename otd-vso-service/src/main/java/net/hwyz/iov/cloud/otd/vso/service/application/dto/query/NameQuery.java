package net.hwyz.iov.cloud.otd.vso.service.application.dto.query;

import lombok.Builder;
import lombok.Data;

/**
 * 名称查询参数
 *
 * @author VSO Team
 */
@Data
@Builder
public class NameQuery {

    private String name;

}
