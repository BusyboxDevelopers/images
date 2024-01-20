package com.example.demo.project;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    @PostMapping
    public ResponseEntity<String> createUser(
            @RequestParam("username") String username,
            @RequestParam("userEmail") String userEmail,
            @RequestBody MultipartFile file) {

        try {
            User user = new User();
            user.setUsername(username);
            user.setUserEmail(userEmail);
            user.setProfilePicture(file.getBytes());

            userRepository.save(user);

//            return new ResponseEntity<>("User created successfully", HttpStatus.CREATED);
            return new ResponseEntity<>("User created successfully", HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>("User creation failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{username}")
    public ResponseEntity<User> getUserDetails(@PathVariable String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        return userOptional.map(user -> ResponseEntity.ok().body(user))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}/pfp")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            byte[] profilePicture = user.getProfilePicture();

            if (profilePicture != null && profilePicture.length > 0) {
                return ResponseEntity.ok().body(profilePicture);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{username}/pfp")
    public ResponseEntity<String> handleImageUpload(
            @PathVariable String username,
            @RequestParam("file") MultipartFile file) {

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // Save the file data as a byte array in the database
            user.setProfilePicture(file.getBytes());
            userRepository.save(user);

            return new ResponseEntity<>("Image data saved in the database", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Image upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

