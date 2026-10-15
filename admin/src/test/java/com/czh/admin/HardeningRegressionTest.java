package com.czh.admin;

import com.czh.admin.baseController.UploadController;
import com.czh.common.exception.ErrorException;
import com.czh.service.entity.Menu;
import com.czh.service.service.impl.MenuServiceImpl;
import com.czh.service.vo.admin.AuthMenuVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * 本轮加固修复的回归用例（BE-07 / B-03）。
 * <p>
 * 关闭标准沿用《项目缺陷评估报告》7.6：安全相关修复必须证明「旧 payload 现在失败」，
 * 而不是「代码看起来改了」。两条用例分别对应：
 * <ul>
 *   <li>BE-07：五家存储分支统一的 type→扩展名白名单——白名单外的扩展名（如 exe）必须被拒；</li>
 *   <li>B-03：菜单树内存排序兜底——即使 SQL 丢了 ORDER BY，兄弟节点顺序也不漂移。</li>
 * </ul>
 * 注：BE-06 的密钥启动自检用例已随「放弃环境变量模式」决策移除——独立密钥机制保留，
 * 但默认值直接写在 admin/application.yaml，不再要求 fail-closed 启动。
 */
class HardeningRegressionTest {

    // ==================== BE-07：上传扩展名白名单 ====================

    private UploadController controllerWithVisibleUploadSet() {
        UploadController controller = new UploadController();
        // uploadSetService 在 BaseController 里注入，测试里用反射塞 mock
        com.czh.service.service.UploadSetService uploadSetService = mock(com.czh.service.service.UploadSetService.class);
        com.czh.service.entity.UploadSet set = new com.czh.service.entity.UploadSet();
        set.setVisible(1);// 七牛分支：白名单校验发生在 switch 之前，不会真正触达第三方 SDK
        org.mockito.Mockito.when(uploadSetService.getById(1)).thenReturn(set);
        ReflectionTestUtils.setField(controller, "uploadSetService", uploadSetService);
        return controller;
    }

    private MockMultipartFile fileOf(String filename) {
        return new MockMultipartFile("file", filename, "application/octet-stream", new byte[]{1, 2, 3});
    }

    @Test
    @DisplayName("BE-07：type=1(图片) 上传 exe 必须被拒——白名单外的扩展名不再放行")
    void exeMustBeRejectedForImageType() {
        UploadController controller = controllerWithVisibleUploadSet();

        ErrorException error = assertThrows(ErrorException.class,
                () -> controller.upload(fileOf("evil.exe"), "file", 1),
                "type=1 时 exe 不在图片白名单内，必须拒绝（旧实现五家云分支任意扩展名都能传）");
        assertTrue(error.getMessage().contains("不支持上传此类文件"),
                "拒绝原因必须是扩展名白名单拦截，实际: " + error.getMessage());
    }

    @Test
    @DisplayName("BE-07：type=1(图片) 上传 7z 也必须被拒（合法扩展名但类型不符）")
    void archiveExtMustBeRejectedForImageType() {
        UploadController controller = controllerWithVisibleUploadSet();

        ErrorException error = assertThrows(ErrorException.class,
                () -> controller.upload(fileOf("a.7z"), "file", 1),
                "7z 属于压缩包白名单（type=6），放到 type=1 下同样必须拒绝");
        assertTrue(error.getMessage().contains("不支持上传此类文件"),
                "实际: " + error.getMessage());
    }

    @Test
    @DisplayName("BE-07：type=7(未知类型文件) 是业务兜底，不做白名单限制（保持既有语义）")
    void unknownTypeKeepsLegacyBehavior() {
        UploadController controller = controllerWithVisibleUploadSet();

        //type=7 无白名单 → 不会抛"不支持上传此类文件"；后续分支行为不在本用例范围
        Throwable error = assertThrows(Throwable.class,
                () -> controller.upload(fileOf("data.dat"), "file", 7));
        assertTrue(!(error instanceof ErrorException && error.getMessage().contains("不支持上传此类文件")),
                "type=7 不应被白名单拦截（业务兜底语义），实际: " + error.getMessage());
    }

    // ==================== B-03：菜单树内存排序兜底 ====================

    private Menu menuOf(int id, int pid, String name, int sort) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setPid(pid);
        menu.setName(name);
        menu.setSort(sort);
        return menu;
    }

    @Test
    @DisplayName("B-03：传入乱序列表（模拟 SQL 丢了 ORDER BY），兄弟节点顺序仍必须按 sort 升序")
    void treeShouldBeSortedBySortFieldEvenWhenSqlOrderMissing() {
        MenuServiceImpl service = new MenuServiceImpl();

        //故意乱序：A(sort=30) B(sort=10) C(sort=20)
        List<Menu> flat = new ArrayList<>(Arrays.asList(
                menuOf(10, 0, "A", 30),
                menuOf(11, 0, "B", 10),
                menuOf(12, 0, "C", 20)));

        List<AuthMenuVo> tree = ReflectionTestUtils.invokeMethod(
                service, "buildAuthMenuTree", flat, 0, "");

        assertEquals(3, tree.size(), "三个一级节点都应进树");
        assertEquals("B", tree.get(0).getTitle(), "sort=10 的 B 必须排第一");
        assertEquals("C", tree.get(1).getTitle(), "sort=20 的 C 必须排第二");
        assertEquals("A", tree.get(2).getTitle(), "sort=30 的 A 必须排第三");
    }

    @Test
    @DisplayName("B-03：sort 为 null 的节点排在最后，且不得抛 NPE")
    void nullSortMustNotBreakSorting() {
        MenuServiceImpl service = new MenuServiceImpl();

        Menu nullSort = menuOf(13, 0, "N", 0);
        nullSort.setSort(null);
        List<Menu> flat = new ArrayList<>(Arrays.asList(
                menuOf(10, 0, "A", 5),
                nullSort,
                menuOf(12, 0, "C", 1)));

        List<AuthMenuVo> tree = ReflectionTestUtils.invokeMethod(
                service, "buildAuthMenuTree", flat, 0, "");

        assertEquals(3, tree.size());
        assertEquals("C", tree.get(0).getTitle(), "sort=1 的 C 排第一");
        assertEquals("A", tree.get(1).getTitle(), "sort=5 的 A 排第二");
        assertEquals("N", tree.get(2).getTitle(), "sort=null 的 N 兜底排最后");
    }
}
