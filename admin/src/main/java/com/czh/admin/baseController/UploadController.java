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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
public class UploadController extends BaseController {

    /**
     * 上传子目录白名单：仅允许多级 字母/数字/下划线/中横线，如 admin/file。
     * 禁止 {@code ..}、{@code .}、反斜杠、绝对路径、盘符等，防止路径穿越写到存储根目录之外。
     */
    private static final Pattern DIR_PATTERN = Pattern.compile("[a-zA-Z0-9_-]+(/[a-zA-Z0-9_-]+)*");

    /** 扩展名白名单：仅允许纯字母数字，防止文件名夹带路径分隔符 */
    private static final Pattern EXT_PATTERN = Pattern.compile("[a-zA-Z0-9]+");

    /**
     * 文件类型 → 扩展名白名单（BE-07）。
     * <p>
     * key 与 {@code type} 参数对应：1图片 2视频 3Excel 4word 5pdf 6压缩包；
     * type=7（未知类型文件）是业务兜底，只做 {@link #EXT_PATTERN} 格式校验、不限制内容，
     * 因此不在本表内。type=8（文件夹）不适用于单文件上传接口，同样不放行。
     * <p>
     * 合并说明：原实现只有本地分支（LocalUploadUtil）有白名单，七牛/阿里/腾讯/火山
     * 四个分支任意字母数字扩展名都能传（exe 也能过）。现在五家分支统一在 doUpload 入口收口。
     * 注意：svg 有内嵌脚本（XSS）风险，exe/dll/jar 等可执行格式一律不放行，均不加入白名单。
     */
    private static final Map<Integer, Set<String>> TYPE_EXT_WHITELIST = new HashMap<>();

    static {
        TYPE_EXT_WHITELIST.put(1, new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "ico")));
        TYPE_EXT_WHITELIST.put(2, new HashSet<>(Arrays.asList("mp4", "avi", "mov", "wmv", "mkv", "flv")));
        TYPE_EXT_WHITELIST.put(3, new HashSet<>(Arrays.asList("xls", "xlsx", "csv")));
        TYPE_EXT_WHITELIST.put(4, new HashSet<>(Arrays.asList("doc", "docx")));
        TYPE_EXT_WHITELIST.put(5, new HashSet<>(Arrays.asList("pdf")));
        TYPE_EXT_WHITELIST.put(6, new HashSet<>(Arrays.asList("zip", "rar", "7z", "gz", "tar")));
    }

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
        //目录参数校验：dir 直接参与存储路径拼接，必须严格白名单，防止 ../ 路径穿越
        if(dir!=null && !dir.isEmpty() && !DIR_PATTERN.matcher(dir).matches())
        {
            throw new ErrorException("上传目录参数非法!");
        }
        String originalFilename=file.getOriginalFilename();
        String[] arr=originalFilename.split("\\.");
        String extName=arr[arr.length -1].toLowerCase(Locale.ROOT);
        //扩展名校验：避免文件名夹带路径分隔符（如 a.png/../../evil）
        if(!EXT_PATTERN.matcher(extName).matches())
        {
            throw new ErrorException("文件扩展名非法!");
        }
        //类型→扩展名白名单（BE-07）：五家存储分支统一收口，原实现只有本地分支有白名单。
        //白名单外的 type（7 兜底 / 8 文件夹）不做内容限制，仍受 EXT_PATTERN 格式约束。
        Set<String> allowedExts=TYPE_EXT_WHITELIST.get(type);
        if(allowedExts!=null && !allowedExts.contains(extName))
        {
            throw new ErrorException("不支持上传此类文件:"+extName);
        }
        String key= StringUtil.getRadomStr(8)+"."+extName;
        if(dir!=null && !dir.isEmpty())
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
