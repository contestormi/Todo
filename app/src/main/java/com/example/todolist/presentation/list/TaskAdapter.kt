package com.example.todolist.presentation.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.data.formatter.DateFormatter
import com.example.todolist.databinding.ItemTaskBinding
import com.example.todolist.domain.model.Priority
import com.example.todolist.domain.model.Task

class TaskAdapter(
    private val onClick: (Task) -> Unit,
    private val dateFormatter: DateFormatter
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(Diff) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskAdapter.TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding, onClick, dateFormatter)
    }

    override fun onBindViewHolder(
        holder: TaskAdapter.TaskViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(
        private val binding: ItemTaskBinding,
        private val onClick: (Task) -> Unit,
        private val dateFormatter: DateFormatter,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Task) {
            val context = binding.root.context

            binding.titleTextView.text = item.title
            binding.statusTextView.text =
                if (item.isDone) context.getString(R.string.done) else context.getString(
                    R.string.inWork
                )

            val priorityText = when (item.priority) {
                Priority.LOW -> context.getString(R.string.priorityLow)
                Priority.MEDIUM -> context.getString(R.string.priorityMedium)
                Priority.HIGH -> context.getString(R.string.priorityHigh)
            }

            val dueText = dateFormatter.formatOrNull(item.dueAtMillis) ?: "—"

            binding.metaTextView.text =
                context.getString(
                    R.string.priorityAndDue,
                    priorityText,
                    dueText
                )

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem == newItem
    }
}