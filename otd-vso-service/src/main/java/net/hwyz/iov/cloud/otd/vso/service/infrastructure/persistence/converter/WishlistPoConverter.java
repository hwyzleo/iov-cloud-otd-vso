package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.WishlistPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 心愿单数据对象转换类
 *
 * @author VSO Team
 */
@Mapper
public interface WishlistPoConverter {

    WishlistPoConverter INSTANCE = Mappers.getMapper(WishlistPoConverter.class);

    @Mapping(target = "id", source = "wishlistId")
    Wishlist toDomain(WishlistPo po);

    @Mapping(target = "wishlistId", source = "id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "modifyBy", ignore = true)
    @Mapping(target = "description", ignore = true)
    WishlistPo toPo(Wishlist domain);

    List<Wishlist> toDomainList(List<WishlistPo> poList);

    default List<String> mapStringToList(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    default String mapListToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

}