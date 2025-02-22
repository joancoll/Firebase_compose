package cat.dam.andy.firebase_compose.model

class ItemComparator : Comparator<Item> {
    override fun compare(o1: Item, o2: Item): Int {
        // Ordena els usuaris pel nom ignorant les majúscules
        val nameComparison = o1.name.compareTo(o2.name, ignoreCase = true)
        // Si els noms són iguals, comparem pels lastnames
        return if (nameComparison == 0) {
            o1.lastname.compareTo(o2.lastname, ignoreCase = true)
        } else {
            nameComparison
        }
    }
}
