package kiinse.me.zonezero.api.core.config

enum class Addresses(val value: String) {
    EMAIL("http://zonezero-api-emails:7227/email/"),
    PLAYER("http://zonezero-api-players:7224/players/"), // TODO: На релизе заменить на player
    SERVER("http://zonezero-api-servers:7225/server/"),
    WEB("http://zonezero-api-web:7226/web/"),
    TEST("http://zonezero-api-test:7228/test/")
}