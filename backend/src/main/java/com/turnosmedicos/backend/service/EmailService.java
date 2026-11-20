package com.turnosmedicos.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreoRecuperacion(String destinatario, String token) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Recuperación de contraseña - NEXA Salud");
        mensaje.setText("Usá este código para restablecer tu contraseña: " + token
                + "\n\nEste código vence en 30 minutos. Si no pediste este cambio, ignorá este mensaje.");
        mailSender.send(mensaje);
    }
}
