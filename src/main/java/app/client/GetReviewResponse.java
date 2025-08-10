package app.client;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetReviewResponse {
    private UUID reviewId;
    private String username;
    private String storeName;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}
