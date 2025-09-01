package com.czh.admin.baseController.admin;

import com.czh.admin.baseController.BaseController;
import com.czh.admin.security.AdminDetails;
import com.czh.common.annotation.Log;
import com.czh.common.annotation.Permission;
import com.czh.common.utils.DateUtil;
import com.czh.common.utils.PWDUtil;
import com.czh.common.utils.SecurityUtil;
import com.czh.common.utils.ValidateUtil;
import com.czh.common.vo.EmptyVo;
import com.czh.common.vo.JSONResult;
import com.czh.service.dto.admin.*;
import com.czh.service.entity.Admin;
import com.czh.service.entity.AdminActionLog;
import com.czh.service.entity.AuthConfig;
import com.czh.service.entity.Setting;
import com.czh.service.vo.admin.AdminListVo;
import com.czh.service.vo.admin.AuthMenuVo;
import com.czh.service.vo.admin.GetLoginInfoVo;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin/admin")
public class AdminController extends BaseController {


    /**
     * 退出登录
     * @return
     */
    @PostMapping("/loginOut")
    @Permission(required = false)
    public JSONResult<EmptyVo> loginOut()
    {
        AdminDetails adminDetails= SecurityUtil.getLoginInfo(AdminDetails.class);
        String redisKey="adminToken_"+adminDetails.getAdminId();
        redis.delete(redisKey);
        return JSONResult.success();
    }

    /**
     * 获取登录信息
     * @return
     */
    @PostMapping("/getLoginInfo")
    @Permission(required = false)
    public JSONResult<GetLoginInfoVo>getLoginInfo()
    {
        AdminDetails adminDetails= SecurityUtil.getLoginInfo(AdminDetails.class);
        GetLoginInfoVo vo=new GetLoginInfoVo();
        String redisKey="roleMenus_"+adminDetails.getRoleId();
        //获取当前角色的菜单权限
        List<AuthMenuVo> authMenuVos=menuService.getAuthMenuByRoleId(adminDetails.getRoleId());
        vo.setMenus(authMenuVos);
        Setting setting= settingService.getById(1);//系统名称
        vo.setAvatar(adminDetails.getAvatar()==null?"":adminDetails.getAvatar());
        vo.setName(setting.getValue());
        vo.setUsername(adminDetails.getUsername());
        //查询密码修改
        vo.setChangePwdType(0);
        AuthConfig authConfig= authConfigService.getById(1);
        //判断多少天没修改过密码了
        Integer day;
        if(adminDetails.getLastChangePwdTime()==null)
        {
            day=99999;
        }else{
            day= DateUtil.dateDifference(adminDetails.getLastChangePwdTime(), DateUtil.getDaDate());
        }

        if(day>=authConfig.getPassWran() && day<authConfig.getPassMax())
        {
            //达到警告但是没有强制
            vo.setChangePwdType(1);
            vo.setChangePwdTip("您的密码已有"+day+"天未修改了，请注意密码安全!");
        }else if(day>=authConfig.getPassMax())
        {
            vo.setChangePwdType(2);
            vo.setChangePwdTip("您的密码已有"+day+"天未修改了,请立即修改密码!");
        }
        return JSONResult.success(vo);
    }

