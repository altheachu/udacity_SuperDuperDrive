package com.udacity.jwdnd.course1.cloudstorage.controller;

import com.udacity.jwdnd.course1.cloudstorage.entity.User;
import com.udacity.jwdnd.course1.cloudstorage.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("/signup")
@Controller
public class SignupController {

    private UserService userService;

    public SignupController(UserService userService){
        this.userService = userService;
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
