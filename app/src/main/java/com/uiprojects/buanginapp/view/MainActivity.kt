package com.uiprojects.buanginapp.view

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.uiprojects.buanginapp.R
import com.uiprojects.buanginapp.databinding.ActivityMainBinding
import com.uiprojects.buanginapp.model.Waste
import com.uiprojects.buanginapp.utils.SharedPrefs
import dagger.hilt.android.AndroidEntryPoint
import org.w3c.dom.Text
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var userWasteHistoryList: ArrayList<Waste>
    private lateinit var db: FirebaseFirestore
    private lateinit var adapterHistory: WasteListAdapter
    private lateinit var progressBarHistory: ProgressBar
    private lateinit var userUid: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var containerAllHistory :LinearLayout
    private lateinit var totalEarningTextView: TextView
    private lateinit var totalWeightTextView: TextView
    private lateinit var thisMonthEarning: TextView
    private lateinit var logoutButton: TextView
    @Inject
    lateinit var sharedPrefs: SharedPrefs
    private lateinit var binding: ActivityMainBinding
//    private lateinit var userData : User

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val nameTextView: TextView = binding.userNameTextView
        totalEarningTextView = binding.userEarningsText
        totalWeightTextView = binding.userTotalDisposal
        thisMonthEarning = binding.userMonthlyEarningText
        val recyclerView = binding.dashboardHistoryRecyclerView
        bottomNavigation = binding.bottomNavigationView
        progressBarHistory = binding.progressHomeList
        containerAllHistory = binding.seeMoreHistoryContainer
        logoutButton = binding.logoutButton

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        if (auth.currentUser != null) {
            userUid = auth.currentUser!!.uid
        }

        userWasteHistoryList = arrayListOf()
        adapterHistory = WasteListAdapter(userWasteHistoryList)
        recyclerView.adapter = adapterHistory
        eventChangeListener()
        containerAllHistory.setOnClickListener {
            startActivity(Intent(this@MainActivity, WasteHistoryActivity::class.java))
        }


        progressBarHistory.visibility = View.INVISIBLE
        val name = auth.currentUser!!.displayName
        nameTextView.text = name.toString()
        supportActionBar?.hide()
        bottomNavigation.selectedItemId= R.id.home_option
        logoutButton.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setMessage("Keluarkan akun dari Aplikasi?")
                setPositiveButton("Ya"){dialog,which->
                    dialog.dismiss()
                    signOut()
                }
                setNegativeButton("Tidak"){ dialog,which ->
                    dialog.dismiss()
                }
            }.create().show()

        }
        bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.waste_history_option ->{
                    if (userWasteHistoryList.size < 1){
                        AlertDialog.Builder(this).apply {
                            setMessage("Belum ada Riwayat Pembuangan! Tambahkan Data?")
                            setPositiveButton("Ya"){dialog,which->
                                startActivity(Intent(this@MainActivity, AddWasteActivity::class.java))
                            }
                            setNegativeButton("Tidak"){ dialog,which ->
                                dialog.dismiss()
                                bottomNavigation.selectedItemId = R.id.home_option
                            }
                        }.create().show()
                    }else {
                        startActivity(Intent(this@MainActivity, WasteHistoryActivity::class.java))
                    }
                    true
                }
                R.id.add_waste_option->{
                    startActivity(Intent(this@MainActivity, AddWasteActivity::class.java))
                    true
                }
                R.id.home_option->{
                    Log.d("navigation","already at home")
                    true
                }
                else -> {
                    false}
            }
        }

    }

    private fun checkIsLoggedIn() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun eventChangeListener() {
        db.collection("wastes")
            .whereEqualTo("userId", userUid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(3)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    progressBarHistory.visibility = View.VISIBLE
                    if (error != null) {
                        Log.d("Error on Firebase Get History:", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val wasteObj = dc.document.toObject(Waste::class.java)
                            userWasteHistoryList.add(wasteObj)
                        }
                        if (dc.type == DocumentChange.Type.REMOVED) {
                            val wasteObj = dc.document.toObject(Waste::class.java)
                            userWasteHistoryList.remove(wasteObj)
                        }

                    }
                    getSummaryData()
                    adapterHistory.notifyDataSetChanged()
                    progressBarHistory.visibility = View.INVISIBLE
                }
            })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getSummaryData(){
        val locale = Locale("in", "ID")
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)
        val revenueQuery  = db.collection("wastes").whereEqualTo("userId",userUid).aggregate(
            AggregateField.sum("revenue"),
            AggregateField.sum("weight"),
        )
        revenueQuery.get(AggregateSource.SERVER).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                Log.d(TAG, "Sum: ${snapshot.get(AggregateField.sum("revenue"))}")
                totalEarningTextView.text = currencyFormat.format(snapshot.get(AggregateField.sum("revenue")))
                val totalWeight = "${snapshot.get(AggregateField.sum("weight")).toString()} Kg"
                totalWeightTextView.text = totalWeight
            } else {
                Log.d(TAG, "Aggregate failed: ", task.exception)
            }
        }
        val todayDate = LocalDate.now().withDayOfMonth(1)
        val todayDateAsDate = Date.from(todayDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val todayDateAsTimestamp = Timestamp(todayDateAsDate)

        val monthlyQuery = db.collection("wastes").whereEqualTo("userId",userUid)
            .whereGreaterThan("createdAt",todayDateAsTimestamp).aggregate(
                AggregateField.sum("revenue"),
                AggregateField.sum("weight"),
            )
        monthlyQuery.get(AggregateSource.SERVER).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                Log.d(TAG, "Sum: ${snapshot.get(AggregateField.sum("revenue"))}")
                thisMonthEarning.text = currencyFormat.format(snapshot.get(AggregateField.sum("revenue")))
            } else {
                Log.d(TAG, "Aggregate failed: ", task.exception)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkIsLoggedIn()
    }

    override fun onResume() {
        super.onResume()
        bottomNavigation.selectedItemId = R.id.home_option
    }

    private fun signOut() {
        Firebase.auth.signOut()
        sharedPrefs.clear()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }

    fun convertStringToTimestamp(data: String): Timestamp? {
        val regex = """Timestamp\(seconds=(\d+), nanoseconds=(\d+)\)""".toRegex()
        val matchResult = regex.find(data)
        return if (matchResult != null) {
            val (seconds, nanoseconds) = matchResult.destructured
            Timestamp(seconds.toLong(), nanoseconds.toInt())
        } else {
            null
        }
    }
}

