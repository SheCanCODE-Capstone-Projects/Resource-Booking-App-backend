package org.example.resourcebookingappbackend.Service.Authentication;

import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public EmailService(
            @Value("${brevo.api-key}") String apiKey,
            @Value("${brevo.sender-mail}") String senderEmail,
            @Value("${brevo.sender-name:SpaceSync}") String senderName,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.objectMapper = objectMapper;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Sends an OTP verification email through Brevo.
     *
     * @param recipientEmail recipient's email address
     * @param otpCode        generated six-digit OTP
     * @param purpose        email verification or password reset
     */
    public void sendOtp(String recipientEmail, String otpCode, String purpose) {

        try {

            String subject = buildSubject(purpose);
            String emailContent = buildEmailContent(otpCode, purpose);


            Map<String, Object> payload = Map.of(

                    "sender", Map.of(
                            "name", senderName,
                            "email", senderEmail
                    ),

                    "to", new Object[]{
                            Map.of(
                                    "email",
                                    recipientEmail
                            )
                    },

                    "subject", subject,

                    "textContent", emailContent
            );

            // Convert payload to JSON
            String jsonPayload =
                    objectMapper.writeValueAsString(payload);

            // Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_API_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("api-key", apiKey)
                    .header(
                            "Content-Type",
                            "application/json"
                    )
                    .header(
                            "Accept",
                            "application/json"
                    )
                    .POST(
                            HttpRequest.BodyPublishers.ofString(
                                    jsonPayload,
                                    StandardCharsets.UTF_8
                            )
                    )
                    .build();

            // Send request to Brevo
            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            int statusCode = response.statusCode();

            // Check if Brevo accepted the email
            if (statusCode >= 200 && statusCode < 300) {

                log.info(
                        "Brevo accepted OTP email request. Status: {}",
                        statusCode
                );

                return;
            }

            // Log a failed request
            log.error(
                    "Brevo email request failed. HTTP status: {}",
                    statusCode
            );

            // Log a limited response for troubleshooting.
            // Do not log API keys or OTP values.
            log.error(
                    "Brevo error response: {}",
                    sanitizeError(response.body())
            );

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to send verification email"
            );

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            log.error(
                    "Brevo email request interrupted",
                    ex
            );

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Email service interrupted",
                    ex
            );

        } catch (ResponseStatusException ex) {

            // Preserve the original HTTP error
            throw ex;

        } catch (Exception ex) {

            log.error(
                    "Unexpected error while sending OTP email: {}",
                    ex.getClass().getSimpleName()
            );

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Verification email service unavailable",
                    ex
            );
        }
    }

    /**
     * Creates the email subject based on OTP purpose.
     */
    private String buildSubject(String purpose) {

        if ("password reset".equalsIgnoreCase(purpose)) {
            return "SpaceSync - Password Reset OTP";
        }

        return "SpaceSync - Email Verification OTP";
    }

    /**
     * Creates the OTP email body.
     */
    private String buildEmailContent(
            String otpCode,
            String purpose
    ) {

        String action = "password reset"
                .equalsIgnoreCase(purpose)
                ? "reset your password"
                : "verify your email address";

        return """
                Hello,

                We received a request to %s for your SpaceSync account.

                Your verification code is: %s

                This OTP will expire in 10 minutes.

                Please do not share this code with anyone.

                If you did not request this action, you can ignore this email.

                Best regards,
                SpaceSync Team
                """.formatted(action, otpCode);
    }

    /**
     * Removes control characters and limits the length
     * of error responses before logging.
     */
    private String sanitizeError(String responseBody) {

        if (responseBody == null || responseBody.isBlank()) {
            return "No error response returned";
        }

        String sanitized = responseBody
                .replaceAll("[\\r\\n\\t]", " ");

        return sanitized.substring(
                0,
                Math.min(sanitized.length(), 500)
        );
    }
}

