package com.unilabs.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 — argumento inválido (validações de negócio)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, "Pedido inválido", ex.getMessage());
    }

    // 400 — body JSON mal formado ou tipo incompatível
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        String msg = "O corpo do pedido é inválido ou está mal formado.";
        if (ex.getMessage() != null && ex.getMessage().contains("ChannelType")) {
            msg = "Valor inválido para 'channelType'. Use: EMAIL, SMS ou PUSH.";
        }
        return error(HttpStatus.BAD_REQUEST, "Corpo do pedido inválido", msg);
    }

    // 400 — parâmetro de query obrigatório em falta
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        return error(HttpStatus.BAD_REQUEST, "Parâmetro em falta",
                "O parâmetro '" + ex.getParameterName() + "' é obrigatório.");
    }

    // 400 — tipo errado num parâmetro (ex: UUID inválido no path)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = "O valor '" + ex.getValue() + "' não é válido para o parâmetro '" + ex.getName() + "'.";
        if (ex.getRequiredType() != null && ex.getRequiredType().getSimpleName().equals("UUID")) {
            msg = "O identificador '" + ex.getValue() + "' não é um UUID válido.";
        }
        return error(HttpStatus.BAD_REQUEST, "Tipo de parâmetro inválido", msg);
    }

    // 404 — rota não encontrada
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Recurso não encontrado",
                "O endpoint '" + ex.getResourcePath() + "' não existe.");
    }

    // 405 — método HTTP não suportado
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido",
                "O método " + ex.getMethod() + " não é suportado neste endpoint.");
    }

    // 503 — erros de base de dados / infraestrutura
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccess(DataAccessException ex) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "Erro de base de dados",
                "Ocorreu um erro ao aceder à base de dados. Tente novamente.");
    }

    // 500 — fallback para qualquer outro erro inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor",
                "Ocorreu um erro inesperado. Contacte o administrador se o problema persistir.");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
