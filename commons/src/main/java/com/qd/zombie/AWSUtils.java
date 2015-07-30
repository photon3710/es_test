package com.jidian.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsResult;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.ProgressListener;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;

// We assume that AWS access key ID is expected to be in the accessKey property and 
// the AWS secret key is expected to be in the secretKey property.
public class AWSUtils {
  private AWSCredentials AWS_CREDENTIALS = null;
  private static final Logger LOG = LoggerFactory.getLogger(AWSUtils.class);

  public AWSUtils() {
	  AWS_CREDENTIALS = (new ClasspathPropertiesFileCredentialsProvider()).getCredentials();
  }
  
  
  public AWSCredentials getAWSCredentials() {
    return AWS_CREDENTIALS;
  }

  public AmazonElasticLoadBalancing getELBByName(String name) {
    AmazonEC2Client amazonEC2Client = new AmazonEC2Client(getAWSCredentials());
    DescribeRegionsResult describeRegionsResult = amazonEC2Client.describeRegions();
    List<Region> regions = describeRegionsResult.getRegions();
    AmazonElasticLoadBalancing loadBalancer = null;
    for(int i = 0; i < regions.size() && loadBalancer == null; ++i) {
      Region region = regions.get(i);
      AmazonElasticLoadBalancingClient amazonElasticLoadBalancingClient = new AmazonElasticLoadBalancingClient(getAWSCredentials());
      amazonElasticLoadBalancingClient.setEndpoint("elasticloadbalancing." + region.getRegionName() + ".amazonaws.com");
      DescribeLoadBalancersResult describeLoadBalancersResult = amazonElasticLoadBalancingClient.describeLoadBalancers();
      List<LoadBalancerDescription> loadBalancerDescriptions = describeLoadBalancersResult.getLoadBalancerDescriptions();
      for(LoadBalancerDescription loadBalancerDescription : loadBalancerDescriptions) {
        if(loadBalancerDescription.getLoadBalancerName().equalsIgnoreCase(name)) loadBalancer = amazonElasticLoadBalancingClient;
      }
    }

    return loadBalancer;
  }

  public Instance getInstanceByPublicDNS(AWSServer server) {
    String cnameToPublicDNS = null;
    AmazonRoute53Client amazonRoute53Client = new AmazonRoute53Client(getAWSCredentials());
    ListHostedZonesResult listHostedZonesResult = amazonRoute53Client.listHostedZones();
    for(int i = 0; i < listHostedZonesResult.getHostedZones().size() && cnameToPublicDNS == null; ++i) {
      HostedZone hostedZone = listHostedZonesResult.getHostedZones().get(i);
      ListResourceRecordSetsRequest listResourceRecordSetsRequest = new ListResourceRecordSetsRequest(hostedZone.getId());
      listResourceRecordSetsRequest.setMaxItems("1000");
      listResourceRecordSetsRequest.setStartRecordName(server.getPath().getHost());
      ListResourceRecordSetsResult listResourceRecordSetsResult = amazonRoute53Client.listResourceRecordSets(listResourceRecordSetsRequest);
      List<ResourceRecordSet> recordSets = listResourceRecordSetsResult.getResourceRecordSets();
      for(int j = 0; j < recordSets.size() && cnameToPublicDNS == null; ++j) {
        ResourceRecordSet resourceRecordSet = recordSets.get(j);
        String resourceRecordSetName = resourceRecordSet.getName();
        if(resourceRecordSetName.endsWith(".")) {
          resourceRecordSetName = resourceRecordSetName.substring(0, resourceRecordSetName.lastIndexOf('.'));
        }

        if(resourceRecordSetName.equalsIgnoreCase(server.getPath().getHost())) {
          List<ResourceRecord> resourceRecords = resourceRecordSet.getResourceRecords();
          for(int k = 0; k < resourceRecords.size() && cnameToPublicDNS == null; ++k) {
            ResourceRecord resourceRecord = resourceRecords.get(k);
            cnameToPublicDNS = resourceRecord.getValue();
          }
        }
      }
    }

    Instance targetInstance = null;
    if(cnameToPublicDNS != null) {
      AmazonEC2Client amazonEC2Client = new AmazonEC2Client(getAWSCredentials());
      DescribeRegionsResult describeRegionsResult = amazonEC2Client.describeRegions();
      List<Region> regions = describeRegionsResult.getRegions();

      for(int i = 0; i < regions.size() && targetInstance == null; ++i) {
        Region region = regions.get(i);
        amazonEC2Client.setEndpoint(region.getEndpoint());
        DescribeInstancesResult describeInstancesResult = amazonEC2Client.describeInstances();
        List<Reservation> reservations = describeInstancesResult.getReservations();
        for(int j = 0; j < reservations.size() && targetInstance == null; ++j) {
          Reservation reservation = reservations.get(j);
          List<Instance> instances = reservation.getInstances();
          for(int k = 0; k < instances.size() && targetInstance == null; ++k) {
            Instance instance = instances.get(k);
            if(instance.getPublicDnsName().equalsIgnoreCase(cnameToPublicDNS)) {
              targetInstance = instance;
            }
          }
        }
      }
    }

    return targetInstance;
  }

