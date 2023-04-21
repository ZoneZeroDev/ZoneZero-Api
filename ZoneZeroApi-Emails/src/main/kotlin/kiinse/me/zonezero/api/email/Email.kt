package kiinse.me.zonezero.api.email

data class Email(val address: String, val subject: String, val template: EmailTemplate?)
