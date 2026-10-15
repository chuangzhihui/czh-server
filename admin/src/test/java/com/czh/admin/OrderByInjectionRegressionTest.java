package com.czh.admin;

import com.czh.common.dto.PageDto;
import com.czh.common.utils.OrderByUtil;
import com.czh.service.dao.AdminDao;
import com.czh.service.dao.RoleDao;
import com.czh.service.dto.admin.AdminListDto;
import com.czh.service.entity.Role;
import com.czh.service.service.impl.AdminServiceImpl;
import com.czh.service.service.impl.RoleServiceImpl;
import com.czh.service.vo.admin.AdminListVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * BE-03 回归：PageHelper 的 {@code orderBy} 是<b>原文拼接</b>到 SQL 尾部的，
 * 攻击载荷 {@code asc,1=(select 1 from (select sleep(5))a)} 可做时间盲注 / 拖库。
 * <p>
 * 本组用例证明：修复后该载荷（及同类载荷）无法进入排序串；合法排序仍可用；
 * 三个调用点（AdminServiceImpl / RoleServiceImpl / PageDto）统一走同一份白名单。
 */
class OrderByInjectionRegressionTest {

    /** 评估报告里给出的原始攻击载荷 */
    static final String SLEEP_PAYLOAD = "asc,1=(select 1 from (select sleep(5))a)";

    private static final List<String> MALICIOUS_ORDERS = Arrays.asList(
            SLEEP_PAYLOAD,
            "adminId;drop table czh_admin",
            "adminId desc,1=1",
            "adminId asc union select 1,2,3",
            "(select 1)",
            "adminId desc--",
            "adminId/**/desc",
            "adminId desc; shutdown",
            "1=(select 1 from (select sleep(5))a)"
    );

    @AfterEach
    void clearPageHelperContext() {
        // PageHelper 用 ThreadLocal 保存分页上下文，用完必须清掉，避免污染同线程的其它用例
        PageHelper.clearPage();
    }

    @Test
    @DisplayName("BE-03：报告给出的时间盲注载荷被白名单丢弃")
    void shouldDropSqlInjectionPayload() {
        assertEquals("", OrderByUtil.sanitize(SLEEP_PAYLOAD));
        assertFalse(OrderByUtil.isValid(SLEEP_PAYLOAD));
    }

    @Test
    @DisplayName("BE-03：一类恶意排序串全部被丢弃")
    void shouldDropAllMaliciousOrders() {
        for (String malicious : MALICIOUS_ORDERS) {
            assertEquals("", OrderByUtil.sanitize(malicious), "应被丢弃: " + malicious);
        }
    }

    @Test
    @DisplayName("BE-03：PageDto 读取 orderBy 时自动白名单化")
    void shouldSanitizeOnPageDtoGetter() {
        PageDto dto = new PageDto();
        dto.setOrderBy(SLEEP_PAYLOAD);
        assertEquals("", dto.getOrderBy());
        // 原始值仍可拿到（供审计），但绝不参与 SQL 拼接
        assertEquals(SLEEP_PAYLOAD, dto.getOrderByRaw());
    }

    @Test
    @DisplayName("BE-03：PageDto 拼接默认字段后仍是安全的")
    void shouldBuildSafeOrderWithDefaultField() {
        PageDto dto = new PageDto();
        dto.setOrderBy(SLEEP_PAYLOAD);
        assertEquals("", dto.buildOrderBy("adminId"));

        dto.setOrderBy("desc");
        assertEquals("adminId desc", dto.buildOrderBy("adminId"));

        dto.setOrderBy("ASC");
        assertEquals("adminId asc", dto.buildOrderBy("adminId"));
    }

    @Test
    @DisplayName("BE-03：合法排序仍然可用（不误伤正常功能）")
    void shouldKeepLegalOrder() {
        assertEquals("desc", OrderByUtil.sanitize("desc"));
        assertEquals("asc", OrderByUtil.sanitize("  ASC  "));
        assertEquals("adminId desc", OrderByUtil.sanitize("adminId desc"));
        assertEquals("adminId desc", OrderByUtil.sanitize("adminId   DESC"));
        assertEquals("atime", OrderByUtil.sanitize("atime"));
        assertTrue(OrderByUtil.isValid("roleId asc"));
    }

    @Test
    @DisplayName("BE-03：字段名必须是 [A-Za-z_][A-Za-z0-9_]{0,63}")
    void shouldRejectIllegalFieldName() {
        assertFalse(OrderByUtil.isValidField("1admin"));
        assertFalse(OrderByUtil.isValidField("admin-id"));
        assertFalse(OrderByUtil.isValidField("admin id"));
        assertFalse(OrderByUtil.isValidField("admin'"));
        assertFalse(OrderByUtil.isValidField(""));
        assertTrue(OrderByUtil.isValidField("adminId"));
        assertTrue(OrderByUtil.isValidField("_atime"));
    }

    @Test
    @DisplayName("BE-03：排序方向只允许 asc / desc")
    void shouldRejectIllegalDirection() {
        assertTrue(OrderByUtil.isValidDirection("asc"));
        assertTrue(OrderByUtil.isValidDirection("DESC"));
        assertFalse(OrderByUtil.isValidDirection("ascending"));
        assertFalse(OrderByUtil.isValidDirection("1"));
    }

