package app.ccb.repositories;

import app.ccb.domain.entities.Client;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {

    Optional<Client> findByFullName(String name);

    @Query("" +
            "select c " +
            "from clients as c " +
            "join c.bankAccount as b " +
            "join b.cards " +
            "group by c.id " +
            "order by size(b.cards) desc ")
    List<Client> findClientsOrderByCardCount(Pageable pageable);
}
