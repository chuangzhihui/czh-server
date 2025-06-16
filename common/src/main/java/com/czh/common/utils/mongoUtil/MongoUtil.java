package com.czh.common.utils.mongoUtil;

import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public interface MongoUtil {
    <T> PageInfo<T> queryPage(List<Criteria> criterias, PageRequest pageRequest, Class<T> type);
    void removeData(List<Criteria> criterias,String collectionName);
}
