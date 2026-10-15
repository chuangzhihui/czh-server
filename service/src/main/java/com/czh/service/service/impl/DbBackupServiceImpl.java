package com.czh.service.service.impl;

import lombok.extern.slf4j.Slf4j;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.czh.common.exception.ErrorException;
import com.czh.common.utils.DateUtil;
import com.czh.common.utils.FileUtil;
import com.czh.service.dao.DbBackupDao;
import com.czh.service.dto.admin.DbBackUpListDto;
import com.czh.service.entity.DbBackup;
import com.czh.service.service.DbBackupService;
import com.czh.service.vo.admin.DbBackupVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * 数据库备份记录(DbBackup)表服务实现类
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
@Slf4j
@Service
public class DbBackupServiceImpl extends ServiceImpl<DbBackupDao,DbBackup> implements DbBackupService {

    /**
     * 密码通过环境变量 {@code MYSQL_PWD} 传给 mysqldump / mysql 客户端（BE-02）。
     * <p>
     * 为什么不继续用 {@code -p<password>}：即便改成参数数组、不走 shell，
     * 命令行参数仍会被同机任何用户通过 {@code ps} 看到，且仍可能被写进日志。
     * 通过环境变量传递可以把密码彻底移出 argv。
     */
    private static final String MYSQL_PWD_ENV = "MYSQL_PWD";

