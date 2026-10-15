package com.czh.service.cache;

/**
 * 鉴权相关 Redis key 与 TTL 的集中定义。
 * <p>
 * 集中在这里是为了让「谁在读、谁在失效」可枚举：
 * 新增缓存必须在此登记 key 前缀，并同步到对应 Service 的失效方法。
 *
 * @author czh
 */
public final class AuthCacheKeys {

    private AuthCacheKeys() {
    }

    /** 管理员鉴权缓存前缀，完整 key = 前缀 + adminId */
    public static final String ADMIN_PREFIX = "adminAuth:";

    /** 登录安全配置缓存 key（AuthConfig 只有 id=1 一行） */
    public static final String AUTH_CONFIG = "authConfig:1";

    /** 角色权限缓存前缀，完整 key = 前缀 + roleId（由 RoleServiceImpl 维护） */
    public static final String ROLE_PERMISSION_PREFIX = "rolePermission";

    /**
     * 角色权限缓存的「已缓存角色」索引集合。
     * <p>
     * 用途：改菜单 route 时要失效所有角色的权限串，但又不知道哪些角色引用了它。
     * 这里登记已写入缓存的 roleId，清理时枚举索引集合即可，
     * <b>避免用 {@code keys rolePermission*} 全库扫描（会阻塞 Redis 主线程）</b>。
     */
    public static final String ROLE_PERMISSION_INDEX = "rolePermissionIndex";

    /**
     * 角色权限缓存存活时间（秒）。
     * <p>
     * 正常路径已能即时失效：改菜单 route 由 MenuServiceImpl 触发全量清理，
     * 改角色权限由 RoleService#updateRoleCache 触发单个清理。
     * TTL 是双保险，覆盖索引集合丢失（Redis 重启 / flush）等边缘情况。
     */
    public static final long ROLE_PERMISSION_TTL_SECONDS = 300L;

    /**
     * 管理员缓存存活时间（秒）。
     * <p>
     * 定 60 秒的依据：所有写操作都会主动删除，TTL 只是兜底，
     * 覆盖「不经过 Service 的数据变更」（例如运维直连数据库改库、跑脚本刷数据）。
     * 这个值决定了这类旁路修改最迟多久生效。
     */
    public static final long ADMIN_TTL_SECONDS = 60L;

    /**
     * 安全配置缓存存活时间（秒）。
     * <p>
     * AuthConfig 只影响 token 续期时长，配错不会造成越权，
     * 所以给 5 分钟，容忍度比管理员高。
     */
    public static final long AUTH_CONFIG_TTL_SECONDS = 300L;
}
