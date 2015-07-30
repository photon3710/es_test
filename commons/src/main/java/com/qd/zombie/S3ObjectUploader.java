package com.jidian.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

public class S3ObjectUploader {
  private static final Logger LOG = LoggerFactory.getLogger(S3ObjectUploader.class);

  private static final String indexHomeStr = "indexHome";
  private static final String versionStr = "version";
  private static final String bucketNameStr = "bucketName";

  private Properties properties;
  private OptionAccessor option;
  private AWSUtils awsUtils;

  S3ObjectUploader(Properties properties, String prefix) {
	  this.properties = properties;
	  this.option = new OptionAccessor(this.properties, prefix);
	  this.awsUtils = new AWSUtils();
  }
  
  public void uploadIndexedDataToS3() {
    AWSCredentials credentials = awsUtils.getAWSCredentials();
    final AmazonS3Client amazonS3Client = new AmazonS3Client(credentials);

    final File indexHomeFile = new File(option.getValue(indexHomeStr));
    File[] zipFiles = indexHomeFile.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".zip");
      }
    });
    for(File zipFile : zipFiles) {
      if(zipFile.delete()) LOG.info("Deleted [" + zipFile.getAbsolutePath() + "]");
    }

    String prefix = "dev";

    File[] builtIndexFolders = indexHomeFile.listFiles();
    Map<File, List<File>> zipToContents = new HashMap<File, List<File>>();
    String version = option.getValue(versionStr);
    for(File builtIndexFolder : builtIndexFolders) {
      File compressedIndex = new File(indexHomeFile, prefix + "-" + builtIndexFolder.getName() + "-compressed-" + version + ".zip");
      if(compressedIndex.exists()) {
        boolean deletedCompressedIndex = compressedIndex.delete();
        if(deletedCompressedIndex) {
          LOG.info("Deleted compressed Index [" + compressedIndex + "]");
        }
      }

      if(builtIndexFolder.exists() && builtIndexFolder.isDirectory() && builtIndexFolder.listFiles() != null && builtIndexFolder.listFiles().length > 0)
        zipToContents.put(compressedIndex, getListOfLuceneIndexFiles(builtIndexFolder));
    }

    List<ForkJoinTask<Object>> compressionAndUploadTasks = new ArrayList<ForkJoinTask<Object>>();
    for(final Map.Entry<File, List<File>> zipFile : zipToContents.entrySet()) {
      compressionAndUploadTasks.add(new ForkJoinTask<Object>() {
        private Object result;
        @Override
        public Object getRawResult() {
          return result;
        }

        @Override
        protected void setRawResult(Object o) {
          this.result = o;
        }

        @Override
        protected boolean exec() {
          LOG.info("Compressing [" + zipFile.getKey() + "] files to compress: [" + zipFile.getValue().size() + "]");
          try {
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile.getKey());
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            byte[] buffer = new byte[1048650];
            int counter = 1;
            for(File file : zipFile.getValue()) {
              LOG.info("\tCompressing [" + file.getAbsolutePath() + "] [" + counter + " / " + zipFile.getValue().size() + "]");
              ZipEntry zipEntry = new ZipEntry(file.getAbsolutePath().substring(indexHomeFile.getAbsolutePath().length() + 1));
              zipOutputStream.putNextEntry(zipEntry);
              FileInputStream fileInputStream = new FileInputStream(file);

              int len;
              while((len = fileInputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, len);
              }

              fileInputStream.close();
              ++counter;
            }

            zipOutputStream.closeEntry();
            zipOutputStream.close();
          }
          catch (IOException e) {
            LOG.info("Error with creating zip file", e);
          }

          String bucketName = option.getValue(bucketNameStr);
          awsUtils.uploadFileToS3(bucketName, zipFile.getKey().getName(), zipFile.getKey(), amazonS3Client);

          return true;
        }
      });
    }

    long startCompressAndUpload = System.currentTimeMillis();
    Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
    	@Override
		public void uncaughtException(Thread t, Throwable e) {
    		Logger LOGGER = LoggerFactory.getLogger("AnonymousUncaughtExceptionHandler");
			LOGGER.error("An Uncaught Exception took place in Thread [" + t.getName() + "]", e);
		}
	};
	
    ForkJoinPool forkJoinPool = new ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            handler, false);
    
    for (ForkJoinTask<Object> forkJoinTask : compressionAndUploadTasks) {
      forkJoinPool.execute(forkJoinTask);
    }

    for (int i = compressionAndUploadTasks.size() - 1; i >= 0; --i) {
      compressionAndUploadTasks.get(i).join();
    }
    long endCompressAndUpload = System.currentTimeMillis();

    LOG.info("Compress and Upload Finished in [" + (endCompressAndUpload - startCompressAndUpload) + "]");
  }

  private List<File> getListOfLuceneIndexFiles(File luceneHome) {
    List<File> filesToCompress = new ArrayList<File>();
    if(luceneHome != null) {
      if(luceneHome.isFile()) filesToCompress.add(luceneHome);
      else if(luceneHome.isDirectory()) {
        for(File file : luceneHome.listFiles()) {
          filesToCompress.addAll(getListOfLuceneIndexFiles(file));
        }
      }
    }
    return filesToCompress;
  }
}