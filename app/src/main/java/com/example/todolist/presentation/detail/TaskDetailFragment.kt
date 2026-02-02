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
import com.example.todolist.presentation.model.TaskFormData
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch

class TaskDetailFragment : Fragment(R.layout.fragment_task_detail) {
    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private val taskId: Long? by lazy {
        val id = requireArguments().getLong(ARG_TASK_ID, -1L)
        if (id == -1L) null else id
    }

    private val viewModel: TaskDetailViewModel by viewModels {
        val component = (requireActivity().application as ToDoApp).appComponent
        component.taskDetailViewModelFactory().create(taskId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTaskDetailBinding.bind(view)

        setupPriorityDropdown()
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.screenTitleTextView.text =
            if (taskId == null) getString(R.string.newTask) else getString(R.string.task)
        binding.deleteButton.visibility =
            if (taskId == null) View.GONE else View.VISIBLE

        binding.dueDateButton.setOnClickListener { openDatePicker() }
        binding.clearDueDateButton.setOnClickListener { viewModel.setDueAt(null) }

        binding.saveButton.setOnClickListener { onSaveClick() }
        binding.deleteButton.setOnClickListener { onDeleteClick() }
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun onSaveClick() {
        val formData = TaskFormData(
            title = binding.titleEditText.text?.toString().orEmpty(),
            description = binding.descriptionEditText.text?.toString().orEmpty(),
            priority = viewModel.selectedPriority.value,
            isDone = binding.doneCheckBox.isChecked
        )
        viewModel.save(formData)
    }

    private fun onDeleteClick() {
        viewModel.delete()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.initialTaskToBind.collect { task ->
                        if (task == null) return@collect
                        binding.titleEditText.setText(task.title)
                        binding.descriptionEditText.setText(task.description)
                        setPriorityToUi(task.priority)
                        binding.doneCheckBox.isChecked = task.isDone
                        viewModel.markInitialDataBound()
                    }
                }
                launch {
                    viewModel.formattedDueDate.collect { formattedDate ->
                        binding.dueDateButton.text =
                            if (formattedDate == null) getString(R.string.pick_due_date)
                            else getString(R.string.due, formattedDate)
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is TaskDetailEvent.EmptyTitleError ->
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_empty_title),
                                    Toast.LENGTH_SHORT
                                ).show()
                            is TaskDetailEvent.Saved ->
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.saved),
                                    Toast.LENGTH_SHORT
                                ).show()
                            is TaskDetailEvent.Deleted ->
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.deleted),
                                    Toast.LENGTH_SHORT
                                ).show()
                            is TaskDetailEvent.NavigateBack ->
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
        }
    }

    private fun setupPriorityDropdown() {
        val priorities = listOf(Priority.LOW, Priority.MEDIUM, Priority.HIGH)
        val items = priorities.map { priority ->
            getString(
                when (priority) {
                    Priority.LOW -> R.string.priorityLow
                    Priority.MEDIUM -> R.string.priorityMedium
                    Priority.HIGH -> R.string.priorityHigh
                }
            )
        }
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1, items
        )
        binding.priorityAutoComplete.setAdapter(adapter)
        
        binding.priorityAutoComplete.setOnItemClickListener { _, _, position, _ ->
            viewModel.setSelectedPriority(priorities[position])
        }

        if (taskId == null) {
            viewModel.setSelectedPriority(Priority.MEDIUM)
            binding.priorityAutoComplete.setText(
                getString(R.string.priorityMedium), false
            )
        }
    }

    private fun setPriorityToUi(priority: Priority) {
        viewModel.setSelectedPriority(priority)
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
        private const val NEW_TASK_MARKER = -1L

        fun newInstance(taskId: Long?): TaskDetailFragment =
            TaskDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_TASK_ID, taskId ?: NEW_TASK_MARKER)
                }
            }
    }
}