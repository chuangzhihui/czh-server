package com.czh.service.service.impl;

import com.czh.common.utils.mongoUtil.MongoUtil;
import com.czh.service.dto.admin.AdminLogDto;
import com.czh.service.entity.AdminActionLog;
import com.czh.service.repository.AdminActionLogRepository;
import com.czh.service.service.AdminActionLogService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AdminActionLogServiceImpl implements AdminActionLogService {
    @Autowired
    AdminActionLogRepository adminActionLogRepository;
    @Autowired
    MongoUtil mongoUtil;
    @Override
    public void saveAdminActionLog(AdminActionLog adminActionLog) {
        adminActionLogRepository.save(adminActionLog);
    }

    @Override
    public PageInfo<AdminActionLog> adminLog(AdminLogDto req) {
        PageRequest pageRequest = PageRequest.of(req.getPage()-1, req.getSize(), Sort.by(Sort.Direction.DESC,"createTime"));
        ArrayList<Criteria> criteria = new ArrayList<>();
        if (req.getAdminId() != null){
            criteria.add(Criteria.where("userID").is(req.getAdminId()));
        }
        if (req.getIp() != null && !req.getIp().isEmpty()) {
            criteria.add(Criteria.where("ip").regex(".*?\\"+req.getIp()+".*"));
        }
        if (req.getAddress() != null && !req.getAddress().isEmpty()) {
            criteria.add(Criteria.where("method").regex(".*?\\"+req.getAddress()+".*"));
        }
        if (req.getDesc() != null && !req.getDesc().isEmpty()) {
            criteria.add(Criteria.where("operation").regex(".*?\\"+req.getDesc()+".*"));
        }
        return mongoUtil.queryPage(criteria, pageRequest, AdminActionLog.class);
    }
}
