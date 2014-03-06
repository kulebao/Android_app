package com.djc.logintest.upload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.graphics.Bitmap;

import com.aliyun.android.oss.OSSClient;
import com.djc.logintest.utils.Utils;

/**
 * 该示例代码展示了如果在OSS中创建和删除一个Bucket，以及如何上传和下载一个文件。
 * 
 * 该示例代码的执行过程是： 1. 检查指定的Bucket是否存在，如果不存在则创建它； 2. 上传一个文件到OSS； 3. 下载这个文件到本地； 4.
 * 清理测试资源：删除Bucket及其中的所有Objects。
 * 
 * 尝试运行这段示例代码时需要注意： 1. 为了展示在删除Bucket时除了需要删除其中的Objects,
 * 示例代码最后为删除掉指定的Bucket，因为不要使用您的已经有资源的Bucket进行测试！ 2.
 * 请使用您的API授权密钥填充ACCESS_ID和ACCESS_KEY常量； 3.
 * 需要准确上传用的测试文件，并修改常量uploadFilePath为测试文件的路径； 修改常量downloadFilePath为下载文件的路径。 4.
 * 该程序仅为示例代码，仅供参考，并不能保证足够健壮。
 * 
 */
public class OSSMgr {

    private static final String ACCESS_ID = "0qt02QY9NRHDX24H";
    private static final String ACCESS_KEY = "pr4QP3C10dzyKOOKL7jFR3VCkwFPbn";
    private static final String OSS_ENDPOINT = "http://oss.aliyuncs.com/";
    private static String BUCKETNAME = "cocobabys";
    public static String OSS_HOST = "http://cocobabys.oss.aliyuncs.com/";

    /**
     * @param url 
     * @param input
     * @param length
     * @param args
     */
    public static void UploadPhoto(Bitmap bitmap, String url) {
        OSSClient client = new OSSClient(ACCESS_ID, ACCESS_KEY);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        client.uploadObject(BUCKETNAME, url, baos.toByteArray());
    }
    
    public static String getOssHost(){
    	return OSS_HOST;
    }
}