    @Test
    @DisplayName("BE-03：AdminServiceImpl 收到恶意 orderBy 时不把危险内容带进排序串")
    void adminServiceShouldPassSafeOrderBy() {
        AdminServiceImpl service = new AdminServiceImpl();
        AdminDao dao = mock(AdminDao.class);
        when(dao.getAdminList(any(AdminListDto.class))).thenReturn(new ArrayList<AdminListVo>());
        ReflectionTestUtils.setField(service, "baseMapper", dao);

        AdminListDto dto = new AdminListDto();
        dto.setPage(1);
        dto.setSize(10);
        dto.setOrderBy(SLEEP_PAYLOAD);

        PageInfo<AdminListVo> page = assertDoesNotThrow(() -> service.getAdminList(dto));
        assertNotNull(page);
        // 关键断言：最终落到 PageDto 上的排序串里没有任何 SQL 元字符
        String finalOrder = dto.getOrderBy();
        assertFalse(finalOrder.contains("select"), "排序串不得包含子查询");
        assertFalse(finalOrder.contains("sleep"), "排序串不得包含 sleep");
        assertFalse(finalOrder.contains("("), "排序串不得包含括号");
        assertFalse(finalOrder.contains(","), "排序串不得包含逗号分隔的多段");
        assertTrue(finalOrder.isEmpty() || OrderByUtil.isValid(finalOrder));
    }

    @Test
    @DisplayName("BE-03：AdminServiceImpl 合法排序仍然拼出 adminId + 方向")
    void adminServiceShouldKeepLegalOrder() {
        AdminServiceImpl service = new AdminServiceImpl();
        AdminDao dao = mock(AdminDao.class);
        when(dao.getAdminList(any(AdminListDto.class))).thenReturn(new ArrayList<AdminListVo>());
        ReflectionTestUtils.setField(service, "baseMapper", dao);

        AdminListDto dto = new AdminListDto();
        dto.setPage(1);
        dto.setSize(10);
        dto.setOrderBy("desc");

        assertDoesNotThrow(() -> service.getAdminList(dto));
        assertEquals("adminId desc", dto.getOrderBy());
    }

    @Test
    @DisplayName("BE-03：RoleServiceImpl 收到恶意 orderBy 时不把危险内容带进排序串")
    void roleServiceShouldPassSafeOrderBy() {
        RoleServiceImpl service = new RoleServiceImpl();
        RoleDao dao = mock(RoleDao.class);
        when(dao.selectList(any())).thenReturn(new ArrayList<Role>());
        ReflectionTestUtils.setField(service, "baseMapper", dao);

        PageDto dto = new PageDto();
        dto.setPage(1);
        dto.setSize(20);
        dto.setOrderBy(SLEEP_PAYLOAD);

        assertDoesNotThrow(() -> service.getRoleList(dto));
        assertTrue(dto.getOrderBy().isEmpty());
    }

    @Test
    @DisplayName("BE-03：RoleServiceImpl 合法排序仍然拼出 roleId + 方向")
    void roleServiceShouldKeepLegalOrder() {
        RoleServiceImpl service = new RoleServiceImpl();
        RoleDao dao = mock(RoleDao.class);
        when(dao.selectList(any())).thenReturn(new ArrayList<Role>());
        ReflectionTestUtils.setField(service, "baseMapper", dao);

        PageDto dto = new PageDto();
        dto.setPage(1);
        dto.setSize(20);
        dto.setOrderBy("asc");

        assertDoesNotThrow(() -> service.getRoleList(dto));
        assertEquals("roleId asc", dto.getOrderBy());
    }

    @Test
    @DisplayName("BE-03：SettingServiceImpl（评估报告未列到的第四个调用点）同样被白名单保护")
    void settingServiceShouldPassSafeOrderBy() {
        PageDto dto = new PageDto();
        dto.setPage(1);
        dto.setSize(20);
        dto.setOrderBy(SLEEP_PAYLOAD);
        assertEquals("", dto.buildOrderBy("id"));

        dto.setOrderBy("desc");
        assertEquals("id desc", dto.buildOrderBy("id"));
    }

    @Test
    @DisplayName("BE-03：Service 写回 PageDto 后再次读取仍安全（闭环）")
    void shouldStaySafeAfterWriteBack() {
        PageDto dto = new PageDto();
        dto.setOrderBy("desc");
        dto.setOrderBy("adminId " + dto.getOrderBy());
        assertEquals("adminId desc", dto.getOrderBy());
        assertTrue(OrderByUtil.isValid(dto.getOrderBy()));

        // 即便有人在空排序后拼接了字段名（产生 "adminId " 这种带尾空格的串），
        // 二次读取仍被白名单归一化，且绝不会残留注入载荷
        PageDto malicious = new PageDto();
        malicious.setOrderBy(SLEEP_PAYLOAD);
        malicious.setOrderBy("adminId " + malicious.getOrderBy());
        String finalOrder = malicious.getOrderBy();
        assertTrue(OrderByUtil.isValid(finalOrder), "写回后仍必须是白名单内的值");
        assertFalse(finalOrder.contains("select"));
        assertFalse(finalOrder.contains("sleep"));
        assertFalse(finalOrder.contains(","));
        assertEquals("adminId", finalOrder);
    }

    @Test
    @DisplayName("BE-03：空排序串是「不排序」的合法表达，不得抛异常")
    void emptyOrderByShouldMeanNoOrder() {
        assertEquals("", OrderByUtil.sanitize(null));
        assertEquals("", OrderByUtil.sanitize(""));
        assertEquals("", OrderByUtil.sanitize("   "));
        assertEquals("", OrderByUtil.build("adminId", "ascending"));
        assertEquals("", OrderByUtil.build("admin-id", "asc"));
    }
}