    /**
     * 获取密码规则
     * @return
     */
    @PostMapping("/getPwdRule")
    @Permission(required = false)
    public JSONResult<AuthConfig> getPwdRule()
    {
        AuthConfig authConfig=authConfigService.getById(1);
        return JSONResult.success(authConfig);
    }
    /**
     * 修改密码
     * @param req
     * @return
     */
    @PostMapping("/editPwd")
//    @Log(operation = "修改密码")
    @Permission(required = false)
    public JSONResult<EmptyVo> editPwd(@RequestBody EditPwdDto req){
        AdminDetails adminDetails= SecurityUtil.getLoginInfo(AdminDetails.class);
        //校验密码是否正确
        if(!PWDUtil.hashpasswordVerify(req.getOldPwd(),adminDetails.getPassword())){
            return JSONResult.error("密码错误!");
        }
        //设置密码
        Admin uadmin=new Admin();
        uadmin.setAdminId(adminDetails.getAdminId());
        uadmin.setPassword(PWDUtil.createHashPassword(req.getPassword()));
        uadmin.setLastChangePwdTime(DateUtil.getDaDate());
        if(adminService.updateById(uadmin)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 修改头像和昵称
     * @param req
     * @return
     */
    @RequestMapping("/editAvatar")
    @Permission(required = false)
    public JSONResult<EmptyVo> editAvatar(@RequestBody EditAvatar req){
        if(req.getAvatar().equals("") && req.getUsername().equals("")){
            return JSONResult.error("用户名和头像不能同时为空");
        }
       AdminDetails adminDetails= SecurityUtil.getLoginInfo(AdminDetails.class);
        Admin update=new Admin();
        update.setAdminId(adminDetails.getAdminId());
        update.setAvatar(req.getAvatar());
        update.setUserName(req.getUsername());
        if(adminService.updateById(update)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 添加管理员
     * @param req
     * @param result
     * @return
     */
    @RequestMapping("/addAdmin")
    @Log(operation = "添加管理员")
    public JSONResult<EmptyVo> addAdmin(@RequestBody @Valid AddAdminDto req, BindingResult result){
        ValidateUtil.checkError(result);
        req.setPassword(PWDUtil.createHashPassword(req.getPassword()));
        Admin admin=new Admin();
        BeanUtils.copyProperties(req,admin);
        if(adminService.save(admin)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 修改管理员
     * @param req
     * @param result
     * @return
     */
    @RequestMapping("/editAdmin")
    @Log(operation = "修改管理员")
    public JSONResult<EmptyVo> editAdmin(@RequestBody @Valid EditAdminDto req, BindingResult result){
        ValidateUtil.checkError(result);
        Admin uadmin=new Admin();
        uadmin.setAdminId(req.getAdminId());
        if(req.getPassword()!=null && !req.getPassword().equals("")){
            uadmin.setPassword(PWDUtil.createHashPassword(req.getPassword()));
            uadmin.setLastChangePwdTime(DateUtil.getDaDate());
        }
        uadmin.setUserName(req.getUserName());
        uadmin.setRoleId(req.getRoleId());
        if(adminService.updateById(uadmin)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 删除管理员
     * @param adminId
     * @return
     */
    @RequestMapping("/delAdmin/{adminId}")
    @Log(operation = "删除管理员")
    public JSONResult<EmptyVo> delAdmin(@PathVariable Integer adminId){

        if(adminService.removeById(adminId)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 管理员列表
     * @param req
     * @return
     */
    @RequestMapping("/adminList")
    public JSONResult<PageInfo<AdminListVo>> adminList(@RequestBody AdminListDto req){
        PageInfo<AdminListVo> pageVo=adminService.getAdminList(req);
        return JSONResult.success(pageVo);
    }

    /**
     * 冻结解冻管理员
     * @param adminId
     * @return
     */
    @RequestMapping("/changeAdminStatus/{adminId}")
    @Log(operation = "冻结解冻管理员")
    public JSONResult<EmptyVo> changeAdminStatus(@PathVariable Integer adminId){
        Admin admin=adminService.getById(adminId);
        admin.setStatus(admin.getStatus().equals(0)?1:0);
        adminService.updateById(admin);
        return JSONResult.success();
    }

    /**
     * 管理员操作日志
     * @param req
     * @return
     */
    @RequestMapping("/adminLog")
    public JSONResult<PageInfo<AdminActionLog>> adminLog(@RequestBody AdminLogDto req){
        PageInfo<AdminActionLog> pageVo= adminActionLogService.adminLog(req);
        return JSONResult.success(pageVo);
    }
}
