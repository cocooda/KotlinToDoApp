package com.example.todoapp.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.model.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onItemClick: (Task) -> Unit,     // Click for edit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback()) {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)

        fun bind(task: Task) {
            val context = itemView.context
            tvTitle.text = task.title

            if (task.dueDate != null) {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tvDueDate.text = context.getString(R.string.due_label, sdf.format(Date(task.dueDate)))
            } else {
                tvDueDate.text = context.getString(R.string.due_no_date)
            }

            tvPriority.text = when (task.priority) {
                0 -> "Priority: Low"
                1 -> "Priority: Medium"
                2 -> "Priority: High"
                else -> "Priority: Unknown"
            }

            // Click for edit
            itemView.setOnClickListener {
                onItemClick(task)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem == newItem
    }
}
