package com.example.tasks.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.tasks.R;
import com.example.tasks.entities.Priority;
import com.example.tasks.entities.Response;
import com.example.tasks.entities.Task;
import com.example.tasks.service.constants.TaskConstants;
import com.example.tasks.util.DateHandler;
import com.example.tasks.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private final ViewHolder mViewHolder = new ViewHolder();
    private TaskViewModel mViewModel;
    private int mTaskId = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        // Botão de voltar nativo
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Mapeamento de elementos
        this.componentMapping();

        // ViewModel
        this.mViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Listeners
        this.setListeners();

        // Cria observadores
        this.loadObservers();
        this.mViewModel.getList();
        this.loadData();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_date) {
            this.showDatePicker();
        } else if (id == R.id.button_save) {
            this.handleSave();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        final SimpleDateFormat mFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth);

        String date = mFormat.format(c.getTime());
        this.mViewHolder.buttonDate.setText(date);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            this.mTaskId = bundle.getInt(TaskConstants.BUNDLE.TASKID);
            this.mViewModel.get(this.mTaskId);
            mViewHolder.buttonSave.setText(this.getText(R.string.update_task));
        }
    }

    private void setListeners() {
        this.mViewHolder.buttonDate.setOnClickListener(this);
        this.mViewHolder.buttonSave.setOnClickListener(this);
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void handleSave() {
        Task task = new Task();
        task.setId(this.mTaskId);
        task.setDescription(this.mViewHolder.editDescription.getText().toString());
        task.setComplete(this.mViewHolder.checkComplete.isChecked());
        task.setDueDate(this.mViewHolder.buttonDate.getText().toString());
        task.setPriorityId(((Priority)this.mViewHolder.spinnerPriority.getSelectedItem()).getId());

        this.mViewModel.save(task);
    }

    private void componentMapping() {
        this.mViewHolder.editDescription = findViewById(R.id.edit_description);
        this.mViewHolder.spinnerPriority = findViewById(R.id.spinner_priority);
        this.mViewHolder.checkComplete = findViewById(R.id.check_complete);
        this.mViewHolder.buttonDate = findViewById(R.id.button_date);
        this.mViewHolder.buttonSave = findViewById(R.id.button_save);
    }

    /**
     * Observadores
     */
    private void loadObservers() {
        this.mViewModel.listPriority.observe(this, new Observer<List<Priority>>() {
            @Override
            public void onChanged(List<Priority> list) {
                setSpinnerData(list);
            }
        });

        this.mViewModel.task.observe(this, new Observer<Task>() {
            @Override
            public void onChanged(Task task) {
                mViewHolder.editDescription.setText(task.getDescription());
                mViewHolder.buttonDate.setText(DateHandler.format(task.getDueDate()));
                mViewHolder.checkComplete.setChecked(task.isComplete());
                mViewHolder.spinnerPriority.setSelection(getIndex(task));
            }
        });

        this.mViewModel.taskResponse.observe(this, new Observer<Response>() {
            @Override
            public void onChanged(Response response) {
                if (response.isSuccess()) {
                    if (mTaskId == 0) {
                        toast(getApplication().getString(R.string.task_created));
                    } else {
                        toast(getApplication().getString(R.string.task_updated));
                    }
                    finish();
                } else {
                    toast(response.getMessage());
                }
            }
        });
    }

    private int getIndex(Task task) {
        // Pensar se realmente essa é a melhor maneira?
        Priority priority = new Priority();
        priority.setId(task.getPriorityId());
        @SuppressWarnings("unchecked")
        ArrayAdapter<Priority> tempArrayAdapter =
        (ArrayAdapter<Priority>) mViewHolder.spinnerPriority.getAdapter();
        return tempArrayAdapter.getPosition(priority);
    }

    private void setSpinnerData(List<Priority> list) {
        ArrayAdapter<Priority> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, list);
        mViewHolder.spinnerPriority.setAdapter(adapter);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * ViewHolder
     */
    private static class ViewHolder {
        EditText editDescription;
        Spinner spinnerPriority;
        CheckBox checkComplete;
        Button buttonDate, buttonSave;
    }
}