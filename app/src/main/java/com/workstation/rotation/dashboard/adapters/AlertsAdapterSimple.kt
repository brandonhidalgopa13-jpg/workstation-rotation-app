package com.workstation.rotation.dashboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.dashboard.models.AlertItem

/**
 * Versión simplificada del AlertsAdapter para el dashboard ejecutivo
 * TODO: Reemplazar con la versión completa cuando se tenga el layout correcto
 */
class AlertsAdapterSimple(
    private val onAlertClick: (AlertItem) -> Unit,
    private val onAlertDismiss: (AlertItem) -> Unit
) : RecyclerView.Adapter<AlertsAdapterSimple.AlertViewHolder>() {
    
    private var alerts: List<AlertItem> = emptyList()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return AlertViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alerts[position])
    }
    
    override fun getItemCount(): Int = alerts.size
    
    fun updateAlerts(newAlerts: List<AlertItem>) {
        alerts = newAlerts
        notifyDataSetChanged()
    }
    
    inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)
        
        fun bind(alert: AlertItem) {
            text1.text = alert.title
            text2.text = alert.description
            
            itemView.setOnClickListener {
                onAlertClick(alert)
            }
        }
    }
}