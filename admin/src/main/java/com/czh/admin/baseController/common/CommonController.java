package com.czh.admin.baseController.common;

import com.czh.admin.baseController.BaseController;
import com.czh.common.annotation.Permission;
import com.czh.common.vo.JSONResult;
import com.czh.common.vo.SelectVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/common")
public class CommonController extends BaseController {
    //获取角色下拉列表
    @PostMapping("/getRoleSelectList")
    @Permission(required = false)
    public JSONResult<List<SelectVo>>getRoleSelectList()
    {
        List<SelectVo> vos=roleService.getRoleSelectList();
        return JSONResult.success(vos);
    }
    //获取管理员下拉
    @PostMapping("/getAdminSelectList")
    @Permission(required = false)
    public JSONResult<List<SelectVo>>getAdminSelectList()
    {
        List<SelectVo> vos=adminService.getAdminSelectList();
        return JSONResult.success(vos);
    }
}
