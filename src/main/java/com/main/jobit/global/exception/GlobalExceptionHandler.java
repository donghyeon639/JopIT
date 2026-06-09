package com.main.jobit.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

// 전역 예외 처리기. @RestControllerAdvice로 모든 @RestController에서 던져진 예외를 한곳에서 잡아
// 일관된 JSON 에러 응답으로 변환한다. 컨트롤러마다 try-catch를 반복하지 않기 위한 공통 처리.
// 도메인 전용 예외 클래스를 따로 두지 않고 Spring 내장 예외(+ ResponseStatusException)를 활용하는 컨벤션.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 잘못된 입력값으로 인한 IllegalArgumentException -> 400. 본문은 {"message": ...} 형태로 통일.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    // 로그인 실패(비밀번호 불일치 등) -> 401. Spring Security가 던지는 인증 예외를 받는다.
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", e.getMessage()));
    }

    // @Valid 검증 실패 -> 400. 필드별 메시지를 {필드명: 메시지} 맵으로 묶어 프런트가 폼 단위로 표시하기 쉽게 한다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                // 같은 필드에 에러가 둘 이상이면 먼저 들어온 메시지를 유지(merge 함수 (a,b)->a).
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
                        (a, b) -> a));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}

