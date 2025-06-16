package com.czh.common.utils.mongoUtil;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoUtilImpl implements MongoUtil{
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public <T> PageInfo<T> queryPage(List<Criteria> criterias, PageRequest pageRequest, Class<T> type) {
        Query query = new Query();
        for (Criteria criteria : criterias) {
            query.addCriteria(criteria);
        }
//        long count=mongoTemplate.count(query,type);
        PageInfo<T> pageInfo=new PageInfo<>(mongoTemplate.find(query.with(pageRequest),type));
        return pageInfo;
    }

    @Override
    public void removeData(List<Criteria> criterias,String collectionName) {
        Query query = new Query();
        for (Criteria criteria : criterias) {
            query.addCriteria(criteria);
        }
        mongoTemplate.remove(query,collectionName);
    }


}
