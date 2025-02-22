package cat.dam.andy.firebase_compose.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import cat.dam.andy.firebase_compose.model.ItemComparator
import cat.dam.andy.firebase_compose.R
import cat.dam.andy.firebase_compose.viewmodel.UserListViewModel
import cat.dam.andy.firebase_compose.model.Item
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot


class FirestoreDataBaseHelper(appContext: Context, private val viewModel: UserListViewModel) {
    private var db: FirebaseFirestore
    private var context = appContext
    private var message: String = ""

    companion object {
        private const val COLLECTION_KEY = "USERS"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_LASTNAME = "lastname"
        private const val TAG = "FirebaseFireStore"
    }

    init {
        // Inicialitzem la base de dades
        db = FirebaseFirestore.getInstance()
        startDbChangeListener()
    }

    fun startDbChangeListener() {
        // Inicia un listener per detectar canvis a la base de dades encara que no tingui connexió
        db.collection(COLLECTION_KEY)
            .addSnapshotListener(EventListener<QuerySnapshot> { documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
                Log.d(TAG, "Canvis detectats a la base de dades.")
                assert(documentSnapshots != null)
                getAllContacts { success, items ->
                    if (success) {
                        // Ordena alfabèticament la llista d'usuaris
                        val sortedItems = items.sortedWith(ItemComparator())
                        // Utilitza el ViewModel per actualitzar les dades inicials amb una nova llista ordenada
                        viewModel.updateUserList(sortedItems)
                    } else {
                        Toast.makeText(context, "Error: $items", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    fun addContact(item: Item, onResult: (Boolean, String) -> Unit) {
        val name = item.name
        val lastname = item.lastname

        val user = Item(name = name, lastname = lastname)

        db.collection(COLLECTION_KEY)
            .add(user)
            .addOnSuccessListener { documentReference: DocumentReference? ->
                message =
                    context.getString(R.string.new_user_added) + " " + user.name + " " + user.lastname
                Log.w(TAG, message)
                showToast(message)

                // Assigna l'`id` generat per Firestore al camp `id` de l'objecte Item
                val newUser = user.copy(id = documentReference?.id.orEmpty())

                // Informa de l'èxit i proporciona l'usuari amb l'`id` actualitzat
                onResult(true, message)
            }
            .addOnFailureListener { e: Exception ->
                message = context.getString(R.string.error_user_add) + " " + e.message
                Log.w(TAG, message)
                showToast(message)

                // Informa de l'error i retorna l'objecte Item original sense l'`id`
                onResult(false, message)
            }
    }

    fun updateContact(item: Item, editedItem: Item, onResult: (Boolean, String) -> Unit) {
        try {
            val name = item.name
            val lastname = item.lastname
            db.collection(COLLECTION_KEY)
                .whereEqualTo(COLUMN_NAME, name)
                .whereEqualTo(COLUMN_LASTNAME, lastname)
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if (task.isSuccessful) {
                        val querySnapShot = task.result
                        if (querySnapShot.size() > 0) {
                            // Obtenim l'ID del document existent per actualitzar-lo
                            val existingDocument = querySnapShot.documents[0]
                            val documentId = existingDocument.id

                            // Utilitza els valors de editedItem per actualitzar les dades
                            val updatedUser = editedItem

                            // Actualitza les dades del document existent
                            db.collection(COLLECTION_KEY)
                                .document(documentId)
                                .set(updatedUser)
                                .addOnSuccessListener {
                                    message =
                                        context.getString(R.string.user_updated) + " " + updatedUser.name + " " + updatedUser.lastname
                                    Log.w(TAG, message)
                                    showToast(message)
                                    onResult(true, message)
                                }
                                .addOnFailureListener { e: Exception ->
                                    message =
                                        context.getString(R.string.error_user_update) + " " + e.message
                                    Log.w(TAG, message)
                                    showToast(message)
                                    onResult(false, message)
                                }
                        } else {
                            message =
                                context.getString(R.string.error_user_not_found) + " " + name + " " + lastname
                            Log.w(TAG, message)
                            showToast(message)
                            onResult(false, message)
                        }
                    } else {
                        message =
                            context.getString(R.string.error_unexpected) + " " + task.exception
                        Log.w(TAG, message)
                        showToast(message)
                        onResult(false, message)
                    }
                }
        } catch (e: FirebaseException) {
            e.printStackTrace()
            message =
                context.getString(R.string.error_connectivity) + " " + e.printStackTrace()
            Log.w(TAG, message)
            showToast(message)
            onResult(false, message)
        }
    }

    fun removeContact(item: Item, onResult: (Boolean, String) -> Unit) {
        val name = item.name
        val lastname = item.lastname
        db.collection(COLLECTION_KEY)
            .whereEqualTo(COLUMN_NAME, name)
            .whereEqualTo(COLUMN_LASTNAME, lastname)
            .get().addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful) {
                    val querySnapShot = task.result
                    if (querySnapShot.size() > 0) {
                        // Trobat l'usuari, procedim a eliminar-lo
                        val documentId = querySnapShot.documents[0].id
                        db.collection(COLLECTION_KEY)
                            .document(documentId)
                            .delete()
                            .addOnSuccessListener {
                                message =
                                    context.getString(R.string.user_removed) + " " + item.name + " " + item.lastname
                                Log.w(TAG, message)
                                showToast(message)
                                onResult(true, message)
                            }
                            .addOnFailureListener { e: Exception ->
                                message =
                                    context.getString(R.string.error_user_remove) + " " + e.message
                                Log.w(TAG, message)
                                showToast(message)
                                onResult(false, message)
                            }
                    } else {
                        // Usuari no trobat
                        message =
                            context.getString(R.string.error_user_not_found) + " " + name + " " + lastname
                        Log.w(TAG, message)
                        showToast(message)
                        onResult(false, message)
                    }
                } else {
                    // Error en la consulta
                    message = context.getString(R.string.error_user) + "(" + task.exception + ")"
                    Log.w(TAG, message)
                    showToast(message)
                    onResult(false, message)
                }
            }
    }

    fun getAllContacts(onComplete: (success: Boolean, items: List<Item>) -> Unit) {
        db.collection(COLLECTION_KEY)
            .get()
            .addOnSuccessListener { result ->
                val items = mutableListOf<Item>()
                for (document in result) {
                    // Converteix cada document a un objecte Item i afegeix a la llista
                    val item = document.toObject(Item::class.java)
                    items.add(item)
                }
                // Actualitza el MutableStateFlow del ViewModel amb les dades més recents
                viewModel.updateUserList(items)
                onComplete(true, items)
            }
            .addOnFailureListener { exception ->
                // Maneixa errors en la consulta
                onComplete(false, emptyList())
                Toast.makeText(context, "Error getting documents: $exception", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun getContacts(filter: String, onComplete: (Boolean, List<Item>) -> Unit) {
        // Filtra els resultats en funció de filter
        // Firestore no suporta consultes de subcadenes de manera nativa (no pot filtrar per subcadena)
        // tampoc suporta consultes de text que no siguin exactes ni diferents de majúscules/minúscules
        if (filter.isNotBlank()) {
            val filterLowercase = filter.lowercase()

            // Consulta 1: Cerca a fullNameLowercase
            val query1 = db.collection(COLLECTION_KEY)
                .orderBy("fullNameLowercase")
                .startAt(filterLowercase)
                .endAt("$filterLowercase\uf8ff")
                .limit(10)

            // Consulta 2: Cerca a fullNameReversedLowercase
            val query2 = db.collection(COLLECTION_KEY)
                .orderBy("fullNameReversedLowercase")
                .startAt(filterLowercase)
                .endAt("$filterLowercase\uf8ff")
                .limit(10)

            // Executa les dues consultes
            val items = mutableListOf<Item>()
            val seenIds = mutableSetOf<String>() // Per evitar duplicats

            // Funció per processar els resultats
            fun processResult(result: QuerySnapshot) {
                for (document in result) {
                    if (!seenIds.contains(document.id)) {
                        val item = document.toObject(Item::class.java)
                        items.add(item)
                        seenIds.add(document.id)
                    }
                }
            }

            // Executa les dues consultes en paral·lel
            Tasks.whenAllSuccess<QuerySnapshot>(query1.get(), query2.get())
                .addOnSuccessListener { results ->
                    results.forEach { processResult(it) }
                    onComplete(true, items)
                }
                .addOnFailureListener { exception ->
                    onComplete(false, emptyList())
                    Log.w(TAG, "Error getting documents: $exception")
                }
        } else {
            // Si el filtre està buit, obté tots els resultats sense filtrar
            getAllContacts(onComplete)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}