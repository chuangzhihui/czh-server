package com.czh.admin.baseController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.czh.common.exception.ErrorException;
import com.czh.common.utils.StringUtil;
import com.czh.common.utils.aliOssUtil.AliOssUtil;
import com.czh.common.utils.aliOssUtil.entity.AliOssEntity;
import com.czh.common.utils.localUploadUtil.LocalUploadUtil;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadConfig;
import com.czh.common.utils.localUploadUtil.entity.LocalUploadVo;
import com.czh.common.utils.qiniuUtil.QiniuUtil;
import com.czh.common.utils.qiniuUtil.entity.QiniuEntity;
import com.czh.common.utils.tosUtil.TosUtil;
import com.czh.common.utils.tosUtil.entity.TosEntity;
import com.czh.common.utils.txCosUtil.TxCosUtil;
import com.czh.common.utils.txCosUtil.entity.TxCosEntity;
import com.czh.common.vo.JSONResult;
import com.czh.service.entity.UploadSet;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@RestController
public class UploadController extends BaseController {

    @RequestMapping("/upload")
    public JSONResult<LocalUploadVo> upload(@RequestParam("file") MultipartFile file,@RequestParam("dir") String dir,
                                            @RequestParam("type") Integer type
                                            ){
        LocalUploadVo vo=doUpload(file,dir,type);

        return JSONResult.success(vo);
    }

    /**
     *
     * @param file 上传的文件
     * @param dir
     * @param type 文件类型  1图片 2视频 3 Excel 4 word 5 pdf 6 zip 7 未知类型文件 8文件夹
     * @return
     */
    private LocalUploadVo doUpload(MultipartFile file,String dir,Integer type)
    {
        UploadSet set=uploadSetService.getById(1);
        if(set==null || set.getVisible().equals(0)){
           throw new ErrorException("未配置上传信息!");
        }
        String originalFilename=file.getOriginalFilename();
        String[] arr=originalFilename.split("\\.");
        String extName=arr[arr.length -1].toLowerCase(Locale.ROOT);
        String key= StringUtil.getRadomStr(8)+"."+extName;
        if(!dir.isEmpty())
        {
            key=dir+"/"+key;
        }
        LocalUploadVo vo;
        //选用的哪个 0未选用 1七牛 2阿里 3腾讯   4本地 5 火山云tos
        switch (set.getVisible())
        {
            case 1://七牛云
                QiniuEntity qiniuEntity = JSON.toJavaObject(JSONObject.parseObject(set.getQiniu()),QiniuEntity.class);
                QiniuUtil qiniuUtil=new QiniuUtil(qiniuEntity);
                vo= qiniuUtil.uploadFile(file,key,type);
                break;
            case 2://阿里云
                AliOssEntity entity =  JSON.toJavaObject(JSONObject.parseObject(set.getAlioss()),AliOssEntity.class);
                AliOssUtil aliOssUtil=new AliOssUtil(entity);
                vo= aliOssUtil.uploadFile(file,key,type);
                break;
            case 3://腾讯COS
                TxCosEntity txCosEntity = JSON.toJavaObject(JSONObject.parseObject(set.getTxcos()),TxCosEntity.class);
                TxCosUtil txCosUtil=new TxCosUtil(txCosEntity);
                vo= txCosUtil.uploadFile(file,key,type);
                break;
            case 4://本地上传
                LocalUploadConfig localUploadConfig= JSON.toJavaObject(JSONObject.parseObject(set.getLocal()),LocalUploadConfig.class);
                LocalUploadUtil localUploadUtil=new LocalUploadUtil(localUploadConfig);
                vo= localUploadUtil.upload(file,dir,type);
                break;
            case 5://火山云TOS
                TosEntity tosEntity = JSON.toJavaObject(JSONObject.parseObject(set.getTos()),TosEntity.class);
                TosUtil tosUtil=new TosUtil(tosEntity);
                vo= tosUtil.uploadFile(file,key,type);
                break;
            default:
                throw new ErrorException("未配置上传信息");
        }
        vo.setDomain(set.getVisible());
        vo.setType(type);
        return vo;
    }
}
