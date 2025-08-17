package app.domain.menu.model.repository;

import app.domain.menu.model.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {

    List<Stock> findByMenuMenuIdIn(List<UUID> menuIds);

    Optional<Stock> findByMenu_MenuId(UUID menuId);
}
