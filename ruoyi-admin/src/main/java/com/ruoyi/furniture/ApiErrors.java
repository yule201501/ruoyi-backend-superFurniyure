package com.ruoyi.furniture;

import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.ruoyi.furniture")
public class ApiErrors {
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<AjaxResult> status(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(
            AjaxResult.error(
                e.getStatusCode().value(),
                e.getReason() == null ? "Request failed" : e.getReason()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<AjaxResult> validation(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(AjaxResult.error(400, "Please check all required fields"));
  }

  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<AjaxResult> duplicate(DuplicateKeyException e) {
    return ResponseEntity.status(409)
        .body(AjaxResult.error(409, "Duplicate identifier. Reload and retry."));
  }
}
