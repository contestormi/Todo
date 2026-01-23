package com.example.todolist.presentation.detail

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.todolist.R
import com.example.todolist.ToDoApp
import com.example.todolist.databinding.FragmentTaskDetailBinding
import com.example.todolist.domain.model.Priority
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TaskDetailFragment : Fragment(R.layout.fragment_task_detail) {
    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private val taskId: Long by lazy { requireArguments().getLong(ARG_TASK_ID) }

    private val viewModel: TaskDetailViewModel by viewModels {
        TaskDetailViewModel.Factory(
            taskId = taskId,
            repository = (requireActivity().application as ToDoApp).appContainer.taskRepository
        )
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        .withZone(ZoneId.systemDefault())

    private var hasBoundInitialData: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTaskDetailBinding.bind(view)

        setupPriorityDropdown()

        binding.screenTitleTextView.text =
            if (taskId == 0L) getString(R.string.newTask) else getString(R.string.task)
        binding.deleteButton.visibility =
            if (taskId == 0L) View.GONE else View.VISIBLE
        binding.dueDateButton.setOnClickListener { openDatePicker() }
        binding.clearDueDateButton.setOnClickListener { viewModel.setDueAt(null) }

        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text?.toString().orEmpty()
            if (title.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_empty_title),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val description =
                binding.descriptionEditText.text?.toString().orEmpty()
            val priority = priorityFromUi()
            val isDone = binding.doneCheckBox.isChecked

            viewModel.save(title, description, priority, isDone) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.saved), Toast.LENGTH_SHORT
                ).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }


        }

        binding.deleteButton.setOnClickListener {
            viewModel.delete {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.deleted), Toast.LENGTH_SHORT
                ).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.task.collect { task ->
                        if (task == null || hasBoundInitialData) return@collect
                        hasBoundInitialData = true
                        binding.titleEditText.setText(task.title)
                        binding.descriptionEditText.setText(task.description)
                        setPriorityToUi(task.priority)
                        binding.doneCheckBox.isChecked = task.isDone
                    }
                }
                launch {
                    viewModel.dueAtMillis.collect { due ->
                        binding.dueDateButton.text =
                            if (due == null) getString(R.string.pick_due_date)
                            else getString(
                                R.string.due, dateFormatter.format(
                                    Instant.ofEpochMilli(
                                        due
                                    )
                                )
                            )
                    }
                }
            }
        }
    }

    private fun setupPriorityDropdown() {
        val items = listOf(
            getString(R.string.priorityLow),
            getString(R.string.priorityMedium),
            getString(R.string.priorityHigh),
        )
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, items
        )
        binding.priorityAutoComplete.setAdapter(adapter)
        if (taskId == 0L) {
            binding.priorityAutoComplete.setText(
                getString(R.string.priorityMedium), false
            )
        }
    }

    private fun priorityFromUi(): Priority {
        return when (binding.priorityAutoComplete.text?.toString()) {
            getString(R.string.priorityLow) -> Priority.LOW
            getString(R.string.priorityHigh) -> Priority.HIGH
            else -> Priority.MEDIUM

        }
    }

    private fun setPriorityToUi(priority: Priority) {
        val text = when (priority) {
            Priority.LOW -> getString(R.string.priorityLow)
            Priority.MEDIUM -> getString(R.string.priorityMedium)
            Priority.HIGH -> getString(R.string.priorityHigh)
        }
        binding.priorityAutoComplete.setText(text, false)
    }

    private fun openDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.pick_due_date)).build()

        picker.addOnPositiveButtonClickListener { selection ->
            viewModel.setDueAt(selection)
        }
        picker.show(parentFragmentManager, "due_date_picker")
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_TASK_ID = "task_id"

        fun newInstance(taskId: Long): TaskDetailFragment =
            TaskDetailFragment().apply {
                arguments = Bundle().apply { putLong(ARG_TASK_ID, taskId) }
            }
    }
}