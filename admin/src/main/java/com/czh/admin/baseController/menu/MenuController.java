package com.czh.admin.baseController.menu;

import com.czh.admin.baseController.BaseController;
import com.czh.common.annotation.Permission;
import com.czh.common.dto.PageDto;
import com.czh.common.vo.EmptyVo;
import com.czh.common.vo.JSONResult;
import com.czh.service.dto.admin.AddMenuDto;
import com.czh.service.dto.admin.EditMenuDto;
import com.czh.service.entity.Menu;
import com.czh.service.vo.admin.GetMenuListVo;
import com.czh.service.vo.admin.GetMenusByPidVo;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单管理
 */
@RestController
@RequestMapping("/admin/menu")
public class MenuController extends BaseController {
    /**
     * 添加菜单
     * @param req
     * @return
     */
    @RequestMapping("/addMenu")
    public JSONResult<EmptyVo> addMenu(@RequestBody AddMenuDto req){
        Menu menu=new Menu();
        BeanUtils.copyProperties(req,menu);
        //插入菜单记录
        if(menuService.save(menu)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 根据PID获取菜单列表
     * @param pid
     * @return
     */
    @RequestMapping("/getMenusByPid/{pid}")
    @Permission(permissionList = {"/admin/menu/addMenu","/admin/menu/editMenu"})
    public JSONResult<List<GetMenusByPidVo>> getMenusByPid(@PathVariable Integer pid){
        List<GetMenusByPidVo> vos=menuService.getMenusByPid(pid);
        return JSONResult.success(vos);
    }

    /**
     * 编辑菜单
     * @param req
     * @return
     */
    @RequestMapping("/editMenu")
    public JSONResult<EmptyVo> editMenu(@RequestBody EditMenuDto req){
        menuService.editMenu(req);
        return JSONResult.success();
    }

    /**
     * 删除菜单
     * @param id
     * @return
     */
    @RequestMapping("/delMenu/{id}")
    public JSONResult<EmptyVo> delMenu(@PathVariable Integer id)
    {
        List<GetMenusByPidVo> vos=menuService.getMenusByPid(id);
        if(!vos.isEmpty()){
            return JSONResult.error("菜单下存在子菜单无法删除!");
        }
        menuService.removeById(id);
        return JSONResult.success();
    }

    /**
     * 菜单列表
     * @param req
     * @return
     */
    @RequestMapping("/menuList")
    public JSONResult<PageInfo<GetMenuListVo>> menuList(@RequestBody PageDto req){
        PageInfo<GetMenuListVo>pageVo=menuService.menuList(req);
        return JSONResult.success(pageVo);
    }
}
