package com.turnosmedicos.backend.service;

import com.turnosmedicos.backend.dto.ConfirmarRecuperacionRequestDTO;
import com.turnosmedicos.backend.dto.LoginRequestDTO;
import com.turnosmedicos.backend.dto.LoginResponseDTO;
import com.turnosmedicos.backend.dto.MensajeResponseDTO;
import com.turnosmedicos.backend.dto.RegistroRequestDTO;
import com.turnosmedicos.backend.dto.RegistroResponseDTO;
import com.turnosmedicos.backend.dto.SolicitarRecuperacionRequestDTO;
import com.turnosmedicos.backend.entity.Paciente;
import com.turnosmedicos.backend.entity.PasswordResetToken;
import com.turnosmedicos.backend.entity.Usuario;
import com.turnosmedicos.backend.enums.Rol;
import com.turnosmedicos.backend.exception.ConflictoException;
import com.turnosmedicos.backend.exception.DatosInvalidosException;
import com.turnosmedicos.backend.repository.PacienteRepository;
import com.turnosmedicos.backend.repository.PasswordResetTokenRepository;
import com.turnosmedicos.backend.repository.UsuarioRepository;
import com.turnosmedicos.backend.security.JwtService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(UsuarioRepository usuarioRepository, PacienteRepository pacienteRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @Transactional
    public RegistroResponseDTO registrar(RegistroRequestDTO request) {
        // Busca puntualmente la fila PACIENTE de ese dni: si el mismo dni ya
        // tiene una fila ADMINISTRADOR, el registro no debe tocarla (un mismo dni
        // puede ser PACIENTE y ADMINISTRADOR a la vez).
        Usuario usuario = usuarioRepository.findByDniAndRol(request.dni(), Rol.PACIENTE).orElse(null);

        if (usuario != null && usuario.getActivo()) {
            throw new ConflictoException("Ya existe un usuario activo con ese DNI");
        }

        if (usuario == null) {
            usuario = Usuario.builder()
                    .dni(request.dni())
                    .contrasena(passwordEncoder.encode(request.contrasena()))
                    .rol(Rol.PACIENTE)
                    .build();
        } else {
            usuario.setActivo(true);
            usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
            usuario.setRol(Rol.PACIENTE);
        }
        usuario = usuarioRepository.save(usuario);

        Paciente paciente = pacienteRepository.findByDni(request.dni()).orElse(null);

        if (paciente == null) {
            paciente = Paciente.builder()
                    .usuario(usuario)
                    .nombreApellido(request.nombreApellido())
                    .dni(request.dni())
                    .fechaNacimiento(request.fechaNacimiento())
                    .email(request.email())
                    .celular(request.celular())
                    .build();
        } else {
            paciente.setUsuario(usuario);
            paciente.setActivo(true);
            paciente.setNombreApellido(request.nombreApellido());
            paciente.setFechaNacimiento(request.fechaNacimiento());
            paciente.setEmail(request.email());
            paciente.setCelular(request.celular());
        }
        paciente = pacienteRepository.save(paciente);

        return new RegistroResponseDTO(usuario.getId(), paciente.getId(), "Registro exitoso.");
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        List<Usuario> activosConEseDni = usuarioRepository.findByDni(request.dni()).stream()
                .filter(Usuario::getActivo)
                .toList();

        Usuario usuario;
        if (request.rol() != null) {
            usuario = activosConEseDni.stream()
                    .filter(u -> u.getRol() == request.rol())
                    .findFirst()
                    .orElseThrow(() -> new BadCredentialsException("DNI o contraseña incorrectos"));
        } else if (activosConEseDni.size() > 1) {
            throw new DatosInvalidosException(
                    "Ese DNI tiene más de un rol activo; indicá con cuál querés ingresar");
        } else {
            usuario = activosConEseDni.stream()
                    .findFirst()
                    .orElseThrow(() -> new BadCredentialsException("DNI o contraseña incorrectos"));
        }

        if (!passwordEncoder.matches(request.contrasena(), usuario.getContrasena())) {
            throw new BadCredentialsException("DNI o contraseña incorrectos");
        }

        String token = jwtService.generarToken(usuario.getDni(), usuario.getRol().name());

        return new LoginResponseDTO(token, usuario.getRol());
    }

    public MensajeResponseDTO solicitarRecuperacion(SolicitarRecuperacionRequestDTO request) {
        pacienteRepository.findByEmail(request.email()).ifPresent(paciente -> {
            Usuario usuario = paciente.getUsuario();
            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .usuario(usuario)
                    .token(token)
                    .fechaExpiracion(LocalDateTime.now().plusMinutes(30))
                    .build();

            passwordResetTokenRepository.save(resetToken);
            emailService.enviarCorreoRecuperacion(request.email(), token);
        });

        return new MensajeResponseDTO("Si el email esta registrado, te enviamos un correo con instrucciones.");
    }

    public MensajeResponseDTO confirmarRecuperacion(ConfirmarRecuperacionRequestDTO request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BadCredentialsException("El token no es valido"));

        if (resetToken.getUsado() || resetToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("El token no es valido o ya expiro");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setContrasena(passwordEncoder.encode(request.contrasenaNueva()));
        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        passwordResetTokenRepository.save(resetToken);

        return new MensajeResponseDTO("Contraseña actualizada correctamente.");
    }
}