    /** 数据库名白名单：mysqldump 的库名参数必须命中，防止配置被越权改造成注入载体 */
    private static final Pattern DB_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_$]{1,64}$");

    /** 主机白名单：允许域名/IP，禁止空白、分号、管道等 shell 元字符 */
    private static final Pattern HOST_PATTERN = Pattern.compile("^[A-Za-z0-9._\\-]{1,253}$");

    /** 端口白名单：纯数字 1~5 位 */
    private static final Pattern PORT_PATTERN = Pattern.compile("^[0-9]{1,5}$");

    @Value("${spring.datasource.backuppath}")
    private String backupPath;//备份文件目录

    @Value("${spring.sql.dbname}")
    private String mysqlDatabase;//数据库名称
    @Value("${spring.sql.username}")
    private String mysqlUsername;//数据库用户名
    @Value("${spring.sql.password}")
    private String mysqlPassword;//数据库密码
    @Value("${spring.sql.host}")
    private String mysqlHost;//数据库地址
    @Value("${spring.sql.port}")
    private String mysqlPort;//数据库端口

    @Override
    public void backup(Integer adminId) {
        Path folderPath = Paths.get(backupPath + "SQL" + File.separator);
        try {
            // 检查文件夹是否已经存在
            if (!Files.exists(folderPath)) {
                // 创建文件夹
                Files.createDirectories(folderPath);
                log.info("文件夹已成功创建: " + folderPath);
            } else {
                log.info("文件夹已经存在: " + folderPath);
            }
        } catch (IOException e) {
            // 捕获并处理IOException
            log.error("执行异常", e);
        }

        String mysqlFile=backupPath+"mysql.sql";

        Path mysqlPath =Paths.get(mysqlFile);
        try {
            Files.deleteIfExists(mysqlPath);
        } catch (IOException e) {
            log.error("执行异常", e);
            throw new ErrorException("备份数据库失败!");
        }
        //备份MySQL
        backupMysql();
        String fileName= DateUtil.getStrDate("yyyyMMddHHmmss")+".zip";
        String zipFilePath=backupPath+fileName;
        try{
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath));
            // 打包到ZIP文件
            // 添加MySQL文件
            zipOut.putNextEntry(new ZipEntry("mysql.sql"));
            zipOut.write(Files.readAllBytes(mysqlPath));
            zipOut.closeEntry();
            // 删除临时文件
            Files.deleteIfExists(mysqlPath);
            zipOut.close();
            //插入记录
            File file=new File(zipFilePath);
            DbBackup dbBackup=new DbBackup();
            dbBackup.setFileName(fileName);
            dbBackup.setFileSize((int) file.length());
            dbBackup.setFilePath(zipFilePath);
            dbBackup.setAdminId(adminId);
            save(dbBackup);
        }catch (IOException e)
        {
            log.error("执行异常", e);
            throw new ErrorException("备份数据库失败!");
        }
    }

    @Override
    public void restoreDbById(Integer id) {
        DbBackup backup = getById(id);
        if (backup == null) {
            throw new ErrorException("备份信息不存在!");
        }
        File file = new File(backup.getFilePath());
        if (!file.exists()) {
            throw new ErrorException("备份文件不存在!");
        }
        //解压到指定目录
        FileUtil.unZipFiles(backup.getFilePath(),backupPath+"restoreTempDir");
        //恢复mysql
        restoreMysql(backupPath+"restoreTempDir/mysql.sql");
    }

    @Override
    public PageInfo<DbBackupVo> getList(DbBackUpListDto dto) {
        PageHelper.startPage(dto.getPage(), dto.getSize(), "a.id desc");
        List<DbBackupVo> vos = baseMapper.getList(dto);
        return   new PageInfo<>(vos);
    }

    //备份MySQL
    private void backupMysql()
    {
        String file_name = "mysql.sql";
        String real_path = backupPath + file_name;
        // BE-02：不再拼接字符串命令，改用参数数组；密码走 MYSQL_PWD 环境变量
        ProcessBuilder processBuilder = buildMysqlProcess("mysqldump",
                Arrays.asList("-r", real_path, "--no-tablespaces"));
        try {
            log.info("执行 mysqldump 备份 db={} program={} 参数个数={}（命令行不含密码，已脱敏）",
                    mysqlDatabase, "mysqldump", processBuilder.command().size());
            Process process = processBuilder.start();
            // 必须先读空 stderr / stdout，否则缓冲区写满会导致子进程阻塞
            String errorOutput = readFully(process.getErrorStream());
            readFully(process.getInputStream());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ErrorException("MYSQL备份失败:" + errorOutput);
            }
        } catch (IOException e) {
            log.error("执行异常", e);
            throw new ErrorException("MYSQL备份失败:" + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorException("MYSQL备份被中断!");
        }
    }

    /**
     * 恢复MySQL
     *
     * @param sqlFile 待导入的 SQL 文件路径
     */
    private void restoreMysql(String sqlFile)
    {
        // BE-02：原来这里既有 log.info("command:" + command) 把含明文密码的命令行写进日志，
        // 又用 Runtime.exec(字符串) 交给 shell 解析。现改为参数数组 + MYSQL_PWD，日志只记脱敏摘要。
        ProcessBuilder processBuilder = buildMysqlProcess("mysql", Collections.emptyList());
        Process process = null;
        try {
            log.info("执行 mysql 恢复 db={} program={} 参数个数={}（命令行不含密码，已脱敏）",
                    mysqlDatabase, "mysql", processBuilder.command().size());
            process = processBuilder.start();
        } catch (IOException e) {
            log.error("执行异常", e);
            throw new ErrorException("恢复数据库失败!" + e.getMessage());
        }
        try {
            // 1) 先把 SQL 灌进 mysql 客户端的 stdin
            try (OutputStream outputStream = process.getOutputStream();
                 Writer outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(sqlFile), StandardCharsets.UTF_8)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    outputStreamWriter.write(line);
                    outputStreamWriter.write("\n");
                }
                outputStreamWriter.flush();
            }
            // 2) 读完 stderr 再等退出码，避免输出缓冲区满导致死锁
            String errorOutput = readFully(process.getErrorStream());
            readFully(process.getInputStream());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ErrorException("恢复数据库失败:" + errorOutput);
            }
        } catch (IOException e) {
            log.error("执行异常", e);
            throw new ErrorException("恢复数据库失败!" + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorException("恢复数据库被中断!");
        }
    }

    /**
     * 构建 mysql / mysqldump 的进程（BE-02 核心）。
     * <p>
     * 三道防线：① 参数数组，不经 shell 解析，命令注入无效；
     * ② 密码走环境变量，不出现在 argv（也就不会被 ps 或日志捕获）；
     * ③ 库名/主机/端口走白名单，配置被越权改写也无法注入。
     *
     * @param program   程序名（mysqldump / mysql）
     * @param extraArgs 附加参数（如 -r 输出文件），可为空列表
     * @return 已注入 MYSQL_PWD 的 ProcessBuilder
     */
    private ProcessBuilder buildMysqlProcess(String program, List<String> extraArgs)
    {
        List<String> command = new ArrayList<>();
        command.add(program);
        command.add("-h");
        command.add(requireSafeHost(mysqlHost));
        command.add("-P");
        command.add(requireSafePort(mysqlPort));
        command.add("-u");
        command.add(requireNonBlank(mysqlUsername, "数据库用户名"));
        command.add(requireSafeDatabase(mysqlDatabase));
        if (extraArgs != null && !extraArgs.isEmpty()) {
            command.addAll(extraArgs);
        }
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put(MYSQL_PWD_ENV, mysqlPassword == null ? "" : mysqlPassword);
        return processBuilder;
    }

    /**
     * 读取流内容并关闭（防止子进程输出缓冲区写满导致阻塞）
     *
     * @param inputStream 进程输出流，允许为 null
     * @return 文本内容；读取失败返回空串
     */
    private String readFully(InputStream inputStream)
    {
        if (inputStream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(line);
            }
        } catch (IOException e) {
            log.warn("读取进程输出流失败", e);
        }
        return sb.toString();
    }

    private String requireSafeDatabase(String database)
    {
        String value = requireNonBlank(database, "数据库名称");
        if (!DB_NAME_PATTERN.matcher(value).matches()) {
            throw new ErrorException("数据库名称非法!");
        }
        return value;
    }

    private String requireSafeHost(String host)
    {
        String value = host == null || host.isBlank() ? "127.0.0.1" : host.trim();
        if (!HOST_PATTERN.matcher(value).matches()) {
            throw new ErrorException("数据库地址非法!");
        }
        return value;
    }

    private String requireSafePort(String port)
    {
        String value = port == null || port.isBlank() ? "3306" : port.trim();
        if (!PORT_PATTERN.matcher(value).matches()) {
            throw new ErrorException("数据库端口非法!");
        }
        return value;
    }

    private String requireNonBlank(String value, String desc)
    {
        if (value == null || value.isBlank()) {
            throw new ErrorException(desc + "未配置!");
        }
        return value.trim();
    }
}
