package com.slt.iau_portal.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, Model model) {
        logger.warn("File upload size exceeded: {}", ex.getMessage());
        model.addAttribute("error", "File upload size exceeded. Maximum size is 10MB per file.");
        return "error";
    }

    @ExceptionHandler(ComplaintProcessingException.class)
    public String handleComplaintProcessingException(ComplaintProcessingException ex, Model model) {
        logger.error("Complaint processing error: {}", ex.getMessage(), ex);
        model.addAttribute("error", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        logger.error("Unexpected error occurred", ex);
        model.addAttribute("error", "An unexpected error occurred. Please try again later.");
        return "error";
    }
}
