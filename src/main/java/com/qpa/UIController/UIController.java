package com.qpa.UIController;

import com.qpa.dto.LocationDTO;
import com.qpa.dto.RegisterDTO;
import com.qpa.dto.SpotCreateDTO;
import com.qpa.entity.PriceType;
import com.qpa.entity.SpotType;
import com.qpa.entity.VehicleType;
import com.qpa.service.AuthService;
import com.qpa.service.SpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping("/")  // Base mapping for the controller
public class UIController {

    private final AuthService authService;
    private final SpotService spotService;

    @Autowired
    public UIController(AuthService authService, SpotService spotService) {
        this.authService = authService;
        this.spotService = spotService;
    }

    @GetMapping("/")
    public String showRootPage() {
        return "login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "register";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";  // Refers to home.html in templates
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterDTO registerDTO) {
        authService.createUser(
                registerDTO.getUsername(),
                registerDTO.getPassword(),
                registerDTO.getRoles()
        );
        return "redirect:/login?registered";  // Fixed redirect URL
    }

    @PostMapping("/spots/create")
    public String createSpot(@ModelAttribute SpotCreateDTO spotCreateDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "spot_create";
        }

        // Ensure empty collections are initialized
        if (spotCreateDTO.getSupportedVehicle() == null) {
            spotCreateDTO.setSupportedVehicle(new HashSet<>());
        }

        // Create empty images list as you mentioned
        List<MultipartFile> images = new ArrayList<>();

        // Save the spot
        spotService.createSpot(spotCreateDTO, images);
        return "redirect:/spots/list";
    }

    @GetMapping("/spots/create")
    public String showCreateSpotForm(Model model) {
        // Initialize the DTO with empty objects to avoid null pointer exceptions
        SpotCreateDTO spotCreateDTO = new SpotCreateDTO();
        spotCreateDTO.setLocation(new LocationDTO());
        spotCreateDTO.setSupportedVehicle(new HashSet<>());

        model.addAttribute("spotCreateDTO", spotCreateDTO);
        model.addAttribute("spotTypes", SpotType.values());
        model.addAttribute("priceTypes", PriceType.values());
        model.addAttribute("vehicleTypes", VehicleType.values());

        return "spot_create";
    }

    @GetMapping("/spots/list")  // Fixed URL to match templates
    public String showSpotList(Model model) {
        model.addAttribute("spots", spotService.getAllSpots());
        return "spot_list";
    }
}