package com.uiprojects.buanginapp.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uiprojects.buanginapp.R
import com.uiprojects.buanginapp.databinding.ActivityAddWasteBinding
import com.uiprojects.buanginapp.model.Waste
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date

class AddWasteActivity : AppCompatActivity() {
    private var wasteWeight: Double = 0.0
    private lateinit var wasteType: String
    private lateinit var wasteDesc: String
    private var wastePrice: Double = 0.0
    private lateinit var binding: ActivityAddWasteBinding
    private var isTrade: Boolean = false
    private lateinit var wastePriceEditText: EditText
    private lateinit var wasteDescEditText: EditText
    private lateinit var wasteTypeEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var db: FirebaseFirestore

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddWasteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        weightEditText = binding.addWeightEditText
        val buttonChoiceMoney = binding.choiceMoneyButton
        val buttonChoiceBarang = binding.choiceBarangButton
        val containerWastePrice = binding.containerWastePrice
        val containerWasteTrade = binding.containerWasteTrade
        val buttonSubmitWaste = binding.confirmAddWasteButton
        wastePriceEditText = binding.addWastePriceEditText
        wasteDescEditText = binding.addWasteTradeDescEditText
        wasteTypeEditText = binding.addWasteTypeEditText
        initializeData()
        db = FirebaseFirestore.getInstance()
        val todayDateAsDate = Date.from(LocalDateTime.now().toInstant(
            ZoneOffset.UTC))
        val todayDateAsTimestamp = Timestamp(todayDateAsDate)

        buttonChoiceBarang.setOnClickListener {
            isTrade = true
            containerWasteTrade.visibility = View.VISIBLE
            containerWastePrice.visibility = View.GONE
            buttonChoiceBarang.setBackgroundResource(R.drawable.filled_bg_border)
            buttonChoiceBarang.setTextColor(ContextCompat.getColor(this, R.color.white))
            buttonChoiceMoney.setBackgroundResource(R.drawable.transparent_bg_bordered_button)
            buttonChoiceMoney.setTextColor(ContextCompat.getColor(this, R.color.primary_green))
            wastePriceEditText.text.clear()
        }

        buttonChoiceMoney.setOnClickListener {
            isTrade = false
            containerWasteTrade.visibility = View.GONE
            containerWastePrice.visibility = View.VISIBLE
            buttonChoiceMoney.setBackgroundResource(R.drawable.filled_bg_border)
            buttonChoiceMoney.setTextColor(ContextCompat.getColor(this, R.color.white))
            buttonChoiceBarang.setBackgroundResource(R.drawable.transparent_bg_bordered_button)
            buttonChoiceBarang.setTextColor(ContextCompat.getColor(this, R.color.primary_green))
            wasteDescEditText.text.clear()
        }

        val dialogLoading = Dialog(this)
        dialogLoading.setContentView(R.layout.layout_loading_dialog)

        buttonSubmitWaste.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setMessage("Tambahkan Data Sampah?")
                setPositiveButton("Ya") { dialog, which ->
                    val waste = Waste()
                    waste.name = wasteType
                    waste.weight = wasteWeight

                    if (wastePrice != 0.0) {
                        waste.revenue = wastePrice
                    }
                    if (wasteDesc != "") {
                        waste.tradeDesc = wasteDesc
                    }
                    waste.createdAt = todayDateAsTimestamp
                    dialogLoading.show()
                    Log.d("waste payload", waste.toString())
                    db.collection("wastes").add(waste).addOnSuccessListener { ref ->
                        Log.d("Added: ", ref.id)
                        dialogLoading.dismiss()
                        startActivity(Intent(this@AddWasteActivity, MainActivity::class.java))
                    }.addOnFailureListener { e ->
                        Log.d(
                            "Error AddWasteActivity: ",
                            "Failed to Add Data: " + e.message.toString()
                        )
                        dialogLoading.dismiss()
                        dialog.dismiss()
                    }
                }
                setNegativeButton("Tidak") { dialog, which ->
                    dialog.dismiss()
                }
            }.create().show()
        }

    }

    override fun onStart() {
        super.onStart()
        checkIsLoggedIn()
        listenTextChange()
    }

    private fun initializeData() {
        wasteType = wasteTypeEditText.text.toString()
        wasteDesc = wasteDescEditText.text.toString()
        if (weightEditText.text.toString() != "") {
            wasteWeight = weightEditText.text.toString().toDouble()
        }
        if (wastePriceEditText.text.toString() != "") {
            wastePrice = wastePriceEditText.text.toString().toDouble()
        }

    }

    private fun listenTextChange() {
        wasteTypeEditText.doOnTextChanged { text, start, before, count ->
            wasteType = text.toString()
        }
        wasteDescEditText.doOnTextChanged { text, start, before, count ->
            wasteDesc = text.toString()
        }
        weightEditText.doOnTextChanged { text, start, before, count ->
            if (weightEditText.text.toString() != "") {
                wasteWeight = text.toString().toDouble()
            }
        }
        wastePriceEditText.doOnTextChanged { text, start, before, count ->
            if (wastePriceEditText.text.toString() != "") {
                wastePrice = text.toString().toDouble()
            }
        }
    }

    private fun checkIsLoggedIn() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this@AddWasteActivity, LoginActivity::class.java))
            finish()
        }
    }


}

