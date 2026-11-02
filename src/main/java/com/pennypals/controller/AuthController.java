package com.pennypals.controller;

import com.pennypals.dto.UserRegistrationDto;
import com.pennypals.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registrationDto") UserRegistrationDto registrationDto,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // Grab the first validation error message to show the user
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/?registerError";
        }

        try {
            // Capture raw password before hashing for immediate login
            String rawPassword = registrationDto.getPassword();

            // Register user and provision their starting pennies profile
            userService.registerUser(registrationDto);

            // Automatically authenticate the new user session
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(registrationDto.getUsername(), rawPassword)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/?registerError";
        }

        return "redirect:/aquarium";
    }

    @PostMapping("/forgot-password/fetch-question")
    public String fetchSecurityQuestion(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            com.pennypals.model.User user = userService.findByEmail(email);
            String questionText = userService.getSecurityQuestionText(user.getSecurityQuestion());

            redirectAttributes.addFlashAttribute("securityQuestion", questionText);

            return "redirect:/?resetStep2&email=" + java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email address not found.");
            return "redirect:/?resetErrorStep1";
        }
    }

    @PostMapping("/forgot-password/reset")
    public String resetPassword(@RequestParam("email") String email,
                                @RequestParam("securityAnswer") String securityAnswer,
                                @RequestParam("newPassword") String newPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(email, securityAnswer, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password successfully reset! Please sign in with your new password.");
            return "redirect:/?resetSuccess";
        } catch (IllegalArgumentException e) {
            try {
                com.pennypals.model.User user = userService.findByEmail(email);
                String questionText = userService.getSecurityQuestionText(user.getSecurityQuestion());
                redirectAttributes.addFlashAttribute("securityQuestion", questionText);
            } catch (Exception ex) {
            }

            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/?resetErrorStep2";
        }
    }
}