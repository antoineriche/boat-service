package com.ariche.boatapi.repository;

import com.ariche.boatapi.entity.BoatEntity;
import com.ariche.boatapi.repository.customset.IBoatImgSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoatRepository extends JpaRepository<BoatEntity, Long> {

    @Query(
        nativeQuery = true,
        value = "SELECT IMG_NAME FROM BOATS B WHERE B.ID = :id"
    )
    Optional<String> fetchImgNameById(@Param("id") final Long boatId);

    @Modifying
    @Query(
        nativeQuery = true,
        value = "UPDATE BOATS SET IMG_NAME = :imgName WHERE ID = :boatId"
    )
    void updateImgNameByBoatId(@Param("boatId") final Long boatId,
                               @Param("imgName") final String imgName);

    @Query(
        nativeQuery = true,
        value = """
                SELECT IMG_NAME as imgName,
                ID as id
                FROM BOATS WHERE ID = :boatId
                """
    )
    Optional<IBoatImgSet> findBoatImgById(@Param("boatId") final Long boatId);
}
