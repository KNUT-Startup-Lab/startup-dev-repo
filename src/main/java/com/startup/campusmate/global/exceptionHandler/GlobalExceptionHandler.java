package com.startup.campusmate.global.exceptionHandler;

import com.startup.campusmate.global.exceptions.GlobalException;
import com.startup.campusmate.global.rsData.RsData;
import com.startup.campusmate.standard.base.Empty;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        return handleApiException(ex);
    }

    private ResponseEntity<Object> handleApiException(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("resultCode", "500-1");
        body.put("statusCode", 500);
        body.put("msg", ex.getLocalizedMessage());

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        body.put("data", data);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        data.put("trace", sw.toString().replace("\t", "    ").split("\\r\\n"));

        String path = req.getRequestURI();
        data.put("path", path);

        body.put("success", false);
        body.put("fail", true);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(GlobalException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @RequestMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<RsData<Empty>> handle(GlobalException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getRsData().getStatusCode());
        resp.setStatus(ex.getRsData().getStatusCode());
        return new ResponseEntity<>(ex.getRsData(), status);
    }
}
