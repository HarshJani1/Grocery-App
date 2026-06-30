package com.grocery.service_email.service;

import com.grocery.service_email.dto.CheckoutEvent;
import com.grocery.service_email.dto.CheckoutItemDTO;
import com.grocery.service_email.dto.WelcomeEvent;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String SENDER = "harshjani399@gmail.com";
    private static final String APP_NAME = "Grocery App";

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(WelcomeEvent event) {
        log.info("Sending welcome email | to={}", event.getEmail());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(SENDER);
            helper.setTo(event.getEmail());
            helper.setSubject("Welcome to " + APP_NAME + "! 🛒");
            helper.setText(buildWelcomeHtml(event.getUsername()), true); // true = HTML

            mailSender.send(message);
            log.info("Welcome email sent successfully | to={}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email | to={} | error={}", event.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Bill / Checkout Email
    // ──────────────────────────────────────────────────────────────

    public void sendBillEmail(CheckoutEvent event) {
        log.info("Sending bill email | to={} | items={} | total={}",
                event.getEmail(), event.getItems().size(), event.getTotalAmount());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(SENDER);
            helper.setTo(event.getEmail());
            helper.setSubject("Your " + APP_NAME + " Order Invoice 🧾");
            helper.setText(buildBillHtml(event), true);

            mailSender.send(message);
            log.info("Bill email sent successfully | to={} | total={}", event.getEmail(), event.getTotalAmount());
        } catch (Exception e) {
            log.error("Failed to send bill email | to={} | error={}", event.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send bill email", e);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // HTML Builders
    // ──────────────────────────────────────────────────────────────

    private String buildWelcomeHtml(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 12px;
                                 overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #2ecc71, #27ae60); padding: 40px 30px; text-align: center; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 28px; letter-spacing: -0.5px; }
                    .header p  { color: #d5f5e3; margin: 8px 0 0; font-size: 14px; }
                    .body   { padding: 36px 30px; color: #333333; }
                    .body h2 { font-size: 22px; margin: 0 0 12px; }
                    .body p  { font-size: 15px; line-height: 1.7; color: #555555; }
                    .cta    { text-align: center; margin: 28px 0; }
                    .cta a  { background: #2ecc71; color: #fff; padding: 14px 32px; border-radius: 8px;
                              text-decoration: none; font-weight: bold; font-size: 15px; }
                    .footer { background: #f9f9f9; text-align: center; padding: 18px; font-size: 12px; color: #aaaaaa; }
                  </style>
                </head>
                <body>
                  <div class="container">
                    <div class="header">
                      <h1>🛒 Grocery App</h1>
                      <p>Fresh groceries, delivered to your door</p>
                    </div>
                    <div class="body">
                      <h2>Welcome, %s! 👋</h2>
                      <p>We're thrilled to have you on board. Your account is all set and ready to go.</p>
                      <p>Browse hundreds of fresh products, add them to your cart, and check out in seconds.</p>
                      <div class="cta">
                        <a href="#">Start Shopping Now</a>
                      </div>
                      <p style="font-size:13px; color:#aaa;">If you did not create this account, please ignore this email.</p>
                    </div>
                    <div class="footer">
                      &copy; 2026 Grocery App &nbsp;|&nbsp; Sent from harshjani399@gmail.com
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(username);
    }

    private String buildBillHtml(CheckoutEvent event) {
        StringBuilder rows = new StringBuilder();
        for (CheckoutItemDTO item : event.getItems()) {
            BigDecimal lineTotal = item.getLineTotal();
            rows.append("""
                    <tr>
                      <td style="padding:10px 12px; border-bottom:1px solid #f0f0f0;">%s</td>
                      <td style="padding:10px 12px; border-bottom:1px solid #f0f0f0; color:#777; font-size:13px;">%s</td>
                      <td style="padding:10px 12px; border-bottom:1px solid #f0f0f0; text-align:center;">%d</td>
                      <td style="padding:10px 12px; border-bottom:1px solid #f0f0f0; text-align:right;">₹%.2f</td>
                      <td style="padding:10px 12px; border-bottom:1px solid #f0f0f0; text-align:right; font-weight:600;">₹%.2f</td>
                    </tr>
                    """.formatted(
                    item.getName(),
                    item.getDescription() != null ? item.getDescription() : "",
                    item.getQuantity(),
                    item.getPrice(),
                    lineTotal
            ));
        }

        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 0; }
                    .container { max-width: 650px; margin: 40px auto; background: #ffffff; border-radius: 12px;
                                 overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #3498db, #2980b9); padding: 36px 30px; text-align: center; }
                    .header h1 { color: #fff; margin: 0; font-size: 26px; }
                    .header p  { color: #d6eaf8; margin: 6px 0 0; font-size: 13px; }
                    .body   { padding: 30px; }
                    .body p  { font-size: 15px; color: #555; line-height: 1.6; }
                    table   { width: 100%%; border-collapse: collapse; margin-top: 20px; font-size: 14px; }
                    thead th { background: #2980b9; color: #fff; padding: 10px 12px; text-align: left; }
                    thead th:nth-child(3),
                    thead th:nth-child(4),
                    thead th:nth-child(5) { text-align: right; }
                    .total-row td { background: #f0f8ff; font-weight: bold; font-size: 15px;
                                    padding: 14px 12px; border-top: 2px solid #2980b9; }
                    .footer { background: #f9f9f9; text-align: center; padding: 18px; font-size: 12px; color: #aaaaaa; }
                  </style>
                </head>
                <body>
                  <div class="container">
                    <div class="header">
                      <h1>🧾 Order Invoice</h1>
                      <p>Thank you for shopping at Grocery App!</p>
                    </div>
                    <div class="body">
                      <p>Hi there! Here's a summary of your order. We hope you enjoy every item! 🥦🥛🍞</p>
                      <table>
                        <thead>
                          <tr>
                            <th>Item</th>
                            <th>Description</th>
                            <th style="text-align:center;">Qty</th>
                            <th style="text-align:right;">Unit Price</th>
                            <th style="text-align:right;">Subtotal</th>
                          </tr>
                        </thead>
                        <tbody>
                          %s
                        </tbody>
                        <tfoot>
                          <tr class="total-row">
                            <td colspan="4" style="text-align:right; padding-right:16px;">Grand Total</td>
                            <td style="text-align:right;">₹%.2f</td>
                          </tr>
                        </tfoot>
                      </table>
                    </div>
                    <div class="footer">
                      &copy; 2026 Grocery App &nbsp;|&nbsp; Sent from harshjani399@gmail.com
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(rows.toString(), event.getTotalAmount());
    }
}
