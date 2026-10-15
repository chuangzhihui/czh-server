package com.czh.admin;

import com.czh.service.dao.MenuDao;
import com.czh.service.entity.Menu;
import com.czh.service.entity.Role;
import com.czh.service.service.RoleService;
import com.czh.service.service.impl.MenuServiceImpl;
import com.czh.service.vo.admin.AuthMenuVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BE-30 回归：菜单树 N+1 递归查询。
 * <p>
 * 修复前 {@code getAuthMenuList(pid, ids, parentPath)} <b>每个菜单节点各查一次库</b>，
 * 递归下去 M 个节点 = M 次查询；而 {@code getAuthMenuByRoleId} 经由 {@code getLoginInfo}
 * 在<b>用户每次刷新页面</b>时都会触发，菜单一多直接成为首屏瓶颈。
 * <p>
 * 修复后改为「一次查全表 + 内存构树」。本用例验证两件事：
 * <ol>
 *   <li>对 DB 的查询次数从「节点数」降到 <b>1 次</b>；</li>
 *   <li>构树结果（结构、顺序、path 拼接）与旧递归实现<b>一致</b>。</li>
 * </ol>
 */
class MenuTreeN1RegressionTest {

    private MenuServiceImpl service;
    private MenuDao menuDao;

    @BeforeEach
    void setUp() {
        service = new MenuServiceImpl();
        menuDao = mock(MenuDao.class);
        ReflectionTestUtils.setField(service, "baseMapper", menuDao);
    }

    @Test
    @DisplayName("BE-30：只查一次库（一次查全表 + 内存构树，旧递归 mapper 已物理删除）")
    void shouldQueryDatabaseOnlyOnce() {
        givenRoleWithIds("[1,2,9]");
        when(menuDao.selectAuthMenuFlat(anyString())).thenReturn(flatMenus());

        service.getAuthMenuByRoleId(1);

        //旧递归方法 getAuthMenuList 已从 MenuDao 删除，"不再调用"由编译器保证，无需 verify(never())
        verify(menuDao, times(1)).selectAuthMenuFlat(anyString());
    }

    @Test
    @DisplayName("BE-30：树结构与 path 拼接与旧递归实现一致")
    void treeStructureShouldBeIdentical() {
        givenRoleWithIds("[1,2,5,6,9,14]");
        when(menuDao.selectAuthMenuFlat(anyString())).thenReturn(flatMenus());

        List<AuthMenuVo> tree = service.getAuthMenuByRoleId(1);

        assertEquals(1, tree.size(), "顶层只有一个 pid=0 的节点");
        AuthMenuVo root = tree.get(0);
        assertEquals(1, root.getId());
        assertEquals("系统设置", root.getTitle());
        assertEquals("System", root.getPath(), "顶层节点直接取自身 path");
        assertEquals("icon-jibenguanli", root.getIcon());

        // 子节点按 sort 升序：管理员列表(sort=1) 在 角色列表(sort=2) 之前
        assertEquals(3, root.getChild().size());
        assertEquals("管理员列表", root.getChild().get(0).getTitle());
        assertEquals("角色列表", root.getChild().get(1).getTitle());
        assertEquals("操作日志", root.getChild().get(2).getTitle());

        // path 拼接：父 + "/" + 自身
        assertEquals("System/AdminList", root.getChild().get(0).getPath());
        assertEquals("System/RoleList", root.getChild().get(1).getPath());

        // 第三层
        AuthMenuVo adminList = root.getChild().get(0);
        assertEquals(1, adminList.getChild().size());
        assertEquals("添加管理员", adminList.getChild().get(0).getTitle());
        assertEquals("System/AdminList/addAdmin", adminList.getChild().get(0).getPath());
    }

    @Test
    @DisplayName("BE-30：扁平列表中挂不上的孤儿节点不进树（与旧实现一致）")
    void orphanNodesShouldNotAppear() {
        givenRoleWithIds("[1]");
        // id=99 的 pid 指向一个不在列表里的父节点
        Menu orphan = new Menu(99, 999, "孤儿", "Orphan", "/admin/orphan", 3, "", 9, 1);
        when(menuDao.selectAuthMenuFlat(anyString())).thenReturn(Arrays.asList(menu(1, 0, "系统设置", "System", 1), orphan));

        List<AuthMenuVo> tree = service.getAuthMenuByRoleId(1);

        assertEquals(1, tree.size());
        assertEquals(1, tree.get(0).getId());
        assertEquals(0, tree.get(0).getChild().size());
    }

    @Test
    @DisplayName("BE-30：空结果返回空树，不抛异常")
    void emptyResultShouldReturnEmptyTree() {
        givenRoleWithIds("[]");
        when(menuDao.selectAuthMenuFlat(anyString())).thenReturn(new ArrayList<Menu>());

        List<AuthMenuVo> tree = service.getAuthMenuByRoleId(1);
        assertNotNull(tree);
        assertEquals(0, tree.size());
    }

    @Test
    @DisplayName("BE-30：角色不存在时仍按既有行为抛业务异常")
    void missingRoleShouldThrow() {
        RoleService roleService = mock(RoleService.class);
        when(roleService.getById(any())).thenReturn(null);
        ReflectionTestUtils.setField(service, "roleService", roleService);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.czh.common.exception.ErrorException.class,
                () -> service.getAuthMenuByRoleId(999)
        );
    }

    // ==================== 辅助 ====================

    private void givenRoleWithIds(String idsJson) {
        Role role = new Role();
        role.setRoleId(1);
        role.setIds(idsJson);
        RoleService roleService = mock(RoleService.class);
        when(roleService.getById(any())).thenReturn(role);
        ReflectionTestUtils.setField(service, "roleService", roleService);
    }

    private Menu menu(int id, int pid, String name, String path, int sort) {
        return new Menu(id, pid, name, path, "/admin/" + path.toLowerCase(), 2, "", sort, 1);
    }

    /**
     * 按 sql/czh.sql 里 czh_menu 的真实数据构造一份扁平列表（已按 sort 升序）。
     * 结构：系统设置(System) → 管理员列表(AdminList) → 添加管理员(addAdmin)
     *                       → 角色列表(RoleList)
     *                       → 操作日志(OperationLog)
     */
    private List<Menu> flatMenus() {
        Menu root = new Menu(1, 0, "系统设置", "System", "", 1, "icon-jibenguanli", 1, 1);
        Menu adminList = new Menu(5, 1, "管理员列表", "AdminList", "/admin/admin/adminList", 2, "", 1, 1);
        Menu addAdmin = new Menu(6, 5, "添加管理员", "addAdmin", "/admin/admin/addAdmin", 3, "", 0, 1);
        Menu roleList = new Menu(2, 1, "角色列表", "RoleList", "/admin/role/roleList", 2, "", 2, 1);
        Menu operationLog = new Menu(14, 1, "操作日志", "OperationLog", "/admin/admin/adminLog", 2, "", 6, 1);
        return Arrays.asList(root, adminList, addAdmin, roleList, operationLog);
    }
}
