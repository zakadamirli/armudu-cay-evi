package az.armudu.cayevi.jam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JamRepository extends JpaRepository<Jam, Long> {

    List<Jam> findAllByOrderBySortOrderAsc();
}
