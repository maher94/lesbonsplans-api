package com.lebonplan.service;

import com.lebonplan.dto.request.PostRequest;
import com.lebonplan.dto.request.PostSearchRequest;
import com.lebonplan.dto.response.ImageResponse;
import com.lebonplan.dto.response.PageResponse;
import com.lebonplan.dto.response.PostResponse;
import com.lebonplan.entity.Category;
import com.lebonplan.entity.Image;
import com.lebonplan.entity.Post;
import com.lebonplan.entity.User;
import com.lebonplan.exception.ResourceNotFoundException;
import com.lebonplan.repository.CategoryRepository;
import com.lebonplan.repository.ImageRepository;
import com.lebonplan.repository.PostRepository;
import com.lebonplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository     postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository     userRepository;
    private final ImageRepository    imageRepository;
    private final CloudinaryService  cloudinaryService;

    // ── Lecture ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> search(PostSearchRequest req) {

        Sort sort = switch (req.sortBy()) {
            case "viewsCount" -> Sort.by(Sort.Direction.DESC, "viewsCount");
            case "eventDate"  -> Sort.by(Sort.Direction.ASC, "eventDate");
            default           -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(req.page(), req.size(), sort);

        String search = isBlank(req.search())
                ? null
                : "%" + req.search().trim().toLowerCase() + "%";

        String city = isBlank(req.city())
                ? null
                : "%" + req.city().trim().toLowerCase() + "%";

        Integer categoryId = req.categoryId();

        Page<Post> page = postRepository.search(
                search,
                city,
                categoryId,
                pageable
        );

        return PageResponse.from(page.map(PostResponse::summary));
    }
    @Transactional
    public PostResponse getById(UUID id) {
        Post post = findActiveOrThrow(id);
        postRepository.incrementViews(id);
        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getFeatured(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findFeatured(pageable)
                .stream()
                .map(PostResponse::summary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getByUser(UUID userId) {
        return postRepository.findByUserId(userId)
                .stream()
                .map(PostResponse::summary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getNearby(double lat, double lng, double radiusKm, int limit) {
        return postRepository.findNearby(lat, lng, radiusKm, limit)
                .stream()
                .map(PostResponse::summary)
                .toList();
    }

    // ── Écriture ─────────────────────────────────────────────

    @Transactional
    public PostResponse create(PostRequest request, String userEmail) {
        User user         = findUserOrThrow(userEmail);
        Category category = findCategoryOrThrow(request.categoryId());

        Post post = Post.builder()
                .user(user)
                .category(category)
                .title(request.title())
                .description(request.description())
                .address(request.address())
                .city(request.city())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .eventDate(request.eventDate())
                .eventTime(request.eventTime())
                .price(request.price())
                .priceLabel(request.priceLabel())
                .sourceUrl(request.sourceUrl())
                .status(Post.PostStatus.ACTIVE)
                .build();

        if (request.expireInDays() != null && request.expireInDays() > 0) {
            post.setExpiresAt(Instant.now().plusSeconds(request.expireInDays() * 86400L));
        }

        return PostResponse.from(postRepository.save(post));
    }

    @Transactional
    public PostResponse update(UUID id, PostRequest request, String userEmail) {
        Post post = findActiveOrThrow(id);
        checkOwnerOrAdmin(post, userEmail);

        Category category = findCategoryOrThrow(request.categoryId());

        post.setCategory(category);
        post.setTitle(request.title());
        post.setDescription(request.description());
        post.setAddress(request.address());
        post.setCity(request.city());
        post.setLatitude(request.latitude());
        post.setLongitude(request.longitude());
        post.setEventDate(request.eventDate());
        post.setEventTime(request.eventTime());
        post.setPrice(request.price());
        post.setPriceLabel(request.priceLabel());
        post.setSourceUrl(request.sourceUrl());

        if (request.expireInDays() != null && request.expireInDays() > 0) {
            post.setExpiresAt(Instant.now().plusSeconds(request.expireInDays() * 86400L));
        }

        return PostResponse.from(postRepository.save(post));
    }

    @Transactional
    public void delete(UUID id, String userEmail) {
        Post post = findOrThrow(id);
        checkOwnerOrAdmin(post, userEmail);
        post.setStatus(Post.PostStatus.DELETED);
        postRepository.save(post);
        log.info("Post {} supprimé par {}", id, userEmail);
    }

    // ── Images ───────────────────────────────────────────────

    @Transactional
    public ImageResponse addImage(UUID postId, MultipartFile file, String userEmail) throws IOException {
        Post post = findActiveOrThrow(postId);
        checkOwnerOrAdmin(post, userEmail);

        if (imageRepository.countByPostId(postId) >= 5) {
            throw new IllegalStateException("Maximum 5 images par annonce");
        }

        Map<String, String> uploaded = cloudinaryService.upload(file, "lebonplan/posts/" + postId);

        int position = (int) imageRepository.countByPostId(postId);
        Image image = Image.builder()
                .post(post)
                .url(uploaded.get("url"))
                .publicId(uploaded.get("publicId"))
                .position(position)
                .build();

        return ImageResponse.from(imageRepository.save(image));
    }

    @Transactional
    public void deleteImage(UUID postId, UUID imageId, String userEmail) {
        Post post = findActiveOrThrow(postId);
        checkOwnerOrAdmin(post, userEmail);

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image introuvable"));

        cloudinaryService.delete(image.getPublicId());
        imageRepository.delete(image);
    }
    @Transactional(readOnly = true)
    public List<PostResponse> getMyPosts(String username) {

        User user = findUserOrThrow(username);

        return postRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(PostResponse::summary)
                .toList();
    }
    // ── Helpers ──────────────────────────────────────────────

    private Post findActiveOrThrow(UUID id) {
        Post post = findOrThrow(id);
        if (post.getStatus() == Post.PostStatus.DELETED) {
            throw new ResourceNotFoundException("Annonce introuvable : " + id);
        }
        return post;
    }

    private Post findOrThrow(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce introuvable : " + id));
    }

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + email));
    }

    private Category findCategoryOrThrow(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + id));
    }

    private void checkOwnerOrAdmin(Post post, String userEmail) {
        boolean isOwner = post.getUser().getEmail().equals(userEmail);
        boolean isAdmin = userRepository.findByEmail(userEmail)
                .map(u -> u.getRole() == User.Role.ADMIN)
                .orElse(false);
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cette annonce");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
    
}