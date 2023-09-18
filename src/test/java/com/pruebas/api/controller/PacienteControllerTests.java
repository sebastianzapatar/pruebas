package com.pruebas.api.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pruebas.api.entity.Paciente;
import com.pruebas.api.exception.InvalidRequestException;
import com.pruebas.api.exception.NotFoundException;
import com.pruebas.api.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@WebMvcTest(PacienteController.class) //espicifica a Spring Boot que se utiliza para probar controladores
public class PacienteControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PacienteService pacienteService;

    Paciente PACIENTE_001 = new Paciente(1l,"Christian Ramirez",17,"c1@gmail.com");
    Paciente PACIENTE_002 = new Paciente(2l,"Julen Oliva",18,"j1@gmail.com");
    Paciente PACIENTE_003 = new Paciente(3l,"Raul Castillo",19,"r1@gmail.com");

    @Test
    void testListarPacientes() throws Exception {
        List<Paciente> pacientes = new ArrayList<>(Arrays.asList(PACIENTE_001,PACIENTE_002,PACIENTE_003));

        Mockito.when(pacienteService.getAllPacientes()).thenReturn(pacientes);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/pacientes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(3)))
                .andExpect(jsonPath("$[2].nombre",is("Raul Castillo")));
    }

    @Test
    void testListarPacientesPorId() throws Exception {
        Mockito.when(pacienteService.getPacienteById(PACIENTE_001.getPacienteId()))
                .thenReturn(Optional.of(PACIENTE_001));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/pacientes/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",notNullValue()))
                .andExpect(jsonPath("$.nombre",is("Christian Ramirez")));
    }

    @Test
    void testGuardarPaciente() throws Exception {
        Paciente paciente = Paciente.builder()
                .pacienteId(4l)
                .nombre("Olga Lopez")
                .edad(19)
                .correo("o2@gmail.com")
                .build();

        Mockito.when(pacienteService.createPaciente(paciente)).thenReturn(paciente);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/api/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paciente));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",notNullValue()))
                .andExpect(jsonPath("$.nombre",is("Olga Lopez")));
    }

    @Test
    void testActualizarPacienteConExito() throws Exception {
        Paciente pacienteUpdate = Paciente.builder()
                .pacienteId(1l)
                .nombre("Christian Raul Ramirez")
                .edad(18)
                .correo("c222@gmail.com")
                .build();

        Mockito.when(pacienteService.getPacienteById(PACIENTE_001.getPacienteId())).thenReturn(Optional.of(PACIENTE_001));
        Mockito.when(pacienteService.updatePaciente(pacienteUpdate)).thenReturn(pacienteUpdate);

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/api/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pacienteUpdate));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",notNullValue()))
                .andExpect(jsonPath("$.nombre",is("Christian Raul Ramirez")));
    }

    @Test
    void testActualizarPacienteNoEncontrado() throws Exception {
        Paciente pacienteUpdate = Paciente.builder()
                .pacienteId(9l)
                .nombre("Christian Raul Ramirez")
                .edad(18)
                .correo("c222@gmail.com")
                .build();

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/api/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pacienteUpdate));

        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof NotFoundException
                ))
                .andExpect(result -> assertEquals("Paciente con el ID : " + pacienteUpdate.getPacienteId() + " no existe",result.getResolvedException().getMessage()));
    }

    @Test
    void testActualizarPacienteConIdNulo() throws Exception {
        Paciente pacienteUpdate = Paciente.builder()
                .nombre("Christian Raul Ramirez")
                .edad(18)
                .correo("c222@gmail.com")
                .build();

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/api/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pacienteUpdate));

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof InvalidRequestException
                ))
                .andExpect(result -> assertEquals("Los datos del paciente no pueden ser nulos",result.getResolvedException().getMessage()));
    }

    @Test
    void testEliminarPacienteConExito() throws Exception {
        Mockito.when(pacienteService.getPacienteById(PACIENTE_002.getPacienteId())).thenReturn(Optional.of(PACIENTE_002));

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/pacientes/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testEliminarPacienteNoEncontrado() throws Exception {
        Mockito.when(pacienteService.getPacienteById(10l)).thenReturn(Optional.of(PACIENTE_002));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/pacientes/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof NotFoundException));
    }
}
