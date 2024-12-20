/********************************************************************************
 * Copyright (c) 12-20-2024 Contributors to the Eclipse Foundation
 * 
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 ********************************************************************************/

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
 * This class initializes the server with various resources and endpoints.
 * It supports both CoAP and CoAPs protocols. The server configuration is
 * loaded from a file and customized using a DefinitionsProvider.
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
        server.add(new ActuatorResource(sharedData));
        server.add(new ActuatorEchoResource(sharedData));
        server.add(new ActuatorStatResource(sharedData));

        // Add the ValidateResource
        server.add(new ValidateResource(sharedData));


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