  public synchronized void removeServerFromLoadBalancer(AWSServer server, String loadBalancer) {
    LOG.info("Removing [" + server.getPath().toString() + "] from load balancer [" + loadBalancer + "]");
    AmazonElasticLoadBalancing loadBalancing = getELBByName(loadBalancer);
    if(loadBalancing != null) {
      LOG.info("Found target Load Balancer [" + loadBalancing + "]");
      Instance instance = getInstanceByPublicDNS(server);
      if(instance != null) {
        LOG.info("Found target instance to remove [" + instance.getImageId() + ", " + instance.getPublicIpAddress() + "]");
        List<com.amazonaws.services.elasticloadbalancing.model.Instance> deregisterInstance = new ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance>();
        com.amazonaws.services.elasticloadbalancing.model.Instance instanceToRemove = new com.amazonaws.services.elasticloadbalancing.model.Instance();
        instanceToRemove.setInstanceId(instance.getInstanceId());
        deregisterInstance.add(instanceToRemove);
        DeregisterInstancesFromLoadBalancerRequest deregisterInstancesFromLoadBalancerRequest = new DeregisterInstancesFromLoadBalancerRequest(loadBalancer, deregisterInstance);
        loadBalancing.deregisterInstancesFromLoadBalancer(deregisterInstancesFromLoadBalancerRequest);
        LOG.info("Deregistering [" + server.getPath().toString() + "] from load balancer [" + loadBalancer + "]");
      }
    }
  }

  public void addServerToLoadBalancer(AWSServer server, String loadBalancer) {
    LOG.info("Adding [" + server.getPath().toString() + "] to load balancer [" + loadBalancer + "]");
    AmazonElasticLoadBalancing loadBalancing = getELBByName(loadBalancer);
    if(loadBalancer != null) {
      Instance instance = getInstanceByPublicDNS(server);
      if(instance != null) {
        List<com.amazonaws.services.elasticloadbalancing.model.Instance> registerInstances = new ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance>();
        com.amazonaws.services.elasticloadbalancing.model.Instance registerInstance = new com.amazonaws.services.elasticloadbalancing.model.Instance();
        registerInstance.setInstanceId(instance.getInstanceId());
        registerInstances.add(registerInstance);
        RegisterInstancesWithLoadBalancerRequest registerInstancesWithLoadBalancerRequest = new RegisterInstancesWithLoadBalancerRequest(loadBalancer, registerInstances);
        loadBalancing.registerInstancesWithLoadBalancer(registerInstancesWithLoadBalancerRequest);
        LOG.info("Registering [" + server.getPath().toString() + "] with load balancer [" + loadBalancer + "]");
      }
    }
  }

  public void uploadFileToS3(String bucketName, String key, File file) {
    AWSCredentials credentials = getAWSCredentials();
    AmazonS3Client amazonS3Client = new AmazonS3Client(credentials);
    uploadFileToS3(bucketName, key, file, amazonS3Client);
  }

  public void uploadFileToS3(String bucketName, final String key, File file, AmazonS3 amazonS3Client) {
    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file);
    final long fileLength = file.length();
    ProgressListener progressListener = new ProgressListener() {
      long transferCounter = 0;
      int eventCounter = 0;
      @Override
      public void progressChanged(ProgressEvent progressEvent) {
        if(progressEvent.getEventCode() == ProgressEvent.STARTED_EVENT_CODE) {
          LOG.info("Started Upload");
        }
        else if(progressEvent.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
          LOG.info("Finished Upload");
        }
        else if(progressEvent.getEventCode() == ProgressEvent.PART_COMPLETED_EVENT_CODE || progressEvent.getEventCode() == 0) {
          transferCounter += progressEvent.getBytesTransferred();
          ++eventCounter;
          if(eventCounter % 1000 == 0)
            LOG.info("Transferred [" + key + "] [" + transferCounter + " / " + fileLength + "] bytes [" + (((double) transferCounter / (double) fileLength) * 100) + " %]");
        }
      }
    };

    putObjectRequest.setProgressListener(progressListener);
    TransferManager transferManager = new TransferManager(amazonS3Client);
    Upload upload = transferManager.upload(putObjectRequest);

    long uploadStartTime = System.currentTimeMillis();
    UploadResult uploadResult = null;
    try {
      uploadResult = upload.waitForUploadResult();
    }
    catch (InterruptedException e) {
      LOG.debug("Upload interrupted", e);
    }
    if(uploadResult != null) {
      LOG.info("Upload Result: [" + uploadResult.toString() + "]");
    }
    long uploadEndTime = System.currentTimeMillis();
    LOG.info("Upload Finished in [" + (uploadEndTime - uploadStartTime) + "] ms");
  }
}