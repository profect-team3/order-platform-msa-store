package app.domain.store.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import app.commonUtil.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

	@Id
	@GeneratedValue
	private UUID reviewId;

	@Column(name = "b_order_id")
	private UUID orders;

	@Column(name="user_id")
	private Long userId;

	@Column(name="store_id")
	private UUID store;

	@Column(nullable = false)
	private Long rating;

	@Column(nullable = false)
	private String content;

	public void delete(){
		this.deletedAt = LocalDateTime.now();
	}

}