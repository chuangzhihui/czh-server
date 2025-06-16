package com.czh.admin.baseController.setting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.czh.admin.AdminApplication;
import com.czh.admin.baseController.BaseController;
import com.czh.common.annotation.Permission;
import com.czh.common.dto.PageDto;
import com.czh.common.utils.DateUtil;
import com.czh.common.utils.ValidateUtil;
import com.czh.common.utils.aliOssUtil.entity.AliOssEntity;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadConfig;
import com.czh.common.utils.qiniuUtil.entity.QiniuEntity;
import com.czh.common.utils.tosUtil.TosUtil;
import com.czh.common.utils.tosUtil.entity.GetTosSignUrlDto;
import com.czh.common.utils.tosUtil.entity.TosEntity;
import com.czh.common.utils.txCosUtil.entity.TxCosEntity;
import com.czh.common.vo.EmptyVo;
import com.czh.common.vo.JSONResult;
import com.czh.service.dto.admin.*;
import com.czh.service.entity.AuthConfig;
import com.czh.service.entity.Setting;
import com.czh.service.entity.UploadFiles;
import com.czh.service.entity.UploadSet;
import com.czh.service.vo.admin.GetFileListVo;
import com.czh.service.vo.admin.GetSettingListVo;
import com.czh.service.vo.admin.GetUploadConfigVo;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统设置类
 */
@RestController
@RequestMapping("/admin/setting")
public class SettingController extends BaseController {
    /**
     * 获取上传配置信息
     * @return
     */
    @RequestMapping("/getUploadConfig")
    public JSONResult<GetUploadConfigVo> getUploadConfig(){
        //获取配置信息
        UploadSet set=uploadSetService.getBaseMapper().selectById(1);
        GetUploadConfigVo vo=new GetUploadConfigVo();
        vo.setVisible(set.getVisible());
        QiniuEntity qiniuEntity= JSON.toJavaObject(JSONObject.parseObject(set.getQiniu()),QiniuEntity.class);
        vo.setQiniu(qiniuEntity);
        AliOssEntity aliOssEntity=JSON.toJavaObject(JSONObject.parseObject(set.getAlioss()),AliOssEntity.class);
        vo.setAlioss(aliOssEntity);
        TxCosEntity txCosEntity=JSON.toJavaObject(JSONObject.parseObject(set.getTxcos()),TxCosEntity.class);
        vo.setTxcos(txCosEntity);
        TosEntity tosEntity=JSON.toJavaObject(JSONObject.parseObject(set.getTos()),TosEntity.class);
        vo.setTos(tosEntity);
        LocalUploadConfig  localUploadConfig=JSON.toJavaObject(JSONObject.parseObject(set.getLocal()),LocalUploadConfig.class);
        vo.setLocal(localUploadConfig);
        return JSONResult.success(vo);
    }


