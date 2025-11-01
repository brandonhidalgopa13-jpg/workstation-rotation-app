package com.workstation.rotation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.databinding.ItemOnboardingPageBinding
import com.workstation.rotation.models.OnboardingPage

/**
 * Adaptador para las páginas del onboarding
 */
class OnboardingAdapter(
    private val pages: List<OnboardingPage>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    class OnboardingViewHolder(
        private val binding: ItemOnboardingPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(page: OnboardingPage) {
            binding.apply {
                tvTitle.text = page.title
                tvDescription.text = page.description
                ivIcon.setImageResource(page.iconRes)
                
                // Aplicar color de fondo
                root.setBackgroundColor(
                    ContextCompat.getColor(root.context, page.backgroundColor)
                )
            }
        }
    }
}