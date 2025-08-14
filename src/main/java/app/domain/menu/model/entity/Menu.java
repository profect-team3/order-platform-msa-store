package app.domain.menu.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;

import app.domain.store.model.entity.Store;
import app.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "p_menu")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Menu extends BaseEntity {

	@Id
	@GeneratedValue
	private UUID menuId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false)
	private Long price;

	@Column
	private String description;

	@Builder.Default
	@Column(name = "is_hidden", nullable = false)
	private boolean hidden = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = true)
	private Category category;

	public void update(String name, Long price, String description, Boolean isHidden) {
		if (name != null) {
			this.name = name;
		}
		if (price != null) {
			this.price = price;
		}
		if (description != null) {
			this.description = description;
		}
		if (isHidden != null) {
			this.hidden = isHidden;
		}
	}

	private LocalDateTime deletedAt;

	public void markAsDeleted() {
		this.setDeletedAt(java.time.LocalDateTime.now());
	}

}

