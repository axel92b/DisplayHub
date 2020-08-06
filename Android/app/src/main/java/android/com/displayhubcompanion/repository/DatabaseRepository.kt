package android.com.displayhubcompanion.repository

import android.com.displayhubcompanion.models.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.functions.FirebaseFunctions
import java.lang.Exception

class DatabaseRepository {
    //Global static database abstraction class
    companion object DB {
        private val auth: FirebaseAuth = FirebaseAuth.getInstance()
        private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
        private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()

        //TODO methods to get/put data from/to DB

        //Example method
        fun getLoggedInUser() : Task<DocumentSnapshot> {
            if (auth.currentUser == null) {
                return taskOnLoggedInUserFailure()
            }
            return firestore.collection("users").document(auth.currentUser!!.uid).get()
        }

        fun getFirebaseAuth(): FirebaseAuth{
            return auth
        }

        fun createNewUserEntryInDB(user: User) : Task<Void> {
            return firestore.collection("users").document(auth.currentUser?.uid!!).set(user)
        }

        fun getMyBoardsQuery(): Query {
            if (auth.currentUser == null) {
                //TODO consider empty query
                return firestore.collection("users")
            }
            return firestore
                .collection("users")
                .document(auth.currentUser?.uid!!)
                .collection("boards")
                .orderBy("name", Query.Direction.ASCENDING)
        }

        fun addNewBoardToLoggedInUser(board: BoardFirebaseModel) : Task<Void> {
            if (auth.currentUser == null) {
                return taskOnLoggedInUserFailure()
            }
            return firestore.collection("users").document(auth.currentUser!!.uid).collection("boards").document(board.mac_address!!).set(board)
        }

        fun getBoardInfo(mac_address: String) : Task<DocumentSnapshot> {
            if (auth.currentUser == null) {
                return taskOnLoggedInUserFailure()
            }
            return firestore.collection("users").document(auth.currentUser!!.uid).collection("boards").document(mac_address).get()
        }

        private fun <T> taskOnLoggedInUserFailure() : Task<T> {
            return Tasks.forException(Exception("User is not logged in"))
        }

        fun removeBoard(macAddress: String) {
            FirebaseFirestore.getInstance().collection("users").document(
              auth.currentUser?.uid!!)
                .collection("boards").document(macAddress).delete()
        }
        fun getListOfModules(): Task<ArrayList<String>> {
            return functions.getHttpsCallable("getListOfModules").call().continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as ArrayList<String>
                result
            }
        }

        fun addModuleToBoard(boardMac: String, module: Module) {
            FirebaseFirestore.getInstance().collection("users").document(
                auth.currentUser?.uid!!)
                .collection("boards").document(boardMac).collection("modules").add(module)
        }

        fun setModuleToBoard(boardMac: String, doc: Map<String,Any>, id: String) : Task<Void> {
            return FirebaseFirestore.getInstance().collection("users").document(
                auth.currentUser?.uid!!)
                .collection("boards").document(boardMac).collection("modules").document(id).set(doc)
        }

        fun getListOfBoardModulesSorted(board_mac: String):  Query {
            return firestore
                .collection("users")
                .document(auth.currentUser?.uid!!)
                .collection("boards")
                .document(board_mac)
                .collection("modules")
                .orderBy("index", Query.Direction.ASCENDING)
        }

        fun getSizeOfListOfBoardModules(board_mac: String): Task<Int> {
            return firestore
                .collection("users")
                .document(auth.currentUser?.uid!!)
                .collection("boards")
                .document(board_mac)
                .collection("modules").get().continueWith {
                    if (it.isSuccessful) {
                        val documents = it.result?.documents;
                        return@continueWith documents?.size ?: 0
                    }
                    return@continueWith 0
                }
        }

        fun getClockSettings(source: Source, board_mac: String, doc_id: String) : Task<ClockSettingsModel> {
            return firestore
                .collection("users")
                .document(auth.currentUser?.uid!!)
                .collection("boards")
                .document(board_mac)
                .collection("modules")
                .document(doc_id).get(source).continueWith {
                    if (it.isSuccessful) {
                        return@continueWith it.result?.toObject(ClockSettingsModel::class.java);
                    }
                    return@continueWith null
                }
        }

        fun setClockSettings(board_mac: String, doc_id: String, settings: ClockSettingsModel): Task<Void> {
            return firestore
                .collection("users")
                .document(auth.currentUser?.uid!!)
                .collection("boards")
                .document(board_mac)
                .collection("modules")
                .document(doc_id).set(settings)
        }

        fun getNewsSettings(source: Source, board_mac: String, doc_id: String) : Task<NewsSettingsModel> {
            return firestore
                .collection("users")
                .document(auth.currentUser?.uid!!)
                .collection("boards")
                .document(board_mac)
                .collection("modules")
                .document(doc_id).get(source).continueWith {
                    if (it.isSuccessful) {
                        return@continueWith it.result?.toObject(NewsSettingsModel::class.java);
                    }
                    return@continueWith null
                }
        }

        fun setNewsSettings(board_mac: String, doc_id: String, settings: NewsSettingsModel): Task<Void> {
            return firestore
                .collection("users")
                .document(auth.currentUser?.uid!!)
                .collection("boards")
                .document(board_mac)
                .collection("modules")
                .document(doc_id).set(settings)
        }

        fun getStockSettings(source: Source, board_mac: String, doc_id: String) : Task<StockSettingsModel> {
            return firestore
                .collection("users")
                .document(auth.currentUser?.uid!!)
                .collection("boards")
                .document(board_mac)
                .collection("modules")
                .document(doc_id).get(source).continueWith {
                    if (it.isSuccessful) {
                        return@continueWith it.result?.toObject(StockSettingsModel::class.java);
                    }
                    return@continueWith null
                }
        }

        fun setStockSettings(board_mac: String, doc_id: String, settings: StockSettingsModel): Task<Void> {
            return firestore
                .collection("users")
                .document(auth.currentUser?.uid!!)
                .collection("boards")
                .document(board_mac)
                .collection("modules")
                .document(doc_id).set(settings)
        }

        fun getWeatherSettings(source: Source,board_mac: String, doc_id: String) : Task<WeatherSettingsModel> {
            return firestore
                .collection("users")
                .document(auth.currentUser?.uid!!)
                .collection("boards")
                .document(board_mac)
                .collection("modules")
                .document(doc_id).get(source).continueWith {
                    if (it.isSuccessful) {
                        return@continueWith it.result?.toObject(WeatherSettingsModel::class.java);
                    }
                    return@continueWith null
                }
        }

        fun setWeatherSettings(board_mac: String, doc_id: String, settings: WeatherSettingsModel): Task<Void> {
            return firestore
                .collection("users")
                .document(auth.currentUser?.uid!!)
                .collection("boards")
                .document(board_mac)
                .collection("modules")
                .document(doc_id).set(settings)
        }

        fun removeModule(boardMacAddress: String?, index: Long?): Task<Void> {
            return FirebaseFirestore.getInstance().collection("users").document(
                auth.currentUser?.uid!!)
                .collection("boards").document(boardMacAddress!!).collection("modules").whereEqualTo("index",index).get().addOnSuccessListener { query ->
                    query.documents.first().reference.delete()
                }.continueWith{ _ -> return@continueWith null }
        }

        fun getBoardModules(boardMacAddress: String): Task<QuerySnapshot> {
            return firestore
                .collection("users")
                .document(auth.currentUser?.uid!!)
                .collection("boards")
                .document(boardMacAddress)
                .collection("modules").get()
        }
    }
}