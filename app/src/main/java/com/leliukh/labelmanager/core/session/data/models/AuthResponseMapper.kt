package com.leliukh.labelmanager.core.session.data.models

import com.leliukh.labelmanager.contract.dto.auth.AuthResponseDto
import com.leliukh.labelmanager.core.session.domain.models.AuthSession

fun AuthResponseDto.toSession(): AuthSession = AuthSession(
    userId = user.id,
    email = user.email,
    labelId = user.labelId,
    accessToken = accessToken,
    accessTokenExpiresAt = accessTokenExpiresAt,
    refreshToken = refreshToken,
)