    /**
     * 保存七牛配置
     * @param req
     * @return
     */
    @RequestMapping("/saveQiniu")
    @Permission(permissionList = {"/admin/setting/getUploadConfig"})
    public JSONResult<EmptyVo> saveQiniu(@RequestBody QiniuDto req){
        QiniuEntity qiniuEntity=(QiniuEntity)req;
        String qiniu=JSON.toJSONString(qiniuEntity);
        UploadSet uploadSet=new UploadSet();
        uploadSet.setId(1);
        uploadSet.setVisible(req.getVisible());
        uploadSet.setQiniu(qiniu);
        if(uploadSetService.updateById(uploadSet)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 保存阿里oss
     * @param req
     * @return
     */
    @RequestMapping("/saveAlioss")
    @Permission(permissionList = {"/admin/setting/getUploadConfig"})
    public JSONResult<EmptyVo> saveAlioss(@RequestBody AliOssDto req){
        AliOssEntity aliOssEntity=(AliOssEntity) req;
        String alioss=JSON.toJSONString(aliOssEntity);
        UploadSet uploadSet=new UploadSet();
        uploadSet.setId(1);
        uploadSet.setVisible(req.getVisible());
        uploadSet.setAlioss(alioss);
        if(uploadSetService.updateById(uploadSet)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 保存腾讯cos
     * @param req
     * @return
     */
    @RequestMapping("/saveTxcos")
    @Permission(permissionList = {"/admin/setting/getUploadConfig"})
    public JSONResult<EmptyVo> saveTxcos(@RequestBody TxCosDto req){
        TxCosEntity txCosEntity=(TxCosEntity) req;
        String txcos=JSON.toJSONString(txCosEntity);
        UploadSet uploadSet=new UploadSet();
        uploadSet.setId(1);
        uploadSet.setVisible(req.getVisible());
        uploadSet.setTxcos(txcos);
        if(uploadSetService.updateById(uploadSet)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 保存本地配置
     * @param dto
     * @return
     */
    @RequestMapping("/saveLocal")
    @Permission(permissionList = {"/admin/setting/getUploadConfig"})
    public JSONResult<EmptyVo>saveLocal(@RequestBody SaveLocalDto dto)
    {
        LocalUploadConfig localUploadConfig=(LocalUploadConfig) dto;
        String local=JSON.toJSONString(localUploadConfig);
        UploadSet uploadSet=new UploadSet();
        uploadSet.setId(1);
        uploadSet.setVisible(dto.getVisible());
        uploadSet.setLocal(local);
        if(uploadSetService.updateById(uploadSet)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 保存火山云TOS配置
     * @param dto
     * @return
     */
    @RequestMapping("/saveTos")
    @Permission(permissionList = {"/admin/setting/getUploadConfig"})
    public JSONResult<EmptyVo> saveTos(@RequestBody SaveTosDto dto)
    {
        TosEntity tosEntity=(TosEntity) dto;
        String tos=JSON.toJSONString(tosEntity);
        UploadSet uploadSet=new UploadSet();
        uploadSet.setId(1);
        uploadSet.setVisible(dto.getVisible());
        uploadSet.setTos(tos);
        if(uploadSetService.updateById(uploadSet)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 新增配置
     * @param req
     * @return
     */
    @RequestMapping("/addSetting")
    public JSONResult<EmptyVo> addSetting(@RequestBody AddSetting req){
        Setting setting=new Setting(

        );
        BeanUtils.copyProperties(req, setting);
        if(settingService.save(setting)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 编辑配置
     * @param req
     * @return
     */
    @RequestMapping("/editSetting")
    public JSONResult<EmptyVo> editSetting(@RequestBody EditSettingDto req){
        Setting setting=settingService.getById(req.getId());
        if(setting==null){
            return JSONResult.error("信息不存在");
        }
        BeanUtils.copyProperties(req, setting);
        if(settingService.updateById(setting)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 删除配置
     * @param id
     * @return
     */
    @RequestMapping("/delSetting/{id}")
    public JSONResult<EmptyVo> delSetting(@PathVariable Integer id){
        Setting setting=settingService.getById(id);
        if(setting==null){
            return JSONResult.error("信息不存在");
        }
        if(setting.getCanDel().equals(0)){
            return JSONResult.error("此配置无法删除!");
        }
        if(settingService.removeById(id)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 获取配置列表
     * @param req
     * @return
     */
    @RequestMapping("/settingList")
    public JSONResult<PageInfo<GetSettingListVo>> settingList(@RequestBody PageDto req){
        PageInfo<GetSettingListVo> pageInfo=settingService.getSettingList(req);
        return JSONResult.success(pageInfo);
    }

    /**
     * 获取当前的上传配置信息
     * @return
     */
    @RequestMapping("/getUploadToken")
    @Permission(required = false)
    public JSONResult<Map<String,Object>> getUploadToken(){
        Map<String,Object> config=uploadSetService.getUploadToken();
        return JSONResult.success(config);
    }
    //获取火山云的签名地址
    @PostMapping("/getTosSignUrl")
    @Permission(required = false)
    public JSONResult<Map<String,Object>>getTosSignUrl(@RequestBody GetTosSignUrlDto req)
    {
        UploadSet set=uploadSetService.getById(1);
        TosEntity tosEntity= JSON.toJavaObject(JSONObject.parseObject(set.getTos()),TosEntity.class);
        TosUtil tosUtil=new TosUtil(tosEntity);
        Map<String,Object> map=tosUtil.getUploadUrl(req.getKey());
        return JSONResult.success(map);
    }

    /**
     * 新增文件
     * @param req
     * @param result
     * @return
     */
    @RequestMapping("/addFile")
    @Permission(required = false)
    public JSONResult<EmptyVo> addFile(@RequestBody @Valid AddFileDto req, BindingResult result){
        ValidateUtil.checkError(result);
        UploadFiles files=new UploadFiles(
                0,req.getDomain(),req.getType(), req.getName(), req.getKey(), req.getUrl(), DateUtil.getDaDate(), req.getPid()
        );
        if(uploadFilesService.save(files)){
            return JSONResult.success();
        }
        return JSONResult.error();
    }

    /**
     * 删除文件
     * @param id
     * @return
     */
    @RequestMapping("/delFile/{id}")
    public JSONResult<EmptyVo> delFile(@PathVariable Integer id){
        uploadFilesService.removeFileById(id);
        return JSONResult.success();
    }

    /**
     * 获取文件列表
     * @param req
     * @return
     */
    @RequestMapping("/getFileList")
    @Permission(required = false)
    public JSONResult<PageInfo<GetFileListVo>>getFileList(@RequestBody GetFileListDto req)
    {
        PageInfo<GetFileListVo> pageInfo=uploadFilesService.getFileList(req);
        return JSONResult.success(pageInfo);
    }

    /**
     * 设置安全规则
     * @param dto
     * @return
     */
    @RequestMapping("/setAuthConfig")
    public JSONResult<EmptyVo> setAuthConfig(@RequestBody SetAuthConfigDto dto)
    {

        AuthConfig authConfig=authConfigService.getById(1);
        boolean reload=!authConfig.getBakupDbCron().equals(dto.getBakupDbCron()) || !authConfig.getAutoBackup().equals(dto.getAutoBackup());
        BeanUtils.copyProperties(dto,authConfig);
        authConfig.setAtime(DateUtil.getDaDate());
        authConfigService.updateById(authConfig);
        if(reload)
        {
            new Runnable(){
                @SneakyThrows
                @Override
                public void run() {

                    ApplicationArguments args = AdminApplication.context.getBean(ApplicationArguments.class);
                    Thread thread = new Thread(() -> {
                        System.out.println("springboot restart...");
                        AdminApplication.context.close();
                        AdminApplication.context = SpringApplication.run(AdminApplication.class, args.getSourceArgs());
                    });
                    // 设置为用户线程，不是守护线程
                    thread.setDaemon(false);
                    thread.start();
                }
            }.run();

        }
        return JSONResult.success();
    }
}
