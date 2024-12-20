

package org.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.interceptors.MessageTracer;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.elements.config.Configuration.DefinitionsProvider;
import org.eclipse.californium.elements.util.SslContextUtil;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.MdcConnectionListener;
import org.eclipse.californium.scandium.config.DtlsConfig;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.config.DtlsConfig.DtlsRole;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.pskstore.AdvancedMultiPskStore;
import org.eclipse.californium.scandium.dtls.x509.SingleCertificateProvider;
import org.eclipse.californium.scandium.dtls.x509.StaticNewAdvancedCertificateVerifier;
import org.eclipse.californium.scandium.dtls.CertificateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class to start the CoAP and CoAPs server.
 * 
 * <p>This class initializes the server with various resources and endpoints.
 * It supports both CoAP and CoAPs protocols. The server configuration is
 * loaded from a file and customized using a DefinitionsProvider.
 * 
 * <p>Resources added to the server:
 * <ul>
 *   <li>SensorResource</li>
 *   <li>StatResource</li>
 *   <li>LargeDownloadResource</li>
 *   <li>LargeUploadAckResource</li>
 *   <li>LargeUploadEchoResource</li>
 *   <li>ActuatorResource</li>
 *   <li>ActuatorEchoResource</li>
 *   <li>ActuatorStatResource</li>
 *   <li>ValidateResource</li>
 * </ul>
 * 
 * <p>Endpoints added to the server:
 * <ul>
 *   <li>CoAP endpoint on port 5683</li>
 *   <li>CoAPs endpoint on port 5684 with DTLS configuration</li>
 * </ul>
 * 
 * <p>The server uses a PSK store and loads credentials and trusted certificates
 * from specified keystore and truststore locations.
 * 
 * <p>Logging is used to capture any errors during the initialization of the
 * keystore.
 * 
 * <p>After starting the server, it prints the listening ports for both CoAP
 * and CoAPs endpoints.
 * 
 * <p>Each endpoint is configured with a MessageTracer interceptor to trace
 * messages.
 * 
 * @see org.eclipse.californium.core.CoapServer
 * @see org.eclipse.californium.core.network.CoapEndpoint
 * @see org.eclipse.californium.scandium.DTLSConnector
 * @see org.eclipse.californium.scandium.config.DtlsConnectorConfig
 * @see org.eclipse.californium.scandium.dtls.pskstore.AdvancedMultiPskStore
 * @see org.eclipse.californium.elements.config.Configuration
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class.getName());
    private static final int COAP_PORT = 5683;
    private static final int COAPS_PORT = 5684;
    private static final char[] KEY_STORE_PASSWORD = "endPass".toCharArray();
    private static final String KEY_STORE_LOCATION = "certs/keyStore.jks";
    private static final char[] TRUST_STORE_PASSWORD = "rootPass".toCharArray();
    private static final String TRUST_STORE_LOCATION = "certs/trustStore.jks";

    static {
        CoapConfig.register();
        DtlsConfig.register();
    }

    private static DefinitionsProvider DEFAULTS = new DefinitionsProvider() {
        @Override
        public void applyDefinitions(Configuration config) {
            config.set(DtlsConfig.DTLS_ROLE, DtlsRole.SERVER_ONLY);
            config.set(DtlsConfig.DTLS_RECOMMENDED_CIPHER_SUITES_ONLY, false);
            config.set(DtlsConfig.DTLS_PRESELECTED_CIPHER_SUITES, CipherSuite.STRONG_ENCRYPTION_PREFERENCE);
        }
    };

    public static void main(String[] args) {
        Configuration configuration = Configuration.createWithFile(Configuration.DEFAULT_FILE, "DTLS example server", DEFAULTS);
        Configuration.setStandard(configuration);

        CoapServer server = new CoapServer();

        SharedData sharedData = new SharedData();

        // Add the SensorResource
        server.add(new SensorResource(sharedData));

        // Add the StatResource
        server.add(new StatResource(sharedData));

        // Add large packet resources
        server.add(new LargeDownloadResource(sharedData));
        server.add(new LargeUploadAckResource(sharedData));
        server.add(new LargeUploadEchoResource(sharedData));

        // Add the ActuatorResource
        ActuatorResource actuatorResource = new ActuatorResource(sharedData);
        server.add(actuatorResource);
        server.add(new ActuatorEchoResource(sharedData));
        server.add(new ActuatorStatResource(sharedData));

        // Add the ValidateResource
        server.add(new ValidateResource(sharedData, actuatorResource));


        // Add CoAP endpoint
        CoapEndpoint.Builder coapBuilder = new CoapEndpoint.Builder();
        coapBuilder.setInetSocketAddress(new InetSocketAddress(COAP_PORT));
        coapBuilder.setConfiguration(configuration);
        server.addEndpoint(coapBuilder.build());

        // Add CoAPs endpoint
        try {
            AdvancedMultiPskStore pskStore = new AdvancedMultiPskStore();
            pskStore.setKey("twttestbed", "secretkey".getBytes());

            SslContextUtil.Credentials serverCredentials = SslContextUtil.loadCredentials(
                    SslContextUtil.CLASSPATH_SCHEME + KEY_STORE_LOCATION, "server", KEY_STORE_PASSWORD, KEY_STORE_PASSWORD);
            Certificate[] trustedCertificates = SslContextUtil.loadTrustedCertificates(
                    SslContextUtil.CLASSPATH_SCHEME + TRUST_STORE_LOCATION, "root", TRUST_STORE_PASSWORD);

            DtlsConnectorConfig.Builder dtlsBuilder = DtlsConnectorConfig.builder(configuration)
                    .setAddress(new InetSocketAddress(COAPS_PORT))
                    .setAdvancedPskStore(pskStore)
                    .setCertificateIdentityProvider(new SingleCertificateProvider(
                            serverCredentials.getPrivateKey(), serverCredentials.getCertificateChain(), CertificateType.RAW_PUBLIC_KEY, CertificateType.X_509))
                    .setAdvancedCertificateVerifier(StaticNewAdvancedCertificateVerifier.builder()
                            .setTrustedCertificates(trustedCertificates).setTrustAllRPKs().build())
                    .setConnectionListener(new MdcConnectionListener());

            DTLSConnector dtlsConnector = new DTLSConnector(dtlsBuilder.build());
            CoapEndpoint.Builder coapsBuilder = new CoapEndpoint.Builder();
            coapsBuilder.setConnector(dtlsConnector);
            coapsBuilder.setConfiguration(configuration);
            server.addEndpoint(coapsBuilder.build());

        } catch (GeneralSecurityException | IOException e) {
            LOG.error("Could not load the keystore", e);
        }

        server.start();

        for (Endpoint ep : server.getEndpoints()) {
            ep.addInterceptor(new MessageTracer());
        }

        System.out.println(ServerTimestamp.getElapsedTime()+"CoAP server is listening on port " + COAP_PORT);
        System.out.println(ServerTimestamp.getElapsedTime()+"CoAPs server is listening on port " + COAPS_PORT);
    }
}