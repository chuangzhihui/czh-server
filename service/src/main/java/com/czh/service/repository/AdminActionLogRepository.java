package com.czh.service.repository;

import com.czh.service.entity.AdminActionLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminActionLogRepository extends MongoRepository<AdminActionLog, String> {
}
