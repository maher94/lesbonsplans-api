package com.lebonplan.service;

import com.lebonplan.dto.request.CommentRequest;
import com.lebonplan.dto.response.CommentResponse;
import com.lebonplan.entity.Comment;
import com.lebonplan.entity.Post;
import com.lebonplan.entity.User;
import com.lebonplan.exception.ResourceNotFoundException;
import com.lebonplan.repository.CommentRepository;
import com.lebonplan.repository.PostRepository;
import com.lebonplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository    postRepository;
    private final UserRepository    userRepository;

    @Transactional(readOnly = true)
    public List<CommentResponse> getByPost(UUID postId) {
    	return commentRepository.findByPostIdNotDeleted(postId)
    	        .stream()
    	        .map(CommentResponse::from)
    	        .toList();
    }

    @Transactional
    public CommentResponse create(UUID postId, CommentRequest request, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce introuvable"));
        User user = findUserOrThrow(userEmail);

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(request.content())
                .build();

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponse update(UUID id, CommentRequest request, String userEmail) {
        Comment comment = findOrThrow(id);
        checkOwnerOrAdmin(comment, userEmail);

        comment.setContent(request.content());
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public void delete(UUID id, String userEmail) {
        Comment comment = findOrThrow(id);
        checkOwnerOrAdmin(comment, userEmail);
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    private Comment findOrThrow(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire introuvable"));
    }

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private void checkOwnerOrAdmin(Comment comment, String userEmail) {
        boolean isOwner = comment.getUser().getEmail().equals(userEmail);
        boolean isAdmin = userRepository.findByEmail(userEmail)
                .map(u -> u.getRole() == User.Role.ADMIN)
                .orElse(false);
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier ce commentaire");
        }
    }
}