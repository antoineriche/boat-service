package com.ariche.boatapi.config;

import com.ariche.boatapi.service.storage.CloudStorageService;
import com.ariche.boatapi.service.storage.LocalStorageService;
import com.ariche.boatapi.service.storage.StorageException;
import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder;
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StorageConfigurationTest {

    private final StorageConfiguration configuration = spy(new StorageConfiguration());

    @Test
    void test_BuildClient() throws StorageException {

        final AmazonS3 s3 = mock(AmazonS3.class);
        final AmazonS3ClientBuilder builder = mock(AmazonS3ClientBuilder.class);
        when(builder.withClientConfiguration(any())).thenReturn(builder);
        when(builder.withCredentials(any())).thenReturn(builder);
        when(builder.withEndpointConfiguration(any())).thenReturn(builder);
        when(builder.withIAMEndpoint(any())).thenReturn(builder);
        when(builder.withPathStyleAccessEnabled(any())).thenReturn(builder);
        when(builder.build()).thenReturn(s3);


        try (MockedConstruction<BasicIBMOAuthCredentials> mockedConstruction = mockConstruction(BasicIBMOAuthCredentials.class);
             MockedStatic<AmazonS3ClientBuilder> mockedStatic = mockStatic(AmazonS3ClientBuilder.class)){

            mockedStatic
                .when(AmazonS3ClientBuilder::standard)
                .thenReturn(builder);

            final ArgumentCaptor<ClientConfiguration> captor = ArgumentCaptor.forClass(ClientConfiguration.class);
            final ArgumentCaptor<AwsClientBuilder.EndpointConfiguration> captor1 = ArgumentCaptor.forClass(AwsClientBuilder.EndpointConfiguration.class);

            assertEquals(s3, configuration.buildClient("url", "location", "iam", "key", "id", 20_000));

            verify(builder).withCredentials(any(AWSStaticCredentialsProvider.class));
            verify(builder).withEndpointConfiguration(captor1.capture());
            verify(builder).withIAMEndpoint("iam");
            verify(builder).withPathStyleAccessEnabled(false);
            verify(builder).withClientConfiguration(captor.capture());
            verify(builder).build();

            final ClientConfiguration clientConfiguration = captor.getValue();
            assertEquals(20_000, clientConfiguration.getRequestTimeout());
            assertTrue(clientConfiguration.useTcpKeepAlive());
            assertFalse(clientConfiguration.isUseExpectContinue());

            assertEquals("url", captor1.getValue().getServiceEndpoint());
            assertEquals("location", captor1.getValue().getSigningRegion());
        }
    }

    @Test
    void test_BuildClient_StorageException() {
        final AmazonS3ClientBuilder builder = mock(AmazonS3ClientBuilder.class);
        when(builder.withClientConfiguration(any())).thenReturn(builder);
        when(builder.withCredentials(any())).thenReturn(builder);
        when(builder.withEndpointConfiguration(any())).thenReturn(builder);
        when(builder.withIAMEndpoint(any())).thenReturn(builder);
        when(builder.withPathStyleAccessEnabled(any())).thenReturn(builder);

        final RuntimeException exception = new RuntimeException("nope");
        when(builder.build()).thenThrow(exception);


        try (MockedConstruction<BasicIBMOAuthCredentials> mockedConstruction = mockConstruction(BasicIBMOAuthCredentials.class);
             MockedStatic<AmazonS3ClientBuilder> mockedStatic = mockStatic(AmazonS3ClientBuilder.class)){

            mockedStatic
                .when(AmazonS3ClientBuilder::standard)
                .thenReturn(builder);

            final ArgumentCaptor<ClientConfiguration> captor = ArgumentCaptor.forClass(ClientConfiguration.class);
            final ArgumentCaptor<AwsClientBuilder.EndpointConfiguration> captor1 = ArgumentCaptor.forClass(AwsClientBuilder.EndpointConfiguration.class);

            assertThrows(StorageException.class,
                () -> configuration.buildClient("url", "location", "iam", "key", "id", 20_000));

            verify(builder).withCredentials(any(AWSStaticCredentialsProvider.class));
            verify(builder).withEndpointConfiguration(captor1.capture());
            verify(builder).withIAMEndpoint("iam");
            verify(builder).withPathStyleAccessEnabled(false);
            verify(builder).withClientConfiguration(captor.capture());
            verify(builder).build();

            final ClientConfiguration clientConfiguration = captor.getValue();
            assertEquals(20_000, clientConfiguration.getRequestTimeout());
            assertTrue(clientConfiguration.useTcpKeepAlive());
            assertFalse(clientConfiguration.isUseExpectContinue());

            assertEquals("url", captor1.getValue().getServiceEndpoint());
            assertEquals("location", captor1.getValue().getSigningRegion());
        }
    }

    @Test
    void test_LocalStorageService() {
        assertDoesNotThrow(() -> configuration.localStorageService("storage"));
        assertTrue(configuration.localStorageService("storage") instanceof LocalStorageService);
    }

    @Test
    void test_CloudCOSClient() {
        doReturn(mock(AmazonS3.class))
            .when(configuration)
            .buildClient(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());

        assertDoesNotThrow(() -> configuration.cloudCOSClient("url", "location", 20_000, "bucket","iam", "key", "id"));
        verify(configuration)
            .buildClient("url", "location", "iam", "key", "id", 20_000);

        assertTrue(configuration.cloudCOSClient("url", "location", 20_000, "bucket","iam", "key", "id") instanceof CloudStorageService);

    }
}
