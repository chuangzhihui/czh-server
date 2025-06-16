package com.czh.service.service;

import com.czh.service.entity.AdminActionLog;
import com.czh.service.entity.UploadFiles;
import com.czh.service.entity.UploadSet;

public interface AsyncService {

    void ceshiAsync();

    void addAdminLog(AdminActionLog adminActionLog);

    void removeFile(UploadFiles file, UploadSet byId);
}
