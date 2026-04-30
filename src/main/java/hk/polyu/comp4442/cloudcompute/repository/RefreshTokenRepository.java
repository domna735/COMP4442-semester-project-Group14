package hk.polyu.comp4442.cloudcompute.repository;

import hk.polyu.comp4442.cloudcompute.entity.AppUser;
import hk.polyu.comp4442.cloudcompute.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;           

import java.util.Optional;                                   

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    Optional<RefreshToken> findByToken(String token);
    
    @Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByUser(AppUser user);
}