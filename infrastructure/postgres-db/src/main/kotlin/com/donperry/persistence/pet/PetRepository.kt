package com.donperry.persistence.pet

import com.donperry.persistence.pet.entities.PetData
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface PetRepository : ReactiveCrudRepository<PetData, UUID> {
    @Query("SELECT COUNT(*) FROM petapp.pets WHERE owner = :owner AND deleted_at IS NULL")
    fun countByOwner(owner: String): Mono<Long>

    @Query("SELECT * FROM petapp.pets WHERE id = :id AND deleted_at IS NULL")
    override fun findById(id: UUID): Mono<PetData>

    @Query("SELECT * FROM petapp.pets WHERE owner = :owner AND deleted_at IS NULL")
    fun findAllByOwnerAndDeletedAtIsNull(owner: String): Flux<PetData>

    @Modifying
    @Query("UPDATE petapp.pets SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id")
    fun softDeleteById(id: UUID): Mono<Void>
}