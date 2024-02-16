package cat.dam.andy.firebase_compose

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Item(
    @DocumentId val id: String = "", // no s'ha de posar a la BD, es genera automàticament
    @ServerTimestamp val createdAt: Date = Date(), // data de creació es genera automàticament
    val name: String = "",
    val lastname: String = "",
)