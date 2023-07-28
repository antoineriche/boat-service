package com.ariche.boatapi.repository;

import com.ariche.boatapi.entity.UserEntity;
import com.ariche.boatapi.repository.customset.IUserAuthoritySet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    @Query(
        nativeQuery = true,
        value = """
                SELECT login AS login, password_hash AS password, authority_name AS authorityName
                FROM _user INNER JOIN _user_authority UA ON UA.USER_ID = _user.ID
                WHERE login = :login
                """
    )
    List<IUserAuthoritySet> findUserWithAuthorities(@Param(value = "login") final String login);

}
