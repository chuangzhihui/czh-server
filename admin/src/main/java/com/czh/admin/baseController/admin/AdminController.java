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
import com.czh.service.entity.AuthConfig;
import com.czh.service.entity.Setting;
import com.czh.service.vo.admin.AdminActionLogVo;
import com.czh.service.vo.admin.AdminListVo;
import com.czh.service.vo.admin.AuthMenuVo;
import com.czh.service.vo.admin.GetLoginInfoVo;
import com.czh.service.vo.admin.GetPwdRuleVo;
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
        // BE-30：删除死变量 `String redisKey="roleMenus_"+roleId`。
        // 菜单树缓存被计划过但从未实现（声明后从未读取），留着会误导后人以为菜单走了缓存，
        // 从而忽略 getAuthMenuByRoleId 的 N+1 递归查询。缓存本轮不引入，改为一次查全表+内存构树。
        // 注意：loginOut() 里那个 redisKey 是另一个局部变量且在用，不要一起删。
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
    public JSONResult<GetPwdRuleVo> getPwdRule()
    {
        return JSONResult.success(authConfigService.getPwdRule());
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
        //  注意：不能复用 adminDetails.getPassword()——请求鉴权链路注入的 AdminDetails
        //  由不含密码的鉴权缓存构造（密码哈希不落 Redis），该字段为 null。
        //  校验旧密码必须回源 DB。
        Admin current=adminService.getById(adminDetails.getAdminId());
        if(current==null || !PWDUtil.hashpasswordVerify(req.getOldPwd(),current.getPassword())){
            return JSONResult.error("密码错误!");
        }
        //设置密码
        Admin uadmin=new Admin();
        uadmin.setAdminId(adminDetails.getAdminId());
        uadmin.setPassword(PWDUtil.createHashPassword(req.getPassword()));
        uadmin.setLastChangePwdTime(DateUtil.getDaDate());
        if(adminService.updateById(uadmin)){
            //改密码后立刻吊销当前登录态（BE-06 配套）：管理员令牌不再以密码哈希做密钥，
            //"换密码即失效旧令牌"的隐式保障随之消失，改为显式删除 Redis 登录态
            //（key 规则与 AdminTokenService.ADMIN_TOKEN_PREFIX 一致，勿改其一漏其一）。
            redis.delete("adminToken_"+adminDetails.getAdminId());
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
    public JSONResult<PageInfo<AdminActionLogVo>> adminLog(@RequestBody AdminLogDto req){
        PageInfo<AdminActionLogVo> pageVo= adminActionLogService.adminLog(req);
        return JSONResult.success(pageVo);
    }
}
