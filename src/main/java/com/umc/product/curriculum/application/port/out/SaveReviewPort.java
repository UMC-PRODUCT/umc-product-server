package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.Review;

public interface SaveReviewPort {

    Review save(Review review);
}
