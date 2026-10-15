package com.czh.admin.baseController.setting;

import lombok.extern.slf4j.Slf4j;

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
import com.czh.common.utils.tosUtil.entity.TosEntity;
import com.czh.common.utils.txCosUtil.entity.TxCosEntity;
import com.czh.common.vo.EmptyVo;
import com.czh.common.vo.JSONResult;
import com.czh.service.dto.admin.*;
import com.czh.service.entity.AuthConfig;
import com.czh.service.entity.Setting;
import com.czh.service.entity.UploadFiles;
import com.czh.service.entity.UploadSet;
import com.czh.service.vo.admin.GetSettingListVo;
import com.czh.service.vo.admin.GetUploadConfigVo;
import com.czh.service.vo.admin.GetUploadTokenVo;
import com.czh.service.vo.admin.UploadFileListVo;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * 系统设置类
 */
@RestController
@RequestMapping("/admin/setting")
@Slf4j
public class SettingController extends BaseController {

    /**
     * 密钥掩码（BE-05）：
     * 云存储的 secretKey / sk / ak 属于主密钥，一旦随配置接口下发到前端，
     * 会同时进入浏览器内存、Network 面板与可能的代理日志，等同于把桶交出去。
     * 因此读取时只回「前 4 位 + ****」，且保存时必须识别掩码并不回写。
     */
    private static final String SECRET_MASK = "****";

    /**
     * 读取上传配置：密钥字段一律脱敏后下发。
     * <p>
     * 注意：前端 UploadSet 页面会把下发值直接填进表单，保存时原样回传，
     * 所以必须配合 {@link #isMaskedOrEmpty(String)} 在保存侧忽略掩码值，
     * 否则用户"打开页面→直接保存"就会把 **** 写回库里覆盖真值。
     */
    @RequestMapping("/getUploadConfig")
    public JSONResult<GetUploadConfigVo> getUploadConfig(){
        //获取配置信息
        UploadSet set=uploadSetService.getBaseMapper().selectById(1);
        GetUploadConfigVo vo=new GetUploadConfigVo();
        vo.setVisible(set.getVisible());
        QiniuEntity qiniuEntity= JSON.toJavaObject(JSONObject.parseObject(set.getQiniu()),QiniuEntity.class);
        if(qiniuEntity!=null){
            qiniuEntity.setAccessKey(maskSecret(qiniuEntity.getAccessKey()));
            qiniuEntity.setSecretKey(maskSecret(qiniuEntity.getSecretKey()));
        }
        vo.setQiniu(qiniuEntity);
        AliOssEntity aliOssEntity=JSON.toJavaObject(JSONObject.parseObject(set.getAlioss()),AliOssEntity.class);
        if(aliOssEntity!=null){
            aliOssEntity.setAk(maskSecret(aliOssEntity.getAk()));
            aliOssEntity.setSk(maskSecret(aliOssEntity.getSk()));
        }
        vo.setAlioss(aliOssEntity);
        TxCosEntity txCosEntity=JSON.toJavaObject(JSONObject.parseObject(set.getTxcos()),TxCosEntity.class);
        if(txCosEntity!=null){
            txCosEntity.setAk(maskSecret(txCosEntity.getAk()));
            txCosEntity.setSk(maskSecret(txCosEntity.getSk()));
        }
        vo.setTxcos(txCosEntity);
        TosEntity tosEntity=JSON.toJavaObject(JSONObject.parseObject(set.getTos()),TosEntity.class);
        if(tosEntity!=null){
            tosEntity.setAccessKey(maskSecret(tosEntity.getAccessKey()));
            tosEntity.setSecretKey(maskSecret(tosEntity.getSecretKey()));
        }
        vo.setTos(tosEntity);
        LocalUploadConfig  localUploadConfig=JSON.toJavaObject(JSONObject.parseObject(set.getLocal()),LocalUploadConfig.class);
        vo.setLocal(localUploadConfig);
        return JSONResult.success(vo);
    }

    /**
     * 密钥脱敏：保留前 4 位便于管理员辨认是哪一对密钥，其余全部掩码。
     *
     * @param value 真实密钥，允许为 null / 空
     * @return 掩码后的值；null / 空原样返回
     */
    private String maskSecret(String value){
        if(value==null || value.isEmpty()){
            return value;
        }
        if(value.length()<=4){
            return SECRET_MASK;
        }
        return value.substring(0,4)+SECRET_MASK;
    }

    /**
     * 判断是否为"掩码值或空值"——这两种情况都表示用户没有改动密钥，保存时必须沿用库里的真值。
     *
     * @param value 前端回传的密钥值
     * @return 需要忽略（沿用旧值）返回 true
     */
    private boolean isMaskedOrEmpty(String value){
        return value==null || value.isBlank() || value.contains(SECRET_MASK);
    }

