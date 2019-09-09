package com.coinninja.coinkeeper.ui.lightning.loading

import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.BaseBottomDialogFragment

class LightningLoadingOptionsDialog : BaseBottomDialogFragment() {

    val cancelButton: Button? get() = findViewById(R.id.cancel)
    val options: RecyclerView? get() = findViewById(R.id.loading_options)
    val adapter = object : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = 2

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.view.apply {
                if (position == 0) {
                    text = getString(R.string.loading_option_load_lightning)
                    setOnClickListener { activityNavigationUtil.showLoadLightningWith(context) }
                } else {
                    text = getString(R.string.loading_option_unload_lightning)
                    setOnClickListener { activityNavigationUtil.showWithdrawalLightning(context) }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(layoutInflater.inflate(R.layout.item_load_option, parent, false) as TextView)
        }

    }

    inner class ViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)

    override fun getContentViewLayoutId(): Int {
        return R.layout.dialog_loading_options
    }

    override fun onResume() {
        super.onResume()
        cancelButton?.setOnClickListener { dismiss() }
        options?.let { list ->
            val layoutManager = LinearLayoutManager(context)
            list.layoutManager = layoutManager
            list.setHasFixedSize(false)
            list.adapter = adapter
            val dividerItemDecoration = DividerItemDecoration(context, layoutManager.getOrientation())
            list.addItemDecoration(dividerItemDecoration)
        }
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    override fun configureDialog() {
        dialog?.let {
            it.window?.let { window ->
                val params = window.attributes
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                window.attributes = params
                window.decorView.setPadding(0, 0, 0, 0)
                val content = window.findViewById<FrameLayout>(android.R.id.content)
                content.minimumHeight = 0
                content.setPadding(0, 0, 0, 0)
                window.decorView.background = null
            }
        }
    }

}
