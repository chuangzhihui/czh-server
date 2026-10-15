package com.czh.admin;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.czh.common.exception.ErrorException;
import com.czh.service.service.impl.DbBackupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BE-02 回归：数据库备份/恢复命令。
 * <p>
 * 修复前：
 * <ul>
 *   <li>{@code log.info("command:" + command)} 把含明文数据库密码的完整命令行写进日志；</li>
 *   <li>{@code Runtime.getRuntime().exec(command)} 用字符串交给 shell 解析，存在命令注入面。</li>
 * </ul>
 * 修复后：命令行参数化（ProcessBuilder + List），密码只通过 {@code MYSQL_PWD} 环境变量传递，
 * 库名 / 主机 / 端口走白名单。
 */
class DbBackupCommandRegressionTest {

    /**
     * 测试数据：<b>刻意不使用仓库历史里那份真实生产密码</b>。
     * 那份密码已在 git 历史中泄露（见 BE-01 遗留问题），把它再抄进测试代码没有任何收益，
     * 只会让"哪些地方还写着真密码"这件事更难查。本常量只需是一个"会被拿来比对的字符串"。
     */
    private static final String DB_PASSWORD = "QaTestOnly-DbPassword-0a1b2c3d";
    private static final String DB_NAME = "czh_db";
    private static final String DB_HOST = "127.0.0.1";
    private static final String DB_PORT = "3306";
    private static final String DB_USER = "czh";

