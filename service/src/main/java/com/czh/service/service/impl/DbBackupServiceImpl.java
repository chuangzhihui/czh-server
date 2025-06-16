package com.czh.service.service.impl;

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
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * 数据库备份记录(DbBackup)表服务实现类
 *
 * @author makejava
 * @since 2025-06-05 22:20:51
 */
@Service
public class DbBackupServiceImpl extends ServiceImpl<DbBackupDao,DbBackup> implements DbBackupService {
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

    @Value("${spring.data.mongodb.database}")
    private String mongoDatabase;//数据库名称
    @Value("${spring.data.mongodb.host}")
    private String mongoHost;//数据库地址
    @Value("${spring.data.mongodb.username}")
    private String mongoUsername;//数据库用户名
    @Value("${spring.data.mongodb.password}")
    private String mongoPassword;//数据库密码
    @Value("${spring.data.mongodb.port}")
    private String mongoPort;//数据库端口
    @Override
    public void backup(Integer adminId) {
        Path folderPath = Paths.get(backupPath + "SQL" + File.separator);
        try {
            // 检查文件夹是否已经存在
            if (!Files.exists(folderPath)) {
                // 创建文件夹
                Files.createDirectories(folderPath);
                System.out.println("文件夹已成功创建: " + folderPath);
            } else {
                System.out.println("文件夹已经存在: " + folderPath);
            }
        } catch (IOException e) {
            // 捕获并处理IOException
            e.printStackTrace();
        }

        String mysqlFile=backupPath+"mysql.sql";
        String mongoFile=backupPath+"mongodb_backup.zip";
        String mongoFloder=backupPath+"mongodb_backup";
        FileUtil.deleteFolder(new File(mongoFloder));

        Path mysqlPath =Paths.get(mysqlFile);
        Path mongoPath =Paths.get(mongoFile);
        try {
            Files.deleteIfExists(mysqlPath);
            Files.deleteIfExists(mongoPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ErrorException("备份数据库失败!");
        }
        //备份MySQL
        backupMysql();
        backupMongoDB();
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
            //添加MongoDB文件
            zipOut.putNextEntry(new ZipEntry("mongodb.zip"));
            zipOut.write(Files.readAllBytes(mongoPath));
            zipOut.closeEntry();
            // 删除临时文件
            Files.deleteIfExists(mongoPath);
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
            e.printStackTrace();
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
        //一般情况不恢复MongoDB 因为里面只有操作日志
        //获取MongoDB
        //先解压
//        FileUtil.unZipFiles(backupPath+"restoreTempDir/mongodb.zip",backupPath+"restoreTempDir/mongodb");
        //执行恢复
//        resotreMongoDb(backupPath+"restoreTempDir/mongodb");
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
        String command = "mysqldump -h " + mysqlHost + " -P " + mysqlPort + " -u " + mysqlUsername + " -p" + mysqlPassword + " " + mysqlDatabase + " -r " + real_path + " --no-tablespaces";
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {

            } else {
                throw new ErrorException("MYSQL备份失败:" + process.getErrorStream().toString());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new ErrorException("MYSQL备份失败:" + e.getMessage());
        }
    }
    //mongoDB数据备份
    private void backupMongoDB() {

        // 创建备份文件
        String filePath = backupPath + "mongodb_backup";
        String zipPath = backupPath + "mongodb_backup.zip";
        try {
            // 使用 mongodump 命令备份数据库
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "mongodump",
                    "--host", mongoHost,
                    "--port", mongoPort,
                    "--username", mongoUsername,
                    "--password", mongoPassword,
                    "--db", mongoDatabase,
                    "--out", filePath);

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 等待备份完成
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                FileUtil.addFolderToZip(filePath,zipPath);
                FileUtil.deleteFolder(new File(filePath));
            } else {
                throw new ErrorException("mongoDB备份失败:" + process.getErrorStream().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorException("mongoDB备份失败:" + e.getMessage());
        }
    }

    @SneakyThrows
    private void restoreMysql(String sqlFile)
    {
        String command = "mysql -h " + mysqlHost + " -P " + mysqlPort + " -u " + mysqlUsername + " -p" + mysqlPassword + " " + mysqlDatabase;
        System.out.println("command:" + command);
        Process process = null;
        try {
//            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
//            Process process = processBuilder.start();
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ErrorException("恢复数据库失败!" + e.getMessage());
        }
        OutputStream outputStream = process.getOutputStream();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(sqlFile), "utf-8"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuffer sb = new StringBuffer();
        String b = null;
        while ((b = bufferedReader.readLine()) != null) {
            sb.append(b + "\r\n");
        }
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "utf-8");
        outputStreamWriter.write(sb.toString());
        outputStreamWriter.flush();
        outputStreamWriter.close();
        bufferedReader.close();
        outputStream.close();
    }
    private void resotreMongoDb(String mongoDir)
    {
//        mongorestore --host <hostname> --port <port> --username <username> --password <password> --authenticationDatabase <auth-db> /path/to/backup/
        String command = "mongorestore --host="+mongoHost+" --port="+mongoPort+" --username="+mongoUsername+" --password="+mongoPassword+" --drop --db="+mongoDatabase+" " + mongoDir+"/"+mongoDatabase;
        System.out.println("commond:"+command);
        try {
            Process process = Runtime.getRuntime().exec(command);
            int exitCode=process.waitFor();
            if (exitCode == 0) {

            } else {
                throw new ErrorException("恢复mongoDB备份失败:" + process.getErrorStream().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
