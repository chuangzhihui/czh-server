package com.czh.admin.baseController.login;

import com.czh.admin.baseController.BaseController;
import com.czh.admin.security.AdminAuthenticationProvider.AdminLoginDetails;
import com.czh.admin.security.AdminDetails;
import com.czh.admin.security.AdminTokenService;
import com.czh.admin.security.CaptchaService;
import com.czh.common.annotation.Log;
import com.czh.common.exception.LoginFailException;
import com.czh.common.utils.ValidateCodeUtil;
import com.czh.common.vo.JSONResult;
import com.czh.service.dto.admin.AdminLoginDto;
import com.czh.service.entity.Setting;
import com.czh.service.vo.admin.AdminLoginVo;
import com.czh.service.vo.admin.GetSystemNameVo;
import com.czh.service.vo.admin.GetVerifyVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * 登录模块
 * <p>
 * 本类只负责「接收参数 → 提交认证 → 翻译结果」，不包含任何认证规则：
 * 验证码校验、失败计数、账号冻结统统下沉到 {@link com.czh.admin.security.AdminAuthenticationProvider}，
 * 令牌签发下沉到 {@link AdminTokenService}。
 */
@Slf4j
@RestController
@RequestMapping("/admin/login")
public class LoginController extends BaseController {

    @Autowired
    CaptchaService captchaService;
    @Autowired
    AdminTokenService adminTokenService;
    @Autowired
    AuthenticationManager authenticationManager;

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
        String uuid = captchaService.create(vCode.getCode());
        String encode = Base64.getEncoder().encodeToString(buff);
        GetVerifyVo vo=new GetVerifyVo();
        vo.setImg("data:image/png;base64,"+encode);
        vo.setUuid(uuid);
        return JSONResult.success(vo);
    }

    //用户登录
    @PostMapping("/login")
    @Log(operation = "账户登录")
    public JSONResult<AdminLoginVo> login(@RequestBody AdminLoginDto req){
        //1.组装未认证令牌：凭证=账号密码；图形验证码按 Spring Security 惯例挂在 details 上
        UsernamePasswordAuthenticationToken authRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(req.getUsername(), req.getPassword());
        authRequest.setDetails(new AdminLoginDetails(req.getUuid(), req.getCode()));
        try {
            //2.提交认证：成功返回已认证令牌，任何失败原因都表现为 AuthenticationException
            Authentication authResult = authenticationManager.authenticate(authRequest);
            //3.落地安全上下文（本项目为 STATELESS，后续请求由 JWTAuthFilter 依据 token 重建）
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authResult);
            SecurityContextHolder.setContext(context);
            //4.签发令牌，返回登录信息
            AdminDetails principal = (AdminDetails) authResult.getPrincipal();
            String token = adminTokenService.issue(principal);
            return JSONResult.success(new AdminLoginVo(principal.getAvatar(), principal.getUsername(), token));
        } catch (AuthenticationException e) {
            log.warn("登录失败 username={} reason={}", req.getUsername(), e.getClass().getSimpleName());
            return JSONResult.error(resolveFailMessage(e));
        }
    }

    /**
     * 把 Spring Security 的认证异常翻译成前端可读文案。
     * 前端约定：msg 以 "ALERT" 开头时弹模态框，否则走轻提示。
     */
    private String resolveFailMessage(AuthenticationException e) {
        //登录前账号就已被冻结
        if (e instanceof DisabledException) {
            return "ALERT账号已被冻结，请联系管理员解除!";
        }
        //本次失败导致冻结 / 验证码问题或临界提醒：文案已由 Provider 写好，原样透出
        if (e instanceof LockedException || e instanceof LoginFailException) {
            return e.getMessage();
        }
        //账号不存在、密码错误统一模糊提示，避免账号枚举
        return "用户名或密码错误!";
    }
}
