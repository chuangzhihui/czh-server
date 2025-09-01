-- MySQL dump 10.13  Distrib 8.0.36, for Linux (x86_64)
--
-- Host: localhost    Database: jichu
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `czh_admin`
--

DROP TABLE IF EXISTS `czh_admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `czh_admin` (
  `adminId` int NOT NULL AUTO_INCREMENT COMMENT '管理员id',
  `userName` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(60) NOT NULL COMMENT '密码',
  `lastLoginIp` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '最后登录ip',
  `lastLoginTime` datetime DEFAULT NULL COMMENT '最后登录时间',
  `roleId` int NOT NULL COMMENT '角色id',
  `atime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '账号状态 0冻结 1正常',
  `lastTryLoginIp` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '上次登录失败的IP',
  `lastTryLoginTime` datetime DEFAULT NULL COMMENT '上次登录失败的时间',
  `failNum` int NOT NULL DEFAULT '0' COMMENT '连续登录失败次数',
  `lastChangePwdTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上次修改密码的时间',
  PRIMARY KEY (`adminId`)
) ENGINE=MyISAM AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb3 COMMENT='管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `czh_admin`
--

LOCK TABLES `czh_admin` WRITE;
/*!40000 ALTER TABLE `czh_admin` DISABLE KEYS */;
INSERT INTO `czh_admin` VALUES (4,'admin','$2y$10$1ldsbxEi.A3FWBjtqJMVKuZ5V8N5nvX1gz7qp05mNKN9zGKmelvMG','220.192.129.27','2025-06-03 23:18:09',1,'2021-12-12 22:43:58','https://qiniu.datangchaochong.cn/update/17419209848886409461.png',1,'27.8.8.179','2025-08-25 16:52:41',1,'2025-08-25 17:38:55'),(7,'ceshi21','$2y$10$aivg0DoXU7uleKJQrydouuo9ArT5AgJzee3PDrm6fzOyQfTGb.biS',NULL,NULL,1,'2024-10-31 10:22:16',NULL,1,NULL,NULL,0,NULL);
/*!40000 ALTER TABLE `czh_admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `czh_auth_config`
--

DROP TABLE IF EXISTS `czh_auth_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `czh_auth_config` (
  `id` int NOT NULL,
  `pwdReg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码正则',
  `failNum` int NOT NULL COMMENT '连续密码错误次数后冻结账号',
  `failNumTime` int NOT NULL COMMENT '清楚密码错误记录的时间(秒)',
  `pwdRegDesc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码正则描述',
  `timeOut` int NOT NULL COMMENT '静默多久后登录失效(秒)',
  `passMax` int NOT NULL COMMENT '密码多少天后必须强制更换',
  `passWran` int NOT NULL COMMENT '多少天开始提示用户修改密码',
  `autoBackup` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否开启自动备份',
  `bakupDbCron` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '备份数据得cron表达式',
  `atime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上次编辑时间',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录安全配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `czh_auth_config`
--

