package com.turnosmedicos.backend.service;

public class TurnoService {
    @Service
    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final CoberturaRepository coberturaRepository;

    public TurnoService(TurnoRepository turnoRepository,
                        PacienteRepository pacienteRepository,
                        MedicoRepository medicoRepository,
                        DisponibilidadRepository disponibilidadRepository,
                        CoberturaRepository coberturaRepository) {
            this.turnoRepository = turnoRepository;
            this.pacienteRepository = pacienteRepository;
            this.medicoRepository = medicoRepository;
            this.disponibilidadRepository = disponibilidadRepository;
            this.coberturaRepository = coberturaRepository;
    }
}
