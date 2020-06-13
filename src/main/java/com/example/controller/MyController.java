package com.example.controller;

import com.example.websocket.MyWsServer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @Description
 * @ClassName MyController
 * @Author User
 * @date 2020.06.12 23:05
 */
@RestController
@RequestMapping(("/api/v1"))
public class MyController {
    @GetMapping("/index")
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("<h1>请求成功</h1>");
    }

    @GetMapping("/pushMessage/{fromUserId}/{toUserId}")
    public ResponseEntity<String> pushToClients(@RequestParam("message") String message, @PathVariable("fromUserId") String fromUserId, @PathVariable("toUserId") String toUserId) throws IOException {
        MyWsServer.sendInfo(message, fromUserId, toUserId);
        return ResponseEntity.ok("MSG SEND SUCCESS");
    }
}
