package com.donperry.client.rest.user.mapper

import com.donperry.client.rest.user.dto.UserData
import com.donperry.model.user.User

class UserMapper {
    companion object {
        fun toUserData(user: User): UserData =
            UserData(
                id = user.id,
                email = user.email,
            )

        fun toUser(userData: UserData): User =
            User(
                id = userData.id,
                email = userData.email,
            )
    }
}
