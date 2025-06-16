package com.czh.admin.baseController.role;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.czh.admin.baseController.BaseController;
import com.czh.common.annotation.Permission;
import com.czh.common.dto.PageDto;
import com.czh.common.utils.DateUtil;
import com.czh.common.vo.EmptyVo;
import com.czh.common.vo.JSONResult;
import com.czh.service.dto.admin.AddRoleDto;
import com.czh.service.dto.admin.EditRoleDto;
import com.czh.service.entity.Admin;
import com.czh.service.entity.Role;
import com.czh.service.vo.admin.AddRoleGetMenusVo;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/role")
public class RoleController extends BaseController {
    /**
     * 角色列表
     * @param req
     * @return
     */
    @RequestMapping("/roleList")
    public JSONResult<PageInfo<Role>> roleList(@RequestBody PageDto req){
        PageInfo<Role>pageVo=roleService.getRoleList(req);
        return JSONResult.success(pageVo);
    }

    /**
     * 新增角色获取所有的菜单
     * @return
     */
    @RequestMapping("/addRoleGetMenus")
    @Permission(permissionList = {"/admin/role/addRole","/admin/role/editRole"})
    public JSONResult<List<AddRoleGetMenusVo>> addRoleGetMenus(){
        List<AddRoleGetMenusVo>vos=roleService.getAddRoleMenus();
        return JSONResult.success(vos);
    }


    /**
     * 新增角色
     * @param req
     * @return
     */
    @RequestMapping("/addRole")
    public JSONResult<EmptyVo> addRole(@RequestBody AddRoleDto req){
        Role role=new Role(
                0,req.getRoleName(),req.getIds(),req.getDescribe(), DateUtil.getDaDate()
        );
        if(roleService.save(role)){
            return  JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 编辑角色
     * @param req
     * @return
     */
    @RequestMapping("/editRole")
    public JSONResult<EmptyVo> editRole(@RequestBody EditRoleDto req){
        Role role=new Role(
                req.getRoleId(),req.getRoleName(),req.getIds(),req.getDescribe(), DateUtil.getDaDate()
        );
        if(roleService.updateById(role)){
            roleService.updateRoleCache(role.getRoleId());
            return  JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 删除角色
     * @param roleId
     * @return
     */
    @RequestMapping("/delRole/{roleId}")
    public JSONResult<EmptyVo> delRole(@PathVariable Integer roleId){
        List<Admin> admins=adminService.getBaseMapper().selectList(
                new QueryWrapper<Admin>().eq("roleId",roleId)
        );
        if(!admins.isEmpty()){
            return JSONResult.error("该角色下存在管理员，无法删除!");
        }
        if(roleService.removeById(roleId)){
            String redisKey="rolePermission"+roleId;
            redis.delete(redisKey);
            return JSONResult.success();
        }
        return JSONResult.error();
    }
}
