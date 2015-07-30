package com.jidian.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by xiaoyun on 4/18/14.
 */
public class S3InputStreamBuilder implements InputStreamBuilder {

    private static final String bucketNameStr = "bucketName";
    private static final String keyStr = "key";

    // We must keep a reference to s3Client live while the S3 input stream
    // is being used, because of a bug in the AWS SDK:
    // http://stackoverflow.com/a/14641542/696418
    AmazonS3Client s3Client = new AmazonS3Client((new AWSUtils()).getAWSCredentials(),
            new ClientConfiguration().withSocketTimeout(0));

    String bucketName;
    String key;

    @Override
    public void init(Properties prop, String prefix) {
        OptionAccessor accessor = new OptionAccessor(prop, prefix);
        bucketName = accessor.getValue(bucketNameStr);
        key = accessor.getValue(keyStr);
    }

    @Override
    public InputStream buildInputStream() throws IOException {
        return s3Client.getObject(bucketName, key).getObjectContent();
    }
}
