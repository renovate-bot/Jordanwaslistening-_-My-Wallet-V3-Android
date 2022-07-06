package com.blockchain.nabu.api.getuser.domain

import com.blockchain.nabu.models.responses.nabu.NabuUser
import io.reactivex.rxjava3.core.Single

interface GetUserStoreService {
    fun getUser(): Single<NabuUser>
}
