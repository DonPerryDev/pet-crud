package com.donperry.persistence.pet

import com.donperry.persistence.pet.entities.PetData
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PetRepository : ReactiveCrudRepository<PetData, UUID> {
}