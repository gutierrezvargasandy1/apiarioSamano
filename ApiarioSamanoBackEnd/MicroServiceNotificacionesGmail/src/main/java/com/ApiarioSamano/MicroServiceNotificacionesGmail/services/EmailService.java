package com.ApiarioSamano.MicroServiceNotificacionesGmail.services;

import com.ApiarioSamano.MicroServiceNotificacionesGmail.dto.EmailRequestDTO;
import com.ApiarioSamano.MicroServiceNotificacionesGmail.exception.DestinatarioInvalidoException;
import com.ApiarioSamano.MicroServiceNotificacionesGmail.exception.EmailException;
import com.ApiarioSamano.MicroServiceNotificacionesGmail.exception.TemplateProcessingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void enviarCorreoConOtp(EmailRequestDTO dto) {
        if (dto.getDestinatario() == null || dto.getDestinatario().isEmpty()) {
            throw new DestinatarioInvalidoException(dto.getDestinatario());
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

            Context context = new Context();
            context.setVariables(dto.getVariables());

            String html = templateEngine.process("otp", context);

            helper.setTo(dto.getDestinatario());
            helper.setSubject(dto.getAsunto());
            helper.setText(html, true);

            mailSender.send(mensaje);

        } catch (MessagingException e) {
            throw new EmailException("Error al enviar el correo: " + e.getMessage());
        } catch (Exception e) {
            throw new TemplateProcessingException("Error al procesar el template del correo", e);
        }
    }

    public void enviarCorreoConContrasenaTemporal(EmailRequestDTO dto) {
        if (dto.getDestinatario() == null || dto.getDestinatario().isEmpty()) {
            throw new DestinatarioInvalidoException(dto.getDestinatario());
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

            Context context = new Context();
            context.setVariables(dto.getVariables());

            String html = templateEngine.process("contrasenaTemporal", context);

            helper.setTo(dto.getDestinatario());
            helper.setSubject(dto.getAsunto());
            helper.setText(html, true);

            mailSender.send(mensaje);

        } catch (MessagingException e) {
            throw new EmailException("Error al enviar el correo: " + e.getMessage());
        } catch (Exception e) {
            throw new TemplateProcessingException("Error al procesar el template del correo", e);
        }
    }

}
