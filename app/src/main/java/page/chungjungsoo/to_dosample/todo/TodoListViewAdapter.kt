package page.chungjungsoo.to_dosample.todo

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import page.chungjungsoo.to_dosample.R
import java.text.SimpleDateFormat
import java.util.*


class TodoListViewAdapter (context: Context, var resource: Int, var items: MutableList<Todo> ) : ArrayAdapter<Todo>(context, resource, items){
    private lateinit var db: TodoDatabaseHelper

    override fun getView(position: Int, convertView: View?, p2: ViewGroup): View {
        val layoutInflater : LayoutInflater = LayoutInflater.from(context)
        val view : View = layoutInflater.inflate(resource , null )
        val title : TextView = view.findViewById(R.id.listTitle)
        val description : TextView = view.findViewById(R.id.listDesciption)
        var dateDisplay : TextView = view.findViewById(R.id.dateDisplay)
        val edit : Button = view.findViewById(R.id.editBtn)
        val delete : Button = view.findViewById(R.id.delBtn)

        db = TodoDatabaseHelper(this.context)

        // Get to-do item
        var todo = items[position]

        // Load title and description to single ListView item
        title.text = todo.title
        description.text = todo.description
        dateDisplay.text = todo.date
        if (todo.finished) {
            title.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }
        else{
            title.paintFlags = 0
        }

        // OnClick Listener for edit button on every ListView items
        edit.setOnClickListener {
            // Very similar to the code in MainActivity.kt
            val builder = AlertDialog.Builder(this.context)
            val dialogView = layoutInflater.inflate(R.layout.add_todo_dialog, null)
            val titleToAdd = dialogView.findViewById<EditText>(R.id.todoTitle)
            val descriptionToAdd = dialogView.findViewById<EditText>(R.id.todoDescription)
            val dateToAdd = dialogView.findViewById<TextView>(R.id.selectedDate)
            val timeToAdd = dialogView.findViewById<TextView>(R.id.selectedTime)
            val finToAdd = dialogView.findViewById<CheckBox>(R.id.finishedCheckbox)
            val ime = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            val cal = Calendar.getInstance()
            val selectedDate = dialogView.findViewById<TextView>(R.id.selectedDate)
            val selectedTime = dialogView.findViewById<TextView>(R.id.selectedTime)
            val setDateBtn = dialogView.findViewById<Button>(R.id.setDateBtn)
            val setTimeBtn = dialogView.findViewById<Button>(R.id.setTimeBtn)
            val finishedBox = dialogView.findViewById<CheckBox>(R.id.finishedCheckbox)
//            var finished : Boolean = false

            fun setDate(y:Int,m:Int,d:Int) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                cal.set(Calendar.YEAR,y)
                cal.set(Calendar.MONTH,m)
                cal.set(Calendar.DATE,d)
                selectedDate.text = dateFormat.format(cal.time)
            }
            fun setTime(H:Int,m:Int){
                val timeFormat = SimpleDateFormat("HH:mm")
                cal.set(Calendar.HOUR,H)
                cal.set(Calendar.MINUTE,m)
                selectedTime.text = timeFormat.format(cal.time)
            }

            setDateBtn.setOnClickListener {
                DatePickerDialog(this.context, { _, y, m, d -> setDate(y,m,d) }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)).show()
            }
            setTimeBtn.setOnClickListener {
                TimePickerDialog(this.context, { _, h, m -> setTime(h,m) }, cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), true).show()
            }
//            finishedBox.setOnClickListener{
//                finished = finishedBox.isChecked
//            }

            titleToAdd.setText(todo.title)
            descriptionToAdd.setText(todo.description)
            dateToAdd.text = todo.date
            timeToAdd.text = todo.time
            finToAdd.isChecked = todo.finished
            titleToAdd.requestFocus()
            ime.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

            builder.setView(dialogView)
                .setPositiveButton("수정") { _, _ ->
                    val tmp = Todo(
                        titleToAdd.text.toString(),
                        descriptionToAdd.text.toString(),
                        dateToAdd.text.toString(),
                        timeToAdd.text.toString(),
                        finToAdd.isChecked
                    )

                    val result = db.updateTodo(tmp, position)
                    if (result) {
                        todo.title = titleToAdd.text.toString()
                        todo.description = descriptionToAdd.text.toString()
                        todo.date = dateToAdd.text.toString()
                        todo.time = timeToAdd.text.toString()
                        todo.finished = finToAdd.isChecked
                        notifyDataSetChanged()
                        ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                    }
                    else {
                        Toast.makeText(this.context, "수정 실패! :(", Toast.LENGTH_SHORT).show()
                        notifyDataSetChanged()
                    }
                }
                .setNegativeButton("취소") {_, _ ->
                    // Cancel Btn. Do nothing. Close keyboard.
                    ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                }
                .show()
        }

        // OnClick Listener for X(delete) button on every ListView items
        delete.setOnClickListener {
            val result = db.delTodo(position)
            if (result) {
                items.removeAt(position)
                notifyDataSetChanged()
            }
            else {
                Toast.makeText(this.context, "삭제 실패! :(", Toast.LENGTH_SHORT).show()
                notifyDataSetChanged()
            }
        }

        return view
    }

}