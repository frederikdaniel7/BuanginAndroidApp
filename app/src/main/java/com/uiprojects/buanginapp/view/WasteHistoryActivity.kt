package com.uiprojects.buanginapp.view

import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.uiprojects.buanginapp.R
import com.uiprojects.buanginapp.databinding.ActivityWasteHistoryBinding
import com.uiprojects.buanginapp.model.Waste


class WasteHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWasteHistoryBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var listWaste: ArrayList<Waste>
    private lateinit var adapterHistory: WasteListAdapter
    private var isScrolling = false
    private var isLastItemReached = false
    private val limit = 10;
    private lateinit var lastVisible : DocumentSnapshot
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWasteHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val recyclerView = binding.wasteHistoryRecyclerView
        val linearLayoutLoading = binding.linearLayoutLoading
        auth = Firebase.auth
        recyclerView.layoutManager = LinearLayoutManager(this)
        listWaste = arrayListOf()
        adapterHistory = WasteListAdapter(listWaste)
        recyclerView.adapter = adapterHistory
        db = FirebaseFirestore.getInstance()
        val wasteRef = db.collection("wastes").whereEqualTo("userId", auth.currentUser!!.uid)
        val query = wasteRef.orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    val wasteModel: Waste = document.toObject(Waste::class.java)
                    listWaste.add(wasteModel)
                }
                adapterHistory.notifyDataSetChanged()

                if (task.result.size() > 0) {
                    lastVisible = task.result.documents[task.result.size() - 1]
                } else {
                    isLastItemReached = true
                }

                val onScrollListener: RecyclerView.OnScrollListener =
                    object : RecyclerView.OnScrollListener() {
                        override fun onScrollStateChanged(
                            recyclerView: RecyclerView,
                            newState: Int
                        ) {
                            super.onScrollStateChanged(recyclerView, newState)
                            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                isScrolling = true
                            }
                        }

                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                            val firstVisibleItemPosition =
                                linearLayoutManager!!.findFirstVisibleItemPosition()
                            val visibleItemCount = linearLayoutManager.childCount
                            val totalItemCount = linearLayoutManager.itemCount

                            if (isScrolling && firstVisibleItemPosition + visibleItemCount == totalItemCount && !isLastItemReached) {
                                linearLayoutLoading.visibility = View.VISIBLE
                                isScrolling = false

                                val nextQuery = wasteRef.orderBy("createdAt", Query.Direction.DESCENDING)
                                    .startAfter(lastVisible)
                                    .limit(limit.toLong())

                                nextQuery.get().addOnCompleteListener { t ->
                                    linearLayoutLoading.visibility = View.GONE
                                    if (t.isSuccessful) {
                                        if (t.result.size() > 0) {
                                            for (d in t.result) {
                                                val wasteModel: Waste = d.toObject(Waste::class.java)
                                                listWaste.add(wasteModel)
                                            }
                                            adapterHistory.notifyDataSetChanged()
                                            lastVisible = t.result.documents[t.result.size() - 1]
                                            if (t.result.size() < limit) {
                                                isLastItemReached = true
                                            }
                                        } else {
                                            // No more documents, mark as last item reached
                                            isLastItemReached = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                recyclerView.addOnScrollListener(onScrollListener)
            }
        }
    }
}