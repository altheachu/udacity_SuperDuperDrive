package com.udacity.jwdnd.course1.cloudstorage.controller;

import com.udacity.jwdnd.course1.cloudstorage.entity.User;
import com.udacity.jwdnd.course1.cloudstorage.services.UserService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@RequestMapping("/signup")
@Controller
public class SignupController {

    private UserService userService;
    private CacheManager cacheManager;

    public SignupController(UserService userService, CacheManager cacheManager){
        this.userService = userService;
        this.cacheManager = cacheManager;
    }

    @GetMapping
    public String getSignupPage(Model model){
        return "signup";
    }

    @PostMapping
    public String doSignup(Model model, RedirectAttributes redirectAttributes, @ModelAttribute("user") User user) {

        boolean signupError = false;

        if (!userService.isUsernameAvailable(user.getUsername())) {
            signupError = true;
            model.addAttribute("errorMsg","The username already exists.");
        }

        if (signupError == false) {

            int userId = userService.createUser(user, true);
            // test code
            /*
            Cache.ValueWrapper cacheValueWrapper =cacheManager.getCache("user").get(user.getUsername());
            if(Optional.ofNullable(cacheValueWrapper).isPresent()){
                System.out.println("check value of key in store of cache: " + cacheValueWrapper.get());
            }
            */

            if (userId <= 0) {
                signupError = false;
                model.addAttribute("errorMsg","create user failed.");
            }
        }

        if (signupError == false) {
//            model.addAttribute("signupSuccess", true);
            redirectAttributes.addFlashAttribute("signupSuccess", true);
            return "redirect:/login";
        } else {
            model.addAttribute("signupError", signupError);
            return "signup";
        }
    }
}
