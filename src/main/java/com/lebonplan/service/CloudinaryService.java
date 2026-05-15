package com.lebonplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
@Service
@Slf4j
public class CloudinaryService {

    @Value("${app.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${app.cloudinary.api-key:}")
    private String apiKey;

    @Value("${app.cloudinary.api-secret:}")
    private String apiSecret;

    //private final HttpClient httpClient = HttpClient.newHttpClient();
    private final HttpClient httpClient = unsafeHttpClient();
    /**
     * Upload une image sur Cloudinary et retourne { url, publicId }.
     * Si Cloudinary n'est pas configuré, retourne une URL placeholder.
     */
    public Map<String, String> upload(MultipartFile file, String folder) throws IOException {
        if (cloudName.isBlank()) {
            log.warn("Cloudinary non configuré — utilisation d'une URL placeholder");
            return Map.of(
                "url",      "https://placehold.co/800x600?text=Image",
                "publicId", "placeholder"
            );
        }

        // Encode le fichier en base64
        String base64 = Base64.getEncoder().encodeToString(file.getBytes());
        String dataUri = "data:" + file.getContentType() + ";base64," + base64;

        long timestamp = System.currentTimeMillis() / 1000;
        String signature = sign("folder=" + folder + "&timestamp=" + timestamp, apiSecret);

        // Corps multipart simplifié via form-urlencoded
        String body = "file="      + encode(dataUri)
                    + "&folder="   + encode(folder)
                    + "&timestamp="+ timestamp
                    + "&api_key="  + apiKey
                    + "&signature="+ signature;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();

            // Parse manuellement les deux champs nécessaires
            String url      = extractJson(json, "secure_url");
            String publicId = extractJson(json, "public_id");

            log.info("Image uploadée : {}", publicId);
            return Map.of("url", url, "publicId", publicId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrompu", e);
        }
    }

    /**
     * Supprime une image de Cloudinary par son publicId.
     */
    public void delete(String publicId) {
        if (cloudName.isBlank() || publicId == null || publicId.equals("placeholder")) return;
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            String signature = sign("public_id=" + publicId + "&timestamp=" + timestamp, apiSecret);

            String body = "public_id=" + encode(publicId)
                        + "&timestamp=" + timestamp
                        + "&api_key="   + apiKey
                        + "&signature=" + signature;

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Image supprimée de Cloudinary : {}", publicId);
        } catch (Exception e) {
            log.warn("Impossible de supprimer l'image Cloudinary : {}", publicId, e);
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    private String sign(String data, String secret) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update((data + secret).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String extractJson(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        return end > start ? json.substring(start, end) : "";
    }
    private HttpClient unsafeHttpClient() {
        try {

            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(
                    null,
                    trustAllCerts,
                    new SecureRandom()
            );

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
