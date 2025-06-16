package com.czh.service.vo.admin;

import com.czh.service.entity.DbBackup;
import lombok.Data;

@Data
public class DbBackupVo extends DbBackup {
    private String adminName;
}
