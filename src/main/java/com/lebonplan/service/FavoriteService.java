package com.lebonplan.service;

import com.lebonplan.dto.response.FavoriteResponse;
import com.lebonplan.entity.Favorite;
import com.lebonplan.entity.Post;
import com.lebonplan.entity.User;
import com.lebonplan.exception.ResourceNotFoundException;
import com.lebonplan.repository.FavoriteRepository;
import com.lebonplan.repository.PostRepository;
import com.lebonplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PostRepository     postRepository;
    private final UserRepository     userRepository;

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getMyFavorites(String userEmail) {
        User user = findUserOrThrow(userEmail);
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    @Transactional
    public Map<String, Object> toggle(UUID postId, String userEmail) {
        User user = findUserOrThrow(userEmail);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce introuvable : " + postId));

        boolean exists = favoriteRepository.existsByUserIdAndPostId(user.getId(), postId);

        if (exists) {
            favoriteRepository.deleteByUserIdAndPostId(user.getId(), postId);
            return Map.of("favorited", false, "postId", postId);
        } else {
            Favorite fav = Favorite.builder()
                    .user(user)
                    .post(post)
                    .build();
            favoriteRepository.save(fav);
            return Map.of("favorited", true, "postId", postId);
        }
    }

    @Transactional(readOnly = true)
    public boolean isFavorited(UUID postId, String userEmail) {
        User user = findUserOrThrow(userEmail);
        return favoriteRepository.existsByUserIdAndPostId(user.getId(), postId);
    }

    @Transactional(readOnly = true)
    public long countByPost(UUID postId) {
        return favoriteRepository.countByPostId(postId);
    }

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }
}