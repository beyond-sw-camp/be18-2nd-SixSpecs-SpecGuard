package com.beyond.specguard.company.invite.controller;

import com.beyond.specguard.company.invite.dto.InviteRequestDTO;
import com.beyond.specguard.company.invite.dto.InviteResponseDTO;
import com.beyond.specguard.company.invite.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//지금은 restcontroller로 두고 뷰 반환이 될때  controller로 바꾸자 까먹지말고
@RestController
@RequestMapping("/api/v1/invite")
@RequiredArgsConstructor

public class InviteController {
    private final InviteService inviteService;

    @PostMapping
    public ResponseEntity<InviteResponseDTO> sendInvite(@RequestBody @Validated InviteRequestDTO request){
        InviteResponseDTO response = inviteService.sendInvite(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
