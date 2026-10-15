package com.czh.admin;

import com.alibaba.fastjson.JSON;
import com.czh.admin.baseController.setting.SettingController;
import com.czh.service.dao.UploadSetDao;
import com.czh.service.dto.admin.QiniuDto;
import com.czh.service.entity.UploadSet;
import com.czh.service.service.UploadSetService;
import com.czh.service.vo.admin.GetUploadConfigVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BE-05 的<b>契约级</b>验收（补齐 {@code UploadConfigMaskRegressionTest} 未覆盖的一层）。
 * <p>
 * 已有的 {@code UploadConfigMaskRegressionTest} 只反射调用了 {@code maskSecret} / {@code isMaskedOrEmpty}
 * 两个私有方法 —— 它能证明"这两个函数写对了"，但<b>证明不了它们被真正接到了链路上</b>：
 * 万一 {@code getUploadConfig} 漏掉了火山 TOS 的 {@code secretKey}，或者 {@code saveTos}
 * 忘了调 {@code isMaskedOrEmpty}，那组用例依然是全绿的。
 * <p>
 * 7.6 的关闭标准写的是「调用 {@code GET /admin/setting/getUploadConfig} 后检查<b>响应体</b>」
 * 与「再原样保存回去，<b>库里的真值不得被 **** 覆盖</b>」，因此本组用例直接走 Controller 出入口：
 * <ol>
 *   <li>响应体序列化后不得残留任何一家云存储的明文密钥；</li>
 *   <li>把响应体原样回传保存（模拟"打开页面→直接保存"），落库的必须是库里的真值，不得是 {@code ****}。</li>
 * </ol>
 */
class UploadConfigContractRegressionTest {

    /** 四家云存储各自的真实密钥（测试数据，非真实凭据） */
    private static final String QINIU_SECRET = "qiniu-real-secret-K7MDENG-bx";
    private static final String ALI_SK = "alioss-real-sk-abcdefghijkl";
    private static final String TX_SK = "txcos-real-sk-mnopqrstuvwx";
    private static final String TOS_SECRET = "tos-real-secret-yz0123456789";

    private SettingController controller;
    private UploadSetService uploadSetService;
    private UploadSetDao uploadSetDao;

    @BeforeEach
    void setUp() {
        controller = new SettingController();
        uploadSetService = mock(UploadSetService.class);
        uploadSetDao = mock(UploadSetDao.class);
        when(uploadSetService.getBaseMapper()).thenReturn(uploadSetDao);
        when(uploadSetService.updateById(any(UploadSet.class))).thenReturn(true);
        ReflectionTestUtils.setField(controller, "uploadSetService", uploadSetService);
    }

    @Test
    @DisplayName("BE-05【关键】：getUploadConfig 的响应体不得残留任何云存储明文密钥")
    void responseBodyMustNotContainPlainSecret() {
        when(uploadSetDao.selectById(1)).thenReturn(buildUploadSet());

        GetUploadConfigVo vo = controller.getUploadConfig().data;
        String body = JSON.toJSONString(vo);

        assertFalse(body.contains(QINIU_SECRET), "七牛 secretKey 明文不得出现在响应体: " + body);
        assertFalse(body.contains(ALI_SK), "阿里云 sk 明文不得出现在响应体: " + body);
        assertFalse(body.contains(TX_SK), "腾讯云 sk 明文不得出现在响应体: " + body);
        assertFalse(body.contains(TOS_SECRET), "火山 TOS secretKey 明文不得出现在响应体: " + body);

        // 非密钥字段仍必须正常下发，否则配置页会变成一片空白
        assertTrue(body.contains("qiniu-bucket"), "bucket 等非敏感字段必须照常下发");
        assertTrue(body.contains("oss-cn-hangzhou"), "endpoint 等非敏感字段必须照常下发");
    }

    @Test
    @DisplayName("BE-05【关键】：响应体里四家的密钥字段都必须是掩码值（逐家核对，防止漏配一家）")
    void everyProviderSecretMustBeMasked() {
        when(uploadSetDao.selectById(1)).thenReturn(buildUploadSet());

        GetUploadConfigVo vo = controller.getUploadConfig().data;

        assertTrue(vo.getQiniu().getSecretKey().endsWith("****"), "七牛 secretKey 未掩码");
        assertTrue(vo.getQiniu().getAccessKey().endsWith("****"), "七牛 accessKey 未掩码");
        assertTrue(vo.getAlioss().getSk().endsWith("****"), "阿里云 sk 未掩码");
        assertTrue(vo.getAlioss().getAk().endsWith("****"), "阿里云 ak 未掩码");
        assertTrue(vo.getTxcos().getSk().endsWith("****"), "腾讯云 sk 未掩码");
        assertTrue(vo.getTxcos().getAk().endsWith("****"), "腾讯云 ak 未掩码");
        assertTrue(vo.getTos().getSecretKey().endsWith("****"), "火山 TOS secretKey 未掩码");
        assertTrue(vo.getTos().getAccessKey().endsWith("****"), "火山 TOS accessKey 未掩码");
    }