    private DbBackupServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DbBackupServiceImpl();
        ReflectionTestUtils.setField(service, "mysqlPassword", DB_PASSWORD);
        ReflectionTestUtils.setField(service, "mysqlDatabase", DB_NAME);
        ReflectionTestUtils.setField(service, "mysqlHost", DB_HOST);
        ReflectionTestUtils.setField(service, "mysqlPort", DB_PORT);
        ReflectionTestUtils.setField(service, "mysqlUsername", DB_USER);
        ReflectionTestUtils.setField(service, "backupPath", "/tmp/czh-backup/");
    }

    @Test
    @DisplayName("BE-02：备份命令行不再包含数据库密码")
    void backupCommandShouldNotContainPassword() {
        ProcessBuilder pb = invokeBuild("mysqldump", Arrays.asList("-r", "/tmp/czh-backup/mysql.sql", "--no-tablespaces"));

        List<String> command = pb.command();
        assertFalse(containsPassword(command), "命令行参数里不得出现密码，实际: " + command);
        assertFalse(command.toString().contains("-p" + DB_PASSWORD), "不得再使用 -p<密码> 形式");
    }

    @Test
    @DisplayName("BE-02：恢复命令行不再包含数据库密码")
    void restoreCommandShouldNotContainPassword() {
        ProcessBuilder pb = invokeBuild("mysql", Collections.emptyList());

        List<String> command = pb.command();
        assertFalse(containsPassword(command), "命令行参数里不得出现密码，实际: " + command);
        assertEquals("mysql", command.get(0));
    }

    @Test
    @DisplayName("BE-02：密码只通过 MYSQL_PWD 环境变量传递")
    void passwordShouldBePassedByEnvOnly() {
        ProcessBuilder pb = invokeBuild("mysqldump", Collections.emptyList());

        Map<String, String> env = pb.environment();
        assertEquals(DB_PASSWORD, env.get("MYSQL_PWD"));
    }

    @Test
    @DisplayName("BE-02：命令改为参数数组，不再交给 shell 解析")
    void commandShouldBeArgumentArray() {
        ProcessBuilder pb = invokeBuild("mysqldump", Arrays.asList("-r", "/tmp/x.sql", "--no-tablespaces"));

        List<String> command = pb.command();
        assertEquals("mysqldump", command.get(0));
        assertEquals("-h", command.get(1));
        assertEquals(DB_HOST, command.get(2));
        assertEquals("-P", command.get(3));
        assertEquals(DB_PORT, command.get(4));
        assertEquals("-u", command.get(5));
        assertEquals(DB_USER, command.get(6));
        assertEquals(DB_NAME, command.get(7));
        assertTrue(command.contains("--no-tablespaces"));
        // 任何一个参数都不得含 shell 元字符
        command.forEach(arg -> {
            assertFalse(arg.contains(";"), "参数含分号: " + arg);
            assertFalse(arg.contains("|"), "参数含管道: " + arg);
            assertFalse(arg.contains("&&"), "参数含 &&: " + arg);
        });
    }

    @Test
    @DisplayName("BE-02：库名走白名单，非法库名直接拒绝")
    void illegalDatabaseNameShouldBeRejected() {
        ReflectionTestUtils.setField(service, "mysqlDatabase", "czh_db;rm -rf /");
        assertThrows(ErrorException.class, () -> invokeBuild("mysqldump", Collections.emptyList()));

        ReflectionTestUtils.setField(service, "mysqlDatabase", "czh db");
        assertThrows(ErrorException.class, () -> invokeBuild("mysqldump", Collections.emptyList()));
    }

    @Test
    @DisplayName("BE-02：主机与端口走白名单")
    void illegalHostOrPortShouldBeRejected() {
        ReflectionTestUtils.setField(service, "mysqlHost", "127.0.0.1;cat /etc/passwd");
        assertThrows(ErrorException.class, () -> invokeBuild("mysqldump", Collections.emptyList()));

        ReflectionTestUtils.setField(service, "mysqlHost", DB_HOST);
        ReflectionTestUtils.setField(service, "mysqlPort", "3306;shutdown");
        assertThrows(ErrorException.class, () -> invokeBuild("mysqldump", Collections.emptyList()));
    }

    @Test
    @DisplayName("BE-02：未配置用户名/库名时直接拒绝，不拼出残缺命令")
    void missingConfigShouldBeRejected() {
        ReflectionTestUtils.setField(service, "mysqlUsername", "  ");
        assertThrows(ErrorException.class, () -> invokeBuild("mysqldump", Collections.emptyList()));
    }

    /**
     * BE-02 的日志脱敏验收。
     * <p>
     * ⚠️ 本用例的<b>旧写法是无效断言</b>：它自己在测试里拼了一行
     * {@code "执行 mysqldump 备份 db=... 参数个数=..."} 再断言这行不含密码 ——
     * 字符串由测试自己构造、按构造就不含密码，因此无论生产代码怎么写它都是绿的，
     * 对 7.6「日志中不含 command: 与 -p&lt;密码&gt;」这条关闭标准零证明力。
     * <p>
     * 现改为：挂 ListAppender 抓<b>真实打出来的日志</b>，走真实的 {@code backupMysql()} 路径
     * （本机无 mysqldump，进程启动时 IOException → ErrorException，登录动作发生在启动前），
     * 再逐条断言真实日志里没有密码 / {@code command:} / {@code -p<密码>}。
     */
    @Test
    @DisplayName("BE-02：真实日志输出不含密码 / command: / -p<密码>（抓真实日志，不是自己拼字符串）")
    void realLogOutputShouldNotLeakPassword() {
        Logger logger = (Logger) LoggerFactory.getLogger(DbBackupServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            // 走真实备份路径：本机没有 mysqldump，会在 ProcessBuilder.start() 处失败，
            // 不会真的连库、不会写任何文件；而脱敏日志在 start() 之前就已打出。
            assertThrows(ErrorException.class, () ->
                    ReflectionTestUtils.invokeMethod(service, "backupMysql"));

            List<ILoggingEvent> events = appender.list;
            assertFalse(events.isEmpty(), "备份路径必须留下日志；若为空则本断言无意义");

            String joined = events.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .reduce("", (a, b) -> a + "\n" + b);

            assertFalse(joined.contains(DB_PASSWORD), "日志泄露了数据库密码: " + joined);
            assertFalse(joined.contains("command:"), "日志仍在打印完整命令行（command:）: " + joined);
            assertFalse(joined.contains("-p" + DB_PASSWORD), "日志仍在打印 -p<密码>: " + joined);
            assertTrue(joined.contains("mysqldump"), "日志应保留脱敏摘要（程序名），实际: " + joined);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    @DisplayName("BE-02：恢复路径的真实日志同样不含密码")
    void restoreLogOutputShouldNotLeakPassword() {
        Logger logger = (Logger) LoggerFactory.getLogger(DbBackupServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            assertThrows(ErrorException.class, () ->
                    ReflectionTestUtils.invokeMethod(service, "restoreMysql", "/tmp/not-exists.sql"));

            String joined = appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .reduce("", (a, b) -> a + "\n" + b);

            assertFalse(joined.contains(DB_PASSWORD), "恢复日志泄露了数据库密码: " + joined);
            assertFalse(joined.contains("command:"), "恢复日志仍在打印完整命令行: " + joined);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private ProcessBuilder invokeBuild(String program, List<String> extraArgs) {
        return ReflectionTestUtils.invokeMethod(service, "buildMysqlProcess", program, extraArgs);
    }

    private boolean containsPassword(List<String> command) {
        for (String arg : command) {
            if (arg.contains(DB_PASSWORD)) {
                return true;
            }
        }
        return false;
    }
}
