package com.ariche.boatapi.config;

import com.ariche.boatapi.service.storage.CloudStorageService;
import com.ariche.boatapi.service.storage.LocalStorageService;
import com.ariche.boatapi.service.storage.StorageException;
import com.ariche.boatapi.service.storage.StorageService;
import com.ariche.boatapi.service.storage.dto.EStorageError;
import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder;
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class StorageConfiguration {

    @Bean
    @Profile({"local"})
    public StorageService localStorageService(@Value("${io.path.storage}") String ioStorageFolderPath) {
        log.info("StorageService: init LocalStorageService");
        return new LocalStorageService(ioStorageFolderPath);
    }

    @Bean
    @Profile({"!local"})
    public StorageService cloudCOSClient(@Value("${cos.endpoint:}") String endpoint,
                                         @Value("${cos.location:}") String location,
                                         @Value("${cos.timeout:30000}") int timeout,
                                         @Value("${cos.bucket-name:}") String bucketName,
                                         @Value("${cos.endpoint-IAM:}") String endpointIAM,
                                         @Value("${cos.credentials.api-key:}") String apiKey,
                                         @Value("${cos.credentials.resource-id:}") String resourceId) throws StorageException {
        log.info("StorageService: init CloudStorageService");
        final AmazonS3 amazonS3 = buildClient(endpoint, location, endpointIAM, apiKey, resourceId, timeout);
        return new CloudStorageService(amazonS3, bucketName);
    }

    protected AmazonS3 buildClient(final String endpoint,
                                   final String location,
                                   final String endpointIAM,
                                   final String apiKey,
                                   final String resourceId,
                                   final int timeout) throws StorageException {
        final BasicIBMOAuthCredentials credentials = new BasicIBMOAuthCredentials(apiKey, resourceId);
        final ClientConfiguration clientConfig = new ClientConfiguration()
            .withRequestTimeout(timeout)
            .withTcpKeepAlive(true)
            .withUseExpectContinue(false);

        AmazonS3 amazonS3;
        try {
            amazonS3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, location))
                .withIAMEndpoint(endpointIAM)
                .withPathStyleAccessEnabled(false)
                .withClientConfiguration(clientConfig)
                .build();
        } catch (Exception e) {
            log.error("Could not create AmazonS3 client: {}", e.getMessage(), e);
            throw new StorageException(EStorageError.INIT_STORAGE_SYSTEM_ERROR, "Could not build AmazonS3 Client", e);
        }

        log.debug("AmazonS3 client successfully created");
        return amazonS3;
    }
}
