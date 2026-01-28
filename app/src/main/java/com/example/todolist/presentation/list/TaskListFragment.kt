package com.example.todolist.presentation.list

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.MainNavigator
import com.example.todolist.R
import com.example.todolist.ToDoApp
import com.example.todolist.databinding.FragmentTaskListBinding
import com.example.todolist.domain.repository.TaskSort
import kotlinx.coroutines.launch
import kotlin.getValue

class TaskListFragment : Fragment(R.layout.fragment_task_list) {
    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskListViewModel by viewModels {
        (requireActivity().application as ToDoApp).appComponent.taskListViewModelFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTaskListBinding.bind(view)

        val navigator = activity as? MainNavigator
        val component = (requireActivity().application as ToDoApp).appComponent

        val adapter = TaskAdapter(
            onClick = { task -> navigator?.openTask(task.id) },
            dateFormatter = component.dateFormatter()
        )
        binding.tasksRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.tasksRecyclerView.adapter = adapter
        binding.tasksRecyclerView.setHasFixedSize(true)

        binding.sortToggle.check(binding.sortByCreatedAtButton.id)
        binding.sortToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                binding.sortByCreatedAtButton.id -> viewModel.setSort(TaskSort.CREATED_AT_DESC)
                binding.sortByPriorityButton.id -> viewModel.setSort(TaskSort.PRIORITY_DESC)
            }
        }

        binding.searchEditText.doAfterTextChanged { text ->
            viewModel.setQuery(text?.toString().orEmpty())
        }

        binding.addTaskFab.setOnClickListener {
            navigator?.openNewTask()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tasks.collect { list ->
                        adapter.submitList(list)
                        binding.emptyTextView.visibility =
                            if (list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = TaskListFragment()
    }
}
