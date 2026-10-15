package com.czh.admin.baseController;

import com.czh.common.utils.RedisUtil;
import com.czh.service.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

public class BaseController {
    @Autowired
    protected RedisUtil redis;
    @Autowired
    @Lazy
    protected AdminService adminService;
    @Autowired
    @Lazy
    protected AuthConfigService authConfigService;
    @Autowired
    @Lazy
    protected DbBackupService dbBackupService;

    @Autowired
    @Lazy
    protected MenuService menuService;

    @Autowired
    @Lazy
    protected RoleService roleService;

    @Autowired
    @Lazy
    protected SettingService settingService;

    @Autowired
    @Lazy
    protected UploadFilesService uploadFilesService;

    @Autowired
    @Lazy
    protected UploadSetService uploadSetService;

    @Autowired
    protected AdminActionLogService adminActionLogService;


}
