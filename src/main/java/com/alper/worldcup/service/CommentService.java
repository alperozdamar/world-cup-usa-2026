package com.alper.worldcup.service;

import com.alper.worldcup.dao.UserCommentRepository;
import com.alper.worldcup.entity.UserComment;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private static final int MAX_CONTENT_LENGTH = 2000;

    private final UserCommentRepository userCommentRepository;

    public CommentService(UserCommentRepository userCommentRepository) {
        this.userCommentRepository = userCommentRepository;
    }

    public List<UserComment> getAllComments() {
        return userCommentRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void addComment(String username, String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Please enter a comment.");
        }
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Comment is too long (max " + MAX_CONTENT_LENGTH + " characters).");
        }
        userCommentRepository.save(new UserComment(username, trimmed, Instant.now()));
    }
}
