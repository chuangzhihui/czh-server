package com.czh.admin.security;

import com.czh.common.exception.LoginFailException;
import com.czh.common.utils.DateUtil;
import com.czh.common.utils.RequestUtil;
import com.czh.service.entity.Admin;
import com.czh.service.entity.AuthConfig;
import com.czh.service.service.AdminService;
import com.czh.service.service.AuthConfigService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

/**
 * 后台管理员认证 Provider
 * <p>
 * 继承 DaoAuthenticationProvider，同时自己实现 {@link UserDetailsService}（加载用户），
 * 只覆写三个钩子，其余沿用 Spring Security 原生流程：
 * <ol>
 *   <li>{@link #loadUserByUsername}：按用户名取管理员，交给父类作为 UserDetails 来源；
 *       查不到时抛 UsernameNotFoundException，父类中转层会转成 BadCredentialsException，避免暴露账号是否存在。</li>
 *   <li>{@link #authenticate}：先校验图形验证码（挂在 details 上），再交给父类；成功后清空历史失败次数。
 *       验证码放在这里校验，好处是校验不通过时既不查库也不累计失败次数。</li>
 *   <li>{@link #additionalAuthenticationChecks}：父类已确认账号存在且密码不匹配时才进入，
 *       在此累计连续失败次数，达到阈值即冻结账号并抛 {@link LockedException}。</li>
 * </ol>
 * "账号登录前就已被冻结"不再手写判断：{@link AdminDetails#isEnabled()} 返回 status==1，
 * 由父类的 DefaultPreAuthenticationChecks 自动抛出 DisabledException。
 */
@Slf4j
public class AdminAuthenticationProvider extends DaoAuthenticationProvider implements UserDetailsService {

    /** 账号状态：冻结 */
    private static final int STATUS_FROZEN = 0;

    private final AdminService adminService;
    private final AuthConfigService authConfigService;
    private final CaptchaService captchaService;

    public AdminAuthenticationProvider(AdminService adminService,
                                       AuthConfigService authConfigService,
                                       CaptchaService captchaService) {
        this.adminService = adminService;
        this.authConfigService = authConfigService;
        this.captchaService = captchaService;
        // UserDetails 来源就是自身（loadUserByUsername），不必再单独写一个 UserDetailsService 实现类
        setUserDetailsService(this);
    }

    /**
     * 按用户名加载管理员（UserDetailsService 契约）
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminService.getAdminByUsername(username);
        if (admin == null) {
            // 父类会把它转成 BadCredentialsException，避免暴露账号是否存在
            throw new UsernameNotFoundException("用户不存在!");
        }
        // 冻结账号不再在这里判断：由 AdminDetails#isEnabled 表达，
        // Spring Security 的 DefaultPreAuthenticationChecks 会自动抛 DisabledException
        return new AdminDetails(admin);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        checkCaptcha(authentication.getDetails());
        // 父类：成功返回已认证令牌，失败抛 AuthenticationException（不会返回 null）
        Authentication authenticated = super.authenticate(authentication);
        clearFailNum(authenticated);
        return authenticated;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        try {
            // 标准实现：交给注入的 PasswordEncoder 比对密文
            super.additionalAuthenticationChecks(userDetails, authentication);
        } catch (BadCredentialsException e) {
            throw countPasswordError((AdminDetails) userDetails, e);
        }
    }

    /**
     * 校验图形验证码
     */
    private void checkCaptcha(Object details) {
        if (!(details instanceof AdminLoginDetails) || !((AdminLoginDetails) details).hasCaptcha()) {
            throw new LoginFailException("图形验证码不能为空!");
        }
        AdminLoginDetails loginDetails = (AdminLoginDetails) details;
        captchaService.verify(loginDetails.getCaptchaKey(), loginDetails.getCaptchaCode());
    }

    /**
     * 登录成功：清空连续失败记录，避免历史计数累积到下次把账号误冻
     */
    private void clearFailNum(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof AdminDetails)) {
            return;
        }
        AdminDetails principal = (AdminDetails) authentication.getPrincipal();
        Integer failNum = principal.getFailNum();
        if (failNum == null || failNum <= 0) {
            return;
        }
        Admin admin = new Admin();
        admin.setAdminId(principal.getAdminId());
        admin.setFailNum(0);
        adminService.updateById(admin);
    }

    /**
     * 密码错误：累计连续失败次数，达到系统阈值即冻结账号
     *
     * @return 携带用户可见文案的认证异常（冻结 LockedException / 临界提醒 LoginFailException / 原凭证错误异常）
     */
    private AuthenticationException countPasswordError(AdminDetails principal, BadCredentialsException source) {
        AuthConfig authConfig = authConfigService.getById(1);
        int maxFail = authConfig.getFailNum() == null ? Integer.MAX_VALUE : authConfig.getFailNum();
        int windowSeconds = authConfig.getFailNumTime() == null ? 0 : authConfig.getFailNumTime();
        int history = principal.getFailNum() == null ? 0 : principal.getFailNum();

        // 距上次失败已超过系统设定的时间窗口 → 重新计数
        boolean windowPassed = principal.getLastTryLoginTime() == null
                || (DateUtil.getDaDate().getTime() - principal.getLastTryLoginTime().getTime()) / 1000 > windowSeconds;
        int failNum = windowPassed ? 1 : history + 1;

        Admin admin = new Admin();
        admin.setAdminId(principal.getAdminId());
        admin.setLastTryLoginIp(RequestUtil.getIp());
        admin.setLastTryLoginTime(DateUtil.getDaDate());
        admin.setFailNum(failNum);

        if (failNum >= maxFail) {
            admin.setStatus(STATUS_FROZEN);
            adminService.updateById(admin);
            log.warn("账号连续密码错误达上限被冻结 adminId={} failNum={}", principal.getAdminId(), failNum);
            return new LockedException("ALERT密码连续错误" + failNum + "次,账号已被冻结,请联系管理员解除!");
        }
        adminService.updateById(admin);
        if (maxFail - failNum == 1) {
            return new LoginFailException("ALERT密码已连续错误" + failNum + "次,再次输入错误将冻结账号!");
        }
        return source;
    }

    /**
     * 登录附加信息（对标 Spring Security 的 WebAuthenticationDetails）
     * <p>
     * 图形验证码不属于"身份凭证"，塞不进 Principal / Credentials，
     * 因此按 Spring Security 的惯例挂在 {@link Authentication#getDetails()} 上，
     * 由本 Provider 在认证开始前取出校验。仅登录一处使用，故作为内嵌类存在。
     */
    @Getter
    @RequiredArgsConstructor
    public static final class AdminLoginDetails {

        /** 验证码标识：getCaptcha 接口返回的 uuid */
        private final String captchaKey;
        /** 用户填写的验证码 */
        private final String captchaCode;

        /**
         * 验证码标识与内容都非空才算携带了验证码
         */
        public boolean hasCaptcha() {
            return StringUtils.hasText(captchaKey) && StringUtils.hasText(captchaCode);
        }
    }
}
