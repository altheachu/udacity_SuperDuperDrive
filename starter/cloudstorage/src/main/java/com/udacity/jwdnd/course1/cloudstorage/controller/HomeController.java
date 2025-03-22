package com.udacity.jwdnd.course1.cloudstorage.controller;

import com.udacity.jwdnd.course1.cloudstorage.entity.Note;
import com.udacity.jwdnd.course1.cloudstorage.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/home")
public class HomeController {

    private NoteService noteService;
    private UserService userService;
    private EncryptionService encryptionService;
    private CredentialService credentialService;
    private FileService fileService;

    public HomeController(NoteService noteService, UserService userService, CredentialService credentialService, EncryptionService encryptionService, FileService fileService){
        this.noteService = noteService;
        this.userService = userService;
        this.credentialService = credentialService;
        this.encryptionService = encryptionService;
        this.fileService = fileService;
    }
    @GetMapping
    public String getHomePage(Model model, Authentication authentication){
        Integer userId = null;
        boolean useRedisTemplate = true;
        if(useRedisTemplate){
            userId = userService.findUserIdByRedisTemplate(authentication.getName());
        } else {
            userId = userService.findUserIdByUsername(authentication.getName());
        }
        model.addAttribute("notes", noteService.findNotesByUserId(userId));
        model.addAttribute("credentials",credentialService.findCredentialsByUserId(userId));
        model.addAttribute("encryptionService",encryptionService);
        model.addAttribute("files",fileService.findFilesByUserId(userId));
        return "home";
    }



}
