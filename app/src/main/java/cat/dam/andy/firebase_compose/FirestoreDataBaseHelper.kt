package cat.dam.andy.firebase_compose

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
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
                        //ordena alfabèticament la llista d'usuaris (cal crear un objecte per actualitzar)
                        val sortedItems = items.sortedWith(CustomComparator())
                        // Utilitza el ViewModel per actualitzar les dades inicials amb una nova llista ordenada
                        viewModel.updateUserList(sortedItems.toList())
                    } else {
                        Toast.makeText(context, "Error: $items", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }


    fun addContact(item: Item, onResult: (Boolean, Item?) -> Unit) {
        val itemName = item.name
        val itemLastname = item.lastname

        val user = Item(name = itemName, lastname = itemLastname)

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
                onResult(true, newUser)
            }
            .addOnFailureListener { e: Exception ->
                message = context.getString(R.string.error_user_add) + " " + e.message
                Log.w(TAG, message)
                showToast(message)

                // Informa de l'error i retorna l'objecte Item original sense l'`id`
                onResult(false, user)
            }
    }

    fun updateContact(item: Item, editedItem: Item, onResult: (Boolean, Item?) -> Unit) {
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
                            // Obtinguem la referència del document existent
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
                                    onResult(true, updatedUser)
                                }
                                .addOnFailureListener { e: Exception ->
                                    message =
                                        context.getString(R.string.error_user_update) + " " + e.message
                                    Log.w(TAG, message)
                                    showToast(message)
                                    onResult(false, updatedUser)
                                }
                        } else {
                            message =
                                context.getString(R.string.error_user_not_found) + " " + name + " " + lastname
                            Log.w(TAG, message)
                            showToast(message)
                            onResult(false, item)
                        }
                    } else {
                        message =
                            context.getString(R.string.error_unexpected) + " " + task.exception
                        Log.w(TAG, message)
                        showToast(message)
                        onResult(false, item)
                    }
                }
        } catch (e: FirebaseException) {
            // Aquí captures i gestionaràs l'error de connexió amb Firebase
            e.printStackTrace()
            message =
                context.getString(R.string.error_connectivity) + " " + e.printStackTrace()
            Log.w(TAG, message)
            showToast(message)
            onResult(false, item)
        }
    }


    fun removeContact(item: Item, onResult: (Boolean, Item?) -> Unit) {
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
                                onResult(true, item)
                            }
                            .addOnFailureListener { e: Exception ->
                                message =
                                    context.getString(R.string.error_user_remove) + " " + e.message
                                Log.w(TAG, message)
                                showToast(message)
                                onResult(false, item)
                            }
                    } else {
                        // Usuari no trobat
                        message =
                            context.getString(R.string.error_user_not_found) + " " + name + " " + lastname
                        Log.w(TAG, message)
                        showToast(message)
                        onResult(false, item)
                    }
                } else {
                    // Error en la consulta
                    message = context.getString(R.string.error_user) + "(" + task.exception + ")"
                    Log.w(TAG, message)
                    showToast(message)
                    onResult(false, null)
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
        if (filter.isNotBlank()) {
            println(filter)
            // Filtra els resultats en funció de filter
            db.collection(COLLECTION_KEY)
                .orderBy(COLUMN_NAME)
                .startAt(filter)
                .endAt(filter + '\uf8ff')// darrer caràcter Unicode
                .limit(10)
                //.whereArrayContains(COLUMN_NAME, filter)
                //.whereEqualTo(COLUMN_NAME, filter)
                .get()
                .addOnSuccessListener { result ->
                    val items = mutableListOf<Item>()
                    for (document in result) {
                        // Converteix cada document a un objecte Item i afegeix a la llista
                        val item = document.toObject(Item::class.java)
                        items.add(item)
                    }
                    println(items)
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
