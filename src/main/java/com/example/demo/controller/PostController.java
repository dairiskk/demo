package com.example.demo.controller;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostController(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // 📌 Get all posts
    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    // 📌 Get posts by userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(postRepository.findByUser(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 📌 Create post for user
    @PostMapping("/user/{userId}")
    public ResponseEntity<Post> createPost(
            @PathVariable Long userId,
            @RequestBody Post postRequest) {

        return userRepository.findById(userId)
                .map(user -> {
                    postRequest.setUser(user);
                    Post savedPost = postRepository.save(postRequest);
                    return ResponseEntity.ok(savedPost);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
