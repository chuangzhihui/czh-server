package com.czh.admin.baseController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.czh.common.exception.ErrorException;
import com.czh.common.utils.DateUtil;
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
import com.czh.service.entity.UploadFiles;
import com.czh.service.entity.UploadSet;
import com.czh.service.vo.admin.UploadLog;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UploadController extends BaseController {

    @RequestMapping("/upload")
    public JSONResult<LocalUploadVo> upload(@RequestParam("file") MultipartFile file){
        LocalUploadVo vo=doUpload(file);
        return JSONResult.success(vo);
    }

    private LocalUploadVo doUpload(MultipartFile file)
    {
        UploadSet set=uploadSetService.getById(1);
        if(set==null || set.getLocal()==null || set.getLocal().isEmpty()){
           throw new ErrorException("未配置上传信息!");
        }
        LocalUploadConfig localUploadConfig= JSON.toJavaObject(JSONObject.parseObject(set.getLocal()),LocalUploadConfig.class);
        LocalUploadUtil localUploadUtil=new LocalUploadUtil(localUploadConfig);
        return localUploadUtil.upload(file);
    }


    @RequestMapping( value = "/ueditor",method = RequestMethod.GET)
    public Object ueditor(@RequestParam(value = "action") String action, HttpServletRequest request){
        if(action.equals("config")){
            JSONObject json=JSONObject.parseObject(configStr);
            return json;
        }
        if(action.equals("listimage")){
            int page=Integer.parseInt(request.getParameter("start"));
            int size=Integer.parseInt(request.getParameter("size"));
            PageHelper.startPage(page,size,"id desc");
            List<UploadLog> lists=uploadFilesService.getImgList();
            PageInfo<UploadLog> pageInfo=new PageInfo<>(lists);
            Map json=new HashMap();
            json.put("list",lists);
            json.put("start",page);
            json.put("state","SUCCESS");
            json.put("total",pageInfo.getTotal());
            return json;

        }
        return null;
    }
    @RequestMapping( value = "/ueditor",method = RequestMethod.POST)
    public Object upload_ue(@RequestParam(value = "upfile") MultipartFile multipartFile) throws IOException {

        if(multipartFile.isEmpty()){
            return "没有文件";
        }
        String fileUrl="";
        //查询现在上传方式
        UploadSet config=uploadSetService.getById(1);
        if(config==null){
            return "没有配置上传方式";
        }
        String fileKey="admin-upload/"+multipartFile.getOriginalFilename();
        if(config.getVisible()==1){
            //七牛
            QiniuEntity qiniuEntity= JSON.toJavaObject(JSONObject.parseObject(config.getQiniu()),QiniuEntity.class);
            QiniuUtil qiniu=new QiniuUtil(qiniuEntity);
            qiniu.uploadFile(multipartFile.getBytes(),fileKey);
            fileUrl=qiniuEntity.getDomain()+"/"+fileKey;
        }
        else if(config.getVisible()==2){
            //阿里oss
            AliOssEntity aliOssEntity=JSON.toJavaObject(JSONObject.parseObject(config.getAlioss()),AliOssEntity.class);
            AliOssUtil aliOSS=new AliOssUtil(aliOssEntity);
            aliOSS.uploadFile(multipartFile.getBytes(),fileKey);
            fileUrl=aliOSS.getDomain()+"/"+fileKey;
        }
        else if(config.getVisible()==3){
            //腾讯cos
            TxCosEntity txCosEntity=JSON.toJavaObject(JSONObject.parseObject(config.getTxcos()),TxCosEntity.class);
            TxCosUtil txCosUtil=new TxCosUtil(txCosEntity);
            final File excelFile =File.createTempFile(multipartFile.getOriginalFilename(),null);
            txCosUtil.uploadFile(excelFile,fileKey);
            fileUrl=txCosUtil.getPath()+"/"+fileKey;
        }else if(config.getVisible()==4){
            //本地上传
            LocalUploadVo localUploadVo=doUpload(multipartFile);
            fileUrl=localUploadVo.getUrl();
        }
        else if(config.getVisible()==5){
            //火山云
            TosEntity tosEntity=JSON.toJavaObject(JSONObject.parseObject(config.getTos()),TosEntity.class);
            TosUtil tosUtil=new TosUtil(tosEntity);
            tosUtil.uploadFile(multipartFile.getBytes(),fileKey);
            fileUrl=tosEntity.getDomain()+"/"+fileKey;
        }
        else{
            return JSONResult.error("未配置上传信息");
        }
        //文件类型
        String fileType=multipartFile.getContentType();
        int type;
        if(fileType.contains("image")){
            type=1;
        }else if(fileType.contains("video")){
            type=2;
        }else{
            type=3;
        }
        //插入上传记录
        UploadFiles log=new UploadFiles();
        log.setAtime(DateUtil.getDaDate());
        log.setName(multipartFile.getOriginalFilename());
        log.setType(type);
        log.setDomain(config.getVisible());
        log.setUrl(fileUrl);
        uploadFilesService.save(log);
        Map json=new HashMap();
        json.put("state","SUCCESS");
        json.put("original",multipartFile.getOriginalFilename());
        json.put("title",multipartFile.getOriginalFilename());
        json.put("url",fileUrl);
        json.put("type","."+fileType);
        return json;
    }
    private String configStr="{\n" +
            "    /* 上传图片配置项 */\n" +
            "    \"imageActionName\": \"uploadimage\", /* 执行上传图片的action名称 */\n" +
            "    \"imageFieldName\": \"upfile\", /* 提交的图片表单名称 */\n" +
            "    \"imageMaxSize\": 2048000, /* 上传大小限制，单位B */\n" +
            "    \"imageAllowFiles\": [\".png\", \".jpg\", \".jpeg\", \".gif\", \".bmp\"], /* 上传图片格式显示 */\n" +
            "    \"imageCompressEnable\": true, /* 是否压缩图片,默认是true */\n" +
            "    \"imageCompressBorder\": 1600, /* 图片压缩最长边限制 */\n" +
            "    \"imageInsertAlign\": \"none\", /* 插入的图片浮动方式 */\n" +
            "    \"imageUrlPrefix\": \"\", /* 图片访问路径前缀 */\n" +
            "    \"imagePathFormat\": \"/ueditor/php/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}\", /* 上传保存路径,可以自定义保存路径和文件名格式 */\n" +
            "                                /* {filename} 会替换成原文件名,配置这项需要注意中文乱码问题 */\n" +
            "                                /* {rand:6} 会替换成随机数,后面的数字是随机数的位数 */\n" +
            "                                /* {time} 会替换成时间戳 */\n" +
            "                                /* {yyyy} 会替换成四位年份 */\n" +
            "                                /* {yy} 会替换成两位年份 */\n" +
            "                                /* {mm} 会替换成两位月份 */\n" +
            "                                /* {dd} 会替换成两位日期 */\n" +
            "                                /* {hh} 会替换成两位小时 */\n" +
            "                                /* {ii} 会替换成两位分钟 */\n" +
            "                                /* {ss} 会替换成两位秒 */\n" +
            "                                /* 非法字符 \\ : * ? \" < > | */\n" +
            "                                /* 具请体看线上文档: fex.baidu.com/ueditor/#use-format_upload_filename */\n" +
            "\n" +
            "    /* 涂鸦图片上传配置项 */\n" +
            "    \"scrawlActionName\": \"uploadscrawl\", /* 执行上传涂鸦的action名称 */\n" +
            "    \"scrawlFieldName\": \"upfile\", /* 提交的图片表单名称 */\n" +
            "    \"scrawlPathFormat\": \"/ueditor/php/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}\", /* 上传保存路径,可以自定义保存路径和文件名格式 */\n" +
            "    \"scrawlMaxSize\": 2048000, /* 上传大小限制，单位B */\n" +
            "    \"scrawlUrlPrefix\": \"\", /* 图片访问路径前缀 */\n" +
            "    \"scrawlInsertAlign\": \"none\",\n" +
            "\n" +
            "    /* 截图工具上传 */\n" +
            "    \"snapscreenActionName\": \"uploadimage\", /* 执行上传截图的action名称 */\n" +
            "    \"snapscreenPathFormat\": \"/ueditor/php/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}\", /* 上传保存路径,可以自定义保存路径和文件名格式 */\n" +
            "    \"snapscreenUrlPrefix\": \"\", /* 图片访问路径前缀 */\n" +
            "    \"snapscreenInsertAlign\": \"none\", /* 插入的图片浮动方式 */\n" +
            "\n" +
            "    /* 抓取远程图片配置 */\n" +
            "    \"catcherLocalDomain\": [\"127.0.0.1\", \"localhost\", \"img.baidu.com\"],\n" +
            "    \"catcherActionName\": \"catchimage\", /* 执行抓取远程图片的action名称 */\n" +
            "    \"catcherFieldName\": \"source\", /* 提交的图片列表表单名称 */\n" +
            "    \"catcherPathFormat\": \"/ueditor/php/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}\", /* 上传保存路径,可以自定义保存路径和文件名格式 */\n" +
            "    \"catcherUrlPrefix\": \"\", /* 图片访问路径前缀 */\n" +
            "    \"catcherMaxSize\": 2048000, /* 上传大小限制，单位B */\n" +
            "    \"catcherAllowFiles\": [\".png\", \".jpg\", \".jpeg\", \".gif\", \".bmp\"], /* 抓取图片格式显示 */\n" +
            "\n" +
            "    /* 上传视频配置 */\n" +
            "    \"videoActionName\": \"uploadvideo\", /* 执行上传视频的action名称 */\n" +
            "    \"videoFieldName\": \"upfile\", /* 提交的视频表单名称 */\n" +
            "    \"videoPathFormat\": \"/ueditor/php/upload/video/{yyyy}{mm}{dd}/{time}{rand:6}\", /* 上传保存路径,可以自定义保存路径和文件名格式 */\n" +
            "    \"videoUrlPrefix\": \"\", /* 视频访问路径前缀 */\n" +
            "    \"videoMaxSize\": 102400000, /* 上传大小限制，单位B，默认100MB */\n" +
            "    \"videoAllowFiles\": [\n" +
            "        \".flv\", \".swf\", \".mkv\", \".avi\", \".rm\", \".rmvb\", \".mpeg\", \".mpg\",\n" +
            "        \".ogg\", \".ogv\", \".mov\", \".wmv\", \".mp4\", \".webm\", \".mp3\", \".wav\", \".mid\"], /* 上传视频格式显示 */\n" +
            "\n" +
            "    /* 上传文件配置 */\n" +
            "    \"fileActionName\": \"uploadfile\", /* controller里,执行上传视频的action名称 */\n" +
            "    \"fileFieldName\": \"upfile\", /* 提交的文件表单名称 */\n" +
            "    \"filePathFormat\": \"/ueditor/php/upload/file/{yyyy}{mm}{dd}/{time}{rand:6}\", /* 上传保存路径,可以自定义保存路径和文件名格式 */\n" +
            "    \"fileUrlPrefix\": \"\", /* 文件访问路径前缀 */\n" +
            "    \"fileMaxSize\": 51200000, /* 上传大小限制，单位B，默认50MB */\n" +
            "    \"fileAllowFiles\": [\n" +
            "        \".png\", \".jpg\", \".jpeg\", \".gif\", \".bmp\",\n" +
            "        \".flv\", \".swf\", \".mkv\", \".avi\", \".rm\", \".rmvb\", \".mpeg\", \".mpg\",\n" +
            "        \".ogg\", \".ogv\", \".mov\", \".wmv\", \".mp4\", \".webm\", \".mp3\", \".wav\", \".mid\",\n" +
            "        \".rar\", \".zip\", \".tar\", \".gz\", \".7z\", \".bz2\", \".cab\", \".iso\",\n" +
            "        \".doc\", \".docx\", \".xls\", \".xlsx\", \".ppt\", \".pptx\", \".pdf\", \".txt\", \".md\", \".xml\"\n" +
            "    ], /* 上传文件格式显示 */\n" +
            "\n" +
            "    /* 列出指定目录下的图片 */\n" +
            "    \"imageManagerActionName\": \"listimage\", /* 执行图片管理的action名称 */\n" +
            "    \"imageManagerListPath\": \"/ueditor/php/upload/image/\", /* 指定要列出图片的目录 */\n" +
            "    \"imageManagerListSize\": 20, /* 每次列出文件数量 */\n" +
            "    \"imageManagerUrlPrefix\": \"\", /* 图片访问路径前缀 */\n" +
            "    \"imageManagerInsertAlign\": \"none\", /* 插入的图片浮动方式 */\n" +
            "    \"imageManagerAllowFiles\": [\".png\", \".jpg\", \".jpeg\", \".gif\", \".bmp\"], /* 列出的文件类型 */\n" +
            "\n" +
            "    /* 列出指定目录下的文件 */\n" +
            "    \"fileManagerActionName\": \"listfile\", /* 执行文件管理的action名称 */\n" +
            "    \"fileManagerListPath\": \"/ueditor/php/upload/file/\", /* 指定要列出文件的目录 */\n" +
            "    \"fileManagerUrlPrefix\": \"\", /* 文件访问路径前缀 */\n" +
            "    \"fileManagerListSize\": 20, /* 每次列出文件数量 */\n" +
            "    \"fileManagerAllowFiles\": [\n" +
            "        \".png\", \".jpg\", \".jpeg\", \".gif\", \".bmp\",\n" +
            "        \".flv\", \".swf\", \".mkv\", \".avi\", \".rm\", \".rmvb\", \".mpeg\", \".mpg\",\n" +
            "        \".ogg\", \".ogv\", \".mov\", \".wmv\", \".mp4\", \".webm\", \".mp3\", \".wav\", \".mid\",\n" +
            "        \".rar\", \".zip\", \".tar\", \".gz\", \".7z\", \".bz2\", \".cab\", \".iso\",\n" +
            "        \".doc\", \".docx\", \".xls\", \".xlsx\", \".ppt\", \".pptx\", \".pdf\", \".txt\", \".md\", \".xml\"\n" +
            "    ] /* 列出的文件类型 */\n" +
            "\n" +
            "}";
}
