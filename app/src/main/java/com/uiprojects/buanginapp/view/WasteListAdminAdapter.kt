package com.uiprojects.buanginapp.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.uiprojects.buanginapp.R
import com.uiprojects.buanginapp.databinding.ItemWasteReportAdminBinding
import com.uiprojects.buanginapp.databinding.ItemWasteReportBinding
import com.uiprojects.buanginapp.model.Waste
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WasteListAdminAdapter(private val wasteList: MutableList<Waste>) :
    RecyclerView.Adapter<WasteListAdminAdapter.ViewHolder>() {

    class ViewHolder(val itemWasteReportBinding: ItemWasteReportAdminBinding) :
        RecyclerView.ViewHolder(itemWasteReportBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemWasteReportAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return wasteList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val locale = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(locale)
        holder.itemWasteReportBinding.cardTrashImage.setImageResource(R.drawable.outline_recycle)
        val waste: Waste = wasteList[position]
        holder.itemWasteReportBinding.cardWasteTypeTextView.text = waste.name
        val wasteWeight = waste.weight.toString() + " Kg"
        holder.itemWasteReportBinding.cardWeightTextView.text = wasteWeight
        val wasteCreator = waste.userName
        val createdAtTime  = waste.createdAt
        val milliseconds = createdAtTime!!.seconds * 1000 + createdAtTime.nanoseconds / 1000000
        val sdf = SimpleDateFormat("d MMMM yyyy")
        val netDate = Date(milliseconds)
        val date = sdf.format(netDate).toString()
        holder.itemWasteReportBinding.cardDateTextView.text = date.toString()
        holder.itemWasteReportBinding.cardUsernameTextView.text = wasteCreator
        val wasteRevenue = waste.revenue
        if (wasteRevenue!= null){
            holder.itemWasteReportBinding.wastePriceTextView.text = wasteRevenue.toString()
        } else if (wasteRevenue == 0.0){
            holder.itemWasteReportBinding.wastePriceTextView.text = "DITUKAR"
        }
        else {
            holder.itemWasteReportBinding.wastePriceTextView.text = "DITUKAR"
        }
    }
}