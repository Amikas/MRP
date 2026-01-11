package mrp.di;

import mrp.repository.*;
import mrp.repository.interfaces.*;
import mrp.service.*;
import mrp.service.interfaces.*;

import java.sql.SQLException;

/**
 * Simple dependency injection container/factory for the MRP application
 */
public class DIContainer {

    // Repository instances (singleton-like for this simple implementation)
    private static IUserRepository userRepository;
    private static IMediaRepository mediaRepository;
    private static IRatingRepository ratingRepository;
    private static IFavoriteRepository favoriteRepository;

    // Service instances
    private static IUserService userService;
    private static IMediaService mediaService;
    private static IRatingService ratingService;
    private static IFavoriteService favoriteService;
    private static IRecommendationService recommendationService;

    static {
        try {
            // Initialize repositories
            userRepository = (IUserRepository) new UserRepository();
            mediaRepository = (IMediaRepository) new MediaRepository();
            ratingRepository = (IRatingRepository) new RatingRepository();
            favoriteRepository = (IFavoriteRepository) new FavoriteRepository();

            // Initialize services with their dependencies
            userService = new UserService(userRepository);
            mediaService = new MediaService(mediaRepository, userRepository);
            ratingService = new RatingService(ratingRepository, mediaRepository, userRepository);
            favoriteService = new FavoriteService(favoriteRepository, mediaRepository, userRepository);
            recommendationService = new RecommendationService(mediaRepository, ratingRepository, userRepository);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DI container", e);
        }
    }

    // Repository getters
    public static IUserRepository getUserRepository() {
        return userRepository;
    }

    public static IMediaRepository getMediaRepository() {
        return mediaRepository;
    }

    public static IRatingRepository getRatingRepository() {
        return ratingRepository;
    }

    public static IFavoriteRepository getFavoriteRepository() {
        return favoriteRepository;
    }

    // Service getters
    public static IUserService getUserService() {
        return userService;
    }

    public static IMediaService getMediaService() {
        return mediaService;
    }

    public static IRatingService getRatingService() {
        return ratingService;
    }

    public static IFavoriteService getFavoriteService() {
        return favoriteService;
    }

    public static IRecommendationService getRecommendationService() {
        return recommendationService;
    }
}