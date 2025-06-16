package com.czh.admin.baseController.backup;

import com.czh.admin.baseController.BaseController;
import com.czh.admin.security.AdminDetails;
import com.czh.common.annotation.Log;
import com.czh.common.exception.ErrorException;
import com.czh.common.vo.EmptyVo;
import com.czh.common.vo.JSONResult;
import com.czh.service.dto.admin.DbBackUpListDto;
import com.czh.service.entity.DbBackup;
import com.czh.service.vo.admin.DbBackupVo;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * 数据库备份
 */
@RestController
@RequestMapping("/admin/backup")
public class DbBackUpController extends BaseController {
    /**
     * 备份数据库
     * @return
     * @throws InterruptedException
     */
    @RequestMapping("/backUpDb")
    @Log(operation = "备份数据库")
    public JSONResult<EmptyVo> backUpDb() throws InterruptedException {



        AdminDetails adminDetails = (AdminDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        dbBackupService.backup(adminDetails.getAdminId());
        return JSONResult.success();
    }

    /**
     * 恢复备份
     * @param id
     * @return
     */
    @RequestMapping("/restoreDb/{id}")
    @Log(operation = "恢复备份")
    public JSONResult<EmptyVo> restoreDb(@PathVariable Integer id){
        dbBackupService.restoreDbById(id);
        return JSONResult.success();
    }

    /**
     * 备份文件列表
     * @param dto
     * @return
     */
    @RequestMapping("/getList")
    public JSONResult<PageInfo<DbBackupVo>> getList(@RequestBody DbBackUpListDto dto)
    {
        PageInfo<DbBackupVo> pageVo=dbBackupService.getList(dto);
        return JSONResult.success(pageVo);
    }

    /**
     * 下载备份
     * @param id
     * @param response
     * @return
     * @throws FileNotFoundException
     */
    @PostMapping("/download/{id}")
    public InputStreamSource download(@PathVariable Integer id, HttpServletResponse response) throws FileNotFoundException {
        DbBackup backup= dbBackupService.getById(id);
        if(backup==null)
        {
            throw new ErrorException("文件不存在!");
        }
        File file=new File(backup.getFilePath());
        if(!file.exists())
        {
            throw new ErrorException("文件不存在!");
        }
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        response.setHeader("Content-Disposition","attachment; filename=" + backup.getFileName());
        response.setHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return resource;
    }

    /**
     * 删除备份
     * @param id
     * @return
     */
    @RequestMapping("/removeBackUpFile/{id}")
    @Log(operation = "删除备份文件")
    public JSONResult<EmptyVo> removeBackUpFile(@PathVariable Integer id)
    {
        DbBackup backup= dbBackupService.getById(id);
        if(backup==null)
        {
            return JSONResult.error("数据不存在!");
        }
        dbBackupService.removeById(id);
        File file=new File(backup.getFilePath());
        file.delete();
        return JSONResult.success();
    }
}
