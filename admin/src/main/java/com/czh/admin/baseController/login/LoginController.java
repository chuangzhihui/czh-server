package com.czh.admin.baseController.login;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.czh.admin.baseController.BaseController;
import com.czh.admin.security.AdminDetails;
import com.czh.common.annotation.Log;
import com.czh.common.exception.PasswordErrorException;
import com.czh.common.utils.DateUtil;
import com.czh.common.utils.JWTUtil;
import com.czh.common.utils.RequestUtil;
import com.czh.common.utils.ValidateCodeUtil;
import com.czh.common.utils.tosUtil.TosUtil;
import com.czh.common.utils.tosUtil.entity.GetTosSignUrlDto;
import com.czh.common.utils.tosUtil.entity.TosEntity;
import com.czh.common.vo.JSONResult;
import com.czh.service.dto.admin.AdminLoginDto;
import com.czh.service.entity.Admin;
import com.czh.service.entity.AuthConfig;
import com.czh.service.entity.Setting;
import com.czh.service.entity.UploadSet;
import com.czh.service.vo.admin.AdminLoginVo;
import com.czh.service.vo.admin.GetSystemNameVo;
import com.czh.service.vo.admin.GetVerifyVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 登录模块
 */
@RestController
@RequestMapping("/admin/login")
public class LoginController extends BaseController {
    //获取系统名称
    @GetMapping("/getSystemName")
    public JSONResult<GetSystemNameVo> getSystemName(){
        Setting setting=settingService.getById(1);
        GetSystemNameVo vo=new GetSystemNameVo();
        vo.setName(setting.getValue());
        return JSONResult.success(vo);
    }
    //获取图形验证码
    @GetMapping("/getCaptcha")
    public JSONResult<GetVerifyVo> getVerify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ValidateCodeUtil vCode = new ValidateCodeUtil(151,60,4,200);
        ByteArrayOutputStream outStream =new ByteArrayOutputStream();
        ImageIO.write(vCode.getBuffImg(), "png", outStream);
        byte[] buff = outStream.toByteArray();;
        outStream.close();
        String uuid = UUID.randomUUID().toString();
        redis.set("verify_code_"+uuid,vCode.getCode());
        String encode = Base64.getEncoder().encodeToString(buff);
        GetVerifyVo vo=new GetVerifyVo();
        vo.setImg("data:image/png;base64,"+encode);
        vo.setUuid(uuid);
        return JSONResult.success(vo);
    }
    @Autowired
    AuthenticationManager authenticationManager;
    //用户登录
    @PostMapping("/login")
    @Log(operation = "账户登录")
    public JSONResult<AdminLoginVo>login(@RequestBody AdminLoginDto req,HttpServletRequest request){
//        String rediskey="verify_code_"+req.getUuid();
//        String verifyCode=redis.get(rediskey);
//        if(verifyCode==null){
//            return JSONResult.error("验证码错误!");
//        }
//        redis.delete(rediskey);
        AuthConfig authConfig= authConfigService.getById(1);
        try {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword());
            //进行登录认证
            Authentication auth = authenticationManager.authenticate(authentication);
            if (auth == null)
            {
                return JSONResult.error("登录异常!");
            }
            AdminDetails admin = (AdminDetails) auth.getPrincipal();
            String token = JWTUtil.getJWTToken(admin.getAdminId().toString(),admin.getPassword(), RequestUtil.getIp());
            AdminLoginVo vo=new AdminLoginVo(admin.getAvatar(),admin.getUsername(),token);
            redis.setEx("adminToken_"+admin.getAdminId(),token,authConfig.getTimeOut(), TimeUnit.SECONDS);
            return JSONResult.success(vo);
        }catch (PasswordErrorException e)
        {
            //密码错误异常
            Admin admin=adminService.getAdminByUsername(req.getUsername());
            if(admin != null)
            {
                //查询上次登录错误和本次的相差时间
                long last=0;
                if(admin.getLastTryLoginTime()!=null)
                {
                    last=admin.getLastTryLoginTime().getTime();
                }
                long now_time= DateUtil.getDaDate().getTime();
                //如果时间差超过系统设定的值 重新计数
                long cha=(now_time-last)/1000;
                Integer fail_num=admin.getFailNum()+1;
                if(cha>authConfig.getFailNumTime())
                {
                    fail_num=1;
                }
                admin.setLastTryLoginIp(RequestUtil.getIp());
                admin.setLastTryLoginTime(DateUtil.getDaDate());
                admin.setFailNum(fail_num);
                if(fail_num>=authConfig.getFailNum())
                {
                    //冻结账号
                    admin.setStatus(0);
                    adminService.updateById(admin);
                    return JSONResult.error("ALERT密码连续错误"+fail_num+"次,账号已被冻结,请联系管理员解除!");
                }
                adminService.updateById(admin);
                if(authConfig.getFailNum()-fail_num==1)
                {
                    return JSONResult.error("ALERT密码已连续错误"+fail_num+"次,再次输入错误将冻结账号!");
                }
            }
            return JSONResult.error("用户名或密码错误!");
        }

    }


    /**
     * 获取当前的上传配置信息
     * @return
     */
    @RequestMapping("/getUploadToken")
    public JSONResult<Map<String,Object>> getUploadToken(){
        Map<String,Object> config=uploadSetService.getUploadToken();
        return JSONResult.success(config);
    }
    //获取火山云的签名地址
    @PostMapping("/getTosSignUrl")
    public JSONResult<Map<String,Object>>getTosSignUrl(@RequestBody GetTosSignUrlDto req)
    {
        UploadSet set=uploadSetService.getById(1);
        TosEntity tosEntity= JSON.toJavaObject(JSONObject.parseObject(set.getTos()),TosEntity.class);
        TosUtil tosUtil=new TosUtil(tosEntity);
        Map<String,Object> map=tosUtil.getUploadUrl(req.getKey());
        return JSONResult.success(map);
    }

}
