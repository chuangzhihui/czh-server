package com.czh.common.utils.txCosUtil;

import com.czh.common.exception.ErrorException;
import com.czh.common.utils.txCosUtil.entity.TxCosEntity;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.utils.Jackson;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
public class TxCosUtil {
    private String bucket;
    private String bucketName;
    private String path;
    private String AccessKeyID;
    private String AccessKey;
    public TxCosUtil(TxCosEntity txCosEntity){
        this.AccessKey=txCosEntity.getSk();
        this.AccessKeyID=txCosEntity.getAk();
        this.bucket=txCosEntity.getBucket();
        this.bucketName=txCosEntity.getBucketName();
        this.path=txCosEntity.getDomain();
    }
    //删除文件
    public void delFile(String key){
        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(AccessKeyID, AccessKey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(bucket));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        try {
            cosclient.deleteObject(bucketName, key);
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }
    public  void uploadFile(File file, String key){
        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(AccessKeyID, AccessKey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(bucket));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        // 这里创建一个空的 ByteArrayInputStream 来作为示例

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,key,file);
        try {
            PutObjectResult putObjectResult = cosclient.putObject(putObjectRequest);
            System.out.println(putObjectResult.getRequestId());
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }finally {
            // 确认本进程不再使用 cosClient 实例之后，关闭之
            cosclient.shutdown();
        }
    }
    public Map<String,Object> getTempKey(){
        TreeMap<String, Object> config = new TreeMap<String, Object>();

        try {

            // 云 api 密钥 SecretId
            config.put("secretId", AccessKeyID);
            // 云 api 密钥 SecretKey
            config.put("secretKey", AccessKey);


            // 临时密钥有效时长，单位是秒
            config.put("durationSeconds", 1800);
            config.put("bucket", bucketName);
            // 换成 bucket 所在地区
            config.put("region", bucket);
            config.put("allowPrefixes", new String[] {
                    "*"
            });

            // 密钥的权限列表。必须在这里指定本次临时密钥所需要的权限。
            // 简单上传、表单上传和分块上传需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
            String[] allowActions = new String[] {
                    // 简单上传
                    "name/cos:PutObject",
                    // 表单上传、小程序上传
                    "name/cos:PostObject",
                    // 分块上传
                    "name/cos:InitiateMultipartUpload",
                    "name/cos:ListMultipartUploads",
                    "name/cos:ListParts",
                    "name/cos:UploadPart",
                    "name/cos:CompleteMultipartUpload"
            };
            config.put("allowActions", allowActions);
//            // 将 Policy 示例转化成 String，可以使用任何 json 转化方式，这里是本 SDK 自带的推荐方式
//            config.put("policy", Jackson.toJsonPrettyString(policy));

            Response response = CosStsClient.getCredential(config);
            System.out.println(Jackson.toJsonPrettyString(response));
            Map<String,Object> json=new HashMap<>();
            json.put("TmpSecretId",response.credentials.tmpSecretId);
            json.put("TmpSecretKey",response.credentials.tmpSecretKey);
            json.put("SecurityToken",response.credentials.sessionToken);
            json.put("StartTime",response.startTime);
            json.put("ExpiredTime",response.expiredTime);
            json.put("Bucket",bucketName);
            json.put("Region",bucket);
            json.put("domain",path);
            return json;
        } catch (Exception e) {
            throw new ErrorException("获取失败!");
        }
    }
}
