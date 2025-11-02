package com.workstation.rotation.analytics.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.analytics.adapters.PredictionsAdapter
import com.workstation.rotation.analytics.models.RotationPrediction

/**
 * Fragment para mostrar predicciones de rotación
 */
class PredictionsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PredictionsAdapter
    private var predictions: List<RotationPrediction> = emptyList()

    companion object {
        fun newInstance(predictions: List<RotationPrediction>): PredictionsFragment {
            val fragment = PredictionsFragment()
            fragment.predictions = predictions
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_predictions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView(view)
        loadPredictions()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerPredictions)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PredictionsAdapter()
        recyclerView.adapter = adapter
    }

    private fun loadPredictions() {
        adapter.updatePredictions(predictions)
    }
}