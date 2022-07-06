package com.remote.tools.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GetObjectRequest;
import java.io.File;

/**
 * 连接OSS的工具类
 * */
public class OSSConnection {

        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "yourAccessKeyId";
        String accessKeySecret = "yourAccessKeySecret";
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "examplebucket";

        //要保存的文件路径
        String frontName = "../../../../../../../../micro-services/";
        String backName = "/src/main/resources/inputData.zip";

    /**
     *  下载Object到本地文件，并保存到指定的本地路径中。如果指定的本地文件存在会覆盖，不存在则新建。
     *  如果未指定本地路径，则下载后的文件默认保存到程序所属项目对应本地路径中。
     */
    public void downloadFile(String fileType,String fileName) {
        // 不包含Bucket名称在内的Object完整路径
        String objectName = fileName;
        String pathName = "";
        switch (fileType){
            case "change-detection":
                pathName = frontName + "change_detection" + backName;
                break;
            case "target-detection":
                pathName = frontName + "target_detection" + backName;
                break;
            case "target-extraction":
                pathName = frontName + "target_extraction" + backName;
                break;
            case "terrian-classification":
                pathName = frontName + "terrian_classification" + backName;
                break;
            default:
                break;
        }
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {

            ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File(pathName));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}