    @Test
    @DisplayName("BE-05【关键】：把掩码值原样保存回去，库里的真值不得被 **** 覆盖")
    void savingMaskedValueBackMustKeepRealSecret() {
        when(uploadSetDao.selectById(1)).thenReturn(buildUploadSet());

        // 模拟前端"打开配置页 → 什么都不改 → 直接保存"
        GetUploadConfigVo vo = controller.getUploadConfig().data;
        QiniuDto dto = new QiniuDto();
        dto.setAccessKey(vo.getQiniu().getAccessKey());
        dto.setSecretKey(vo.getQiniu().getSecretKey());
        dto.setBucket(vo.getQiniu().getBucket());
        dto.setVisible(1);

        controller.saveQiniu(dto);

        ArgumentCaptor<UploadSet> captor = ArgumentCaptor.forClass(UploadSet.class);
        verify(uploadSetService).updateById(captor.capture());
        String persisted = captor.getValue().getQiniu();

        assertFalse(persisted.contains("****"), "库里不得被写入掩码值 " + persisted);
        assertTrue(persisted.contains(QINIU_SECRET), "未改动的密钥必须沿用库里真值，实际落库: " + persisted);
    }

    @Test
    @DisplayName("BE-05：用户填入全新密钥时必须被真正写库（防掩码逻辑误伤正常修改）")
    void newRealSecretMustBePersisted() {
        when(uploadSetDao.selectById(1)).thenReturn(buildUploadSet());

        QiniuDto dto = new QiniuDto();
        dto.setAccessKey("qiniu-new-ak-99999999");
        dto.setSecretKey("qiniu-new-sk-88888888");
        dto.setBucket("qiniu-bucket");
        dto.setVisible(1);

        controller.saveQiniu(dto);

        ArgumentCaptor<UploadSet> captor = ArgumentCaptor.forClass(UploadSet.class);
        verify(uploadSetService).updateById(captor.capture());
        String persisted = captor.getValue().getQiniu();

        assertTrue(persisted.contains("qiniu-new-sk-88888888"), "新密钥必须写库，实际: " + persisted);
        assertFalse(persisted.contains(QINIU_SECRET), "旧密钥必须被覆盖，实际: " + persisted);
    }

    @Test
    @DisplayName("BE-05：空值回传（前端清空输入框后保存）同样沿用旧值，不得把库写成空")
    void emptySecretMustKeepOldValue() {
        when(uploadSetDao.selectById(1)).thenReturn(buildUploadSet());

        QiniuDto dto = new QiniuDto();
        dto.setAccessKey("");
        dto.setSecretKey("   ");
        dto.setBucket("qiniu-bucket");
        dto.setVisible(1);

        controller.saveQiniu(dto);

        ArgumentCaptor<UploadSet> captor = ArgumentCaptor.forClass(UploadSet.class);
        verify(uploadSetService).updateById(captor.capture());
        String persisted = captor.getValue().getQiniu();

        assertTrue(persisted.contains(QINIU_SECRET), "空值应视为未改动并沿用旧值，实际: " + persisted);
    }

    private UploadSet buildUploadSet() {
        UploadSet set = new UploadSet();
        set.setId(1);
        set.setVisible(1);
        set.setQiniu("{\"accessKey\":\"qiniu-real-ak-ZXCVBN\",\"secretKey\":\"" + QINIU_SECRET
                + "\",\"bucket\":\"qiniu-bucket\",\"endpoint\":\"oss-cn-hangzhou\",\"domain\":\"https://q.example.com\"}");
        set.setAlioss("{\"ak\":\"LTAI5tRealAk\",\"sk\":\"" + ALI_SK
                + "\",\"bucket\":\"ali-bucket\",\"endpoint\":\"oss-cn-hangzhou\",\"domain\":\"https://a.example.com\"}");
        set.setTxcos("{\"ak\":\"AKIDRealTx\",\"sk\":\"" + TX_SK
                + "\",\"bucket\":\"tx-bucket\",\"endpoint\":\"oss-cn-hangzhou\",\"domain\":\"https://t.example.com\"}");
        set.setTos("{\"accessKey\":\"TOSRealAk\",\"secretKey\":\"" + TOS_SECRET
                + "\",\"bucket\":\"tos-bucket\",\"endpoint\":\"oss-cn-hangzhou\",\"domain\":\"https://o.example.com\"}");
        set.setLocal("{\"host\":\"https://local.example.com\"}");
        return set;
    }

    @Test
    @DisplayName("BE-05：getUploadConfig 的返回结构本身不得为 null（防 NPE 反噬）")
    void responseMustBeNonNull() {
        when(uploadSetDao.selectById(1)).thenReturn(buildUploadSet());
        GetUploadConfigVo vo = controller.getUploadConfig().data;
        assertTrue(vo.getQiniu() != null && vo.getAlioss() != null
                && vo.getTxcos() != null && vo.getTos() != null);
        assertEquals(Integer.valueOf(1), vo.getVisible());
    }
}