LOCK TABLES `czh_auth_config` WRITE;
/*!40000 ALTER TABLE `czh_auth_config` DISABLE KEYS */;
INSERT INTO `czh_auth_config` VALUES (1,'^(?![\\d]+$)(?![a-z]+$)(?![A-Z]+$)[\\da-zA-z]{8,16}$',5,600,'允许数字、大小写字母、但至少包含其中两种，且长度在8-16之间',3600,30,20,1,'0 30 1 * * ?','2025-06-12 15:42:02');
/*!40000 ALTER TABLE `czh_auth_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `czh_db_backup`
--

DROP TABLE IF EXISTS `czh_db_backup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `czh_db_backup` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fileName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '备份文件名称',
  `adminId` int NOT NULL COMMENT '备份人',
  `filePath` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备份文件完整路径',
  `fileSize` int NOT NULL DEFAULT '0' COMMENT '文件大小',
  `atime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '备份时间',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据库备份记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `czh_db_backup`
--

LOCK TABLES `czh_db_backup` WRITE;
/*!40000 ALTER TABLE `czh_db_backup` DISABLE KEYS */;
INSERT INTO `czh_db_backup` VALUES (1,'20250615233535.zip',4,'/home/www/java_demo/backup/20250615233535.zip',6004,'2025-06-15 23:35:35');
/*!40000 ALTER TABLE `czh_db_backup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `czh_menu`
--

DROP TABLE IF EXISTS `czh_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `czh_menu` (
  `id` int NOT NULL AUTO_INCREMENT,
  `pid` int DEFAULT NULL COMMENT '上级id',
  `name` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '菜单名称',
  `path` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '前端路由',
  `route` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '后端路由',
  `level` tinyint(1) DEFAULT '1' COMMENT '菜单等级',
  `icon` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '图标',
  `sort` int DEFAULT NULL COMMENT '排序 低到高',
  `display` tinyint(1) DEFAULT '1' COMMENT '是否显示',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=100 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `czh_menu`
--

LOCK TABLES `czh_menu` WRITE;
/*!40000 ALTER TABLE `czh_menu` DISABLE KEYS */;
INSERT INTO `czh_menu` VALUES (1,0,'系统设置','','',1,'icon-jibenguanli',2,1),(2,1,'角色列表','RoleList','/admin/role/roleList',2,'',2,1),(5,1,'管理员列表','AdminList','/admin/admin/adminList',2,'',1,1),(6,5,'添加管理员','','/admin/admin/addAdmin',3,'',0,0),(9,2,'添加角色','','/admin/role/addRole',3,'',2,0),(10,2,'编辑角色','','/admin/role/editRole',3,'',3,0),(11,2,'删除角色','','/admin/role/delRole',3,'',4,0),(12,5,'编辑管理员','','/admin/admin/editAdmin',3,'',1,0),(13,5,'删除管理员','','/admin/admin/delAdmin',3,'',3,0),(14,1,'操作日志','OperationLog','/admin/admin/adminLog',2,'',6,1),(15,1,'菜单管理','MenuSet','/admin/menu/menuList',2,'',4,1),(16,15,'新增菜单','','/admin/menu/addMenu',3,'',0,0),(17,15,'编辑菜单','','/admin/menu/editMenu',3,'',1,0),(18,15,'删除菜单','','/admin/menu/delMenu',3,'',2,0),(20,1,'上传设置','UploadSet','/admin/setting/getUploadConfig',2,'',5,1),(21,1,'参数配置','BasicInfo','/admin/setting/settingList',2,'',3,1),(22,21,'新增配置','','/admin/setting/addSetting',3,'',1,0),(23,21,'修改配置','','/admin/setting/editSetting',3,'',3,0),(24,21,'删除配置','','/admin/setting/delSetting',3,'',2,0),(25,20,'删除文件','','/admin/setting/delFile',3,'',1,0),(28,5,'冻结/解冻管理员','','/admin/admin/changeAdminStatus',3,'',4,0),(29,1,'登录安全配置','AuthConfig','/admin/setting/setAuthConfig',2,'',7,1),(30,1,'数据库备份','DbBackUp','',2,'',8,1),(31,30,'备份列表','','/admin/backup/getList',3,'',1,0),(32,30,'备份数据','','/admin/backup/backUpDb',3,'',2,0),(33,30,'恢复数据库','','/admin/backup/restoreDb',3,'',3,0),(34,30,'下载备份','','/admin/backup/download',3,'',4,0),(35,30,'删除备份','','/admin/backup/removeBackUpFile',3,'',5,0);
/*!40000 ALTER TABLE `czh_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `czh_role`
--

DROP TABLE IF EXISTS `czh_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `czh_role` (
  `roleId` int NOT NULL AUTO_INCREMENT,
  `roleName` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `ids` text NOT NULL,
  `describe` varchar(255) NOT NULL,
  `atime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`roleId`)
) ENGINE=MyISAM AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb3 COMMENT='管理员角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `czh_role`
--

LOCK TABLES `czh_role` WRITE;
/*!40000 ALTER TABLE `czh_role` DISABLE KEYS */;
INSERT INTO `czh_role` VALUES (1,'超级管理员','[37,38,36,6,12,13,28,5,9,10,11,2,22,24,23,21,16,17,18,19,15,25,20,14,29,31,32,33,34,35,30,1,41,42,43,44,40,76,47,48,49,50,46,39,52,54,55,56,57,53,59,60,61,62,63,64,58,69,51,70,71,72,66,74,75,73,65,79,80,90,91,78,84,85,83,86,77]','最高权限','2025-06-12 11:33:00');
/*!40000 ALTER TABLE `czh_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `czh_setting`
--

DROP TABLE IF EXISTS `czh_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `czh_setting` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` char(64) DEFAULT NULL COMMENT '配置标题',
  `type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 文本  2图片  ',
  `value` text NOT NULL,
  `canDel` tinyint(1) DEFAULT '1' COMMENT '是否允许删除',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 COMMENT='系统设置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `czh_setting`
--

LOCK TABLES `czh_setting` WRITE;
/*!40000 ALTER TABLE `czh_setting` DISABLE KEYS */;
INSERT INTO `czh_setting` VALUES (1,'系统名称',1,'创智汇管理后台模板',0);
/*!40000 ALTER TABLE `czh_setting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `czh_upload_files`
--

DROP TABLE IF EXISTS `czh_upload_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `czh_upload_files` (
  `id` int NOT NULL AUTO_INCREMENT,
  `domain` tinyint(1) DEFAULT '0' COMMENT '文件保存在哪里的 0虚拟文件夹 1七牛 2阿里oss 3腾讯cos',
  `type` tinyint(1) DEFAULT NULL COMMENT '文件类型  1图片 2视频 3 Excel 4 word 5 pdf 6 zip 7 未知类型文件 8文件夹',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '文件名称',
  `key` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '上传到第三方的key',
  `url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '文件地址',
  `atime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `pid` int DEFAULT '0' COMMENT '上级目录ID 顶级目录为0',
  `fileWidth` int NOT NULL DEFAULT '0' COMMENT '图片或者视频宽其它为0',
  `fileHeight` int NOT NULL DEFAULT '0' COMMENT '图片或者视频高其它为0',
  `fileSize` int NOT NULL DEFAULT '0' COMMENT '文件大小kb',
  `thumb` varchar(255) DEFAULT NULL COMMENT '图片的缩略图或者视频的封面图',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=39 DEFAULT CHARSET=latin1 COMMENT='文件库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `czh_upload_files`
--

LOCK TABLES `czh_upload_files` WRITE;
/*!40000 ALTER TABLE `czh_upload_files` DISABLE KEYS */;
INSERT INTO `czh_upload_files` VALUES (38,3,2,'2313_1755585720.mp4','admin/17563967174724604331.mp4','https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com/admin/17563967174724604331.mp4',NULL,0,592,1280,482,'https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com/admin/17563967174724604331.mp4?ci-process=snapshot&time=1&format=jpg'),(37,3,2,'2313_1755585720.mp4','admin/17563965847754037301.mp4','https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com/admin/17563965847754037301.mp4',NULL,0,592,1280,482,'https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com/admin/17563965847754037301.mp4?ci-process=snapshot&time=1&format=jpg'),(36,3,2,'2471_1755651777.mp4','admin/17563965770366103601.mp4','https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com/admin/17563965770366103601.mp4',NULL,0,592,1280,384,'https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com/admin/17563965770366103601.mp4?ci-process=snapshot&time=1&format=jpg'),(35,3,1,'douyin.png','admin/17563959643281923361.png','https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com/admin/17563959643281923361.png',NULL,0,64,64,1,'https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com/admin/17563959643281923361.png?imageMogr2/thumbnail/160x160'),(34,3,1,'gh_81e921452c42_258.jpg','admin/17563959555538578851.jpg','https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com/admin/17563959555538578851.jpg',NULL,0,258,258,42,'https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com/admin/17563959555538578851.jpg?imageMogr2/thumbnail/160x160'),(32,4,2,'video 1.mp4','/home/www/java_demo/static/uploads/admin/20250828/video 1.mp4','https://java.honghukeji.net:10443/uploads/admin/20250828/video 1.mp4',NULL,0,1440,1440,3818,NULL),(33,4,1,'17562890908234597871.jpg','/home/www/java_demo/static/uploads/admin/20250828/17562890908234597871.jpg','https://java.honghukeji.net:10443/uploads/admin/20250828/17562890908234597871.jpg',NULL,0,419,809,64,'https://java.honghukeji.net:10443/uploads/admin/20250828/17562890908234597871_thumb.jpg'),(29,3,1,'15531623788861.jpg','admin/17563773236488549491.jpg','https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com/admin/17563773236488549491.jpg',NULL,0,1405,1054,1139,'https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com/admin/17563773236488549491.jpg?imageMogr2/thumbnail/160x160');
/*!40000 ALTER TABLE `czh_upload_files` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `czh_upload_set`
--

DROP TABLE IF EXISTS `czh_upload_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `czh_upload_set` (
  `id` int NOT NULL,
  `qiniu` text,
  `alioss` text,
  `txcos` text,
  `tos` text COMMENT '火山云tos',
  `local` text COMMENT '本地上传配置',
  `visible` tinyint(1) NOT NULL COMMENT '选用的哪个 0未选用 1七牛 2阿里 3腾讯',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='文件上传配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `czh_upload_set`
--

LOCK TABLES `czh_upload_set` WRITE;
/*!40000 ALTER TABLE `czh_upload_set` DISABLE KEYS */;
INSERT INTO `czh_upload_set` VALUES (1,'{\"accessKey\":\"gyWUi1Uxreo8hyq_n0rjPYWGtG_a0eEVDLdVYMHW\",\"bucket\":\"hhzz001\",\"domain\":\"https://qiniu.cqczh.com.cn\",\"endpoint\":\"up-z2.qiniup.com\",\"secretKey\":\"8ERvH_QG7HB9G8hSIFaBjP_z90piMLcqU6Jin-er\",\"visible\":1}','{\"ak\":\"LTAI5tBM2UE4SrNjF9AowUbe\",\"bucket\":\"dxcx\",\"domain\":\" https://cqczh.oss-cn-shenzhen.aliyuncs.com\",\"endpoint\":\"oss-cn-shenzhen.aliyuncs.com\",\"sk\":\"LYtcB5ey4WOQMDmFSyP114C6ULZUfx\",\"visible\":2}','{\"ak\":\"AKIDb5PWQ7dz86rTs8D6oLgrunV0Umuqb9Xl\",\"bucket\":\"ap-chengdu\",\"bucketName\":\"50yanglao-1333745924\",\"domain\":\"https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com\",\"sk\":\"wqZ9au32sZ4NXpy1tbordJpBLK9qPo4A\",\"visible\":3}','{\"accessKey\":\"AKLTMDVjMjU3NzM5N2I0NGVmNzk3ZjQ3ZTI2MmJlYzk2MTY\",\"bucket\":\"czh\",\"domain\":\"https://czh.tos-cn-beijing.volces.com\",\"region\":\"cn-beijing\",\"secretKey\":\"WmpFd056UXlaR0ppTlRFM05EWTJNV0V6TmpobFlUbG1aVE0yT1RKak9EUQ==\",\"visible\":5}','{\"domain\":\"https://java.honghukeji.net:10443/uploads\",\"host\":\"https://java.honghukeji.net:10443/upload\",\"path\":\"/home/www/java_demo/static/uploads\",\"visible\":4}',3);
/*!40000 ALTER TABLE `czh_upload_set` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'jichu'
--

--
-- Dumping routines for database 'jichu'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-01  9:12:46
