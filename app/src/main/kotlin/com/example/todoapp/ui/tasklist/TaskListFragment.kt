package com.example.todoapp.ui.tasklist

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.todoapp.data.model.Task
import com.example.todoapp.data.repository.TaskRepository
import com.example.todoapp.viewmodel.TaskViewModelFactory
import com.example.todoapp.databinding.FragmentTaskListBinding
import com.example.todoapp.databinding.LayoutMenuFilterBinding
import com.example.todoapp.databinding.LayoutMenuSortBinding
import com.example.todoapp.di.AppDatabaseProvider
import com.example.todoapp.ui.common.TaskAdapter
import com.example.todoapp.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar


@AndroidEntryPoint
class TaskListFragment : Fragment() {

    // ViewBinding for the fragment's layout
    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    // Adapter for RecyclerView to display task list
    private lateinit var adapter: TaskAdapter

    // ViewModel instance to manage UI-related data
    private lateinit var viewModel: TaskViewModel

    // Holds the current list of tasks displayed (after filtering, sorting, searching)
    private var currentDisplayedTasks = listOf<Task>()

    // Inflate the fragment layout using ViewBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Called immediately after onCreateView; set up ViewModel, RecyclerView, and UI observers here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModel()         // Initialize ViewModel
        setupRecyclerView()      // Setup RecyclerView with adapter and swipe gestures
        observeTasks()           // Start observing tasks from ViewModel

        setupUI()                // Setup UI event listeners (buttons, search, etc)
    }

    // Initialize ViewModel using a factory, passing repository from database
    private fun setupViewModel() {
        val database = AppDatabaseProvider.getDatabase(requireContext())
        val repository = TaskRepository(database.taskDao())
        val factory = TaskViewModelFactory(repository)
        // Get ViewModel scoped to activity so shared between fragments
        viewModel = ViewModelProvider(requireActivity(), factory)[TaskViewModel::class.java]
    }

    // Initialize RecyclerView adapter and attach swipe-to-delete behavior
    private fun setupRecyclerView() {
        // Pass a lambda to handle clicks on individual tasks (navigate to edit screen)
        adapter = TaskAdapter { task ->
            val action = TaskListFragmentDirections.actionTaskListFragmentToAddEditFragment(task.id)
            findNavController().navigate(action)
        }
        binding.recyclerViewTasks.adapter = adapter

        setupSwipeToDelete()
    }

    // Setup swipe gestures for RecyclerView items to delete tasks with undo option
    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false  // Disable move support

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val taskToDelete = adapter.currentList[position]
                // Instruct ViewModel to delete task
                viewModel.deleteTask(taskToDelete)

                // Show Snackbar with Undo action
                Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        viewModel.undoDelete(taskToDelete)  // Undo deletion
                    }
                    .setActionTextColor(Color.BLACK)
                    .also { snackbar ->
                        snackbar.view.setBackgroundColor(Color.WHITE)  // Set Snackbar background color
                    }
                    .show()
            }
        }
        // Attach ItemTouchHelper to RecyclerView for swipe functionality
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerViewTasks)
    }

    // Observe task data from ViewModel and update RecyclerView whenever tasks change
    private fun observeTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.allTasks.collect { tasks ->
                    currentDisplayedTasks = tasks  // Update current displayed list
                    adapter.submitList(currentDisplayedTasks) // Submit new list to adapter
                }
            }
        }
    }

    // Setup UI event listeners like FAB click, sort/filter button clicks, and search input
    private fun setupUI() {
        // Navigate to Add Task screen when FAB is clicked
        binding.fabAddTask.setOnClickListener {
            val action = TaskListFragmentDirections.actionTaskListFragmentToAddEditFragment()
            findNavController().navigate(action)
        }

        // Show sorting popup menu when sort icon clicked
        binding.ivSort.setOnClickListener {
            showPopupMenuSort(it)
        }

        // Show filtering popup menu when filter icon clicked
        binding.ivFilter.setOnClickListener {
            showPopupMenuFilter(it)
        }

        // Listen for text changes in search input and filter the task list accordingly
        binding.edtInputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterWithSearch(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Filter the displayed task list based on search query (case-insensitive)
    private fun filterWithSearch(query: String) {
        val filtered = if (query.isEmpty()) {
            currentDisplayedTasks
        } else {
            currentDisplayedTasks.filter {
                it.title?.contains(query, ignoreCase = true) == true
            }
        }
        adapter.submitList(filtered) // Submit filtered list to adapter
    }

    // Show a popup menu to sort the task list alphabetically A-Z or Z-A
    private fun showPopupMenuSort(anchor: View) {
        // Inflate custom popup layout for sorting options
        val popupBinding = LayoutMenuSortBinding.inflate(LayoutInflater.from(requireContext()))
        val popup = PopupWindow(popupBinding.root, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.elevation = 100f

        // Sort ascending (A to Z) when clicked
        popupBinding.lnAZ.setOnClickListener {
            val sorted = currentDisplayedTasks.sortedBy { it.title }
            adapter.submitList(sorted)
            popup.dismiss()
        }

        // Sort descending (Z to A) when clicked
        popupBinding.lnZA.setOnClickListener {
            val sorted = currentDisplayedTasks.sortedByDescending { it.title }
            adapter.submitList(sorted)
            popup.dismiss()
        }

        // Display the popup anchored to the sort icon
        showSmartPopup(popup, anchor)
    }

    // Show a popup menu to filter tasks by priority (All, Low, Medium, High)
    private fun showPopupMenuFilter(anchor: View) {
        // Inflate custom popup layout for filtering options
        val popupBinding = LayoutMenuFilterBinding.inflate(LayoutInflater.from(requireContext()))
        val popup = PopupWindow(popupBinding.root, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.elevation = 100f

        // Show all tasks when "All" selected
        popupBinding.lnAll.setOnClickListener {
            adapter.submitList(currentDisplayedTasks)
            popup.dismiss()
        }

        // Filter low priority tasks (priority == 0)
        popupBinding.lnLow.setOnClickListener {
            val filtered = currentDisplayedTasks.filter { it.priority == 0 }
            adapter.submitList(filtered)
            popup.dismiss()
        }

        // Filter medium priority tasks (priority == 1)
        popupBinding.lnMedium.setOnClickListener {
            val filtered = currentDisplayedTasks.filter { it.priority == 1 }
            adapter.submitList(filtered)
            popup.dismiss()
        }

        // Filter high priority tasks (priority == 2)
        popupBinding.lnHigh.setOnClickListener {
            val filtered = currentDisplayedTasks.filter { it.priority == 2 }
            adapter.submitList(filtered)
            popup.dismiss()
        }

        // Display the popup anchored to the filter icon
        showSmartPopup(popup, anchor)
    }

    // Helper function to smartly position the popup above or below the anchor view
    private fun showSmartPopup(popup: PopupWindow, anchor: View) {
        val location = IntArray(2)
        anchor.getLocationOnScreen(location) // Get anchor position on screen
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        anchor.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popup.contentView.measuredHeight

        // If popup would go beyond screen bottom, show above the anchor; else show below
        if (location[1] + anchor.height + popupHeight > screenHeight) {
            popup.showAsDropDown(anchor, -350, -(popupHeight + anchor.height), Gravity.NO_GRAVITY)
        } else {
            popup.showAsDropDown(anchor, -350, 30, Gravity.NO_GRAVITY)
        }
    }

    // Clean up view binding reference when the fragment view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
