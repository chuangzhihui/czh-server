package com.czh.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czh.common.exception.ErrorException;
import com.czh.service.dao.UploadFilesDao;
import com.czh.service.dto.admin.GetFileListDto;
import com.czh.service.entity.UploadFiles;
import com.czh.service.service.AsyncService;
import com.czh.service.service.UploadFilesService;
import com.czh.service.service.UploadSetService;
import com.czh.service.vo.admin.GetFileListVo;
import com.czh.service.vo.admin.UploadLog;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 文件库(UploadFiles)表服务实现类
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
@Service
public class UploadFilesServiceImpl extends ServiceImpl<UploadFilesDao,UploadFiles> implements UploadFilesService {
    @Autowired
    protected AsyncService asyncService;
    @Autowired
    protected UploadSetService uploadSetService;
    @Override
    public void removeFileById(Integer id) {
        UploadFiles file=getById(id);
        if(file==null)
        {
            throw new ErrorException("文件不存在!");
        }
        //判断如果文件为文件夹形式
        if(file.getType()==8){
            //文件夹-判断下面是否还有文件
            long num=count(new QueryWrapper<UploadFiles>().eq("pid",id));
            if(num>0)
            {
                throw new ErrorException("请先删除文件夹中的文件!");
            }
        }
        //删除文件
        if(removeById(id)) {
            //子线程删除第三方文件
            asyncService.removeFile(file, uploadSetService.getById(1));
        }
    }

    @Override
    public PageInfo<GetFileListVo> getFileList(GetFileListDto req) {
        PageHelper.startPage(req.getPage(),req.getSize(),"atime desc");
        List<GetFileListVo> vos=baseMapper.selectByPid(req);
        return new PageInfo<>(vos);
    }

    @Override
    public List<UploadLog> getImgList() {
        return baseMapper.getImgList();
    }
}
