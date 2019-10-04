package app.coinninja.cn.thunderdome.model

import app.dropbit.annotations.Mockable


@Mockable
data class PayErrorResponse(val error: String, val code: String, val message: String)