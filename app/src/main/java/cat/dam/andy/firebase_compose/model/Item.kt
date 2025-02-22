package cat.dam.andy.firebase_compose.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Item(
    @DocumentId val id: String = "", // no s'ha de posar a la BD, es genera automàticament
    @ServerTimestamp val createdAt: Date = Date(), // data de creació es genera automàticament
    val name: String = "",
    val lastname: String = "",
    val fullNameLowercase: String = "${name.lowercase()} ${lastname.lowercase()}",
    val fullNameReversedLowercase: String = "${lastname.lowercase()} ${name.lowercase()}"
)