package com.example.filesystem.exception;

import com.example.filesystem.controller.WebController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** Friendly redirect (instead of JSON) when an upload from the web page is too large. */
@ControllerAdvice(assignableTypes = WebController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tooLarge() { return "redirect:/files?tooLarge=1"; }
}
