package com.qkwl.admin.layui.oss;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSErrorCode;
import com.aliyun.oss.ServiceException;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.qkwl.common.framework.redis.RedisHelper;
import com.qkwl.common.util.GUIDUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * OSS公共接口
 * @author ZKF
 * PS: 新调用在spring中增加以下配置
 * <bean id="ossHelper" class="com.qkwl.common.oss.OssHelper"></bean>
 */
public class OssHelper {

	private RedisHelper redisHelper;
	
	private String bucketBase;
	private String endPoint;
	private String accessKey;
	private String secretKey;
	
	public void setRedisHelper(RedisHelper redisHelper) {
		this.redisHelper = redisHelper;
	}

	
	
	public String getBucketBase() {
		return bucketBase;
	}



	public void setBucketBase(String bucketBase) {
		this.bucketBase = bucketBase;
	}



	public String getEndPoint() {
		return endPoint;
	}



	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public String getAccessKey() {
		return accessKey;
	}



	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}



	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}



	/**
	 * 上传文件
	 * @param file
	 * @param filePath
	 * @return
	 */
	public String uploadFile(MultipartFile file, String filePath) {
		String result;
		try {
			result = uploadOSS(getBucketBase(), file.getContentType(), new ByteArrayInputStream(file.getBytes()), file.getBytes().length, file.getOriginalFilename(), filePath);
		} catch (Exception e) {
			return null;
		}
		if (result == null) {
			return null;
		}
		return redisHelper.getSystemArgs("imgUploadUrl") + result;
	}


	/**
	 * OSS上传文件
	 * @param Bucket
	 * @param contentType
	 * @param fileSize
	 * @param input
	 * @param fileName
	 * @param filePath
	 * @return
	 */
	public String uploadOSS(String Bucket, String contentType, InputStream input, long fileSize, String fileName, String filePath) throws Exception {
		// 参数校验
		if (StringUtils.isEmpty(contentType) || StringUtils.isEmpty(filePath) || StringUtils.isEmpty(fileName)) {
			return null;
		}
		String key = filePath + GUIDUtils.getGUIDString().toLowerCase() + fileName;
		OSSClient client = new OSSClient(getEndPoint(), getAccessKey(), getSecretKey());
		ObjectMetadata objectMeta = new ObjectMetadata();
		objectMeta.setContentLength(fileSize);

		try {
			// 创建bucket
			client.createBucket(Bucket);
			client.setBucketAcl(Bucket, CannedAccessControlList.PublicRead);
		} catch (ServiceException e) {
			// 如果Bucket已经存在，则忽略
			if (!OSSErrorCode.BUCKET_ALREADY_EXISTS.equals(e.getErrorCode())) {
				throw e;
			}
		}

		objectMeta.setContentType(contentType);
		client.putObject(Bucket, key, input, objectMeta);
		client.shutdown();
		return key;
	}

}