    /**
     * 读取库中已有的上传配置（用于保存时补齐未改动的密钥）
     *
     * @return 已存在的 UploadSet；不存在返回 null
     */
    private UploadSet getExistsUploadSet(){
        return uploadSetService.getBaseMapper().selectById(1);
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
        // 前端回显的是掩码值：未改动的密钥沿用库里真值，绝不让 "****" 覆盖真值（BE-05）
        UploadSet exists=getExistsUploadSet();
        if(exists!=null && exists.getQiniu()!=null && !exists.getQiniu().isEmpty()){
            QiniuEntity saved=JSON.toJavaObject(JSONObject.parseObject(exists.getQiniu()),QiniuEntity.class);
            if(saved!=null){
                if(isMaskedOrEmpty(qiniuEntity.getAccessKey())){
                    qiniuEntity.setAccessKey(saved.getAccessKey());
                }
                if(isMaskedOrEmpty(qiniuEntity.getSecretKey())){
                    qiniuEntity.setSecretKey(saved.getSecretKey());
                }
            }
        }
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
        UploadSet exists=getExistsUploadSet();
        if(exists!=null && exists.getAlioss()!=null && !exists.getAlioss().isEmpty()){
            AliOssEntity saved=JSON.toJavaObject(JSONObject.parseObject(exists.getAlioss()),AliOssEntity.class);
            if(saved!=null){
                if(isMaskedOrEmpty(aliOssEntity.getAk())){
                    aliOssEntity.setAk(saved.getAk());
                }
                if(isMaskedOrEmpty(aliOssEntity.getSk())){
                    aliOssEntity.setSk(saved.getSk());
                }
            }
        }
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
        UploadSet exists=getExistsUploadSet();
        if(exists!=null && exists.getTxcos()!=null && !exists.getTxcos().isEmpty()){
            TxCosEntity saved=JSON.toJavaObject(JSONObject.parseObject(exists.getTxcos()),TxCosEntity.class);
            if(saved!=null){
                if(isMaskedOrEmpty(txCosEntity.getAk())){
                    txCosEntity.setAk(saved.getAk());
                }
                if(isMaskedOrEmpty(txCosEntity.getSk())){
                    txCosEntity.setSk(saved.getSk());
                }
            }
        }
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
        UploadSet exists=getExistsUploadSet();
        if(exists!=null && exists.getTos()!=null && !exists.getTos().isEmpty()){
            TosEntity saved=JSON.toJavaObject(JSONObject.parseObject(exists.getTos()),TosEntity.class);
            if(saved!=null){
                if(isMaskedOrEmpty(tosEntity.getAccessKey())){
                    tosEntity.setAccessKey(saved.getAccessKey());
                }
                if(isMaskedOrEmpty(tosEntity.getSecretKey())){
                    tosEntity.setSecretKey(saved.getSecretKey());
                }
            }
        }
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
     * 获取文件上传token
     * @return
     */
    @RequestMapping("/getUploadToken")
    // TODO(BE-31①): 取消免鉴权前必须先做数据侧确认——本项目权限串 = 自身请求路由，
    //  必须与某条 menu.route 精确相等才放行。删掉 required=false 后，本端点会立刻要求
    //  czh_menu 表中存在 route = '/admin/setting/getUploadToken' 的行；
    //  若不存在，上传功能会对所有角色（含超管）403。
    //  因此这一改动必须与 menu 表数据迁移一起规划，不能只改代码。本轮按裁定不做。
    @Permission(required = false)
    public JSONResult<GetUploadTokenVo> getUploadToken(){
        GetUploadTokenVo vo=uploadSetService.getUploadToken();
        return JSONResult.success(vo);
    }


    /**
     * 新增文件
     * @param req
     * @param result
     * @return
     */
    @RequestMapping("/addFile")
    // TODO(BE-31①): 同 getUploadToken——取消 required=false 前必须先确认 czh_menu 表中
    //  存在 route = '/admin/setting/addFile' 的行，否则文件入库接口对所有角色 403。
    //  本轮按裁定不做（本机无 mysql 客户端，无法验证 menu 表实际数据）。
    @Permission(required = false)
    public JSONResult<EmptyVo> addFile(@RequestBody @Valid AddFileDto req, BindingResult result){
        ValidateUtil.checkError(result);
        UploadFiles files=new UploadFiles();
        BeanUtils.copyProperties(req, files);
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
    public JSONResult<PageInfo<UploadFileListVo>>getFileList(@RequestBody GetFileListDto req)
    {
        PageInfo<UploadFileListVo> pageInfo=uploadFilesService.getFileList(req);
        return JSONResult.success(pageInfo);
    }

    /**
     * 获取登录安全配置
     * @return
     */
    @PostMapping("/getAuthConfig")
    @Permission(permissionList = {"/admin/setting/setAuthConfig"})
    public JSONResult<AuthConfig>getAuthConfig(){
        return JSONResult.success(authConfigService.getById(1));
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
                        log.info("springboot restart...");
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
