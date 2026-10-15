package com.czh.api;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 容器启动冒烟测试。
 * <p>
 * ⚠️ 已禁用，但<b>它不是废弃测试</b> —— CI 具备条件后应当重新启用，方法同
 * {@code AdminApplicationTests}：删掉 {@code @Disabled} 或改为
 * {@code @EnabledIfEnvironmentVariable(named = "CZH_IT_DB", matches = ".+")}，
 * 前提是先具备 MySQL + Redis 服务（密钥已直接配置在 application*.yaml，无需额外注入）。
 * <p>
 * <b>为什么禁用</b>：同 {@code AdminApplicationTests} —— 本机没有 MySQL / Redis，
 * 直接跑会让整个 {@code mvn test} 失败，进而掩盖真正的缺陷回归用例。
 */
@Disabled("需要真实 MySQL+Redis；"
        + "CI 具备后按类注释改为 @EnabledIfEnvironmentVariable(named=\"CZH_IT_DB\", matches=\".+\")")
@SpringBootTest
class ApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
