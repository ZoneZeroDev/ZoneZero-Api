package kiinse.me.zonezero.api.security.annotations

import io.micronaut.aop.Around
import io.micronaut.core.annotation.Internal
import kiinse.me.zonezero.api.security.enums.AccountType

@Suppress("UNUSED")
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Around
@Internal
annotation class Authentication(val permissions: Array<AccountType> = [])