package com.donperry.persistence.pet.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Table(name = "pets")
data class PetData(
    @Id
    val id: UUID? = null,
    @Column("name")
    val name: String,
    @Column("species")
    val species: String,
    @Column("breed")
    val breed: String? = null,
    @Column("age")
    val age: Int,
    @Column("birthdate")
    val birthdate: LocalDate? = null,
    @Column("weight")
    val weight: BigDecimal? = null,
    @Column("nickname")
    val nickname: String? = null,
    @Column("owner")
    val owner: String,
    @Column("registration_date")
    val registrationDate: LocalDate,
    @Column("photo_url")
    val photoUrl: String? = null,
    @Column("deleted_at")
    val deletedAt: LocalDateTime? = null
)
