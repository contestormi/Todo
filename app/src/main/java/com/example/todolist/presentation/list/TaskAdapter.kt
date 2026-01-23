package com.example.todolist.presentation.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.databinding.ItemTaskBinding
import com.example.todolist.domain.model.Priority
import com.example.todolist.domain.model.Task
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TaskAdapter(private val onClick: (Task) -> Unit) :
    ListAdapter<Task, TaskAdapter.VH>(Diff) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskAdapter.VH {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(
        holder: TaskAdapter.VH,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemTaskBinding,
        private val onClick: (Task) -> Unit,
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

            val dueText = item.dueAtMillis?.let {
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.ofEpochMilli(it))
            } ?: "—"

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