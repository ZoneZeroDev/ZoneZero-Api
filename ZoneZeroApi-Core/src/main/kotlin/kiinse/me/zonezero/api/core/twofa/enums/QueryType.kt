package kiinse.me.zonezero.api.core.twofa.enums

enum class QueryType(val value: String) {
    ENABLE_TFA("Enable 2Fa"),
    DISABLE_TFA("Disable 2Fa"),
    CHANGE_PASSWORD("Change password"),
    AUTH("Auth"),
    ACCOUNT_REMOVE("Account remove")
}